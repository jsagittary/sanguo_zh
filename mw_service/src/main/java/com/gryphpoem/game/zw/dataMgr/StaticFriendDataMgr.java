package com.gryphpoem.game.zw.dataMgr;

import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreditShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticMasterReaward;

/**
 * @ClassName StaticFriendDataMgr.java
 * @Description 加载好友师徒相关数据
 * @author QiuKun
 * @date 2017年7月4日
 */
public class StaticFriendDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 师徒积分商城商品配置表,key 商品id
    private static Map<Integer, StaticCreditShop> creditShopMap;

    // 师徒奖励配置, key 奖励id
    private static Map<Integer, StaticMasterReaward> masterReawardMap;

    public static void init() {
        Map<Integer, StaticCreditShop> shopMap = staticDataDao.selectCreditShopMap();
        StaticFriendDataMgr.creditShopMap = shopMap;

        Map<Integer, StaticMasterReaward> reawardMap = staticDataDao.selectMasterReawardMap();
        StaticFriendDataMgr.masterReawardMap = reawardMap;
    }

    public static Map<Integer, StaticCreditShop> getCreditShopMap() {
        return creditShopMap;
    }

    public static Map<Integer, StaticMasterReaward> getMasterReawardMap() {
        return masterReawardMap;
    }

    /**
     * 获取积分商品
     * 
     * @param productId
     * @return 返回null说明商品不存在
     */
    public static StaticCreditShop getCreditProduct(int productId) {
        if (creditShopMap == null) {
            return null;
        }
        return creditShopMap.get(productId);
    }

    /**
     * 获取积分奖励
     * 
     * @param reawardId
     * @return 返回null不存在奖励
     */
    public static StaticMasterReaward getReaward(int reawardId) {
        if (masterReawardMap == null) {
            return null;
        }
        return masterReawardMap.get(reawardId);
    }
}
