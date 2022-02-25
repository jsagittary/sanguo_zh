package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticBanditArea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticBanditDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 流寇属性, key:流寇id
    private static Map<Integer, StaticBandit> banditMap;

    // 流寇按等级分
    private static Map<Integer, List<StaticBandit>> banditLvMap;

    // 流寇分区刷新配置, key:area (key=id)
    private static Map<Integer, StaticBanditArea> banditAreaMap;
    private static Map<Integer,List<StaticBanditArea>> banditAreaMapGroup = new HashMap<>();

    public static void init() {
        Map<Integer, StaticBandit> banditMap = staticDataDao.selectBanditMap();
        StaticBanditDataMgr.banditMap = banditMap;

        List<StaticBandit> lvList;
        StaticBanditDataMgr.banditLvMap = new HashMap<>();
        for (StaticBandit staticBandit : banditMap.values()) {
            lvList = banditLvMap.get(staticBandit.getLv());
            if (null == lvList) {
                lvList = new ArrayList<>();
                banditLvMap.put(staticBandit.getLv(), lvList);
            }
            lvList.add(staticBandit);
        }

        Map<Integer, StaticBanditArea> banditAreaMap = staticDataDao.selectBanditAreaMap();
        StaticBanditDataMgr.banditAreaMap = banditAreaMap;
        banditAreaMap.values().forEach(o -> {
            List<StaticBanditArea> tmpList = banditAreaMapGroup.get(o.getAreaOrder());
            if(tmpList == null) {
                tmpList = new ArrayList<>();
                banditAreaMapGroup.put(o.getAreaOrder(),tmpList);
            }
            tmpList.add(o);
        });
    }

    public static Map<Integer, StaticBandit> getBanditMap() {
        return banditMap;
    }

    public static StaticBandit getBanditById(int banditId) {
        return banditMap.get(banditId);
    }

    public static Map<Integer, StaticBanditArea> getBanditAreaMap() {
        return banditAreaMap;
    }

    public static List<StaticBandit> getBanditByLv(int lv) {
        return banditLvMap.get(lv);
    }

    public static List<StaticBanditArea> getStaticBanditAreaByAreaOrder(int areaOrder){
        return banditAreaMapGroup.get(areaOrder);
    }
}
