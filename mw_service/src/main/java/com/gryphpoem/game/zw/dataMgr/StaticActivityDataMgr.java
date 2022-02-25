package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticIniDao;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.ActivityTriggerService;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class StaticActivityDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
    private static StaticIniDao staticIniDao = DataResource.ac.getBean(StaticIniDao.class);

    private static ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);

    // key: activityId
    private static Map<Integer, List<StaticActAward>> awardMap;
    // key: keyId
    private static Map<Integer, StaticActAward> actAwardMap;

    // 固定活动 key: activityId
    private static Map<Integer, StaticActivityOpen> activityOpenMap;

    private static List<ActivityBase> activityList;

    private static Map<Integer, List<StaticActivityTime>> activityTimeMap = new HashMap<>();

    private static Map<Integer, Map<Integer, List<StaticActRank>>> actRankMap;

    // private Map<Integer, List<StaticActivityTime>> activityTimeMap = new
    // HashMap<Integer,
    // List<StaticActivityTime>>();

    private static Map<Integer, Map<Integer, StaticActivityEffect>> activityEffectMap;

//    private static Map<Integer, List<StaticDay7Act>> day7ActList;
//    private static Map<Integer, StaticDay7Act> day7ActMap;
//    private static Map<Integer, List<StaticDay7Act>> day7ActTypeList;

    public static Map<Integer, StaticDay7Act> getAct7DayMap() {
        return act7DayMap;
    }

    private static Map<Integer, StaticDay7Act> act7DayMap = new HashMap<>();

    // 攻城掠地活动 key: keyId
    private static Map<Integer, StaticAtkCityAct> atkCityActMap;

    private static List<StaticAtkCityAct> atkCityActList;

    // key: taskType
    private static Map<Integer, List<StaticAtkCityAct>> atkCityActTypeMap;

    // 黑鹰计划活动 key: keyId

    private static Map<Integer, StaticActBlackhawk> actBlackhawkMap;

    // 礼包活动的礼包 key: giftpackId
    private static Map<Integer, StaticActGiftpack> actGiftpackMap;
    // 礼包活动的计划
    private static List<StaticGiftpackPlan> giftpackPlanList;
    // 触发式礼包活动的计划
    private static List<StaticGiftPackTriggerPlan> giftPackTriggerPlanList;
    // // 触发式礼包配置 key: keyId
    private static Map<Integer,StaticTriggerConf> triggerConfMapList;
    // key: triggerId
    private static Map<Integer, List<StaticTriggerConf>> triggerConfMap;
    // key: giftId
    private static Map<Integer, StaticTriggerConf> GiftConfMap;
    // 每个排行榜的容量,key:activiyId,value:容量
    private static Map<Integer, Integer> rankCapacityMap;
    // 每个排行榜的奖励数据,带排序 key:activiyId
    private static Map<Integer, List<StaticActAward>> rankActMap;

    private static Map<Integer, StaticActivity> staticActivityMap;
    // 每日充值计算缓存
    private static Map<Integer, List<StaticActAward>> actDailyPayCacheMap = new HashMap<>();

    // 兑换活动配置 key: activiyId
    private static Map<Integer, List<StaticActExchange>> staticActExchangeMap;
    // 兑换活动配置 key: keyId
    private static Map<Integer, StaticActExchange> staticActExchangeIdMap;

    // 打折礼包配置 key: promotionId
    private static Map<Integer, StaticPromotion> staticPromotionMap;
    // 打折礼包配置 key: type
    private static Map<Integer, List<StaticPromotion>> staticPromotionTypeMap;
    // 打折礼包配置 key: actId
    private static Map<Integer, List<StaticPromotion>> staticPromotionActIdMap;

    // 幸运转盘配置 key: activityId
    private static Map<Integer, List<StaticTurnplateConf>> staticTurnplateConf;
    // 幸运转盘配置 key: TurnplateId
    private static Map<Integer, StaticTurnplateConf> staticTurnplateIdMap;
    // 转盘的次数奖励 key: key
    private static Map<Integer, StaticTurnplateExtra> staticTurnplateExtraMap;
    // 转盘的次数奖励 key: actId
    private static Map<Integer, List<StaticTurnplateExtra>> staticTurnplateExtraActIdMap;

    // 装备转盘配置 key: activityId
    private static Map<Integer, List<StaticEquipTurnplateConf>> staticEquipTurnplateConf;
    // 装备转盘配置 key: TurnplateId
    private static Map<Integer, StaticEquipTurnplateConf> staticEquipTurnplateIdMap;
    // 充值转盘 key:activityId
    private static Map<Integer, List<StaticActPayTurnplate>> actPayTurnplateMap;
    // 矿石转盘 key:activityId
    private static Map<Integer, List<StaticActOreTurnplate>> actOreTurnplateMap;
    // 每日特惠活动
    private static Map<Integer, StaticActDaydiscounts> actDaydiscountsMap;
    // 每日特惠活动, key: activityId
    private static Map<Integer, List<StaticActDaydiscounts>> actDaydiscountsListMap;
    // 大富翁活动配置 key:keyId
    private static Map<Integer, StaticActMonopoly> actMonopolyMap;
    // 大富翁活动配置 key:activityId
    private static Map<Integer, List<StaticActMonopoly>> actMonopolyByActId;
    // 大富翁活动的档位的配置 key:activityId
    private static Map<Integer, List<StaticActMonopolyCount>> actMonopolyCountMap;
    // 特殊活动
    private static List<StaticSpecialPlan> specialPlans;
    /** 改*巴顿活动[key:activityId] */
    private static Map<Integer,List<StaticActBarton>> actBartonList;
    /**
     * 掉落活动[key:type] 1 击飞玩家 2 攻打匪军 3 攻打精英叛军
     */
    private static Map<Integer, List<StaticActBandit>> actBanditMap;
    /**
     * 掉落活动[key:type] 1 击飞玩家 2 攻打匪军
     */
    private static List<StaticActBandit> actBanditList;
    /**
     * 签到活动key: activityId, key:time
     */
    private static Map<Integer, Map<Integer, StaticActLogin>> actLoginDayiyMap;

    /**
     * 罗宾汉活动 key: activityId
     */
    private static Map<Integer, List<StaticActRobinHood>> actRobinHoodMap;


    /**
     * 阵营比拼 key: activityId
     */
    private static Map<Integer, List<StaticRoyalArenaTask>> royalArenaTaskMap;

    /**
     * 阵营比拼 key: activityId
     */
    private static Map<Integer, List<StaticRoyalArenaAward>> royalArenaAwardMap;

    /**
     * 复活节活动 key: activityId
     */
    private static Map<Integer, List<StaticEasterAward>> easterAwardMap;

    /**
     * 复活节活动 key: activityId, key: condId
     */
    private static Map<Integer, Map<Integer, List<StaticEasterAward>>> easterCondAwardMap;

    /**
     * 复活节活动 key: keyId
     */
    private static Map<Integer, StaticEasterAward> easterAwardKeyMap;

    /**
     * 热销商品
     * <actId, <tab, 热销商品的配置>>
     */
    private static Map<Integer, Map<Integer, List<StaticActHotProduct>>> actHotProductMap;

    /**
     * keyId: keyId, 热销商品的配置
     */
    private static Map<Integer, StaticActHotProduct> actHotProductKeyMap;

    /**
     * keyId: activityId, 代币转换配置
     */
    private static Map<Integer, List<StaticActVoucher>> actVoucherMap;
    /**
     * keyId:拍卖品id value:拍卖品
     */
    private static Map<Integer, StaticActAuction> staticActAuctionMap;
    private static Map<Integer, List<StaticActAuction>> staticActIdAuctionMap;

    /**
     * 喜悦金秋-日出而作活动任务配置
     */
    private static Map<Integer, StaticActAutumnDayTask> staticActAutumnDayTaskMap;

    public static void init() {
        List<StaticActAward> list = staticDataDao.selectActAward();

        Map<Integer, StaticActAward> actAwardMap = new HashMap<Integer, StaticActAward>();
        Map<Integer, List<StaticActAward>> awardMap = new HashMap<Integer, List<StaticActAward>>();
        for (StaticActAward e : list) {
            int activityId = e.getActivityId();
            actAwardMap.put(e.getKeyId(), e);
            // 活动
            List<StaticActAward> eeList = awardMap.get(activityId);
            if (eeList == null) {
                eeList = new ArrayList<StaticActAward>();
                awardMap.put(activityId, eeList);
            }
            eeList.add(e);
        }
        StaticActivityDataMgr.awardMap = awardMap;
        StaticActivityDataMgr.actAwardMap = actAwardMap;

        List<StaticActRank> srankList = staticDataDao.selectActRankList();
        Map<Integer, Map<Integer, List<StaticActRank>>> actRankMap = new HashMap<Integer, Map<Integer, List<StaticActRank>>>();
        for (StaticActRank e : srankList) {
            int activityId = e.getActivityId();
            Map<Integer, List<StaticActRank>> rankListMap = actRankMap.get(activityId);
            if (rankListMap == null) {
                rankListMap = new HashMap<Integer, List<StaticActRank>>();
                actRankMap.put(activityId, rankListMap);
            }
            int sortId = e.getSortId();
            List<StaticActRank> sortRankList = rankListMap.get(sortId);
            if (sortRankList == null) {
                sortRankList = new ArrayList<StaticActRank>();
                rankListMap.put(sortId, sortRankList);
            }
            sortRankList.add(e);
        }
        StaticActivityDataMgr.actRankMap = actRankMap;

        // 黑鹰计划活动
        StaticActivityDataMgr.actBlackhawkMap = staticDataDao.selectActBlackhawkMap();

        // 礼包数据
        StaticActivityDataMgr.actGiftpackMap = staticDataDao.selectStaticActGiftpack();
        final int selfServerId = DataResource.ac.getBean(ServerSetting.class).getServerID();
        StaticActivityDataMgr.giftpackPlanList = staticDataDao.selectStaticGiftpackPlan().stream().filter(gp -> {
            if (CheckNull.isEmpty(gp.getServerId())) {
                return true;
            } else {
                return gp.getServerId().stream()
                        .filter(l -> !CheckNull.isEmpty(l) && l.get(0) <= selfServerId && selfServerId <= l.get(1))
                        .findFirst().orElse(null) != null;
            }
        }).collect(Collectors.toList());
        // 触发式礼包数据
        StaticActivityDataMgr.giftPackTriggerPlanList = staticDataDao.selectStaticGiftPackTriggerPlan().stream().filter(gp -> {
            if (CheckNull.isEmpty(gp.getServerId())) {
                return true;
            } else {
                return gp.getServerId().stream()
                        .filter(l -> !CheckNull.isEmpty(l) && l.get(0) <= selfServerId && selfServerId <= l.get(1))
                        .findFirst().orElse(null) != null;
            }
        }).collect(Collectors.toList());
        // 触发式礼包数据
        List<StaticTriggerConf> staticTriggerConfs = staticDataDao.selectStaticTriggerConfList();
        Map<Integer, List<StaticTriggerConf>> triggerConfMap = new HashMap<>();
        Map<Integer, StaticTriggerConf> GiftConfMap = new HashMap<>();
        Map<Integer, StaticTriggerConf> giftConfMapList = new HashMap<>();
        for (StaticTriggerConf e : staticTriggerConfs) {
            int triggerId = e.getTriggerId();
            int giftId = e.getGiftId();
            List<StaticTriggerConf> triggerConfList = triggerConfMap.get(triggerId);
            if (triggerConfList == null) {
                triggerConfList = new ArrayList<>();
            }
            triggerConfList.add(e);
            GiftConfMap.put(giftId, e);
            triggerConfMap.put(triggerId, triggerConfList);
            giftConfMapList.put(e.getId(),e);
        }
        StaticActivityDataMgr.triggerConfMap = triggerConfMap;
        StaticActivityDataMgr.GiftConfMap = GiftConfMap;
        StaticActivityDataMgr.triggerConfMapList = giftConfMapList;

        List<StaticActExchange> staticActExchangelist = staticDataDao.selectStaticActExchange();
        Map<Integer, List<StaticActExchange>> staticActExchangeMap = new HashMap<>();
        Map<Integer, StaticActExchange> staticActExchangeIdMap = new HashMap<>();
        for (StaticActExchange exchange : staticActExchangelist) {
            Integer keyId = exchange.getKeyId();
            int activityId = exchange.getActivityId();
            List<StaticActExchange> staticActivities = staticActExchangeMap.get(activityId);
            if (CheckNull.isEmpty(staticActivities)) {
                staticActivities = new ArrayList<>();
            }
            staticActivities.add(exchange);
            staticActExchangeMap.put(activityId, staticActivities);
            staticActExchangeIdMap.put(keyId, exchange);
        }
        StaticActivityDataMgr.staticActExchangeIdMap = staticActExchangeIdMap;
        StaticActivityDataMgr.staticActExchangeMap = staticActExchangeMap;

        // 打折礼包配置
        StaticActivityDataMgr.staticPromotionMap = staticDataDao.selectPromotionMap();
        Map<Integer, List<StaticPromotion>> staticPromotionTypeMap = new HashMap<>();
        Map<Integer, List<StaticPromotion>> staticPromotionActIdMap = new HashMap<>();
        for (StaticPromotion promotion : staticPromotionMap.values()) {
            int type = promotion.getType();
            int activityId = promotion.getActivityId();
            List<StaticPromotion> PromotionIdList = staticPromotionActIdMap.get(activityId);
            if (CheckNull.isEmpty(PromotionIdList)) {
                PromotionIdList = new ArrayList<>();
            }
            List<StaticPromotion> promotionList = staticPromotionTypeMap.get(type);
            if (CheckNull.isEmpty(promotionList)) {
                promotionList = new ArrayList<>();
            }
            PromotionIdList.add(promotion);
            staticPromotionActIdMap.put(activityId, PromotionIdList);
            promotionList.add(promotion);
            staticPromotionTypeMap.put(type, promotionList);
        }
        StaticActivityDataMgr.staticPromotionActIdMap = staticPromotionActIdMap;
        StaticActivityDataMgr.staticPromotionTypeMap = staticPromotionTypeMap;

        // 幸运转盘配置
        List<StaticTurnplateConf> turnplateConfs = staticDataDao.selectTurnplateConf();
        Map<Integer, List<StaticTurnplateConf>> staticTurnplateConfMap = new HashMap<>();
        Map<Integer, StaticTurnplateConf> staticTurnplateIdMap = new HashMap<>();
        for (StaticTurnplateConf conf : turnplateConfs) {
            int turnplateId = conf.getTurnplateId();
            int actId = conf.getActivityId();
            List<StaticTurnplateConf> turnplateConfList = staticTurnplateConfMap.get(actId);
            if (CheckNull.isEmpty(turnplateConfList)) {
                turnplateConfList = new ArrayList<>();
            }
            turnplateConfList.add(conf);
            staticTurnplateConfMap.put(actId, turnplateConfList);
            staticTurnplateIdMap.put(turnplateId, conf);
        }
        StaticActivityDataMgr.staticTurnplateConf = staticTurnplateConfMap;
        StaticActivityDataMgr.staticTurnplateIdMap = staticTurnplateIdMap;

        List<StaticTurnplateExtra> turnplateExtras = staticDataDao.selectTurnplateExtra();
        StaticActivityDataMgr.staticTurnplateExtraMap = turnplateExtras.stream().collect(Collectors.toMap(StaticTurnplateExtra::getId, st -> st));
        StaticActivityDataMgr.staticTurnplateExtraActIdMap = turnplateExtras.stream().collect(Collectors.groupingBy(StaticTurnplateExtra::getActivityId));

        // 装备转盘配置
        List<StaticEquipTurnplateConf> equipTurnplateConfs = staticDataDao.selectEquipTurnplateConf();
        Map<Integer, List<StaticEquipTurnplateConf>> staticEquipTurnplateConfMap = new HashMap<>();
        Map<Integer, StaticEquipTurnplateConf> staticEquipTurnplateIdMap = new HashMap<>();
        for (StaticEquipTurnplateConf conf : equipTurnplateConfs) {
            int turnplateId = conf.getTurnplateId();
            int actId = conf.getActivityId();
            List<StaticEquipTurnplateConf> turnplateConfList = staticEquipTurnplateConfMap.get(actId);
            if (CheckNull.isEmpty(turnplateConfList)) {
                turnplateConfList = new ArrayList<>();
            }
            turnplateConfList.add(conf);
            staticEquipTurnplateConfMap.put(actId, turnplateConfList);
            staticEquipTurnplateIdMap.put(turnplateId, conf);
        }
        StaticActivityDataMgr.staticEquipTurnplateConf = staticEquipTurnplateConfMap;
        StaticActivityDataMgr.staticEquipTurnplateIdMap = staticEquipTurnplateIdMap;

        // 充值转盘
        StaticActivityDataMgr.actPayTurnplateMap = new HashMap<>();
        List<StaticActPayTurnplate> actPayTurnplateList = staticDataDao.selectActPayTurnplate();
        for (StaticActPayTurnplate sapt : actPayTurnplateList) {
            int activityId = sapt.getActivityId();
            List<StaticActPayTurnplate> saptList = actPayTurnplateMap.get(activityId);
            if (saptList == null) {
                saptList = new ArrayList<>();
                StaticActivityDataMgr.actPayTurnplateMap.put(activityId, saptList);
            }
            saptList.add(sapt);
        }

        // 矿石转盘
        StaticActivityDataMgr.actOreTurnplateMap = new HashMap<>();
        List<StaticActOreTurnplate> actOreTurnplateList = staticDataDao.selectActOreTurnplate();
        for (StaticActOreTurnplate sapt : actOreTurnplateList) {
            int activityId = sapt.getActivityId();
            List<StaticActOreTurnplate> saptList = actOreTurnplateMap.get(activityId);
            if (saptList == null) {
                saptList = new ArrayList<>();
                StaticActivityDataMgr.actOreTurnplateMap.put(activityId, saptList);
            }
            saptList.add(sapt);
        }
        // 每日特惠活动
        StaticActivityDataMgr.actDaydiscountsMap = staticDataDao.selectActDaydiscountsMap();
        Map<Integer, List<StaticActDaydiscounts>> actDaydiscountsListMap = new HashMap<>();
        for (StaticActDaydiscounts sad : StaticActivityDataMgr.actDaydiscountsMap.values()) {
            int activityId = sad.getActivityId();
            List<StaticActDaydiscounts> sadList = actDaydiscountsListMap.get(activityId);
            if (sadList == null) {
                sadList = new ArrayList<>();
                actDaydiscountsListMap.put(activityId, sadList);
            }
            sadList.add(sad);
        }
        StaticActivityDataMgr.actDaydiscountsListMap = actDaydiscountsListMap;

        // 巴顿活动
        List<StaticActBarton> staticActBarton = staticDataDao.selectStaticActBartonList();
        if(staticActBarton != null && !staticActBarton.isEmpty()){
            actBartonList = staticActBarton.stream().collect(Collectors.groupingBy(StaticActBarton::getActivityId));
        }

        List<StaticActBandit> staticActBandits = staticDataDao.selectStaticActBanditList();
        StaticActivityDataMgr.actBanditList = staticActBandits;
        Map<Integer, List<StaticActBandit>> actBanditMap = new HashMap<>();
        for (StaticActBandit sActBandit : staticActBandits) {
            int type = sActBandit.getType();
            List<StaticActBandit> actBandits = actBanditMap.get(type);
            if (actBandits == null) {
                actBandits = new ArrayList<>();
                actBanditMap.put(type, actBandits);
            }
            actBandits.add(sActBandit);
        }
        StaticActivityDataMgr.actBanditMap = actBanditMap;

        List<StaticActLogin> staticActLogins = staticDataDao.selectStaticActLoginList();
        if (!CheckNull.isEmpty(staticActLogins)) {
            StaticActivityDataMgr.actLoginDayiyMap = staticActLogins.stream().collect(Collectors
                    .groupingBy(StaticActLogin::getActivityId, HashMap::new,
                            Collectors.toMap(StaticActLogin::getTime, sal -> sal)));
        }

        // 复活节活动
        List<StaticEasterAward> staticEasterAwards = staticDataDao.selectStaticEasterAwardList();
        StaticActivityDataMgr.easterAwardMap = staticEasterAwards.stream().collect(Collectors.groupingBy(StaticEasterAward::getActivityId));
        StaticActivityDataMgr.easterCondAwardMap = staticEasterAwards.stream().collect(Collectors.groupingBy(StaticEasterAward::getActivityId, HashMap::new,
                Collectors.groupingBy(sea -> sea.getParam().get(0))));
        StaticActivityDataMgr.easterAwardKeyMap = staticEasterAwards.stream().collect(Collectors.toMap(StaticEasterAward::getKeyId, sea -> sea));

        //喜悦金秋任务表
        Map<Integer, StaticActAutumnDayTask> staticActAutumnDayTaskMap = staticDataDao.selectActAutumnDayTaskMap();
        setStaticActAutumnDayTaskMap(null == staticActAutumnDayTaskMap ? new HashMap<>() : staticActAutumnDayTaskMap);

        s_christmas_award();

        activity();
        initActivityEffectMap();
        initActDayTask();
        initActivityTimeMap();
        initActRank();
        actDailyPayCacheMap.clear();
        initActMonopoly();
        initRoyalArena();
        // 时间触发礼包
        ActivityTriggerService activityTriggerService = DataResource.ac.getBean(ActivityTriggerService.class);
        activityTriggerService.checkTimeTriggerActivity();
    }

    private static List<StaticChristmasAward> staticChristmasAwardList;
    private static void s_christmas_award(){
        staticChristmasAwardList = staticDataDao.selectChristmasAwardList();
    }

    public static StaticChristmasAward getStaticChristmasAwardByLv(int lv,int activityId){
        return staticChristmasAwardList.stream().filter(o -> lv >= o.getLv().get(0) && lv <= o.getLv().get(1) && o.getActivityId() == activityId).findFirst().orElse(null);
    }

    private static void initRoyalArena() {
        List<StaticRoyalArenaTask> royalArenaTaskList = staticDataDao.selectRoyalArenaTaskMap();
        StaticActivityDataMgr.royalArenaTaskMap = royalArenaTaskList.stream().collect(Collectors.groupingBy(StaticRoyalArenaTask::getActivityId));

        List<StaticRoyalArenaAward> royalArenaAwardList = staticDataDao.selectRoyalArenaAwardMap();
        StaticActivityDataMgr.royalArenaAwardMap = royalArenaAwardList.stream().collect(Collectors.groupingBy(StaticRoyalArenaAward::getActivityId));
    }

    /** 初始化大富翁活动 */
    private static void initActMonopoly() {
        StaticActivityDataMgr.actMonopolyMap = staticDataDao.selectActMonopolyMap();
        // 分组排序
        StaticActivityDataMgr.actMonopolyByActId = actMonopolyMap.values().stream()
                .collect(Collectors.groupingBy(StaticActMonopoly::getActivityId));

        // 档位配置
        List<StaticActMonopolyCount> sacList = staticDataDao.selectStaticActMonopolyCountList();
        // 档位配置根据activityId进行分组
        StaticActivityDataMgr.actMonopolyCountMap = sacList.stream()
                .collect(Collectors.groupingBy(StaticActMonopolyCount::getActivityId));
    }

    public static StaticActVoucher getActVoucherByActId(int actId) {
        return actVoucherMap.get(actId).get(0);
    }

    public static List<StaticActVoucher> getActVoucherListByActId(int actId){
        return actVoucherMap.get(actId);
    }

//    public static StaticActVoucher getActVoucherByType(int type){
//        return actVoucherMap.values().stream().filter(o -> o.getType() == type).findFirst().orElse(null);
//    }

    public static List<StaticEasterAward> getEasterAwardList(int actId) {
        return easterAwardMap.get(actId);
    }

    public static Map<Integer, List<StaticEasterAward>> getEasterAwardCondMap(int actId) {
        return easterCondAwardMap.get(actId);
    }

    public static List<StaticEasterAward> getEasterAwardList(int actId, int cond) {
        Map<Integer, List<StaticEasterAward>> easterAward = easterCondAwardMap.get(actId);
        if (!CheckNull.isEmpty(easterAward)) {
            return easterAward.get(cond);
        }
        return null;
    }

    public static StaticEasterAward getEasterAwardListByKey(int keyId) {
        return easterAwardKeyMap.get(keyId);
    }

    public static List<StaticRoyalArenaTask> getRoyalArenaTaskList(int actId) {
        return royalArenaTaskMap.get(actId);
    }

    public static StaticRoyalArenaTask getRoyalArenaTaskByActIdAndTaskId(int actId, int taskId) {
        List<StaticRoyalArenaTask> arenaTasks = royalArenaTaskMap.get(actId);
        if (!CheckNull.isEmpty(arenaTasks)) {
            return arenaTasks.stream().filter(sTask -> sTask.getId() == taskId).findFirst().orElse(null);
        }
        return null;
    }

    public static StaticRoyalArenaAward getRoyalArenaAwardByActIdAndAwardId(int actId, int awardId) {
        List<StaticRoyalArenaAward> arenaAwards = royalArenaAwardMap.get(actId);
        if (!CheckNull.isEmpty(arenaAwards)) {
            return arenaAwards.stream().filter(sAward -> sAward.getId() == awardId).findFirst().orElse(null);
        }
        return null;
    }

    public static List<StaticRoyalArenaAward> getRoyalArenaAwardByActId(int actId) {
        return royalArenaAwardMap.get(actId);
    }


    public static List<StaticActBandit> getActBanditList(int type) {
        return actBanditMap.get(type);
    }

    public static List<StaticActBandit> getActBanditList() {
        return actBanditList;
    }

    public static StaticActBandit getActBanditList(int actId,int type) {
        return actBanditList.stream().filter(o -> o.getActivityId()==actId&&o.getType()==type).findFirst().orElse(null);
    }

    public static StaticActLogin getActLogin(int activityId, int dayiy) {
        Map<Integer, StaticActLogin> actLoginMap = actLoginDayiyMap.get(activityId);
        if (!CheckNull.isEmpty(actLoginMap)) {
            return actLoginMap.get(dayiy);
        }
        return null;
    }

    public static StaticActLogin getMaxActLogin(int activityId){
        return actLoginDayiyMap.get(activityId).values().stream().max(Comparator.comparing(StaticActLogin::getTime)).orElse(null);
    }

    /**
     * 在活动时间内的特殊活动
     *
     * @return
     */
    public static List<StaticSpecialPlan> getSpecialPlans() {
        Date now = new Date();
        return specialPlans.stream().filter(plan -> DateHelper.isInTime(now, plan.getBeginTime(), plan.getEndTime()))
                .collect(Collectors.toList());
    }

    public static List<StaticTurnplateConf> getActTurnPlateListByActId(int actId) {
        return staticTurnplateConf.get(actId);
    }

    public static StaticTurnplateConf getActTurnPlateById(int id) {
        return staticTurnplateIdMap.get(id);
    }

    public static StaticTurnplateExtra getActTurnPlateExtraById(int id) {
        return staticTurnplateExtraMap.get(id);
    }

    public static List<StaticTurnplateExtra> getActTurnplateExtraByActId(int actId) {
        return staticTurnplateExtraActIdMap.get(actId);
    }

    public static List<StaticEquipTurnplateConf> getEquipTurnPlateListByActId(int actId) {
        return staticEquipTurnplateConf.get(actId);
    }

    public static StaticEquipTurnplateConf getEquipTurnPlateById(int id) {
        return staticEquipTurnplateIdMap.get(id);
    }

    public static List<StaticActExchange> getActExchangeListById(int type) {
        return staticActExchangeMap.get(type);
    }

    public static StaticActExchange getActExchangeListByKeyId(int keyId) {
        return staticActExchangeIdMap.get(keyId);
    }

    public static StaticActGiftpack getActGiftpackMapById(int giftpackId) {
        return actGiftpackMap.get(giftpackId);
    }

    public static StaticGiftpackPlan getGiftpackPlanMapById(int giftpackId) {
        // for (StaticGiftpackPlan plan : giftpackPlanList) {
        // if (plan.getGiftpackId() == giftpackId) {
        // return plan;
        // }
        // }
        // return null;
        return giftpackPlanList.stream().filter(p -> p.getGiftpackId() == giftpackId).findFirst().orElse(null);
    }

    @Deprecated
    public static List<StaticGiftpackPlan> getGiftpackPlanMapByDay(int createLordDay, int openServerDay, Date nowDate) {
        return giftpackPlanList.stream().filter(p -> p.isThisInTime(openServerDay, createLordDay, nowDate))
                .collect(Collectors.toList());
    }

    public static List<StaticGiftpackPlan> getGiftPackPlanByDate(Date createLord, Date openServer, Date nowDate) {
        return giftpackPlanList.stream()
                .filter(sgp -> sgp.isThisInTime(openServer, createLord, nowDate))
                .collect(Collectors.toList());
    }

    public static List<StaticGiftPackTriggerPlan> getGiftPackTriggerPlan(int openServerDay,int createLordDay,Date nowDate){
        if(giftPackTriggerPlanList == null){
            return null;
        }
        return giftPackTriggerPlanList.stream().filter(g->g.isThisInTime(openServerDay,createLordDay,nowDate))
                .collect(Collectors.toList());
    }

    public static StaticGiftPackTriggerPlan getGiftPackTriggerPlan(int keyId){
        if(giftPackTriggerPlanList == null){
            return null;
        }
        return giftPackTriggerPlanList.stream().filter(g ->g.getTriggerId() == keyId)
                .findFirst().orElse(null);
    }

    public static List<StaticTriggerConf> getTriggerGiftConfById(int triggerId, Player player, final boolean checkOpen) {
        if (ObjectUtils.isEmpty(triggerConfMap.get(triggerId)))
            return null;
        return triggerConfMap.get(triggerId)
                .stream()
                .filter(t -> {
                    if (checkOpen) {
                        return t.checkTriggerOpenCnf(player);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public static List<StaticTriggerConf> getTriggerGiftConfById(int triggerId, Player player) {
        return getTriggerGiftConfById(triggerId, player, true);
    }

    public static StaticTriggerConf getTriggerConf(int id) {
        return triggerConfMapList.get(id);
    }

    public static StaticTriggerConf getTriggerGiftConfByGiftId(int giftId) {
        return GiftConfMap.get(giftId);
    }

    public static Collection<StaticTriggerConf> getTriggerConfList() {
        return GiftConfMap.values();
    }


    public static List<StaticActAward> getActAwardById(int activityId) {
        return awardMap.get(activityId);
    }

    public static StaticActAward getActAward(int keyId) {
        return actAwardMap.get(keyId);
    }

    public static StaticPromotion getPromotionById(int promotionId) {
        return staticPromotionMap.get(promotionId);
    }

    public static Map<Integer, StaticPromotion> getStaticPromotionMap() {
        return staticPromotionMap;
    }

    public static List<StaticPromotion> getStaticPromotionListByType(int type) {
        return staticPromotionTypeMap.get(type);
    }

    public static List<StaticPromotion> getStaticPromotionListByActId(int actId) {
        return staticPromotionActIdMap.get(actId);
    }

    public static Set<Integer> getSorts(int activityId) {
        Set<Integer> sets = new HashSet<>();
        List<StaticActAward> list = awardMap.get(activityId);
        for (StaticActAward ee : list) {
            if (!sets.contains(ee.getSortId())) {
                sets.add(ee.getSortId());
            }
        }
        return sets;
    }

    public static StaticActRank getActRank(int activityId, int sortId, int rank) {
        if (!actRankMap.containsKey(activityId)) {
            return null;
        }
        List<StaticActRank> list = actRankMap.get(activityId).get(sortId);
        if (list == null) {
            return null;
        }
        for (StaticActRank e : list) {
            if (rank <= e.getRankEnd() && rank >= e.getRankBegin()) {
                return e;
            }
        }
        return null;
    }

    public static Map<Integer, StaticActivityEffect> getActivityEffectById(int activityId) {
        return activityEffectMap.get(activityId);
    }

    public static List<StaticActRobinHood> getActRobinHoodByActId(int activityId) {
        return actRobinHoodMap.get(activityId);
    }

    public static List<StaticActRobinHood> getActRobinHoodByActIdAndTaskCond(int activityId, int cond) {
        List<StaticActRobinHood> robinHoodList = actRobinHoodMap.get(activityId);
        if (CheckNull.isEmpty(robinHoodList)) {
            return null;
        }
        return robinHoodList.stream().filter(sarh -> sarh.getCond() == cond).collect(Collectors.toList());
    }

    private static void initActivityEffectMap() {
        Map<Integer, StaticActivityEffect> map;
        List<StaticActivityEffect> totalLst = staticDataDao.selectActivityEffectList();
        Map<Integer, Map<Integer, StaticActivityEffect>> activityEffectMap = new HashMap<Integer, Map<Integer, StaticActivityEffect>>();
        for (StaticActivityEffect effect : totalLst) {
            map = activityEffectMap.get(effect.getActivityId());
            if (null == map) {
                map = new HashMap<Integer, StaticActivityEffect>();
                activityEffectMap.put(effect.getActivityId(), map);
            }
            map.put(effect.getDay(), effect);
        }
        StaticActivityDataMgr.activityEffectMap = activityEffectMap;
    }

    private static void activity() {
        int activityMoldId = serverSetting.getActMoldId();
        Map<Integer, StaticActivity> activityMap = staticDataDao.selectStaticActivity();
        StaticActivityDataMgr.staticActivityMap = activityMap;
        List<StaticActivityPlan> planList = staticDataDao.selectStaticActivityPlan();
        planList.addAll(staticIniDao.selectStaticActivityPlanCross());

        // 开服时间
        Date openTime = DateHelper.parseDate(serverSetting.getOpenTime());
        List<ActivityBase> activityList = new ArrayList<>();
        for (StaticActivityPlan e : planList) {
            int activityType = e.getActivityType();
            StaticActivity staticActivity = activityMap.get(activityType);
            if (staticActivity == null) {
                continue;
            }
            int moldId = e.getMoldId();
            if (activityMoldId != moldId) {
                continue;
            }
            ActivityBase activityBase = new ActivityBase();
            activityBase.setOpenTime(openTime);
            activityBase.setPlan(e);
            activityBase.setStaticActivity(staticActivity);
            boolean flag = activityBase.initData();// 计算活动的各种时间
            if (flag && activityBase.isSelfSeverPlan(serverSetting.getServerID())) {
                activityList.add(activityBase);
            }
        }
        StaticActivityDataMgr.activityList = activityList;

        StaticActivityDataMgr.specialPlans = staticDataDao.selectSpecialPlan().values().stream()
                .filter(plan -> plan.isSelfSeverPlan(serverSetting.getServerID())).collect(Collectors.toList());

        List<StaticActRobinHood> robinHoodList = staticDataDao.selectActRobinHood();
        StaticActivityDataMgr.actRobinHoodMap = robinHoodList.stream().collect(Collectors.groupingBy(StaticActRobinHood::getActvityId));

        StaticActivityDataMgr.actHotProductKeyMap = staticDataDao.selectActHotProductMap();
        StaticActivityDataMgr.actHotProductMap = StaticActivityDataMgr.actHotProductKeyMap.values().stream().collect(Collectors.groupingBy(StaticActHotProduct::getActivityId, HashMap::new, Collectors.groupingBy(StaticActHotProduct::getTab)));

        actVoucherMap = staticDataDao.selectActVoucherMap().values().stream().collect(Collectors.groupingBy(StaticActVoucher::getActivityId));
        //秋季拍卖配置
        StaticActivityDataMgr.staticActAuctionMap = staticDataDao.selectActAuctionMap();
        if (!ObjectUtils.isEmpty(staticActAuctionMap)) {
            StaticActivityDataMgr.staticActIdAuctionMap = new HashMap<>();
            staticActAuctionMap.forEach((itemId, actAuction) -> {
                if (!staticActIdAuctionMap.containsKey(actAuction.getActivityId())) {
                    staticActIdAuctionMap.put(actAuction.getActivityId(), new ArrayList<>());
                }
                staticActIdAuctionMap.get(actAuction.getActivityId()).add(actAuction);
            });
        }
    }

    public static List<StaticActAuction> getAuctionItemByActId(int activityId) {
        if (ObjectUtils.isEmpty(staticActIdAuctionMap))
            return null;
        return staticActIdAuctionMap.get(activityId);
    }

    public static Set<Integer> getAuctionItemByType(int activityId, int type, int round) {
        if (ObjectUtils.isEmpty(staticActIdAuctionMap) || ObjectUtils.isEmpty(staticActIdAuctionMap.get(activityId))) {
            return null;
        }

        return staticActIdAuctionMap.get(activityId).stream().filter(staticActAuction -> staticActAuction.getRound() == round
                && staticActAuction.getType() == type).map(StaticActAuction::getId).collect(Collectors.toSet());
    }

    public static StaticActAuction getAuctionItemById(int itemId) {
        return staticActAuctionMap.get(itemId);
    }

    public static List<StaticActAuction> getAuctionItemByRound(int activityId, int round) {
        if (ObjectUtils.isEmpty(staticActIdAuctionMap) || ObjectUtils.isEmpty(staticActIdAuctionMap.get(activityId))) {
            return null;
        }

        return  staticActIdAuctionMap.get(activityId).stream().filter(staticActAuction -> staticActAuction.getRound() == round).collect(Collectors.toList());
    }

    public static Map<Integer, List<StaticActAuction>> getAuctionItemByLessThanRound(int activityId, int round, boolean contains) {
        if (ObjectUtils.isEmpty(staticActIdAuctionMap) || ObjectUtils.isEmpty(staticActIdAuctionMap.get(activityId))) {
            return null;
        }

        List<StaticActAuction> list = null;
        if (!contains) {
            list = staticActIdAuctionMap.get(activityId).stream().filter(staticActAuction -> staticActAuction.getRound() < round).collect(Collectors.toList());
        } else {
            list = staticActIdAuctionMap.get(activityId).stream().filter(staticActAuction -> staticActAuction.getRound() <= round).collect(Collectors.toList());
        }

        if (ObjectUtils.isEmpty(list)) {
            return null;
        }

        Map<Integer, List<StaticActAuction>> resultMap = new HashMap<>();
        list.forEach(staticActAuction -> {
            if (!resultMap.containsKey(staticActAuction.getRound())) {
                resultMap.put(staticActAuction.getRound(), new ArrayList<>());
            }

            resultMap.get(staticActAuction.getRound()).add(staticActAuction);
        });

        return resultMap;
    }

    public static StaticActAuction getMinRoundAuction() {
        if (ObjectUtils.isEmpty(staticActAuctionMap)) {
            return null;
        }

        ActivityBase activityBase = getActivityByType(ActivityConst.ACT_AUCTION);
        if (ObjectUtils.isEmpty(activityBase)) {
            return null;
        }

        return Optional.ofNullable(staticActAuctionMap.values().stream().
                filter(staticActAuction -> staticActAuction.getActivityId() == activityBase.getActivityId()).
                min(Comparator.comparing(StaticActAuction::getRound))).get().orElse(null);
    }

    /**
     * 合服使用的方法,根据区服获取活动信息
     *
     * @param serverId
     * @param openTime
     * @param activityMoldId
     * @return
     */
    public static List<ActivityBase> getActivityBaseByServerIdAndMoldId(int serverId, Date openTime,
            int activityMoldId) {
        List<StaticActivityPlan> planList = staticDataDao.selectStaticActivityPlan();
        List<ActivityBase> activityList = new ArrayList<>();
        for (StaticActivityPlan e : planList) {
            int activityType = e.getActivityType();

            StaticActivity staticActivity = StaticActivityDataMgr.staticActivityMap.get(activityType);
            if (staticActivity == null) {
                continue;
            }
            int moldId = e.getMoldId();
            if (activityMoldId != moldId) {
                continue;
            }
            ActivityBase activityBase = new ActivityBase();
            activityBase.setOpenTime(openTime);
            activityBase.setPlan(e);
            activityBase.setStaticActivity(staticActivity);
            boolean flag = activityBase.initData();// 计算活动的各种时间
            if (flag && activityBase.isSelfSeverPlan(serverId)) {
                activityList.add(activityBase);
            }
        }
        return activityList.stream()
                .filter(actBase -> (actBase.getBaseOpen() != ActivityConst.OPEN_CLOSE) || actBase.isBaseDisplay())
                .collect(Collectors.toList());
    }

    public static List<ActivityBase> getActivityList() {
        return activityList;
    }

    public static ActivityBase getActivityByType(int activityType) {
        ActivityBase rab = null;
        for (ActivityBase e : activityList) {
            StaticActivity a = e.getStaticActivity();
            StaticActivityPlan plan = e.getPlan();
            if (a == null || plan == null) {
                continue;
            }
            if (a.getType() == activityType && e.getStep0() != ActivityConst.OPEN_CLOSE) {
                if (rab != null && rab.getPlan().getKeyId() > plan.getKeyId()) continue; // 如果是同样的 ,返回keyId大的
                rab = e;
            }
        }
        return rab;
    }

//    /**
//     * 不处理活动状态，用于在endtime displaytime处理活动逻辑是需要拿到ActivityBase
//     *  定时器可能会延时执行，避免加上活动状态拿不到对象
//     * @param activityType
//     * @return
//     */
//    public static ActivityBase getActivityByType0(int activityType){
//        ActivityBase ab = null;
//        for (ActivityBase e : activityList) {
//            StaticActivity a = e.getStaticActivity();
//            StaticActivityPlan plan = e.getPlan();
//            if (a == null || plan == null) {
//                continue;
//            }
//            if(a.getType() == activityType){
//                if(ab == null || plan.getKeyId() > ab.getPlan().getKeyId()){
//                    ab = e;
//                }
//            }
//        }
//        return ab;
//    }

    public static ActivityBase getActivityByTypeIgnoreStep(int activityType) {
        for (ActivityBase e : activityList) {
            StaticActivity a = e.getStaticActivity();
            StaticActivityPlan plan = e.getPlan();
            if (a == null || plan == null) {
                continue;
            }
            if (a.getType() == activityType) {
                return e;
            }
        }
        return null;
    }

    private static void initActDayTask() {
        List<StaticDay7Act> list = staticDataDao.selectStaticDay7ActList();
        List<StaticAtkCityAct> atkCityActList = staticDataDao.selectStaticAtkCityAct();

        Map<Integer, List<StaticDay7Act>> day7ActList = new TreeMap<Integer, List<StaticDay7Act>>();
        Map<Integer, StaticDay7Act> day7ActMap = new TreeMap<Integer, StaticDay7Act>();
        Map<Integer, List<StaticDay7Act>> day7ActTypeList = new TreeMap<Integer, List<StaticDay7Act>>();
        Map<Integer, StaticAtkCityAct> atkCityActMap = new TreeMap<Integer, StaticAtkCityAct>();
        Map<Integer, List<StaticAtkCityAct>> atkCityActTypeMap = new TreeMap<Integer, List<StaticAtkCityAct>>();

        // 攻城掠地
        for (StaticAtkCityAct e : atkCityActList) {
            atkCityActMap.put(e.getKeyId(), e);
            List<StaticAtkCityAct> typeList = atkCityActTypeMap.get(e.getTaskType());
            if (typeList == null) {
                typeList = new ArrayList<>();
                atkCityActTypeMap.put(e.getTaskType(), typeList);
            }
            typeList.add(e);
        }

        list.forEach(o -> act7DayMap.put(o.getKeyId(),o));
        // 过滤配置满足ServerId
        list = list.stream().filter(sd7c -> sd7c.checkServerPlan(serverSetting.getServerID())).collect(Collectors.toList());
        // 七日活动
        for (StaticDay7Act e : list) {
            List<StaticDay7Act> dayList = day7ActList.computeIfAbsent(e.getDay(), k -> new ArrayList<>());
            dayList.add(e);
            day7ActMap.put(e.getKeyId(), e);
            List<StaticDay7Act> typeList = day7ActTypeList.computeIfAbsent(e.getTaskType(), k -> new ArrayList<>());
            typeList.add(e);
        }

//        StaticActivityDataMgr.day7ActList = day7ActList;
//        StaticActivityDataMgr.day7ActMap = day7ActMap;
//        StaticActivityDataMgr.day7ActTypeList = day7ActTypeList;

        StaticActivityDataMgr.atkCityActMap = atkCityActMap;
        StaticActivityDataMgr.atkCityActList = atkCityActList;
        StaticActivityDataMgr.atkCityActTypeMap = atkCityActTypeMap;
    }

    private static void initActivityTimeMap() {
        List<StaticActivityTime> list;
        List<StaticActivityTime> totalLst = staticDataDao.selectActivityTimeList();
        Map<Integer, List<StaticActivityTime>> activityTimeMap = new HashMap<>();
        for (StaticActivityTime time : totalLst) {
            list = activityTimeMap.get(time.getActivityId());
            if (null == list) {
                list = new ArrayList<>();
                activityTimeMap.put(time.getActivityId(), list);
            }
            list.add(time);
        }
        StaticActivityDataMgr.activityTimeMap = activityTimeMap;
    }

//    public static List<StaticDay7Act> getDay7ActList(int day) {
//        return day7ActList.get(day);
//    }

//    public static StaticDay7Act getDay7Act(int keyId) {
//        return day7ActMap.get(keyId);
//    }

//    public static Map<Integer, StaticDay7Act> getDay7ActMap() {
//        return day7ActMap;
//    }

//    public static List<StaticDay7Act> getDay7ActTypeList(int type) {
//        return day7ActTypeList.get(type);
//    }

    public static Map<Integer, StaticActBlackhawk> getActBlackhawkMap() {
        return actBlackhawkMap;
    }

    public static Map<Integer, StaticActivityOpen> getActivityOpenMap() {
        return activityOpenMap;
    }

    public static List<StaticActivityTime> getActivityTimeById(int activityId) {
        return activityTimeMap.get(activityId);
    }

    public static Map<Integer, StaticActivity> getStaticActivityMap() {
        return staticActivityMap;
    }

    public static List<StaticAtkCityAct> getAtkCityActListByDay(int openServerTime) {
        return atkCityActList.stream().filter(e -> e.getDay() <= openServerTime).collect(Collectors.toList());
    }

    public static List<StaticAtkCityAct> getAtkCityActListByType(int type) {
        return atkCityActTypeMap.get(type);
    }

    public static List<StaticAtkCityAct> getAtkCityActList() {
        return atkCityActList;
    }

    public static StaticAtkCityAct getAtkCityAct(int keyId) {
        return atkCityActMap.get(keyId);
    }

    /**
     * 获取排行榜容量
     *
     * @param activityId
     * @return 返回排行榜容量,没有则为0
     */
    public static int getRankCapacityByActivityId(int activityId) {
        return Optional.ofNullable(rankCapacityMap.get(activityId)).orElse(0);
    }

    /**
     * 获取某个排行榜容量上限值
     *
     * @param activityId
     * @return
     */
    private static int getMaxRank(int activityId) {
        List<StaticActAward> list = awardMap.get(activityId);
        if (CheckNull.isEmpty(list)) {
            return 0;
        }
        StaticActAward maxSaa = list.stream()
                .max((saa0, saa1) -> saa0.getCond() < saa1.getCond() ? -1 : (saa0.getCond() == saa1.getCond() ? 0 : 1))
                .get();
        return maxSaa.getCond();
    }

    /**
     * 初始化排行活动相关数据
     */
    private static void initActRank() {
        rankCapacityMap = new HashMap<>();
        rankActMap = new HashMap<>();
        for (Entry<Integer, List<StaticActAward>> a : awardMap.entrySet()) {
            List<StaticActAward> value = a.getValue();
            if (CheckNull.isEmpty(value)) {
                continue;
            }
            StaticActAward staticActAward = value.get(0);
            if (!isActTypeRank(staticActAward.getType())
                    && staticActAward.getType() != ActivityConst.ACT_NEWYEAR_2022_FISH) {
                continue;
            }
            Integer actId = a.getKey();
            rankCapacityMap.put(actId, getMaxRank(actId));
            // 排序保存
            List<StaticActAward> sortList = value.stream()
                    .sorted((saa0, saa1) -> saa0.getParam().get(0) < saa1.getParam().get(0) ? -1
                            : (saa0.getParam().get(0) == saa1.getParam().get(0) ? 0 : 1))
                    .collect(Collectors.toList());
            rankActMap.put(actId, sortList);
        }

    }

    /**
     * 查找当前排行在什么档位
     *
     * @param actId
     * @param rank 名次
     * @return
     */
    public static StaticActAward findRankAward(int actId, int rank) {
        List<StaticActAward> list = getRankActAwardByActId(actId);
        if (CheckNull.isEmpty(list)) {
            return null;
        }
        for (StaticActAward saa : list) {
            if (rank <= saa.getCond()) {
                return saa;
            }
        }
        return null;
    }

    /**
     * 是否为只能领取对应档次的排行奖励的排行类型
     *
     * @param type
     * @return
     */
    public static boolean isOnlyRankAward(int type) {
        return type == ActivityConst.ACT_CAMP_RANK || type == ActivityConst.ACT_CAMP_FIGHT_RANK;
    }

    /**
     * 获取排行榜奖励配置数据,带排序的
     *
     * @param actId
     * @return
     */
    public static List<StaticActAward> getRankActAwardByActId(int actId) {
        return rankActMap.get(actId);
    }

    /**
     * 判断是否是一个全局排行榜活动
     *
     * @param activityType
     * @return true 排行榜活动
     */
    public static boolean isActTypeRank(int activityType) {
        return ActivityConst.ACT_CAMP_BATTLE_RANK == activityType || ActivityConst.ACT_CITY_BATTLE_RANK == activityType
                || ActivityConst.ACT_SUPPLY_RANK == activityType || ActivityConst.ACT_ORE_RANK == activityType
                || ActivityConst.ACT_REMOULD_RANK == activityType || ActivityConst.ACT_FORGE_RANK == activityType
                || ActivityConst.ACT_PARTY_BUILD_RANK == activityType || ActivityConst.ACT_ARMY_RANK == activityType
                || ActivityConst.ACT_PAY_RANK == activityType || ActivityConst.ACT_GESTAPO_RANK == activityType
                || ActivityConst.ACT_LUCKY_TURNPLATE == activityType
                || ActivityConst.FAMOUS_GENERAL_TURNPLATE == activityType
                || ActivityConst.ACT_EQUIP_TURNPLATE == activityType
                || ActivityConst.ACT_PRESENT_GIFT_RANK == activityType || ActivityConst.ACT_CAMP_RANK == activityType
                || ActivityConst.ACT_PAY_RANK_NEW == activityType || ActivityConst.ACT_CAMP_FIGHT_RANK == activityType
                || ActivityConst.ACT_PAY_RANK_V_3 == activityType || ActivityConst.ACT_MERGE_PAY_RANK == activityType
                || ActivityConst.ACT_CONSUME_GOLD_RANK == activityType || ActivityConst.ACT_TUTOR_RANK == activityType
                || ActivityConst.ACT_ROYAL_ARENA == activityType || ActivityConst.ACT_CHRISTMAS == activityType
                || ActivityConst.ACT_DIAOCHAN    == activityType || ActivityConst.ACT_REPAIR_CASTLE == activityType
                || ActivityConst.ACT_SEASON_HERO == activityType;
    }

    /**
     * 获取空降补给奖励按照 param
     *
     * @param actId
     * @param param
     * @return
     */
    public static List<StaticActAward> getSupplyDorpByParam(int actId, int param) {
        List<StaticActAward> awardList = getActAwardById(actId);
        if (CheckNull.isEmpty(awardList)) {
            return new ArrayList<>();
        }
        return awardList.stream().filter(ssa -> ssa.getParam().get(0) == param).collect(Collectors.toList());
    }

    /**
     * 获取空降补给最大天数按照 param
     *
     * @param actId
     * @param param
     * @return
     */
    public static StaticActAward getSupplyMaxByParam(int actId, int param) {
        List<StaticActAward> awardList = getSupplyDorpByParam(actId,param);
        if (CheckNull.isEmpty(awardList)) {
            return null;
        }
        return awardList.stream().max(Comparator.comparingInt(StaticActAward::getCond)).orElse(null);
    }

    /**
     * 每日充值奖励list获取
     *
     * @param actId
     * @return
     */
    public static List<StaticActAward> getDailyPayAward(int actId) {
        if (actDailyPayCacheMap.containsKey(actId)) {
            return actDailyPayCacheMap.get(actId);
        } else {
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(actId);
            List<StaticActAward> collect = condList.stream()
                    .filter(e -> !CheckNull.isEmpty(e.getParam()) && e.getParam().get(0) >= 1)
                    .sorted((s1, s2) -> Integer.compare(s1.getParam().get(0), s2.getParam().get(0)))
                    .collect(Collectors.toList());
            actDailyPayCacheMap.put(actId, collect);
            return collect;
        }

    }

    /**
     * 根据actId获取转盘数据
     *
     * @param actId
     * @return
     */
    public static List<StaticActPayTurnplate> getActPayTurnplateListByActId(int actId) {
        return actPayTurnplateMap.get(actId);
    }

    /**
     *
     * @Title: getActOreTurnplateListByActId @Description: 根据actId获取矿石转盘数据 @param actId @return 参数
     *         List<StaticActOreTurnplate> 返回类型 @throws
     */
    public static List<StaticActOreTurnplate> getActOreTurnplateListByActId(int actId) {
        return actOreTurnplateMap.get(actId);
    }

    public static Map<Integer, StaticActDaydiscounts> getActDaydiscountsMap() {
        return actDaydiscountsMap;
    }

    public static List<StaticActDaydiscounts> getActDaydiscountsMapByActId(int actId) {
        return actDaydiscountsListMap.get(actId);
    }

    public static StaticActMonopoly getActMonopolyById(int keyId) {
        return actMonopolyMap.get(keyId);
    }

    public static List<StaticActMonopoly> getActMonopolyListByActId(int actId) {
        return actMonopolyByActId.get(actId);
    }

    public static List<StaticActMonopolyCount> getActMonopolyCountByActId(int actId) {
        return actMonopolyCountMap.get(actId);
    }

    public static Map<Integer, StaticActAutumnDayTask> getStaticActAutumnDayTaskMap() {
        return staticActAutumnDayTaskMap;
    }

    public static void setStaticActAutumnDayTaskMap(Map<Integer, StaticActAutumnDayTask> staticActAutumnDayTaskMap) {
        StaticActivityDataMgr.staticActAutumnDayTaskMap = staticActAutumnDayTaskMap;
    }

    /**
     * 根据活动id获取配置
     * @param activityId
     * @return
     */
    public static List<StaticActBarton> getActBartonList(int activityId){
        return actBartonList.get(activityId);
    }

    public static Map<Integer, List<StaticActHotProduct>> getActHotProductByActId(int activityId) {
        return actHotProductMap.get(activityId);
    }

    public static StaticActHotProduct getActHotProductByKey(int keyId) {
        return actHotProductKeyMap.get(keyId);
    }


}
