package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName HonorDailyConstant.java
 * @Description 荣耀日报相关配置
 * @author QiuKun
 * @date 2018年8月28日
 */
public interface HonorDailyConstant {

    /**
     * 与PlayerConstant存储keyId 对应关系 key:condId,value:condId
     */
    public Map<Integer, Integer> CONDID_MAPPER_PLAYERCONSTANT = new HashMap<>();

    /**
     * 根据condId,获取荣耀日报存储在PlayerConstant的key值
     * 
     * @param condId
     * @return
     */
    public static Integer getPlayerConstantKeyIdByCondId(int condId) {
        if (CheckNull.isEmpty(CONDID_MAPPER_PLAYERCONSTANT)) {
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_1, PlayerConstant.HONOR_DAILY_COND_ID_1);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_2, PlayerConstant.HONOR_DAILY_COND_ID_2);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_3, PlayerConstant.HONOR_DAILY_COND_ID_3);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_4, PlayerConstant.HONOR_DAILY_COND_ID_4);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_5, PlayerConstant.HONOR_DAILY_COND_ID_5);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_6, PlayerConstant.HONOR_DAILY_COND_ID_6);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_7, PlayerConstant.HONOR_DAILY_COND_ID_7);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_8, PlayerConstant.HONOR_DAILY_COND_ID_8);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_9, PlayerConstant.HONOR_DAILY_COND_ID_9);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_10, PlayerConstant.HONOR_DAILY_COND_ID_10);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_11, PlayerConstant.HONOR_DAILY_COND_ID_11);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_12, PlayerConstant.HONOR_DAILY_COND_ID_12);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_13, PlayerConstant.HONOR_DAILY_COND_ID_13);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_14, PlayerConstant.HONOR_DAILY_COND_ID_14);
            CONDID_MAPPER_PLAYERCONSTANT.put(COND_ID_15, PlayerConstant.HONOR_DAILY_COND_ID_15);
        }
        return CONDID_MAPPER_PLAYERCONSTANT.get(condId);
    }

    /** 当天搬离城市X次 */
    public int COND_ID_1 = 1;
    /** 占领了非本阵营的据点x次 */
    public int COND_ID_2 = 2;
    /** 击败匪徒达到X次 */
    public int COND_ID_3 = 3;
    /** 当天击败X个敌人 */
    public int COND_ID_4 = 4;
    /** 当天击败X个盟军阵营 y等级 */
    public int COND_ID_5 = 5;
    /** 当天击败X个帝国阵营 y等级 */
    public int COND_ID_6 = 6;
    /** 当天击败X个联军阵营 y等级 */
    public int COND_ID_7 = 7;
    /** 当天被其他玩家击败X次 y等级 */
    public int COND_ID_8 = 8;
    /** 当天打匪军失败x次 y等级 */
    public int COND_ID_9 = 9;
    /** 当天对其他玩家使用x次侦查 */
    public int COND_ID_10 = 10;
    /** 当天派遣将领出战行走最远x英里 */
    public int COND_ID_11 = 11;
    /** 当天被其他玩家掠夺了X个资源 */
    public int COND_ID_12 = 12;
    /** 当天掠夺其他玩家x个资源最（3种资源之和，燃油、电力、补给）） */
    public int COND_ID_13 = 13;
    /** 当天损失了x个士兵（3种兵之和） */
    public int COND_ID_14 = 14;
    /** 当天送礼的亲密值达到x点（互动次数不算，礼物亲密值之和） */
    public int COND_ID_15 = 15;

}
