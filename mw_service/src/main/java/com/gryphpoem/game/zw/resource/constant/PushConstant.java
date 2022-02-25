package com.gryphpoem.game.zw.resource.constant;

/**
 * @author TanDonghai
 * @Description 消息推送相关配置常量
 * @date 创建时间：2017年9月7日 上午11:13:35
 */
public class PushConstant {

    private PushConstant() {
    }

    /**
     * 推送消息未发送过
     */
    public static final int PUSH_NOT_PUSHED = 0;

    /**
     * 推送消息已发送过
     */
    public static final int PUSH_HAS_PUSHED = 1;

    // ======================推送消息ID配置信息========================//

    /*    *//** 玩家被攻击 *//*
    public static final int ID_ATTACKED = 1;
    *//** 玩家被侦查 *//*
    public static final int ID_SCOUTED = 2;
    *//** 装备打造完成 *//*
    public static final int ID_EQUIP_FORGE_FINISH = 3;
    /** 科技研究完成 */
    public static final int ID_UP_TECH_FINISH = 4;
    /** 建筑升级完成 */
    public static final int ID_UP_BUILD_FINISH = 5;
    /** 资源满仓 *//*
    public static final int ID_RESOURCE_FULL = 6;
    *//** 要塞战开始 *//*
    public static final int FORT_BATTLE_IS_START = 7;*/
    /**
     * 行动力已满
     */
    public static final int ACT_IS_FULL = 8;
    /*    *//** 造兵完成,需要分3种兵 *//*
    public static final int PRODUCT_ARMY_COMPLETE = 9;
    *//** 化工厂生产完成 */
    // public static final int CHEMICAL_COMPLETE = 10;
    /** 装备改造次数已满 *//*
    public static final int WASH_EQUIP_IS_FULL = 11;
    *//** 洗髓次数已满 *//*
    public static final int WASH_HERO_IS_FULL = 12;
    *//** 聚宝盆冷却时间完成 *//*
    public static final int TREASURE_CD_COMPLETE = 13;
    *//** 采集完成 *//*
    public static final int COLLECT_COMPLETE = 14;*/
    /** 城防军可招募 */
    // public static final int WALL_RECRUIT = 15;
    /*    *//** 将领采集被%s伏击 *//*
    public static final int COLLECT_BY_ATTCK = 16;
    *//** %s：您占领的%s%s告急 *//*
    public static final int ATTCK_CAMP_BATTLE = 17;
    *//** 互动次数已满 */
    // public static final int INTERACTION_CNT_FULL = 18;
    /**
     * 阵营邮件通知
     */
    public static final int CAMP_MAIL_NOTICE = 19;
    /** 超级矿点被锤了 *//*
    public static final int SUPER_MINE_ATTCK = 20;*/
    /**
     * 24≤离线时间过长＜48
     */
    public static final int OFF_LINE_ONE_DAY = 21;
    /**
     * 48≤离线时间过长<72
     */
    public static final int OFF_LINE_TWO_DAY = 22;
    /**
     * 离线时间过长大于等于72
     */
    public static final int OFF_LINE_THREE_DAY = 23;
    /**
     * 爱丽丝到达
     */
    public static final int ALICE_ARRIVE = 24;
    /**
     * 体力赠送中午12点
     */
    public static final int ACT_POWER_TWELVE = 25;
    /**
     * 体力赠送下午6点
     */
    public static final int ACT_POWER_SIX = 26;

    /**
     * 被人宣战
     */
    public static final int ENEMIES_ARE_APPROACHING = 27;

    /**
     * 正在被攻打，无论防守成功或失败
     */
    public static final int ATTACKED_AND_BEATEN = 28;

    /**
     * 完成世界任务
     */
    public static final int ENOUGH_WORLD_SCHEDULE = 29;

}
