package com.gryphpoem.game.zw.resource.constant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-04 14:10
 * @description: 全局杂乱数据定义, 可定义全服和全阵营两种
 * @modified By:
 */
public class GlobalConstant {

    /**
     * 每日清除的数据keyId
     */
    private static List<String> CLEAN_LIST_KEY = new ArrayList<>();

    /**
     * 需要区分阵营
     */
    private static List<String> DISTINGUISH_CAMP_KEY = new ArrayList<>();

    /**
     * 三个阵营
     */
    private static List<Integer> camps = Stream.of(Constant.Camp.EMPIRE, Constant.Camp.ALLIED, Constant.Camp.UNION).collect(Collectors.toList());
    // private static Stream<Integer> camps = Stream.of(Constant.Camp.EMPIRE, Constant.Camp.ALLIED, Constant.Camp.UNION);


    /**
     * 每天攻克城市产生的阵营补给次数, 3个阵营
     */
    public static final String PARTY_SUPPLY_CONQUER_CITY_CAMP = "PARTY_SUPPLY_";

    /**
     * 阵型的难度系数, 不分阵营
     */
    public static final String FORM_COEF_OF_DIFFICULTY = "FORM_COEF_OF_DIFFICULTY";

    /**
     * 匪军叛乱下次开放的时间
     */
    public static final String REBEL_NEXT_OPEN_TIME = "REBEL_NEXT_OPEN_TIME";

    /**
     * 飞艇下次开放的时间
     */
    public static final String AIR_SHIP_NEXT_OPEN_TIME = "AIR_SHIP_NEXT_OPEN_TIME";

    /**
     * 反攻德意志下次开放的时间
     */
    public static final String COUNTER_ATK_NEXT_OPEN_TIME = "COUNTER_ATK_NEXT_OPEN_TIME";

    /**
     * 飞艇本次的开放时间
     */
    public static final String AIR_SHIP_CUR_OPEN_TIME = "AIR_SHIP_CUR_OPEN_TIME";

    /**
     * 战令的最近一次发送邮件的key
     */
    public static final String BATTLE_PASS_SEND_MAIL_KEY = "BATTLE_PASS_SEND_MAIL_KEY";

    /**
     * 世界进程 限时目标: N个城被攻克
      */
    public static final String WORLD_SCHEDULE_GOAL_COND_CONQUER_CITY = "WORLD_SCHEDULE_GOAL_COND_CONQUER_CITY";

    /**
     * 飞艇本次当前区域击杀数量, 3个阵营
     */
    public static final String AIR_SHIP_CUR_AREA_KILL_NUM = "AIR_SHIP_CUR_AREA_KILL_NUM_";

    /**
     * 全区域首杀结束显示的时间, 合服不保存, 因为首杀会清除
     */
    public static final String WORLD_AREA_FIRST_KILL_END_DISPLAY_TIME = "WORLD_AREA_FIRST_KILL_END_DISPLAY_TIME";

    /**
     * 难度系数
     */
    public interface CoefDifficulty {

        /**
         * 德意志反攻
         */
        int COUNTER_ATTACK = 1;
        // TODO: 2019-03-15 后期有新增的难度系数, 在这里定义
    }


    static {
        addDistinguishCampKey();
        addCleanListKey();
    }

    /**
     * 添加需要每日清除的key
     */
    private static void addCleanListKey() {
        addCleanListKeyLogic(PARTY_SUPPLY_CONQUER_CITY_CAMP);
        addCleanListKeyLogic(AIR_SHIP_CUR_AREA_KILL_NUM);
    }

    /**
     * 添加需要区分阵营存储的key
     */
    private static void addDistinguishCampKey() {
        addDistinguishCampKeyLogic(PARTY_SUPPLY_CONQUER_CITY_CAMP);
        addDistinguishCampKeyLogic(AIR_SHIP_CUR_AREA_KILL_NUM);
    }

    /**
     * 添加key到区分阵营容器
     *
     * @param key
     */
    private static void addDistinguishCampKeyLogic(String key) {
        DISTINGUISH_CAMP_KEY.add(key);
    }

    /**
     * 添加key到每日清除容器
     *
     * @param key
     */
    private static void addCleanListKeyLogic(String key) {
        if (DISTINGUISH_CAMP_KEY.contains(key)) {
            camps.forEach(camp -> CLEAN_LIST_KEY.add(key + camp));
        } else {
            CLEAN_LIST_KEY.add(key);
        }
    }

    /**
     * 是否需要区分阵营
     *
     * @param key
     * @return
     */
    public static boolean needDistinguishCampKey(String key) {
        return DISTINGUISH_CAMP_KEY.contains(key);
    }

    /**
     * 每日清除的数据keyId
     *
     * @return
     */
    public static List<String> getCleanList() {
        return CLEAN_LIST_KEY;
    }
}
