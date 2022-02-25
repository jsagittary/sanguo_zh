package com.gryphpoem.game.zw.dataMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticBerlinShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticMentorShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticMultcombatShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticShop;

/**
 * 商店
 * 
 * @author tyler
 *
 */
public class StaticShopDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static Map<Integer, StaticShop> shopMap;
    private static Map<Integer, List<StaticShop>> typeShopMap;
    // key:id
    private static Map<Integer, StaticBerlinShop> berlinShopMap;
    // 荣耀演练场商店 key:id
    private static Map<Integer, StaticMentorShop> mentorShopMap;
    private static Map<Integer, StaticMultcombatShop> multCombatShopMap;

    public static void init() {
        Map<Integer, StaticShop> shopMap = staticDataDao.selectShopMap();
        StaticShopDataMgr.shopMap = shopMap;

        Map<Integer, List<StaticShop>> typeShopMap = new HashMap<>();
        for (StaticShop shop : shopMap.values()) {
            List<StaticShop> list = typeShopMap.get(shop.getType());
            if (list == null) {
                list = new ArrayList<>();
                typeShopMap.put(shop.getType(), list);
            }
            list.add(shop);
        }

        StaticShopDataMgr.typeShopMap = typeShopMap;
        StaticShopDataMgr.berlinShopMap = staticDataDao.selectBerlinShopMap();
        StaticShopDataMgr.mentorShopMap = staticDataDao.selectMentorShopMap();
        StaticShopDataMgr.multCombatShopMap = staticDataDao.selectMultcombatShopMap();

    }

    public static StaticShop getShopMap(int id) {
        return shopMap.get(id);
    }

    public static List<StaticShop> getTypeShopMap(int type) {
        return typeShopMap.get(type);
    }

    public static Map<Integer, StaticBerlinShop> getBerlinShopMap() {
        return berlinShopMap;
    }

    public static Map<Integer, StaticMentorShop> getMentorShopMap() {
        return mentorShopMap;
    }

    public static Map<Integer, StaticMultcombatShop> getMultCombatShopMap() {
        return multCombatShopMap;
    }

}
