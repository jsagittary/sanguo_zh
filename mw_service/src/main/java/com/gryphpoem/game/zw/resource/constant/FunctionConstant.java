package com.gryphpoem.game.zw.resource.constant;

/**
 * @author TanDonghai
 * @Description 功能解锁相关常量类
 * @date 创建时间：2017年6月8日 上午10:24:49
 */
public class FunctionConstant {

    /**
     * 功能解锁类型：IP白名单
     */
    public static final int UNLOCK_TYPE_IP = 1;
    /**
     * 功能解锁类型：玩家等级
     */
    public static final int UNLOCK_TYPE_ROLE_LV = 2;
    /**
     * 功能解锁类型：司令部等级
     */
    public static final int UNLOCK_TYPE_CITY_LV = 3;
    /**
     * 功能解锁类型：任务
     */
    public static final int UNLOCK_TYPE_TASK = 4;
    /**
     * 功能解锁类型：关卡
     */
    public static final int UNLOCK_TYPE_COMBAT = 5;
    /**
     * 功能解锁类型：科研所等级
     */
    public static final int UNLOCK_TYPE_TECH_LV = 6;
    /**
     * 功能解锁类型：军团等级
     */
    public static final int UNLOCK_TYPE_PARTY_LV = 7;
    /**
     * 功能解锁类型：离线收益
     */
    public static final int OFF_LINE_INCOME_LOCK_LV = 2003;
    /**
     * 功能解锁类型：教官功能
     */
    public static final int UNLOCK_TYPE_MENTOR = 2014;
    /**
     * 功能解锁类型：空军指挥官
     */
    public static final int UNLOCK_TYPE_PLANE_MENTOR = 2016;
    /**
     * 功能解锁类型：多人副本
     */
    public static final int UNLOCK_TYPE_MULTCOMBAT = 2017;
    /**
     * 功能解锁类型：装甲师
     */
    public static final int UNLOCK_TYPE_MENTOR_3 = 2018;

    // 需要解锁的功能id定义，对应s_function_unlock表中的functionId，由于该表控制的功能粒度过细（客户端同样使用该表），服务端只针对性的对部分功能添加判断
    /**
     * 功能：排行榜
     */
    public static final int FUNC_ID_RANK = 1001;
    /**
     * 功能：任务
     */
    public static final int FUNC_ID_TASK = 1002;
    /**
     * 功能：背包
     */
    public static final int FUNC_ID_BAG = 1003;
    /**
     * 功能：系统公告
     */
    public static final int FUNC_ID_SYS_CHAT = 1004;
    /**
     * 功能：武将
     */
    public static final int FUNC_ID_HERO = 1008;
    /**
     * 功能：武将洗髓
     */
    public static final int FUNC_ID_HERO_WASH = 1009;
    /**
     * 功能：邮件
     */
    public static final int FUNC_ID_MAIL = 1010;
    /**
     * 功能：世界
     */
    public static final int FUNC_ID_WORLD = 1011;
    /**
     * 功能：副本
     */
    public static final int FUNC_ID_COMBAT = 1012;
    /**
     * 功能：扫荡
     */
    public static final int FUNC_ID_WIPE = 1013;
    /**
     * 功能：商用建造队列
     */
    public static final int FUNC_ID_BUILD_QUEUE = 1014;
    /**
     * 功能解锁类型: 阵营
     */
    public static final int FUNC_ID_PARTY = 1028;
    /**
     * 功能解锁类型: 好友
     */
    public static final int FUNC_ID_FRIEND = 1030;
    /**
     * 功能解锁类型: 私聊频道聊天
     */
    public static final int FUNC_CHAT_IN_PRIVATE = 1037;
    /**
     * 功能解锁类型: 公共频道聊天
     */
    public static final int FUNC_CHAT_IN_PUBLIC = 1040;
    /**
     * 功能:进入跨服
     */
    public static final int FUNC_ID_ENTER_CROSS_WAR = 1044;
    /**
     * 图腾
     */
    public static final int FUNC_ID_TOTEM = 1046;
    /**
     * 搜索叛军
     */
    public static final int SEARCH_BANDIT = 1052;
    /**
     * 功能：战令
     */
    public static final int FUNC_ID_BATTLE_PASS = 2025;
    /**
     * 勇冠三军
     */
    public static final int ACT_ACT_BRAVEST_ARMY = 2027;

    /**
     * 功能: 建筑礼包
     */
    public static final int FUNC_BUILD_GIFT = 3002;

    /**
     * 宝具征程
     */
    public static final int ACT_TREASURE_WARE_JOURNEY = 3037;

    /**
     * 神兵宝具
     */
    public static final int ACT_MAGIC_TREASURE_WARE = 3038;

    /**
     * 宝具
     */
    public static final int FUNC_TREASURE_WARE = 1047;
    /**
     * 心愿武将
     */
    public static final int FUNC_WISH_HERO = 1050;
    /**
     * 常驻抽卡功能
     */
    public static final int FUNC_PERMANENT_DRAW_CARD = 1051;
    /**
     * 功能：进入世界争霸条件
     */
    public static final int FUNC_ID_ENTER_WORLDWAR = 8886;
    /**
     * 功能: 高阶勋章开启
     */
    public static final int FUNC_ID_RED_MEDAL = 9003;

    /**
     * 功能：阵营对拼活动
     */
    public static final int FUNC_ID_ROYAL_ARENA = 9004;

    public static final int FUNC_ACTIVITY_DIAOCHAN = 9011;

    public static final int FUNC_ACTIVITY_SEASON_HERO = 9014;

    /**
     * 赛季天赋功能开关
     */
    public static final int FUNC_SEASON_TALENT = 9018;

    /**
     * 功能：阵营补给
     */
    public static final int FUNC_PARTY_SUPPLY = 9021;

    /**
     * 功能: 跨服战火燎原
     */
    public static final int FUNC_CROSS_WAR_FIRE = 9022;

    /**
     * 跨服战火聊天
     */
    public static final int FUNC_CROSS_WAR_FIRE_CHAT = 9023;

    /**
     * 王朝遗迹
     */
    public static final int FUNC_ID_RELIC = 8035;

}
