package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;

/**
 * @Description 机器人相关常量
 * @author TanDonghai
 * @date 创建时间：2017年9月20日 下午5:23:02
 *
 */
public class RobotConstant {
    private RobotConstant() {
    }

    /** 机器人创建配置，格式：[[treeId,count],[treeId,count]] */
    public static List<List<Integer>> ROBOT_INIT;

    /** 机器人默认睡眠间隔时间，单位：秒 */
    public static int ROBOT_SLEEP;

    /** 单次唤醒机器人最大数 */
    public static int ROBOT_AWAKE_LIMIT;

    /** 攻击流寇的机器人最低等级限制 */
    public static int ATTACK_BANDIT_MIN_LV;

    /** 机器人执行自动招募士兵逻辑的周期，单位：秒 */
    public static int ARM_RECRUIT_DELAY;

    /** 装备改造逻辑执行时间间隔，单位：秒 */
    public static int EQUIP_REFIT_DELAY;

    /** 自动挑战副本逻辑执行时间间隔，单位：秒 */
    public static int DO_COMBAT_DELAY;

    /** 机器人自动搜寻矿点半径 */
    public static int MINE_RADIUS;

    /** 机器人日志打印状态 */
    public static int ROBOT_LOG_STATE;

    /** 机器人功能开关 */
    public static int ROBOT_STATE_SWITCH;

    /** 机器人区域数量,list[0] : 单次迁移的上限,list[1] : 区域中容纳的机器人的上限*/
    public static List<Integer> ROBOT_AREA_COUNT;


    public static void loadSystem() {
        ROBOT_INIT = SystemTabLoader.getListListIntSystemValue(SystemId.ROBOT_INIT, "[[1,1]]");
        ROBOT_SLEEP = SystemTabLoader.getIntegerSystemValue(SystemId.ROBOT_SLEEP, 60);
        ROBOT_AWAKE_LIMIT = SystemTabLoader.getIntegerSystemValue(SystemId.ROBOT_AWAKE_LIMIT, 10);
        ATTACK_BANDIT_MIN_LV = SystemTabLoader.getIntegerSystemValue(SystemId.ATTACK_BANDIT_MIN_LV, 10);
        ARM_RECRUIT_DELAY = SystemTabLoader.getIntegerSystemValue(SystemId.ARM_RECRUIT_DELAY, 3600);
        EQUIP_REFIT_DELAY = SystemTabLoader.getIntegerSystemValue(SystemId.ROBOT_SLEEP, 3600);
        DO_COMBAT_DELAY = SystemTabLoader.getIntegerSystemValue(SystemId.DO_COMBAT_DELAY, 3600);
        MINE_RADIUS = SystemTabLoader.getIntegerSystemValue(SystemId.MINE_RADIUS, 10);
        ROBOT_LOG_STATE = SystemTabLoader.getIntegerSystemValue(SystemId.ROBOT_LOG_STATE, 1);
        ROBOT_STATE_SWITCH = SystemTabLoader.getIntegerSystemValue(SystemId.ROBOT_STATE_SWITCH, 1);
        ROBOT_AREA_COUNT = SystemTabLoader.getListIntSystemValue(SystemId.ROBOT_AREA_COUNT,"[1000,1000]");
    }

    /**
     * 行为树事件类型
     */
    public interface BtreeEvent {

    }

    /**
     * 行为树action节点类型枚举
     */
    public enum ActionNodeType {
        /**
         * 错误类型
         */
        NONE(0, "NONE", "类型错误") {
        },
        BUILDING_UP(1, "建筑升级", "机器人升级城池中的建筑") {
        },
        TECH_UP(2, "研究科技", "研究科技") {
        },
        HERO_RECRUIT(3, "招募将领", "招募将领并上阵") {
        },
        ARM_RECRUIT(4, "招募士兵", "招募士兵并补兵") {
        },
        EQUIP_FORGE(5, "打造装备", "打造并穿戴装备") {
        },
        EQUIP_REFIT(6, "装备改造", "装备改造") {
        },
        ACTIVITY(7, "参加活动", "当活动开启后，主动参加活动") {
        },
        VIP_UP(8, "增加VIP经验", "定时增加VIP经验") {
        },
        GOLD_ADD(9, "增加金币", "定时增加机器人金币") {
        },
        DO_COMBAT(10, "攻打副本", "攻打副本") {
        },
        MINE_COLLECT(11, "采集矿点", "采集世界矿点") {
        },
        ACQUISITION(12, "采集个人资源点", "自动增加个人资源点奖励的资源") {
        },
        ATTACK_BANDIT(13, "攻打流寇", "攻打流寇") {
        },
        SCOUT(14, "侦查玩家", "侦查玩家") {
        },
        ATTACK_PLAYER(15, "进攻玩家", "进攻玩家") {
        },
        ATTACK_CAMP(16, "发动城战", "发动城战（阵营战）") {
        },
        DO_CHAT(17, "发送聊天", "发送简单的聊天信息") {
        },
        TASK_REWARD(18, "领取任务奖励", "领取任务奖励") {
        },
        GAIN_RESOURCE(19, "征收资源", "征收资源") {
        },
        ROLE_EXP(20, "增加角色经验", "定时增加机器人经验") {
        },

        ;

        public static ActionNodeType valueOf(int type) {
            if (type < 1 || type > ActionNodeType.values().length) {
                return NONE;
            }
            return ActionNodeType.values()[type];
        }

        private int type;
        private String name;
        private String desc;

        private ActionNodeType() {
        }

        private ActionNodeType(int type, String name, String desc) {
            this.type = type;
            this.name = name;
            this.desc = desc;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * AI行为树condition节点细分类型
     */
    public enum ConditionNodeType {

        ;

        private int type;
        private String name;
        private String desc;

        private ConditionNodeType() {
        }

        private ConditionNodeType(int type, String name, String desc) {
            this.type = type;
            this.name = name;
            this.desc = desc;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
    }

    /** 机器人默认的platId */
    public static final String DEFAULT_PLAT_ID = "robotplatId";

    /** 机器人默认的deviceNo */
    public static final String DEFAULT_DEVICE_NO = "robotdeviceno";

    /** 单次执行机器人定时任务最长时间限制，单位：毫秒 */
    public static final int ROBOT_EXECUTE_LIMIT = 20;

    /** 单次执行机器人行为树逻辑打印日志阈值，单位：毫秒 */
    public static final int ROBOT_EXECUTE_THRESHOLD = 50;

    /** 机器人状态：失效 */
    public static final int ROBOT_STATE_INVALID = 1;

    /** 机器人状态：正常 */
    public static final int ROBOT_STATE_NORMAL = 0;

    /** 机器人坐标: 拥有 */
    public static final int ROBOT_HAVE_POS = 1;

    /** 机器人坐标: 未拥有 */
    public static final int ROBOT_NOT_HAVE_POS = -1;

    /** 机器人默认的treeID */
    public static final int ROBOT_DEF_TREE_ID = 1;

    /** 机器人行为: 活跃 */
    public static final int ROBOT_EXTERNAL_BEHAVIOR = 0;

    /** 机器人行为: 不活跃(地图上可以看到点,只是玩家看不到机器人的行为,可以自然生长) */
    public static final int ROBOT_INNER_BEHAVIOR = 1;

}
