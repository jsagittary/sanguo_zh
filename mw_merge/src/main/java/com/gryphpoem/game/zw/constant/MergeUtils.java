package com.gryphpoem.game.zw.constant;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.EffectConstant;
import com.gryphpoem.game.zw.resource.constant.GlobalConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName MergeUtils.java
 * @Description 合服的用到的工具
 * @author QiuKun
 * @date 2018年9月20日
 */
public abstract class MergeUtils {

    /**
     * 改名卡邮件id
     */
    public final static int RENAME_MAILID = 501;
    /**
     * 需要保存的活动类型
     */
    public final static Set<Integer> REATIN_ACT_TYPE;
    /**
     * 需要保存Player中mixtureData<br>
     * 
     * 玩家{@link Player#getMixtureData()}
     */
    public final static Set<Integer> REATIN_MIXTURE_DATA_KEY;

    /**
     * 需要保存的buff
     */
    public final static Set<Integer> REATIN_BUFF;

    /**
     * 需要保存的Global中mixtureData<br>
     *
     * 公共 {@link GameGlobal#getMixtureData()}
     */
    public final static Set<String> REATIN_GLOBAL_MIXTURE_DATA_KEY;

    /**
     * 需要保存的特价礼包的活动进度
     */
    public final static Set<Integer> REATIN_VIP_BAG_GIFT_IDS;

    /**
     * 合服之后需要移除的活动类型
     */
    public final static Set<Integer> REATIN_REMOVED_ACT_TYPE;

    public final static Set<Integer> REATIN_NEED_HANDLE_GLOBAL_ACT_DATA;

    public final static Set<Integer> REATIN_PERMANENT_ACT_TYPE;

    static {
        REATIN_ACT_TYPE = new HashSet<>();
        REATIN_ACT_TYPE.add(ActivityConst.ACT_FIRSH_CHARGE);// 首充礼包 首次充值领取奖励
        REATIN_ACT_TYPE.add(ActivityConst.ACT_7DAY);// 七日狂欢 新建角色开启为期7天的活动
        REATIN_ACT_TYPE.add(ActivityConst.ACT_BLACK);// 黑鹰计划（原七星拜将）
        REATIN_ACT_TYPE.add(ActivityConst.ACT_COMMAND_LV);// 基地升级（原主城升级）
        REATIN_ACT_TYPE.add(ActivityConst.ACT_ATTACK_CITY);// 攻占据点（原攻城掠地）
        REATIN_ACT_TYPE.add(ActivityConst.ACT_LEVEL);// 成长基金（原成长计划）
        REATIN_ACT_TYPE.add(ActivityConst.ACT_VIP_BAG);// 特价礼包 充值购买特价礼包
        REATIN_ACT_TYPE.add(ActivityConst.ACT_FOOD);// 能量赠送（原出师大宴）
        REATIN_ACT_TYPE.add(ActivityConst.ACT_WISHING_WELL);// 许愿池活动
        REATIN_ACT_TYPE.add(ActivityConst.ACT_FREE_LUXURY_GIFTS);// 免费豪礼

        // REATIN_ACT_TYPE.add(ActivityConst.ACT_ALL_CHARGE);// 全军返利全服活动 需要特殊
        REATIN_ACT_TYPE.add(ActivityConst.ACT_PAY_7DAY);// 七日充值
        REATIN_ACT_TYPE.add(ActivityConst.ACT_MONOPOLY); // 大富翁
        REATIN_ACT_TYPE.add(ActivityConst.ACT_BUILD_GIFT); // 建筑礼包

        REATIN_ACT_TYPE.add(ActivityConst.ACT_GIFT_PAY); // 充值有礼 累计充值指定黄金领取奖励
        REATIN_ACT_TYPE.add(ActivityConst.ACT_COST_GOLD);// 消费有礼 累计消费指定黄金领取奖励
        // REATIN_ACT_TYPE.add(ActivityConst.ACT_PAY_RANK);// 充值排行
        REATIN_ACT_TYPE.add(ActivityConst.ACT_CHARGE_TOTAL); // 累计充值
        // 根据充值参与排名领取超值奖励！
        REATIN_ACT_TYPE.add(ActivityConst.ACT_SUPPLY_DORP);// 空降补给（原屯田计划）
                                                           // 花费黄金购买，每天登录领取奖励，领完五次奖励后返还消耗的黄金
        // REATIN_ACT_TYPE.add(ActivityConst.ACT_DAILY_PAY);// 每日充值

        // REATIN_ACT_TYPE.add(ActivityConst.ACT_LUCKY_TURNPLATE); //
        // 神秘装备（原幸运罗盘）
        REATIN_ACT_TYPE.add(ActivityConst.ACT_PAY_TURNPLATE); // 充值转盘
        // REATIN_ACT_TYPE.add(ActivityConst.ACT_CONSUME_GOLD_RANK); // 消费排行活动
        // REATIN_ACT_TYPE.add(ActivityConst.ACT_ORE_TURNPLATE); // 矿石转盘
        // REATIN_ACT_TYPE.add(ActivityConst.ACT_EQUIP_TURNPLATE); // 装备转盘
        REATIN_ACT_TYPE.add(ActivityConst.ACT_REAL_NAME); //实名认证活动
        REATIN_ACT_TYPE.add(ActivityConst.ACT_PHONE_BINDING); //手机绑定
        REATIN_ACT_TYPE.add(ActivityConst.ACT_CHRISTMAS);//圣诞活动
        REATIN_ACT_TYPE.add(ActivityConst.ACT_REPAIR_CASTLE);//修缮城堡
        REATIN_ACT_TYPE.add(ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE);//专属客服, 合服保留合服前的进度
        REATIN_ACT_TYPE.add(ActivityConst.ACT_DAY_DISCOUNTS);//专属客服, 合服保留合服前的进度
        REATIN_ACT_TYPE.add(ActivityConst.ACT_MAGIC_TREASURE_WARE);
        REATIN_ACT_TYPE.add(ActivityConst.ACT_TREASURE_WARE_JOURNEY);

        /*---------------------------游戏服需要移除的活动类型-------------------------------*/
        REATIN_REMOVED_ACT_TYPE = new HashSet<>();
        REATIN_REMOVED_ACT_TYPE.add(ActivityConst.ACT_MAGIC_TREASURE_WARE);
        /*---------------------------游戏服需要处理的公共活动数据-------------------------------*/
        REATIN_NEED_HANDLE_GLOBAL_ACT_DATA = new HashSet<>();
        REATIN_NEED_HANDLE_GLOBAL_ACT_DATA.add(ActivityConst.ACT_MAGIC_TREASURE_WARE);
        /*---------------------------游戏服从服永久性活动-------------------------------*/
        REATIN_PERMANENT_ACT_TYPE = new HashSet<>();
        REATIN_PERMANENT_ACT_TYPE.add(ActivityConst.ACT_TREASURE_WARE_JOURNEY);

        /*---------------------------Player.mixtureData需要保存的东西-------------------------------*/
        REATIN_MIXTURE_DATA_KEY = new HashSet<>();
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.RYRB_LOCK_TIME);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.EQUIP_MAKE_COUNT);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.EQUIP_MAKE_NUM);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.EQUIP_MAKE_PROBABILITY);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.EQUIP_MAKE_INTERVAL);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.EQUIP_MAKE_RANGE);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.MENTOR_BILL);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.COUNTER_ATK_CREDIT);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.PLANE_FACTORY_MAKE_NUM);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.PLANE_FACTORY_SEARCH_AWARD);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.WAR_FIRE_PRICE);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.CROSS_WAR_FIRE_PRICE);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.DAILY_TITLE_REWARD);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.WISH_HERO);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.WISH_HERO_SEARCH_COUNT);
        REATIN_MIXTURE_DATA_KEY.add(PlayerConstant.NORMAL_HERO_SEARCH_COUNT);

        /*---------------------------保留的buff--------------------------*/
        REATIN_BUFF = new HashSet<>();
        REATIN_BUFF.add(EffectConstant.PROTECT);
        REATIN_BUFF.add(EffectConstant.BUILD_CNT);
        REATIN_BUFF.add(EffectConstant.WALK_SPEED);
        REATIN_BUFF.add(EffectConstant.ARM_CREATE_SPEED);
        REATIN_BUFF.add(EffectConstant.WALK_SPEED_HIGHT);
        REATIN_BUFF.add(EffectConstant.BANDIT_GOLD_BUFFER);
        REATIN_BUFF.add(EffectConstant.BANDIT_WOOD_BUFFER);

        /*---------------------------GlobalGame.mixtureData需要保存的东西--------------------------*/
        REATIN_GLOBAL_MIXTURE_DATA_KEY = new HashSet<>();
        REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.PARTY_SUPPLY_CONQUER_CITY_CAMP);
        REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.FORM_COEF_OF_DIFFICULTY);
        REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.REBEL_NEXT_OPEN_TIME);
        REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.AIR_SHIP_NEXT_OPEN_TIME);
        // 反攻德意志的下次开放时间合服后不保存
        // REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME);
        REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.AIR_SHIP_CUR_OPEN_TIME);
        REATIN_GLOBAL_MIXTURE_DATA_KEY.add(GlobalConstant.BATTLE_PASS_SEND_MAIL_KEY);

        /*---------------------------保留的特价礼包进度--------------------------*/
        REATIN_VIP_BAG_GIFT_IDS = new HashSet<>();
        REATIN_VIP_BAG_GIFT_IDS.add(10001);
        REATIN_VIP_BAG_GIFT_IDS.add(10002);
        REATIN_VIP_BAG_GIFT_IDS.add(10003);
        REATIN_VIP_BAG_GIFT_IDS.add(10004);
        REATIN_VIP_BAG_GIFT_IDS.add(10005);
        REATIN_VIP_BAG_GIFT_IDS.add(10006);
    }

    public static void invokeCalcExecTime(String param, IMergeCommand command) throws Exception {
        long start = System.currentTimeMillis();
        try {
            command.action();
        } finally {
            LogUtil.start(param + "--- 执行耗时:" + (System.currentTimeMillis() - start) + " 毫秒");
        }
    }

    @FunctionalInterface
    public static interface IMergeCommand {
        void action() throws Exception;
    }

}
