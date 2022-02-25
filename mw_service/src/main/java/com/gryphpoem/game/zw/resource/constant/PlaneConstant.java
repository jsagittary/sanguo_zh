package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-15 15:03
 * @description:
 * @modified By:
 */
public class PlaneConstant {

    /** 空军基地免费抽取配置, 格式:[[战机1卡池免费抽取时间(单位:秒)，1天可以免费抽取的次数],[抽取券配置],[抽取赠送的物品配置]] */
    public static List<List<Integer>> PLANE_SEARCH_FREE_NUM;
    /** 空军基地寻访价格配置, 格式:[[单抽价格，10抽价格],[抽取券配置],[抽取赠送的物品配置]] 填金币数 */
    public static List<List<Integer>> PLANE_SEARCH_GOLD_NUM;
    /** 空军基地限时寻访配置, 格式:[[单抽价格，10抽价格],[抽取券配置],[抽取赠送的物品配置]] 填金币数 */
    public static List<List<Integer>> PLANE_SEARCH_LIMIT_NUM;
    /** 战机属性系数, 格式:[[属性类型,系数],[属性类型,系数],...] */
    public static Map<Integer, Integer> WAR_PLANE_RADIO_NUM;
    /** 空军基地奖励时间, 格式:[单抽价格，10抽价格]*/
    public static List<Integer> PLANE_FACTORY_AWARD_TIME;
    /** 空军基地寻访抽取跑马灯配置, 抽取战机的品质 */
    public static int PLANE_SEARCH_CHAT_QUALITY;
    /** 击飞玩家获得战机, 格式:[掉落概率(单位:)，最大数量] */
    public static List<Integer> PLANE_HIT_FLY_AWARD;
    /** 低级卡池第一次抽取的飞机 */
    public static List<List<Integer>> PLANE_FIRST_SEARCH_AWARD;
    /** 战机的最大等级 */
    public static int PLANE_LEVEL_MAX;
    /** 战机高级卡池免费抽取间隔时间 */
    public static int PLANE_SUPER_SEARCH_CD;
    /** 战机技能数 */
    public static int PLANE_SKILL_LEN;
    /** 触发战机技能的兵排 */
    public static Map<Integer, Integer> LINE_PLANE_SKILL;


    /**
     * 根据寻访次数类型，获取对应的金币消耗
     * @param countType
     * @return 当找不到对应的类型时，会返回-1
     */
    public static int getHeroSearchGoldByType(int countType) {
        int gold = -1;
        if (countType == COUNT_TYPE_ONE) {
            gold = PLANE_SEARCH_GOLD_NUM.get(0).get(0);
        } else if (countType == COUNT_TYPE_TEN) {
            gold = PLANE_SEARCH_GOLD_NUM.get(0).get(1);
        }
        return gold;
    }

    /**
     * 根据寻访类型, 获取对应的抽取卷消耗
     * @param searchType
     * @return
     */
    public static List<Integer> getHeroSearchScrollByType(int searchType) {
        List<Integer> scroll = null;
        if (searchType == SEARCH_TYPE_NORMAL) {
            scroll = PLANE_SEARCH_FREE_NUM.get(1);
        } else if (searchType == SEARCH_TYPE_SUPER) {
            scroll = PLANE_SEARCH_GOLD_NUM.get(1);
        } else if (searchType == SEARCH_TYPE_LIMIT) {
            scroll = PLANE_SEARCH_LIMIT_NUM.get(1);
        }
        return scroll;
    }

    /**
     * 根据寻访类型, 获取对应的抽奖次数奖励
     * @param searchType
     * @return
     */
    public static List<Integer> getSearchlotteryAwardByType(int searchType) {
        List<Integer> award = null;
        if (searchType == SEARCH_TYPE_NORMAL) {
            award = PLANE_SEARCH_FREE_NUM.get(2);
        } else if (searchType == SEARCH_TYPE_SUPER) {
            award = PLANE_SEARCH_GOLD_NUM.get(2);
        } else if (searchType == SEARCH_TYPE_LIMIT) {
            award = PLANE_SEARCH_LIMIT_NUM.get(2);
        }
        return award;
    }

    /**
     * 根据升级类型获取战机经验道具
     * @param type
     * @return 当找不到对应的类型时, 会返回0
     */
    public static int getPlaneExpPropByType(int type) {
        int propId = 0;
        switch (type) {
            case QUICK_UP_TYPE_LOW:
                propId = PropConstant.PROP_ID_LOW_PLANE_EXP;
                break;
            case QUICK_UP_TYPE_MIDDLE:
                propId = PropConstant.PROP_ID_MIDDLE_PLANE_EXP;
                break;
            case QUICK_UP_TYPE_HIGH:
                propId = PropConstant.PROP_ID_HIGH_PLANE_EXP;
                break;
            case QUICK_UP_TYPE_TOP:
                propId = PropConstant.PROP_ID_TOP_PLANE_EXP;
                break;
        }
        return propId;
    }

    public static void loadSystem() {
        PLANE_SEARCH_FREE_NUM = SystemTabLoader.getListListIntSystemValue(SystemId.PLANE_SEARCH_FREE_NUM, "[[900,5],[4,3002,1]]");
        PLANE_SEARCH_GOLD_NUM = SystemTabLoader.getListListIntSystemValue(SystemId.PLANE_SEARCH_GOLD, "[[300,2700],[4,3003,1]]");
        WAR_PLANE_RADIO_NUM = SystemTabLoader.getMapIntSystemValue(SystemId.WAR_PLANE_RADIO_NUM, "[[1,850],[2,12500]]");
        PLANE_FACTORY_AWARD_TIME = SystemTabLoader.getListIntSystemValue(SystemId.PLANE_FACTORY_AWARD_TIME, "[604800,20]");
        PLANE_SEARCH_CHAT_QUALITY = SystemTabLoader.getIntegerSystemValue(SystemId.PLANE_SEARCH_CHAT_QUALITY, 4);
        PLANE_HIT_FLY_AWARD = SystemTabLoader.getListIntSystemValue(SystemId.PLANE_HIT_FLY_AWARD, "[3000,2]");
        PLANE_FIRST_SEARCH_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.PLANE_FIRST_SEARCH_AWARD, "[[12,10210,1]]");
        PLANE_LEVEL_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.PLANE_MAX_LEVEL, 10);
        PLANE_SUPER_SEARCH_CD = SystemTabLoader.getIntegerSystemValue(SystemId.PLANE_SUPER_SEARCH_CD, 172800);
        PLANE_SKILL_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.PLANE_SKILL_LEN, 6);
        LINE_PLANE_SKILL = SystemTabLoader.getMapIntSystemValue(SystemId.LINE_PLANE_SKILL, "[[1,1],[3,2],[5,3],[7,4]]");
        PLANE_SEARCH_LIMIT_NUM = SystemTabLoader.getListListIntSystemValue(SystemId.PLANE_SEARCH_LIMIT_NUM, "[[300,2700],[4,3003,1],[4,7002,1]]");
    }

    /**
     * 根据属性id获取战机的属性系数
     *
     * @param attrId
     * @return
     */
    public static int getPlaneRadioByAttrId(int attrId) {
        return WAR_PLANE_RADIO_NUM.getOrDefault(attrId, 0);
    }

    /**
     * 获取当天免费寻访次数
     * @param player
     * @return
     */
    public static int getPlaneFreeSearchCnt(Player player) {
        return player.getMixtureDataById(PlayerConstant.PLANE_FREE_SEARCH_CNT);
    }

    /**
     * 获取当天最后一次免费寻访CD的结束时间
     * @param player
     * @return
     */
    public static int getLastPlaneFreeSearchCDTime(Player player) {
        return player.getMixtureDataById(PlayerConstant.PLANE_LAST_FREE_SEARCH_TIME) + PlaneConstant.PLANE_SEARCH_FREE_NUM.get(0).get(0);
    }

    /**
     * 获取最后一次高级免费寻访CD的结束时间
     * @param player
     * @return
     */
    public static int getlastplaneSuperFreeSearchCDTime(Player player) {
        return player
                .getMixtureDataById(PlayerConstant.PLANE_SUPER_FREE_SEARCH_TIME) + PlaneConstant.PLANE_SUPER_SEARCH_CD;
    }


    /** 战机寻访类型：低级寻访 */
    public static final int SEARCH_TYPE_NORMAL = 1;
    /** 战机寻访类型：高级寻访 */
    public static final int SEARCH_TYPE_SUPER = 2;
    /** 战机寻访类型: 寻访奖励 */
    public static final int SEARCH_TYPE_AWARD = 3;
    /** 战机寻访类型: 限时寻访 */
    public static final int SEARCH_TYPE_LIMIT = 4;

    /** 战机寻访类型：寻访一次 */
    public static final int COUNT_TYPE_ONE = 1;
    /** 战机寻访类型：寻访10次 */
    public static final int COUNT_TYPE_TEN = 2;

    /** 战机寻访消耗类型：免费次数 */
    public static final int SEARCH_COST_FREE = 1;
    /** 战机寻访消耗类型：低级研发券或高级研发券 */
    public static final int SEARCH_COST_PROP = 2;
    /** 战机寻访消耗类型：金币 */
    public static final int SEARCH_COST_GOLD = 3;

    /** 将领快速升级类型：低级 */
    public static final int QUICK_UP_TYPE_LOW = 1;
    /** 将领快速升级类型：中级 */
    public static final int QUICK_UP_TYPE_MIDDLE = 2;
    /** 将领快速升级类型：高级 */
    public static final int QUICK_UP_TYPE_HIGH = 3;
    /** 将领快速升级类型：顶级 */
    public static final int QUICK_UP_TYPE_TOP = 4;

    /** 战机寻访结果:战机 */
    public static final int SEARCH_RESULT_PLANE = 1;
    /** 战机寻访结果:碎片 */
    public static final int SEARCH_RESULT_CHIP = 2;

    /** 战机技能:无视防御的固定值伤害 */
    public static final int PLANE_SKILL_1 = 1;
    /** 战机技能:所属将领攻击力x%的伤害 */
    public static final int PLANE_SKILL_2 = 2;
    /** 战机技能:所属将领防御力x%的伤害 */
    public static final int PLANE_SKILL_3 = 3;
    /** 战机技能:所属将领兵力x%的伤害 */
    public static final int PLANE_SKILL_4 = 4;

    /** 战机替换类型: 上阵 */
    public static final int PLANE_SWAP_TYPE_UP = 0;
    /** 战机替换类型: 下阵 */
    public static final int PLANE_SWAP_TYPE_DOWN = 1;

    /** 战机技能伤害系数 */
    public static final int PLANE_SKILL_RADIO = 3;

    /** 战机状态：空闲 */
    public static final int PLANE_STATE_IDLE = 0;
    /** 战机状态：出征 */
    public static final int PLANE_STATE_BATTLE = 1;

    /** 战机寻访首次抽取 */
    public static final int PLANE_SEARCH_FIRST = 0;
    
    /** 战机品质 */
    public static final int PLANE_QUALITY_FIVE = 5;

    /**
     * 技能临时变量
     */
    public interface SkillParam {
        /** 专业技能:最大释放次数 */
        int MAX_RELEASE_CNT = 1;
        /** 专业技能:已释放次数 */
        int RELEASE_CNT = 2;
        /** 专业技能:上次技能伤害 */
        int LAST_HURT_NUM = 3;
    }

}
