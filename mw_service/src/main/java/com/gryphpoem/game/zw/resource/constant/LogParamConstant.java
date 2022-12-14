package com.gryphpoem.game.zw.resource.constant;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-12 14:19
 */
public class LogParamConstant {
    // 英雄突破
    // 1,品质相同时进行的突破。2.品质变化时进行的突破
    public static final byte HERO_BREAK_IN_SAME_QUALITY = 1;
    public static final byte HERO_BREAK_IN_DIFFERENT_QUALITY = 2;

    public static final byte TURNTABLE_ONE_COUNT_TYPE = 1;
    public static final byte TURNTABLE_TEN_COUNT_TYPE = 2;

    public static final String NO_FIRST_KILL_CITY = "-1";
    public static final String IS_FIRST_KILL_CITY = "1";
    public static final String IS_NOT_FIRST_KILL_CITY = "0";

    public static final int PERMANENT_DRAW_CARD_TYPE = 1;
    public static final int TIME_LIMITED_DRAW_CARD_TYPE = 2;

    //=======================事件名称==========================
    public static final String LEVEL_UP = "levelUp";
    public static final String HERO_BREAK = "heroBreak";
    public static final String HERO_SEARCH_METHOD = "heroSearch";
    public static final String TURNTABLE_ACT = "turntable";
    public static final String POINT = "point";
    public static final String FIGHTING_CHANGE = "fightingChangeNew";
    public static final String DRAW_HERO_CARD_LOG = "drawHeroCard";
    public static final String HERO_UPGRADE = "heroUpgrade";
    public static final String MEDAL_STRENGTHEN = "medalStrengthen";
    public static final String UPGRADE_SUPER_EQUIP = "upgradeSuperEquip";
    public static final String ACTIVATE_OR_CLEAR_HERO_TALENT = "activateOrClearHeroTalent";
    public static final String UPGRADE_HERO_TALENT = "upgradeHeroTalent";
}
