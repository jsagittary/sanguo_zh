package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-26 16:55
 * @description: 教官常量类
 * @modified By:
 */
public class MentorConstant {

    /** 教官的最大等级,key: mentorType, val: maxLv */
    public static Map<Integer, Integer> MENTOR_MAX_LV = new HashMap<>();
    /** 教官的技能最大等级,key: skillType, val: maxLv */
    public static Map<Integer, Integer> MENTOR_SKILL_MAX_LV = new HashMap<>();
    /** 教官技能的开启条件,key: mentorType, val: mentorLv , val:[[pos, skillId]] */
    public static Map<Integer, Map<Integer, List<List<Integer>>>> MENTOR_SKILL_UNSEAL = new HashMap<>();
    /** 教官开启后开放教官id */
    public static List<Integer> UNSEAL_MENTOR_ID;
    /** 教官升级功能相关配置 */
    private static List<List<Integer>> MENTOR_UP_CONF;
    /** 教官升级功能, 道具消耗配置, key: mentorType, val:[mentorUP] */
    public static Map<Integer, List<Integer>> MENTOR_UP_PROP_CONF;

    public static void loadSystem() {
        UNSEAL_MENTOR_ID = SystemTabLoader.getListIntSystemValue(SystemId.UNSEAL_MENTOR_ID, "[]");
        MENTOR_UP_CONF = SystemTabLoader.getListListIntSystemValue(SystemId.MENTOR_UP_CONF, "[[]]");
        MENTOR_UP_PROP_CONF = SystemTabLoader.getMapListSystemValue(SystemId.MENTOR_UP_PROP_CONF, 0, "[[]]");
    }

    /**
     * 获取教官升级配置根据类型
     * @param type
     * @return
     */
    public static List<Integer> upConfByType(int type) {
        List<Integer> conf = null;
        if (type == MENTOR_TYPE_1) {
            conf = MENTOR_UP_CONF.get(0);
        } else if (type == MENTOR_TYPE_2) {
            conf = MENTOR_UP_CONF.get(1);
        } else if (type == MENTOR_TYPE_3) {
            conf = MENTOR_UP_CONF.get(2);
        }
        return conf;
    }

    /**
     * 获取教官升级消耗道具
     * @param type
     * @return
     */
    public static int mentorUpPropByType(int type) {
        int propId = 0;
        if (type == MentorConstant.MENTOR_TYPE_1) {
            propId = PropConstant.PROP_ID_MENTOR_EXP;
        } else if (type == MentorConstant.MENTOR_TYPE_2) {
            propId = PropConstant.PROP_ID_PLANE_MENTOR_EXP;
        }
        return propId;
    }


    /**
     * 根据教官类型获取functionId
     * @param type
     * @return
     */
    public static final int functionIdByType(int type) {
        int id = 0;
        if (type == MentorConstant.MENTOR_TYPE_1) {
            id = FunctionConstant.UNLOCK_TYPE_MENTOR;
        } else if (type == MentorConstant.MENTOR_TYPE_2) {
            id = FunctionConstant.UNLOCK_TYPE_PLANE_MENTOR;
        } else if (type == MentorConstant.MENTOR_TYPE_3) {
            id = FunctionConstant.UNLOCK_TYPE_MENTOR_3;
        }
        return id;
    }

    /**
     * 根据教官类型获取Porp消耗conf
     * @param type
     * @return
     */
    public static List<Integer> mentorUpPropConf(int type) {
        return MENTOR_UP_PROP_CONF.get(type);
    }

    /** 教官升级奖励领取状态 */
    public static final int MENTOR_UPAWARD_HAS_GAIN = 1;    // 领取
    public static final int MENTOR_UPAWARD_NOT_HAS_GAIN = 0;// 未领取

    /** 贩卖教官装备类型 */
    public static final int MENTOR_SELL_EQUIP_TYPE_1 = 1;   // 当前选中
    public static final int MENTOR_SELL_EQUIP_TYPE_2 = 2;   // 50个
    public static final int MENTOR_SELL_EQUIP_TYPE_3 = 3;   // 强制卖出全部, 未穿戴的装备

    /** 每次贩卖数量 */
    public static final int MENTOR_SELL_EQUIP_CNT = 50;


    /** 教官类型 */
    public static final int MENTOR_TYPE_1 = 1;
    public static final int MENTOR_TYPE_2 = 2;
    public static final int MENTOR_TYPE_3 = 3;
}
