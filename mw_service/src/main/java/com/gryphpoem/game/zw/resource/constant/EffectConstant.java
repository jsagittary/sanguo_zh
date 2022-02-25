package com.gryphpoem.game.zw.resource.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 效果
 * 
 * @author tyler
 *
 */
public class EffectConstant {

    /** 护盾 */
    public static final int PROTECT = 1;
    /** 攻击万分比 */
    public static final int ATK_MUT = 2;
    /** 防御万分比 */
    public static final int DEF_MUT = 3;
    /** 行军加速(风字令) */
    public static final int WALK_SPEED = 4;
    /** 募兵加速 */
    public static final int ARM_CREATE_SPEED = 5;
    /** 建筑加速 */
    public static final int BUILD_SPEED = 6;
    /** 行军加速高级(军曹官) */
    public static final int WALK_SPEED_HIGHT = 7;
    /** 建造队列+1 */
    public static final int BUILD_CNT = 8;
    /** 战前buff加成: 攻击 (加的是具体数值) */
    public static final int PREWAR_ATK = 9;
    /** 战前buff加成: 防御 (加的是具体数值) */
    public static final int PREWAR_DEF = 10;
    /** 战前buff加成: 兵力 (加的是具体数值) */
    public static final int PREWAR_LEAD = 11;
    /** 柏林战前buff: 行军加速 (加的是万分比) */
    public static final int PREWAR_WALK_SPEED = 12;
    public static final int PREWAR_ATTACK_EXT = 16;//柏林战前BUFF：穿甲
    /**
     * 匪军资源加成: 金币 +10%
     */
    public static final int BANDIT_GOLD_BUFFER = 13;
    /**
     * 匪军资源加成: 木材 +10%
     */
    public static final int BANDIT_WOOD_BUFFER = 14;
    /**
     * 攻击NPC城池和叛军生效
     */
    public static final int AIRSHIP_WALK_SPEED = 15;

    /**
     * 柏林战前buff
     */
    public static final List BERLIN_PRE_BUFF = Arrays.asList(PREWAR_ATK, PREWAR_DEF, PREWAR_LEAD, PREWAR_ATTACK_EXT);
}
