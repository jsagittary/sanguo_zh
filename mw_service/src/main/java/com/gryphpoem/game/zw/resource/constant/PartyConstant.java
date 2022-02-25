package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;
import java.util.Map;

/**
 * @ClassName PartyConstant.java
 * @Description 军团常量类
 * @author TanDonghai
 * @date 创建时间：2017年4月26日 上午10:17:31
 *
 */
public class PartyConstant {
    /** 每日军团建设次数上限 */
    public static int PARTY_BUILD_MAX;

    /** 军团公告长度 */
    public static int PARTY_SLOGAN_LEN;

    /** 军团荣誉排行榜显示条数 */
    public static int PARTY_HONOR_RANK_LEN;

    /** 军团官员选举显示数量（系统自动任命官员数） */
    public static int PARTY_ELECT_NUM;

    /** 军团官员中，军长最大数量 */
    public static int MAX_GENERAL;

    /** 军团官员选举排行榜最大长度 */
    public static int PARTY_ELECT_RANK_NUM;

    /** 军团官员选举，拉票单价 */
    public static int PARTY_CANVASS_GOLD;

    /** 军团官员选举时长 */
    public static int PARTY_ELECT_TIME;

    /** 军团官员任期天数 */
    public static int PARTY_JOB_DATE;

    /** 军团任务每三小时刷一次 */
    public static int PARTY_TASK_REFRESH_PER_HOUR;
    /** 军团任务额外奖励每阶段获得上限 */
    public static int PARTY_TASK_EXT_REWARD_CNT;
    /** 进官员选举榜等级限制 */
    public static int PARTY_JOB_MIN_LV;
    /** 荣耀日报进攻方,防守方排名筛选 */
    public static List<List<Integer>> HONOR_DAILY_RANK;
    /** 补给持续时间(秒) */
    public static int SUPPLY_CONTINUE_TIME;
    /** 阵营消耗金币产生补给能量值(消耗100金币生成5点能量) */
    public static List<Integer> COST_GOLD_ENERGY_NUM;
    /** 阵营补给, 每日最大生成数量 */
    public static Map<Integer, Integer> PARTY_SUPPLY_MAX_CNT_CONF;
    /** 阵营补给, 一键领取的配置type */
    public static List<Integer> PARTY_SUPPLY_ONE_KEY_ALL;

    /**
     * 官员投票的等级限制
     */
    public static int PARTY_VOTE_LV;

    public static void loadSystem() {
        PARTY_BUILD_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_BUILD_MAX, 11);
        PARTY_SLOGAN_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_SLOGAN_LEN, 300);
        PARTY_HONOR_RANK_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_HONOR_RANK_LEN, 5);
        PARTY_ELECT_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_ELECT_NUM, 11);
        // 所有官职减去三个顶级的官职，即是军长职务的数量
        MAX_GENERAL = PARTY_ELECT_NUM - 3;
        PARTY_ELECT_RANK_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_ELECT_RANK_NUM, 30);
        PARTY_CANVASS_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_CANVASS_GOLD, 10);
        PARTY_ELECT_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_ELECT_TIME, 12 * 3600);
        PARTY_JOB_DATE = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_JOB_DATE, 14);
        PARTY_TASK_REFRESH_PER_HOUR = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_TASK_REFRESH_PER_HOUR, 3);
        PARTY_TASK_EXT_REWARD_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_TASK_EXT_REWARD_CNT, 1);
        PARTY_JOB_MIN_LV = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_JOB_MIN_LV, 30);
        HONOR_DAILY_RANK = SystemTabLoader.getListListIntSystemValue(SystemId.HONOR_DAILY_RANK, "[[1,10],[1,30]]");
        SUPPLY_CONTINUE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.SUPPLY_CONTINUE_TIME, 86400);
        COST_GOLD_ENERGY_NUM = SystemTabLoader.getListIntSystemValue(SystemId.COST_GOLD_ENERGY_NUM, "[100,5]");
        PARTY_SUPPLY_MAX_CNT_CONF = SystemTabLoader.getMapIntSystemValue(SystemId.PARTY_SUPPLY_MAX_CNT_CONF, "[[3,200]]");
        PARTY_SUPPLY_ONE_KEY_ALL = SystemTabLoader.getListIntSystemValue(SystemId.PARTY_SUPPLY_ONE_KEY_ALL, "[1,2,3]");
        PARTY_VOTE_LV = SystemTabLoader.getIntegerSystemValue(SystemId.PARTY_VOTE_LV, 50);
    }



    /** 军团特权：军长任免权 */
    public static final int PRIVILEGE_COMMAND_APPOINT = 1;
    /** 军团特权：特殊战事权 */
    public static final int PRIVILEGE_SPECIAL_BATTLE = 2;
    /** 军团特权：上线提醒 */
    public static final int PRIVILEGE_LOGIN_NOTIFY = 3;
    /** 军团特权：闪电战免体力 */
    public static final int PRIVILEGE_BLITZ = 4;
    /** 军团特权：奔袭战免体力 */
    public static final int PRIVILEGE_RAID = 5;
    /** 军团特权：远征战免体力 */
    public static final int PRIVILEGE_EXPEDITION = 6;
    /** 军团特权：召唤 */
    public static final int PRIVILEGE_CALL = 7;
    /** 军团特权：修改公告 */
    public static final int PRIVILEGE_SLOGAN = 8;
    /** 军团权限: 修改城池名称 */
    public static final int PRIVILEGE_CITY_RENAME = 9;
    /** 军团权限: 免费决斗 */
    public static final int PRIVILEGE_DECISIVE = 10;
    /** 军团权限：留言板更改 */
    public static final int PRIVILEGE_BBS = 11;

    /** 军团荣誉排行榜类型：城战次数排行榜 */
    public static final int RANK_TYPE_CITY = 1;
    /** 军团荣誉排行榜类型：阵营战次数排行榜 */
    public static final int RANK_TYPE_CAMP = 2;
    /** 军团荣誉排行榜类型：军团建设次数排行榜 */
    public static final int RANK_TYPE_BUILD = 3;

    /** 军团荣誉排行榜其他排名（未进入前列）统一代用名次 */
    public static final int HONOR_RANK_OTHER = -1;

    /** 军团日志分页，每页条数 */
    public static final int PARTY_LOG_PAGE_NUM = 10;

    /** 军团状态：未开启官员功能 */
    public static final int PARTY_STATUS_INIT = 0;
    /** 军团状态：官员投票中 */
    public static final int PARTY_STATUS_ELECT = 1;
    /** 军团状态：已投票结束 */
    public static final int PARTY_STATUS_ELECT_END = 2;

    /** 军团官员任期结束时间，小时 */
    public static final int PARTY_JOB_END_HOUR = 12;

    /** 军团官员撤职 */
    public static final int PARTY_JOB_DISMISS = -1;

    /**
     * 军团官员职务
     */
    public interface Job {
        int KING = 1;// 总司令，国王
        int COMMISSAR = 2;// 政委
        int CHIEF = 3;// 参谋长
        int GENERAL = 4;// 军长
    }

    /**
     * 根据选举排名，返回对应的官职，如果超出系统任命官职范围，返回0
     * 
     * @param rank
     * @return
     */
    public static int getJobByRank(int rank) {
        if (rank == 1) {
            return Job.KING;
        } else if (rank == 2) {
            return Job.COMMISSAR;
        } else if (rank == 3) {
            return Job.CHIEF;
        } else if (rank >= 4 && rank <= PARTY_ELECT_NUM) {
            return Job.GENERAL;
        }
        return 0;
    }

    public interface SupplyType {
        /**
         * 击杀匪军
         */
        int KILL_BANDIT = 1;

        /**
         * 击杀盖世太保
         */
        int KILL_GESTAPO = 2;

        /**
         * 攻克城市
         */
        int CONQUER_CITY = 3;

        /**
         * 充值金币
         */
        int PAY_GOLD = 4;

        /**
         * 参与柏林会战
         */
        int JOIN_BERLIN_WAR = 5;

        /**
         * 首次攻克城市
         */
        int FIRST_CONQUER_CITY = 6;
    }

    // ***********************军团日志模版************************
    /** 城池重建 */
    public static final int LOG_CITY_REBUILD = 1;// %s重建了%s[%s,%s]
    /** 占领城池 */
    public static final int LOG_CITY_CONQUERED = 2;// 据点%s[%s,%s]由我方%s率众占领
    /** 皇城占领达上限 */
    public static final int LOG_CITY_LIMIT = 3;// 据点%s[%s,%s]由我方%s率众攻破，因我军占领据点数已达到上限，故放弃占领，该据点回归中立
    /** 玩家主动撤离城池 */
    public static final int LOG_LEAVE_CITY = 4;// %s深明大义，让出了据点%s[%s,%s]
    /** 城池被攻破 */
    public static final int LOG_CITY_BREACHED = 5;// 我方%s[%s,%s]被%s的%s攻破
    /** 军衔进阶 */
    public static final int LOG_PROMOTE_RANKS = 6;// 经过%s不懈努力，终于成功升级为%s

}
