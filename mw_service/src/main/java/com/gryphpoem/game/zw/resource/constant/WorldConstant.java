package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @author TanDonghai
 * @ClassName WorldConstant.java
 * @Description 世界相关常量配置类
 * @date 创建时间：2017年3月29日 下午3:47:20
 */
public class WorldConstant {

    /**
     * 奔袭战增加时间
     */
    public static int RAID_BATTLE_TIME;

    /**
     * 远征战增加时间
     */
    public static int EXPEDITION_BATTLE_TIME;

    /**
     * 城池最大产出数量
     */
    public static int CITY_MAX_PRODUCE;

    /**
     * 阵营战城池保护时间
     */
    public static int CITY_FREE_TIME;

    /**
     * 侦查类型对加成玩家侦查科技的加成等级
     */
    public static List<Integer> SCOUT_TYPE_ADD;

    /**
     * 侦查允许最大CD时间
     */
    public static int SCOUT_CD_MAX_TIME;

    /**
     * 单次侦查CD时间
     */
    public static int SCOUT_CD;

    /**
     * 城池竞选时间
     */
    public static int CAMPAIGN_TIME;

    /**
     * 城池城主额外奖励道具数量
     */
    public static int CITY_EXTRA_REWARD_NUM;

    /**
     * 城池城主任期时间
     */
    public static int CITY_OWNER_TIME;

    /**
     * 城池单次修复比例
     */
    public static float CITY_REPAIR_RATIO;

    /**
     * 将领洗髓免费次数刷新时间
     */
    public static int HERO_WASH_TIME;

    /**
     * 将领洗髓免费次数上限
     */
    public static int HERO_WASH_FREE_MAX;

    /**
     * 将领至尊洗髓消耗金币数
     */
    public static int HERO_WASH_GOLD;

    /**
     * 个人资源点最大采集队列数
     */
    public static int ACQUISITION_MAX_QUE;

    /**
     * 闪电战[最大时间,消耗体力,消耗道具,增加时间]
     */
    public static List<Integer> FLAY_BATTLE_TIME;
    /**
     * 奔袭战[最大时间,消耗体力,消耗道具,增加时间]
     */
    public static List<Integer> RUN_BATTLE_TIME;
    /**
     * 远征战[最大时间,消耗体力,消耗道具,增加时间]
     */
    public static List<Integer> WALK_BATTLE_TIME;
    /**
     * 军团职位闪电奔袭远征战消耗[[消耗体力,消耗道具]]
     */
    public static List<List<Integer>> PARY_JOB_BATTLE_COST;
    /**
     * 世界任务开启条件:任务id
     */
    public static int WORLD_TASK_OPEN_TASK_ID;
    /**
     * 行军耗补给系数
     */
    public static int MOVE_COST_FOOD;
    /**
     * NPC每4个小时攻城
     */
    public static int CITY_NPC_FIGHT;
    /**
     * 名城最多占领几个
     */
    public static int CITY_TYPE_8_MAX;
    /**
     * 201最近的城cityId
     */
    public static List<List<Integer>> HOME_1_CITY_ROUND;
    /**
     * 201最近的城cityId
     */
    public static List<List<Integer>> HOME_2_CITY_ROUND;
    /**
     * 201最近的城cityId
     */
    public static List<List<Integer>> HOME_3_CITY_ROUND;
    /**
     * 201最近的城cityId
     */
    public static List<List<Integer>> HOME_4_CITY_ROUND;
    /** 都城开发消耗 */
    // public static List<List<Integer>> HOME_DEV_COST;
    /**
     * 都城开发增加人口
     */
    public static int HOME_DEV_GAIN;
    /**
     * 都城开发需要30分钟冷却
     */
    public static int HOME_DEV_CD;
    /**
     * 攻击系数
     */
    public static float ATK_RATIO;
    /**
     * 防御系数
     */
    public static float DEF_RATIO;
    /**
     * 兵力系数
     */
    public static float ARMY_RATIO;
    /**
     * 面板属性系数
     */
    public static int ATTR_RATIO;
    /**
     * 名城人口
     */
    public static int MILDDLE_CITY_PEOPLE;
    /**
     * 超过15分钟不允许发起战斗
     */
    public static int ATK_MAX_TIME;
    /**
     * 打城罩子保护时间分钟
     */
    public static int CITY_PROTECT_TIME;
    /**
     * 需强制刷新流寇的等级（1~6）
     */
    public static int TASK_COMBAT_LV;
    /**
     * 新建角色进入世界时加保护罩
     */
    public static int NEW_LORD_PROTECT_TIME;
    /**
     * 阵营战等级限制
     */
    public static int ATTACK_STATE_NEED_LV;
    /**
     * 采集队伍上限
     */
    public static int MINE_MAX_CNT;

    /**
     * 闪电战、奔袭战、远征战时间上限
     */
    public static List<Integer> MARCH_UPPER_LIMIT_TIME;
    /**
     * 营地战递增时间
     */
    public static int CITY_BATTLE_INCREASE_TIME;
    /**
     * 要塞发起州郡 闪电战、奔袭战、远征战时间
     */
    public static List<Integer> SPECIAL_BATTLE_TIME;
    /**
     * 要塞发起州郡营地战行军时间
     */
    public static int SPECIAL_MARCH_TIME;
    /**
     * 都城开发次数上限
     */
    public static int UP_CAPITALCITY_MAX_CNT;

    /**
     * 前线阵地伤害系数
     */
    public static List<List<Integer>> BATTLE_FRONT_HURT;
    /**
     * 阵地进攻间隔
     */
    public static List<List<Integer>> BATTLE_FRONT_ATK_CD;
    /**
     * 圣域争霸战斗狂热触发配置
     */
    public static List<Integer> BERLIN_BATTLE_FRENZY_TRIGGER_CONF;

    /**
     * 柏林战斗狂热最大攻击轮数
     */
    public static int BERLIN_BATTLE_FRENZY_MAX_ROUND;

    /**
     * 柏林战斗狂热扩展伤害
     */
    private static List<List<Integer>> BERLIN_BATTLE_FRENZY_EXT_HURT;

    /**
     * 关平增援赠送指定品阶关平
     */
    public static List<Integer> GUAN_PING_RESCUE_REWARD;

    /**
     * 柏林会战预显示时间(定时器)
     */
    public static String PRE_VIEW_CRON;
    /**
     * 柏林会战开启时间(定时器)[这不是一个标准的Crontable表达式]
     */
    public static String BERLIN_BEGIN_CRON;
    /**
     * 柏林会战胜利占领时间, 单位: 秒
     */
    public static int BERLIN_WIN_OF_TIME;
    /**
     * 柏林会战强袭消耗金币
     */
    public static int BERLIN_PRESS_GOLD;
    /**
     * 柏林临时读取时间
     */
    public static List<String> BERLIN_TEMP_TIME;
    /**
     * 柏林会战参战角色获得军备箱
     */
    public static List<List<Integer>> BERLIN_JOIN_BATTLE_REWARD;
    /**
     * 柏林将领复活CD
     */
    public static int BERLIN_RESURRECTION_CD;
    /**
     * 柏林补偿奖励
     */
    public static List<List<Integer>> BERLIN_COMPENSATION_AWARD;
    /**
     * 柏林强袭出击金币消耗
     */
    public static List<Integer> BERLIN_PRESS_CNT_CONSUME;
    /**
     * 柏林复活次数金币消耗
     */
    public static List<Integer> BERLIN_RESURRECTION_CNT_CONSUME;
    /**
     * 柏林立即出击金币消耗
     */
    public static List<Integer> BERLIN_IMMEDIATELY_CNT_CONSUME;
    /**
     * 备战柏林耗资源最大次数
     */
    public static int BERLIN_PREWAR_BUFF_BUY_RES_CNT;
    /**
     * 柏林势力值计算的配置
     */
    public static List<Integer> BERLIN_INFLUENCE_CONF;

    /**
     * 免柏林将领复活CD的持续时间
     */
    public static int FREE_BERLIN_RESURRECTION_CD_TIME;
    /**
     * 攻打NPC城池行军速度加倍
     */
    public static int ATTACK_NPC_CITY_MARCH_NUM;
    /**
     * 柏林损兵获得的军工比例 每损1点兵获得5个军功 [损兵数，获得军功数]
     */
    public static List<Integer> BERLIN_LOST_EXPLOIT_NUM;

    /**
     * 德意志反攻活动发起进攻时间 每月的[[第几周,第几周],[周几,开始时间(准点小时数),持续的秒值],[每个阶段的持续时间（秒）]]
     */
    public static List<List<Integer>> COUNTER_ATTACK_BERLIN_TIME_CFG;
    /**
     * 德意志反攻每个阵营选取的玩家个数
     */
    public static int COUNTER_ATTACK_CAMP_HIT_CNT;
    /**
     * 德意志反攻积分 [[损失兵力，得分（玩家进攻boss时)]，[击杀敌人个数，获得积分（boss进攻玩家时)] ,[玩家防守成功得分]，[活动参与分],[击杀boss全民获取积分]]
     */
    public static List<List<Integer>> COUNTER_ATTACK_CREDIT;
    /**
     * 德意志反攻, 难度增加系数
     */
    public static int COUNTER_ATK_FORM_COEF;

    /**
     * 决战教官等级配置
     */
    public static List<Integer> DECISIVE_BATTLE_LEVEL;
    /**
     * 决战失败后的时间配置 [白旗时间，buff时间，冒火时间],读取秒
     */
    public static List<Integer> DECISIVE_BATTLE_FINAL_TIME;
    /**
     * 柏林霸主决战指令奖励
     */
    public static List<List<Integer>> BERLIN_OVERLORD_COMPENSATION_AWARD;
    /**
     * 核弹井生产物品
     */
    public static List<List<Integer>> DECISIVE_BATTLE_PROP;
    /**
     * [每天可免费攻打世界boss次数, 每天可购买攻打世界boss次数 ]
     */
    public static List<Integer> ATK_BOSS_CNT_EVERDAY;
    /**
     * 世界BOSS奖励 [保底经验, 带入公式计算的经验, 获取经验的上限]
     */
    public static List<Integer> SCHEDULE_BOSS_AWARD;
    /**
     * 打城罩子保护时间分钟
     */
    public static int CITY_PROTECT_TIME_NEW_MAP;
    /**
     * 纽约争霸优胜奖
     */
    public static List<List<Integer>> NEWYORK_WAR_JOIN_SUCCESS_AWARD;
    /**
     * 纽约争霸参与奖
     */
    public static List<List<Integer>> NEWYORK_WAR_JOIN_AWARD;
    /**
     * 纽约争霸进入排行榜最低杀敌数量
     */
    public static int NEWYORK_WAR_RANK_MIN_ATTACK;
    /**
     * 纽约争霸每轮攻守持续时间
     */
    public static int NEWYORK_WAR_EACH_ROUND_ATTACK;
    /**
     * 纽约争霸每轮休战持续时间
     */
    public static int NEWYORK_WAR_EACH_ROUND_TRUCE;
    /**
     * 纽约争霸预显示时间
     */
    public static String NEWYORK_WAR_PRE_TIME;
    /**
     * 纽约争霸开启时间
     */
    public static String NEWYORK_WAR_START_END_TIME;
    /**
     * 纽约争霸损兵获得经验比
     */
    public static List<Integer> NEWYORK_WAR_LOST_EXP;
    /**
     * 纽约争霸开启时间条件 单位周
     */
    public static int NEWYORK_WAR_BEGIN_WEEK;
    /**
     * 世界争霸纽约调整系数 除以10000
     */
    public static int WORLD_WAR_CITY_EFFECT;

    /**
     * 快速买兵价格 [[购买1次时兵数，钻石价格],[购买2次时兵数，钻石价格]
     */
    public static List<List<Integer>> QUICK_BUY_ARMY_PRICE;

    /**
     * 快速买兵 最大购买次数
     */
    public static int QUICK_BUY_ARMY_MAX_CNT;

    /**
     * 快速买兵数量占max兵营容量比 系数 万份比
     */
    public static int QUICK_BUY_ARMY_COEF;

    /**
     * 战火燎原功能开启时间配置
     */
    public static List<List<String>> WAR_FIRE_TIME_CONF;

    /**
     * 战火燎原安全区配置
     */
    public static List<List<Integer>> WAR_FIRE_SAFE_AREA;

    /**
     * 战火燎原发放奖励时需要的积分下限
     */
    public static int WAR_FIRE_AWARDS_SCORE_LIMIT;

    /**
     * 战火燎原开启条件配置
     */
    public static List<List<Integer>> WAR_FIRE_OPEN_COND_CONF;

    /**
     * 战火燎原击杀获得积分
     */
    public static List<Integer> WAR_FIRE_KILL_SCORE;

    /**
     * 战火燎原, 退出再进入的CD
     */
    public static int WAR_FIRE_ENTER_CD;

    /**
     * 战火燎原据点单个阵营部队上限
     */
    public static int WAR_FIRE_CITY_ARMY_MAX;

    /**
     * 战火燎原请求支援CD
     */
    public static int WAR_FIRE_CITY_HELP_CD;

    /**
     * 战火燎原行军系数
     */
    public static int WAR_FIRE_MARCH_TIME_COEF;

    /**
     * 跨服战火燎原每击杀X兵力获得积分
     */
    public static List<Integer> CROSS_WAR_FIRE_KILL_SCORE;

    /**
     * 跨服战火燎原退出活动后可再次参加活动时间间隔
     */
    public static int CROSS_WAR_FIRE_ENTER_CD;

    /**
     * 搜索叛军范围（以自身城池为中心）
     */
    public static int SEARCH_THE_RANGE_OF_THE_REBELS;

    /**
     * 根据次数来获取消耗金币数量
     *
     * @param arr
     * @param cnt
     * @return
     */
    public static int getConsumeByCnt(List<Integer> arr, int cnt) {
        if (CheckNull.isEmpty(arr)) {
            return 0;
        }
        if (cnt >= arr.size() - 1) {
            cnt = arr.size() - 1;
        }
        return arr.get(cnt);
    }

    /**
     * 获取柏林临时时间
     *
     * @param needDate
     * @return
     */
    public static Date getBerlinTempDate(int needDate) {
        Date date = new Date();
        if (!CheckNull.isEmpty(BERLIN_TEMP_TIME)) {
            try {
                switch (needDate) {
                    case BERLIN_STATUS_OPEN:
                        date = DateHelper.getDateFormat1().parse(BERLIN_TEMP_TIME.get(0));
                        break;
                    case BERLIN_STATUS_CLOSE:
                        date = DateHelper.getDateFormat1().parse(BERLIN_TEMP_TIME.get(1));
                        break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * 获取柏林会战Cron信息
     *
     * @param needCronData
     * @return
     */
    public static List<String> getBerlinCronInfo(int needCronData, String cronString) {
        ArrayList<String> cronData = new ArrayList<>();
        if (!CheckNull.isNullTrim(cronString)) {
            String[] split = cronString.split(" ");
            switch (needCronData) {
                case BERLIN_CRON_DATE:
                    String time = split[2];
                    String[] beginAndEnd = time.split("-");
                    for (int i = 0; i < beginAndEnd.length; i++) {
                        cronData.add(new StringBuilder(beginAndEnd[i]).append(":00:00").toString());
                    }
                    break;
                case BERLIN_CRON_WEEK:
                    String week = split[5];
                    cronData.add(String.valueOf(TimeHelper.getCalendarDayOfWeek(TimeHelper.getWeekEnConverNum(week))));
                    break;
                case BERLIN_CRON_ATK_CD:
                    String atkCD = split[0];
                    // String[] CD = atkCD.split("/");
                    // TODO: 2018-08-03 这里战斗间隔写死,不支持配置
                    cronData.add("3");
                    break;
            }
        }
        return cronData;
    }

    public static int getNewYorkCron(String cronStr, String regex, int index) {
        Map<Integer, DayOfWeek> map = new HashMap<>(5);
        map.put(1, DayOfWeek.SUNDAY);
        map.put(2, DayOfWeek.MONDAY);
        map.put(3, DayOfWeek.TUESDAY);
        map.put(4, DayOfWeek.WEDNESDAY);
        map.put(5, DayOfWeek.THURSDAY);
        map.put(6, DayOfWeek.FRIDAY);
        map.put(7, DayOfWeek.SATURDAY);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime one = today.with(map.get(Integer.parseInt(cronStr.split(" ")[5])))
                .withHour(Integer.parseInt(cronStr.split(" ")[2].split(regex)[index]))
                .withMinute(0).withSecond(0);
        return (int) one.toEpochSecond(ZoneOffset.of("+8"));
    }

    //    public static float K0;
    public static float K1;
    public static float K2;
    public static float K3;
    public static float K4;
    public static float K5;
    public static float K6;
    public static float K7;
    public static float K8; // 默认兵种克制
    public static float K9; // 阶级克制比
    public static float K10; // 阶级加成

    public static void loadSystem() {
        RAID_BATTLE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.RAID_BATTLE_TIME, 180);
        EXPEDITION_BATTLE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.EXPEDITION_BATTLE_TIME, 300);
        CITY_MAX_PRODUCE = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_MAX_PRODUCE, 6);
        CITY_FREE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_FREE_TIME, 3600);
        SCOUT_TYPE_ADD = SystemTabLoader.getListIntSystemValue(SystemId.SCOUT_TYPE_ADD, "[0,2,4]");
        SCOUT_CD_MAX_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.SCOUT_CD_MAX_TIME, 3600);
        SCOUT_CD = SystemTabLoader.getIntegerSystemValue(SystemId.SCOUT_CD, 0);
        CAMPAIGN_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CAMPAIGN_TIME, 300);
        CITY_EXTRA_REWARD_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_EXTRA_REWARD_NUM, 2);
        CITY_OWNER_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_OWNER_TIME, 86400);
        CITY_REPAIR_RATIO = SystemTabLoader.getFloatSystemValue(SystemId.CITY_REPAIR_RATIO, 0.05f);
        HERO_WASH_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_WASH_TIME, 10800);
        HERO_WASH_FREE_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_WASH_FREE_MAX, 12);
        HERO_WASH_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_WASH_GOLD, 100);
        ACQUISITION_MAX_QUE = SystemTabLoader.getIntegerSystemValue(SystemId.ACQUISITION_MAX_QUE, 2);
        FLAY_BATTLE_TIME = SystemTabLoader.getListIntSystemValue(SystemId.FLAY_BATTLE_TIME, "[300,5,1]");
        RUN_BATTLE_TIME = SystemTabLoader.getListIntSystemValue(SystemId.RUN_BATTLE_TIME, "[600,10,2,300]");
        WALK_BATTLE_TIME = SystemTabLoader.getListIntSystemValue(SystemId.WALK_BATTLE_TIME, "[0,20,4,600]");
        PARY_JOB_BATTLE_COST = SystemTabLoader.getListListIntSystemValue(SystemId.PARY_JOB_BATTLE_COST,
                "[[0,0],[0,0],[5,1]]");
        WORLD_TASK_OPEN_TASK_ID = SystemTabLoader.getIntegerSystemValue(SystemId.WORLD_TASK_OPEN_TASK_ID, 30);
        MOVE_COST_FOOD = SystemTabLoader.getIntegerSystemValue(SystemId.MOVE_COST_FOOD, 5);
        CITY_TYPE_8_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_TYPE_8_MAX, 7);
        CITY_NPC_FIGHT = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_NPC_FIGHT, 4);
        HOME_1_CITY_ROUND = SystemTabLoader.getListListIntSystemValue(SystemId.HOME_1_CITY_ROUND, "[[]]");
        HOME_2_CITY_ROUND = SystemTabLoader.getListListIntSystemValue(SystemId.HOME_2_CITY_ROUND, "[[]]");
        HOME_3_CITY_ROUND = SystemTabLoader.getListListIntSystemValue(SystemId.HOME_3_CITY_ROUND, "[[]]");
        HOME_4_CITY_ROUND = SystemTabLoader.getListListIntSystemValue(SystemId.HOME_4_CITY_ROUND, "[[]]");
        // HOME_DEV_COST = SystemTabLoader.getListListIntSystemValue(SystemId.HOME_DEV_COST, "[[]]");
        HOME_DEV_GAIN = SystemTabLoader.getIntegerSystemValue(SystemId.HOME_DEV_GAIN, 20);
        HOME_DEV_CD = SystemTabLoader.getIntegerSystemValue(SystemId.HOME_DEV_CD, 30);
        ATTR_RATIO = SystemTabLoader.getIntegerSystemValue(SystemId.ATTR_RATIO, 4);
        ATK_RATIO = SystemTabLoader.getIntegerSystemValue(SystemId.ATK_RATIO, 8) * 1.0F / 100;
        DEF_RATIO = SystemTabLoader.getIntegerSystemValue(SystemId.DEF_RATIO, 6) * 1.0F / 100;
        ARMY_RATIO = SystemTabLoader.getIntegerSystemValue(SystemId.ARMY_RATIO, 24) * 1.0F / 100;
        MILDDLE_CITY_PEOPLE = SystemTabLoader.getIntegerSystemValue(SystemId.MILDDLE_CITY_PEOPLE, 300);
        ATK_MAX_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.ATK_MAX_TIME, 300);
        CITY_PROTECT_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_PROTECT_TIME, 120);
        TASK_COMBAT_LV = SystemTabLoader.getIntegerSystemValue(SystemId.TASK_COMBAT_LV, 6);
        NEW_LORD_PROTECT_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.NEW_LORD_PROTECT_TIME, 1728000);
        ATTACK_STATE_NEED_LV = SystemTabLoader.getIntegerSystemValue(SystemId.ATTACK_STATE_NEED_LV, 45);
        MINE_MAX_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.MINE_MAX_CNT, 4);
        DECISIVE_BATTLE_LEVEL = SystemTabLoader.getListIntSystemValue(SystemId.DECISIVE_BATTLE_LEVEL, "[]");
        DECISIVE_BATTLE_FINAL_TIME = SystemTabLoader.getListIntSystemValue(SystemId.DECISIVE_BATTLE_FINAL_TIME,
                "[43200,43200,300]");
        BERLIN_OVERLORD_COMPENSATION_AWARD = SystemTabLoader
                .getListListIntSystemValue(SystemId.BERLIN_OVERLORD_COMPENSATION_AWARD, "[[4,5044,1]]");

        K1 = SystemTabLoader.getFloatSystemValue(SystemId.K1, 1);
        K2 = SystemTabLoader.getFloatSystemValue(SystemId.K2, 1);
        K3 = SystemTabLoader.getFloatSystemValue(SystemId.K3, 1);
        K4 = SystemTabLoader.getFloatSystemValue(SystemId.K4, 1);
        K5 = SystemTabLoader.getFloatSystemValue(SystemId.K5, 1);
        K6 = SystemTabLoader.getFloatSystemValue(SystemId.K6, 1);
        K7 = SystemTabLoader.getFloatSystemValue(SystemId.K7, 1);
        K8 = SystemTabLoader.getFloatSystemValue(SystemId.K8, 0.05f);
        K9 = SystemTabLoader.getFloatSystemValue(SystemId.K9, 0.01f);
        K10 = SystemTabLoader.getFloatSystemValue(SystemId.K10, 0.01f);

        MARCH_UPPER_LIMIT_TIME = SystemTabLoader.getListIntSystemValue(SystemId.MARCH_UPPER_LIMIT_TIME, "[300,600,0]");
        CITY_BATTLE_INCREASE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_BATTLE_INCREASE_TIME, 300);
        SPECIAL_BATTLE_TIME = SystemTabLoader.getListIntSystemValue(SystemId.SPECIAL_BATTLE_TIME, "[299,599,899]");
        SPECIAL_MARCH_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.SPECIAL_MARCH_TIME, 299);
        UP_CAPITALCITY_MAX_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.UP_CAPITALCITY_MAX_CNT, 11);

        CAPTAIN_CITYIDS.clear();
        CAPTAIN_CITYIDS.add(HOME_CITY_1);
        CAPTAIN_CITYIDS.add(HOME_CITY_2);
        CAPTAIN_CITYIDS.add(HOME_CITY_3);
        CAPTAIN_CITYIDS.add(HOME_CITY_4);

        BATTLE_FRONT_HURT = SystemTabLoader.getListListIntSystemValue(SystemId.BATTLE_FRONT_HURT, "[[1,10,2],[11,11,4],[12,13,4]]");
        BATTLE_FRONT_ATK_CD = SystemTabLoader.getListListIntSystemValue(SystemId.BATTLE_FRONT_ATK_CD, "[[1,10,25],[11,11,25],[12,13,25]]");
        BERLIN_BATTLE_FRENZY_TRIGGER_CONF = SystemTabLoader.getListIntSystemValue(SystemId.BERLIN_BATTLE_FRENZY_TRIGGER_CONF, "[3000,6000,9000]");
        BERLIN_BATTLE_FRENZY_MAX_ROUND = SystemTabLoader.getIntegerSystemValue(SystemId.BERLIN_BATTLE_FRENZY_MAX_ROUND, 60);
        BERLIN_BATTLE_FRENZY_EXT_HURT = SystemTabLoader.getListListIntSystemValue(SystemId.BERLIN_BATTLE_FRENZY_EXT_HURT, "[[11,13,50]]");
        GUAN_PING_RESCUE_REWARD = SystemTabLoader.getListIntSystemValue(SystemId.GUAN_PING_RESCUE_REWARD, "[]");
        PRE_VIEW_CRON = SystemTabLoader.getStringSystemValue(SystemId.PRE_VIEW_CRON, "0 * 12 * * 4");
        BERLIN_BEGIN_CRON = SystemTabLoader.getStringSystemValue(SystemId.BERLIN_BEGIN_CRON, "0/3 * 20-21 * * 5");
        BERLIN_WIN_OF_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.BERLIN_WIN_OF_TIME, 1800);
        BERLIN_PRESS_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.BERLIN_PRESS_GOLD, 10);
        BERLIN_TEMP_TIME = SystemTabLoader.getListStringSystemValue(SystemId.BERLIN_TEMP_TIME,
                "['2018-08-17 20:00:00','2018-08-17 21:00:00']");
        BERLIN_JOIN_BATTLE_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.BERLIN_JOIN_BATTLE_REWARD,
                "[[4,1807,5],[4,1807,2]]");
        BERLIN_RESURRECTION_CD = SystemTabLoader.getIntegerSystemValue(SystemId.BERLIN_RESURRECTION_CD, 60);
        BERLIN_COMPENSATION_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.BERLIN_COMPENSATION_AWARD,
                "[[4,5003,1]]");
        BERLIN_PRESS_CNT_CONSUME = SystemTabLoader.getListIntSystemValue(SystemId.BERLIN_PRESS_CNT_CONSUME,
                "[10,30,50,80,100,150]");
        BERLIN_RESURRECTION_CNT_CONSUME = SystemTabLoader
                .getListIntSystemValue(SystemId.BERLIN_RESURRECTION_CNT_CONSUME, "[10,30,50,80,100,150]");
        BERLIN_IMMEDIATELY_CNT_CONSUME = SystemTabLoader.getListIntSystemValue(SystemId.BERLIN_IMMEDIATELY_CNT_CONSUME,
                "[10,30,50,80,100,150]");
        BERLIN_PREWAR_BUFF_BUY_RES_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.BERLIN_PREWAR_BUFF_BUY_RES_CNT,
                8);
        BERLIN_INFLUENCE_CONF = SystemTabLoader.getListIntSystemValue(SystemId.BERLIN_INFLUENCE_CONF, "[]");
        FREE_BERLIN_RESURRECTION_CD_TIME = SystemTabLoader
                .getIntegerSystemValue(SystemId.FREE_BERLIN_RESURRECTION_CD_TIME, 60);
        ATTACK_NPC_CITY_MARCH_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.ATTACK_NPC_CITY_MARCH_NUM, 5000);
        COUNTER_ATTACK_BERLIN_TIME_CFG = SystemTabLoader
                .getListListIntSystemValue(SystemId.COUNTER_ATTACK_BERLIN_TIME_CFG, "[[]]");
        COUNTER_ATTACK_CAMP_HIT_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.COUNTER_ATTACK_CAMP_HIT_CNT, 10);
        COUNTER_ATTACK_CREDIT = SystemTabLoader.getListListIntSystemValue(SystemId.COUNTER_ATTACK_CREDIT,
                "[[100,1],[200,1],[300],[200],[2000]]");
        BERLIN_LOST_EXPLOIT_NUM = SystemTabLoader.getListIntSystemValue(SystemId.BERLIN_LOST_EXPLOIT_NUM, "[1,5]");
        ATK_BOSS_CNT_EVERDAY = SystemTabLoader.getListIntSystemValue(SystemId.ATK_BOSS_CNT_EVERDAY, "[1,1]");
        SCHEDULE_BOSS_AWARD = SystemTabLoader.getListIntSystemValue(SystemId.SCHEDULE_BOSS_AWARD,
                "[3000,5000]");
        COUNTER_ATK_FORM_COEF = SystemTabLoader.getIntegerSystemValue(SystemId.COUNTER_ATK_FORM_COEF, 100);
        CITY_PROTECT_TIME_NEW_MAP = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_PROTECT_TIME_NEW_MAP, 30);

        NEWYORK_WAR_JOIN_SUCCESS_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.NEWYORK_WAR_JOIN_SUCCESS_AWARD,
                "[[18,1,200]]");
        NEWYORK_WAR_JOIN_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.NEWYORK_WAR_JOIN_AWARD,
                "[[18,1,100]]");
        NEWYORK_WAR_RANK_MIN_ATTACK = SystemTabLoader.getIntegerSystemValue(SystemId.NEWYORK_WAR_RANK_MIN_ATTACK, 10000);
        NEWYORK_WAR_EACH_ROUND_ATTACK = SystemTabLoader.getIntegerSystemValue(SystemId.NEWYORK_WAR_EACH_ROUND_ATTACK, 10);
        NEWYORK_WAR_EACH_ROUND_TRUCE = SystemTabLoader.getIntegerSystemValue(SystemId.NEWYORK_WAR_EACH_ROUND_TRUCE, 5);
        NEWYORK_WAR_PRE_TIME = SystemTabLoader.getStringSystemValue(SystemId.NEWYORK_WAR_PRE_TIME, "0 0 12 ? * 7");
        NEWYORK_WAR_START_END_TIME = SystemTabLoader.getStringSystemValue(SystemId.NEWYORK_WAR_START_END_TIME, "0 0 20-21 ? * 1");
        NEWYORK_WAR_LOST_EXP = SystemTabLoader.getListIntSystemValue(SystemId.NEWYORK_WAR_LOST_EXP, "[8,1]");
        NEWYORK_WAR_BEGIN_WEEK = SystemTabLoader.getIntegerSystemValue(SystemId.NEWYORK_WAR_BEGIN_WEEK, 5);
        WORLD_WAR_CITY_EFFECT = SystemTabLoader.getIntegerSystemValue(SystemId.WORLD_WAR_CITY_EFFECT, 500);
        // QUICK_BUY_ARMY_PRICE = SystemTabLoader.getListListIntSystemValue(SystemId.QUICK_BUY_ARMY_PRICE, "[[5000,240],[5000,260],[5000,280]]");
        // QUICK_BUY_ARMY_MAX_CNT = QUICK_BUY_ARMY_PRICE.size();
        QUICK_BUY_ARMY_COEF = SystemTabLoader.getIntegerSystemValue(SystemId.QUICK_BUY_ARMY_COEF, 5000);
        WAR_FIRE_TIME_CONF = SystemTabLoader.getListListSystemValue(SystemId.WAR_FIRE_TIME_CONF, "[[TUE,08:00:00],[WED,20:00:00,21:00:00],[WED,23:59:59],[5]]");
        WAR_FIRE_SAFE_AREA = SystemTabLoader.getListListIntSystemValue(SystemId.WAR_FIRE_SAFE_AREA, "[[5,5],[55,5],[5,55]]");
        WAR_FIRE_AWARDS_SCORE_LIMIT = SystemTabLoader.getIntegerSystemValue(SystemId.WAR_FIRE_AWARDS_SCORE_LIMIT, 2000);
        WAR_FIRE_OPEN_COND_CONF = SystemTabLoader.getListListIntSystemValue(SystemId.WAR_FIRE_OPEN_COND_CONF, "[[80],[30],[1,13]]");
        WAR_FIRE_KILL_SCORE = SystemTabLoader.getListIntSystemValue(SystemId.WAR_FIRE_KILL_SCORE, "[10000,800]");
        WAR_FIRE_ENTER_CD = SystemTabLoader.getIntegerSystemValue(SystemId.WAR_FIRE_ENTER_CD, 10);
        WAR_FIRE_CITY_ARMY_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.WAR_FIRE_CITY_ARMY_MAX, 10);
        WAR_FIRE_CITY_HELP_CD = SystemTabLoader.getIntegerSystemValue(SystemId.WAR_FIRE_CITY_HELP_CD, 300);
        WAR_FIRE_MARCH_TIME_COEF = SystemTabLoader.getIntegerSystemValue(SystemId.WAR_FIRE_MARCH_TIME_COEF, 25000);
        CROSS_WAR_FIRE_KILL_SCORE = SystemTabLoader.getListIntSystemValue(SystemId.CROSS_WAR_FIRE_KILL_SCORE, "[10000,800]");
        CROSS_WAR_FIRE_ENTER_CD = SystemTabLoader.getIntegerSystemValue(SystemId.CROSS_WAR_FIRE_ENTER_CD, 900);
        SEARCH_THE_RANGE_OF_THE_REBELS = SystemTabLoader.getIntegerSystemValue(SystemId.SEARCH_THE_RANGE_OF_THE_REBELS, 10);
    }

    // 4个都城id
    public static final int HOME_CITY_1 = 201;
    public static final int HOME_CITY_2 = 202;
    public static final int HOME_CITY_3 = 203;
    public static final int HOME_CITY_4 = 204;
    // 都城id的集合
    public static Set<Integer> CAPTAIN_CITYIDS = new HashSet<>();
    /**
     * 城
     */
    public static final int CITY_TYPE_3 = 3;
    public static final int CITY_TYPE_HOME = 7;// 都城总部
    public static final int CITY_TYPE_8 = 8;// 名城
    public static final int CITY_TYPE_KING = 9;// 王城

    public static final int AREA_TYPE_13 = 13;// 要塞

    public static final int ARMY_TYPE_HLEP = -1;// 不会被召回的部队
    public static final int ARMY_TYPE_WALL_NPC = -2;// 城池NPC部队

    public static final int REFRESH_TYPE_BANDIT_1 = 1;// 刷新玩家周围流寇
    public static final int REFRESH_TYPE_BANDIT_2 = 2;// 刷新世界流寇
    public static final int REFRESH_TYPE_BANDIT_3 = 3;// 清理并刷新世界流寇
    public static final int REFRESH_TYPE_BANDIT_4 = 4;// 玩家上线刷新周围流寇上线

    public static final int REFRESH_TYPE_MINE_1 = 1;// 刷矿点规则1
    public static final int REFRESH_TYPE_MINE_2 = 2;// 刷矿点规则2
    /**
     * 柏林cityId
     */
    public static final int BERLIN_CITY_ID = 321;
    // 城市首杀
    public static final String KILL_SPONSOR = "sponsor";// 发起者
    public static final String KILL_ATKLIST = "atkList";// 参与者

    /**
     * 根据侦查类型获取侦查科技等级加成
     *
     * @param scoutType
     * @return
     */
    public static int getScoutAddByType(int scoutType) {
        if (CheckNull.isEmpty(SCOUT_TYPE_ADD) || SCOUT_TYPE_ADD.size() < scoutType) {
            return 0;
        }

        if (scoutType < SCOUT_TYPE_PRIMARY || scoutType > SCOUT_TYPE_SENIOR) {
            return 0;
        }

        return SCOUT_TYPE_ADD.get(scoutType - 1);
    }

    /**
     * 世界地图分区开启次序：默认开启
     */
    public static final int AREA_ORDER_1 = 1;
    /**
     * 世界地图分区开启次序：初次解锁开启
     */
    public static final int AREA_ORDER_2 = 2;
    /**
     * 世界地图分区开启次序：二次解锁开启、皇城
     */
    public static final int AREA_ORDER_3 = 3;

    /**
     * 分区状态：未开启
     */
    public static final int AREA_STATUS_CLOSE = 0;
    /**
     * 分区状态：已开启
     */
    public static final int AREA_STATUS_OPEN = 1;
    /**
     * 分区状态：已开通下一级分区
     */
    public static final int AREA_STATUS_PASS = 2;

    /**
     * 城池状态：空闲
     */
    public static final int CITY_STATUS_CALM = 0;
    /**
     * 城池状态：国战开启
     */
    public static final int CITY_STATUS_BATTLE = 1;
    /**
     * 城池状态：保护
     */
    public static final int CITY_STATUS_FREE = 2;

    /**
     * 世界地图上的实例类型：玩家
     */
    public static final int FORCE_TYPE_PLAYER = 1;
    /**
     * 世界地图上的实例类型：流寇
     */
    public static final int FORCE_TYPE_BANDIT = 2;
    /**
     * 世界地图上的实例类型：矿点
     */
    public static final int FORCE_TYPE_MINE = 3;
    /**
     * 世界地图上的实例类型:点兵统领
     */
    public static final int FORCE_TYPE_CABINET_LEAD = 4;
    /**
     * 世界地图上的实例类型:盖世太保
     */
    public static final int FORCE_TYPE_GESTAPO = 5;
    /**
     * 世界地图上的实例类型:超级矿点
     */
    public static final int FORCE_TYPE_SPUER_MINE = 6;
    /**
     * 世界地图上的实例类型:飞艇
     */
    public static final int FORCE_TYPE_AIRSHIP = 7;
    /**
     * 世界地图上的实例类型:世界进度boss
     */
    public static final int FORCE_TYPE_SCHEDULE_BOSS = 8;
    /**
     * 新地图上的安全区
     */
    public static final int FORCE_TYPE_WAR_FIRE_SAFE_AREA = 9;
    /**
     * 世界地图上的实例类型:圣坛
     */
    public static final int FORCE_TYPE_ALTAR = 10;

    /**
     * 世界地图实例类型：遗迹
     */
    public static final int FORCE_TYPE_RELIC = 11;

    /**
     * 战斗类型：城战 [打玩家]
     */
    public static final int BATTLE_TYPE_CITY = 1;
    /**
     * 战斗类型：国战、阵营战 [打城池]
     */
    public static final int BATTLE_TYPE_CAMP = 2;
    /**
     * 战斗类型：盖世太保战
     */
    public static final int BATTLE_TYPE_GESTAPO = 3;
    /**
     * 战斗类型：闪电战
     */
    public static final int BATTLE_TYPE_LIGHTNING_WAR = 4;
    /**
     * 战斗类型：柏林会战(圣域之战, 卡美洛)
     */
    public static final int BATTLE_TYPE_BERLIN_WAR = 5;
    /**
     * 战斗类型：超级矿点战斗
     */
    public static final int BATTLE_TYPE_SUPER_MINE = 6;
    /**
     * 战斗类型: 匪军叛乱 (叛军来袭)
     */
    public static final int BATTLE_TYPE_REBELLION = 7;
    /**
     * 战斗类型: 反攻德意志
     */
    public static final int BATTLE_TYPE_COUNTER_ATK = 8;
    /**
     * 战斗类型: 决战
     */
    public static final int BATTLE_TYPE_DECISIVE_BATTLE = 9;
    /**
     * 战斗类型: 纽约争霸
     */
    public static final int BATTLE_TYPE_NEW_YORK_WAR = 10;

    /**
     * 战斗类型: 飞艇（多人叛军）
     */
    public static final int BATTLE_TYPE_AIRSHIP = 11;

    /**
     * 战斗类型: 叛军
     */
    public static final int BATTLE_TYPE_BANDIT = 12;

    /**
     * 战斗类型: 采集战斗
     */
    public static final int BATTLE_TYPE_MINE_GUARD = 13;

    /**
     * 战斗类型: 王朝遗迹
     */
    public static final int BATTLE_TYPE_HIS_REMAIN = 14;

    /**
     * 战斗类型: 叛军入侵
     */
    public static final int BATTLE_TYPE_REBEL_INVADE = 15;

    /**
     * 是否计算攻坚或者据守
     *
     * @param battleType
     * @return
     */
    public static final boolean calcAttackOrDefend(int battleType) {
        return battleType == WorldConstant.BATTLE_TYPE_CITY || battleType == WorldConstant.BATTLE_TYPE_CAMP
                || battleType == WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE;
    }

    /**
     * 迁城类型：低级迁城（纯随机）
     */
    public static final int MOVE_TYPE_RANDOM = 1;
    /**
     * 迁城类型：定区域
     */
    public static final int MOVE_TYPE_AREA = 2;
    /**
     * 迁城类型：定点
     */
    public static final int MOVE_TYPE_POS = 3;
    /**
     * 迁城类型: 郡迁城令
     */
    public static final int MOVE_TYPE_OPEN_ORDER_1 = 4;
    /**
     * 前往州城
     */
    public static final int MOVE_TYPE_TO_STATE = 5;

    /**
     * 城战类型：闪电战
     */
    public static final int CITY_BATTLE_BLITZ = 1;
    /**
     * 城战类型：奔袭战
     */
    public static final int CITY_BATTLE_RAID = 2;
    /**
     * 城战类型：远征战
     */
    public static final int CITY_BATTLE_EXPEDITION = 3;
    /**
     * 城战类型：决战
     */
    public static final int CITY_BATTLE_DECISIVE = 4;
    /**
     * 城战类型：跨服驻防
     */
    public static final int CROSS_CITY_GARRISON_WALL = 11;

    /**
     * 侦查科技id
     */
    public static final int SCOUT_TECH_ID = 11;

    /**
     * 侦查类型：初级
     */
    public static final int SCOUT_TYPE_PRIMARY = 1;
    /**
     * 侦查类型：中级
     */
    public static final int SCOUT_TYPE_MIDDLE = 2;
    /**
     * 侦查类型：高级
     */
    public static final int SCOUT_TYPE_SENIOR = 3;

    /**
     * 侦查结果：失败
     */
    public static final int SCOUT_RET_FAIL = 0;
    /**
     * 侦查结果：只获取资源信息
     */
    public static final int SCOUT_RET_SUCC1 = 1;
    /**
     * 侦查结果：获取资源、城池信息
     */
    public static final int SCOUT_RET_SUCC2 = 2;
    /**
     * 侦查结果：获取资源、城池、将领信息
     */
    public static final int SCOUT_RET_SUCC3 = 3;

    /**
     * 侦查将领来源：玩家上阵将领
     */
    public static final int HERO_SOURCE_BATTLE = 1;
    /**
     * 侦查将领来源：城防将
     */
    public static final int HERO_SOURCE_DEF_HERO = 2;
    /**
     * 侦查将领来源：城防军
     */
    public static final int HERO_SOURCE_DEF_ARM = 3;
    /**
     * 侦查将领来源：友军驻防
     */
    public static final int HERO_SOURCE_GUARD = 4;

    /**
     * 侦查将领状态：驻守城池
     */
    public static final int SCOUT_HERO_STATE_DEF = 1;
    /**
     * 侦查将领状态：出征中
     */
    public static final int SCOUT_HERO_STATE_OUT = 2;
    /**
     * 侦查将领状态：采集中
     */
    public static final int SCOUT_HERO_STATE_COLLECT = 3;
    /**
     * 侦查将领状态：友军驻守中
     */
    public static final int SCOUT_HERO_STATE_GUARD = 4;

    /**
     * CD类型：侦查
     */
    public static final int CD_TYPE_SCOUT = 1;

    /**
     * 队列免费加速类型：资源点免费加速
     */
    public static final int SPEED_TYPE_ACQUISITE = 1;
    /**
     * 队列免费加速类型：VIP特权
     */
    public static final int SPEED_TYPE_VIP = 2;
    public static final int INIT_WORLD_TASKID = 2; // 初始世界任务ID

    /**
     * 通知被攻击玩家找到攻击
     */
    public static final int ATTACK_ROLE_1 = 1;
    /**
     * 通知被攻击玩家取消攻击警报
     */
    public static final int ATTACK_ROLE_0 = 0;

    /**
     * 区域ID: 下限
     */
    public static final int AREA_MIN_ID = 0;
    /**
     * 区域ID: 上限
     */
    public static final int AREA_MAX_ID = 25;

    /**
     * 夜袭活动恢复兵力
     */
    public static final int LOST_RECV_CALC_NIGHT = 1;
    /**
     * 太保活动恢复兵力
     */
    public static final int LOST_RECV_CALC_GESTAPO = 2;

    /**
     * 闪电战Boss状态: 空闲
     */
    public static final int BOSS_STATUS_CALM = 0;
    /**
     * 闪电战Boss状态: 战斗中
     */
    public static final int BOSS_STATUS_BATTLE = 1;
    /**
     * 闪电战Boss状态: 死亡
     */
    public static final int BOSS_STATUS_DEAD = 2;

    /**
     * 闪电战发送推送: key
     */
    public static final int CHAT_TIME_KEY = 0;

    /**
     * 决战状态：战斗中
     */
    public static final int DECISIVE_BATTLE_ING = 1;
    /**
     * 决战状态：失败
     */
    public static final int DECISIVE_BATTLE_FINAL = 2;

    /**
     * 柏林会战加入战斗: 普通
     */
    public static final int BERLIN_ATTACK_TYPE_COMMON = 1;
    /**
     * 柏林会战加入战斗: 强袭
     */
    public static final int BERLIN_ATTACK_TYPE_PRESS = 2;
    /**
     * 柏林会战: 进攻方
     */
    public static final int BERLIN_ATK = 1;
    /**
     * 柏林会战: 防守方
     */
    public static final int BERLIN_DEF = 2;
    /**
     * 柏林会战: 日期信息
     */
    public static final int BERLIN_CRON_DATE = 1;
    /**
     * 柏林会战: 星期
     */
    public static final int BERLIN_CRON_WEEK = 2;
    /**
     * 柏林会战: 进攻CD
     */
    public static final int BERLIN_CRON_ATK_CD = 3;
    /**
     * 柏林会战: 预显示
     */
    public static final int BERLIN_STATUS_PRE_DISPLAY = 1;
    /**
     * 柏林会战: 开启
     */
    public static final int BERLIN_STATUS_OPEN = 2;
    /**
     * 柏林会战: 关闭
     */
    public static final int BERLIN_STATUS_CLOSE = 3;
    /**
     * 柏林会战排行Type: 连续击杀兵力
     */
    public static final int BERLIN_RANK_KILL_STREAK_ARMY = 1;
    /**
     * 柏林会战排行Type: 总杀敌数量
     */
    public static final int BERLIN_RANK_KILL_ARMY_CNT = 2;
    /**
     * 柏林会战: 战前buff开启时间
     */
    public static final int BERLIN_PREWAR_BUFF_TIME = 15;

    public interface CityBuffer {

        /**
         * 石油采集加成
         */
        int COLLECT_OIL_BUFFER = 1;

        /**
         * 电力采集加成
         */
        int COLLECT_ELE_BUFFER = 2;

        /**
         * 补给采集加成
         */
        int COLLECT_FOOD_BUFFER = 3;

        /**
         * 矿石采集加成
         */
        int COLLECT_ORE_BUFFER = 4;

        /**
         * 生产部队数量增加
         */
        int ADD_ARMY_RECRUIT = 5;

        /**
         * 上阵将领防御提升
         */
        int DEF_BUFFER = 6;

        /**
         * 行军时间减少
         */
        int MARCH_BUFFER = 7;

        /**
         * 击败流寇，获取的资源增益
         */
        int CABINET_AWARD_BUFFER = 8;

        /**
         * 军事禁区: 行军时间减少
         */
        int MILITARY_RESTRICTED_ZONES = 9;

        int[] COLLECT_BUFFER = {COLLECT_OIL_BUFFER, COLLECT_ELE_BUFFER, COLLECT_FOOD_BUFFER, COLLECT_ORE_BUFFER};
    }

    /**
     * Begin-End 根据 intervalTime 周期性执行
     */
    public static final String BL_LW_START_CALLBACK_NAME = "BL_LW_START_CALLBACK_NAME";
    /**
     * EndTime 执行
     */
    public static final String BL_LW_END_CALLBACK_NAME = "BL_LW_END_CALLBACK_NAME";
    /**
     * BOSS 防守
     */
    public static final int COUNTER_ATK_DEF = 0;
    /**
     * BOSS 进攻
     */
    public static final int COUNTER_ATK_ATK = 1;
    /**
     * BOSS 状态 防守
     */
    public static final int COUNTER_ATK_BOSS_STATUS_DEF = 1;
    /**
     * BOSS 状态 进攻
     */
    public static final int COUNTER_ATK_BOSS_STATUS_ATK = 2;
    /**
     * BOSS 状态 死亡
     */
    public static final int COUNTER_ATK_BOSS_STATUS_DEAD = 3;

    /**
     * 坐标发生改变的类型, 1 迁城, 2 被击飞 , 3 召唤
     */
    public static final int CHANGE_POS_TYPE_1 = 1;
    public static final int CHANGE_POS_TYPE_2 = 2;
    public static final int CHANGE_POS_TYPE_3 = 3;


    /**
     * 解救任务， 打匪军999级的
     */
    public static final int BANDIT_LV_999 = 999;

    public static int berlinAoeExtHurt(int schdeule) {
        if (CheckNull.isEmpty(WorldConstant.BERLIN_BATTLE_FRENZY_EXT_HURT)) {
            return 0;
        }
        return WorldConstant.BERLIN_BATTLE_FRENZY_EXT_HURT.stream().filter(conf -> schdeule >= conf.get(0) && schdeule <= conf.get(1)).mapToInt(conf -> conf.get(2)).findAny().orElse(0);
    }

}
