package com.gryphpoem.game.zw.resource.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildingType {
    // 君王殿
    public static final int COMMAND = 1;
    // 太史院
    public static final int TECH = 11;
    // 尚书台
    public static final int WAR_FACTORY = 12;
    // 仓库
    public static final int STOREHOUSE = 13;
    // 群贤馆
    public static final int WAR_COLLEGE = 14;
    // 冶炼铺
    public static final int REMAKE_WEAPON_HOUSE = 15;
    // 铁匠铺
    public static final int MAKE_WEAPON_HOUSE = 16;
    // 渡口
    public static final int FERRY = 17;;
    // 戏台
    public static final int SMALL_GAME_HOUSE = 18;
    // 城墙
    public static final int WALL = 29;
    // 寻访台
    public static final int DRAW_HERO_HOUSE = 60;
    // 铸星台
    public static final int SUPER_EQUIP_HOUSE = 62;
    // 雕像
    public static final int STATUTE = 63;
    // 天录阁
    public static final int MEDAL_HOUSE = 65;
    // 军备堂
    public static final int TRADE_CENTRE = 91;
    // 市场
    public static final int MALL = 92;
    // 铜雀台
    public static final int CIA = 99;

    // 铸币厂
    public static final int RES_OIL = 31;
    // 伐木场
    public static final int RES_ELE = 32;
    // 良田
    public static final int RES_FOOD = 33;
    // 矿石厂
    public static final int RES_ORE = 34;
    // 民居
    public static final int RESIDENT_HOUSE = 35;

    // 步兵营
    public static final int FACTORY_1 = 41;
    // 骑兵营
    public static final int FACTORY_2 = 42;
    // 弓兵营
    public static final int FACTORY_3 = 43;

    // 兵营(可改造)
    public static final int TRAIN_FACTORY_1 = 51;
    public static final int TRAIN_FACTORY_2 = 52;
    public static final int TRAIN_FACTORY_3 = 53;

    // 空军基地(没用)
    public static final int AIR_BASE = 9999;
    // 赛季宝库建筑(没用)
    public static final int SEASON_TREASURY = 70;
    // 码头(没用)
    public static final int WHARF = 97;

    // 大富翁解锁条件id
    public static final int MONOPOLY_LOCK_ID = 2004;
    /** 荣耀演习场解锁条件 */
    public static final int PITCHCOMBAT_LOCK_ID = 2015;
    /** 进入德意志条件 */
    public static final int ENTER_AREA_13_COND = 8887;
    // 宝石进阶功能
    public static final int STONE_IMPROVE_FUNCTION = 1039;
    // 荣耀日报
    public static final int RYRB = 8888;

    /*------------------------------解锁条件----------------------------------*/

    /**
     * 资源建筑数组
     */
    public static final int[] RES_ARRAY = new int[] { RES_OIL, RES_ELE, RES_FOOD, RES_ORE, RESIDENT_HOUSE };
    public static final int[] TRAIN_ARRAY = new int[] { TRAIN_FACTORY_1, TRAIN_FACTORY_2, TRAIN_FACTORY_3 };
    public static final int[] FACTORY_ARRAY = new int[] { FACTORY_1, FACTORY_2, FACTORY_3 };

    /**
     * 自动建造的顺序规则
     */
    public static final int[] AUTO_BUILD_RULE = { TECH, RES_ORE, RES_FOOD, RES_ELE, RES_OIL, STOREHOUSE, WALL,
            FACTORY_1, FACTORY_2, FACTORY_3, TRAIN_FACTORY_1, TRAIN_FACTORY_2, TRAIN_FACTORY_3 };

    /**
     * 功能建筑数组
     */
    public static final int[] FUNCTION_BUILDING = { COMMAND, TECH, WAR_FACTORY, STOREHOUSE, WAR_COLLEGE, REMAKE_WEAPON_HOUSE,
            MAKE_WEAPON_HOUSE, FERRY, WALL, SMALL_GAME_HOUSE, DRAW_HERO_HOUSE, SUPER_EQUIP_HOUSE, STATUTE, MEDAL_HOUSE,
            TRADE_CENTRE, MALL, CIA, FACTORY_1, FACTORY_2, FACTORY_3};

    /**
     * 一键委任武将的建筑及顺序
     */
    public static final int[] AUTO_DISPATCH_HERO_RULE = { RES_FOOD, RES_ORE, RES_ELE, RES_OIL, FERRY, MALL,
            SMALL_GAME_HOUSE, FACTORY_1, FACTORY_2, FACTORY_3 };

    /**
     * 一键派遣居民的建筑类型
     */
    public static final int[] AUTO_DISPATCH_RESIDENT_BUILDING = { FERRY, RES_ORE, RES_FOOD, RES_OIL, RES_ELE };

    public static final List<Integer> autoDispatchResidentBuilding = new ArrayList<>(Arrays.asList(FERRY, RES_ORE, RES_FOOD, RES_OIL, RES_ELE));

    /** 建筑可升级 */
    public static final int BUILD_CAN_UP_STATUS = 1;
    /** 建不可升级 */
    public static final int BUILD_NOT_UP_STATUS = 0;

    /** 建筑可拆除 */
    public static final int BUILD_CAN_DESTORY_STATUS = 1;
    /** 建筑不可拆除 */
    public static final int BUILD_NOT_DESTORY_STATUS = 0;

    /** 建筑可产资源 */
    public static final int BUILD_CAN_RES_STATUS = 1;
    /** 建不可产资源 */
    public static final int BUILD_NOT_RES_STATUS = 0;

    public static int getFactoryConvertTrain(int type) {
        switch (type) {
            case TRAIN_FACTORY_1:
                return FACTORY_1;
            case TRAIN_FACTORY_2:
                return FACTORY_2;
            case TRAIN_FACTORY_3:
                return FACTORY_3;
            case FACTORY_1:
            case FACTORY_2:
            case FACTORY_3:
                return TRAIN_FACTORY_1;
        }
        return 0;
    }

    public static int getResourceByBuildingType(int buildingType) {
        switch (buildingType) {
            case BuildingType.FACTORY_1:
                return AwardType.Army.FACTORY_1_ARM;
            case BuildingType.FACTORY_2:
                return AwardType.Army.FACTORY_2_ARM;
            case BuildingType.FACTORY_3:
                return AwardType.Army.FACTORY_3_ARM;
            case BuildingType.TRAIN_FACTORY_1:
                return AwardType.Army.FACTORY_1_ARM;
            case BuildingType.TRAIN_FACTORY_2:
                return AwardType.Army.FACTORY_2_ARM;
            case BuildingType.TRAIN_FACTORY_3:
                return AwardType.Army.FACTORY_3_ARM;
        }
        return 0;
    }
}
