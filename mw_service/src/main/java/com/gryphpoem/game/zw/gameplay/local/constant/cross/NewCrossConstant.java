package com.gryphpoem.game.zw.gameplay.local.constant.cross;

import com.gryphpoem.game.zw.resource.constant.PropConstant;
import org.springframework.util.StringUtils;

public class NewCrossConstant {

    /**
     * 未开始, 或已经结束
     */
    public static final int STAGE_OVER = 0;
    /**
     * 预备期
     */
    public static final int STAGE_DIS_PLAYER = 1;
    /**
     * 正式期
     */
    public static final int STAGE_RUNNING = 2;
    /**
     * 兑换期
     */
    public static final int STAGE_END_PLAYER = 3;

    //===========================跨服地图信息=======================
    /** 跨服战火燎原地图*/
    public static final int CROSS_WAR_FIRE_MAP = 51;

    //===========================跨服分组=======================
    public enum CrossGroup {
        GROUP_RED(1, "red"),
        GROUP_BLUE(2, "blue"),
        GROUP_YELLOW(3, "yellow"),;

        private int type;
        private String name;

        CrossGroup(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public static CrossGroup convertTo(String name) {
            if (StringUtils.isEmpty(name)) {
                return null;
            }

            for (CrossGroup crossGroup : values()) {
                if (name.equalsIgnoreCase(crossGroup.getName()))
                    return crossGroup;
            }

            return null;
        }
    }

    //======================行军加速=======================
    public enum AccelerateCrossArmyProp {
        /** 行军加速类型：高级行军加速 */
        MOVE_TYPE_1(1, PropConstant.ITEM_ID_5011),
        /** 行军加速类型：顶级行军加速 */
        MOVE_TYPE_2(2, PropConstant.ITEM_ID_5012),;

        private int client;

        private int propId;

        public int getClient() {
            return client;
        }

        public int getPropId() {
            return propId;
        }

        AccelerateCrossArmyProp(int client, int propId) {
            this.client = client;
            this.propId = propId;
        }

        public static AccelerateCrossArmyProp convertTo(int client) {
            for (AccelerateCrossArmyProp prop : values()) {
                if (prop.getClient() == client) {
                    return prop;
                }
            }

            return null;
        }
    }

    //======================撤回军队========================
    public enum RetreatCrossArmyProp {
        /** 行军加速类型：高级行军加速 */
        RETREAT_TYPE_1(1, PropConstant.ITEM_ID_5021),
        /** 行军加速类型：顶级行军加速 */
        RETREAT_TYPE_2(2, PropConstant.ITEM_ID_5022),
        /** 行军加速类型：免费 */
        RETREAT_TYPE_3(3, 0),;

        private int client;

        private int propId;

        public int getClient() {
            return client;
        }

        public int getPropId() {
            return propId;
        }

        RetreatCrossArmyProp(int client, int propId) {
            this.client = client;
            this.propId = propId;
        }

        public static RetreatCrossArmyProp convertTo(int client) {
            for (RetreatCrossArmyProp prop : values()) {
                if (prop.getClient() == client) {
                    return prop;
                }
            }

            return null;
        }
    }

    //===========跨服迁城=================
    public enum CrossMoveCity {
        /** 进入新地图 */
        MOVE_CITY_TYPE_ENTER(1, 0),
        /** 退出新地图 */
        MOVE_CITY_TYPE_LEAVE(2, 0),
        /** 随机世界迁城(在新地图使用) */
        MOVE_CITY_TYPE_RANDOM(3, PropConstant.ITEM_ID_5002),
        /** 高级世界迁城(在新地图使用) */
        MOVE_CITY_TYPE_POS(4, PropConstant.ITEM_ID_5003),;

        private int client;

        private int propId;

        public int getClient() {
            return client;
        }

        public int getPropId() {
            return propId;
        }

        CrossMoveCity(int client, int propId) {
            this.client = client;
            this.propId = propId;
        }

        public static CrossMoveCity convertTo(int client) {
            for (CrossMoveCity prop : values()) {
                if (prop.getClient() == client) {
                    return prop;
                }
            }

            return null;
        }
    }

    //=======================跨服buff类型====================
    public interface CrossWarFire {
        /** 商店buff*/
        public static final int BUFF_TYPE_ATTACK = 1;
        public static final int BUFF_TYPE_DEF = 2;
        public static final int BUFF_TYPE_RECOVER_ARMY = 3;

        /**
         * 增伤
         */
        public static final int BUFF_TYPE_3 = 3;
        /**
         * 减伤
         */
        public static final int BUFF_TYPE_4 = 4;
    }
}
