package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.pojo.world.BerlinCityInfo;

/**
 * @ClassName BerlinWarConstant.java
 * @Description 柏林会战相关常量值
 * @author QiuKun
 * @date 2018年8月8日
 */
public interface BerlinWarConstant {

    /** 募兵数量 */
    int BUFF_TYPE_ARMY = 1;
    /** 燃油 */
    int BUFF_TYPE_OIL = 2;
    /** 电力 */
    int BUFF_TYPE_ELE = 3;
    /** 补给 */
    int BUFF_TYPE_FOOD = 4;
    /** 矿石 */
    int BUFF_TYPE_ORE = 5;
    /** 建筑时间 */
    int BUFF_TYPE_BUILDING_TIME = 6;
    /** 行军时间 */
    int BUFF_TYPE_MARCH_TIME = 7;
    /** 科技时间 */
    int BUFF_TYPE_TECH_TIME = 8;
    /** 立即出击最大轮数, 之前是写死的10轮, 现在改成20 */
    int IMMEDIATELY_BATTLE_MAX_ROUND = 20;

    /**
     * {@link BerlinCityInfo#getStatusTime()} key的前缀, 1-3存储的是累积的占领时间 101-103存储的是势力值
     */
    int INFLUENCE_VALUE_PREFIX = 100;


    /**
     * BerlinRoleInfo中Status的Key值
     */
    interface RoleInfo {
        /** 强袭出击次数 */
        int PRESS_CNT = 1;
        /** 将领复活次数 */
        int RESURRECTION_CNT = 2;
        /** 立即出击次数 */
        int IMMEDIATELY_CNT = 3;
    }
}
