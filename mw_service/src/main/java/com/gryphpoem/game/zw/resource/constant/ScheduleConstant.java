package com.gryphpoem.game.zw.resource.constant;

/**
 * @author QiuKun
 * @ClassName ScheduleConstant.java
 * @Description 世界进程常量
 * @date 2019年2月21日
 */
public interface ScheduleConstant {

    /**
     * 阵营补给排行榜
     */
    int SCHEDULE_RANKTYPE_CAMP_FEED = 1;

    /**
     * 积分排行榜
     */
    int SCHEDULE_RANKTYPE_INTEGRAL = 2;

    /**
     * boss伤害排行
     */
    int SCHEDULE_RANKTYPE_ATTCKBOSS = 3;

    /**
     * 限时目标: 指挥官基地提升至N级
     */
    int GOAL_COND_COMMAND_LV = 1;

    /**
     * 限时目标: N个城被攻克
     */
    int GOAL_COND_CONQUER_CITY = 2;

    /**
     * 限时目标: 部队最大战力达到, 算个人历史最高的战斗力
     */
    int GOAL_COND_FIGHT = 3;

    /**
     * 限时目标: 对防线攻击N次
     */
    int GOAL_COND_ATTACK_BOSS = 4;

    /**
     * 限时目标: 攻打盖世太保或流寇
     */
    int GOAL_COND_ATK_GESTAPO_BANDIT = 5;

    /**
     * 限时目标: 全服有N个N次觉醒英雄
     */
    int GOAL_COND_HERO_DECORATED = 6;

    /**
     * 世界进度未开始
     */
    int SCHEDULE_STATUS_NOT_YET_BEGIN = 0;

    /**
     * 世界进度进行中
     */
    int SCHEDULE_STATUS_PROGRESS = 1;

    /**
     * 世界进度已经结束
     */
    int SCHEDULE_STATUS_FINISH = 2;

    /**
     * 世界进度id: 世界boss1
     */
    int SCHEDULE_BOOS_1_ID = 5;

    /**
     * 世界进度id: 世界boss2
     */
    int SCHEDULE_BOOS_2_ID = 9;

    /**
     * 世界进度id: 柏林会战
     */
    int SCHEDULE_BERLIN_ID = 10;

    /**
     * 世界进度id: 6
     */
    int SCHEDULE_ID_6 = 6;

    /**
     * 世界进度id: 11
     */
    int SCHEDULE_ID_11 = 11;

    /**
     * 世界进度id: 12
     */
    int SCHEDULE_ID_12 = 12;

    /**
     * 世界进程id: 13
     */
    int SCHEDULE_ID_13 = 13;

    /**
     * 没有世界boss死亡,第一个世界boss都没打死的状态
     */
    int BOSS_NO_DEAD = 0;

    /**
     * 世界boss 1 死亡
     */
    int BOSS_1_DEAD = 1;

    /**
     * 世界boss 2 死亡
     */
    int BOSS_2_DEAD = 2;

    /**
     * 全局活动时间轮, 模板1
     */
    int GLOBAL_ACT_TIME_ROUND_TEMPLATE_1 = 28;

    /**
     * 全局活动时间轮, 模板2
     */
    int GLOBAL_ACT_TIME_ROUND_TEMPLATE_2 = 14;
}
