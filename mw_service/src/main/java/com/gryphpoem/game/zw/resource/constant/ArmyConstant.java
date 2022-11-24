package com.gryphpoem.game.zw.resource.constant;

/**
 * @author TanDonghai
 * @ClassName ArmyConstant.java
 * @Description 行军常量类
 * @date 创建时间：2017年3月31日 下午4:44:23
 */
public class ArmyConstant {

    /**
     * 兵种类型：战车（步兵）
     */
    public static final int ARM1 = 1;
    /**
     * 兵种类型：坦克（骑兵）
     */
    public static final int ARM2 = 2;
    /**
     * 兵种类型：火箭（弓兵）
     */
    public static final int ARM3 = 3;

    /**
     * 行军类型：攻击流寇
     */
    public static final int ARMY_TYPE_ATK_BANDIT = 1;
    /**
     * 行军类型：攻击玩家、城战
     */
    public static final int ARMY_TYPE_ATK_PLAYER = 2;
    /**
     * 行军类型：矿点采集
     */
    public static final int ARMY_TYPE_COLLECT = 3;
    /**
     * 行军类型：阵营战
     */
    public static final int ARMY_TYPE_ATK_CAMP = 4;
    /**
     * 行军类型：城墙驻防
     */
    public static final int ARMY_TYPE_GUARD = 5;
    /**
     * 行军类型: 攻击点兵统领
     */
    public static final int ARMY_TYPE_ATK_CABINET_LEAD = 6;
    /**
     * 行军类型: 攻击流寇任务
     */
    public static final int ARMY_TYPE_ATK_CABINET_TASK = 7;
    /**
     * 行军类型: 攻击盖世太保
     */
    public static final int ARMY_TYPE_ATK_GESTAPO = 8;
    /**
     * 行军类型: 闪电战
     */
    public static final int ARMY_TYPE_LIGHTNING_WAR = 9;
    /**
     * 行军类型: 超级矿点采集
     */
    public static final int ARMY_TYPE_COLLECT_SUPERMINE = 10;
    /**
     * 行军类型: 超级矿点攻击
     */
    public static final int ARMY_TYPE_ATK_SUPERMINE = 11;
    /**
     * 行军类型: 超级矿点驻防
     */
    public static final int ARMY_TYPE_HELP_SUPERMINE = 12;
    /**
     * 行军类型: 柏林皇城战
     */
    public static final int ARMY_TYPE_BERLIN_WAR = 13;
    /**
     * 行军类型: 柏林据点战
     */
    public static final int ARMY_TYPE_BATTLE_FRONT_WAR = 14;
    /**
     * 行军类型: 匪军叛乱帮助
     */
    public static final int ARMY_TYPE_REBEL_BATTLE = 15;
    /**
     * 行军类型: 反攻德意志BOSS防守战
     */
    public static final int ARMY_TYPE_COUNTER_BOSS_DEF = 16;
    /**
     * 行军类型: 反攻德意志BOSS进攻战, 驻防
     */
    public static final int ARMY_TYPE_COUNTER_BOSS_ATK_HELP = 17;
    /**
     * 行军类型: 决战
     */
    public static final int ARMY_TYPE_DECISIVE_BATTLE = 18;
    /**
     * 行军类型: 攻打飞艇
     */
    public static final int ARMY_TYPE_ATTACK_AIRSHIP = 19;
    /**
     * 行军类型: 攻打世界boss
     */
    public static final int ARMY_TYPE_ATTACK_SCHEDULE_BOSS = 20;
    /**
     * 世界争霸 - 纽约争夺战
     */
    public static final int ARMY_TYPE_NEW_YORK_WAR = 21;
    /**
     * 战火燎原: 打城池
     */
    public static final int ARMY_TYPE_WF_ATK_CITY = 22;
    /**
     * 行军类型: 拜访圣坛
     */
    public static final int ARMY_TYPE_ALTAR = 23;
    /*** 行军类型: 王朝遗迹*/
    public static final int ARMY_TYPE_RELIC_BATTLE = 24;
    /*** 行军类型: 州郡雄踞一方*/
    public static final int ARMY_TYPE_STATE_DOMINATE_ATTACK = 25;
    /*** 行军类型: 司隶雄踞一方*/
    public static final int ARMY_TYPE_SI_LI_DOMINATE_ATTACK = 26;

    /**
     * 行军状态：空闲
     */
    public static final int ARMY_STATE_IDLE = 0;
    /**
     * 行军状态：行军
     */
    public static final int ARMY_STATE_MARCH = 1;
    /**
     * 行军状态：返回
     */
    public static final int ARMY_STATE_RETREAT = 2;
    /**
     * 行军状态：采集
     */
    public static final int ARMY_STATE_COLLECT = 3;
    /**
     * 行军状态：驻守
     */
    public static final int ARMY_STATE_GUARD = 4;
    /**
     * 行军状态：城战或阵营战
     */
    public static final int ARMY_STATE_BATTLE = 5;
    /**
     * 行军状态：盖世太保战
     */
    public static final int ARMY_GESTAPO_BATTLE = 6;
    /**
     * 行军状态：闪电战
     */
    public static final int ARMY_LIGHTNING_WAR = 7;
    /**
     * 行军状态: 柏林会战
     */
    public static final int ARMY_BERLIN_WAR = 8;
    /**
     * 行军状态: 匪军叛乱帮助
     */
    public static final int ARMY_STATE_REBEL_BATTLE = 9;
    /**
     * 行军类型: 反攻德意志BOSS防守战
     */
    public static final int ARMY_STATE_COUNTER_BOSS_DEF = 10;
    /**
     * 行军类型: 反攻德意志BOSS进攻战, 驻防
     */
    public static final int ARMY_STATE_COUNTER_BOSS_ATK_HELP = 11;
    /**
     * 行军类型: 反攻德意志BOSS进攻战, 驻防
     */
    public static final int ARMY_STATE_DECISIVE_BATTLE = 12;
    /**
     * 行军类型: 攻打飞艇等待中
     */
    public static final int ARMY_STATE_ATTACK_AIRSHIP_WAIT = 13;
    /**
     * 行军类型: 纽约争夺战(客户端占用)
     */
    public static final int ARMY_STATE_NEW_YORK_WAR = 14;
    /**
     * 行军类型: 在跨服中,其实是hero的state
     */
    public static final int ARMY_STATE_CROSS = 15;
    /**
     * 行军类型: 跨服中的复活状态
     */
    public static final int ARMY_STATE_CROSS_REVIVAL = 16;
    /**
     * 行军类型: 战火燎原城池战斗
     */
    public static final int ARMY_STATE_WAR_FIRE_CITY = 17;
    /*** 部队状态: 王朝遗迹中防守战斗*/
    public static final int ARMY_STATE_RELIC_BATTLE = 18;
    /*** 行军状态：王朝遗迹探索中 */
    public static final int ARMY_STATE_RELIC_PROBING = 19;
    /** 行军状态：州郡雄踞一方探索中 */
    public static final int ARMY_STATE_STATE_DOMINATE_HOLDER = 20;
    /** 行军状态：司隶雄踞一方探索中 */
    public static final int ARMY_STATE_SI_LI_DOMINATE_HOLDER = 21;

    /**
     * 战斗结果：平局或其他
     */
    public static final int FIGHT_RESULT_DRAW = 0;
    /**
     * 战斗结果：胜利
     */
    public static final int FIGHT_RESULT_SUCCESS = 1;
    /**
     * 战斗结果：失败
     */
    public static final int FIGHT_RESULT_FAIL = 2;

    /**
     * 行军加速类型：高级行军加速
     */
    public static final int MOVE_TYPE_1 = 1;
    /**
     * 行军加速类型：顶级行军加速
     */
    public static final int MOVE_TYPE_2 = 2;
    /**
     * 行军加速类型：行军召回
     */
    public static final int MOVE_BACK_TYPE_1 = 1;
    /**
     * 行军加速类型： 高级行军召回
     */
    public static final int MOVE_BACK_TYPE_2 = 2;

    /**
     * 属性调整进行初始化
     */
    public static final int ATTR_CHANGE_STATE_NO_DO = 0;
    /**
     * 属性调整不可被进行
     */
    public static final int ATTR_CHANGE_STATE_NO = 1;
    /**
     * 属性调整可被进行
     */
    public static final int ATTR_CHANGE_STATE_YES = 2;
    /**
     * 属性调整可被最大指挥官数量
     */
    public static final int ATTR_CHANGE_STATE_LORD_COUNT = 10;

    /**
     * 坐标驻军行军类型
     */
    public static final int GUARD_MARCH = 1000000;

}
