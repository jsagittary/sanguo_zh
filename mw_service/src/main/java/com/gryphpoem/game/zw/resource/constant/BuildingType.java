package com.gryphpoem.game.zw.resource.constant;

public class BuildingType {
    // 君王殿
    public static final int COMMAND = 1;
    // 太史院
    public static final int TECH = 11;
    // 战争工厂
    public static final int WAR_FACTORY = 12;
    // 仓库
    public static final int STOREHOUSE = 13;
    // 军事学院
    public static final int WAR_COLLEGE = 14;
    // 改造中心
    public static final int REMAKE = 15;
    // 军工厂
    public static final int ORDNANCE_FACTORY = 16;
    // 化工厂
    public static final int CHEMICAL_PLANT = 17;
    // 空军基地
    public static final int AIR_BASE = 18;
    // 围墙
    public static final int WALL = 29;

    // 铸币厂
    public static final int RES_OIL = 31;
    // 伐木场
    public static final int RES_ELE = 32;
    // 良田
    public static final int RES_FOOD = 33;
    // 矿石厂
    public static final int RES_ORE = 34;
    //核弹井
    public static final int NUCLEAR_BOMB = 35;

    // 兵营
    public static final int FACTORY_1 = 41;
    // 坦克工厂
    public static final int FACTORY_2 = 42;
    // 装甲基地
    public static final int FACTORY_3 = 43;

    // 战车训练基地
    public static final int TRAIN_FACTORY_1 = 51;
    // 坦克训练基地
    public static final int TRAIN_FACTORY_2 = 52;
    // 火箭训练基地
    public static final int TRAIN_FACTORY_3 = 53;
    // 赛季宝库建筑
    public static final int SEASON_TREASURY = 70;

    // 贸易中心
    public static final int TRADE_CENTRE = 91;

    // 俱乐部
    public static final int CLUB = 92;

    //码头
    public static final int WHARF = 97;

    /*------------------------------解锁条件----------------------------------*/
    // 情报部
    public static final int CIA = 99;
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

    public static final int[] RES_ARRAY = new int[] { RES_OIL, RES_ELE, RES_FOOD, RES_ORE };
    public static final int[] TRAIN_ARRAY = new int[] { TRAIN_FACTORY_1, TRAIN_FACTORY_2, TRAIN_FACTORY_3 };
    public static final int[] FACTORY_ARRAY = new int[] { FACTORY_1, FACTORY_2, FACTORY_3 };

    // 自动建造的顺序规则
    public static final int[] AUTO_BUILD_RULE = { TECH, RES_ORE, RES_FOOD, RES_ELE, RES_OIL, STOREHOUSE, WALL,
            FACTORY_1, FACTORY_2, FACTORY_3, TRAIN_FACTORY_1, TRAIN_FACTORY_2, TRAIN_FACTORY_3 };

    /**
     * 功能建筑数组
     */
    public static final int[] FUNCTION_BUILDING = { COMMAND, TECH, WAR_FACTORY, STOREHOUSE, WAR_COLLEGE, REMAKE,
            ORDNANCE_FACTORY, CHEMICAL_PLANT, WALL, FACTORY_1, FACTORY_2, FACTORY_3, TRAIN_FACTORY_1, TRAIN_FACTORY_2 };

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
