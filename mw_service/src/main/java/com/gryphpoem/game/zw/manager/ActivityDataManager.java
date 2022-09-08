package com.gryphpoem.game.zw.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.NumUtils;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.SyncActChangeRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncLuckyPoolChangeRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.dao.impl.p.ActivityDao;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.pojo.tavern.DrawCardData;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.FriendService;
import com.gryphpoem.game.zw.service.activity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class ActivityDataManager {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private ActivityDao activityDao;
    @Autowired
    private StaticDataDao staticDataDao;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private FriendService friendService;
    @Autowired
    private ActivityHotProductService activityHotProductService;
    @Autowired
    private ActivityLotteryService activityLotteryService;
    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;
    @Autowired
    private ActivityChargeContinueService activityChargeContinueService;
    @Autowired
    private ActivityTemplateService activityTemplateService;

    // key:activityType
    private Map<Integer, GlobalActivityData> activityMap = new ConcurrentHashMap<>();

    private Map<Integer, MultiHandleGlobalActProcess> multiHandleGlobalActMap = new HashMap<>();

    // @PostConstruct
    public void init() throws InvalidProtocolBufferException {
        iniGlobalActivity();
        multiHandleGlobalActMap.put(ActivityConst.ACT_MAGIC_TREASURE_WARE, this::actGlobalMagicTreasureWare);
    }

    public void iniGlobalActivity() throws InvalidProtocolBufferException {
        List<GlobalActivity> list = activityDao.selectGlobalActivity();
        if (list != null) {
            for (GlobalActivity e : list) {
                GlobalActivityData usualActivity;
                switch (e.getActivityType()) {
                    case ActivityConst.ACT_ROYAL_ARENA:
                        usualActivity = new GlobalRoyalArena(e);
                        break;
                    case ActivityConst.ACT_AUCTION:
                        usualActivity = new GlobalActivityAuctionData(e);
                        break;
                    case ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE:
                        usualActivity = new GlobalActivityCreativeOfficeData(e);
                        break;
                    default:
                        usualActivity = new GlobalActivityData(e);
                }

                activityMap.put(e.getActivityType(), usualActivity);
            }
        }
        // 排行榜相关活动数据初始化
        List<ActivityBase> actBaseList = StaticActivityDataMgr.getActivityList();
        for (ActivityBase e : actBaseList) {
            loadActRankData(e);
        }
        //加载排行数据
        activityTemplateService.execLoadRankOnStartup();
    }

    private void loadActRankData(ActivityBase e) {
        int open = e.getBaseOpen();
        int actType = e.getActivityType();
        if (!StaticActivityDataMgr.isActTypeRank(actType) || open == ActivityConst.OPEN_CLOSE) {// 不是排行活动
            return;
        }
        GlobalActivityData gAct = getGlobalActivity(actType);
        if (gAct == null) {
            return;
        }
        // int rankCapacity =
        // StaticActivityDataMgr.getRankCapacityByActivityId(gAct.getActivityId());
        if (actType == ActivityConst.ACT_EQUIP_TURNPLATE) {// 初始化装备转盘的 全服抽奖次数
            loadActEquipTurnplate(e, gAct);
        } else if (actType == ActivityConst.ACT_CAMP_RANK) {
            loadAndInitActCampRank(e, gAct);
        } else if (actType == ActivityConst.ACT_CAMP_FIGHT_RANK) {
            initAndLoadActCampFightRank();
        } else if (actType == ActivityConst.ACT_DIAOCHAN || actType == ActivityConst.ACT_SEASON_HERO){
            this.loadRankData4DiaoChan(gAct,actType);
        } else if (AbsRankActivityService.isActRankAct(actType)) {
            AbsRankActivityService.loadActRankAct(e, gAct);
        } else {// 其他的排行活动
            loadNormalRank(e, gAct);
        }

        LogUtil.debug("----------初始排行榜活动数据: actvityType:", actType);
    }

    private void loadRankData4DiaoChan(GlobalActivityData gAct,int activityType){
        playerDataManager.getPlayers().values().forEach(player -> {
            Activity act = getActivityInfo(player, activityType);
            //总排行
            Long value = act.getStatusCnt().get(0); // 个人的排行榜进度或存在 0 的位置
            Long time = act.getStatusCnt().get(1);
            if (value != null && time != null) {
                int timeInt = time == null ? 0 : time.intValue();
                gAct.addPlayerRank(player, value, activityType, timeInt); // 添加玩家
            }
            //今日排行
            for(int i=1;i<8;i++){
                Turple<Integer, Integer> turple = act.getDayScore().get(i);
                if(turple != null && turple.getA() != null && turple.getB() != null && turple.getA() > 0){
                    int v1 = turple.getA();
                    int v2 = turple.getB();
                    int type_ = activityDiaoChanService.getDayRankKey(activityType,i);
                    gAct.addPlayerRank(player, (long) v1,type_,v2);
                }
            }
        });
    }

    /**
     * 加载装备转盘的 全服抽奖次数
     *
     * @param gAct
     */
    private void loadActEquipTurnplate(ActivityBase e, GlobalActivityData gAct) {
        int actType = gAct.getActivityType();
        for (Player player : playerDataManager.getPlayers().values()) {
            loadRankByPlayer(gAct, actType, player);
            EquipTurnplat turnplat = (EquipTurnplat) getActivityInfo(player, actType);
            gAct.addInitEquipTurLuckNums(turnplat.getGoldCnt()); // 初始化装备转盘的 全服抽奖次数
        }
    }

    /**
     * 加载初始化普通排行活动
     *
     * @param e
     * @param gAct
     */
    private void loadNormalRank(ActivityBase e, GlobalActivityData gAct) {
        int actType = gAct.getActivityType();
        for (Player player : playerDataManager.getPlayers().values()) {
            loadRankByPlayer(gAct, actType, player);
        }
    }

    /**
     * 加载或初始化阵营排行
     *
     * @param e
     * @param gAct
     */
    private void loadAndInitActCampRank(ActivityBase e, GlobalActivityData gAct) {
        if (e.getBaseOpen() == ActivityConst.OPEN_CLOSE) {
            return;
        }
        int actType = gAct.getActivityType();
        int now = TimeHelper.getCurrentSecond();
        for (Player player : playerDataManager.getPlayers().values()) {
            Activity act = getActivityInfo(player, actType);
            if (act == null) continue;
            Long value = act.getStatusCnt().get(0); // 个人的排行榜进度或存在 0 的位置
            Long time = act.getStatusCnt().get(1);
            if (value != null && time != null) {
                int timeInt = time == null ? 0 : time.intValue();
                gAct.addPlayerRank(player, value, actType, timeInt); // 添加玩家
            } else {
                long fight = player.lord.getFight();
                act.getStatusCnt().put(0, fight);
                act.getStatusCnt().put(1, (long) now);
                gAct.addPlayerRank(player, fight, actType, now); // 添加玩家
            }
        }
    }

    /**
     * 将玩家的数据添加到全局活动数据中
     *
     * @param gAct
     * @param actType
     * @param player
     */
    private void loadRankByPlayer(GlobalActivityData gAct, int actType, Player player) {
        Activity act = getActivityInfo(player, actType);
        Long value = act.getStatusCnt().get(0); // 个人的排行榜进度或存在 0 的位置
        Long time = act.getStatusCnt().get(1);
        if (value != null && time != null) {
            int timeInt = time == null ? 0 : time.intValue();
            gAct.addPlayerRank(player, value, actType, timeInt); // 添加玩家
        }
    }

    /**
     * 是否可以满足条件
     *
     * @param player
     * @return true表示
     */
    private boolean isActCampFightCond(Player player) {
        long fight = player.lord.getFight();
        int lv = player.lord.getLevel();
        return lv > ActParamConstant.ACT_CAMP_FIGHT_RANK_JOIN_COND.get(0)
                && fight > ActParamConstant.ACT_CAMP_FIGHT_RANK_JOIN_COND.get(1);
    }

    /**
     * 初始化和加载阵营战力排行
     */
    public void initAndLoadActCampFightRank() {
        int actType = ActivityConst.ACT_CAMP_FIGHT_RANK;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (activityBase == null || activityBase.getBaseOpen() == ActivityConst.OPEN_CLOSE) return;
        GlobalActivityData gAct = getGlobalActivity(actType);
        if (gAct == null) return;
        int now = TimeHelper.getCurrentSecond();
        // int rankCapacity = StaticActivityDataMgr.getRankCapacityByActivityId(gAct.getActivityId());
        // 阵营战斗力排行
        for (Player player : playerDataManager.getPlayers().values()) {
            Activity act = getActivityInfo(player, actType);
            if (act == null) continue;
            Long value = act.getStatusCnt().get(0); // 个人的排行榜进度或存在 0 的位置
            Long time = act.getStatusCnt().get(1);
            if (value != null && time != null) {
                int timeInt = time == null ? 0 : time.intValue();
                gAct.addPlayerRank(player, value, actType, timeInt); // 添加玩家
            } else {
                long fight = player.lord.getFight();
                act.getStatusCnt().put(0, fight);
                act.getStatusCnt().put(1, (long) now);
                gAct.addPlayerRank(player, fight, actType, now); // 添加玩家
                if (isActCampFightCond(player)) {
                    if (player.lord.getCamp() == Constant.Camp.EMPIRE) {
                        gAct.getTopupa().addAndGet(fight);
                    } else if (player.lord.getCamp() == Constant.Camp.ALLIED) {
                        gAct.getTopupb().addAndGet(fight);
                    } else if (player.lord.getCamp() == Constant.Camp.UNION) {
                        gAct.getTopupc().addAndGet(fight);
                    }
                }
            }
        }
    }

    public Map<Integer, GlobalActivityData> getActivityMap() {
        return activityMap;
    }

    public void updateActivityData(GlobalActivity activity) {
        activityDao.updateActivity(activity);
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-06 14:09
     * @Description: 检查建筑升级是否满足触发礼包条件
     */
    public int checkBuildTrigger(Player player, int type, int buildLevel) {
        List<StaticTriggerConf> triggerConfs = StaticActivityDataMgr
                .getTriggerGiftConfById(ActivityConst.TRIGGER_GIFT_UPGRADE_BUILD, player);
        if (triggerConfs == null) {
            return 0;
        }
        for (StaticTriggerConf conf : triggerConfs) {
            if (conf.getCond().get(0) == type && conf.getCond().get(1) == buildLevel + 1) {
                return conf.getGiftId();
            }
        }
        return 0;
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-01 19:30
     * @Description: 获取触发式礼包信息
     */
    public Map<Integer, TriggerGift> getTriggerGiftInfo(Player player, int triggerId) throws MwException {
        List<StaticTriggerConf> triggerConf = StaticActivityDataMgr.getTriggerGiftConfById(triggerId, player);
        if (triggerConf == null) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测触发配置,没有该触发条件 roleId:", player.roleId,
                    ", TriggerId:", triggerId);
        }
        // if (!checkPlayerLevel(player, triggerConf)) {
        // throw new MwException(GameError.TRIGGER_LEVEL_ERR.getCode(), " 检测触发配置,触发等级不满足
        // roleId:", player.roleId,
        // ", TriggerId:", triggerId);
        // }

        Map<Integer, TriggerGift> TriggerGiftMap = new HashMap<>();
        for (StaticTriggerConf conf : triggerConf) {
            TriggerGiftMap = player.triggerGifts.computeIfAbsent(conf.getTriggerId(), k -> new HashMap<>());
            TriggerGift gift = TriggerGiftMap.get(conf.getGiftId());
            if (gift == null) {
                gift = new TriggerGift(conf.getGiftId());
                TriggerGiftMap.put(conf.getGiftId(), gift);
            } else {
                // 重置礼包
                gift.isRestart(checkTriggerGiftIsRestart(gift, conf, player));
            }
        }
        return TriggerGiftMap;
    }

    /**
     * 检查礼包状态
     *
     * @param gift        礼包
     * @param triggerConf 触发式礼包配置
     * @param giftpack    活动礼包奖励表
     * @param now         现在的时间
     * @return true 礼包状态正常 false 礼包状态错误
     */
    public boolean checkGiftState(TriggerGift gift, StaticTriggerConf triggerConf, StaticActGiftpack giftpack,
                                  int now) {
        if (gift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
            LogUtil.common("触发式礼包还未触发 giftpackId:", gift.getGiftId(), ", state:", gift.getState());
            return false;
        }
        // 已经过了持续时间
        if (gift.getEndTime() <= now) {
            LogUtil.common("礼包已经过了持续时间 giftpackId:", gift.getGiftId(), ", endTime:", gift.getEndTime());
            return false;
        }
        // 购买次数已使用完
        if (gift.getCount() >= giftpack.getCount()) {
            LogUtil.common("购买次数已使用完 giftpackId:", gift.getGiftId(), ", count:", gift.getCount());
            return false;
        }
        return true;
    }

    /**
     * 更新触发式礼包进度
     *
     * @param triggerId
     * @param player
     * @param schedule
     */
    public void updateTriggerStatus(int triggerId, Player player, int schedule) {
        try {
            int now = TimeHelper.getCurrentSecond();
            List<StaticTriggerConf> triggerConf = StaticActivityDataMgr.getTriggerGiftConfById(triggerId, player);
            if (CheckNull.isEmpty(triggerConf)) {
                return;
            }
            Map<Integer, TriggerGift> triggerGiftInfo = getTriggerGiftInfo(player, triggerId);
            List<TriggerGift> triggerGifts = new ArrayList<>(triggerGiftInfo.values());
            GamePb3.SyncTriggerGiftRs.Builder builder = GamePb3.SyncTriggerGiftRs.newBuilder();
            for (TriggerGift triggerGift : triggerGifts) {
                StaticTriggerConf conf = triggerConf.stream().filter(t -> t.getGiftId() == triggerGift.getGiftId())
                        .findFirst().orElse(null);
                if (CheckNull.isNull(conf)) {
                    continue;
                }
                StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
                if (CheckNull.isNull(triggerGiftConf)) {
                    throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:",
                            player.roleId, ", giftId:", conf.getGiftId());
                }
                int status = triggerGift.getStatus();
                triggerGift.setStatus(status + schedule);
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_REBUILD
                        || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL
                        || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_EXPEDITION_FAIL
                        || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_TREASURE_OPEN) { // 重建家园、战役战斗失败、帝国远征战斗失败、翻牌触发礼包
                    triggerGift.setBeginTime(now);// 更新上次被击飞时间
                }
            }
        } catch (MwException e) {
            LogUtil.error("更新触发式礼包进度,Error:", e);
        }
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-03-01 19:30
     * @Description: 获取触发式礼包信息
     */
    public TriggerGift getTriggerGiftInfoByGiftId(Player player, int giftId, boolean checkLevel) throws MwException {
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        if (conf == null) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测触发配置,没有该触发条件 roleId:", player.roleId,
                    ", giftId:", giftId);
        }
        if (checkLevel && !conf.checkTriggerOpenCnf(player)) {
            throw new MwException(GameError.TRIGGER_LEVEL_ERR.getCode(), " 检测触发配置,触发条件不满足 roleId:", player.roleId,
                    ", giftId:", giftId);
        }
        Map<Integer, TriggerGift> TriggerGiftMap = player.triggerGifts.computeIfAbsent(conf.getTriggerId(), k -> new HashMap<>());
        TriggerGift gift = TriggerGiftMap.get(conf.getGiftId());
        if (gift == null) {
            gift = new TriggerGift(conf.getGiftId());
            TriggerGiftMap.put(conf.getGiftId(), gift);
        } else {
            gift.isRestart(checkTriggerGiftIsRestart(gift, conf, player));
        }
        return gift;
    }

    /**
     * @Description: 获取触发式礼包信息(时间触发类型)
     */
    public TriggerGift getTimeTriggerGiftInfo(Player player, int giftId, int triggerPlanId, boolean checkLevel) throws MwException {
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        if (conf == null) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), "检测触发配置,没有该触发条件 roleId:", player.roleId,
                    ", giftId: ", giftId);
        }
        if (checkLevel && !conf.checkTriggerOpenCnf(player)) {
            throw new MwException(GameError.TRIGGER_LEVEL_ERR.getCode(), "检测触发配置,触发条件不满足 roleId:", player.roleId,
                    ", giftId: ", giftId);
        }
        Map<Integer, TriggerGift> TriggerGiftMap = player.triggerGifts.computeIfAbsent(conf.getTriggerId(), k -> new HashMap<>());
        TriggerGift gift = TriggerGiftMap.get(conf.getGiftId());
        if (gift == null) {
            gift = new TriggerGift(conf.getGiftId(), triggerPlanId);
            TriggerGiftMap.put(conf.getGiftId(), gift);
        } else {
            if (gift.getKeyId() != triggerPlanId) {
                gift.isRestart(true);
                gift.setKeyId(triggerPlanId);
            }
        }
        return gift;
    }

    /**
     * 检测礼包是否过了CD期间
     *
     * @param gift
     * @param conf
     * @param player
     * @return
     * @throws MwException
     */
    private boolean checkTriggerGiftIsRestart(TriggerGift gift, StaticTriggerConf conf, Player player) {
        int now = TimeHelper.getCurrentSecond();
        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        if (CheckNull.isNull(triggerGiftConf)) {
            LogUtil.error(" 检测礼包配置,没有该礼包配置 roleId:", player.roleId, ", giftId:", conf.getGiftId());
            return false;
        }
        // CD时间为0, 则认为不会重置触发式礼包
        if (conf.getInterval() == 0) {
            return false;
        }
        if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_REBUILD
                || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL
                || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_EXPEDITION_FAIL
                || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_TREASURE_OPEN) { // 重建家园
            Date beginDate = TimeHelper.getDate(Long.valueOf(gift.getBeginTime()));
            if (gift.getBeginTime() > 0 && DateHelper.dayiy(beginDate, new Date()) > 1) { // 不是同一天
                return true;
            }
        }
        // 时间触发礼包 有且只能触发一次, 将领授勋礼包也只能触发一次
        else if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_TIME_COND
                || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_HERO_DECORATED
                || conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_HERO_SEARCH) {
            if (gift.getState() != ActivityConst.NOT_TRIGGER_STATUS) {
                return false;
            }
        } else if (gift.getState() != ActivityConst.NOT_TRIGGER_STATUS) {// 未触发
            // 已经过了持续时间
            if (gift.getEndTime() <= now) {
                // 已经过了CD时间
                if (gift.getEndTime() + conf.getInterval() <= now) {
                    return true;
                }
            }
        }
        return false;
    }

    // /**
    //  * 检测玩家等级是否满足
    //  *
    //  * @param player
    //  * @param staticTriggerConf
    //  * @return
    //  */
    // public boolean checkPlayerLevel(Player player, StaticTriggerConf... staticTriggerConf) {
    //     for (StaticTriggerConf triggerConf : staticTriggerConf) {
    //         if (triggerConf.getLevel().get(0) <= player.lord.getLevel()
    //                 && triggerConf.getLevel().get(1) >= player.lord.getLevel()) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // /**
    //  * 检测玩家等级是否满足
    //  *
    //  * @param player
    //  * @param staticTriggerConf
    //  * @return
    //  */
    // public boolean checkPlayerLevel(Player player, List<StaticTriggerConf> staticTriggerConf) {
    //     for (StaticTriggerConf triggerConf : staticTriggerConf) {
    //         if (triggerConf.getLevel().get(0) <= player.lord.getLevel()
    //                 && triggerConf.getLevel().get(1) >= player.lord.getLevel()) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // /**
    //  * 检测玩家vip等级是否满足
    //  *
    //  * @param player
    //  * @param staticTriggerConf
    //  * @return
    //  */
    // public boolean checkPlayerVipLevel(Player player, StaticTriggerConf... staticTriggerConf) {
    //     for (StaticTriggerConf triggerConf : staticTriggerConf) {
    //         if (triggerConf.getVip().get(0) <= player.lord.getVip()
    //                 && triggerConf.getVip().get(1) >= player.lord.getVip()) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    /**
     * Function:活动信息通用部分,开启,记录重置
     *
     * @param player
     * @param activityType
     * @return
     */
    public Activity getActivityInfo(Player player, int activityType) {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
//        if(activityType == ActivityConst.ACT_DIAOCHAN || activityType == ActivityConst.ACT_SEASON_HERO){
//            activityBase = StaticActivityDataMgr.getActivityByType0(activityType);
//        }
        if (activityBase == null) {
            return null;
        }
        Date beginTime = activityBase.getBeginTime();
        int begin = TimeHelper.getDay(beginTime);
        Activity activity = player.activitys.get(activityType);
        if (activity == null) {
            activity = conActivity(activityBase, activityType, begin, player);
            player.activitys.put(activityType, activity);
        } else {
            activity.isReset(begin, player);// 是否重新设置活动
            activity.autoDayClean(activityBase);
            activity.cleanActivityAuction(activityBase);
        }
        activity.setOpen(activityBase.getBaseOpen());
        return activity;
    }

    public Activity getPersonalActivityInfo(Player player, int actKeyId) {
        ActivityBase ab = StaticActivityDataMgr.getPersonalActivityByType(actKeyId);
        if (ab == null) {
            return null;
        }
        Date beginTime = ab.getBeginTime();
        int begin = TimeHelper.getDay(beginTime);
        Activity activity = player.activitys.get(ab.getActivityType());
        if (activity == null) {
            activity = conActivity(ab, ab.getActivityType(), begin, player);
            player.activitys.put(ab.getActivityType(), activity);
        } else {
            activity.isReset(begin, player);// 是否重新设置活动
            activity.autoDayClean(ab);
            activity.cleanActivityAuction(ab);
        }
        activity.setOpen(ab.getBaseOpen());
        return activity;
    }

    /**
     * 初始化Activity对象
     *
     * @param activityBase
     * @param activityType
     * @param begin
     * @param player
     * @return
     */
    public Activity conActivity(ActivityBase activityBase, int activityType, int begin, Player player) {
        Activity activity;
        if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE
                || activityType == ActivityConst.FAMOUS_GENERAL_TURNPLATE
                || activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                || activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                || activityType == ActivityConst.ACT_SEASON_TURNPLATE
                || activityType == ActivityConst.ACT_MAGIC_TREASURE_WARE
        ) {
            activity = new ActTurnplat(activityBase, begin, player);
        } else if (activityType == ActivityConst.ACT_EQUIP_TURNPLATE) {
            activity = new EquipTurnplat(activityBase, begin, player);
        } else {
            activity = new Activity(activityBase, begin);
        }
        return activity;
    }

    public GlobalActivityData getGlobalActivity(int activityType) {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
//        if(activityType == ActivityConst.ACT_DIAOCHAN || activityType == ActivityConst.ACT_SEASON_HERO){
//            activityBase = StaticActivityDataMgr.getActivityByType0(activityType);
//        }
        if (activityBase == null) {
            return null;
        }
        int open = activityBase.getBaseOpen();
        Date beginTime = activityBase.getBeginTime();
        // int begin = TimeHelper.getDay(beginTime);
        // 精确到分钟重置
        int begin = (int) (beginTime.getTime() / 6000);
        // LogUtil.debug("全服活动type=" + activityType + ",beginTime=" + beginTime + ",begin=" + begin);
        GlobalActivityData activity = activityMap.get(activityType);
        if (activity == null) {
            activity = conGlobalActivity(activityBase, activityType, begin);
            if (activityType == ActivityConst.ACT_LUCKY_POOL) {
                activity.setGoal(ActParamConstant.LUCKY_POOL_1.get(0));
            }
            activityMap.put(activityType, activity);
        } else {
            activity.isReset(begin, null);
            activity.autoDayClean(activityBase);
            //校验秋季拍卖活动
            GlobalActivityAuctionData.checkInit(activity, activityBase.getPlanKeyId());
        }
        activity.setOpen(open);
        return activity;
    }

    public GlobalActivityData conGlobalActivity(ActivityBase activityBase, int activityType, int begin) {
        GlobalActivityData activity;
        switch (activityType) {
            case ActivityConst.ACT_ROYAL_ARENA:
                activity = new GlobalRoyalArena(activityBase, begin);
                break;
            case ActivityConst.ACT_AUCTION:
                activity = new GlobalActivityAuctionData(activityBase, begin);
                break;
            case ActivityConst.ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE:
                activity = new GlobalActivityCreativeOfficeData(activityBase, begin);
                break;
            default:
                activity = new GlobalActivityData(activityBase, begin);
                break;
        }

        return activity;
    }

    /**
     * 更新玩家活动记录
     *
     * @param player
     * @param activityType
     * @param schedule     消费的金币数
     * @param sync         是否需要推送
     */
    public void updActivity(Player player, int activityType, long schedule, int sortId, boolean sync) {
        if (player == null) {
            return;
        }
        try {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if (activityBase == null) {
                return;
            }
            int step = activityBase.getStep();
            if (step != ActivityConst.OPEN_STEP) {
                return;
            }

            Date beginTime = activityBase.getBeginTime();
            int begin = TimeHelper.getDay(beginTime);
            Activity activity = player.activitys.get(activityType);
            if (activity == null) {
                activity = new Activity(activityBase, begin);
                player.activitys.put(activityType, activity);
                activity.setEndTime(TimeHelper.getCurrentDay());
            } else {
                activity.isReset(begin, player);// 是否重新设置活动
                activity.autoDayClean(activityBase);
            }
            if (activityType == ActivityConst.ACT_ONLINE_GIFT) {
                activity.getStatusCnt().put(sortId, schedule);
            } else if (activityType == ActivityConst.ACT_ORE_TURNPLATE) {// 矿石转盘
                // 首先根据总次数 算出当前是处在哪个档位
                int gear = getOreTurnplateGear(activity);
                int surplusGold = activity.getStatusMap().get(ActivityConst.SURPLUS_GOLD) == null ? 0
                        : activity.getStatusMap().get(ActivityConst.SURPLUS_GOLD);// 当前档位已消费的金币数
                int countGold = (int) (surplusGold + schedule);
                while (countGold >= gear) {// 当前消费金币数 + 上次计算剩余的金币数 >= 当前档位值 则增加一次抽奖次数
                    // 抽奖次数+1
                    int surplusNum = activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                            : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM);
                    activity.getStatusMap().put(ActivityConst.SURPLUS_NUM, surplusNum + 1);
                    countGold -= gear;
                    // 刷新档位值
                    gear = getOreTurnplateGear(activity);
                }
                // 重新赋值本次消费 剩余
                activity.getStatusMap().put(ActivityConst.SURPLUS_GOLD, countGold);
                player.activitys.put(activityType, activity);
            } else if (activityType == ActivityConst.ACT_MONOPOLY) { // 大富翁
                processUpActMonopoly(player, activity, schedule, sortId);
            } else if (activityType == ActivityConst.ACT_CHARGE_CONTINUE
                    ||activityType==ActivityConst.ACT_MERGE_CHARGE_CONTINUE) { // 连续充值
                activityChargeContinueService.updateChargeContinueActivity(activityBase, activity, schedule);
            } else if (activityType == ActivityConst.ACT_LUCKY_POOL) {
                activity.getSaveMap().put(1, activity.getSaveMap().getOrDefault(1, 0) + (int) schedule);
            } else if (activityType == ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE) {
                Long time = activity.getStatusCnt().getOrDefault(sortId, 0L);
                if (time == 0) {
                    activity.getStatusCnt().put(sortId, schedule);
                }
            } else {
                Long state = activity.getStatusCnt().get(sortId);
                state = state == null ? 0 : state;
                state = state + schedule;
                activity.getStatusCnt().put(sortId, state);
                // 增加积分日志
                LogLordHelper.commonLog("expenditure", AwardFrom.PAY_TURNPLATE, player.account, player.lord, player,
                        state, schedule, activityType);
            }
        } catch (Exception e) {
            LogUtil.error("Activity Exception : " + activityType, e);
        }
        if (sync) {
            // 检查推送
            syncActChange(player, activityType);
        }
    }

    /**
     * 更新活动进度处理大富翁
     *
     * @param player
     * @param schedule
     * @param sortId
     */
    private void processUpActMonopoly(Player player, Activity activity, long schedule, int sortId) {
        // 获取配置档位的配置
        int actId = activity.getActivityId();
        List<StaticActMonopolyCount> samcList = StaticActivityDataMgr.getActMonopolyCountByActId(actId);
        if (CheckNull.isEmpty(samcList)) {
            LogUtil.error("大富翁活动 activityId:", actId, ", 的档位未配置, roleId:", player.roleId, ", schedule:", schedule);
            return;
        }
        int round = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, 1)
                .intValue();
        StaticActMonopolyCount samc = samcList.stream().filter(s -> s.getRound() == round).findFirst().orElse(null);
        if (samc == null) {
            LogUtil.error("大富翁活动 activityId:", actId, ", 的档位未配置, roleId:", player.roleId, ", schedule:", schedule,
                    ", round:", round);
            return;
        }
        long todayPayGold = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY,
                0L); // 今日充值金币数量
        long todayAllPayGold = activity.getStatusCnt()
                .getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_ALL_PAY_KEY, 0L);// 今日充值金币总数量(注意这个在进行下一轮的时候不会被清除)
        long hasCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, 0L);// 今日拥有玩色子的次数
        if (todayAllPayGold == 0) { // 当天的首次充值
            // 判断昨天是否有充值
            if (checkMonopolyYesterday(activity)) {
                hasCnt++;
            }
            int now = TimeHelper.getCurrentSecond();
            activity.getSaveMap().put(ActivityConst.ActMonopolyKey.SAVEMAP_LAST_PAY_TIME_KEY, now);
        }
        long curTodayPayGold = todayPayGold + schedule;
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY, curTodayPayGold);// 记录当天充值金币数
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_ALL_PAY_KEY,
                todayAllPayGold + schedule);// 记录当天充值总金币数

        long addCnt = samc.getCount().stream().filter(gold -> gold > todayPayGold && gold <= curTodayPayGold).count();
        hasCnt += addCnt;
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, hasCnt);// 次数进行存储
    }

    /**
     * 判断大富翁昨天是否有充值
     *
     * @param activity
     * @return true表示昨天有充值
     */
    public boolean checkMonopolyYesterday(Activity activity) {
        if (activity.getActivityType() == ActivityConst.ACT_MONOPOLY) {
            int lastPayTime = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_LAST_PAY_TIME_KEY,
                    0);// 上一次充值是时间
            Date lastPayDate = new Date(lastPayTime * 1000L);
            return lastPayTime == 0 ? false : DateHelper.dayiy(lastPayDate, new Date()) == 2;
        }
        return false;
    }

    /**
     * @Title: getOreTurnplateGear @Description: 矿石转盘 获取玩家当前所处的档位值 @param activity @return 参数 int 返回类型 @throws
     */
    public int getOreTurnplateGear(Activity activity) {

        int surplusNum = activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM);
        int alreadyNum = activity.getStatusMap().get(ActivityConst.ALREADY_NUM) == null ? 0
                : activity.getStatusMap().get(ActivityConst.ALREADY_NUM);
        // 玩家当前抽奖总次数
        int count = surplusNum + alreadyNum;
        int gear = 0;// 档位
        List<List<Integer>> lists = Constant.ORE_TURNPLATE_CONFIG;
        if (lists != null && lists.size() > 0) {
            if (count == 0) {// 第一次 直接取第一个档位
                if (lists.get(0).size() < 3) {// 第一个档位配置异常 返回默认
                    return 30;
                }
                return lists.get(0).get(2);
            }
            for (int i = 0; i < lists.size(); i++) {
                if (lists.get(i).size() < 3) {// 配置异常 直接返回默认档位
                    return 30;
                }
                if (count == lists.get(i).get(1)) {// 如果是当前档位的最后一次 则取下个档位的值
                    if (i + 1 == lists.size() || lists.get(i + 1).size() < 3) {// 下一个档位不存在 或者 下一个档位配置异常
                        gear = lists.get(i).get(2);// 直接返回当前档位
                        break;
                    }
                    gear = lists.get(i + 1).get(2);// 返回下个档位
                    break;
                }
                if (count >= lists.get(i).get(0) && count <= lists.get(i).get(1)) {// 返回当前档位
                    gear = lists.get(i).get(2);
                    break;
                }
            }
            if (gear == 0) {// 防止极端 总次数已经超过配置的最大次数 则按最后一个档位算
                return lists.get(lists.size() - 1).get(2);
            }
            return gear;
        } else {// 配置为空 返回默认档位
            return 30;
        }
    }

    /**
     * 更新全局活动
     *
     * @param player
     * @param activityType
     * @param schedule
     * @param sortId
     */
    public void updGlobalActivity(Player player, int activityType, long schedule, int sortId) {
        try {
            GlobalActivityData usualActivityData = getGlobalActivity(activityType);
            if (usualActivityData == null) {
                return;
            }
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if (activityBase == null) {
                return;
            }
            int step = activityBase.getStep();
            if (step != ActivityConst.OPEN_STEP) {
                return;
            }
            // 军备促销、全军返利、勇冠三军
            if (activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_PROP_PROMOTION || activityType == ActivityConst.ACT_BRAVEST_ARMY) {
                // 全军返利、勇冠三军 逻辑 添加玩家个人的进度
                if (activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY) {
                    if (player == null) {
                        return;
                    }

                    // 等级不到，不算入消耗记录
                    if (activityType == ActivityConst.ACT_BRAVEST_ARMY && !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.ACT_ACT_BRAVEST_ARMY)) {
                        return;
                    }
                    Activity activity = getActivityInfo(player, activityType);
                    if (activity != null && activity.getSaveMap() != null) {
                        // 玩家已充值的金币总数
                        int topup = activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                                : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD);
                        activity.getSaveMap().put(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD, (int) schedule + topup);
                    }
                }
                usualActivityData.getCampValByCampAtomic(sortId).addAndGet(schedule);
                // if (activityType == ActivityConst.ACT_ALL_CHARGE) {
                // 检查并推送
                syncAllPlayerActChange(player, activityType);
                // }
            } else if (activityType == ActivityConst.ACT_ATK_GESTAPO) {// 高32位:时间,低32位:数值
                int val = (int) schedule;
                int now = TimeHelper.getCurrentSecond();
                AtomicLong timeValAtomic = usualActivityData.getCampValByCampAtomic(sortId);
                long timeVal = timeValAtomic.get();
                val = NumUtils.separateLong2int(timeVal)[0] + val;
                timeValAtomic.set(NumUtils.combineInt2Long(val, now));
            } else if (activityType == ActivityConst.ACT_LUCKY_POOL) {
                int add = (int) ((ActParamConstant.LUCKY_POOL_1.get(1) / 10000d) * schedule);
                usualActivityData.setGoal(usualActivityData.getGoal() + add);
                syncAllPlayerActChange(player, activityType);
            }
        } catch (Exception e) {
            // LogHelper.ERROR_LOGGER.error("Activity Exception : " + activityId, e);
            LogUtil.error("Activity Exception : " + activityType, e);
        }
        // syncActChange(player, activityType);

    }

    /**
     * 更新排行榜，如果一个活动持续多天，每天都需要一个当天的排行榜则用
     *  貂蝉活动 100000000 + type * 100 + day
     * @param player
     * @param type
     * @param subType
     * @param value
     */
    public void updateDayRank(Player player, int type, int subType, int value){
        Activity activity = getActivityInfo(player, type);
        if (activity == null) {
            return;
        }
        GlobalActivityData globalActivityData = getGlobalActivity(type);
        if (globalActivityData == null) {
            return;
        }
        int currValue = 0;
        if(type == ActivityConst.ACT_DIAOCHAN || type == ActivityConst.ACT_SEASON_HERO){
            currValue = activityDiaoChanService.getRankDayScore(activity,subType);
        }
        currValue += value;
        if(currValue <= 0){
            return;
        }
        int now = TimeHelper.getCurrentSecond();

        activity.getDayScore().get(subType).setA(currValue);
        activity.getDayScore().get(subType).setB(now);

        int type_ = 0;
        if(type == ActivityConst.ACT_DIAOCHAN || type == ActivityConst.ACT_SEASON_HERO){
            type_ = activityDiaoChanService.getDayRankKey(type,subType);
        }
        globalActivityData.addPlayerRank(player, (long) currValue,type_,now);
    }

    /**
     * 更新全局统计排行榜
     *
     * @param player
     * @param activityType
     * @param schedule
     */
    public void updRankActivity(Player player, int activityType, long schedule) {
        if (player == null) {
            return;
        }
        try {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if (activityBase == null) {
                return;
            }
            int step = activityBase.getStep();
            if (step != ActivityConst.OPEN_STEP) {
                return;
            }

            if (activityType == ActivityConst.ACT_CAMP_RANK || activityType == ActivityConst.ACT_PAY_RANK_NEW
                    || activityType == ActivityConst.ACT_PAY_RANK_V_3
                    || activityType == ActivityConst.ACT_MERGE_PAY_RANK
                    || activityType == ActivityConst.ACT_CAMP_FIGHT_RANK
                    || activityType == ActivityConst.ACT_CONSUME_GOLD_RANK
                    || activityType == ActivityConst.ACT_TUTOR_RANK) {
                Date now = new Date();// 当前时间
                if (now.getTime() >= activityBase.getAwardBeginTime().getTime()) {
                    // 阵容奖励已经到了领奖时间了 不能在更新榜单了
                    return;
                }
            }

            // 玩家
            Activity activity = getActivityInfo(player, activityType);
            if (activity == null) {
                return;
            }

            GlobalActivityData usualActivityData = getGlobalActivity(activityType);
            if (usualActivityData == null) {
                return;
            }
            schedule = needFixSchedule(player, activity, schedule);
            if (schedule <= 0) {// 无意义
                return;
            }
            // 更新状态
            int sortId = 0;// 进度会存在key = 0中
            Long state = activity.getStatusCnt().get(sortId);
            state = state == null ? 0 : state;
            long oldState = state; // 旧值
            state = state + schedule;
            int now = TimeHelper.getCurrentSecond();
            int timeKey = 1; // 时间会存到 key = 1中
            activity.getStatusCnt().put(sortId, state);
            activity.getStatusCnt().put(timeKey, Long.valueOf(now));
            usualActivityData.addPlayerRank(player, state, activityType, now);
            int awardTime = (int) (activityBase.getAwardBeginTime().getTime() / 1000);
            if (now >= awardTime) {
                // 最高排名存储到 key = 3中,只有可领奖之后才会记录最高值
                int topRankKey = 3;
                ActRank actRank = usualActivityData.getPlayerRank(player, activityType, player.roleId);
                if (actRank != null) {
                    long realRank = actRank.getRank();
                    Long topRank = activity.getStatusCnt().get(topRankKey);
                    if (topRank != null) {
                        realRank = Math.min(actRank.getRank(), topRank.longValue());
                    }
                    activity.getStatusCnt().put(topRankKey, realRank);
                }
            }

            // 阵营战斗力活动
            if (activityType == ActivityConst.ACT_CAMP_FIGHT_RANK) {
                if (isActCampFightCond(player)) {
                    long addFight = schedule;
                    if (oldState <= ActParamConstant.ACT_CAMP_FIGHT_RANK_JOIN_COND.get(1)) {
                        addFight = state;
                    }
                    // 阵营战斗力值
                    if (player.lord.getCamp() == Constant.Camp.EMPIRE) {
                        usualActivityData.getTopupa().addAndGet(addFight);
                    } else if (player.lord.getCamp() == Constant.Camp.ALLIED) {
                        usualActivityData.getTopupb().addAndGet(addFight);
                    } else if (player.lord.getCamp() == Constant.Camp.UNION) {
                        usualActivityData.getTopupc().addAndGet(addFight);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("Activity Exception : " + activityType, e);
        }
        syncActChange(player, activityType);
    }

    /**
     * 玩家升到指定等级时触发
     *
     * @param player
     */
    public void upLvPlusActCampFightRank(Player player) {
        int activityType = ActivityConst.ACT_CAMP_FIGHT_RANK;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            return;
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.OPEN_STEP) {
            return;
        }
        Activity activity = getActivityInfo(player, activityType);
        if (activity == null) {
            return;
        }
        GlobalActivityData usualActivityData = getGlobalActivity(activityType);
        if (usualActivityData == null) {
            return;
        }
        if (isActCampFightCond(player)) {
            long addFight = player.lord.getFight();
            // 阵营战斗力值
            if (player.lord.getCamp() == Constant.Camp.EMPIRE) {
                usualActivityData.getTopupa().addAndGet(addFight);
            } else if (player.lord.getCamp() == Constant.Camp.ALLIED) {
                usualActivityData.getTopupb().addAndGet(addFight);
            } else if (player.lord.getCamp() == Constant.Camp.UNION) {
                usualActivityData.getTopupc().addAndGet(addFight);
            }
        }

    }

    /**
     * 需要修正排进度值
     *
     * @param player
     * @param activity
     * @param schedule
     * @return
     */
    private long needFixSchedule(Player player, Activity activity, long schedule) {
        int actType = activity.getActivityType();
        if (actType == ActivityConst.ACT_CAMP_RANK || actType == ActivityConst.ACT_CAMP_FIGHT_RANK) {
            long oldSchedule = activity.getStatusCnt().getOrDefault(0, 0L);
            if (schedule > oldSchedule) {
                // 获取最大战力，同时更新到排行榜
                return schedule - oldSchedule;
            } else {
                return 0;
            }
        }
        return schedule;
    }

    public void refreshDay(Activity activity) {
        if (activity == null) {
            return;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            return;
        }
        activity.autoDayClean(activityBase);
    }

    /**
     * 获取排行活动
     *
     * @param player
     * @param activityType
     * @return
     */
    public int getRankAwardSchedule(Player player, int activityType) {
        if (!StaticActivityDataMgr.isActTypeRank(activityType)) {
            return 0;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            return 0;
        }
        Activity activity;
        GlobalActivityData usualActivityData;
        activity = getActivityInfo(player, activityType);
        usualActivityData = getGlobalActivity(activityType);
        if (activity == null || usualActivityData == null) {
            return 0;
        }
        int curRank = currentActivity(player, activity, 0);
        if (curRank == 0) {// 当前无排名,直接返回0
            return 0;
        }

        if (activityType == ActivityConst.ACT_PAY_RANK_NEW
                || activityType == ActivityConst.ACT_PAY_RANK_V_3
                ||activityType == ActivityConst.ACT_MERGE_PAY_RANK
                || activityType == ActivityConst.ACT_CAMP_RANK
                || activityType == ActivityConst.ACT_CAMP_FIGHT_RANK
                || activityType == ActivityConst.ACT_CONSUME_GOLD_RANK) {// 新充值排行活动
            return curRank;
        }

        // 最高排名存储到 key = 3中
        int topRankKey = 3;
        Long topRank = activity.getStatusCnt().get(topRankKey);
        return (topRank == null || topRank.intValue() == 0) ? curRank : topRank.intValue();
    }

    /**
     * 取当前奖励状态
     */
    public int getDay7ActStatus(Player player, StaticDay7Act staticDay7Act) {
        Integer lvMax = 0;
        switch (staticDay7Act.getTaskType()) {
            case ActivityConst.ACT_TASK_LOGIN://
                Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
                int dayiy = DateHelper.dayiy(beginTime, new Date());
                if (dayiy >= staticDay7Act.getDay()) {
                    lvMax = 1;
                }
                break;
            case ActivityConst.ACT_TASK_TECH:
                lvMax = techDataManager.getTechLv(player, staticDay7Act.getParam().get(0));
                break;
            case ActivityConst.ACT_TASK_RECRUIT:
            case ActivityConst.ACT_TASK_ATTACK:
            case ActivityConst.ACT_TASK_JOIN_ATK:
                StringBuilder sb = new StringBuilder(staticDay7Act.getTaskType() + "");
                if (staticDay7Act.getParam() != null && !staticDay7Act.getParam().isEmpty()) {
                    for (int i = 0; i < staticDay7Act.getParam().size(); i++) {
                        sb.append("_" + staticDay7Act.getParam().get(i));
                    }
                }

                String key = sb.toString();
                if (player.day7Act.getTankTypes().containsKey(key)) {
                    lvMax += player.day7Act.getTankTypes().get(key);
                }
                LogUtil.debug("参与攻下几座指定类型的城，最新=" + player.day7Act.getTankTypes() + ",key=" + key + ",case="
                        + staticDay7Act.getTaskType());
                break;
            case ActivityConst.ACT_TASK_LEVEL:
                lvMax = player.lord.getLevel();
                break;
            case ActivityConst.ACT_TASK_ALL_VIP:
                lvMax = playerDataManager.countVip(staticDay7Act.getParam().get(0));
                break;
            case ActivityConst.ACT_TASK_CITY:
                int param = 0;
                if (!staticDay7Act.getParam().isEmpty() && staticDay7Act.getParam().size() > 0) {
                    param = staticDay7Act.getParam().get(0);
                }
                int areaId = MapHelper.getAreaIdByPos(player.lord.getPos());
                lvMax = worldDataManager.getCityTypeNum4CampAndArea(areaId, player.lord.getCamp(), param);
                break;
            case ActivityConst.ACT_TASK_FIGHT:// 战力值达到10000
                lvMax = (int) player.lord.getFight();
                break;
            case ActivityConst.ACT_TASK_COST_GOLD:
                if (player.day7Act.getStatus().containsKey(staticDay7Act.getTaskType())) {
                    lvMax += player.day7Act.getStatus().get(staticDay7Act.getTaskType());
                }
                break;
            case ActivityConst.ACT_TASK_EQUIP://
                Map<Integer, Equip> map = player.equips;
                if (map != null) {
                    StaticEquip staticEquip = null;
                    Iterator<Equip> it = map.values().iterator();
                    while (it.hasNext()) {
                        Equip equip = it.next();
                        staticEquip = StaticPropDataMgr.getEquip(equip.getEquipId());
                        if (staticEquip.getQuality() >= staticDay7Act.getParam().get(0)
                                && (staticDay7Act.getParam().get(1) == 0
                                || staticEquip.getEquipPart() == staticDay7Act.getParam().get(1))) {
                            lvMax++;
                        }
                    }
                }
                break;
            case ActivityConst.ACT_TASK_HERO://
                StaticHero staticHero = null;
                Iterator<Hero> it = player.heros.values().iterator();
                while (it.hasNext()) {
                    Hero hero = it.next();
                    staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                    if (staticDay7Act.getParam() == null || staticDay7Act.getParam().isEmpty()
                            || staticHero.getQuality() == staticDay7Act.getParam().get(0)) {
                        lvMax++;
                    }
                }
                break;
            case ActivityConst.ACT_TASK_COMBAT://
                if (player.lord.combatId >= staticDay7Act.getParam().get(0)) {
                    lvMax++;
                }
                break;
            case ActivityConst.ACT_TASK_HERO_WASH://
                if (CheckNull.isEmpty(staticDay7Act.getParam()))
                    break;
                it = player.heros.values().iterator();
                while (it.hasNext()) {
                    Hero hero = it.next();
                    if (CheckNull.isNull(hero))
                        continue;
                    staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                    if (CheckNull.isNull(staticHero))
                        continue;
                    StaticHeroUpgrade staticHeroUpgrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
                    if (CheckNull.isNull(staticHeroUpgrade))
                        continue;
                    if (staticDay7Act.getParam().size() == 1 && staticDay7Act.getParam().get(0) <= staticHeroUpgrade.getGrade()) {
                        lvMax++;
                    } else if (staticDay7Act.getParam().size() == 2 && staticDay7Act.getParam().get(0) <=
                            staticHeroUpgrade.getGrade() && staticDay7Act.getParam().get(1) == staticHero.getQuality()) {
                        lvMax++;
                    }
                }
                break;
            case ActivityConst.ACT_TASK_BUILDING://
                lvMax = BuildingDataManager.getBuildingTopLv(player, BuildingType.COMMAND);
                break;
            case ActivityConst.ACT_TASK_EQUIP_WASH://
                map = player.equips;
                if (map != null) {
                    StaticEquip staticEquip = null;
                    StaticEquipQualityExtra staticEquipQualityExtra = null;
                    Iterator<Equip> eit = map.values().iterator();
                    while (eit.hasNext()) {
                        Equip equip = eit.next();
                        staticEquip = StaticPropDataMgr.getEquip(equip.getEquipId());
                        if (staticEquip.getQuality() >= staticDay7Act.getParam().get(0)
                                && (staticDay7Act.getParam().get(1) == 0
                                || staticEquip.getEquipPart() == staticDay7Act.getParam().get(1))) {
                            staticEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                                    .get(staticEquip.getWashQuality());
                            if (equip.isAllLvMax(staticEquipQualityExtra.getMaxLv())) {
                                lvMax++;
                            }
                            // if (equip.getAttrLv() >= staticEquipQualityExtra.getMaxLv()) {
                            // lvMax++;
                            // }
                        }
                    }
                }
                break;
            case ActivityConst.ACT_TASK_EQUIP_BUILD:
            case ActivityConst.ACT_TASK_SUPER_EQUIP:
                if (player.supEquips != null) {
                    Iterator<SuperEquip> sit = player.supEquips.values().iterator();
                    while (sit.hasNext()) {
                        SuperEquip superEquip = sit.next();
                        if (staticDay7Act.getParam() != null && !staticDay7Act.getParam().isEmpty()) {
                            if (superEquip.getLv() < staticDay7Act.getParam().get(0)) {
                                continue;
                            }
                        }
                        lvMax++;
                    }
                }
                break;
            case ActivityConst.ACT_TASK_ATK:
            case ActivityConst.ACT_TASK_ATK_BANDIT:
            case ActivityConst.ACT_TASK_JOIN_OR_ATK:
            case ActivityConst.ACT_TASK_DAILY_TASK_CNT:
            case ActivityConst.ACT_TASK_TREASURE_OPEN_CNT:
            case ActivityConst.ACT_TASK_BATTLE_PASS_TASK_CNT:
            case ActivityConst.ACT_TASK_STONE_COMBAT_CNT:
            case ActivityConst.ACT_TASK_CHEMICAL_RECRUIT_CNT:
            case ActivityConst.ACT_TASK_CAMP_TASK_FINSH_CNT:
            case ActivityConst.ACT_TASK_EQUIP_WASH_CNT:
            case ActivityConst.ACT_TASK_MULTI_BANDIT_CNT:
                //  大于或者等于
                for (Entry<Integer, Integer> kv : player.day7Act.getTypeMap(staticDay7Act.getTaskType()).entrySet()) {
                    if (staticDay7Act.getParam() == null || staticDay7Act.getParam().isEmpty()
                            || kv.getKey() >= staticDay7Act.getParam().get(0)) {
                        lvMax += kv.getValue();
                    }
                }
                break;
            case ActivityConst.ACT_TASK_HERO_WASH_CNT:
                //  等于配置的param
                for (Entry<Integer, Integer> kv : player.day7Act.getTypeMap(staticDay7Act.getTaskType()).entrySet()) {
                    if (staticDay7Act.getParam() == null || staticDay7Act.getParam().isEmpty()
                            || kv.getKey().equals(staticDay7Act.getParam().get(0))) {
                        lvMax += kv.getValue();
                    }
                }
                break;
            case ActivityConst.ACT_TASK_CHARGE:
            case ActivityConst.ACT_TASK_VIP_BUY:
            case ActivityConst.ACT_TASK_ARM_TYPE_CNT:
                lvMax = player.day7Act.getStatus().get(staticDay7Act.getTaskType());
                break;
            case ActivityConst.ACT_TASK_MASTER:
                if (!CheckNull.isNull(player.master)) {
                    lvMax = 1;
                }
                break;
            case ActivityConst.ACT_TASK_APPRENTICES:
                lvMax = friendService.getApprenticeCnt(player);
                break;
            case ActivityConst.ACT_TASK_PROMOTE_LV:
                lvMax = player.lord.getRanks();
                break;
            case ActivityConst.ACT_TASK_STONE_CNT:
                // 未穿戴
                lvMax = player.getStoneInfo().getStones().entrySet()
                        .stream()
                        .filter(en -> {
                            int id = en.getKey();
                            StaticStone sStone = StaticPropDataMgr.getStoneMapById(id);
                            if (sStone == null) {
                                return false;
                            }
                            return sStone.getLv() == staticDay7Act.getParam().get(0);
                        }).mapToInt(en -> en.getValue().getCnt()).sum();
                // 已经穿戴
                lvMax += (int) player.getStoneInfo().getStoneHoles().values()
                        .stream()
                        .filter(en -> {
                            int stoneId = en.getStoneId();
                            StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
                            if (sStone == null) {
                                return false;
                            }
                            return sStone.getLv() == staticDay7Act.getParam().get(0);
                        }).count();
                break;
            case ActivityConst.ACT_TASK_HERO_QUALITY_UPGRADE_CNT:
                lvMax += (int) player.heros.values()
                        .stream()
                        .filter(hero -> {
                            if (!CheckNull.isEmpty(staticDay7Act.getParam())) {
                                int quality = staticDay7Act.getParam().get(0);
                                int level = staticDay7Act.getParam().get(1);
                                StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                                return sHero.getQuality() >= quality && hero.getLevel() >= level;
                            }
                            return false;
                        }).count();
                break;
            case ActivityConst.ACT_TASK_AGENT_STAR_CNT:
                Cia cia = player.getCia();
                if (Objects.nonNull(cia)) {
                    lvMax += (int) cia.getFemaleAngets().values()
                            .stream()
                            .filter(agent -> {
                                if (!CheckNull.isEmpty(staticDay7Act.getParam())) {
                                    int star = staticDay7Act.getParam().get(0);
                                    return agent.getStar() >= star;
                                }
                                return false;

                            }).count();
                }
                break;
            case ActivityConst.ACT_TASK_CUMULATIVE_RESIDENT_DRAW_CARD:
                DrawCardData drawCardData = player.getDrawCardData();
                if (Objects.nonNull(drawCardData)) {
                    lvMax += drawCardData.getTotalDrawCount() + player.getFunctionPlanData().
                            getExtData(PlanFunction.DRAW_CARD.getFunctionId());
                }
                break;
            default:
                break;
        }
        return lvMax != null ? lvMax : 0;
    }

    public void updDay7ActSchedule(Player player, int type, Object... params) {
        if (null == player) {
            return;
        }
        Day7Act day7Act = player.day7Act;
        // 加个临时变量-使判断速度加快
        if (day7Act.isExpired()) {
            return;
        }
        long time = TimeHelper.getDateZeroTime(player.account.getCreateDate()).getTime()
                + (7) * TimeHelper.DAY_S * 1000L;
        if (System.currentTimeMillis() > time) {
            day7Act.setExpired(true);
            return;
        }

        long status = 0;
        int param = 0;
        switch (type) {
            case ActivityConst.ACT_TASK_BUILDING:// 替换值
            case ActivityConst.ACT_TASK_FIGHT:
                param = Long.valueOf(params[0] + "").intValue();
                if (day7Act.getStatus().containsKey(type)) {
                    status = day7Act.getStatus().get(type);
                }
                if (param > status) {
                    day7Act.getStatus().put(type, param);
                }
                break;
            case ActivityConst.ACT_TASK_ATK:// 累计值
            case ActivityConst.ACT_TASK_ATK_BANDIT:
            case ActivityConst.ACT_TASK_JOIN_OR_ATK:
            case ActivityConst.ACT_TASK_DAILY_TASK_CNT:
            case ActivityConst.ACT_TASK_TREASURE_OPEN_CNT:
            case ActivityConst.ACT_TASK_BATTLE_PASS_TASK_CNT:
            case ActivityConst.ACT_TASK_STONE_COMBAT_CNT:
            case ActivityConst.ACT_TASK_STONE_CNT:
            case ActivityConst.ACT_TASK_HERO_WASH_CNT:
            case ActivityConst.ACT_TASK_CHEMICAL_RECRUIT_CNT:
            case ActivityConst.ACT_TASK_CAMP_TASK_FINSH_CNT:
            case ActivityConst.ACT_TASK_EQUIP_WASH_CNT:
            case ActivityConst.ACT_TASK_MULTI_BANDIT_CNT:
                int intKey = (Integer) params[0];
                Map<Integer, Integer> warMap = day7Act.getTypeMap(type);
                status = 1;
                if (warMap.containsKey(intKey)) {
                    status += warMap.get(intKey);
                }
                warMap.put(intKey, (int) status);
                break;
            case ActivityConst.ACT_TASK_RECRUIT:
            case ActivityConst.ACT_TASK_ATTACK:
            case ActivityConst.ACT_TASK_JOIN_ATK:
                StringBuilder sb = new StringBuilder(type + "");
                for (Object o : params) {
                    sb.append("_").append(o);
                }
                String key = sb.toString();
                status = 1;
                if (day7Act.getTankTypes().containsKey(key)) {
                    status += day7Act.getTankTypes().get(key);
                }
                day7Act.getTankTypes().put(key, (int) status);
                LogUtil.debug("参与攻下几座指定类型的城， 统计=" + day7Act.getTankTypes() + ",key=" + key);
                break;
            case ActivityConst.ACT_TASK_CHARGE:
            case ActivityConst.ACT_TASK_COST_GOLD:// 累计充值奖励
            case ActivityConst.ACT_TASK_VIP_BUY:
            case ActivityConst.ACT_TASK_ARM_TYPE_CNT:
                status = (Integer) params[0];
                if (day7Act.getStatus().containsKey(type)) {
                    status += day7Act.getStatus().get(type);
                }
                day7Act.getStatus().put(type, (int) status);
                break;
            default:
                // return;
        }

        int createServerId = player.account.getServerId();
        List<StaticDay7Act> staticDay7ActList = StaticActivityDataMgr.getAct7DayMap().values().stream().filter(sd7c -> sd7c.checkServerPlan(createServerId)).collect(Collectors.toList());
        if(staticDay7ActList.isEmpty()){
            return;
        }
//        List<StaticDay7Act> list = StaticActivityDataMgr.getDay7ActTypeList(type);
        List<StaticDay7Act> list = staticDay7ActList.stream().filter(o -> o.getTaskType() == type).collect(Collectors.toList());
        if (list == null) {
            return;
        }

        Date now = new Date();
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        int dayiy = DateHelper.dayiy(beginTime, now);

        int tips = 0;
        if (type != ActivityConst.ACT_TASK_CITY) {
            for (StaticDay7Act e : list) {
                if (day7Act.getCanRecvKeyId().contains(e.getKeyId())) {
                    continue;
                }
                if (day7Act.getRecvAwardIds().contains(e.getKeyId())) {
                    continue;
                }
                if (e.getDay() > dayiy) {
                    continue;
                }
                status = getDay7ActStatus(player, e);
                if (status >= e.getCond()) {
                    day7Act.getCanRecvKeyId().add(e.getKeyId());
                    tips++;
                }
            }
        } else {
            tips = 1;
        }
        if (tips > 0) {
            synDay7ActToPlayer(player);
        }
    }

    public void synDay7ActToPlayer(Player target) {
        // if (target != null && target.isLogin) {
        // SynDay7ActTipsRq.Builder builder = SynDay7ActTipsRq.newBuilder();
        //
        // Base.Builder msg = PbHelper.createSynBase(SynDay7ActTipsRq.EXT_FIELD_NUMBER,
        // SynDay7ActTipsRq.ext,
        // builder.build());
        // GameServer.getInstance().synMsgToPlayer(target.ctx, msg);
        // }

        syncActChange(target, ActivityConst.ACT_7DAY);
    }

    /**
     * 黑鹰计划刷新格子
     *
     * @param tokenCanBuy 是否可以购买信物,true可以购买
     * @return 返回null说明配置格式有错误, 返回格子数据
     */
    public Map<Integer, ActBlackhawkItem> blanckhawkRefresh(boolean tokenCanBuy) {
        Map<Integer, StaticActBlackhawk> blackhawkMap = StaticActivityDataMgr.getActBlackhawkMap();
        if (CheckNull.isEmpty(blackhawkMap)) {
            // 格式错误
            return null;
        }
        Map<Integer, ActBlackhawkItem> itemMap = new HashMap<>();

        for (StaticActBlackhawk sItem : blackhawkMap.values()) {
            // 权重随机
            List<Integer> award = RandomUtil.getRandomByWeight(sItem.getAwardList(), 3, false);
            if (CheckNull.isEmpty(award)) {
                // 格式错误
                return null;
            }
            ActBlackhawkItem item = new ActBlackhawkItem();
            item.setKeyId(sItem.getKeyId());
            item.setCond(sItem.getCond());
            item.setPrice(sItem.getPrice());
            item.setAward(award);
            item.setPurchased(false);
            if (6 == item.getCond()) {
                // 招募过将领 或 信物达到7个就不能购买信物
                item.setPurchased(!tokenCanBuy);
            }
            item.setDiscount(100);
            item.setDiscountPrice(sItem.getPrice());

            itemMap.put(item.getKeyId(), item);
        }

        return itemMap;
    }

    /**
     * 获取黑鹰计划信物的个数
     *
     * @param player
     * @return
     */
    public int getBlanckhawkTokenCount(Player player) {
        if (CheckNull.isNull(player)) {
            return 0;
        }
        ActBlackhawkItem item = player.blackhawkAct.getBlackhawkItemMap().get(ActParamConstant.BLACKHAWK_TOKEN_KEYID);
        if (CheckNull.isNull(item) || CheckNull.isEmpty(item.getAward()) || item.getAward().size() != 3) {
            return 0;
        }
        int tokenCount = (int) rewardDataManager.getRoleResByType(player, item.getAward().get(0),
                item.getAward().get(1));
        return tokenCount;

    }

    /**
     * 是否还有首充礼包
     *
     * @param player
     * @return
     */
    public void firstPayAward(Player player, int price) {
        Activity act = getActivityInfo(player, ActivityConst.ACT_FIRSH_CHARGE);
        if (act == null) {
            return;
        }
        Date now = new Date();
        long createTime = player.account.getCreateDate().getTime();
        long disPlayTime = createTime + (ActParamConstant.ACT_FIRSH_CHARGE_TIME * 1000L);
        if (now.getTime() <= disPlayTime) {
            List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(act.getActivityId());
            if (!CheckNull.isEmpty(awardList)) {
                StaticActAward saa = awardList.get(0);
                Long p = act.getStatusCnt().get(saa.getSortId());
                p = p == null ? 0L : p;
                // 首次充值
                if (p == 0 && price > 0 && player.getMixtureDataById(PlayerConstant.FB_END_TIME) == 0) {
                    int currentSecond = TimeHelper.getCurrentSecond();
                    int endTime = currentSecond + (4 * TimeHelper.HOUR_S);
                    if (endTime > currentSecond) {
                        player.setMixtureData(PlayerConstant.FB_END_TIME, endTime);
                        playerDataManager.syncMixtureData(player);
                    }
                }
                p += price;
                act.getStatusCnt().put(saa.getSortId(), p);
                getCurActTips(player, ActivityConst.ACT_FIRSH_CHARGE);
            }
            // if (!CheckNull.isEmpty(awardList)) {
            // StaticActAward saward = awardList.get(0);
            // mailDataManager.sendAttachMail(player,
            // PbHelper.createAwardsPb(saward.getAwardList()),
            // MailConstant.MOLD_FIRST_PAY_AWARD, AwardFrom.FIRST_PAY_AWARD,
            // TimeHelper.getCurrentSecond());
            // act.getStatusMap().put(saward.getKeyId(), 1);
            // }
        }

    }

    /**
     * 获取当前处于开启时间段内的活动配置
     *
     * @param activityId
     * @return
     */
    public static StaticActivityTime getCurActivityTime(int activityId) {
        List<StaticActivityTime> list = StaticActivityDataMgr.getActivityTimeById(activityId);
        if (!CheckNull.isEmpty(list)) {
            for (StaticActivityTime time : list) {
                if (time.isInThisTime()) {
                    return time;
                }
            }
        }
        return null;
    }

    /**
     * 获取某个活动可以领取的奖励的个数
     *
     * @param player
     * @param actType
     * @return
     */
    public int getCurActTips(Player player, int actType) {
        int currentNow = TimeHelper.getCurrentSecond();
        Date now = new Date();
        int tips = 0;
        if (actType == ActivityConst.ACT_COMMAND_LV || actType == ActivityConst.ACT_LEVEL
                || actType == ActivityConst.ACT_ALL_CHARGE || ActivityConst.ACT_GIFT_PAY == actType
                || actType == ActivityConst.ACT_COST_GOLD || actType == ActivityConst.ACT_FREE_LUXURY_GIFTS
                || actType == ActivityConst.ACT_FIRSH_CHARGE || actType == ActivityConst.ACT_WAR_ROAD
                || actType == ActivityConst.ACT_BIG_KILL || actType == ActivityConst.ACT_WAR_ROAD_DAILY
                || actType == ActivityConst.ACT_BUILD_GIFT || actType == ActivityConst.ACT_BRAVEST_ARMY
        ) { // 通用活动会走此处
            Activity activity = getActivityInfo(player, actType);
            if (activity == null) return tips;
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
            int activityKeyId = activityBase.getActivityId();
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            int schedule = 0;// 某类活动当前的进度值，或者排名活动的当前名次
            if (condList != null) {
                schedule = currentActivity(player, activity, 0, currentNow);
                for (StaticActAward e : condList) {
                    int keyId = e.getKeyId();
                    if (!activity.getStatusMap().containsKey(keyId) && schedule >= e.getCond()) {// 未领取奖励
                        if (actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY) {// 全军返利的 额外逻辑
                            // 玩家充值的金币总数
                            int topup = activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                                    : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD);
                            // 全军返利 个人需充值的金币数
                            int checkLordGlod = 0;
                            if (e.getParam() != null && e.getParam().size() > 0) {
                                checkLordGlod = e.getParam().get(0);
                            }
                            if (topup >= checkLordGlod) {
                                tips++;
                            }
                        } else {
                            tips++;
                        }
                    }
                }
            }
            if (actType == ActivityConst.ACT_LEVEL && null == activity.getStatusCnt().get(0)) {// 说明没有购买成长计划
                tips = 0;
            }
            if ((actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY)
                    && player.lord.getLevel() < ActParamConstant.ACT_ALL_CHARGE_LORD_LV) {// 等级未达到
                tips = 0;
            }
        } else if (actType == ActivityConst.ACT_7DAY) { // 七日活动
            tips = 1;
        } else if (actType == ActivityConst.ACT_FOOD) {// 能量赠送
            tips = 1;
        } else if (ActivityConst.ACT_LUCKY_TURNPLATE == actType) { // 幸运 转盘
            ActTurnplat activity = (ActTurnplat) getActivityInfo(player, actType);
            if (!CheckNull.isNull(activity)) {
                tips = activity.getRefreshCount(); // 免费次数
            }
        } else if (ActivityConst.FAMOUS_GENERAL_TURNPLATE == actType) {// 名将 转盘
            Activity activity = getActivityInfo(player, actType);
            tips = getExchangeActCnt(player, activity);
        } else if (ActivityConst.ACT_EQUIP_TURNPLATE == actType) { // 装备转盘
            EquipTurnplat activity = (EquipTurnplat) getActivityInfo(player, actType);
            if (!CheckNull.isNull(activity)) {
                tips = activity.getRefreshCount(); // 免费次数
            }
        } else if (StaticActivityDataMgr.isActTypeRank(actType)) {// 排行活动
            Activity activity = getActivityInfo(player, actType);
            ActivityBase actBase = StaticActivityDataMgr.getActivityByType(actType);
            if (activity != null && actBase != null) {
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
                if (!CheckNull.isEmpty(condList)) {
                    int schedule = getRankAwardSchedule(player, activity.getActivityType());
                    for (StaticActAward saa : condList) {
                        int keyId = saa.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule > 0 && schedule <= saa.getCond()
                                && now.getTime() >= actBase.getAwardBeginTime().getTime()) {// 未领取奖励
                            tips++;
                        }
                    }
                }
            }
        } else if (actType == ActivityConst.ACT_VIP
                || ActivityConst.ACT_ATTACK_CITY == actType
                || ActivityConst.ACT_CHALLENGE_COMBAT == actType
                || ActivityConst.ACT_TRAINED_SOLDIERS == actType
                || ActivityConst.ACT_TRAINED_SOLDIERS_DAILY == actType
                || ActivityConst.ACT_EQUIP_MATERIAL == actType
                || actType == ActivityConst.ACT_ELIMINATE_BANDIT) {
            Activity activity = getActivityInfo(player, actType);
            ActivityBase actBase = StaticActivityDataMgr.getActivityByType(actType);
            if (activity != null && actBase != null) {
                int activityKeyId = actBase.getActivityId();
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                if (!CheckNull.isEmpty(condList)) {
                    for (StaticActAward saa : condList) {
                        int schedule = currentActivity(player, activity, saa.getParam().isEmpty() ? 0 : saa.getParam().get(0), currentNow);
                        int keyId = saa.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= saa.getCond()) {// 未领取奖励
                            tips++;
                        }
                    }
                }
            }
            // 可领取清空
            if (ActivityConst.ACT_VIP == actType && player.lord.getLevel() < ActParamConstant.ACT_VIP_LORD_LV) {// 等级未达到
                tips = 0;
            }
        } else if (ActivityConst.ACT_DAILY_PAY == actType) { // 每日充值
            Activity activity = getActivityInfo(player, actType);
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
            if (!CheckNull.isNull(activity) && !CheckNull.isNull(activityBase)) {
                int activityKeyId = activityBase.getActivityId();
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                if (!CheckNull.isNull(condList)) {
                    int schedule = currentActivity(player, activity, 0);
                    for (StaticActAward sa : condList) {
                        int keyId = sa.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= sa.getCond()
                                && caluActCailyPayStatus(activityBase.getActivityId(), sa, activity) == 0) {// 未领取奖励
                            tips++;
                        } else if (sa.getParam().get(0) == 0 && !activity.getStatusMap().containsKey(keyId)) {
                            // 之前每日充值,免费领奖的特殊处理
                            tips++;
                        }
                    }
                }
            }
        } else if (ActivityConst.ACT_LOGIN_EVERYDAY == actType) {// 每日登陆
            Activity activity = getActivityInfo(player, actType);
            if (activity != null && activity.getStatusCnt().get(0) != null && activity.getStatusMap().isEmpty()) {
                tips++;
            }
        } else if (ActivityConst.ACT_ATTACK_CITY_NEW == actType) {// 新攻城掠地活动
            Activity activity = getActivityInfo(player, actType);
            if (!CheckNull.isNull(player.atkCityAct) && !CheckNull.isNull(activity)) {
                ActivityBase actBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                if (CheckNull.isNull(actBase)) {
                    return tips;
                }
                final int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // 活动开启的第几天
                List<StaticAtkCityAct> actList = StaticActivityDataMgr.getAtkCityActList();
                // 进度红点
                if (!CheckNull.isEmpty(actList)) {
                    for (StaticAtkCityAct staticAtkCityAct : actList) {
                        int canRecvCnt = getCanRecvCnt(player, staticAtkCityAct, player.atkCityAct);
                        if (canRecvCnt > 0 && staticAtkCityAct.getDay() <= dayiy) {
                            tips++;
                        }
                    }
                }
                // 活跃度红点
                int schedule = currentActivity(player, activity, 0);
                if (schedule > 0) {
                    List<StaticActAward> awards = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
                    for (StaticActAward award : awards) {
                        int keyId = award.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= award.getCond()) {
                            tips++;
                        }
                    }
                }
                if (ActivityConst.ACT_ATTACK_CITY_NEW == actType
                        && player.lord.getLevel() < ActParamConstant.ACT_ATK_CITY_LEVEL.get(0)) {
                    tips = 0; // 可领取清空
                }
            }
        } else if (ActivityConst.ACT_PAY_7DAY == actType) { // 七日充值
            int cRoleDay = playerDataManager.getCreateRoleDay(player, now);
            if (cRoleDay > 7) return tips; // 过期
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                int activityKeyId = activityBase.getActivityId();
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                if (condList != null) {
                    int schedule = currentActivity(player, activity, 0, currentNow);
                    for (StaticActAward e : condList) {
                        int keyId = e.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= e.getCond()) {// 未领取奖励
                            tips++;
                        }
                    }
                }
            }
        } else if (ActivityConst.ACT_CHARGE_TOTAL == actType) { // 累计充值
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                if (activityBase.getStep() == ActivityConst.OPEN_CLOSE) {
                    return tips;// 未开放
                }
                int activityKeyId = activityBase.getActivityId();
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                if (condList != null) {
                    int schedule = currentActivity(player, activity, 0, currentNow);
                    for (StaticActAward e : condList) {
                        int keyId = e.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= e.getCond()) {// 未领取奖励
                            tips++;
                        }
                    }
                }
            }
        } else if (ActivityConst.ACT_PROP_PROMOTION == actType) { // 军备促销
            Activity activity = getActivityInfo(player, actType);
            GlobalActivityData activityData = getGlobalActivity(actType);
            if (!CheckNull.isNull(activity) && !CheckNull.isNull(activityData)) {
                StaticActAward sAward = StaticActivityDataMgr.getActAwardById(activity.getActivityId()).get(0);
                if (!CheckNull.isNull(sAward)) {
                    // int keyId = sAward.getKeyId();
                    // 可领取次数
                    tips = getAwardCnt(sAward, activity, activityData.getCampValByCamp(player.lord.getCamp()));
                }
            }
        } else if (ActivityConst.ACT_PAY_TURNPLATE == actType) {// 充值转盘
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                tips = getPayTurnplateCnt(player, activity);
            }
        } else if (ActivityConst.ACT_LUCKY_POOL == actType) {// 充值转盘
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                Map<Integer, Integer> tempMap = activity.getSaveMap();
                tips = tempMap.getOrDefault(0, 0) + tempMap.getOrDefault(1, 0) - tempMap.getOrDefault(2, 0)
                        - tempMap.getOrDefault(3, 0);
            }
        } else if (ActivityConst.ACT_ORE_TURNPLATE == actType) {// 矿石转盘
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                tips = activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                        : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM);
            }
        } else if (ActivityConst.ACT_DAY_DISCOUNTS == actType) {// 每日特惠
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                int freeKey = 0;
                tips = activity.getStatusMap().containsKey(freeKey) ? 0 : 1;
            }
        } else if (ActivityConst.ACT_MONOPOLY == actType) { // 大富翁
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                tips = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY, 0L)
                        .intValue();
            }
        } else if (ActivityConst.ACT_THREE_REBATE == actType) {// 三倍返利
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                if (!getIsGet(player, activity)) {
                    tips = 1;
                }
            }
        }
        // 战机抽取活动
        else if (ActivityConst.ACT_WAR_PLANE_SEARCH == actType) {
            Activity activity = getActivityInfo(player, actType);
            if (activity != null) {
                ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                if (activityBase.getStep() == ActivityConst.OPEN_CLOSE) {
                    return tips;// 未开放
                }
                int activityKeyId = activityBase.getActivityId();
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                if (condList != null) {
                    for (StaticActAward saa : condList) {
                        int schedule = getWarPlaneSearchSchedule(saa, player, activity);
                        if (!activity.getStatusMap().containsKey(saa.getKeyId()) && schedule >= saa.getCond()) {// 未领取奖励
                            tips++;
                        }
                    }
                }

            }
        } else if (ActivityConst.ACT_COLLECT_RESOURCES == actType || ActivityConst.ACT_RESOUCE_SUB == actType) {
            Activity activity = getActivityInfo(player, actType);
            ActivityBase actBase = StaticActivityDataMgr.getActivityByType(actType);
            if (activity != null && actBase != null) {
                int activityKeyId = actBase.getActivityId();
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                if (!CheckNull.isEmpty(condList)) {
                    for (StaticActAward saa : condList) {
                        int key = saa.getParam().isEmpty() ? 0 : saa.getParam().get(0) * 10000 + saa.getParam().get(1);
                        int schedule = currentActivity(player, activity, key, currentNow);
                        int keyId = saa.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= saa.getCond()) {// 未领取奖励
                            tips++;
                        }
                    }
                }
            }
        } else if (ActivityConst.ACT_EASTER == actType) {
            // 复活节活动
            Activity activity = getActivityInfo(player, actType);
            ActivityBase actBase = StaticActivityDataMgr.getActivityByType(actType);
            if (activity != null && actBase != null) {
                int activityKeyId = actBase.getActivityId();
                // 还剩多少进度
                int schedule = currentActivity(player, activity, 0, currentNow);
                List<StaticEasterAward> easterAwardList = StaticActivityDataMgr.getEasterAwardList(activityKeyId);
                if (!CheckNull.isEmpty(easterAwardList)) {
                    // 根据需要的进度和keyId排序
                    easterAwardList = easterAwardList.stream().sorted(Comparator.comparingInt(StaticEasterAward::getRecharge).thenComparingInt(StaticEasterAward::getKeyId)).collect(Collectors.toList());
                    for (StaticEasterAward sea : easterAwardList) {
                        int recharge = sea.getRecharge();
                        int keyId = sea.getKeyId();
                        if (!activity.getStatusMap().containsKey(keyId) && schedule >= recharge) {
                            tips++;
                            // 减去进度
                            schedule -= recharge;
                        }
                    }
                }
            }
        } else if (ActivityConst.ACT_HOT_PRODUCT == actType) {
            tips = activityHotProductService.getCurActTips(player);
        } else if (ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE == actType) {
            Activity activity = getActivityInfo(player, actType);
            Long state = activity.getStatusCnt().getOrDefault(0, 0L);
            if (state > 0) {
                tips = 1;
            }
        } else if (ActivityConst.ACT_VISIT_ALTAR == actType) {
            // 只要变动就推送
            tips = 1;
        }
        return tips;
    }

    /**
     * 战机开发活动
     *
     * @param saa
     * @param player
     * @param activity
     * @return
     */
    public int getWarPlaneSearchSchedule(StaticActAward saa, Player player, Activity activity) {
        int schedule = 0;
        List<Integer> param = saa.getParam();
        if (CheckNull.isEmpty(param)) {
            return schedule;
        }
        int temp = param.get(1);
        int keyId = saa.getKeyId();

        if (temp == ActivityConst.ACT_WAR_PLANE_TYPE_1) {
            schedule = activity.getStatusCnt().getOrDefault(0, 0L).intValue();
        } else if (temp == ActivityConst.ACT_WAR_PLANE_TYPE_2) {
            int quality = param.get(2);
            schedule = activity.getStatusCnt().getOrDefault(quality, 0L).intValue();
        } else if (temp == ActivityConst.ACT_WAR_PLANE_TYPE_3) {
            int quality = param.get(2);
            schedule = (int) player.warPlanes.values().stream()
                    .map(plane -> StaticWarPlaneDataMgr.getPlaneInitByType(plane.getType()))
                    .filter(sp -> sp.getQuality() == quality).count();
        }
        return schedule;
    }

    /**
     * 获取充值转盘剩余次数
     *
     * @param activity
     * @return
     */
    public int getPayTurnplateCnt(Player p, Activity activity) {
        Long cnt = activity.getStatusCnt().get(0);
        int payGold = cnt == null ? 0 : cnt.intValue();
        int allCnt = calcPayTurnplateCntByPayGold(payGold, activity.getActivityId());
        int gainCnt = activity.getStatusMap().size();
        int hasCnt = allCnt - gainCnt;
        hasCnt = hasCnt < 0 ? 0 : hasCnt;
        LogUtil.debug("获取充值转盘剩余次数  roleId:", p.roleId, ", allCnt:", allCnt, ", gainCnt:", gainCnt);
        return hasCnt;
    }

    /**
     * 根据 活动id 和 充值金额 获得充值转盘抽奖次数
     *
     * @param payGold
     */
    private static int calcPayTurnplateCntByPayGold(int payGold, int activityId) {
        // List<Integer> goldCntList = ActParamConstant.ACT_PAY_TURNPLATE_PAY_GOLD;
        int cnt = 0;
        List<Integer> list = new ArrayList<Integer>();
        for (List<Integer> l : ActParamConstant.ACT_PAY_TURNPLATE_PAY_GOLD) {
            if (l != null && l.size() > 0 && l.get(0) == activityId) {
                list = l;
                break;
            }
        }
        for (int gold : list) {
            if (gold == activityId) {
                continue;
            }
            if (payGold >= gold) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * 全服型活动推送
     */
    public void syncAllPlayerActChange(Player player, int actType) {
        playerDataManager.getPlayers().values().stream().filter(p -> p.isLogin && p.ctx != null).forEach(p -> {
            syncActChange(p, actType);
        });
    }

    /**
     * 推送满足条件可领取的奖励
     *
     * @param player
     * @param actType
     */
    public void syncActChange(Player player, int actType) {
        if (player == null) {
            return;
        }

        int tips = getCurActTips(player, actType);
        switch (actType) {
            case ActivityConst.ACT_LUCKY_POOL:
                SyncLuckyPoolChangeRs.Builder builder = SyncLuckyPoolChangeRs.newBuilder();
                builder.setPoolCnt(getGlobalActivity(actType).getGoal());
                Base.Builder msg = PbHelper.createRsBase(SyncLuckyPoolChangeRs.EXT_FIELD_NUMBER,
                        SyncLuckyPoolChangeRs.ext, builder.build());
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
                break;
            case ActivityConst.FAMOUS_GENERAL_TURNPLATE:
                syncActChange(player, actType, tips);
                break;
            default:
                if (tips > 0) {
                    syncActChange(player, actType, tips);
                }
                break;
        }
    }

    /**
     * 推送满足条件可领取的奖励
     *
     * @param player
     * @param actType
     * @param tips
     */
    public void syncActChange(Player player, int actType, int tips) {
        if (player != null && player.isLogin && player.ctx != null) {
            SyncActChangeRs.Builder actBuild = SyncActChangeRs.newBuilder();
            actBuild.addAct(PbHelper.createTwoIntPb(actType, tips));
            Base.Builder builder = PbHelper.createSynBase(SyncActChangeRs.EXT_FIELD_NUMBER, SyncActChangeRs.ext,
                    actBuild.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
        }
    }

    /**
     * 通过当前奖励获取连续充值奖励的充值数额
     *
     * @param player
     * @param activity
     * @param actAward
     * @param sortId
     * @param now
     * @return
     */
    public int getChargeContinueStatValueByAward(Player player, Activity activity, StaticActAward actAward, int sortId,
                                                 int now) {
        Integer day = actAward.getParam().get(0);
        // 当前天数已充值的钱
        Integer val = activity.getSaveMap().get(day);
        return val == null ? 0 : val;
    }

    /**
     * * 此类活动最新状态值，或者我的排名
     *
     * @param player
     * @param activity
     * @param sortId
     * @return
     */
    public int currentActivity(Player player, Activity activity, int sortId, int now) {
        if (activity.getActivityType() == ActivityConst.ACT_LEVEL) { // 等级
            return player.lord.getLevel();
        } else if (activity.getActivityType() == ActivityConst.ACT_COMMAND_LV) { // 基地升级（原主城升级） 司令部升级
            return BuildingDataManager.getBuildingTopLv(player, BuildingType.COMMAND);
        } else if (activity.getActivityType() == ActivityConst.ACT_VIP) { // 全服VIP达到指定数量
            return globalDataManager.getTotalVIPCnt(sortId);
        } else if (activity.getActivityType() == ActivityConst.ACT_ALL_CHARGE) { // 全服充值
            GlobalActivityData globalActivityData = getGlobalActivity(activity.getActivityType());
            if (globalActivityData != null) {
                return globalActivityData.getTopupa().intValue() + globalActivityData.getTopupb().intValue()
                        + globalActivityData.getTopupc().intValue();
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_BRAVEST_ARMY) {
            // 勇冠三军
            GlobalActivityData activityData = getGlobalActivity(activity.getActivityType());
            if (activityData != null) {
                HashMap<Integer, Long> campVal = new HashMap<>();
                campVal.put(Constant.Camp.EMPIRE, activityData.getTopupa().get());
                campVal.put(Constant.Camp.ALLIED, activityData.getTopupb().get());
                campVal.put(Constant.Camp.UNION, activityData.getTopupc().get());
                int camp = player.lord.getCamp();
                return campVal.getOrDefault(camp, 0L).intValue();
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_ATK_GESTAPO
                || activity.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {// 盖世太保 或 名将转盘
            List<StaticActExchange> staticActExchanges = StaticActivityDataMgr
                    .getActExchangeListById(activity.getActivityId());
            if (CheckNull.isEmpty(staticActExchanges)) return 0;
            List<List<Integer>> expendProps = staticActExchanges.get(0).getExpendProp();
            if (CheckNull.isEmpty(expendProps)) return 0;
            List<Integer> prop = expendProps.get(0);
            if (CheckNull.isNull(prop)) return 0;
            Long cnt = new Long(rewardDataManager.getRoleResByType(player, prop.get(0), prop.get(1)));
            return cnt.intValue();
        } else if (StaticActivityDataMgr.isActTypeRank(activity.getActivityType())) { // 排行榜相关的
            GlobalActivityData gActDate = getGlobalActivity(activity.getActivityType());
            // 此处返回的是排行榜的，名次
            ActRank rank = gActDate.getPlayerRank(player, activity.getActivityType(), player.roleId);
            if (rank == null) {
                return 0;
            } else {
                return rank.getRank();
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_GIFT_PAY
                || activity.getActivityType() == ActivityConst.ACT_COST_GOLD
                || activity.getActivityType() == ActivityConst.ACT_ATTACK_CITY
                || activity.getActivityType() == ActivityConst.ACT_DAILY_PAY
                || activity.getActivityType() == ActivityConst.ACT_ATTACK_CITY_NEW
                || activity.getActivityType() == ActivityConst.ACT_PAY_7DAY
                || activity.getActivityType() == ActivityConst.ACT_CHARGE_TOTAL
                || activity.getActivityType() == ActivityConst.ACT_PROP_PROMOTION
                || activity.getActivityType() == ActivityConst.ACT_FIRSH_CHARGE
                || activity.getActivityType() == ActivityConst.ACT_WAR_ROAD
                || activity.getActivityType() == ActivityConst.ACT_WAR_ROAD_DAILY
                || activity.getActivityType() == ActivityConst.ACT_BIG_KILL
                || activity.getActivityType() == ActivityConst.ACT_BUILD_GIFT
                || activity.getActivityType() == ActivityConst.ACT_HOT_PRODUCT
                || activity.getActivityType() == ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE
            /* || activity.getActivityType() == ActivityConst.ACT_ONLINE_GIFT */) {// 充值有礼 消费有礼 攻占据点 每日充值 战火试炼
            Long val = activity.getStatusCnt().get(sortId);
            return val == null ? 0 : val.intValue();
        } else if (activity.getActivityType() == ActivityConst.ACT_GIFT_OL) {// 在线领奖
            return player.onLineTime();
        } else if (activity.getActivityType() == ActivityConst.ACT_ONLINE_GIFT) {
            /*
             * if (CheckNull.isEmpty(activity.getStatusCnt())) { updActivity(player,
             * ActivityConst.ACT_ONLINE_GIFT, now, 0); activity.getPropMap().put(0,
             * player.lord.getLevel()); }
             */
            Long lastAwardTime = activity.getStatusCnt().get(sortId);// 在线奖励,每次领奖后重新计时
            // 转点清理
            if (lastAwardTime == null) {
                int currentDay = TimeHelper.getDay(now);
                int lastDay = TimeHelper.getDay(player.lord.getOnTime());
                if (currentDay != lastDay) {// 登录时间不为当天,则取0点到当前时间
                    int noTime = TimeHelper.getTodayZone(now);
                    updActivity(player, ActivityConst.ACT_ONLINE_GIFT, new Long(noTime), 0, true);
                    activity.getPropMap().put(0, player.lord.getLevel());
                    lastAwardTime = activity.getStatusCnt().get(sortId);
                } else {
                    lastAwardTime = Long.valueOf(now);
                    updActivity(player, ActivityConst.ACT_ONLINE_GIFT, lastAwardTime, 0, true);
                    activity.getPropMap().put(0, player.lord.getLevel());
                }
            }
            return lastAwardTime == null ? 0 : now - lastAwardTime.intValue();
        } else if (activity.getActivityType() == ActivityConst.ACT_FREE_LUXURY_GIFTS) {// 免费豪礼
            Date beginTime = player.account.getCreateDate();
            int dayiy = DateHelper.dayiy(beginTime, new Date());// 创建角色的第几天
            return dayiy;
        } else if (activity.getActivityType() == ActivityConst.ACT_LOGIN_EVERYDAY) {// 登陆奖励
            return activity.getStatusCnt().isEmpty() ? 0 : 1;
        } else if (activity.getActivityType() == ActivityConst.ACT_REAL_NAME
                || activity.getActivityType() == ActivityConst.ACT_PHONE_BINDING) {// 实名认证活动,手机绑定
            return 1;
        } else if (activity.getActivityType() == ActivityConst.ACT_DAY_DISCOUNTS) {// 每日特惠免费的奖励永远是1
            return 1;
        } else if (activity.getActivityType() == ActivityConst.ACT_THREE_REBATE) { // 三倍返利，拥有橙色战机和已充值即可领取礼包
            if (getIsHave(player) && getIsPay(player, activity)) {
                return 1;
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_CHALLENGE_COMBAT
                || activity.getActivityType() == ActivityConst.ACT_EQUIP_MATERIAL
                || activity.getActivityType() == ActivityConst.ACT_ELIMINATE_BANDIT
        ) { // 挑战战役 装备物资
            return (int) activity.getStatusCnt().entrySet().stream()
                    .filter(en -> {
                        // 这里的key记录的是副本id, 过滤大于要求的副本id
                        if (en.getKey() >= sortId) {
                            return true;
                        }
                        return false;
                    }).mapToLong(Entry::getValue).sum();
        } else if (activity.getActivityType() == ActivityConst.ACT_TRAINED_SOLDIERS
                || activity.getActivityType() == ActivityConst.ACT_TRAINED_SOLDIERS_DAILY
                || activity.getActivityType() == ActivityConst.ACT_COLLECT_RESOURCES
                || activity.getActivityType() == ActivityConst.ACT_RESOUCE_SUB) {
            return (int) activity.getStatusCnt().entrySet().stream()
                    .filter(en -> {
                        // 这里的key记录的兵种类型, 如果为0就是不限制兵种
                        if (sortId == 0) {
                            return true;
                        }
                        if (sortId == en.getKey()) {
                            return true;
                        }
                        return false;
                    }).mapToLong(Entry::getValue).sum();
        } else if (activity.getActivityType() == ActivityConst.ACT_EASTER) {
            List<StaticEasterAward> easterAwards = StaticActivityDataMgr.getEasterAwardList(activity.getActivityId());
            if (!CheckNull.isEmpty(easterAwards)) {
                StaticEasterAward sEasterAward = easterAwards.get(0);
                // 活动开启的时候, 免费给一个砸蛋的机会
                if (activity.getStatusCnt().getOrDefault(999, 0L) == 0L) {
                    updActivity(player, activity.getActivityType(), sEasterAward.getRecharge(), 0, false);
                    // 这里是为了记录免费的砸蛋
                    updActivity(player, activity.getActivityType(), sEasterAward.getRecharge(), 999, false);
                }
            }
            // 进度
            Long val = activity.getStatusCnt().getOrDefault(sortId, 0L);
            // 已经使用的进度
            Long useVal = activity.getStatusCnt().getOrDefault(1, 0L);
            return (int) (val - useVal);
        } else if (activity.getActivityType() == ActivityConst.ACT_GOOD_LUCK) {
            if (sortId == 0) {
                // 索引0存储的是randomType, 对应s_act_award的taskType
                return Math.toIntExact(activity.getStatusCnt().computeIfAbsent(sortId, (v) -> (long) activityLotteryService.randomGoodLuckType(player, activity)));
            }
            return Math.toIntExact(activity.getStatusCnt().getOrDefault(sortId, 0L));
        } else {
            return Math.toIntExact(activity.getStatusCnt().getOrDefault(sortId, 0L));
        }
        return 0;
    }

    /**
     * * 此类活动最新状态值，或者我的排名
     *
     * @param player
     * @param activity
     * @param sortId
     * @return
     */
    public int currentActivity(Player player, Activity activity, int sortId) {
        return currentActivity(player, activity, sortId, TimeHelper.getCurrentSecond());
    }

    /**
     * 每日充值获取当前的状态,
     *
     * @param actId
     * @param curSaa
     * @param act
     * @return 0未领 1已领 2未解锁
     */
    public int caluActCailyPayStatus(int actId, StaticActAward curSaa, Activity act) {
        if (act.getStatusMap().containsKey(curSaa.getKeyId())) {// 已领取
            return 1;
        } else {// 未领取,
            List<StaticActAward> collect = StaticActivityDataMgr.getDailyPayAward(actId);
            final int preParam = curSaa.getParam().get(0) - 1;
            StaticActAward preParamSaa = collect.stream().filter(ssa -> ssa.getParam().get(0) == preParam)// 上一个奖励
                    .findFirst().orElse(null);
            if (preParamSaa == null) {// 没找到说明是第一个
                return 0;
            } else {
                if (!act.getStatusMap().containsKey(preParamSaa.getKeyId())) {// 上级没领取百分百没当前为解锁
                    return 2;
                } else {// 上级已经领取
                    Integer preDay = act.getStatusMap().get(preParamSaa.getKeyId());
                    int currentDay = TimeHelper.getCurrentDay();
                    if (preDay == currentDay) {// 上一级今天领取过
                        return 2;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    /**
     * 此类活动过期状态值，或者我的排名
     *
     * @param player
     * @param activity
     * @param sortId
     * @return
     */
    public int exceedActivity(Player player, Activity activity, int sortId) {
        if (activity.getActivityType() == ActivityConst.ACT_LEVEL) { // 等级
            return player.lord.getLevel();
        } else if (activity.getActivityType() == ActivityConst.ACT_COMMAND_LV) { // 基地升级（原主城升级） 司令部升级
            return BuildingDataManager.getBuildingTopLv(player, BuildingType.COMMAND);
        } else if (activity.getActivityType() == ActivityConst.ACT_VIP) { // 全服VIP达到指定数量
            return globalDataManager.getTotalVIPCnt(sortId);
        } else if (activity.getActivityType() == ActivityConst.ACT_ALL_CHARGE) { // 全服充值
            GlobalActivityData globalActivityData = activityMap.get(activity.getActivityType());
            if (globalActivityData != null) {
                return globalActivityData.getTopupa().intValue() + globalActivityData.getTopupb().intValue()
                        + globalActivityData.getTopupc().intValue();
            }
        } else if (StaticActivityDataMgr.isActTypeRank(activity.getActivityType())) { // 排行榜相关的
            GlobalActivityData gActDate = getGlobalActivity(activity.getActivityType());
            // 此处返回的是排行榜的，名次
            ActRank rank = gActDate.getPlayerRank(player, activity.getActivityType(), player.roleId);
            if (rank == null) {
                return 0;
            } else {
                return rank.getRank();
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_GIFT_PAY
                || activity.getActivityType() == ActivityConst.ACT_COST_GOLD
                || activity.getActivityType() == ActivityConst.ACT_PAY_7DAY
                || activity.getActivityType() == ActivityConst.ACT_CHARGE_TOTAL
                || activity.getActivityType() == ActivityConst.ACT_WAR_ROAD
                || activity.getActivityType() == ActivityConst.ACT_WAR_ROAD_DAILY
        ) {// 充值有礼 消费有礼 战火试炼
            Long val = activity.getStatusCnt().get(0);
            return val == null ? 0 : val.intValue();
        } else if (activity.getActivityType() == ActivityConst.ACT_BRAVEST_ARMY) {
            // 勇冠三军
            GlobalActivityData activityData = activityMap.get(activity.getActivityType());
            if (activityData != null) {
                HashMap<Integer, Long> campVal = new HashMap<>();
                campVal.put(Constant.Camp.EMPIRE, activityData.getTopupa().get());
                campVal.put(Constant.Camp.ALLIED, activityData.getTopupb().get());
                campVal.put(Constant.Camp.UNION, activityData.getTopupc().get());
                int camp = player.lord.getCamp();
                return campVal.getOrDefault(camp, 0L).intValue();
            }
        }
        return 0;
    }

    /**
     * 获取翻倍奖励获得的倍数
     *
     * @param player
     * @return 倍数
     */
    public int getActDoubleNum(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_DOUBLE_REWARD);
        if (activity == null) { // 获得未开启,不翻倍
            return 1;
        } else {
            if (ActParamConstant.ACT_DOUBLE_NUM < 2) {// 数据配错了,不翻倍
                return 1;
            }
            return ActParamConstant.ACT_DOUBLE_NUM;
        }
    }

    /**
     * 获取副本活动翻倍奖励获得的倍数
     *
     * @param player
     * @return 倍数
     */
    public int getActCombatDoubleNum(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_COMBAT_DOUBLE_REWARD);
        if (activity == null) { // 获得未开启,不翻倍
            return 1;
        } else {
            if (ActParamConstant.ACT_COMBAT_DOUBLE_NUM < 2) {// 数据配错了,不翻倍
                return 1;
            }
            return ActParamConstant.ACT_COMBAT_DOUBLE_NUM;
        }
    }

    /**
     * 获取匪军图纸翻倍
     *
     * @param player
     * @return
     */
    public int getActBanditDrawing(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_BANDIT_DRAWING);
        if (activity == null || activity.getOpen() != ActivityConst.OPEN_STEP) { // 获得未开启,不翻倍
            return 1;
        } else {
            if (ActParamConstant.ACT_BANDIT_DRAWING < 2) {// 数据配错了,不翻倍
                return 1;
            }
            return ActParamConstant.ACT_BANDIT_DRAWING;
        }
    }

    /**
     * 匪军迁城翻倍
     *
     * @param player
     * @return
     */
    public int getActBanditMove(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_BANDIT_MOVE);
        if (activity == null) { // 获得未开启,不翻倍
            return 1;
        } else {
            if (ActParamConstant.ACT_BANDIT_MOVE < 2) {// 数据配错了,不翻倍
                return 1;
            }
            return ActParamConstant.ACT_BANDIT_MOVE;
        }
    }

    /**
     * 匪军资源翻倍
     *
     * @param player
     * @return
     */
    public double getActBanditRes(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_BANDIT_RES);
        if (activity == null) { // 获得未开启,不翻倍
            return 1.0;
        } else {
            if (ActParamConstant.ACT_BANDIT_RES < Constant.TEN_THROUSAND) {// 数据配错了,不翻倍
                return 1.0;
            }
            return ActParamConstant.ACT_BANDIT_RES / Constant.TEN_THROUSAND;
        }
    }

    /**
     * 图纸活动
     *
     * @return
     */
    public int getActCityDrawing() {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_CITY_DRAWING);
        if (activityBase == null) {
            return 1;
        } else {
            if (ActParamConstant.ACT_CITY_DRAWING < 2) {// 数据配错了,不翻倍
                return 1;
            }
            return ActParamConstant.ACT_CITY_DRAWING;
        }
    }

    /**
     * 获取生产加速的比例
     *
     * @param player
     * @return
     */
    public int getActProductionNum(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_PRODUCTTION_EXPEDITE);
        if (activity == null) { // 活动未开启,生产不加速
            return 10000;
        } else {
            if (ActParamConstant.ACT_PRODUCTTION_NUM > 10000 || ActParamConstant.ACT_PRODUCTTION_NUM <= 0) {
                return 10000;
            } else {
                return ActParamConstant.ACT_PRODUCTTION_NUM;
            }
        }
    }

    /**
     * 获取副本掉落的道具
     *
     * @param cnt
     * @return
     */
    public List<List<Integer>> getActCombatDrop(Player player, int cnt) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_COMBAT_DROP);
        if (activity == null) {
            return null;
        } else {
            List<List<Integer>> awardList = new ArrayList<>();
            for (int i = 0; i < cnt; i++) {
                for (List<Integer> a : ActParamConstant.ACT_COMBAT_DROP) {
                    if (a.size() == 4 && RandomHelper.isHitRangeIn10000(a.get(3))) {
                        List<Integer> award = new ArrayList<>();
                        award.add(a.get(0));
                        award.add(a.get(1));
                        award.add(a.get(2));
                        awardList.add(award);
                    }
                }
            }
            return RewardDataManager.mergeAward(awardList);
        }
    }

    /**
     * 更新匪军加速活动
     *
     * @param player
     */
    public List<Award> upActBanditAcce(Player player) {
        List<Integer> award = getActBanditAcce(player);
        if (!CheckNull.isEmpty(award)) {
            int id = award.get(1);
            // 有可加速的队列才会发奖励
            boolean canSend = ((id == AwardType.Special.BUILD_SPEED && player.getCanAddSpeedBuildQue() != null)
                    || (id == AwardType.Special.ARM_SPEED && player.getCanAddSpeedArmQue() != null)
                    || (id == AwardType.Special.TECH_SPEED && player.getCanSpeedTechQue() != null));
            if (canSend) {
                List<List<Integer>> awards = new ArrayList<>();
                awards.add(award);
                return rewardDataManager.sendReward(player, awards, AwardFrom.ACT_BANDIT_ACCE);// "匪军加速"
            }
        }
        return null;
    }

    /**
     * 获取匪军加速
     *
     * @param player
     * @return 返回 null 说明没有加速随机到加速内容或活动未开启
     */
    private List<Integer> getActBanditAcce(Player player) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_BANDIT_ACCE);
        if (activity == null) return null;
        if (!CheckNull.isEmpty(ActParamConstant.ACT_BANDIT_ACCE)) {
            List<Integer> award = RandomUtil.getRandomByWeightAndRatio(ActParamConstant.ACT_BANDIT_ACCE, 3, false,
                    (int) Constant.TEN_THROUSAND);
            return award;
        }
        return null;
    }

    /**
     * 获取采集翻倍的比例
     *
     * @return
     */
    public int getActCollectNum() {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_COLLECT_REWARD);
        if (activityBase == null) {
            return 0;
        } else {
            if (ActParamConstant.ACT_COLLECT_REWARD > 10000 || ActivityConst.ACT_COLLECT_REWARD <= 0) {// 数据配错了,不翻倍
                return 0;
            }
            return ActParamConstant.ACT_COLLECT_REWARD;
        }
    }

    /**
     * 获取募兵翻倍的比例
     *
     * @return
     */
    public int getActRecruitNum() {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_RECRUIT_REWARD);
        if (activityBase == null) {
            return 0;
        } else {
            if (ActParamConstant.ACT_RECRUIT_REWARD > 10000 || ActivityConst.ACT_RECRUIT_REWARD <= 0) {// 数据配错了,不翻倍
                return 0;
            }
            return ActParamConstant.ACT_RECRUIT_REWARD;
        }
    }

    /**
     * 获取击杀掉落活动奖励
     *
     * @param player 玩家对象
     * @param lv     等级
     * @param type
     * @return {@link StaticActBandit#ACT_HIT_DROP_TYPE_1}
     * {@link StaticActBandit#ACT_HIT_DROP_TYPE_2}
     */
    public List<Award> getActHitDrop(Player player, int lv, int type) {
        List<StaticActBandit> actBanditList = StaticActivityDataMgr.getActBanditList(type);
        List<Award> reward = new ArrayList<>();
        if (!CheckNull.isEmpty(actBanditList)) {
            for (StaticActBandit sActBandit : actBanditList) {
                int activityType = sActBandit.getActivityType();
                Activity activity = getActivityInfo(player, activityType);
                if (activity == null) {
                    continue;
                }
                ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
                if (CheckNull.isNull(activityBase)) {
                    continue;
                }
                int open = activityBase.getBaseOpen();
                if (open != ActivityConst.OPEN_STEP) {
                    continue;
                }
                // 判断活动id
                if (activity.getActivityId() != sActBandit.getActivityId()) {
                    continue;
                }
                List<List<Integer>> drop = sActBandit.getDrop();
                if (CheckNull.isEmpty(drop)) {
                    continue;
                }
                if (!checkActHitAwardCnt(player, activityType, sActBandit.getTotal())) {
                    continue;
                }
                List<List<Integer>> awards = new ArrayList<>(2);
                if (type == StaticActBandit.ACT_HIT_DROP_TYPE_1) {
                    awards = drop;
                } else if (type == StaticActBandit.ACT_HIT_DROP_TYPE_2 || type == StaticActBandit.ACT_HIT_DROP_TYPE_3) {
                    List<Integer> temp = drop.stream().filter(list -> lv >= list.get(0) && lv <= list.get(1))
                            .findFirst().orElse(null);
                    if (!CheckNull.isEmpty(temp)) {
                        awards.add(temp.stream().skip(2).collect(Collectors.toList()));
                    }
                }
/*                // TODO: 2020/2/7 情人节活动, 对activityId做特殊处理
                if (activity.getActivityId() == 3101 || activity.getActivityId() == 3102) {
                    awards = Collections.singletonList(Stream.of(4, 1911, 1, 4000).collect(Collectors.toList()));
                }*/
                if (!CheckNull.isEmpty(awards)) {
                    List<Award> sendReward = rewardDataManager.sendReward(player, awards, AwardFrom.ACT_BANDIT_AWARD);
                    if (!CheckNull.isEmpty(sendReward)) {
                        recordActHitAwardCnt(player, activityType);
                        reward.addAll(sendReward);
                    }
                }
            }
        }
        return reward;
    }

    /**
     * 记录当天奖章掉落
     *
     * @param player
     * @param type
     */
    private void recordActHitAwardCnt(Player player, int type) {
        int cnt = getActHitAwardCnt(player, type);
        // 掉落+1
        ++cnt;
        if (type == ActivityConst.ACT_BANDIT_AWARD) {
            player.setMixtureData(PlayerConstant.ACT_BANDIT_AWARD_COUNT, cnt);
        } else if (type == ActivityConst.ACT_CHRISTMAS) {
            player.setMixtureData(PlayerConstant.ACT_CHRISTMAS_AWARD_COUNT, cnt);
        } else if (type == ActivityConst.ACT_REPAIR_CASTLE) {
            player.setMixtureData(PlayerConstant.ACT_REPAIR_CASTLE_COUNT,cnt);
        }else if (type == ActivityConst.ACT_MONSTER_NIAN) {
            Activity activity = getActivityInfo(player, type);
            if (Objects.nonNull(activity)) {
                int today = TimeHelper.getCurrentDay();
                activity.getSaveMap().put(today, cnt);
                // 客户端强烈要求在掉落鞭炮时通知该活动发生了变化, 客户端在打开此活动时并不会向服务器请求数据.
                // 该活动玩家的数据刷新依赖于活动变化协议. 客户端(李潮)
                syncActChange(player, type, 0);
            }
        }else if (type == ActivityConst.ACT_DROP_CONTROL){
            player.setMixtureData(PlayerConstant.ACT_DROP_CONTROL_COUNT, cnt);
        }
    }

    /**
     * 检测奖章掉落上限
     *
     * @param player
     * @param type
     * @param total
     * @return
     */
    private boolean checkActHitAwardCnt(Player player, int type, int total) {
        if (getActHitAwardCnt(player, type) < total) {
            return true;
        }
        return false;
    }

    /**
     * 获取当天奖章掉落
     *
     * @param player
     * @param type
     * @return
     */
    public int getActHitAwardCnt(Player player, int type) {
        if (type == ActivityConst.ACT_BANDIT_AWARD) {
            return player.getMixtureDataById(PlayerConstant.ACT_BANDIT_AWARD_COUNT);
        } else if (type == ActivityConst.ACT_CHRISTMAS) {
            return player.getMixtureDataById(PlayerConstant.ACT_CHRISTMAS_AWARD_COUNT);
        } else if(type == ActivityConst.ACT_REPAIR_CASTLE){
            return player.getMixtureDataById(PlayerConstant.ACT_REPAIR_CASTLE_COUNT);
        } else if (type == ActivityConst.ACT_MONSTER_NIAN) {
            Activity activity = getActivityInfo(player, type);
            if (Objects.nonNull(activity)) {
                int today = TimeHelper.getCurrentDay();
                return activity.getSaveMap().getOrDefault(today, 0);
            } else {
                return Integer.MAX_VALUE;
            }
        } else if (type == ActivityConst.ACT_DROP_CONTROL){
            return player.getMixtureDataById(PlayerConstant.ACT_DROP_CONTROL_COUNT);
        }
        return 0;
    }

    /**
     * 获取盖世太保奖励
     *
     * @param player
     * @param lv
     * @return
     */
    public List<Award> getActGestapoAward(Player player, int lv) {
        Activity activity = getActivityInfo(player, ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activity)) return null;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(activityBase)) return null;
        int open = activityBase.getBaseOpen();
        if (open != ActivityConst.OPEN_STEP) return null;
        if (!CheckNull.isEmpty(ActParamConstant.ACT_BANDIT_BADGE_MAP)) {
            List<List<Integer>> param = ActParamConstant.ACT_BANDIT_BADGE_MAP.get(lv);
            if (!CheckNull.isEmpty(param)) {
                List<Integer> award = RandomUtil.getRandomByWeightAndRatio(param, 3, false,
                        (int) Constant.TEN_THROUSAND);
                List<List<Integer>> awards = new ArrayList<>();
                if (!CheckNull.isEmpty(award)) {
                    awards.add(award);
                }
                return rewardDataManager.sendReward(player, awards, AwardFrom.ACT_BANDIT_ACCE);
            }
        }
        return null;
    }

    /**
     * 检测盖世太保活动可领取状态
     *
     * @param player
     * @param exchange
     * @param activity
     * @param schedule
     * @param keyId
     * @return
     */
    public boolean checkEnoughExchange(Player player, StaticActExchange exchange, Activity activity, int schedule,
                                       Integer keyId) {
        List<Integer> prop = exchange.getProp();
        if (CheckNull.isEmpty(prop)) return false;
        // 兑换道具数量不足
        if (schedule < prop.get(2)) return false;
        // 兑换次数达到上限
        int cnt = activity.getStatusCnt().get(keyId) == null ? 0
                : new Long(activity.getStatusCnt().get(keyId)).intValue();
        if (cnt < 0 || cnt >= exchange.getNumberLimit()) return false;
        // 兑换等级不足
        if (player.lord.getLevel() < exchange.getLvLimit()) return false;
        return true;
    }

    /**
     * 获取盖世太保返还兵力比例
     *
     * @return
     */
    public double getGestapoRecArmyNum() {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATK_GESTAPO);
        if (activityBase == null) {
            return 0.0;
        } else {
            if (ActParamConstant.ACT_GESTAPO_RECOVER_ARMY_NUM > 10000
                    || ActParamConstant.ACT_GESTAPO_RECOVER_ARMY_NUM <= 0) {// 数据配错了,不翻倍
                return 0.0;
            }
            return ActParamConstant.ACT_GESTAPO_RECOVER_ARMY_NUM / Constant.TEN_THROUSAND;
        }
    }

    /**
     * 计算恢复的兵力 公式 恢复=损兵*加成值 向下取整
     *
     * @param lostArmy
     * @return
     */
    public Map<Integer, Integer> calcRecoverArmyCntByGestapo(Map<Integer, Integer> lostArmy) {
        final Map<Integer, Integer> cntMap = new HashMap<>();
        final double recoverArmyEffect = getGestapoRecArmyNum();
        lostArmy.forEach((armyType, lostCnt) -> {
            int cnt = (int) (lostCnt * recoverArmyEffect);// 向下取整
            if (cnt > 0) {
                cntMap.put(armyType, cnt);
            }
        });
        return cntMap;
    }

    /**
     * 获取攻城掠地目标进度
     *
     * @param player
     * @param staticAtkCityAct
     * @return
     */
    public int getAtkCityActStatus(Player player, StaticAtkCityAct staticAtkCityAct) {
        Integer lvMax = 0;
        switch (staticAtkCityAct.getTaskType()) {
            case ActivityConst.ACT_TASK_ATTACK: // 参与并攻陷
            case ActivityConst.ACT_TASK_JOIN_ATK: // 参与攻打据点
                StringBuilder sb = new StringBuilder(staticAtkCityAct.getTaskType() + "");
                if (staticAtkCityAct.getParam() != null && !staticAtkCityAct.getParam().isEmpty()) {
                    for (int i = 0; i < staticAtkCityAct.getParam().size(); i++) {
                        sb.append("_" + staticAtkCityAct.getParam().get(i));
                    }
                }
                String key = sb.toString();
                if (player.atkCityAct.getTankTypes().containsKey(key)) {
                    lvMax += player.atkCityAct.getTankTypes().get(key);
                }
                break;
            case ActivityConst.ACT_TASK_CITY: // 所在地图本军团最多拥有几座指定类型的城
                int param = 0;
                if (!staticAtkCityAct.getParam().isEmpty() && staticAtkCityAct.getParam().size() > 0) {
                    param = staticAtkCityAct.getParam().get(0);
                }
                int areaId = MapHelper.getAreaIdByPos(player.lord.getPos());
                lvMax = worldDataManager.getCityTypeNum4CampAndArea(areaId, player.lord.getCamp(), param);
                break;
            case ActivityConst.ACT_TASK_ATK_AND_JOIN: // 参与并攻打玩家
            case ActivityConst.ACT_TASK_JOIN_OR_ATK: // 参与并击飞
            case ActivityConst.ACT_TASK_JOIN_OR_DEF: // 协防阵营玩家
                for (Entry<Integer, Integer> kv : player.atkCityAct.getTypeMap(staticAtkCityAct.getTaskType())
                        .entrySet()) {
                    if (staticAtkCityAct.getParam() == null || staticAtkCityAct.getParam().isEmpty()
                            || kv.getKey() >= staticAtkCityAct.getParam().get(0)) {
                        lvMax += kv.getValue();
                    }
                }
                break;
            case ActivityConst.ACT_TASK_JOIN_CAMPAGIN: // 申请总督
            case ActivityConst.ACT_TASK_CITY_LEVY: // 城池征收
            case ActivityConst.ACT_TASK_JOIN_DEF: // 防守本国城池
            case ActivityConst.ACT_TASK_JOIN_ATK_OTHER_CITY:// 参与并攻下敌对阵营的城
                lvMax = player.atkCityAct.getStatus().get(staticAtkCityAct.getTaskType());
                break;
            default:
                break;
        }
        return lvMax != null ? lvMax : 0;
    }

    /**
     * 更新攻城掠地的目标进度
     *
     * @param player
     * @param type
     * @param params
     */
    public void updAtkCityActSchedule(Player player, int type, Object... params) {
        if (null == player) {
            return;
        }
        AtkCityAct atkCityAct = player.atkCityAct;
        Activity activity = getActivityInfo(player, ActivityConst.ACT_ATTACK_CITY_NEW);
        if (CheckNull.isNull(activity)) {
            return;
        }
        ActivityBase actBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATTACK_CITY_NEW);
        if (actBase == null) {
            return;
        }
        // 活动不在
        if (activity.getOpen() != ActivityConst.OPEN_STEP) {
            return;
        }
        long status = 0;
        // int param = 0;
        switch (type) {
            case ActivityConst.ACT_TASK_ATK_AND_JOIN:// 参与并攻打
            case ActivityConst.ACT_TASK_JOIN_OR_ATK:// 参与并击飞
            case ActivityConst.ACT_TASK_JOIN_OR_DEF:// 协防阵营玩家
                int intKey = (Integer) params[0];
                Map<Integer, Integer> warMap = atkCityAct.getTypeMap(type);
                status = ActivityConst.INIT_STATUS;
                if (warMap.containsKey(intKey)) {
                    status += warMap.get(intKey);
                }
                warMap.put(intKey, (int) status);
                break;
            case ActivityConst.ACT_TASK_ATTACK: // 参与并攻陷
            case ActivityConst.ACT_TASK_JOIN_ATK: // 参与攻打据点
                StringBuilder sb = new StringBuilder(type + "");
                for (int i = 0; i < params.length; i++) {
                    sb.append("_" + params[i]);
                }
                String key = sb.toString();
                status = ActivityConst.INIT_STATUS;
                if (atkCityAct.getTankTypes().containsKey(key)) {
                    status += atkCityAct.getTankTypes().get(key);
                }
                atkCityAct.getTankTypes().put(key, (int) status);
                LogUtil.debug("参与攻下几座指定类型的城， 统计=" + atkCityAct.getTankTypes() + ",key=" + key);
                break;
            case ActivityConst.ACT_TASK_JOIN_CAMPAGIN: // 申请总督
            case ActivityConst.ACT_TASK_CITY_LEVY: // 城池征收
            case ActivityConst.ACT_TASK_JOIN_DEF: // 防守本国城池
            case ActivityConst.ACT_TASK_JOIN_ATK_OTHER_CITY:// 参与并攻下敌对阵营的城
                Map<Integer, Integer> atkCityMap = atkCityAct.getStatus();
                status = ActivityConst.INIT_STATUS;
                if (atkCityMap.containsKey(type)) {
                    status += atkCityMap.get(type);
                }
                atkCityMap.put(type, (int) status);
                break;
            default:
                // return;
        }

        List<StaticAtkCityAct> list = StaticActivityDataMgr.getAtkCityActListByType(type);
        if (list == null) {
            return;
        }
        Date now = new Date();
        int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // 活动开启的第几天

        int tips = 0;
        if (type != ActivityConst.ACT_TASK_CITY) {
            for (StaticAtkCityAct e : list) {
                int keyId = e.getKeyId();
                if (atkCityAct.getStatusCnt().containsKey(keyId)) {
                    Integer count = atkCityAct.getStatusCnt().get(keyId);
                    if (count >= e.getNum()) { // 已领取次数已满
                        continue;
                    }
                }
                if (atkCityAct.getCanRecvKeyId().contains(keyId)) { // 可领取,还未领取
                    continue;
                }
                if (e.getDay() > dayiy) {
                    continue;
                }
                status = getAtkCityActStatus(player, e);
                if (status >= ActivityConst.INIT_STATUS) {
                    atkCityAct.getCanRecvKeyId().add(keyId);
                    tips++;
                }
            }
        } else {
            tips = 1;
        }
        if (tips > 0) {
            syncActChange(player, ActivityConst.ACT_ATTACK_CITY_NEW);
        }
    }

    /**
     * 攻城掠地获取当前状态
     *
     * @param player
     * @param activity
     * @param staticActAward
     * @return 0可以领取 1已领取 2活跃度未达到
     */
    public int caluActRecvedState(Player player, Activity activity, StaticActAward staticActAward) {
        int keyId = staticActAward.getKeyId();
        if (activity.getStatusMap().containsKey(keyId)) {
            return 1;
        } else {
            int active = currentActivity(player, activity, 0);
            if (active >= staticActAward.getCond()) {
                return 0;
            } else {
                return 2;
            }
        }
    }

    /**
     * 可领取次数
     *
     * @param player
     * @param atkCityAct
     * @param cityAct
     * @return 0 不可领取, 2 可领取几次
     */
    public int getCanRecvCnt(Player player, StaticAtkCityAct atkCityAct, AtkCityAct cityAct) {
        int status = getAtkCityActStatus(player, atkCityAct);
        int num = atkCityAct.getNum();
        // 条件次数
        int cond = atkCityAct.getCond();
        if (cond > status) {
            return 0;
        }
        int temp = 0;
        if (cityAct.getStatusCnt().containsKey(atkCityAct.getKeyId())) {
            int count = cityAct.getStatusCnt().get(atkCityAct.getKeyId());
            temp = count >= num ? 0 : (status - count * cond) / cond;
        } else {
            temp = status / cond;
        }
        return temp;
    }

    /**
     * 领取目标活跃度(不同步活动进度)
     *
     * @param player
     * @param cityAct
     * @param staticAtkCityAct
     * @param canRecvCnt
     * @param activity
     */
    public void recvActiveCnt(Player player, AtkCityAct cityAct, StaticAtkCityAct staticAtkCityAct, int canRecvCnt,
                              Activity activity) {
        int keyId = staticAtkCityAct.getKeyId();
        int count = 0;
        // 目标完成的进度
        for (int i = 0; i < canRecvCnt; i++) {
            if (cityAct.getStatusCnt().containsKey(keyId)) {
                count = cityAct.getStatusCnt().get(keyId);
                if (count >= staticAtkCityAct.getNum()) {
                    break;
                }
            }
            // 更新目标领取进度
            cityAct.getStatusCnt().put(keyId, count + 1);
            cityAct.getCanRecvKeyId().remove(keyId); // 去除可领取
            // 更新活跃度
            Long state = activity.getStatusCnt().get(0);
            state = state == null ? 0 : state;
            state = state + staticAtkCityAct.getPoint();
            activity.getStatusCnt().put(0, state);
        }
    }

    /**
     * 领取目标活跃度
     *
     * @param player
     * @param cityAct
     * @param staticAtkCityAct
     * @param canRecvCnt
     */
    public void recvActiveCnt(Player player, AtkCityAct cityAct, StaticAtkCityAct staticAtkCityAct, int canRecvCnt) {
        int keyId = staticAtkCityAct.getKeyId();
        int count = 0;
        // 目标完成的进度
        for (int i = 0; i < canRecvCnt; i++) {
            if (cityAct.getStatusCnt().containsKey(keyId)) {
                count = cityAct.getStatusCnt().get(keyId);
                if (count >= staticAtkCityAct.getNum()) {
                    break;
                }
            }
            // 更新目标领取进度
            cityAct.getStatusCnt().put(keyId, count + 1);
            cityAct.getCanRecvKeyId().remove(keyId); // 去除可领取
            // 更新活跃度
            updActivity(player, ActivityConst.ACT_ATTACK_CITY_NEW, staticAtkCityAct.getPoint(), 0, false);
        }
        // 检查推送
        syncActChange(player, ActivityConst.ACT_ATTACK_CITY_NEW);
    }

    /**
     * 具备促销积分盒子可领取次数
     *
     * @param sAward
     * @param activity
     * @param val
     * @return 可领取次数
     * @throws MwException
     */
    public int getAwardCnt(StaticActAward sAward, Activity activity, long val) {
        if (CheckNull.isNull(sAward)) {
            return 0;
        }
        int keyId = sAward.getKeyId();
        // 已领取次数
        int state = activity.getSaveMap().containsKey(keyId) ? activity.getSaveMap().get(keyId) : 0;
        int ceil = (int) Math.ceil(val / ActParamConstant.ACT_PROP_PROMOTION_AWARD_NUM);
        int cnt = ceil >= sAward.getCond() ? sAward.getCond() : ceil; // 总积分可领取次数
        return cnt - state;
    }

    /**
     * @param player
     * @param activity
     * @return int
     * @Title: getExchangeActCnt
     * @Description: 获取兑换活动的tips
     */
    public int getExchangeActCnt(Player player, Activity activity) {
        int tips = 0;
        List<StaticActExchange> exchangeList = StaticActivityDataMgr.getActExchangeListById(activity.getActivityId());
        // 兑换道具数量
        int schedule = currentActivity(player, activity, 0);
        for (StaticActExchange exchange : exchangeList) {
            Integer keyId = exchange.getKeyId();
            if (checkEnoughExchange(player, exchange, activity, schedule, keyId)) {
                tips++;
            }
        }
        return tips;
    }

    /**
     * 获取指定活动的开始时间和结束时间
     *
     * @param activityType
     */

    public StaticActivityPlan getStaticActivityPlan(int activityType) {
        return staticDataDao.getStaticActivityPlan(activityType);
    }

    /***
     * 获取三倍返利活动的红点(tips)
     *
     * @param player
     * @param activityType
     * @return
     */
    public int ThreeRebateTips(Player player, int activityType) {
        int tips = 0;
        Activity activity = getActivityInfo(player, activityType);
        if (activity == null) {
            return tips = 0;
        }
        // 判断玩家是否拥有橙色战机 //判断玩家是否充值 //是否领取奖励
        if (getIsHave(player) && getIsPay(player, activity) && getIsGet(player, activity)) {
            return 1;
        }
        if (getIsHave(player) || getIsPay(player, activity)) {
            syncActChange(player, ActivityConst.ACT_THREE_REBATE);
        }
        return tips;
    }

    // 判断是否支付
    public boolean getIsPay(Player player, Activity activity) {
        return player.activitys.get(ActivityConst.ACT_THREE_REBATE).getStatusCnt().get(2) != null;
    }

    // 判断是否领取奖励
    public boolean getIsGet(Player player, Activity activity) {
        return player.activitys.get(ActivityConst.ACT_THREE_REBATE).getStatusMap().containsKey(1);// false 未领取奖励 ,true
        // 已领取奖励
    }

    // 判断用户是否拥有橙色战机
    public boolean getIsHave(Player player) {
        Map<Integer, WarPlane> warPlanes = player.warPlanes;
        for (Integer in : warPlanes.keySet()) {
            int planeId = warPlanes.get(in).getPlaneId();
            StaticPlaneUpgrade upgradeById = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
            int quality = upgradeById.getQuality();
            if (quality == PlaneConstant.PLANE_QUALITY_FIVE) {
                return true;
            }
        }
        return false;
    }

    public void updateLuckyPoolLive(Player player, int live) {
        Activity act = getActivityInfo(player, ActivityConst.ACT_LUCKY_POOL);
        if (act == null) {
            return;
        }
        updateLuckyPoolLive(player, live, act);
    }

    public void updateLuckyPoolLive(Player player, int live, Activity act) {
        for (List<Integer> list : ActParamConstant.LUCKY_POOL_2) {
            int tar = list.get(0);
            // 已经领过
            if (act.getStatusMap().containsKey(tar)) {
                continue;
            }

            if (live >= tar) {
                // 记录领奖状态
                act.getStatusMap().put(tar, list.get(1));
                // 添加可抽取次数
                act.getSaveMap().put(0, list.get(1) + act.getSaveMap().getOrDefault(0, 0));
            }
        }
    }

    public void processCombineServerAct(Player player, int actType, int now) {
        MultiHandleGlobalActProcess process = multiHandleGlobalActMap.get(actType);
        if (CheckNull.isNull(process))
            return;

        process.process(player, actType, now);
    }

    public void actGlobalMagicTreasureWare(Player player, int actType, int now) {
        try {
            if (player == null) {
                return;
            }
            // 此处不能使用 getActivityInfo方法,因为此时活动可能是未开启状态
            Activity activity = player.activitys.get(actType);
            if (CheckNull.isNull(activity))
                return;
            // 发送排行榜奖励
            DataResource.ac.getBean(ActivityMagicTreasureWareService.class).sendSettleRankAward(player, actType, activity);
        } catch (Exception e) {
            LogUtil.error("邮件发送奖励异常", e);
        }
    }

    @FunctionalInterface
    interface MultiHandleGlobalActProcess {
        void process(Player player, int actType, int now);
    }
}
