package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 宝具副本挑战玩家相关常量
 * @Author: DuanShQ
 * @CreateTime: 2022-06-13
 */
public class TreasureChallengePlayerConstant {

    /** 每天免费挑战次数 */
    public final static int FREE_CHALLENGE_NUM = 3;
    /** 对同一玩家的最大连续挑战次数 */
    public final static int CHALLENGE_FOR_PLAYER_NUM = 3;
    /** 刷新挑战玩家冷却时间 */
    public final static int CHALLENGE_REFRESH_TIME = 5 * 60;
    /** 挑战排行榜的随机范围 */
    public final static int CHALLENGE_RANK_RANGE = 20;
    /** 从第xx次开始触发保底 */
    public final static int MIN_BAO_DI = 4;
    /** 超过xx次后不触发保底 */
    public final static int MAX_BAO_DI = 8;
    /** 前 X 次刷新不会出现重复的挑战对象 */
    public final static int NOT_REPETITION_NUM = 10;
    /** 保底战斗力百分比范围 */
    public final static int[][] BAO_DI_FIGHT_PER_RANGE = {
            {95, 85},
            {85, 75},
            {75, 65},
            {65, 55},
            {55, 45}
    };

    /** 刷新挑战玩家消耗钻石数 */
    public static int CHALLENGE_REFRESH_COST_DIAMOND;
    /** 购买挑战次数的钻石消耗 */
    public static List<Integer> PURCHASE_COST;
    /** 可购买的挑战次数 */
    public static int CAN_PURCHASE_NUM;
    /** 特殊道具每日产出数量限制 */
    public static List<List<Integer>> TREASURE_SPECIAL_PROPS_OUTPUT_NUM;
    /** 特殊道具id */
    public static List<Integer> TREASURE_SPECIAL_PROPS_ID;

    public static void loadSystem() {
        CHALLENGE_REFRESH_COST_DIAMOND = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_CHALLENGE_REFRESH_COST_DIAMOND, 20);
        TREASURE_SPECIAL_PROPS_OUTPUT_NUM = SystemTabLoader.getListListIntSystemValue(SystemId.TREASURE_MATERIALS_OUTPUT_NUM, "[[]]");
        TREASURE_SPECIAL_PROPS_ID = TREASURE_SPECIAL_PROPS_OUTPUT_NUM.stream()
                .map(l -> l.get(0))
                .collect(Collectors.toList());

        PURCHASE_COST = SystemTabLoader.getListIntSystemValue(SystemId.TREASURE_COMBAT_CHALLENGE_PLAYER_PURCHASE, "[]");
        if (CheckNull.isEmpty(PURCHASE_COST)) {
            throw new MwException("购买挑战次数的钻石消耗 配置为空! systemId = " + SystemId.TREASURE_COMBAT_CHALLENGE_PLAYER_PURCHASE);
        }
        CAN_PURCHASE_NUM = PURCHASE_COST.size();
    }
}
