package com.gryphpoem.game.zw.service.activity;

import com.alibaba.fastjson.JSONObject;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.NumUtils;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.*;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ActBlackhawk;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.Day7Act;
import com.gryphpoem.game.zw.resource.domain.p.SimpleRank;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Gestapo;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.server.SaveGlobalActivityServer;
import com.gryphpoem.game.zw.server.SendMsgServer;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.anniversary.ActivityFireWorkService;
import com.gryphpoem.game.zw.service.activity.cross.CrossRechargeLocalActivityService;
import com.gryphpoem.game.zw.service.relic.RelicService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ActivityService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private ActivityHelpService activityHelpService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private SignInService signInService;
    @Autowired
    private PropService propService;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private ActivityHotProductService activityHotProductService;
    @Autowired
    private ActivityLotteryService activityLotteryService;
    @Autowired
    private ActivityChristmasService activityChristmasService;
    @Autowired
    private CastleSkinService castleSkinService;
    @Autowired
    private ActivityMonsterNianService activityMonsterNianService;
    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;
    @Autowired
    private ActivityChargeContinueService activityChargeContinueService;
    @Autowired
    private ActivityMergePromotionService activityMergePromotionService;
    @Autowired
    private ActivityFireWorkService activityFireWorkService;
    @Autowired
    private CrossRechargeLocalActivityService crossRechargeLocalActivityService;
    @Autowired
    private ActivityBoxOfficeService activityBoxOfficeService;
    @Autowired
    private MusicFestivalCreativeService musicFestivalCreativeService;
    @Autowired
    private ActivityTemplateService activityTemplateService;

    /**
     * ??????????????????
     *
     * @param roleId
     */
    public GetActivityListRs getActivityList(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // int platFlag = 1;// ?????????????????????
        // int platNo = player.account.getPlatNo();
        List<ActivityBase> list = StaticActivityDataMgr.getActivityList();
        GetActivityListRs.Builder builder = GetActivityListRs.newBuilder();
        Date now = new Date();
        PersonalActService.initData(player);
        for (ActivityBase actBase : list) {
            try {
                int activityType = actBase.getActivityType();
                AbsActivityService absActivityService = activityTemplateService.getActivityService(activityType);
                if (Objects.nonNull(absActivityService) &&
                        (!absActivityService.inChannel(player, actBase) || !absActivityService.functionOpen(player, activityType))) {
                    continue;
                }

                if (ActivityConst.ACT_LIGHTNING_WAR == activityType) {// ???????????????
                    actBase = changeActivityTime(actBase);
                }
                int open = actBase.getBaseOpen();
                if (open == ActivityConst.OPEN_CLOSE || actBase.isBaseDisplay()) {// ???????????????
                    continue;
                }
                Activity activity = activityDataManager.getActivityInfo(player, activityType);
                if (activity == null) {
                    continue;
                }
                if (player.getPersonalActs().containActType(activityType))
                    continue;
                Date awardBeginTime = actBase.getAwardBeginTime();
                int tips = 0;
                if (ActivityConst.ACT_COMMAND_LV == activityType || ActivityConst.ACT_LEVEL == activityType
                        || ActivityConst.ACT_ALL_CHARGE == activityType || ActivityConst.ACT_BRAVEST_ARMY == activityType
                        || ActivityConst.ACT_GIFT_PAY == activityType
                        || ActivityConst.ACT_COST_GOLD == activityType/* || ActivityConst.ACT_GIFT_OL == activityType*/
                        || ActivityConst.ACT_FREE_LUXURY_GIFTS == activityType
                        || ActivityConst.ACT_WAR_ROAD == activityType
                        || ActivityConst.ACT_WAR_ROAD_DAILY == activityType
                        || ActivityConst.ACT_SHARE_REWARD == activityType
                        || ActivityConst.ACT_BIG_KILL == activityType
                    /*|| ActivityConst.ACT_DAILY_PAY == activityType*/) { // ????????????????????????
                    ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                    int activityKeyId = activityBase.getActivityId();
                    List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                    int schedule = 0;// ??????????????????????????????????????????????????????????????????
                    if (condList != null) {
                        schedule = activityDataManager.currentActivity(player, activity, 0);
                        for (StaticActAward saa : condList) {
                            int keyId = saa.getKeyId();
                            if (!activity.getStatusMap().containsKey(keyId) && schedule >= saa.getCond()) {// ???????????????
                                if (activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY) {// ??????????????? ????????????
                                    // ???????????????????????????
                                    int topup = activity.getSaveMap()
                                            .get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                                            : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD);
                                    // ???????????? ???????????????????????????
                                    int checkLordGlod = 0;
                                    if (saa.getParam() != null && saa.getParam().size() > 0) {
                                        checkLordGlod = saa.getParam().get(0);
                                    }
                                    if (topup >= checkLordGlod) {
                                        tips++;
                                    }
                                } else {
                                    tips++;
                                }
                            }
                            /* if (activityType == ActivityConst.ACT_DAILY_PAY && saa.getParam().get(0) == 0
                                && !activity.getStatusMap().containsKey(keyId)) {
                            tips++;
                            }*/
                        }
                        if (activityType == ActivityConst.ACT_LEVEL && null == activity.getStatusCnt().get(0)) {// ??????????????????????????????
                            tips = 0; // ???????????????
                        }
                        if ((activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY)
                                && player.lord.getLevel() < ActParamConstant.ACT_ALL_CHARGE_LORD_LV) {
                            tips = 0; // ???????????????
                        }
                    }
                } else if (ActivityConst.ACT_LUCKY_TURNPLATE == activityType) { // ?????? ??????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.FAMOUS_GENERAL_TURNPLATE == activityType) {// ????????????
                    tips = activityDataManager.getExchangeActCnt(player, activity);
                } else if (ActivityConst.ACT_EQUIP_TURNPLATE == activityType) { // ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (StaticActivityDataMgr.isActTypeRank(activityType)) {// ????????????
                    int activityKeyId = actBase.getActivityId();
                    List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                    if (!CheckNull.isEmpty(condList)) {
                        int schedule = activityDataManager.getRankAwardSchedule(player, activityType);
                        for (StaticActAward saa : condList) {
                            int keyId = saa.getKeyId();
                            // ??? ??????????????????, ??????????????????, ?????????????????????
                            if (ActivityConst.ACT_PAY_RANK_NEW == activityType
                                    || ActivityConst.ACT_CAMP_RANK == activityType
                                    || ActivityConst.ACT_CAMP_FIGHT_RANK == activityType
                                    || ActivityConst.ACT_PAY_RANK_V_3 == activityType
                                    || ActivityConst.ACT_MERGE_PAY_RANK == activityType
                                    || ActivityConst.ACT_CONSUME_GOLD_RANK == activityType
                                    || ActivityConst.ACT_TUTOR_RANK == activityType) {
                                int startRank = 0;// ????????????
                                if (!CheckNull.isEmpty(saa.getParam()) && saa.getParam().size() > 1) {
                                    startRank = saa.getParam().get(1);
                                }
                                if (!activity.getStatusMap().containsKey(keyId) && schedule > 0 && schedule >= startRank
                                        && schedule <= saa.getCond() && now.getTime() >= awardBeginTime.getTime()) {// ???????????????
                                    tips++;
                                }
                            } else {
                                // ??????????????????tips??????
                                if (!activity.getStatusMap().containsKey(keyId) && schedule > 0 && schedule <= saa.getCond()
                                        && now.getTime() >= awardBeginTime.getTime()) {// ???????????????
                                    tips++;
                                }
                            }
                        }
                    }
                } else if (ActivityConst.ACT_VIP == activityType || ActivityConst.ACT_ATTACK_CITY == activityType
                        || ActivityConst.ACT_CHALLENGE_COMBAT == activityType || ActivityConst.ACT_TRAINED_SOLDIERS == activityType || ActivityConst.ACT_TRAINED_SOLDIERS_DAILY == activityType
                        || ActivityConst.ACT_COLLECT_RESOURCES == activityType
                        || ActivityConst.ACT_RESOUCE_SUB == activityType
                        || ActivityConst.ACT_EQUIP_MATERIAL == activityType
                        || ActivityConst.ACT_ELIMINATE_BANDIT == activityType
                ) {// ????????????,??????VIP??????, ????????????, ????????????, ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (activityType == ActivityConst.ACT_VIP_BAG) { // ??????????????????
                    Date nowDate = new Date();
                    // int createLordDay = DateHelper.dayiy(player.account.getCreateDate(), nowDate); // ????????????????????????
                    // int openServerDay = serverSetting.getOpenServerDay(nowDate);// ???????????????
                    // List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftpackPlanMapByDay(createLordDay,
                    //         openServerDay, nowDate);

                    List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftPackPlanByDate(player.account.getCreateDate(), serverSetting.getOpenServerDate(), nowDate);
                    if (CheckNull.isEmpty(planList)) {
                        continue;
                    }
                } else if (activityType == ActivityConst.ACT_FIRSH_CHARGE) {// ????????????
                    long createTime = player.account.getCreateDate().getTime();
                    long disPlayTime = createTime + (ActParamConstant.ACT_FIRSH_CHARGE_TIME * 1000L);
                    // Activity act = activityDataManager.getActivityInfo(player, activityType);
                    // actBase.setEndTime(new Date(disPlayTime));
                    actBase = copyActiviyBase(actBase, player.account.getCreateDate(), new Date(disPlayTime));
                    if (now.getTime() > disPlayTime) {
                        continue;
                    }
                } else if (activityType == ActivityConst.ACT_BLACK || activityType == ActivityConst.ACT_ROBIN_HOOD) { // ?????????????????????????????????
                    try {
                        int endTime = checkBlackhawkIsOver(player, TimeHelper.getCurrentSecond());
                        actBase = copyActiviyBase(actBase, player.account.getCreateDate(), new Date(endTime * TimeHelper.SECOND_MS));
                        // actBase.setEndTime(new Date(endTime * 1000L));
                    } catch (MwException mwe) { // ?????????????????????????????????
                        LogUtil.common("---------???????????????????????????????????????????????????-----------", mwe.getMessage());
                        continue;
                    }
                } else if (activityType == ActivityConst.ACT_7DAY) {// ????????????
                    int cRoleDay = playerDataManager.getCreateRoleDay(player, now);
                    if (cRoleDay > 10) continue; // ???????????????
                    if (player.day7Act == null) continue; // ???????????????????????????
                    tips = player.day7Act.getCanRecvKeyId().size();
                } else if (ActivityConst.ACT_PAY_7DAY == activityType) {// ????????????
                    int cRoleDay = playerDataManager.getCreateRoleDay(player, now);
                    if (cRoleDay > 7) continue; // ???????????????
                    int afterDayTime = DateHelper.afterDayTime(player.account.getCreateDate(), 8);
                    Date endTime = new Date((afterDayTime - 1) * 1000L);
                    actBase = copyActiviyBase(actBase, player.account.getCreateDate(), endTime);
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_CHARGE_TOTAL == activityType) {// ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);// ????????????????????????
                } else if (activityType == ActivityConst.ACT_CHARGE_CONTINUE || activityType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {// ????????????
                    // ??????????????????tips
                    tips = activityChargeContinueService.getChargeContinueTips(player, activityType);
                } else if (activityType == ActivityConst.ACT_FOOD) {// ????????????
                    try {
                        refreshPowerState(player.roleId, activity);
                    } catch (MwException mwe) {
                        continue; // ??????????????????
                    }
                    for (Long state : activity.getStatusCnt().values()) {
                        if (state == 1) {
                            tips++;
                        }
                    }
                } else if (activityType == ActivityConst.ACT_SUPPLY_DORP) {// ????????????
                    List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
                    for (StaticActAward actAward : awardList) {
                        if (isSupplyDorpGet(actAward, activity, now.getTime())) {
                            tips++;
                        }
                    }
                    if (checkSupplyIsAllGot(activity) && actBase.getBaseOpen() == ActivityConst.OPEN_AWARD) {
                        continue;
                    }
                } else if (ActivityConst.ACT_DAILY_PAY == activityType) { // ????????????
                    ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                    int activityKeyId = activityBase.getActivityId();
                    List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                    if (!CheckNull.isNull(condList)) {
                        int schedule = activityDataManager.currentActivity(player, activity, 0);
                        for (StaticActAward sa : condList) {
                            int keyId = sa.getKeyId();
                            if (!activity.getStatusMap().containsKey(keyId) && schedule >= sa.getCond()
                                    && activityDataManager.caluActCailyPayStatus(activityBase.getActivityId(), sa,
                                    activity) == 0) {// ???????????????
                                tips++;
                            }
                            // ??????????????????,???????????????????????????
                            if (sa.getParam().get(0) == 0 && !activity.getStatusMap().containsKey(keyId)) {
                                tips++;
                            }
                        }
                    }
                } else if (ActivityConst.ACT_LOGIN_EVERYDAY == activityType) {// ??????????????????
                    if (activity.getStatusCnt().isEmpty()) {
                        int pLv = player.lord.getLevel();
                        activity.getStatusCnt().put(0, (long) pLv); // ??????????????????
                    }
                    if (activity.getStatusMap().isEmpty()) {
                        tips++;
                    }
                } else if (ActivityConst.ACT_ATK_GESTAPO == activityType) {// ??????????????????
                    tips = activityDataManager.getExchangeActCnt(player, activity);
                } else if (ActivityConst.ACT_ATTACK_CITY_NEW == activityType) {// ?????????????????????
                    int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // ????????????????????????
                    if (CheckNull.isNull(player.atkCityAct)) continue;
                    List<StaticAtkCityAct> actList = StaticActivityDataMgr.getAtkCityActList();
                    // ????????????
                    if (!CheckNull.isEmpty(actList)) {
                        for (StaticAtkCityAct staticAtkCityAct : actList) {
                            int canRecvCnt = activityDataManager.getCanRecvCnt(player, staticAtkCityAct,
                                    player.atkCityAct);
                            if (canRecvCnt > 0 && staticAtkCityAct.getDay() <= dayiy) {
                                tips++;
                            }
                        }
                    }
                    // ???????????????
                    int schedule = activityDataManager.currentActivity(player, activity, 0);
                    if (schedule > 0) {
                        List<StaticActAward> awards = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
                        for (StaticActAward award : awards) {
                            int keyId = award.getKeyId();
                            if (!activity.getStatusMap().containsKey(keyId) && schedule >= award.getCond()) {
                                tips++;
                            }
                        }
                    }
                    if (ActivityConst.ACT_ATTACK_CITY_NEW == activityType
                            && player.lord.getLevel() < ActParamConstant.ACT_ATK_CITY_LEVEL.get(0)) {
                        tips = 0; // ???????????????
                    }
                } else if (ActivityConst.ACT_PROP_PROMOTION == activityType) { // ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_PAY_TURNPLATE == activityType) {// ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_LUCKY_POOL == activityType) {// ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_ORE_TURNPLATE == activityType) {// ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_DAY_DISCOUNTS == activityType) {// ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_GIFT_PROMOTION == activityType) {// ????????????
                    Date[] dateArr = activityHelpService.getGiftPromotionDate();
                    if (dateArr == null) continue;// ?????????????????????
                    actBase = copyActiviyBase(actBase, dateArr[0], dateArr[1]);
                } else if (ActivityConst.ACT_MONOPOLY == activityType) {// ?????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_THREE_REBATE == activityType) { // ????????????
                    tips = activityDataManager.ThreeRebateTips(player, activityType);
                } else if (ActivityConst.ACT_SIGIN == activityType) { // ??????
                    tips = signInService.getRedPoint(player, activity);
                } else if (ActivityConst.ACT_WAR_PLANE_SEARCH == activityType) {
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_EASTER == activityType) {
                    // ???????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_BUILD_GIFT == activityType) {
                    // ?????????????????????????????????, ????????????
                    if (activityDataManager.currentActivity(player, activity, 0) == 2) {
                        int freeAwardTime = activity.getSaveMap().getOrDefault(1, 0);
                        // ????????????????????????????????????
                        if (freeAwardTime == 0) {
                            // ?????????????????????????????????
                            activity.getSaveMap().put(1, TimeHelper.getCurrentSecond());
                        }
                    }
                    int freeTime = activity.getSaveMap().getOrDefault(1, 0);
                    if (freeTime > 0) {
                        long disPlayTime = (freeTime + ActParamConstant.BUILD_GIFT_CHARGE_TIME) * TimeHelper.SECOND_MS;
                        actBase = copyActiviyBase(actBase, actBase.getBeginTime(), new Date(disPlayTime));
                        // ?????????????????????
                        if (now.getTime() > disPlayTime) {
                            continue;
                        }
                    }
                } else if (ActivityConst.ACT_HOT_PRODUCT == activityType) { // ????????????
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE == activityType) {
                    long createTime = activity.getStatusCnt().getOrDefault(0, 0L);
                    if (createTime <= 0)
                        continue;
                    long disPlayTime = TimeHelper.getSomeDayAfter(TimeHelper.secondToDate((int) createTime), ActParamConstant.ACT_DEDICATED_CUSTOMER_SERVICE_CONF.get(1).get(0) - 1, 23, 59, 59) * TimeHelper.SECOND_MS;
                    actBase = copyActiviyBase(actBase, player.account.getCreateDate(), new Date(disPlayTime));
                    if (now.getTime() > disPlayTime) {
                        continue;
                    }
                } else if (ActivityConst.ACT_CHRISTMAS == activityType || ActivityConst.ACT_REPAIR_CASTLE == activityType) {
                    tips = 1;
                } else if (ActivityConst.ACT_ANNIVERSARY_FIREWORK == activity.getActivityType()) {
                    tips = activityFireWorkService.getTips(activity, actBase);
                } else if (ActivityConst.CROSS_ACT_RECHARGE_RANK == activity.getActivityType()) {
                    tips = crossRechargeLocalActivityService.getActivityTips(actBase, activity);
                } else if (ActivityConst.ACT_MUSIC_FESTIVAL_BOX_OFFICE == activity.getActivityType()) {
                    tips = activityBoxOfficeService.getActivityTips(actBase, activity);
                }
                // ????????????????????????????????????
                if (isAllGainActivity(player, actBase, activity)) {
                    continue;
                }
                if (open != ActivityConst.OPEN_STEP && !ActivityConst.isEndDisplayAct(activityType) &&
                        activityType != ActivityConst.FAMOUS_GENERAL_TURNPLATE) { // ??????????????????????????????
                    continue;
                }
                int tips_ = AbsSimpleActivityService.getTipsInActList(player, activityType);
                tips = tips_ == 0 ? tips : tips_;
                if (ActivityConst.ACT_CHRISTMAS == activityType || ActivityConst.ACT_REPAIR_CASTLE == activityType) {
                    builder.addActivity(activityChristmasService.buildActivityPb(activity, actBase, true, tips));
                } else {
                    builder.addActivity(PbHelper.createActivityPb(actBase, true, tips));
                }
            } catch (Exception error) {
                LogUtil.error("---------????????????????????????----------- actType:", actBase.getActivityType());
                LogUtil.error("---------????????????????????????-----------", error);
                continue;
            }
        }

        //?????????????????????????????????????????????
        int tips = 0;
        for (ActivityBase ab : StaticActivityDataMgr.getPersonalActivityList()) {
            try {
                int activityType = ab.getActivityType();
                int open = ab.getBaseOpen();
                // ???????????????
                if (open == ActivityConst.OPEN_CLOSE || ab.isBaseDisplay()) {
                    continue;
                }
                Activity activity = activityDataManager.getActivityInfo(player, activityType);
                if (activity == null) {
                    continue;
                }
                if (open != ActivityConst.OPEN_STEP && !ActivityConst.isEndDisplayAct(activityType)) { // ??????????????????????????????
                    continue;
                }
                if (!player.getPersonalActs().containActKey(activityType, ab.getPlanKeyId()))
                    continue;
                AbsActivityService abService = DataResource.ac.getBean(ActivityTemplateService.class).getActivityService(activityType);
                if (CheckNull.isNull(abService))
                    continue;
                if (!abService.functionOpen(player, activityType)) {
                    continue;
                }
                if (abService.isAllGainActivity(player, ab, activity))
                    continue;
                int tips_ = AbsSimpleActivityService.getTipsInActList(player, activityType);
                tips = tips_ == 0 ? tips : tips_;
                builder.addActivity(PbHelper.createActivityPb(ab, true, tips));
            } catch (Exception error) {
                LogUtil.error("---------????????????????????????----------- actType:", ab.getActivityType());
                LogUtil.error("---------????????????????????????-----------", error);
                continue;
            }
        }

        DataResource.getBean(RelicService.class).buildActivity(player, builder);

        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        int dayiy = DateHelper.dayiy(beginTime, now);
        builder.setDay(dayiy);
        builder.addAllPreVieAct(getDisplayActList(list));
        // ???????????????
        return builder.build();
    }

    /**
     * ?????? ActivityBase
     *
     * @param actBase
     * @param beginTime
     * @param endTime
     * @return
     */
    private ActivityBase copyActiviyBase(ActivityBase actBase, Date beginTime, Date endTime) {
        ActivityBase newActBase = new ActivityBase();
        BeanUtils.copyProperties(actBase, newActBase);
        newActBase.setBeginTime(beginTime);
        newActBase.setEndTime(endTime);
        return newActBase;
    }

    /**
     * ???????????????,???beginDate???endDate????????????????????????
     *
     * @param actBase
     * @return
     */
    private ActivityBase changeActivityTime(ActivityBase actBase) {
        ActivityBase activityBase = new ActivityBase();
        activityBase.setOpenTime(actBase.getOpenTime());
        activityBase.setPlan(actBase.getPlan());
        activityBase.setDisplayTime(actBase.getDisplayTime());
        activityBase.setAwardBeginTime(actBase.getAwardBeginTime());
        activityBase.setDisplayOpen(actBase.getDisplayOpen());
        activityBase.setStaticActivity(actBase.getStaticActivity());
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        Date beginTime = actBase.getBeginTime();
        final Date beginDate = DateHelper.afterStringTime(beginTime, lightningWar.getStartTime());
        if (!CheckNull.isNull(beginDate)) {
            activityBase.setBeginTime(beginDate);
        }
        Date endTime = actBase.getEndTime();
        final Date endDate = DateHelper.afterStringTime(endTime, lightningWar.getEndTime());
        if (!CheckNull.isNull(endDate)) {
            activityBase.setEndTime(endDate);
        }
        actBase = activityBase;
        return actBase;
    }

    /**
     * ??????????????????????????????
     *
     * @param list
     * @return
     */
    private List<CommonPb.Activity> getDisplayActList(List<ActivityBase> list) {
        List<CommonPb.Activity> actList = new ArrayList<>(list.size());
        for (ActivityBase actBase : list) {
            try {
                int activityType = actBase.getActivityType();
                if (ActivityConst.ACT_LIGHTNING_WAR == activityType) { // ?????????
                    actBase = changeActivityTime(actBase);
                }
                if (!actBase.isBaseDisplay()) continue;
                // int display = actBase.getBaseDisplay();
                // if (display != ActivityConst.DISPLAY_OPEN) continue;
                boolean cangetAward = false;// ????????????????????????
                actList.add(PbHelper.createActivityPb(actBase, cangetAward, 0));
            } catch (Exception e) {
                LogUtil.error(e, "??????????????????????????????: actType:", actBase.getActivityType(), ", actId:",
                        actBase.getActivityId());
            }
        }
        return actList;
    }

    /**
     * ???????????????????????????DISPLAY-OPEN???????????????
     *
     * @param roleId
     */
    public GetDisplayActListRs getDisplayActivityList(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<ActivityBase> list = StaticActivityDataMgr.getActivityList();
        GetDisplayActListRs.Builder builder = GetDisplayActListRs.newBuilder();
        Date now = new Date();
        builder.addAllActivity(getDisplayActList(list));
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        int dayiy = DateHelper.dayiy(beginTime, now);
        builder.setDay(dayiy);
        return builder.build();
    }

    /**
     * ???????????????????????????,???????????????????????????
     *
     * @param roleId
     * @param type
     * @return
     */
    public GetActivityRs getActivity(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, type);
        int now = TimeHelper.getCurrentSecond();
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:", type);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:", type);
        }
        LogUtil.debug("getActivity activity=" + activity);
        int activityKeyId = activityBase.getActivityId();
        GetActivityRs.Builder builder = GetActivityRs.newBuilder();
        int state = 0;
        if (activity.getOpen() == ActivityConst.OPEN_STEP) {
            state = activityDataManager.currentActivity(player, activity, 0, now);
        }
        // ????????????????????????
        if (ActivityConst.ACT_COMMAND_LV == activity.getActivityType()
                || ActivityConst.ACT_LEVEL == activity.getActivityType()
                || ActivityConst.ACT_ALL_CHARGE == activity.getActivityType()
                || ActivityConst.ACT_BRAVEST_ARMY == activity.getActivityType()
                || ActivityConst.ACT_GIFT_PAY == activity.getActivityType()
                || ActivityConst.ACT_COST_GOLD == activity.getActivityType()
                || ActivityConst.ACT_GIFT_OL == activity.getActivityType()
                || ActivityConst.ACT_DAILY_PAY == activity.getActivityType()
                || ActivityConst.ACT_ONLINE_GIFT == activity.getActivityType()
                || ActivityConst.ACT_FREE_LUXURY_GIFTS == activity.getActivityType()
                || ActivityConst.ACT_PAY_7DAY == activity.getActivityType()
                || ActivityConst.ACT_CHARGE_TOTAL == activity.getActivityType()
                || ActivityConst.ACT_CHARGE_CONTINUE == activity.getActivityType()
                || ActivityConst.ACT_MERGE_CHARGE_CONTINUE == activity.getActivityType()
                || ActivityConst.ACT_WAR_ROAD == activity.getActivityType()
                || ActivityConst.ACT_WAR_ROAD_DAILY == activity.getActivityType()
                || ActivityConst.ACT_SHARE_REWARD == activity.getActivityType()
                || ActivityConst.ACT_BIG_KILL == activity.getActivityType()
                || ActivityConst.ACT_BUILD_GIFT == activity.getActivityType()
        ) {
            // ??????????????????????????????????????????????????????????????????
            int schedule = activityDataManager.currentActivity(player, activity, 0, now);
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            condList = filterAwardByOLAct(player, activity, condList);
            if (condList != null) {
                for (StaticActAward e : condList) {
                    int keyId = e.getKeyId();
                    int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                    if (ActivityConst.ACT_GIFT_OL == activity.getActivityType()
                            || ActivityConst.ACT_ONLINE_GIFT == activity.getActivityType() && status == 0) {
                        schedule = e.getCond() > schedule ? e.getCond() - schedule + now : now;
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, schedule));
                        checkNextPhaseAward(builder, e, condList);
                        break;
                    } else if (ActivityConst.ACT_DAILY_PAY == activity.getActivityType()) {
                        if (e.getParam().get(0) <= 0) {// ???????????? ????????????
                            builder.addActivityCond(PbHelper.createActivityCondPb(e, status, 1));
                        } else {
                            builder.addActivityCond(PbHelper.createActivityCondPb(e, activityDataManager
                                    .caluActCailyPayStatus(activityBase.getActivityId(), e, activity), schedule));
                        }
                    } else if (ActivityConst.ACT_FREE_LUXURY_GIFTS == activity.getActivityType()) {
                        int time = DateHelper.afterDayTime(player.account.getCreateDate(), e.getCond());
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, time));
                    } else if (type == ActivityConst.ACT_CHARGE_CONTINUE
                            || ActivityConst.ACT_MERGE_CHARGE_CONTINUE == activity.getActivityType()) {
                        Integer s = activity.getStatusMap().get(keyId);
                        status = s == null ? 0 : s;
                        int day = e.getParam().get(0);
                        int chargeGold = activity.getSaveMap().get(day) == null ? 0 : activity.getSaveMap().get(day);
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, chargeGold));// ??????????????????:???
                    } else {
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, schedule));
                    }
                }
            }
            if (type == ActivityConst.ACT_ALL_CHARGE || type == ActivityConst.ACT_BRAVEST_ARMY) {// ????????????
                // 3??????????????????
                GlobalActivityData globalActivityData = activityDataManager
                        .getGlobalActivity(activity.getActivityType());
                if (globalActivityData != null) {
                    builder.addParam(globalActivityData.getTopupa().intValue());
                    builder.addParam(globalActivityData.getTopupb().intValue());
                    builder.addParam(globalActivityData.getTopupc().intValue());
                } else {
                    builder.addParam(0);
                    builder.addParam(0);
                    builder.addParam(0);
                }
                builder.addParam(activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                        : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD));
            } else if (type == ActivityConst.ACT_LEVEL) { // ????????????
                Long val = activity.getStatusCnt().get(0);
                state = val != null ? val.intValue() : 0;
            } else if (type == ActivityConst.ACT_CHARGE_CONTINUE
                    || type == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {
                builder.addParam(activityBase.getDayiyBegin());// ??????????????????:???
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_VIP
                || ActivityConst.ACT_ATTACK_CITY == activity.getActivityType()
                || ActivityConst.ACT_CHALLENGE_COMBAT == activity.getActivityType()
                || ActivityConst.ACT_TRAINED_SOLDIERS == activity.getActivityType()
                || ActivityConst.ACT_TRAINED_SOLDIERS_DAILY == activity.getActivityType()
                || ActivityConst.ACT_EQUIP_MATERIAL == activity.getActivityType()
                || ActivityConst.ACT_ELIMINATE_BANDIT == activity.getActivityType()
        ) {// ???????????? ???????????? ???????????? ???????????? ????????????
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            if (condList != null) {
                for (StaticActAward saa : condList) {
                    int schedule = activityDataManager.currentActivity(player, activity, saa.getParam().get(0));
                    int keyId = saa.getKeyId();
                    int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                    builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
                }
            }
        } else if (ActivityConst.ACT_LOGIN_EVERYDAY == activity.getActivityType()) {// ??????????????????
            Long lv = activity.getStatusCnt().get(0);
            if (lv != null) {
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                int lvInt = lv.intValue();
                for (StaticActAward saa : condList) {
                    List<Integer> lvRegion = saa.getParam();
                    if (lvRegion != null && lvRegion.size() > 1 && lvRegion.get(0) <= lvInt
                            && lvRegion.get(1) >= lvInt) {
                        int keyId = saa.getKeyId();
                        int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                        builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, 1));
                        break;
                    }
                }
            }
            state = 1;
        } else if (ActivityConst.ACT_ATK_GESTAPO == activity.getActivityType()
                || ActivityConst.FAMOUS_GENERAL_TURNPLATE == activity.getActivityType()) {
            List<StaticActExchange> exchangeList = StaticActivityDataMgr.getActExchangeListById(activityKeyId);
            int schedule = activityDataManager.currentActivity(player, activity, 0);
            if (!CheckNull.isEmpty(exchangeList)) {
                for (StaticActExchange exchange : exchangeList) {
                    Integer keyId = exchange.getKeyId();
                    // int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                    // ????????????
                    int status = activity.getStatusCnt().containsKey(keyId)
                            ? new Long(activity.getStatusCnt().get(keyId)).intValue() : 0;
                    builder.addActivityCond(PbHelper.createActivityCondPb(exchange, status, schedule));
                }
            }
        } else if (ActivityConst.ACT_PROP_PROMOTION == activity.getActivityType()) {
            // 3????????????????????????
            GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activity.getActivityType());
            if (!CheckNull.isNull(globalActivity)) {
                long integral = globalActivity.getCampValByCamp(player.lord.getCamp());
                for (StaticPromotion promotion : StaticActivityDataMgr
                        .getStaticPromotionListByActId(activity.getActivityId())) {
                    int keyId = promotion.getPromotionId();
                    // ????????????????????????
                    int cnt = activity.getStatusCnt().containsKey(keyId)
                            ? new Long(activity.getStatusCnt().get(keyId)).intValue() : 0;
                    int status = cnt > 0 ? 1 : 0;
                    builder.addActivityCond(PbHelper.createActivityCondPb(promotion, status, cnt));
                }
                // ?????????
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                for (StaticActAward sAward : condList) {
                    int keyId = sAward.getKeyId();
                    int awardCnt = activityDataManager.getAwardCnt(sAward, activity, integral); // ???????????????
                    builder.addActivityCond(PbHelper.createActivityCondPb(sAward, -1, awardCnt));
                }
                builder.addParam(new Long(integral).intValue()); // ????????????
            }
        } else if (ActivityConst.ACT_FIRSH_CHARGE == activity.getActivityType()) {// ????????????
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            for (StaticActAward saa : condList) {
                int status = activity.getStatusMap().containsKey(saa.getKeyId()) ? 1 : 0;
                int schedule = activityDataManager.currentActivity(player, activity, saa.getSortId());
                builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
            }
        } else if (ActivityConst.ACT_GIFT_PROMOTION == activity.getActivityType()) { // ????????????
            if (activityHelpService.getGiftPromotionDate() == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                        type);
            }
            for (StaticPromotion promotion : StaticActivityDataMgr
                    .getStaticPromotionListByActId(activity.getActivityId())) {
                int keyId = promotion.getPromotionId();
                // ????????????????????????
                int cnt = activity.getStatusCnt().containsKey(keyId)
                        ? activity.getStatusCnt().get(keyId).intValue() : 0;
                int status = cnt > 0 ? 1 : 0;
                builder.addActivityCond(PbHelper.createActivityCondPb(promotion, status, cnt));
            }
        } else if (ActivityConst.ACT_MERGE_PROP_PROMOTION == activity.getActivityType()) {
            activityMergePromotionService.buildActivity(builder, player, activity);
        } else if (ActivityConst.ACT_CAMP_RANK == activity.getActivityType()) {// ??????????????????

        } else if (ActivityConst.ACT_WISHING_WELL == activity.getActivityType()) { // ???????????????
            List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            int curCnt = activity.getStatusCnt()
                    .getOrDefault(ActivityConst.ActWishingWellKey.STATUSCNT_WISHING_CUR_CNT_KEY, 0L).intValue();
            for (StaticActAward saa : actAwardList) {
                int status = !CheckNull.isEmpty(saa.getParam()) && saa.getParam().size() >= 3
                        && saa.getParam().get(0) <= curCnt ? 1 : 0;
                builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, 0));
            }
        }
        // ??????????????????????????????
        else if (ActivityConst.isExchangePropAct(activity.getActivityType())) {
            List<StaticActExchange> exchanges = StaticActivityDataMgr.getActExchangeListById(activityKeyId);
            exchanges.forEach(e -> {
                builder.addActivityCond(
                        PbHelper.createActivityCondPb(
                                e,
                                activity.getStatusCnt().containsKey(e.getKeyId())
                                        ? activity.getStatusCnt().get(e.getKeyId()).intValue() : 0,
                                0
                        )
                );
            });
            // ?????????????????????????????????
            if (activity.getActivityType() == ActivityConst.ACT_BANDIT_AWARD) {
                builder.addParam(activityDataManager.getActHitAwardCnt(player, activity.getActivityType()));
            }
        }
        // ??????????????????
        else if (ActivityConst.ACT_WAR_PLANE_SEARCH == activity.getActivityType()) {
            List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            actAwardList.forEach(saa -> {
                int status = activity.getStatusMap().containsKey(saa.getKeyId()) ? 1 : 0;
                int schedule = activityDataManager.getWarPlaneSearchSchedule(saa, player, activity);
                builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
            });
        } else if (ActivityConst.ACT_COLLECT_RESOURCES == activity.getActivityType() || ActivityConst.ACT_RESOUCE_SUB == activity.getActivityType()) {
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            if (condList != null) {
                for (StaticActAward saa : condList) {
                    int key = saa.getParam().isEmpty() ? 0 : saa.getParam().get(0) * 10000 + saa.getParam().get(1);
                    int schedule = activityDataManager.currentActivity(player, activity, key);
                    int keyId = saa.getKeyId();
                    int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                    builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
                }
            }
        } else if (ActivityConst.ACT_EASTER == activity.getActivityType()) {
            Map<Integer, List<StaticEasterAward>> condAwardMap = StaticActivityDataMgr.getEasterAwardCondMap(activityKeyId);
            if (!CheckNull.isEmpty(condAwardMap)) {
                // ???????????????
                int schdeule = activityDataManager.currentActivity(player, activity, 0);
                // ????????????
                Map<Integer, Integer> statusMap = activity.getStatusMap();
                condAwardMap.forEach((key, value) -> {
                    int cond = key;
                    // ?????????????????????
                    int actAward = 0;
                    // ??????keyId
                    List<Integer> listSort = value.stream().map(StaticEasterAward::getKeyId).collect(Collectors.toList());
                    // ????????????
                    Collections.shuffle(listSort, new Random(roleId + activityKeyId));
                    List<StaticEasterAward> easterAwardList = listSort
                            .stream()
                            .map(StaticActivityDataMgr::getEasterAwardListByKey)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    for (StaticEasterAward sea : easterAwardList) {
                        // ????????????????????????
                        int status = statusMap.containsKey(sea.getKeyId()) ? 1 : 0;
                        if (status == 1) {
                            ++actAward;
                        }
                        builder.addActivityCond(PbHelper.createActivityCondPb(sea, status, schdeule));
                    }
                    // ??????????????????
                    StaticEasterAward staticEA = value.get(0);
                    CommonPb.ActivityCond.Builder acBuilder = CommonPb.ActivityCond.newBuilder();
                    acBuilder.setKeyId(cond);
                    acBuilder.setStatus(statusMap.containsKey(cond) ? 1 : 0);
                    acBuilder.setState(actAward);
                    acBuilder.addAllAward(PbHelper.createAwardsPb(staticEA.getExtra()));
                    builder.addActivityCond(acBuilder.build());
                });
            }
        } else if (ActivityConst.ACT_REAL_NAME == activity.getActivityType()
                || ActivityConst.ACT_PHONE_BINDING == activity.getActivityType()) {
            //?????????????????????????????????????????????????????????????????????????????????
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            for (StaticActAward saa : condList) {
                int keyId = saa.getKeyId();
                state = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                break;
            }
        } else if (ActivityConst.ACT_HOT_PRODUCT == activity.getActivityType()) {
            activityHotProductService.getActivity(player, activity, builder);
        } else if (ActivityConst.ACT_GOOD_LUCK == activity.getActivityType()) {
            activityLotteryService.getGoodLuckActivity(player, activity, builder);
        } else if (activity.getActivityType() == ActivityConst.ACT_CHRISTMAS
                || activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE) {
            List<StaticActExchange> exchangeList = StaticActivityDataMgr.getActExchangeListById(activityKeyId);
            int schedule = activityDataManager.currentActivity(player, activity, 0);
            if (!CheckNull.isEmpty(exchangeList)) {
                for (StaticActExchange exchange : exchangeList) {
                    Integer keyId = exchange.getKeyId();
                    // ???????????????
                    int status = activity.getStatusCnt().containsKey(keyId) ? new Long(activity.getStatusCnt().get(keyId)).intValue() : 0;
                    builder.addActivityCond(PbHelper.createActivityCondPb(exchange, status, schedule));
                }
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_MONSTER_NIAN) {
            state = activityMonsterNianService.buildActivity(builder, activityBase, activity);
        } else if (activity.getActivityType() == ActivityConst.ACT_ANNIVERSARY_FIREWORK) {
            state = activityFireWorkService.buildActivity(builder, activityBase, activity);
        }
        builder.setState(state);
        return builder.build();
    }

    /**
     * Type:301. ??????????????????????????????????????????????????????
     *
     * @param player
     * @param activity
     * @param condList
     * @return
     */
    private List<StaticActAward> filterAwardByOLAct(Player player, Activity activity, List<StaticActAward> condList) {
        if (ActivityConst.ACT_ONLINE_GIFT == activity.getActivityType()) {
            int[] params = new int[2];
            // ???????????????
            /*if (CheckNull.isEmpty(activity.getStatusMap())) {
                params[0] = player.lord.getLevel();
                params[1] = player.lord.getLevel();
            } else {
                Integer keyId = activity.getStatusMap().keySet().stream().findFirst().orElse(null);
                if (!CheckNull.isNull(keyId)) {
                    StaticActAward staticActAward = condList.stream().filter(saa -> saa.getStaticKey() == keyId).findFirst()
                            .orElse(null);
                    if (!CheckNull.isNull(staticActAward)) {
                        params[0] = staticActAward.getParam().get(0);
                        params[1] = staticActAward.getParam().get(1);
                    }
                }
            }*/
            // ??????????????????????????????,?????????propMap
            if (CheckNull.isEmpty(activity.getPropMap())) {
                activity.getPropMap().put(0, player.lord.getLevel());
            }
            if (!CheckNull.isEmpty(activity.getPropMap())) {// ?????????????????????????????????
                params[0] = activity.getPropMap().get(0);
                params[1] = activity.getPropMap().get(0);
            } else if (CheckNull.isEmpty(activity.getStatusMap())) {// ??????????????????????????????
                params[0] = player.lord.getLevel();
                params[1] = player.lord.getLevel();
            } else {// ????????????????????????
                Integer keyId = activity.getStatusMap().keySet().stream().findFirst().orElse(null);
                if (!CheckNull.isNull(keyId)) {
                    StaticActAward staticActAward = condList.stream().filter(saa -> saa.getKeyId() == keyId).findFirst()
                            .orElse(null);
                    if (!CheckNull.isNull(staticActAward)) {
                        params[0] = staticActAward.getParam().get(0);
                        params[1] = staticActAward.getParam().get(1);
                    }
                }
            }
            condList = condList.stream()
                    .filter(e -> params[0] >= e.getParam().get(0) && params[1] <= e.getParam().get(1))
                    .collect(Collectors.toList());
        }
        return condList;
    }

    /**
     * ?????????????????????
     *
     * @param builder
     * @param curSaa
     * @param condList
     */
    private void checkNextPhaseAward(GetActivityRs.Builder builder, StaticActAward curSaa,
                                     List<StaticActAward> condList) {
        final int nextSortId = curSaa.getSortId() + 1;
        StaticActAward nextAward = condList.stream().filter(saa -> saa.getSortId() == nextSortId)// ???????????????
                .findFirst().orElse(null);
        if (!CheckNull.isNull(nextAward)) {
            builder.addActivityCond(PbHelper.createActivityCondPb(nextAward, 2, 0));
        }
    }

    /**
     * ?????????????????????
     *
     * @param builder
     * @param curSaa
     * @param condList
     */
    private void checkNextPhaseAward(GetOnLineAwardRs.Builder builder, StaticActAward curSaa,
                                     List<StaticActAward> condList) {
        final int nextSortId = curSaa.getSortId() + 1;
        StaticActAward nextAward = condList.stream().filter(saa -> saa.getSortId() == nextSortId)// ???????????????
                .findFirst().orElse(null);
        if (!CheckNull.isNull(nextAward)) {
            builder.addActivityCond(PbHelper.createActivityCondPb(nextAward, 2, 0));
        }
    }

    /**
     * @param actId
     * @param curSaa
     * @param act
     * @return 0?????? 1?????? 2?????????
     */
    /*private int caluActCailyPayStatus(int actId, StaticActAward curSaa, Activity act) {
        if (act.getStatusMap().containsKey(curSaa.getKeyId())) {// ?????????
            return 1;
        } else {// ?????????,
            List<StaticActAward> collect = StaticActivityDataMgr.getDailyPayAward(actId);
            final int preParam = curSaa.getParam().get(0) - 1;
            StaticActAward preParamSaa = collect.stream().filter(ssa -> ssa.getParam().get(0) == preParam)// ???????????????
                    .findFirst().orElse(null);
            if (preParamSaa == null) {// ???????????????????????????
                return 0;
            } else {
                if (!act.getStatusMap().containsKey(preParamSaa.getKeyId())) {// ??????????????????????????????????????????
                    return 2;
                } else {// ??????????????????
                    Integer preDay = act.getStatusMap().get(preParamSaa.getKeyId());
                    int currentDay = TimeHelper.getCurrentDay();
                    if (preDay == currentDay) {// ????????????????????????
                        return 2;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }*/


    /**
     * ???????????????????????????
     *
     * @param roleId ??????id
     * @param type   ????????????
     * @param keyId  ??????key
     * @return ????????????
     * @throws MwException ???????????????
     */
    public GetEasterActAwardRs getEasterActAward(long roleId, int type, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ????????????
        Activity activity = player.activitys.get(ActivityConst.ACT_EASTER);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_EASTER);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        // ??????id
        int activityId = activity.getActivityId();
        // ????????????
        List<StaticEasterAward> easterAwardList = StaticActivityDataMgr.getEasterAwardList(activityId);
        if (CheckNull.isEmpty(easterAwardList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
        }

        List<List<Integer>> awardList;
        if (type == 1) {
            // ??????
            StaticEasterAward sEasterAward = StaticActivityDataMgr.getEasterAwardListByKey(keyId);
            if (sEasterAward == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId, ", keyId:", keyId);
            }
            Integer awardStatus = activity.getStatusMap().get(keyId);
            if (awardStatus != null && awardStatus != 0) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????????, roleId:,", roleId, ", keyId:", keyId);
            }
            int status = activityDataManager.currentActivity(player, activity, 0);
            // ??????????????????????????????
            int recharge = sEasterAward.getRecharge();
            if (status < recharge) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
            }
            // ??????????????????
            int cond = sEasterAward.getParam().get(0);
            if (Stream.iterate(1, i -> ++i).limit(cond - 1).anyMatch(condition -> checkEasterAward(condition, easterAwardList, activity))) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
            }
            // ???????????????????????????
            activityDataManager.updActivity(player, ActivityConst.ACT_EASTER, recharge, 1, false);
            // ??????????????????
            activity.getStatusMap().put(keyId, 1);
            // ????????????
            awardList = sEasterAward.getAwardList();
        } else {
            Integer awardStatus = activity.getStatusMap().get(activityId);
            if (awardStatus != null && awardStatus != 0) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????????, roleId:,", roleId, ", activityId:", activityId);
            }
            // ????????????????????????
            if (checkEasterAward(keyId, easterAwardList, activity)) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
            }
            StaticEasterAward staticEA = easterAwardList.stream().filter(sea -> sea.getParam().get(0) == keyId).findAny().orElse(null);
            if (Objects.isNull(staticEA)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId, ", keyId:", keyId);
            }
            // ?????????????????????
            int actAward = Math.toIntExact(easterAwardList.stream().filter(sea -> sea.getParam().get(0) == keyId && activity.getStatusMap().containsKey(sea.getKeyId())).count());
            if (actAward < staticEA.getProgress()) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
            }
            // ??????????????????
            activity.getStatusMap().put(keyId, 1);
            // ????????????
            awardList = staticEA.getExtra();
        }

        GetEasterActAwardRs.Builder builder = GetEasterActAwardRs.newBuilder();
        if (!CheckNull.isEmpty(awardList)) {
            for (List<Integer> e : awardList) {
                int awardType = e.get(0);
                int itemId = e.get(1);
                int count = e.get(2);
                int itemkey = rewardDataManager.addAward(player, awardType, itemId, count, AwardFrom.ACTIVITY_AWARD, keyId,
                        activityId, activity.getActivityType());
                builder.addAward(PbHelper.createAwardPb(awardType, itemId, count, itemkey));
            }
        }
        return builder.build();
    }


    /**
     * ??????????????????????????????, ??????????????????, ???????????????
     *
     * @param condition ??????
     * @return true
     */
    private boolean checkEasterAward(int condition, List<StaticEasterAward> easterAwards, Activity activity) {
        if (!CheckNull.isEmpty(easterAwards)) {
            easterAwards = easterAwards.stream().filter(sea -> sea.getParam().get(0) == condition).collect(Collectors.toList());
            if (!CheckNull.isEmpty(easterAwards)) {
                // ??????????????????
                Map<Integer, Integer> statusMap = activity.getStatusMap();
                return !easterAwards.stream().allMatch(sea -> statusMap.containsKey(sea.getKeyId()));
            }
        }
        return false;
    }

    /**
     * ????????????
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetActivityAwardRs getActivityAward(GetActivityAwardRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int activityType = req.getActivityType();// ??????????????????
        int keyId = req.getKeyId();// ????????????id

        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);// ????????????????????????Award
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
        }

        Activity activity = player.activitys.get(activityType);// ??????????????????????????????
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        if (activityType == ActivityConst.ACT_NEWYEAR_2022_FISH) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "???????????? ???????????????????????????", activityType));
        }

        // if (!activityBase.isReceiveAwardTime()) {// ?????????????????????
        // throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "?????????????????????????????????, roleId:,", roleId);
        // }

        Integer awardStatus = activity.getStatusMap().get(keyId);
        if (awardStatus != null && awardStatus != 0) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????????, roleId:,", roleId);
        }

        List<List<Integer>> awardList = actAward.getAwardList();
        // ????????????????????????
        rewardDataManager.checkBag(player, awardList);

        int sortId = actAward.getSortId();
        if (activityType == ActivityConst.ACT_VIP || activityType == ActivityConst.ACT_ATTACK_CITY
                || activityType == ActivityConst.ACT_CHALLENGE_COMBAT
                || activityType == ActivityConst.ACT_TRAINED_SOLDIERS
                || activityType == ActivityConst.ACT_TRAINED_SOLDIERS_DAILY
                || activityType == ActivityConst.ACT_EQUIP_MATERIAL
                || activityType == ActivityConst.ACT_ELIMINATE_BANDIT
        ) {// ????????????,????????????, ????????????
            sortId = actAward.getParam().get(0); // vip??????
        }
        if (activityType == ActivityConst.ACT_COLLECT_RESOURCES || activityType == ActivityConst.ACT_RESOUCE_SUB) {
            sortId = actAward.getParam().isEmpty() ? 0 : actAward.getParam().get(0) * 10000 + actAward.getParam().get(1);
        }
        int status = activityDataManager.currentActivity(player, activity, sortId);
        if (activityType == ActivityConst.ACT_CHARGE_CONTINUE
                || activityType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {
            // ???????????????????????????????????????????????????????????????????????????????????????
            status = activityDataManager.getChargeContinueStatValueByAward(player, activity, actAward, sortId,
                    TimeHelper.getCurrentSecond());
        } else if (activityType == ActivityConst.ACT_WAR_PLANE_SEARCH) {
            status = activityDataManager.getWarPlaneSearchSchedule(actAward, player, activity);
        } else if (activityType == ActivityConst.ACT_ANNIVERSARY_FIREWORK) {
            status = activityFireWorkService.checkDrawSubActivityAward(player, activity, activityBase, actAward);
        }
        if (status == 0 && activityType != ActivityConst.ACT_DAILY_PAY
                && activityType != ActivityConst.ACT_PROP_PROMOTION) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
        }
        LogUtil.debug("GetActivityAwardRs activity=" + activity);

        // List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        int awardCnt = 1;

        // int serverId = player.account.getServerId();
        // ??????????????????(??????????????????)
        if (activityType == ActivityConst.ACT_GIFT_PAY) {
            if (status < actAward.getCond()) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
            }
            activity.getStatusMap().put(keyId, 1);
        } else if (activityType == ActivityConst.ACT_LOGIN_EVERYDAY) {// ??????????????????
            if (!activity.getStatusMap().isEmpty()) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????? ???????????????, roleId:,", roleId);
            }
            activity.getStatusMap().put(keyId, 1);
        } else if (activityType == ActivityConst.ACT_DAY_DISCOUNTS) {// ??????????????????????????????
            int freeKey = 0;
            if (activity.getStatusMap().containsKey(freeKey)) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "?????????????????? ???????????????, roleId:,", roleId);
            }
            int lvKey = 1;
            Long saveLv = activity.getStatusCnt().get(lvKey);
            if (saveLv == null) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
            }
            int actLv = saveLv.intValue();
            if (!(actAward.getParam().get(0) <= actLv && actLv <= actAward.getParam().get(1))) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "??????????????????????????????, roleId:,", roleId);
            }
            activity.getStatusMap().put(freeKey, 1);
        } else if (activityType == ActivityConst.ACT_THREE_REBATE) { // ??????????????????
            player.activitys.get(ActivityConst.ACT_THREE_REBATE).getStatusMap().put(1, 0);
        } else {
            if (activityType == ActivityConst.ACT_LEVEL) { // ????????????
                // ????????????V4
                Long isBuy = activity.getStatusCnt().get(0);
                if (isBuy == null || isBuy == 0) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), " ????????????????????? roleId:", roleId);
                }
                if (actAward.getCond() > player.lord.getLevel()) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "??????????????????????????? roleId:", roleId);
                }
                activity.getStatusMap().put(keyId, 1);
            } else if (StaticActivityDataMgr.isActTypeRank(activityType)) {// ????????????
                status = activityDataManager.getRankAwardSchedule(player, activityType);
                if (StaticActivityDataMgr.isOnlyRankAward(activityType)) {
                    // ?????????????????????????????????
                    // StaticActAward myAward = StaticActivityDataMgr.findRankAward(activityType, status);
                    if (actAward.getParam().get(1) > status || status > actAward.getCond()) {
                        throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "?????????????????????????????????????????????, roleId:,",
                                roleId, ", myRank:", status, ", needRank:", actAward.getCond());
                    }
                    // ??????????????????
                    if (!activityBase.isReceiveAwardTime()) {// ?????????????????????
                        throw new MwException(GameError.ATK_ACT_TIME.getCode(), "?????????????????????????????????, roleId:,", roleId);
                    }
                }
                // ??????, ???????????????????????????, ???????????????end-award?????????????????????
                if (ActivityConst.ACT_PAY_RANK_NEW == activityType
                        || ActivityConst.ACT_PAY_RANK_V_3 == activityType
                        || ActivityConst.ACT_MERGE_PAY_RANK == activityType
                        || ActivityConst.ACT_CONSUME_GOLD_RANK == activityType
                        || ActivityConst.ACT_TUTOR_RANK == activityType) {// ???
                    // ????????????
                    // ?????????????????????
                    // ??????????????????
                    int open = activityBase.getBaseOpen();
                    if (open != ActivityConst.OPEN_AWARD && open != ActivityConst.OPEN_STEP) {
                        throw new MwException(GameError.ATK_ACT_TIME.getCode(), "?????????????????????????????????, roleId:,", roleId);
                    }
                    if (status < actAward.getParam().get(1)) {// ????????????????????????
                        throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "?????????????????????????????????????????????, roleId:,",
                                roleId, ", myRank:", status, ", startRank:", actAward.getParam().get(1), ", endRank:",
                                actAward.getCond());
                    }
                }
                if (status <= 0 || status > actAward.getCond()) {// ????????????????????????
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "??????????????????????????????, roleId:,", roleId,
                            ", myRank:", status, ", needRank:", actAward.getCond());
                }
                activity.getStatusMap().put(keyId, 1);
            } else if (activityType == ActivityConst.ACT_DAILY_PAY) { // ????????????
                List<StaticActAward> collect = StaticActivityDataMgr.getDailyPayAward(activityBase.getActivityId());
                for (StaticActAward staticActAward : collect) {
                    if (CheckNull.isEmpty(actAward.getParam())) {
                        continue;
                    }
                    if (staticActAward.getParam().get(0) < actAward.getParam().get(0)) {
                        if (!activity.getStatusMap().containsKey(staticActAward.getKeyId())) {
                            throw new MwException(GameError.SUPER_AWARD_NOT_OPEN.getCode(), "?????????????????????, roleId:,",
                                    roleId);
                        }
                    }
                }
                int currentDay = TimeHelper.getCurrentDay();
                for (Integer day : activity.getStatusMap().values()) {
                    if (day == currentDay && actAward.getParam().get(0) != 0) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????,??????????????? roleId:", roleId);
                    }
                }
                int val = actAward.getParam().get(0) == 0 ? 1 : currentDay;
                activity.getStatusMap().put(keyId, val);
            } else if (activityType == ActivityConst.ACT_PROP_PROMOTION) { // ??????????????????
                // ???????????????
                int schedule = activity.getSaveMap().containsKey(keyId) ? activity.getSaveMap().get(keyId) : 0;
                if (schedule >= actAward.getCond()) {
                    throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), " ?????????????????????????????????, roleId:", roleId,
                            ", cnt:", schedule);
                }
                // 3????????????????????????
                GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activity.getActivityType());
                if (!CheckNull.isNull(globalActivity)) {
                    long val = globalActivity.getCampValByCamp(player.lord.getCamp()); // ????????????????????????
                    int ceil = (int) Math.ceil(val / ActParamConstant.ACT_PROP_PROMOTION_AWARD_NUM);
                    int cnt = ceil >= actAward.getCond() ? actAward.getCond() : ceil;
                    awardCnt = cnt - schedule; // ????????????
                    if (awardCnt < 1) {
                        throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "???????????????????????????, roleId:", roleId,
                                ", cnt:", awardCnt);
                    }
                    activity.getSaveMap().put(keyId, schedule + awardCnt);
                }
            } else {// ????????????
                if ((activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY)
                        && player.lord.getLevel() < ActParamConstant.ACT_ALL_CHARGE_LORD_LV) {// ????????????/???????????????????????????
                    throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "??????????????????????????????, ???????????????, roleId:,", roleId);
                }
                if ((activityType == ActivityConst.ACT_VIP)
                        && player.lord.getLevel() < ActParamConstant.ACT_VIP_LORD_LV) {// ???????????????????????????
                    throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "??????????????????????????????, ???????????????, roleId:,", roleId);
                }
                if (actAward.getCond() <= 0 || status < actAward.getCond()) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
                }
                if (activityType == ActivityConst.ACT_PAY_7DAY) {// 7?????????
                    int cRoleDay = playerDataManager.getCreateRoleDay(player, new Date());
                    if (cRoleDay > 7) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
                    }
                }

                if (activityType == ActivityConst.ACT_CHARGE_TOTAL) {// ????????????
                    if (activityBase.getStep() == ActivityConst.OPEN_CLOSE) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
                    }
                }

                if (activityType == ActivityConst.ACT_CHARGE_CONTINUE
                        || activityType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {// ????????????
                    if (activityBase.getStep() == ActivityConst.OPEN_CLOSE) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
                    }

                    Integer s = activity.getStatusMap().get(actAward.getKeyId());
                    if (s != null && s == 1) {
                        throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "??????????????????, roleId:,", roleId);
                    }
                    int day = actAward.getParam().get(0);
                    int chargeGold = activity.getSaveMap().get(day) == null ? 0 : activity.getSaveMap().get(day);
                    if (chargeGold < actAward.getCond()) {
                        throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
                    }
                }

                activity.getStatusMap().put(keyId, 1);
            }
        }
        GetActivityAwardRs.Builder builder = GetActivityAwardRs.newBuilder();

        int size = awardList.size();
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        for (int i = 0; i < size; i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// ????????????
            count *= awardCnt;// ????????????
            if (type == AwardType.EQUIP) {
                for (int c = 0; c < count; c++) {
                    int itemkey = rewardDataManager.addAward(player, type, itemId, 1, AwardFrom.ACTIVITY_AWARD, keyId,
                            activity.getActivityId(), activity.getActivityType());
                    builder.addAward(PbHelper.createAwardPb(type, itemId, 1, itemkey));
                }
            } else {

                // ?????? ?????? ????????? ?????? ??????????????????
                if (activityType == ActivityConst.ACT_FIRSH_CHARGE && type == AwardType.HERO
                        && itemId == HeroConstant.GQS_HERO_ID) {
                    chatDataManager.sendSysChat(ChatConst.CHAT_FIRST_FLUSH, player.lord.getCamp(), 0,
                            player.lord.getCamp(), player.lord.getNick(), itemId);
                }
                if (ActivityConst.ACT_BUILD_GIFT == activityType && type == AwardType.PROP) {
                    StaticProp staticProp = StaticPropDataMgr.getPropMap(itemId);
                    if (staticProp != null) {
                        propService.processEffect(player, staticProp);
                        propService.syncBuffRs(player, player.getEffect().get(EffectConstant.BUILD_CNT));
                        activityDataManager.updActivity(player, ActivityConst.ACT_BUILD_GIFT, 1, 0, true);
                        // ?????????????????????????????????
                        activity.getSaveMap().put(1, TimeHelper.getCurrentSecond());
                    }
                } else {
                    int itemkey = rewardDataManager.addAward(player, type, itemId, count, AwardFrom.ACTIVITY_AWARD, keyId,
                            activity.getActivityId(), activity.getActivityType());
                    builder.addAward(PbHelper.createAwardPb(type, itemId, count, itemkey));
                }
            }
            // LogHelper.logActivity(player.lord, activityId, 0, type, itemId, count, serverId);
        }
        return builder.build();
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param activityBase
     * @param activity
     * @return
     */
    public boolean isAllGainActivity(Player player, ActivityBase activityBase, Activity activity) {
        if (activityBase.getStaticActivity().getIsDisappear() == 0) {// ??????????????????
            return false;
        }
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (activity.getActivityType() == ActivityConst.ACT_ONLINE_GIFT) {
            condList = filterAwardByOLAct(player, activity, condList);
        } else if (activity.getActivityType() == ActivityConst.ACT_ANNIVERSARY_DATE_GIFT) {
            StaticActExchange actExchange = StaticActivityDataMgr
                    .getActExchangeListByKeyId(activity.getActivityId());
            int num = actExchange.getNumberLimit();
            int val = activity.getStatusCnt().get(actExchange.getKeyId()).intValue();
            if (val >= num) {
                return true;
            }
        }
        if (CheckNull.isEmpty(condList)) {
            return false;
        }
        for (StaticActAward actAward : condList) {
            Integer awardStatus = statusMap.get(actAward.getKeyId());
            if (awardStatus == null || awardStatus != 1) {
                return false;
            }
        }
        return true;
    }

    // ===============================???????????????????????????
    // start========================================

    /**
     * ??????????????????
     *
     * @param roleId
     * @param keyId
     * @return
     * @throws MwException
     */
    public ExchangeActAwardRs exchangeActAward(Long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticActExchange actExchange = StaticActivityDataMgr.getActExchangeListByKeyId(keyId);
        if (CheckNull.isNull(actExchange)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId, "keyId=" + keyId);
        }

        Activity activity = activityDataManager.getActivityInfo(player, actExchange.getType());
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:", actExchange.getType());
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    actExchange.getType());
        }
        if (activityBase.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE &&
                activityBase.getBaseOpen() == ActivityConst.OPEN_AWARD) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????,??????????????? roleId:", roleId);
        }

        List<List<Integer>> awardList = actExchange.getAwardList();
        // ????????????????????????
        rewardDataManager.checkBag(player, awardList);

        // ?????????????????????????????? ???????????????
        if (awardList.get(0).get(0) == AwardType.HERO && checkAwardHasHero(awardList.get(0), player)) {
            throw new MwException(GameError.HERO_EXISTS.getCode(), "???????????????, roleId:,", roleId, ", heroId:",
                    awardList.get(0).get(1));
        }

        //???????????????????????????????????????
        if (awardList.get(0).get(0) == AwardType.CASTLE_SKIN && castleSkinService.checkSkinHaving(player, awardList.get(0).get(1))) {
            throw new MwException(GameError.EXCHANGE_SKIN_HAVING.getCode(), "?????????????????????, roleId:,", roleId, ", activityType=" + activity.getActivityType(), ", activityId=" + activity.getActivityId(), ", skinId=" + awardList.get(0).get(1));
        }

        List<Integer> prop = actExchange.getProp();
        if (ActivityConst.ACT_CHRISTMAS == activity.getActivityType() || ActivityConst.ACT_REPAIR_CASTLE == activity.getActivityType()) {

        } else {
            if (CheckNull.isNull(prop)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "??????????????????, roleId:", roleId, ", type:",
                        activity.getActivityId());
            }

            // ????????????????????????????????????
            if (ActivityConst.isExchangePropAct(activity.getActivityType())) {
                if (!rewardDataManager.checkPlayerResourceIsEnough(player, actExchange.getExpendProp())) {
                    throw new MwException(
                            GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(),
                            "????????????????????????, roleId:", roleId, ", need:",
                            actExchange.getExpendProp()
                    );
                }
            } else {
                int schedule = activityDataManager.currentActivity(player, activity, 0);
                if (schedule < prop.get(2)) {
                    throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(), "????????????????????????, roleId:", roleId, ", need:",
                            prop.get(2), ", have:", schedule);
                }
            }

            if (activity.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
                // ???????????????????????????????????????
                long playerPoint = activity.getStatusCnt().getOrDefault(0, 0l);
                if (playerPoint < actExchange.getNeedPoint()) {
                    throw new MwException(GameError.FAMOUS_GENERAL_TURNTABLE_EXCHANGE_NEED_POINT_NOT_ENOUGH, "????????????????????????????????????, roleId: ", roleId,
                            ", minScore: ", actExchange.getNeedPoint(), ", playerScore: ", playerPoint);
                }
            }
        }

        int cnt = activity.getStatusCnt().get(keyId) == null ? 0
                : new Long(activity.getStatusCnt().get(keyId)).intValue();
        if (cnt < 0 || actExchange.getNumberLimit() <= cnt) {
            throw new MwException(GameError.EXCHANGE_AWARD_MAX.getCode(), "??????????????????????????????, roleId:", roleId, ", max:",
                    actExchange.getNumberLimit(), ", cnt:", cnt);
        }
        if (player.lord.getLevel() < actExchange.getLvLimit()) {
            throw new MwException(GameError.EXCHANGE_AWARD_LEVEL_ERR.getCode(), "???????????????????????????, roleId:", roleId, ", min:",
                    actExchange.getLvLimit(), ", level:", player.lord.getLevel());
        }
        if (activity.getActivityType() == ActivityConst.ACT_CHRISTMAS || activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE) {
            activityChristmasService.checkAndSubScore4Exchange(player, activity, actExchange.getNeedPoint(), AwardFrom.ACT_CHRISTMAS_SCORE_EXCHANGE);
        } else {
            // ?????????????????????,????????????????????????
            rewardDataManager.checkAndSubPlayerRes(player, actExchange.getExpendProp(),
                    actExchange.getType() == ActivityConst.ACT_ATK_GESTAPO ? AwardFrom.EXCHANGE_GESTAPO_COST
                            : AwardFrom.EXCHANGE_FAMOUS_GENERAL_COST,
                    keyId);
        }
        // rewardDataManager.checkPropIsEnough(player, prop.get(1), prop.get(2),
        // "??????????????????");
        // rewardDataManager.subProp(player, prop.get(1), prop.get(2),
        // AwardFrom.EXCHANGE_GESTAPO_COST, "??????????????????");

        // ??????????????????
        activity.getStatusMap().put(keyId, 1);
        activity.getStatusCnt().put(keyId, Long.valueOf(cnt + 1));

        ExchangeActAwardRs.Builder builder = ExchangeActAwardRs.newBuilder();

        int size = awardList.size();
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        // ????????????
        AwardFrom awardFrom = AwardFrom.EXCHANGE_FAMOUS_GENERAL_COST;
        if (actExchange.getType() == ActivityConst.ACT_ATK_GESTAPO) {
            awardFrom = AwardFrom.EXCHANGE_GESTAPO_COST;
        } else if (ActivityConst.isExchangePropAct(activity.getActivityType())) {
            awardFrom = AwardFrom.EXCHANGE_ACTIVITY_COST;
        } else if (actExchange.getType() == ActivityConst.ACT_CHRISTMAS || actExchange.getType() == ActivityConst.ACT_REPAIR_CASTLE) {
            awardFrom = AwardFrom.ACT_CHRISTMAS_SCORE_EXCHANGE;
        }

        for (int i = 0; i < size; i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// ????????????
            if (type == AwardType.EQUIP) {
                for (int c = 0; c < count; c++) {
                    int itemkey = rewardDataManager.addAward(player, type, itemId, 1, awardFrom, keyId);
                    builder.addAward(PbHelper.createAwardPb(type, itemId, 1, itemkey));
                }
            } else {
                int itemkey = rewardDataManager.addAward(player, type, itemId, count, awardFrom, keyId);
                builder.addAward(PbHelper.createAwardPb(type, itemId, count, itemkey));
            }
        }
        // ???????????? ????????????
        if (activity.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
            activityDataManager.syncActChange(player, activity.getActivityType());
        }
        if (awardList.get(0).get(0).equals(AwardType.HERO)) {
            // ???????????????
            chatDataManager.sendSysChat(ChatConst.CHAT_FAMOUS_GENERAL_EXCHANGE_GLOBAL_NUM, player.lord.getCamp(), 0,
                    player.lord.getCamp(), player.lord.getNick(), awardList.get(0).get(0), awardList.get(0).get(1),
                    awardList.get(0).get(2), activity.getActivityId());
        }
        if (awardList.get(0).get(0) == AwardType.CASTLE_SKIN) {
            chatDataManager.sendSysChat(ChatConst.CHAT_EXCHANGE_SKIN, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), awardList.get(0).get(0), awardList.get(0).get(1), awardList.get(0).get(2), activity.getActivityId());
        }
        if (activity.getActivityType() == ActivityConst.ACT_CHRISTMAS || activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE) {
            builder.addParam(PbHelper.createTwoIntPb(activity.getActivityType(), activityChristmasService.getMyScore(activity)));
        }
        return builder.build();
    }

    // ===============================??????????????????????????? end========================================

    // ===============================????????????????????? start========================================

    /**
     * ????????????????????????
     *
     * @param roleId
     * @throws MwException
     */
    public GetAtkCityActRs getAtkCityAct(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Date now = new Date();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATTACK_CITY_NEW);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        if (activity.getOpen() == ActivityConst.OPEN_CLOSE) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        ActivityBase actBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATTACK_CITY_NEW);
        if (actBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        AtkCityAct cityAct = player.atkCityAct;
        List<CommonPb.AtkCityActActive> atkCityActActives = new ArrayList<>();
        List<CommonPb.AtkCityActTask> atkCityActTasks = new ArrayList<>();
        int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // ????????????????????????
        for (StaticAtkCityAct atkCityAct : StaticActivityDataMgr.getAtkCityActList()) {
            CommonPb.AtkCityActTask actTask = PbHelper.createAtkCityActTask(cityAct, atkCityAct,
                    activityDataManager.getCanRecvCnt(player, atkCityAct, cityAct));
            atkCityActTasks.add(actTask);
        }
        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        for (StaticActAward actAward : condList) {
            int recved = activityDataManager.caluActRecvedState(player, activity, actAward);
            CommonPb.AtkCityActActive actActive = PbHelper.createAtkCityActActive(actAward, recved);
            atkCityActActives.add(actActive);
        }
        GetAtkCityActRs.Builder builder = GetAtkCityActRs.newBuilder();
        int activie = activityDataManager.currentActivity(player, activity, 0);
        builder.setActice(activie);
        builder.addAllActActive(atkCityActActives);
        builder.addAllActTask(atkCityActTasks);
        builder.setDay(dayiy);

        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @throws MwException
     */
    public RecvActiveRs recvActive(Long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Date now = new Date();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATTACK_CITY_NEW);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        if (activity.getOpen() == ActivityConst.OPEN_CLOSE) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        if (player.lord.getLevel() < ActParamConstant.ACT_ATK_CITY_LEVEL.get(0)) {
            throw new MwException(GameError.TRIGGER_LEVEL_ERR.getCode(), "??????????????????, roleId:,", roleId);
        }
        ActivityBase actBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATTACK_CITY_NEW);
        if (actBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        AtkCityAct cityAct = player.atkCityAct;
        int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // ????????????????????????
        StaticAtkCityAct staticAtkCityAct = StaticActivityDataMgr.getAtkCityAct(keyId);
        if (CheckNull.isNull(staticAtkCityAct)) {
            throw new MwException(GameError.ATK_CONFIG_NOT_FOUND.getCode(), "?????????????????????, roleId:,", roleId, ", keyId:",
                    keyId);
        }
        if (dayiy < staticAtkCityAct.getDay()) {
            throw new MwException(GameError.ATK_ACT_TIME.getCode(), "??????????????????, roleId:,", roleId, ", keyId:", keyId);
        }
        int canRecvCnt = activityDataManager.getCanRecvCnt(player, staticAtkCityAct, cityAct);
        if (canRecvCnt == 0) {
            throw new MwException(GameError.ATK_NOT_AWARD.getCode(), "??????????????????,???????????????, roleId:,", roleId, ", keyId:",
                    keyId);
        }
        // ???????????????
        activityDataManager.recvActiveCnt(player, cityAct, staticAtkCityAct, canRecvCnt);
        RecvActiveRs.Builder builder = RecvActiveRs.newBuilder();
        int activie = activityDataManager.currentActivity(player, activity, 0);
        builder.setActice(activie);
        builder.setActTask(PbHelper.createAtkCityActTask(cityAct, staticAtkCityAct,
                activityDataManager.getCanRecvCnt(player, staticAtkCityAct, cityAct)));
        return builder.build();
    }

    // ===============================????????????????????? end========================================

    // ===============================????????????????????? start========================================

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetDay7ActRs getDay7ActRs(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Day7Act day7Act = player.day7Act;

        Date now = new Date();
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        int dayiy = DateHelper.dayiy(beginTime, now);

        int createServerId = player.account.getServerId();
        List<StaticDay7Act> staticDay7ActList = StaticActivityDataMgr.getAct7DayMap().values().stream().filter(sd7c -> sd7c.checkServerPlan(createServerId)).collect(Collectors.toList());
        List<CommonPb.Day7Act> listDay7Act = new ArrayList<>();
        if (staticDay7ActList.isEmpty()) {
//            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
            dayiy = 0;
        } else {
            if (dayiy <= 10) {
                for (StaticDay7Act e : staticDay7ActList) {
                    if (e.getDay() > dayiy) {
                        continue;
                    }
                    // ????????????????????????
                    if (day7Act.getRecvAwardIds().contains(e.getKeyId())) {
                        if (e.getTaskType() == ActivityConst.ACT_TASK_CHARGE) {
                            // ????????????????????????, ???????????????????????????????????????
                            int status = activityDataManager.getDay7ActStatus(player, e);
                            listDay7Act.add(PbHelper.createDay7ActPb(e.getKeyId(), status, ActivityConst.ACT_7_STATUS_HAS_GAIN));
                        } else {
                            listDay7Act.add(PbHelper.createDay7ActPb(e.getKeyId(), e.getCond(), ActivityConst.ACT_7_STATUS_HAS_GAIN));
                        }
                        continue;
                    }

                    int status = activityDataManager.getDay7ActStatus(player, e);
                    if (status > 0) {
                        listDay7Act.add(PbHelper.createDay7ActPb(e.getKeyId(), status, status >= e.getCond() ? 0 : 1));
                    }
                }
            }
        }

        GetDay7ActRs.Builder builder = GetDay7ActRs.newBuilder();
        builder.setDay(dayiy);
        builder.addAllDay7Acts(listDay7Act);
        builder.setBeginTime(TimeHelper.dateToSecond(beginTime));
        builder.setEndTime(TimeHelper.afterSecondTime(beginTime, 10 * 24 * 60 * 60));
        return builder.build();
    }

    /**
     * ??????7???????????????
     */
    public RecvDay7ActAwardRs recvDay7ActAward(Long roleId, int keyId) throws MwException {
        StaticDay7Act staticDay7Act = StaticActivityDataMgr.getAct7DayMap().get(keyId);
        if (staticDay7Act == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
        }

        Player player = playerDataManager.getPlayer(roleId);
        Day7Act day7Act = player.day7Act;

        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        long time = beginTime.getTime() + (7 + 3) * TimeHelper.DAY_S * 1000;
        if (System.currentTimeMillis() > time) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????, roleId:,", roleId);
        }

        Date now = new Date();
        int dayiy = DateHelper.dayiy(beginTime, now);
        if (staticDay7Act.getDay() > dayiy) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
        }

        if (day7Act.getRecvAwardIds().contains(staticDay7Act.getKeyId())) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????????, roleId:,", roleId);
        }
        // ????????????????????????
        rewardDataManager.checkBag(player, staticDay7Act.getAwardList());

        RecvDay7ActAwardRs.Builder builder = RecvDay7ActAwardRs.newBuilder();

        switch (staticDay7Act.getTaskType()) {
            case 15:// ????????????1
                break;
            // case 18://????????????1
            // if(player.lord.getGold() < staticDay7Act.getParam().get(1)){
            // throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(), "???????????????, roleId:,", roleId);
            // }
            // builder.addAtom2(playerDataManager.subProp(player, AwardType.GOLD, 0, staticDay7Act.getParam().get(1),
            // AwardFrom.RECV_DAY_7_ACT_AWARD));
            // break;
            default:
                int status = activityDataManager.getDay7ActStatus(player, staticDay7Act);
                if (status < staticDay7Act.getCond()) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
                }
                break;
        }

        day7Act.getRecvAwardIds().add(staticDay7Act.getKeyId());
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        for (List<Integer> e : staticDay7Act.getAwardList()) {
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// ????????????
            if (type == AwardType.EQUIP) {// || type == AwardType.PART
                for (int c = 0; c < count; c++) {
                    int itemkey = rewardDataManager.addAward(player, type, itemId, 1, AwardFrom.RECV_DAY_7_ACT_AWARD,
                            keyId);
                    builder.addAward(PbHelper.createAwardPb(type, itemId, 1, itemkey));
                }
            } else {
                int itemkey = rewardDataManager.addAward(player, type, itemId, count, AwardFrom.RECV_DAY_7_ACT_AWARD,
                        keyId);
                builder.addAward(PbHelper.createAwardPb(type, itemId, count, itemkey));
            }
        }

        return builder.build();
    }

    // ===============================????????????????????? start========================================

    // ===========================?????????????????? start===========================

    /**
     * ????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetActBlackhawkRs getActBlackhawk(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // ????????????????????????
        int now = TimeHelper.getCurrentSecond();
        int actEndTime = checkBlackhawkIsOver(player, now);// ??????????????????
        ActBlackhawk act = player.blackhawkAct;
        // ???????????????????????????????????????
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            // ????????????
            // ???????????????
            Map<Integer, ActBlackhawkItem> itemsMap = activityDataManager.blanckhawkRefresh(true);
            if (CheckNull.isEmpty(itemsMap)) {
                // ????????????
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "?????????????????????????????????");
            }
            // ?????????
            act.setRefreshCount(ActParamConstant.BLACKHAWK_FREE_COUNT);
            act.setRecvHero(false);
            act.setRefreshTime(now);
            act.setBlackhawkItemMap(itemsMap);
        } else {
            // ??????????????????
            resetBlackCount(now, act);
        }
        GetActBlackhawkRs.Builder builder = GetActBlackhawkRs.newBuilder();
        builder.addAllItems(PbHelper.createBlackhawkItemList(act.getBlackhawkItemMap().values()));
        builder.setRefreshCount(act.getRefreshCount());
        builder.setIsRecvHero(act.isRecvHero());
        builder.setTokenCount(activityDataManager.getBlanckhawkTokenCount(player)); // ??????????????????
        if (act.getRefreshCount() < ActParamConstant.BLACKHAWK_FREE_COUNT) {
            builder.setRefreshEndTime(act.getRefreshTime() + ActParamConstant.BLACKHAWK_REFRESH_INTERVAL);// ????????????????????????????????????
        }
        builder.setRefreshGold( // ???????????????????????????
                ActParamConstant.BLACKHAWK_INIT_PAY_GOLD
                        + (act.getPayRefreshCount() * ActParamConstant.BLACKHAWK_INCR_GOLD));
        builder.setActEndTime(actEndTime);
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param now
     * @param act
     */
    private void resetBlackCount(int now, ActBlackhawk act) {
        // ??????????????????,???????????????
        // ???????????????????????????????????????
        if (act.getRefreshCount() < ActParamConstant.BLACKHAWK_FREE_COUNT) {
            int limit = now - act.getRefreshTime();
            int count = limit / ActParamConstant.BLACKHAWK_REFRESH_INTERVAL;
            if (count > 0) {
                // ?????????????????????,??????????????????
                if (act.getRefreshCount() + count >= ActParamConstant.BLACKHAWK_FREE_COUNT) {
                    count = ActParamConstant.BLACKHAWK_FREE_COUNT - act.getRefreshCount();
                }
                act.setRefreshCount(act.getRefreshCount() + count);// ??????????????????
                act.setRefreshTime(act.getRefreshTime() + (count * ActParamConstant.BLACKHAWK_REFRESH_INTERVAL));// ??????????????????
            }
        } else {
            // ??????
            act.setRefreshTime(now);
        }

    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @throws MwException
     */
    public int checkBlackhawkIsOver(Player player, int now) throws MwException {
        // int createRoleTime = (int) (player.account.getCreateDate().getTime() / 1000); // ??????????????????
        final int sec4Day = ActParamConstant.ACT_BLACK_TIME; // 4???????????? 60 * 60 * 24 * 4
        // int actEndTime = createRoleTime + sec4Day;
        // ??????????????????
        int actEndTime = TimeHelper.afterSecondTime(player.account.getCreateDate(), sec4Day);
        if (now > actEndTime) {
            // ???????????????
            throw new MwException(GameError.ACTIVITY_IS_OVER.getCode(), "???????????????????????????, roleId:", player.roleId, ", actEndTime:", actEndTime, ", now:", now);
        }
        return actEndTime;
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @param isPay  ??????????????????,true?????????
     * @return
     * @throws MwException
     */
    public BlackhawkRefreshRs blackhawkRefresh(long roleId, boolean isPay) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        // ????????????????????????
        checkBlackhawkIsOver(player, now);
        ActBlackhawk act = player.blackhawkAct;
        // ??????????????????????????????
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "????????????????????????????????? roleId:", roleId);
        }

        if (isPay) {
            int needGold = ActParamConstant.BLACKHAWK_INIT_PAY_GOLD
                    + (act.getPayRefreshCount() * ActParamConstant.BLACKHAWK_INCR_GOLD);
            if (needGold > 108) {
                throw new MwException(GameError.REFRESH_CNT_IS_OVER.getCode(), "???????????????????????????", roleId, "count:",
                        act.getPayRefreshCount(), ", needGold:", needGold);
            }
            // ????????????
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.BLACKHAWK_REFRESH);
            // ????????????+1
            act.setPayRefreshCount(act.getPayRefreshCount() + 1);
        } else {
            if (act.getRefreshCount() <= 0) {
                if (now - act.getRefreshTime() < ActParamConstant.BLACKHAWK_REFRESH_INTERVAL) {
                    // CD???????????????
                    throw new MwException(GameError.ACT_BLACKHAWK_REFRESH_NOT_ENOUGH.getCode(), "?????????????????????????????? roleId:",
                            roleId);
                }
            }
            // ??????????????????,??????????????????
            resetBlackCount(now, act);
            // ??????
            if (act.getRefreshCount() >= ActParamConstant.BLACKHAWK_FREE_COUNT) {
                act.setRefreshTime(now);
            }
            // ??????-1
            act.setRefreshCount(act.getRefreshCount() - 1);
        }

        // ??????????????????
        boolean tokenCanBuy = act.isRecvHero()// ???????????????,????????????????????????,????????????????????????
                || activityDataManager.getBlanckhawkTokenCount(player) >= ActParamConstant.BLACKHAWK_NEED_TOKEN ? false
                : true;
        Map<Integer, ActBlackhawkItem> itemsMap = activityDataManager.blanckhawkRefresh(tokenCanBuy);
        act.setBlackhawkItemMap(itemsMap);
        BlackhawkRefreshRs.Builder builder = BlackhawkRefreshRs.newBuilder();
        builder.addAllItems(PbHelper.createBlackhawkItemList(act.getBlackhawkItemMap().values()));
        if (act.getRefreshCount() < ActParamConstant.BLACKHAWK_FREE_COUNT) {
            builder.setRefreshEndTime(act.getRefreshTime() + ActParamConstant.BLACKHAWK_REFRESH_INTERVAL);// ????????????????????????????????????
        }
        builder.setRefreshCount(act.getRefreshCount());
        builder.setRefreshGold( // ???????????????????????????
                ActParamConstant.BLACKHAWK_INIT_PAY_GOLD
                        + (act.getPayRefreshCount() * ActParamConstant.BLACKHAWK_INCR_GOLD));
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param keyId  ???????????????id
     * @return
     * @throws MwException
     */
    public BlackhawkBuyRs blackhawkBuy(long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        // ????????????????????????
        checkBlackhawkIsOver(player, now);
        ActBlackhawk act = player.blackhawkAct;
        // ??????????????????????????????
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            throw new MwException(GameError.ACQUISITE_NO_OPEN.getCode(), "?????????????????????????????????");
        }
        ActBlackhawkItem buyItem = act.getBlackhawkItemMap().get(keyId);
        // ??????????????????????????????
        if (buyItem.isPurchased()) {
            throw new MwException(GameError.ACT_BLACKHAWK_HAS_BUY.getCode(), "???????????????????????????????????????");
        }
        // ?????????????????????
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                buyItem.getDiscountPrice(), AwardFrom.BLACKHAWK_PROP_BUY);
        // ????????????
        Award award = rewardDataManager.addAwardSignle(player, buyItem.getAward(), AwardFrom.BLACKHAWK_PROP_BUY);
        if (CheckNull.isNull(award)) {
            throw new MwException(GameError.ACT_BLACKHAWK_BUY_ERR.getCode(), " ????????????????????????????????? roleId:", roleId, ", keyId:",
                    keyId);
        }
        // ???????????????
        buyItem.setPurchased(true);
        // ???????????????????????????
        for (ActBlackhawkItem it : act.getBlackhawkItemMap().values()) {
            int price = it.getPrice(); // ??????
            int discount = it.getDiscount() - 10;// ??????
            discount = discount <= 0 ? 0 : discount;
            int discountPrice = (int) ((discount * 1.0f) / 100 * (price * 1.0f));
            it.setDiscountPrice(discountPrice);
            it.setDiscount(discount);
        }
        BlackhawkBuyRs.Builder builder = BlackhawkBuyRs.newBuilder();
        builder.setAward(award);
        builder.addAllItems(PbHelper.createBlackhawkItemList(act.getBlackhawkItemMap().values()));
        builder.setTokenCount(activityDataManager.getBlanckhawkTokenCount(player)); // ??????????????????
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public BlackhawkHeroRs blackhawkHero(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        // ????????????????????????
        checkBlackhawkIsOver(player, now);
        ActBlackhawk act = player.blackhawkAct;
        // ??????????????????????????????
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????????????????? roleId:", roleId);
        }
        // ?????????????????????
        if (act.isRecvHero()) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "???????????????????????????????????????");
        }
        ActBlackhawkItem blackhawkItem = act.getBlackhawkItemMap().get(ActParamConstant.BLACKHAWK_TOKEN_KEYID);
        List<Integer> award = blackhawkItem.getAward();
        // ????????????????????????,?????????
        rewardDataManager.checkAndSubPlayerResHasSync(player, award.get(0), award.get(1),
                ActParamConstant.BLACKHAWK_NEED_TOKEN, AwardFrom.ACT_BLACKHAWK_ADD_HERO);
        // ????????????
        rewardDataManager.addAwardSignle(player, AwardType.HERO, ActParamConstant.BLACKHAWK_HERO_ID, 1,
                AwardFrom.ACT_BLACKHAWK_ADD_HERO);
        act.setRecvHero(true);// ?????????????????????????????????
        act.getBlackhawkItemMap().get(ActParamConstant.BLACKHAWK_TOKEN_KEYID).setPurchased(true);// ?????????????????????
        BlackhawkHeroRs.Builder builder = BlackhawkHeroRs.newBuilder();
        builder.setIsRecvHero(true);
        builder.setAward(PbHelper.createAwardPb(AwardType.HERO, ActParamConstant.BLACKHAWK_HERO_ID, 1));
        return builder.build();
    }

    // ===========================?????????????????? end===========================

    /**
     * ?????????????????????
     *
     * @param roleId
     * @param activityType
     * @return
     * @throws MwException
     */
    public GetActRankRs getActRank(long roleId, int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (activityType == ActivityConst.ACT_CHRISTMAS || activityType == ActivityConst.ACT_REPAIR_CASTLE) {
            return activityChristmasService.getRank(player, activityType);
        } else {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if (activityBase == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????????????????(ActivityBase=null), roleId:", roleId, "activityType=" + activityType);
            }
            int actId = activityBase.getActivityId();
            // int step = activityBase.getStep();
            // if (step != ActivityConst.OPEN_STEP) {
            // throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????? roleId:", roleId);
            // }

            Activity activity = activityDataManager.getActivityInfo(player, activityType);
            if (activity == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "?????????????????????, ???????????????(Player.Activity=null) roleId:", roleId, "activityType=" + activityType);
            }

            GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
            if (gActDate == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????? roleId:", roleId);
            }
            List<StaticActAward> sActAward;
            if (activityType == ActivityConst.ACT_NEWYEAR_2022_FISH) {
                sActAward = StaticActivityDataMgr.getActAwardById(actId);
            } else {
                sActAward = StaticActivityDataMgr.getRankActAwardByActId(actId);
            }
            if (CheckNull.isEmpty(sActAward)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????????????? roleId:", roleId, ", actId:", actId,
                        ", actType:", activityType);
            }

            final int showSize = sActAward.size() + 1; // ???????????????,???????????????+1
            LinkedList<ActRank> rankList = gActDate.getPlayerRanks(player, activityType);
            final int rankSize = rankList.size(); // ??????????????????
            ActRank myRank = gActDate.getPlayerRank(player, activityType, roleId);// ???????????????, null????????????????????????
            StaticActAward myAward = null;// ???????????????????????????, ?????????null????????????????????????
            // ????????????????????????
            if (myRank != null) {
                myAward = StaticActivityDataMgr.findRankAward(actId, myRank.getRank());
            }

            List<ActRank> showRankList = new ArrayList<>();// ????????????????????????
            if (rankSize <= showSize) {
                // 1.???????????????????????????????????????
                for (int i = 0; i < rankList.size(); i++) {
                    ActRank ar = rankList.get(i);
                    ar.setRank(i + 1);
                    showRankList.add(ar);
                }
                if (myRank == null) { // ??????????????????????????????????????????
                    if (rankSize >= showSize) {// ??????????????????
                        showRankList.remove(rankSize - 1);
                    }
                    showRankList.add(new ActRank(roleId, activityType,
                            activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0));// ????????????
                }
            } else {
                // 2. ??????????????????????????????
                // ??????????????????????????????
                List<StaticActAward> sRealActAward = sActAward.stream().filter(saa -> rankSize >= saa.getParam().get(1))
                        .collect(Collectors.toList());
                Set<Integer> rankingSet = new HashSet<>(); // ????????????
                if (myAward != null) {
                    // 3 ????????????????????????????????????
                    for (StaticActAward saa : sRealActAward) {
                        if (saa.getParam().get(0) > myAward.getParam().get(0)) { // ??????????????????????????????????????????????????????
                            rankingSet.add(saa.getParam().get(1));
                        } else if (saa.getParam().get(0) < myAward.getParam().get(0)) {// ?????????????????????????????????????????????????????????
                            rankingSet.add(saa.getCond() > rankSize ? rankSize : saa.getCond());
                        } else {
                            rankingSet.add(myRank.getRank());// ???????????????????????????????????????
                        }
                    }
                    for (int i = 1; rankingSet.size() < showSize; i++) {
                        rankingSet.add(i);
                    }
                    rankingSet.stream().sorted(Comparator.comparingInt(i -> i)).forEach(rank -> {
                        ActRank actRank = rankList.get(rank - 1);
                        actRank.setRank(rank);
                        showRankList.add(actRank);
                    });
                } else {
                    // 4 ?????????????????????????????????
                    for (StaticActAward saa : sRealActAward) {
                        rankingSet.add(saa.getCond() > rankSize ? rankSize : saa.getCond());
                    }
                    for (int i = 1; rankingSet.size() < showSize - 1; i++) { // showSize-1????????????????????????
                        rankingSet.add(i);
                    }
                    rankingSet.stream().sorted(Comparator.comparingInt(i -> i)).forEach(rank -> {
                        ActRank actRank = rankList.get(rank - 1);
                        actRank.setRank(rank);
                        showRankList.add(actRank);
                    });
                    showRankList.add(myRank != null ? myRank
                            : new ActRank(roleId, activityType,
                            activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0)); // ??????????????????,???????????????????????????
                }
            }

            GetActRankRs.Builder builder = GetActRankRs.newBuilder();
            for (ActRank ar : showRankList) {
                Player p = playerDataManager.getPlayer(ar.getLordId());
                if (p == null) {
                    continue;
                }
                if (activityType == ActivityConst.ACT_CAMP_RANK) {
                    builder.addActRank(PbHelper.createActRank(ar, activityType, p.lord.getNick(), p.lord.getCamp(), p.lord.getPortrait(), p.getDressUp().getCurPortraitFrame()));
                } else {
                    builder.addActRank(PbHelper.createActRank(ar, p.lord.getNick(), p.lord.getCamp(), p.lord.getPortrait(), p.getDressUp().getCurPortraitFrame()));
                }
            }
            // ??????
            int myRankSchedule = activityDataManager.getRankAwardSchedule(player, activityType);
            for (StaticActAward e : sActAward) {
                int keyId = e.getKeyId();
                int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                builder.addActivityCond(PbHelper.createActivityCondPb(e, status, myRankSchedule));
            }
            int awardTime = (int) (activityBase.getAwardBeginTime().getTime() / 1000);
            builder.setAwardTime(awardTime);
            // ????????????
            if (activityType == ActivityConst.ACT_CAMP_FIGHT_RANK) {
                builder.addExtParam(PbHelper.createIntLongPc(1, gActDate.getTopupa().get()));
                builder.addExtParam(PbHelper.createIntLongPc(2, gActDate.getTopupb().get()));
                builder.addExtParam(PbHelper.createIntLongPc(3, gActDate.getTopupc().get()));
            }
            return builder.build();
        }
    }

    // ===========================?????????????????? start===========================

//    public void refreshData4AcrossDay(Player player,int activityType){
//        Activity activity = activityDataManager.getActivityInfo(player,activityType);
//        if(Objects.nonNull(activity)){
//            if(activityType == ActivityConst.ACT_MERGE_PROP_PROMOTION){//?????????????????????????????????????????????
//                activity.getStatusMap().clear();
//            }
//        }
//    }

    /**
     * ????????????vip??????????????????
     *
     * @param player
     * @param activityType
     */
    public void refreshTurnplateCnt(Player player, int activityType) {
        ActTurnplat turnTable;
        if (activityType == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            if (CheckNull.isNull(activityBase)) {
                return;
            }

            Date beginTime = activityBase.getBeginTime();
            int begin = TimeHelper.getDay(beginTime);
            Activity activity = player.activitys.get(activityType);
            if (activity == null) {
                activity = activityDataManager.conActivity(activityBase, activityType, begin, player);
                player.activitys.put(activityType, activity);
            } else {
                activity.isReset(begin, player);// ????????????????????????
                activity.autoDayClean(activityBase);
                activity.cleanActivityAuction(activityBase);
            }
            activity.setOpen(activityBase.getBaseOpen());
            turnTable = (ActTurnplat) activity;
            if (activityBase.getStep0() == ActivityConst.OPEN_AWARD) {
                turnTable.setRefreshCount(0);
                turnTable.setCnt(0);
                turnTable.setGoldCnt(0);
                turnTable.setTodayCnt(0);
                turnTable.getWinCnt().clear();
                turnTable.getWinCnt211().clear();
                turnTable.getStatusCnt().clear();
                return;
            }
        } else {
            turnTable = (ActTurnplat) activityDataManager.getActivityInfo(player, activityType);
            if (CheckNull.isNull(turnTable)) { // ????????????
                return;
            }
        }

        turnTable.refreshFreeCnt(player);
    }

    /**
     * ????????????/?????? ????????????
     *
     * @param roleId
     * @throws MwException
     */
    public GetActTurnplatRs getActTurnplat(long roleId, int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ??????????????????
        if (activityType != ActivityConst.ACT_LUCKY_TURNPLATE
                && activityType != ActivityConst.FAMOUS_GENERAL_TURNPLATE
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                && activityType != ActivityConst.ACT_SEASON_TURNPLATE) {
            throw new MwException(GameError.ACT_TYPE_ERROR.getCode(), " ?????????????????? activityType:", activityType);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????,??????????????? roleId:", roleId);
        }

        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????, roleId:,", roleId, ", type:",
                    activityType);
        }

        List<StaticTurnplateConf> turnplateConfs = StaticActivityDataMgr
                .getActTurnPlateListByActId(turnplat.getActivityId());
        if (CheckNull.isEmpty(turnplateConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ??????/?????? ?????????????????????, roleId:", roleId);
        }

        GetActTurnplatRs.Builder builder = GetActTurnplatRs.newBuilder();
        builder.setFreeCount(turnplat.getRefreshCount());
        for (StaticTurnplateConf conf : turnplateConfs) {
            builder.addInfo(PbHelper.createTurnplateInfo(conf));
        }
        StaticTurnplateConf conf = turnplateConfs.get(0);
        List<List<Integer>> awardList = conf.getAwardList();
        for (List<Integer> awards : awardList) {
            if (awards.size() < 3) {
                continue;
            }
            builder.addDisplay(PbHelper.createAwardPb(awards.get(0), awards.get(1), awards.get(2)));
        }

        int param = 0;
        if (!conf.getUpProbability().isEmpty() && !conf.getDownProbability().isEmpty()) {
            param = conf.getUpProbability().get(2) > 0 ? conf.getUpProbability().get(2)
                    : conf.getDownProbability().get(2);
        }
        List<List<Integer>> onlyAward = conf.getOnlyAward();
        for (List<Integer> awards : onlyAward) {
            if (awards.size() < 3) {
                continue;
            }
            builder.addDisplay(PbHelper.createAwardPbWithParam(awards.get(0), awards.get(1), awards.get(2), 0, param));
        }
        List<Award> specialList = getPlayerSpecial(player, conf);
        builder.addAllSpecial(specialList);
        builder.setSpecialCnt(turnplat.currentSpecialCnt());
        List<List<Integer>> getItem = conf.getGetItem();
        for (List<Integer> item : getItem) {
            if (item.size() < 3) {
                continue;
            }
            builder.addGetItem(PbHelper.createAwardPb(item.get(0), item.get(1), item.get(2)));

        }
        // ???????????????
        if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                || activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                || activityType == ActivityConst.ACT_SEASON_TURNPLATE) {
            List<StaticTurnplateExtra> sExtrasConf = StaticActivityDataMgr.getActTurnplateExtraByActId(turnplat.getActivityId());
            if (!CheckNull.isEmpty(sExtrasConf)) {
                for (StaticTurnplateExtra sExtra : sExtrasConf) {
                    int id = sExtra.getId();
                    builder.addStatus(PbHelper.createTwoIntPb(id, turnplat.getStatusMap().getOrDefault(id, 0)));
                }
            }
            // ??????????????????
            builder.setCnt(turnplat.getCnt());
            builder.setTodayCnt(conf.getDailyLimited() - turnplat.getTodayCnt());
        }
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param id       turnplateId
     * @param costType ???????????????1 ???????????????2 ??????
     */
    public LuckyTurnplateRs luckyTurnplate(long roleId, int id, int costType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticTurnplateConf turnplateConf = StaticActivityDataMgr.getActTurnPlateById(id);
        if (CheckNull.isNull(turnplateConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ??????/?????? ?????????????????????, roleId:", roleId);
        }

        int activityType = turnplateConf.getType();

        // ??????????????????
        if (activityType != ActivityConst.ACT_LUCKY_TURNPLATE
                && activityType != ActivityConst.FAMOUS_GENERAL_TURNPLATE
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                && activityType != ActivityConst.ACT_SEASON_TURNPLATE
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
            throw new MwException(GameError.ACT_TYPE_ERROR.getCode(), " ?????????????????? activityType:", activityType);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????,??????????????? roleId:", roleId);
        }
        if (activityType == ActivityConst.FAMOUS_GENERAL_TURNPLATE && activityBase.getBaseOpen() == ActivityConst.OPEN_AWARD) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????,??????????????? roleId:", roleId);
        }

        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????, roleId:,", roleId, ", type:",
                    activityType);
        }

        if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
            if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType && turnplat.getTodayCnt() + turnplateConf.getCount() > turnplateConf.getDailyLimited()) {
                throw new MwException(GameError.ACT_TURNPLAT_NEW_YEAR_TODAY_LIMIT.getCode(), " ????????????, ????????????????????????, roleId:,", roleId, ", type:",
                        activityType);
            }
        }

        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activityType);
        if (CheckNull.isNull(globalActivity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????, roleId:,", roleId, ", type:",
                    activityType);
        }

        boolean initWinCnt = false;

        String logCost = "";
        ChangeInfo change = ChangeInfo.newIns();// ??????????????????????????????
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType) {// ????????????
            if (turnplat.getRefreshCount() <= 0) {
                throw new MwException(GameError.ACT_LUCKY_TURNPLATE_MAX_COUNT.getCode(), " ??????/?????? ??????????????????, ???????????? roleId:",
                        roleId, ", cnt:", turnplat.getRefreshCount());
            }
            turnplat.subRefreshCount();
            LogLordHelper.commonLog("freeLuckyTurnplate", activityType == ActivityConst.ACT_LUCKY_TURNPLATE
                    ? AwardFrom.LUCKY_TURNPLATE_FREE : AwardFrom.FAMOUS_GENERAL_TURNPLATE_FREE, player);
            if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
                initWinCnt = true;
            }
            logCost = "0";
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {// ????????????
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, turnplateConf.getPrice(),
                    activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? "??????????????????" : "??????????????????");
            rewardDataManager.subGold(player, turnplateConf.getPrice(),
                    activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_GOLD
                            : AwardFrom.FAMOUS_GENERAL_TURNPLATE_GOLD, turnplat.getActivityId(), turnplat.getActivityType());
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
            initWinCnt = true;
            logCost = AwardType.MONEY + "," + AwardType.Money.GOLD + "," + turnplateConf.getPrice();
        } else if (ActivityConst.LUCKY_TURNPLATE_PROP == costType) {// ????????????
            rewardDataManager.checkAndSubPlayerRes(player, turnplateConf.getSubstitute(), activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_PROP
                    : AwardFrom.FAMOUS_GENERAL_TURNPLATE_PROP, turnplat.getActivityId(), turnplat.getActivityType());
            if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
                initWinCnt = true;
            }
            List<String> logCostList = CheckNull.isEmpty(turnplateConf.getSubstitute()) ? turnplateConf.getSubstitute().stream().filter(consume -> CheckNull.nonEmpty(consume)).
                    map(consume -> consume.get(0) + "," + consume.get(1) + "," + consume.get(2)).collect(Collectors.toList()) : null;
            if (CheckNull.nonEmpty(logCostList)) {
                logCost = ListUtils.toString(logCostList);
            }
        }

        if (initWinCnt && turnplat.getGoldCnt() == 0) {
            doSearchWinCnt(turnplat, turnplateConf);
        }

        LuckyTurnplateRs.Builder builder = LuckyTurnplateRs.newBuilder();

        builder.setFreeCount(turnplat.getRefreshCount());
        builder.setGold(player.lord.getGold());
        // ????????????
        int integral = 0;
        List<List<Integer>> awards = new ArrayList<>();
        for (int i = 0; i < turnplateConf.getCount(); i++) {
            if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
                turnplat.setGoldCnt(turnplat.getGoldCnt() + 1);
                if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
                    turnplat.setTodayCnt(turnplat.getTodayCnt() + 1);
                }
            } else {
                if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
                    turnplat.setGoldCnt(turnplat.getGoldCnt() + 1);
                }
            }
            // ????????????????????????
            if (activityType == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
                turnplat.getStatusMap().merge(ActTurnplat.TURNTABLE_BOTTOM_GUARANTEE_INDEX, 1, Integer::sum);
            }
            turnplat.setCnt(turnplat.getCnt() + 1);
            awards.add(doSweepstakes(costType, turnplat, turnplateConf, player, integral));
        }
        // ?????????????????????????????????????????????????????????
        if (activityType == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
            twoGeneralChange(awards);
        }
        if (!CheckNull.isEmpty(awards)) {
            for (List<Integer> award : awards) {
                rewardDataManager.addAward(player, award.get(0), award.get(1), award.get(2),
                        activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_AWARD
                                : AwardFrom.FAMOUS_GENERAL_TURNPLATE_AWARD, turnplat.getActivityId(), turnplat.getActivityType());
                builder.addLotteryAward(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2)));
            }
        }

        // ????????????
        integral = turnplateConf.getPoint() > 0 ? turnplateConf.getPoint() : integral;
        if (integral > 0) {
            activityDataManager.updRankActivity(player, activityType, integral); // ??????????????????
        }

        // ??????????????????
        if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
            List<List<Integer>> getItem = turnplateConf.getGetItem();
            for (List<Integer> item : getItem) {
                if (item.size() < 3) {
                    continue;
                }
                rewardDataManager.addAward(player, item.get(0),
                        item.get(1), item.get(2) * turnplateConf.getCount(),
                        activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_GOLD_AWARD : AwardFrom.FAMOUS_GENERAL_TURNPLATE_GOLD_AWARD, turnplat.getActivityId(), turnplat.getActivityType());
                builder.addAward(PbHelper.createAwardPb(item.get(0),
                        item.get(1), item.get(2) * turnplateConf.getCount()));
            }
        }

        // ?????????????????????
        builder.addAllSpecial(getPlayerSpecial(player, turnplateConf));

        // ?????????????????????????????????
        builder.setSpecialCnt(turnplat.currentSpecialCnt());

        // ?????????????????????????????????
        rewardDataManager.syncRoleResChanged(player, change);

        LogUtil.debug("???????????????????????????, roleId:", roleId, ", winCntPoint:", LogUtil.getSetValStr(turnplat.getWinCnt()),
                ", goldCnt:", turnplat.getGoldCnt(), ", ?????????????????????:", turnplat.currentSpecialCnt());

        String resultLog = "";
        if (CheckNull.nonEmpty(awards)) {
            List<String> resultLogList = awards.stream().filter(list -> CheckNull.nonEmpty(list) && list.size() >= 3).
                    map(list -> list.get(0) + "," + list.get(1) + "," + list.get(2) + "&").collect(Collectors.toList());
            if (CheckNull.nonEmpty(resultLogList)) {
                resultLog = ListUtils.toString(resultLogList);
            }
        }
        LogLordHelper.gameLog(LogParamConstant.TURNTABLE_ACT, player, AwardFrom.LOG_TURNTABLE_ACT, activityType, activityBase.getActivityId(),
                turnplateConf.getLogCountType(), logCost, resultLog);

        // ????????????????????????,???????????????
        if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE) {// ????????????
            checkSpecialChipAward(turnplateConf.getOnlyAward(), builder, player);
        }
        if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                || activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                || activityType == ActivityConst.ACT_SEASON_TURNPLATE) {
            builder.setCnt(turnplat.getCnt());
            builder.setTodayCnt(turnplateConf.getDailyLimited() - turnplat.getTodayCnt());
        }
        return builder.build();
    }

    /**
     * ???????????????????????????
     *
     * @param roleId ??????id
     * @param req    ????????????
     * @return
     * @throws MwException
     */
    public TurnplatCntAwardRs turnplatCntAward(long roleId, TurnplatCntAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int actType = req.getActType();
        int keyId = req.getKeyId();

        StaticTurnplateExtra sTurnPlateExtra = StaticActivityDataMgr.getActTurnPlateExtraById(keyId);
        if (CheckNull.isNull(sTurnPlateExtra)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ??????/?????? ?????????????????????, roleId:", roleId);
        }

        // ??????????????????
        if (actType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                && actType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                && actType != ActivityConst.ACT_SEASON_TURNPLATE) {
            throw new MwException(GameError.ACT_TYPE_ERROR.getCode(), " ?????????????????? activityType:", actType);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????,??????????????? roleId:", roleId);
        }

        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, actType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ??????/?????? ?????????????????????, roleId:,", roleId, ", type:",
                    actType);
        }
        // ????????????
        int cnt = turnplat.getCnt();
        if (cnt < sTurnPlateExtra.getTimes()) {
            throw new MwException(GameError.TURNPLATE_CNT_AWARD_ERROR.getCode(), "??????????????????????????????, ???????????????, roleId:", roleId, ", cnt:", cnt, ", needCnt:", sTurnPlateExtra.getTimes());
        }
        int status = turnplat.getStatusMap().getOrDefault(keyId, 0);
        if (status != 0) {
            throw new MwException(GameError.TURNPLATE_CNT_AWARD_ERROR.getCode(), "??????????????????????????????, ??????????????????, roleId:", roleId, ", status:", status);
        }

        TurnplatCntAwardRs.Builder builder = TurnplatCntAwardRs.newBuilder();
        List<List<Integer>> awardList = sTurnPlateExtra.getAwardList();
        if (!CheckNull.isEmpty(awardList)) {
            for (List<Integer> award : awardList) {
                builder.addAward(rewardDataManager.addAwardSignle(player, award, AwardFrom.TURNPLATE_CNT_AWARD));
            }
        }

        // ??????????????????????????????
        turnplat.getStatusMap().put(keyId, 1);

        return builder.build();
    }


    // ?????????????????????????????????????????????????????????????????????????????????
    private void twoGeneralChange(List<List<Integer>> awards) {
        ArrayList<Integer> indexList = new ArrayList<>();// ????????????
        int count = 0;
        if (!CheckNull.isEmpty(awards)) {
            for (int i = 0; i < awards.size(); i++) {
                List<Integer> list = awards.get(i);
                Integer type = list.get(0);
                if (type == AwardType.HERO) {
                    count++;
                    indexList.add(i);
                }
            }
        }
        if (count > 1) {
            List<Integer> listChange = new ArrayList<>();
            indexList.remove(0);
            for (Integer index : indexList) {
                List<Integer> heroList = awards.get(index);
                if (CheckNull.isEmpty(heroList))
                    continue;
                listChange.add(AwardType.HERO_FRAGMENT);
                listChange.add(heroList.get(1));
                listChange.add(HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS);
                awards.set(index, listChange);
            }
        }
    }

    /**
     * ????????????????????????,???????????????
     *
     * @param onlyAward
     * @param builder
     * @param player
     */
    private void checkSpecialChipAward(List<List<Integer>> onlyAward, LuckyTurnplateRs.Builder builder, Player player) {
        if (!CheckNull.isEmpty(onlyAward)) {
            List<Integer> chip = onlyAward.get(0);
            if (!CheckNull.isEmpty(chip)) {
                Integer propId = chip.get(1);
                StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
                if (staticProp == null) {
                    return;
                }
                if (staticProp.getPropType() != 9) {
                    return;
                }
                try {
                    rewardDataManager.checkPropIsEnough(player, propId, staticProp.getChip(), "????????????");
                    rewardDataManager.subProp(player, propId, staticProp.getChip(), AwardFrom.SYNTHETIC_PROP);
                } catch (Exception e) {
                    LogUtil.error("??????????????????", e);
                    return;
                }
                // ????????????
                for (List<Integer> award : staticProp.getRewardList()) {
                    rewardDataManager.addAward(player, award.get(0), award.get(1), award.get(2),
                            AwardFrom.SYNTHETIC_PROP_AWARD);
                    builder.addChipAward(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2)));
                }
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param conf
     * @return
     */
    private List<Award> getPlayerSpecial(Player player, StaticTurnplateConf conf) {
        List<Award> specialList = new ArrayList<>();
        List<List<Integer>> specialAward = conf.getSpecialAward();
        for (List<Integer> special : specialAward) {
            if (special.size() < 2) {
                continue;
            }
            Integer type = special.get(0);
            Integer id = special.get(1);
            int param = player.checkHaveSpecial(type, id) ? 1 : 0;
            specialList.add(PbHelper.createAwardPbWithParam(type, id, 1, 0, param));
        }
        return specialList;
    }

    /**
     * ?????????????????????
     *
     * @param costType
     * @param turnplat
     * @param player
     * @param integral ??????????????????????????????????????????
     */
    private List<Integer> doSweepstakes(int costType, ActTurnplat turnplat, StaticTurnplateConf conf, Player player,
                                        int integral) {
        boolean falg = false;// ?????????????????????
        int goldCnt = turnplat.getGoldCnt();
        Set<Integer> winCnt = turnplat.getWinCnt();// ???????????????????????????
        List<Set<Integer>> winCnt211 = turnplat.getWinCnt211();//?????????????????????????????????
        List<Integer> awardList = new ArrayList<>();
        if (conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {//????????????
            int awardIdx = turnplat.getSetIdxByCnt(goldCnt);
            if (awardIdx != -1) {
                awardList = conf.getProbAward().get(awardIdx);
                getTurnplatePoint(integral, awardList, 3);

                falg = true;// ?????????????????? ???????????????
            } else {
                // ????????????????????????
                awardList = doSweepstakesAwards(conf, player, turnplat);
                getTurnplatePoint(integral, awardList, 4);
            }
        } else {
            if (ActivityConst.LUCKY_TURNPLATE_FREE == costType || ActivityConst.LUCKY_TURNPLATE_PROP == costType) { // ????????????
                // ????????????????????????
                awardList = doSweepstakesAwards(conf, player, turnplat);
                getTurnplatePoint(integral, awardList, 4);
            } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) { // ????????????
                if (conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {//????????????

                } else {
                    if (winCnt.contains(goldCnt)) { // ????????????????????????
                        awardList = !CheckNull.isEmpty(conf.getOnlyAward()) ? conf.getOnlyAward().get(0) : new ArrayList<>();
                        getTurnplatePoint(integral, awardList, 3);
                        activityDataManager.updActivity(player, conf.getType(), 1, ActTurnplat.SPECIAL_SORT, false); // ???????????????????????????????????????

                        falg = true;// ?????????????????? ???????????????
                    } else { // ??????????????????
                        // ????????????????????????
                        awardList = doSweepstakesAwards(conf, player, turnplat);
                        getTurnplatePoint(integral, awardList, 4);
                    }
                }
            }
        }

        // ?????????????????????????????? ???????????????????????? ????????????6??????
        if (awardList.size() > 1 && awardList.get(0) == AwardType.HERO) {
            if (checkAwardHasHero(awardList, player)) {
                int heroId = awardList.get(1);
                awardList.clear();
                awardList.addAll(Arrays.asList(AwardType.HERO_FRAGMENT, heroId, HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS));
            }
        }

        // ????????????????????????????????????????????????
        if (falg || checkAwardChat(awardList)) {
            // ???????????????
            chatDataManager.sendSysChat(
                    conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE ? ChatConst.CHAT_LUCKY_TURNPLATE_GLOBAL_NUM
                            : ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM,
                    player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), awardList.get(0),
                    awardList.get(1), awardList.get(2), turnplat.getActivityId());
            //??????????????????
            int chatId;
            if (conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE) {
                chatId = ChatConst.CHAT_LUCKY_TURNPLATE_GLOBAL_NUM;
            } else {
                chatId = ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM;
            }
            chatDataManager.sendActivityChat(chatId, conf.getType(), 0,
                    player.lord.getCamp(), player.lord.getNick(), awardList.get(0), awardList.get(1), awardList.get(2), turnplat.getActivityId());
        }
        return awardList;
    }

    private boolean checkCntInBetween(int cnt, StaticTurnplateConf conf) {
        for (List<Integer> list : conf.getProbList()) {
            if (cnt >= list.get(0) && cnt <= list.get(1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param awardList
     * @return boolean
     * @Title: checkAwardChat
     * @Description: ???????????????????????????????????????????????????
     */
    public boolean checkAwardHasHero(List<Integer> awardList, Player player) {
        int heroId = awardList.get(1);
        if (player.heros.get(heroId) != null) {
            return true;
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        StaticHero staticHeroOld = null;
        for (Hero v : player.heros.values()) {
            staticHeroOld = StaticHeroDataMgr.getHeroMap().get(v.getHeroId());
            if (staticHeroOld.getHeroType() == staticHero.getHeroType()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param awardList
     * @return boolean
     * @Title: checkAwardChat
     * @Description: ????????????????????????????????????????????????
     */
    public boolean checkAwardChat(List<Integer> awardList) {
        if (awardList == null || awardList.size() < 2) {
            return false;
        }
        for (List<Integer> list : Constant.SEND_CHAT_PROP_IDS) {
            if (list.size() > 1 && list.get(0).intValue() == awardList.get(0).intValue()
                    && list.get(1).intValue() == awardList.get(1).intValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * ????????????????????????????????????
     *
     * @param integral
     * @param awardList
     * @param integralIndex
     */
    public void getTurnplatePoint(int integral, List<Integer> awardList, int integralIndex) {
        if (awardList.size() >= integralIndex + 1) {
            integral += awardList.get(integralIndex);
        }
    }

    /**
     * ????????????????????????
     *
     * @param conf
     * @param player
     * @param turnplat
     */
    public List<Integer> doSweepstakesAwards(StaticTurnplateConf conf, Player player, ActTurnplat turnplat) {
        List<Integer> awardList = null;
        boolean flag = true;
        while (flag) {
            if (ActivityConst.FAMOUS_GENERAL_TURNPLATE == turnplat.getActivityType()) {
                int guaranteeCnt = turnplat.getStatusMap().getOrDefault(ActTurnplat.TURNTABLE_BOTTOM_GUARANTEE_INDEX, 0);
                if (CheckNull.nonEmpty(ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE) && ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE.size() >= 4) {
                    if (guaranteeCnt >= ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE.get(3)) {
                        flag = false;
                        awardList = ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE.subList(0, 3);
                        // ??????????????????
                        turnplat.getStatusMap().put(ActTurnplat.TURNTABLE_BOTTOM_GUARANTEE_INDEX, 0);
                        continue;
                    }
                }
            }
            // ??????????????????
            awardList = RandomUtil.getRandomByWeight(conf.getAwardList(), 3, false);
            if (checkSpecialOnlyAward(awardList, turnplat, conf)) { // ????????????
                if (!player.checkHaveSpecial(awardList.get(0), awardList.get(1))) { // ???????????????
                    player.upSpecialProp(awardList.get(0), awardList.get(1));
                    flag = false;
                }
            } else { // ????????????
                flag = false;
                if (ActivityConst.FAMOUS_GENERAL_TURNPLATE == turnplat.getActivityType() && CheckNull.nonEmpty(ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE) &&
                        ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE.size() >= 2 && ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE.get(0) ==
                        awardList.get(0) && ActParamConstant.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE.get(1) == awardList.get(1)) {
                    // ??????????????????
                    turnplat.getStatusMap().put(ActTurnplat.TURNTABLE_BOTTOM_GUARANTEE_INDEX, 0);
                }
            }
        }
        return awardList;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param awardList
     * @param turnplat
     * @param conf
     * @return
     */
    private boolean checkSpecialOnlyAward(List<Integer> awardList, ActTurnplat turnplat, StaticTurnplateConf conf) {
        List<List<Integer>> specialAward = conf.getSpecialAward();
        if (!CheckNull.isEmpty(specialAward)) {
            for (List<Integer> special : specialAward) {
                if (special.get(0).intValue() == awardList.get(0).intValue()
                        && special.get(1).intValue() == awardList.get(1).intValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param turnplat
     * @param conf
     */
    private void doSearchWinCnt(ActTurnplat turnplat, StaticTurnplateConf conf) {
        // ?????????????????????????????????
        turnplat.getWinCnt().clear();
        try {
            if (conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
                for (int i = 0; i < conf.getProbAward().size(); i++) {
                    Set<Integer> set = new HashSet<>();
                    turnplat.getWinCnt211().add(set);
                }
                conf.getProbList().forEach(list -> {
                    for (int i = list.size() - 2; i < list.size(); i++) {
                        List<Integer> tmpList = new ArrayList<>(3);
                        tmpList.add(list.get(0));
                        tmpList.add(list.get(1));
                        tmpList.add(list.get(i));
                        this.checkAndRandomWinCnt(turnplat.getWinCnt211(), tmpList, i - 2);
                    }
                });
            } else {
                List<Integer> downProbability = conf.getDownProbability();
                RandomHelper.randomWinCnt(downProbability, turnplat.getWinCnt());
                List<Integer> upProbability = conf.getUpProbability();
                RandomHelper.randomWinCnt(upProbability, turnplat.getWinCnt());
            }
        } catch (Exception e) {
            LogUtil.error("??????????????????????????????????????????????????????????????????, ", e);
        }
    }

    private void checkAndRandomWinCnt(List<Set<Integer>> winCnts, List<Integer> tmpList, int awardIdx) {
        Set<Integer> all = new HashSet<>();
        winCnts.forEach(set -> all.addAll(set));
        int n = tmpList.get(2);
        if (n <= 0) {
            return;
        }
        int i = 0;
        while (true) {
            int cnt = RandomUtil.randomIntIncludeEnd(tmpList.get(0), tmpList.get(1));
            if (all.add(cnt)) {
                winCnts.get(awardIdx).add(cnt);
                i++;
            }
            if (i >= n) {
                break;
            }
        }
    }

    // ===========================?????????????????? end===========================

    // ===========================?????????????????? start===========================

    /**
     * @param player
     * @return void
     * @Title: refreshEquipTurnplateCnt
     * @Description: ????????????vip??????????????????
     */
    public void refreshEquipTurnplateCnt(Player player) {
        int activityType = ActivityConst.ACT_EQUIP_TURNPLATE;
        EquipTurnplat turnplat = (EquipTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) { // ????????????
            return;
        }
        turnplat.refreshFreeCnt(player);
    }

    /**
     * @param roleId
     * @return GetActTurnplatRs
     * @throws MwException
     * @Title: getActTurnplat
     * @Description: ????????????????????????
     */
    public GetEquipTurnplatRs getEquipTurnplat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int activityType = ActivityConst.ACT_EQUIP_TURNPLATE;

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,??????????????? roleId:", roleId);
        }

        EquipTurnplat turnplat = (EquipTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????, roleId:,", roleId, ", type:",
                    activityType);
        }

        List<StaticEquipTurnplateConf> turnplateConfs = StaticActivityDataMgr
                .getEquipTurnPlateListByActId(turnplat.getActivityId());
        if (CheckNull.isEmpty(turnplateConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ???????????????????????????, roleId:", roleId);
        }

        GetEquipTurnplatRs.Builder builder = GetEquipTurnplatRs.newBuilder();
        builder.setFreeCount(turnplat.getRefreshCount());
        for (StaticEquipTurnplateConf conf : turnplateConfs) {
            builder.addInfo(PbHelper.createEquipTurnplateInfo(conf));
        }
        StaticEquipTurnplateConf conf = turnplateConfs.get(0);
        List<List<Integer>> awardList = conf.getAwardList();
        for (List<Integer> awards : awardList) {
            if (awards.size() < 3) {
                continue;
            }
            builder.addDisplay(PbHelper.createAwardPb(awards.get(0), awards.get(1), awards.get(2)));
        }
        int param = conf.getProbability().get(2);
        List<List<Integer>> onlyAward = conf.getOnlyAward();
        for (List<Integer> awards : onlyAward) {
            if (awards.size() < 3) {
                continue;
            }
            builder.addDisplay(PbHelper.createAwardPbWithParam(awards.get(0), awards.get(1), awards.get(2), 0, param));
        }
        builder.setSpecialCnt(turnplat.currentSpecialCnt());
        return builder.build();
    }

    /**
     * @param roleId
     * @param id
     * @param costType
     * @return EquipTurnplateRs
     * @throws MwException
     * @Title: equipTurnplate
     * @Description: ??????????????????
     */
    public EquipTurnplateRs equipTurnplate(long roleId, int id, int costType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int activityType = ActivityConst.ACT_EQUIP_TURNPLATE;

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,??????????????? roleId:", roleId);
        }

        EquipTurnplat turnplat = (EquipTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????, roleId:,", roleId, ", type:",
                    activityType);
        }

        StaticEquipTurnplateConf turnplateConf = StaticActivityDataMgr.getEquipTurnPlateById(id);
        if (CheckNull.isNull(turnplateConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ???????????????????????????, roleId:", roleId);
        }

        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activityType);
        if (CheckNull.isNull(globalActivity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????, roleId:,", roleId, ", type:",
                    activityType);
        }

        ChangeInfo change = ChangeInfo.newIns();// ??????????????????????????????
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType) {// ????????????
            if (turnplat.getRefreshCount() <= 0) {
                throw new MwException(GameError.ACT_EQUIP_TURNPLATE_MAX_COUNT.getCode(), " ????????????????????????, ???????????? roleId:",
                        roleId, ", cnt:", turnplat.getRefreshCount());
            }
            turnplat.subRefreshCount();
            LogLordHelper.commonLog("freeEquipTurnplate", AwardFrom.EQUIP_TURNPLATE_FREE, player);
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {// ????????????
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, turnplateConf.getPrice(), "??????????????????");
            rewardDataManager.subGold(player, turnplateConf.getPrice(), AwardFrom.EQUIP_TURNPLATE_GOLD, turnplat.getActivityId(), turnplat.getActivityType());
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
            if (turnplat.getGoldCnt() == 0) {
                equipDoSearchWinCnt(turnplat, turnplateConf);
            }
        } else if (ActivityConst.LUCKY_TURNPLATE_PROP == costType) {// ????????????
            rewardDataManager.checkAndSubPlayerRes(player, turnplateConf.getSubstitute(), activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_PROP : AwardFrom.FAMOUS_GENERAL_TURNPLATE_PROP, turnplat.getActivityId(), turnplat.getActivityType());
        }

        EquipTurnplateRs.Builder builder = EquipTurnplateRs.newBuilder();

        builder.setFreeCount(turnplat.getRefreshCount());
        builder.setGold(player.lord.getGold());
        // ????????????
        int integral = 0;
        List<List<Integer>> awards = new ArrayList<>();
        for (int i = 0; i < turnplateConf.getCount(); i++) {
            if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
                turnplat.setGoldCnt(turnplat.getGoldCnt() + 1);
            }
            awards.add(equipDoSweepstakes(costType, turnplat, turnplateConf, player, integral, activityType));
            // ????????????????????????
            if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
                activityDataManager.getGlobalActivity(activityType).addEquipTurLuckNums();
            }
        }
        if (!CheckNull.isEmpty(awards)) {
            for (List<Integer> award : awards) {
                rewardDataManager.addAward(player, award.get(0), award.get(1), award.get(2), AwardFrom.EQUIP_TURNPLATE_AWARD, turnplat.getActivityId(), turnplat.getActivityType());
                builder.addLotteryAward(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2)));
            }
        }

        // ????????????
        integral = turnplateConf.getPoint() > 0 ? turnplateConf.getPoint() : integral;
        if (integral > 0) {
            activityDataManager.updRankActivity(player, activityType, integral); // ??????????????????
        }

        // ??????????????????
        if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
            if (!CheckNull.isEmpty(ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD)) {
                rewardDataManager.addAward(player, ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(0),
                        ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(1), turnplateConf.getCount(),
                        AwardFrom.EQUIP_TURNPLATE_GOLD_AWARD, turnplat.getActivityId(), turnplat.getActivityType());
                builder.addAward(PbHelper.createAwardPb(ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(0),
                        ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(1), turnplateConf.getCount()));
            }
        }

        // ?????????????????????????????????
        builder.setSpecialCnt(turnplat.currentSpecialCnt());

        // ?????????????????????????????????
        rewardDataManager.syncRoleResChanged(player, change);

        LogUtil.debug("????????????-???????????????????????????, roleId:", roleId, ", winCntPoint:", LogUtil.getSetValStr(turnplat.getWinCnt()),
                ", goldCnt:", turnplat.getGoldCnt(), ",globalNum:",
                activityDataManager.getGlobalActivity(activityType).getEquipTurLuckNums(), ", ?????????????????????:",
                turnplat.currentSpecialCnt());

        // ????????????????????????,???????????????
        equipCheckSpecialChipAward(turnplateConf.getOnlyAward(), builder, player);

        return builder.build();
    }

    /**
     * @param onlyAward
     * @param builder
     * @param player
     * @return void
     * @Title: equipCheckSpecialChipAward
     * @Description: ????????????????????????, ???????????????
     */
    private void equipCheckSpecialChipAward(List<List<Integer>> onlyAward, EquipTurnplateRs.Builder builder,
                                            Player player) {
        if (!CheckNull.isEmpty(onlyAward)) {
            List<Integer> chip = onlyAward.get(0);
            if (!CheckNull.isEmpty(chip)) {
                Integer propId = chip.get(1);
                StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
                if (staticProp == null) {
                    return;
                }
                if (staticProp.getPropType() != 9) {
                    return;
                }
                try {
                    rewardDataManager.checkPropIsEnough(player, propId, staticProp.getChip(), "????????????");
                    rewardDataManager.subProp(player, propId, staticProp.getChip(), AwardFrom.SYNTHETIC_PROP);
                } catch (Exception e) {
                    LogUtil.error("??????????????????", e);
                    return;
                }
                // ????????????
                for (List<Integer> award : staticProp.getRewardList()) {
                    rewardDataManager.addAward(player, award.get(0), award.get(1), award.get(2),
                            AwardFrom.SYNTHETIC_PROP_AWARD);
                    builder.addChipAward(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2)));
                }
            }
        }
    }

    /**
     * @param costType
     * @param turnplat
     * @param conf
     * @param player
     * @param integral
     * @param activityType
     * @return List<Integer>
     * @Title: equipDoSweepstakes
     * @Description: ????????????-?????????????????????
     */
    private List<Integer> equipDoSweepstakes(int costType, EquipTurnplat turnplat, StaticEquipTurnplateConf conf,
                                             Player player, int integral, int activityType) {
        int goldCnt = turnplat.getGoldCnt();// ????????????????????????
        Set<Integer> winCnt = turnplat.getWinCnt();// ???????????????????????????
        List<Integer> awardList = new ArrayList<>();// ???????????????

        List<Integer> onlyAwardList = new ArrayList<>();// ?????????????????????????????????
        boolean falg = true;// ????????????????????????????????????
        for (int i : winCnt) {
            if (i >= goldCnt) {
                falg = false;// ????????????????????? ??????????????????????????? ????????????????????????????????????
                break;
            }
        }
        // ???????????????????????????????????????????????????
        if (falg && goldCnt > 0) {// goldCnt > 0 ??????????????? ???????????????
            onlyAwardList = conf.getOnlyAward().get(0);
        }
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType || ActivityConst.LUCKY_TURNPLATE_PROP == costType) { // ????????????
            // ????????????????????????
            awardList = equipDoSweepstakesAwards(conf, player, turnplat, onlyAwardList);
            getTurnplatePoint(integral, awardList, 4);
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) { // ????????????
            // ???????????????????????????
            List<Integer> confGlobalNums = ActParamConstant.EQUIP_TURNPLATE_GLOBAL_NUM;
            // ??????????????????
            int globalNum = activityDataManager.getGlobalActivity(activityType).getEquipTurLuckNums();
            if (winCnt.contains(goldCnt) || confGlobalNums.contains(globalNum + 1)) { // ???????????????????????? ??? ???????????????????????????
                awardList = conf.getOnlyAward().get(0);
                getTurnplatePoint(integral, awardList, 3);
                activityDataManager.updActivity(player, ActivityConst.ACT_EQUIP_TURNPLATE, 1,
                        EquipTurnplat.SPECIAL_SORT, false); // ???????????????????????????????????????
            } else { // ??????????????????
                // ????????????????????????
                awardList = equipDoSweepstakesAwards(conf, player, turnplat, onlyAwardList);
                getTurnplatePoint(integral, awardList, 4);
            }

            // ??????????????????????????????
            for (List<Integer> sendChat : ActParamConstant.EQUIP_TURNPLATE_SEND_CHAT_AWARD) {
                if (sendChat.get(0).equals(awardList.get(0)) && sendChat.get(1).equals(awardList.get(1))) {
                    // ???????????????
                    chatDataManager.sendSysChat(ChatConst.CHAT_EQUIP_TURNPLATE_GLOBAL_NUM, player.lord.getCamp(), 0,
                            player.lord.getCamp(), player.lord.getNick(), awardList.get(0), awardList.get(1));
                    //??????????????????
                    chatDataManager.sendActivityChat(ChatConst.CHAT_EQUIP_TURNPLATE_GLOBAL_NUM, conf.getType(), 0,
                            player.lord.getCamp(), player.lord.getNick(), awardList.get(0), awardList.get(1));
                }
            }
        }
        return awardList;
    }

    /**
     * @param conf
     * @param player
     * @param turnplat
     * @param onlyAwardList ????????????
     * @return List<Integer>
     * @Title: EquipDoSweepstakesAwards
     * @Description: ????????????-????????????????????????
     */
    private List<Integer> equipDoSweepstakesAwards(StaticEquipTurnplateConf conf, Player player, EquipTurnplat turnplat,
                                                   List<Integer> onlyAwardList) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        list = conf.getAwardList();
        if (onlyAwardList != null && onlyAwardList.size() > 0) {
            list.add(onlyAwardList);
        }
        List<Integer> awardList = null;
        boolean falg = true;
        while (falg) {
            awardList = RandomUtil.getRandomByWeight(list, 3, false);
            if (awardList != null && awardList.size() > 0) {
                falg = false;
            }
        }
        // ??????????????????????????????
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get(0) == conf.getOnlyAward().get(0).get(0)
                    && list.get(i).get(1) == conf.getOnlyAward().get(0).get(1)) {
                list.remove(i);
                break;
            }
        }
        return awardList;
    }

    /**
     * @param equipTurnplat
     * @param equipConf
     * @return void
     * @Title: EquipDoSearchWinCnt
     * @Description: ????????????-???????????????????????????????????????
     */
    private void equipDoSearchWinCnt(EquipTurnplat equipTurnplat, StaticEquipTurnplateConf equipConf) {
        // ?????????????????????????????????
        equipTurnplat.getWinCnt().clear();
        List<Integer> probability = equipConf.getProbability();
        RandomHelper.randomWinCnt(probability, equipTurnplat.getWinCnt());
    }

    // ===========================?????????????????? end===========================

    /* ---------------------------????????????start----------------------------- **/

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPayTurnplateRs getPayTurnplate(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_PAY_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        GetPayTurnplateRs.Builder builder = GetPayTurnplateRs.newBuilder();
        Long cnt = activity.getStatusCnt().get(0);
        int payGold = cnt == null ? 0 : cnt.intValue();
        builder.setPaySum(payGold);
        builder.setRemainCnt(activityDataManager.getPayTurnplateCnt(player, activity));
        List<StaticActPayTurnplate> actPayTurnplateList = StaticActivityDataMgr
                .getActPayTurnplateListByActId(activity.getActivityId());
        actPayTurnplateList.forEach(sapt -> {
            int status = activity.getStatusMap().containsKey(sapt.getId()) ? 1 : 0;
            ActivityCond cond = PbHelper.createActivityCondPbByPayTurnplate(sapt, status);
            builder.addActivityCond(cond);
        });
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public PlayPayTurnplateRs playPayTurnplate(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_PAY_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        int payTurnplateCnt = activityDataManager.getPayTurnplateCnt(player, activity);
        if (payTurnplateCnt < 1) {
            throw new MwException(GameError.ACT_PAY_TURNPLATE_CNT_NOT_ENOUGH.getCode(), "??????????????????????????????, roleId:,", roleId);
        }
        int activityId = activity.getActivityId();
        List<StaticActPayTurnplate> actPayTurnplateList = StaticActivityDataMgr
                .getActPayTurnplateListByActId(activityId);
        Map<Integer, Integer> statusMap = activity.getStatusMap();// ?????????????????????
        final int curCnt = statusMap.size();

        // ????????????????????????????????????
        List<StaticActPayTurnplate> payTurnplateList = actPayTurnplateList.stream()
                .filter(sapt -> sapt.getDownFrequency() <= curCnt && !statusMap.containsKey(sapt.getId()))
                .collect(Collectors.toList());

        if (CheckNull.isEmpty(payTurnplateList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "??????????????????????????????, roleId:,", roleId);
        }
        // ???????????????
        StaticActPayTurnplate hitActPayTurnplate = RandomUtil.getWeightByList(payTurnplateList,
                sapt -> sapt.getWeight());
        if (hitActPayTurnplate == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????????????????, roleId:,", roleId);
        }

        // ??????????????????
        int gainId = hitActPayTurnplate.getId();
        statusMap.put(gainId, 1);
        Award awardRes = rewardDataManager.addAwardSignle(player, hitActPayTurnplate.getAward(),
                AwardFrom.PAY_TURNPLATE);
        LogLordHelper.commonLog("payTurnplate", AwardFrom.PAY_TURNPLATE, player, curCnt, gainId);
        LogLordHelper.gameLog(LogParamConstant.TURNTABLE_ACT, player, AwardFrom.LOG_TURNTABLE_ACT, ActivityConst.ACT_PAY_TURNPLATE, activityId, 1, 1, awardRes);

        PlayPayTurnplateRs.Builder builder = PlayPayTurnplateRs.newBuilder();
        builder.addAward(awardRes);
        builder.setGainId(gainId);
        return builder.build();
    }

    /* ----------------------------????????????end---------------------------- **/
    /* ---------------------------????????????start----------------------------- **/

    /**
     * @param roleId
     * @return GetOreTurnplateRs
     * @throws MwException
     * @Title: getOreTurnplate
     * @Description: ??????????????????
     */
    public GetOreTurnplateRs getOreTurnplate(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        GetOreTurnplateRs.Builder builder = GetOreTurnplateRs.newBuilder();
        builder.setSurplus(activity.getStatusMap().get(ActivityConst.SURPLUS_GOLD) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_GOLD));// ?????????????????????????????????

        builder.setRemainCnt(activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM));// ????????????????????????
        // ????????????????????????
        int gear = activityDataManager.getOreTurnplateGear(activity);
        builder.setGear(gear);
        // ??????????????????
        List<StaticActOreTurnplate> actOreTurnplateList = StaticActivityDataMgr
                .getActOreTurnplateListByActId(activity.getActivityId());
        actOreTurnplateList.forEach(sapt -> {
            ActivityCond cond = PbHelper.createActivityCondPbByOreTurnplate(sapt);
            builder.addActivityCond(cond);
        });
        return builder.build();
    }

    /**
     * @param roleId
     * @return PlayOreTurnplateRs
     * @throws MwException
     * @Title: playOreTurnplate
     * @Description: ??????????????????
     */
    public PlayOreTurnplateRs playOreTurnplate(long roleId, int nums) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        // ??????????????????
        int oreTurnplateCnt = activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM);
        if (oreTurnplateCnt < 0 || oreTurnplateCnt < nums) {
            throw new MwException(GameError.ACT_ORE_TURNPLATE_CNT_NOT_ENOUGH.getCode(), "??????????????????????????????, roleId:,", roleId);
        }

        int activityId = activity.getActivityId();
        // ??????????????????????????????
        List<StaticActOreTurnplate> actOreTurnplateList = StaticActivityDataMgr
                .getActOreTurnplateListByActId(activityId);

        // ????????????????????????????????????
        List<StaticActOreTurnplate> oreTurnplateList = actOreTurnplateList.stream().collect(Collectors.toList());

        if (CheckNull.isEmpty(oreTurnplateList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "??????????????????????????????, roleId:,", roleId);
        }

        PlayOreTurnplateRs.Builder builder = PlayOreTurnplateRs.newBuilder();

        while (nums > 0) {
            // ???????????????
            StaticActOreTurnplate hitActOreTurnplate = RandomUtil.getWeightByList(oreTurnplateList,
                    sapt -> sapt.getWeight());
            if (hitActOreTurnplate == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????????????????, roleId:,", roleId);
            }
            // ????????????+1
            int alreadyNum = activity.getStatusMap().get(ActivityConst.ALREADY_NUM) == null ? 0
                    : activity.getStatusMap().get(ActivityConst.ALREADY_NUM);
            activity.getStatusMap().put(ActivityConst.ALREADY_NUM, alreadyNum + 1);

            // ????????????-1
            oreTurnplateCnt = activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                    : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM);
            activity.getStatusMap().put(ActivityConst.SURPLUS_NUM, oreTurnplateCnt - 1);

            player.activitys.put(activity.getActivityType(), activity);

            int gainId = hitActOreTurnplate.getId();
            Award awardRes = rewardDataManager.addAwardSignle(player, hitActOreTurnplate.getAward(),
                    AwardFrom.ORE_TURNPLATE);
            LogLordHelper.commonLog("oreTurnplate", AwardFrom.ORE_TURNPLATE, player, gainId);
            builder.addAward(awardRes);
            builder.addGainId(gainId);
            nums--;
        }
        return builder.build();
    }

    /* ----------------------------????????????end---------------------------- **/

    /* ---------------------------????????????-??? start----------------------------- **/

    /**
     * @param roleId
     * @return GetOreTurnplateNewRs
     * @throws MwException
     * @Title: getOreTurnplateNew
     * @Description: ??????????????????-???
     */
    public GetOreTurnplateNewRs getOreTurnplateNew(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE_NEW);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        GetOreTurnplateNewRs.Builder builder = GetOreTurnplateNewRs.newBuilder();

        // ??????????????????
        List<StaticActOreTurnplate> actOreTurnplateList = StaticActivityDataMgr
                .getActOreTurnplateListByActId(activity.getActivityId());
        actOreTurnplateList.forEach(sapt -> {
            ActivityCond cond = PbHelper.createActivityCondPbByOreTurnplate(sapt);
            builder.addActivityCond(cond);
        });
        return builder.build();
    }

    /**
     * @param roleId
     * @param nums
     * @return PlayOreTurnplateNewRs
     * @throws MwException
     * @Title: playOreTurnplateNew
     * @Description: ??????????????????-???
     */
    public PlayOreTurnplateNewRs playOreTurnplateNew(long roleId, int nums) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE_NEW);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????, roleId:,", roleId);
        }
        // ??????????????????
        if (nums != 1 && nums != 10) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????, roleId:,", roleId, ",nums:", nums);
        }

        int activityId = activity.getActivityId();
        // ??????????????????????????????
        List<StaticActOreTurnplate> actOreTurnplateList = StaticActivityDataMgr
                .getActOreTurnplateListByActId(activityId);

        // ????????????????????????????????????
        List<StaticActOreTurnplate> oreTurnplateList = actOreTurnplateList.stream().collect(Collectors.toList());

        if (CheckNull.isEmpty(oreTurnplateList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "??????????????????????????????, roleId:,", roleId);
        }

        // ????????????????????????
        List<List<Integer>> goldLists = ActParamConstant.ORE_TRUNPLATE_GOLD;
        if (CheckNull.isEmpty(goldLists)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????????????????, roleId:,", roleId, ",goldLists:",
                    goldLists);
        }
        List<Integer> goldList = new ArrayList<Integer>();
        for (List<Integer> list : goldLists) {
            if (list.size() > 2 && list.get(0) == activityId) {// ????????????id ??????????????????????????? ??????
                goldList = list;
            }
        }
        if (CheckNull.isEmpty(goldList)) {// ???????????????
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????????????????, roleId:,", roleId, ",activityId:",
                    activityId);
        }

        PlayOreTurnplateNewRs.Builder builder = PlayOreTurnplateNewRs.newBuilder();
        ChangeInfo change = ChangeInfo.newIns();// ??????????????????????????????
        int needGold = 0;
        if (nums == 1) {// ??????
            needGold = goldList.get(1);
        } else {// ?????????
            needGold = goldList.get(2);
        }

        if (needGold > 0) {
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "??????????????????");
            rewardDataManager.subGold(player, needGold, AwardFrom.ORE_TURNPLATE_GOLD);
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
        }
        int count = nums;
        while (count > 0) {
            // ???????????????
            StaticActOreTurnplate hitActOreTurnplate = RandomUtil.getWeightByList(oreTurnplateList,
                    sapt -> sapt.getWeight());
            if (hitActOreTurnplate == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????????????????, roleId:,", roleId);
            }

            int gainId = hitActOreTurnplate.getId();
            Award awardRes = rewardDataManager.addAwardSignle(player, hitActOreTurnplate.getAward(),
                    AwardFrom.ORE_TURNPLATE);
            LogLordHelper.commonLog("oreTurnplateNew", AwardFrom.ORE_TURNPLATE, player, gainId);
            builder.addAward(awardRes);
            builder.addGainId(gainId);
            count--;
        }

        // ??????????????????
        if (!CheckNull.isEmpty(ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW)) {
            rewardDataManager.addAward(player, ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(0),
                    ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(1), nums, AwardFrom.ORE_TURNPLATE_GOLD_AWARD_NEW);
            builder.addLotteryAward(PbHelper.createAwardPb(ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(0),
                    ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(1), nums));
        }

        // ?????????????????????????????????
        rewardDataManager.syncRoleResChanged(player, change);

        return builder.build();
    }

    /* ----------------------------????????????-??? end---------------------------- **/

    // ==================================??????????????????start==================================

    /**
     * ????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetSupplyDorpRs getSupplyDorp(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int activityType = ActivityConst.ACT_SUPPLY_DORP;

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,??????????????? roleId:", roleId);
        }
        StaticActivityPlan plan = activityBase.getPlan();
        long now = System.currentTimeMillis();
        long beginTime = activityBase.getBeginTime().getTime();
        long mailTime = activityBase.getSendMailTime() == null ? 0 : activityBase.getSendMailTime().getTime();
        long endTime = activityBase.getEndTime().getTime();
        boolean isBuy = now > beginTime && now < endTime;
        if (now < beginTime || (mailTime != 0 && now > mailTime)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,????????? roleId:", roleId);
        }
        // ????????????????????????
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????,??????????????? roleId:", roleId);
        }
        List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        List<CommonPb.TwoInt> actAwardList = new ArrayList<>();

        for (StaticActAward actAward : awardList) {
            int state = 0;
            int keyId = actAward.getKeyId();
            if (activity.getStatusMap().containsKey(keyId)) {
                state = 2;// ?????????
            } else {
                state = isSupplyDorpGet(actAward, activity, now) ? 1 : 0;
            }
            TwoInt twoInt = PbHelper.createTwoIntPb(keyId, state);
            actAwardList.add(twoInt);
        }
        GetSupplyDorpRs.Builder builder = GetSupplyDorpRs.newBuilder();
        builder.setIsBuy(isBuy);
        builder.addAllAwardList(actAwardList);
        builder.addAllParams(activity.getStatusCnt().keySet());
        return builder.build();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param actAward
     * @param activity
     * @return true ?????????
     */
    private boolean isSupplyDorpGet(StaticActAward actAward, Activity activity, long now) {
        if (CheckNull.isEmpty(actAward.getParam())) {
            return false;
        }
        if (activity.getStatusMap().containsKey(actAward.getKeyId())) {
            return false;// ????????????
        }
        int param = actAward.getParam().get(0);
        if (!activity.getStatusCnt().containsKey(param)) {
            return false; // ????????????
        }
        Date nowDate = new Date(now);
        Date buyDate = new Date(activity.getStatusCnt().get(param));// ??????????????????
        int dayiy = DateHelper.dayiy(buyDate, nowDate);
        int cond = actAward.getCond();
        if (dayiy >= cond) {
            return true; // ??????
        }
        return false;
    }

    /**
     * ??????????????????????????????
     *
     * @param activity
     * @param param
     * @return true??????????????????
     */
    private boolean checkSupplyIsAllGot(Activity activity, int param) {
        List<StaticActAward> awardByParam = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        if (CheckNull.isEmpty(awardByParam)) {
            return false;
        }
        for (StaticActAward ssa : awardByParam) {
            if (!activity.getStatusMap().containsKey(ssa.getKeyId())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSupplyIsAllGot(Activity activity) {
        for (Integer param : activity.getStatusCnt().keySet()) {
            if (!checkSupplyIsAllGot(activity, param)) {
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param param
     * @return
     * @throws MwException
     */
    public SupplyDorpAwardRs supplyDorpAward(long roleId, int param, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int activityType = ActivityConst.ACT_SUPPLY_DORP;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,??????????????? , roleId:", roleId);
        }
        StaticActivityPlan plan = activityBase.getPlan();
        long now = System.currentTimeMillis();
        long beginTime = activityBase.getBeginTime().getTime();
        long mailTime = activityBase.getSendMailTime() == null ? 0 : activityBase.getSendMailTime().getTime();
        if (now < beginTime || (mailTime != 0 && now > mailTime)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,?????????  , roleId:", roleId);
        }
        // ????????????????????????
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????,??????????????? , roleId:", roleId);
        }
        if (!activity.getStatusCnt().containsKey(param)) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "??????????????????,????????? , roleId:", roleId, ", param:",
                    param);
        }

        List<StaticActAward> sAwardList = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        if (CheckNull.isEmpty(sAwardList)) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), "??????????????????,????????????????????? roleId:", roleId, ", param:",
                    param);
        }
        SupplyDorpAwardRs.Builder builder = SupplyDorpAwardRs.newBuilder();
        for (StaticActAward sAward : sAwardList) {
            if (keyId != sAward.getKeyId()) {
                continue;
            }
            // ?????????????????????
            if (!isSupplyDorpGet(sAward, activity, now)) {
                continue;
            }
            // ?????????????????????
            activity.getStatusMap().put(sAward.getKeyId(), 1);
            // ????????????
            builder.addAllAward(
                    rewardDataManager.sendReward(player, sAward.getAwardList(), AwardFrom.ACT_SUPPLY_DORP_AWARD));// "??????????????????????????????"
        }

        for (StaticActAward actAward : sAwardList) {
            int state = 0;
            if (activity.getStatusMap().containsKey(actAward.getKeyId())) {
                state = 2;// ?????????
            } else {
                state = isSupplyDorpGet(actAward, activity, now) ? 1 : 0;
            }
            TwoInt twoInt = PbHelper.createTwoIntPb(actAward.getKeyId(), state);
            builder.addAwardList(twoInt);
        }
        // ????????????????????????,????????????????????????
        //        if (hasAwardGet && checkSupplyIsAllGot(activity, param)) {
        //            List<StaticActAward> awardByParam = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        //            int needGold = awardByParam.get(0).getTaskType(); //?????????????????????
        //            Award awrad = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, needGold);
        //            List<Award> awards = new ArrayList<>();
        //            awards.add(awrad);
        //            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_SUPPLY_DORP_RETURN,
        //                    AwardFrom.ACT_SUPPLY_DORP_RETURN, TimeHelper.getCurrentSecond(), param, param);
        //        }
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param param  ?????? 1 ?????? 2 ?????? 3 ??????
     * @return
     * @throws MwException
     */
    public SupplyDorpBuyRs supplyDorpBuy(long roleId, int param) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (param < 1 || param > 3) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????????????????  param:", param, ", roleId:", roleId);
        }
        int activityType = ActivityConst.ACT_SUPPLY_DORP;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,??????????????? , roleId:", roleId);
        }
        StaticActivityPlan plan = activityBase.getPlan();
        long now = System.currentTimeMillis();
        long beginTime = activityBase.getBeginTime().getTime();
        long endTime = activityBase.getEndTime().getTime();
        if (now < beginTime || now > endTime) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ???????????????????????????,????????? , roleId:", roleId);
        }
        // ????????????????????????
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????????????????,??????????????? , roleId:", roleId);
        }
        // ?????????????????????
        // ?????????????????????Activity?????? StatusCnt???, key???param?????????id, value??????????????????????????????
        if (activity.getStatusCnt().containsKey(param)) {
            throw new MwException(GameError.ACTIVITY_IS_JOIN.getCode(), "??????????????????,???????????????????????? roleId:", roleId, ", param:",
                    param);
        }
        // ????????????????????????
        List<StaticActAward> awardByParam = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        if (CheckNull.isEmpty(awardByParam)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????????????????, roleId:", roleId);
        }
        int needGold = awardByParam.get(0).getTaskType();

        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                AwardFrom.ACTIVITY_BUY);
        activity.getStatusCnt().put(param, System.currentTimeMillis());
        SupplyDorpBuyRs.Builder builder = SupplyDorpBuyRs.newBuilder();
        builder.addAllParams(activity.getStatusCnt().keySet());

        // ???????????????????????????
        List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        List<CommonPb.TwoInt> actAwardList = new ArrayList<>();
        awardList.stream().filter(saa -> saa.getParam().get(0) == param).forEach(actAward -> {
            int state = 0;
            int keyId = actAward.getKeyId();
            if (activity.getStatusMap().containsKey(keyId)) {
                state = 2;// ?????????
            } else {
                state = isSupplyDorpGet(actAward, activity, now) ? 1 : 0;
            }
            TwoInt twoInt = PbHelper.createTwoIntPb(keyId, state);
            actAwardList.add(twoInt);

        });
        builder.addAllAwardList(actAwardList);
        return builder.build();
    }

    // ==================================??????????????????end==================================

    // ==================================????????????start==================================

    /**
     * ????????????
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GiftShowRs giftShow(GiftShowRq req, long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_VIP_BAG);
        int param = req.getParam();
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????,????????? roleId:", roleId);
        }
        GiftShowRs.Builder builder = GiftShowRs.newBuilder();
        Date nowDate = new Date();
        List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftPackPlanByDate(player.account.getCreateDate(), serverSetting.getOpenServerDate(), nowDate);
        planList.forEach(plan -> {
            Long cut = activity.getStatusCnt().get(plan.getKeyId());
            builder.addGiftInfo(PbHelper.createTwoIntPb(plan.getGiftpackId(), cut != null ? cut.intValue() : 0));
            builder.addGiftTime(PbHelper.createTwoIntPb(plan.getGiftpackId(), TimeHelper.dateToSecond(Optional.ofNullable(plan.getDateList(serverSetting.getOpenServerDate(), player.account.getCreateDate(), nowDate).get(1)).orElse(new Date()))));
            builder.addDurationTime(PbHelper.createTwoIntPb(plan.getGiftpackId(), plan.getOpenDuration()));
        });
        // List<TwoInt> giftInfo = new ArrayList<>();
        // List<TwoInt> giftTime = new ArrayList<>();
        // List<TwoInt> durationTime = new ArrayList<>();
        // for (StaticGiftpackPlan plan : planList) {
        //     Long cut = activity.getStatusCnt().get(plan.getKeyId());
        //     giftInfo.add(PbHelper.createTwoIntPb(plan.getGiftpackId(), cut != null ? cut.intValue() : 0));
        //     giftTime.add(PbHelper.createTwoIntPb(plan.getGiftpackId(),
        //             plan.getEndTimeSecond(serverSetting.getOpenServerDate(), player.account.getCreateDate(), nowDate)));
        //     durationTime.add(PbHelper.createTwoIntPb(plan.getGiftpackId(), plan.getOpenDuration()));
        // }
        // builder.addAllGiftInfo(giftInfo);
        // builder.addAllGiftTime(giftTime);
        // builder.addAllDurationTime(durationTime);
        // ?????????IOS?????????
        List<StaticPay> staticPays = StaticVipDataMgr.getPayList().stream().filter(pay -> pay.getBanFlag() == PayService.FLAG_PAY_GIFT)
                .collect(Collectors.toList());
        Collection<CommonPb.PayInfo> payInfos = PbHelper.createPayInfo(staticPays);
        builder.addAllPayInfo(payInfos);
        return builder.build();
    }

    /**
     * ????????????
     *
     * @param player
     */
    public void syncGiftShow(Player player) {
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_VIP_BAG);
        if (activity == null) {
            return;
        }
        Date nowDate = new Date();
        List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftPackPlanByDate(player.account.getCreateDate(), serverSetting.getOpenServerDate(), nowDate);
        SyncGiftShowRs.Builder builder = SyncGiftShowRs.newBuilder();
        planList.forEach(plan -> {
            Long cut = activity.getStatusCnt().get(plan.getKeyId());
            builder.addGiftInfo(PbHelper.createTwoIntPb(plan.getGiftpackId(), cut != null ? cut.intValue() : 0));
            builder.addGiftTime(PbHelper.createTwoIntPb(plan.getGiftpackId(), TimeHelper.dateToSecond(Optional.ofNullable(plan.getDateList(serverSetting.getOpenServerDate(), player.account.getCreateDate(), nowDate).get(1)).orElse(new Date()))));
            builder.addDurationTime(PbHelper.createTwoIntPb(plan.getGiftpackId(), plan.getOpenDuration()));
        });
        // List<TwoInt> giftInfo = new ArrayList<>();
        // List<TwoInt> giftTime = new ArrayList<>();
        // List<TwoInt> durationTime = new ArrayList<>();
        // for (StaticGiftpackPlan plan : planList) {
        //     Long cut = activity.getStatusCnt().get(plan.getKeyId());// ?????????keyId
        //     giftInfo.add(PbHelper.createTwoIntPb(plan.getGiftpackId(), cut != null ? cut.intValue() : 0));
        //     giftTime.add(PbHelper.createTwoIntPb(plan.getGiftpackId(),
        //             plan.getEndTimeSecond(serverSetting.getOpenServerDate(), player.account.getCreateDate(), nowDate)));
        //     durationTime.add(PbHelper.createTwoIntPb(plan.getGiftpackId(), plan.getOpenDuration()));
        // }
        // builder.addAllGiftInfo(giftInfo);
        // builder.addAllGiftTime(giftTime);
        // builder.addAllDurationTime(durationTime);
        Base.Builder msg = PbHelper.createSynBase(SyncGiftShowRs.EXT_FIELD_NUMBER, SyncGiftShowRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    // ==================================????????????end==================================

    public Activity checkActivity(Player player, int activityType) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ????????????,??????????????? roleId:", player.roleId,
                    ", activityType:", activityType);
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.OPEN_STEP) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ????????????,??????????????? roleId:", player.roleId,
                    ", activityType:", activityType);
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            // sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ????????????,??????????????? roleId:", player.roleId,
                    ", activityType:", activityType);
        }
        return activity;
    }

    /**
     * ??????????????????????????????
     */
    public GetFreePowerRs getFreePower(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkActivity(player, ActivityConst.ACT_FOOD);

        final int createRoleDate = DateHelper.dayiy(player.account.getCreateDate(), new Date());
        final StaticActivityTime[] staticActivityTime = {null};
        if (createRoleDate == 1) {
            // ???????????????
            Optional.ofNullable(StaticActivityDataMgr.getActivityTimeById(activity.getActivityId()))
                    .ifPresent(sats -> {
                        // ????????????????????????
                        staticActivityTime[0] = sats.stream()
                                .filter(sat -> {
                                    int key = sat.getKeyId();
                                    Long status = activity.getStatusCnt().get(key);
                                    // ????????????????????????
                                    return status != null && status == 1;
                                })
                                .min(Comparator.comparingInt(StaticActivityTime::getKeyId))
                                .orElse(null);


                    });

        } else {
            staticActivityTime[0] = ActivityDataManager.getCurActivityTime(activity.getActivityId());
        }
        if (null == staticActivityTime[0]) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "??????????????????????????????,????????? roleId:", roleId);
        }

        int state = activity.getStatusCnt().get(staticActivityTime[0].getKeyId()).intValue();
        if (state == 2) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), " ?????????????????????????????? roleId:", roleId);
        }

        // ?????????????????????1 ????????????2 ?????????
        activity.setEndTime(TimeHelper.getCurrentDay());// ????????????
        activity.getStatusCnt().put(staticActivityTime[0].getKeyId(), 2L);

        // ????????????
        GetFreePowerRs.Builder builder = GetFreePowerRs.newBuilder();
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        List<List<Integer>> awardList = new ArrayList<>();
        // ????????????
        for (List<Integer> award : staticActivityTime[0].getAwardList()) {
            awardList.add(award);
        }
        // ????????????
        for (List<Integer> rAward : staticActivityTime[0].getRandomAward()) {
            if (rAward.size() != 4) {
                continue;// ?????????????????????
            }
            if (RandomHelper.isHitRangeIn100(rAward.get(3))) {
                List<Integer> award = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    award.add(rAward.get(i));
                }
                awardList.add(award);
            }
        }
        builder.addAllReward(rewardDataManager.sendReward(player, awardList, num, AwardFrom.ACT_POWER_GIVE_REWARD));
        return builder.build();
    }

    /**
     * ??????????????????????????????
     */
    public GetPowerGiveDataRs getPowerGiveData(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkActivity(player, ActivityConst.ACT_FOOD);

        refreshPowerState(roleId, activity); // ????????????????????????

        GetPowerGiveDataRs.Builder builder = GetPowerGiveDataRs.newBuilder();
        List<StaticActivityTime> list = StaticActivityDataMgr.getActivityTimeById(activity.getActivityId());
        if (!CheckNull.isEmpty(list)) {
            for (StaticActivityTime sat : list) {
                Long state = activity.getStatusCnt().get(sat.getKeyId());
                builder.addState(state == null ? 0 : state.intValue());
            }
        }
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param activity
     * @throws MwException
     */
    private void refreshPowerState(long roleId, Activity activity) throws MwException {
        List<StaticActivityTime> list = StaticActivityDataMgr.getActivityTimeById(activity.getActivityId());
        if (CheckNull.isEmpty(list)) {// || !list.get(0).getOpenWeekDay().contains(weekDay)
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " ?????????????????????????????? roleId:", roleId);
        }
        int totday = TimeHelper.getCurrentDay();
        if (activity.getEndTime() != totday) {// ?????????????????????
            activity.setEndTime(totday);// ???endTime???????????????????????????????????????????????????????????????
            activity.getStatusCnt().clear();
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        Date now = new Date();
        // ???????????????
        if (DateHelper.dayiy(beginTime, now) == 1) {
            list.forEach(sat -> {
                int key = sat.getKeyId();
                Long status = activity.getStatusCnt().get(key);
                // ????????????????????????
                if (status != null && status == 2) {
                    return;
                }
                Date date = DateHelper.afterStringTime(now, sat.getEndTime());
                // ??????????????????
                boolean state = DateHelper.inThisTime(new Date(), sat.getStartTime(), sat.getEndTime()) || DateHelper.isAfterTime(now, date);
                // ???????????????
                activity.getStatusCnt().put(sat.getKeyId(), state ? 1L : 0L);
            });
        } else {
            StaticActivityTime curSat = ActivityDataManager.getCurActivityTime(activity.getActivityId());
            for (StaticActivityTime sat : list) {
                int key = sat.getKeyId();
                Long status = activity.getStatusCnt().get(key);
                if (status != null && status == 2) {// ????????????????????????
                    continue;
                }
                boolean state = curSat != null && curSat.getKeyId() == sat.getKeyId();
                activity.getStatusCnt().put(sat.getKeyId(), state ? 1L : 0L);// ???????????????????????????0 ???????????????1 ????????????2 ?????????
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public ActGrowBuyRs growBuy(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkActivity(player, ActivityConst.ACT_LEVEL);
        Long status = activity.getStatusCnt().get(0);
        if (status != null && status > 0) {
            throw new MwException(GameError.ACTIVITY_IS_JOIN.getCode(), " ???????????????????????? roleId:", roleId);
        }
        List<Integer> need = ActParamConstant.OPEN_ACT_GROW_NEED;
        if (need.isEmpty() || need.size() < 2) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ???????????????????????? roleId:", roleId);
        }
        if (need.get(0) > player.lord.getVip() || need.get(1) <= 0) {
            throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), " ???????????????????????? roleId:", roleId);
        }
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, need.get(1),
                AwardFrom.ACQUISITE);
        activity.getStatusCnt().put(0, TimeHelper.getCurrentSecond() * 1L);
        ActGrowBuyRs.Builder builder = ActGrowBuyRs.newBuilder();
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    public void saveActivityTimerLogic() {
        Iterator<GlobalActivityData> iterator = activityDataManager.getActivityMap().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        int saveCount = 0;
        while (iterator.hasNext()) {
            GlobalActivityData usualActivity = iterator.next();
            if (now - usualActivity.getLastSaveTime() >= DataSaveConstant.ACTIVITY_DATA_SAVE_INTERVAL_SECOND) {
                saveCount++;
                try {
                    usualActivity.setLastSaveTime(now);
                    SaveGlobalActivityServer.getIns().saveData(usualActivity.copyData());
                } catch (Exception e) {
                    // LogHelper.ERROR_LOGGER.error("save activity {" + usualActivity.getActivityId() + "} data error",
                    // e);
                    LogUtil.error("save activity {" + usualActivity.getActivityId() + "} data error", e);
                }
            }
        }

        if (saveCount != 0) {
            // LogHelper.SAVE_LOGGER.trace("save activity count:" + saveCount);
            LogUtil.save("save activity count:" + saveCount);
        }
    }

    /**
     * ??????????????????????????? ????????????
     */
    public void syncAllPlayerActPower() {
        // ???????????????
        for (Player player : playerDataManager.getPlayers().values()) {
            if (player.isLogin) {
                activityDataManager.syncActChange(player, ActivityConst.ACT_FOOD);
            }
        }
        // ?????????????????????
        //  ?????????????????????????????????12???, ????????????id
        // int pushId = TimeHelper.getHour() == 12 ? PushConstant.ACT_POWER_TWELVE : PushConstant.ACT_POWER_SIX;
        //  ???????????????
        // playerDataManager.getPlayers().values().stream().filter(Player::canPushActPower).forEach(p -> {
        //     PushMessageUtil.pushMessage(p.account, pushId);
        // });
    }

    /**
     * ????????????????????????
     */
    public void sendUnrewardedMailBySupplyDorp(Player player, int now) {
        if (player == null) {
            return;
        }
        // ?????????????????? getActivityInfo??????,??????????????????????????????????????????
        Activity activity = player.activitys.get(ActivityConst.ACT_SUPPLY_DORP);
        if (activity == null) {
            return;
        }
        if (activity.getStatusCnt().isEmpty()) {// ?????????
            return;
        }
        List<Award> awards = new ArrayList<>();
        int glod = 0;
        for (Integer param : activity.getStatusCnt().keySet()) {
            //            if (checkSupplyIsAllGot(activity, param)) { // ?????????????????????
            //                continue;
            //            }
            List<StaticActAward> sAwardList = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(),
                    param);
            //???????????????????????????????????????????????????
            Date nowDate = new Date(now * 1000L);
            Date buyDate = new Date(activity.getStatusCnt().get(param));
            int dayiy = DateHelper.dayiy(buyDate, nowDate); //???????????????
            StaticActAward maxAward = StaticActivityDataMgr.getSupplyMaxByParam(activity.getActivityId(), param);
            if (dayiy == maxAward.getCond() + 1 && !activity.getStatusMap().containsKey(maxAward.getParam().get(0))) {
                glod += maxAward.getTaskType();
                // ?????????????????????
                activity.getStatusMap().put(maxAward.getParam().get(0), 1);
            }
            if (dayiy > maxAward.getCond()) {
                for (StaticActAward sAward : sAwardList) {
                    if (!activity.getStatusMap().containsKey(sAward.getKeyId())) {// ????????????
                        // ?????????????????????
                        activity.getStatusMap().put(sAward.getKeyId(), 1);
                        awards.addAll(PbHelper.createAwardsPb(sAward.getAwardList()));
                    }
                }
            }
        }
        if (glod > 0) {
            Award gold = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, glod);
            awards.add(gold);
        }
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_SUPPLY_DORP_ALL_RETURN,
                    AwardFrom.ACT_SUPPLY_DORP_RETURN, now, glod, glod);
        }
    }

    /**
     * ????????????????????????
     *
     * @param actType
     */
    public void actAwardTimeProcess(int actType) {
        LogUtil.debug("---------????????????????????????:", actType);
        if (!StaticActivityDataMgr.isActTypeRank(actType)) {
            return;
        }
        GlobalActivityData usualActivityData = activityDataManager.getGlobalActivity(actType);
        if (usualActivityData == null) {
            return;
        }
        // ???????????????????????????
        LinkedList<ActRank> rankList = usualActivityData.getPlayerRanks(actType);
        if (!CheckNull.isEmpty(rankList)) {
            int rank = 1;
            Iterator<ActRank> it = rankList.iterator();
            while (it.hasNext()) {
                ActRank ar = it.next();
                ar.setRank(rank);
                Player p = playerDataManager.getPlayer(ar.getLordId());
                if (p != null) {
                    Activity activity = activityDataManager.getActivityInfo(p, actType);
                    if (activity != null) {
                        int topRankKey = 3;
                        activity.getStatusCnt().put(topRankKey, (long) rank);
                    }
                }
                rank++;
                // ??????????????????????????????
                // try {
                // activityDataManager.syncActChange(p, actType);
                // } catch (Exception e) {
                // LogUtil.error(e);
                // }
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    public void sendUnrewardedMailForCleanDay(String jobKeyName) {
        LogUtil.debug("??????????????????,??????????????????");
        // ????????????????????????????????????,?????????????????????23:59:50??????(??????Activity?????????,autoDayClean()?????????????????????)
        final int now = TimeHelper.getCurrentSecond() + 10;
        sendUnrewardedMail(ActivityConst.ACT_DAILY_PAY, now, jobKeyName);
        autoClearActData(ActivityConst.ACT_DAILY_PAY);
        // for (StaticActivity s : StaticActivityDataMgr.getStaticActivityMap().values()) {
        // if (s.getClean() == ActivityConst.CLEAN_DAY) {
        // sendUnrewardedMail(s.getType(), now);
        // }
        // }
        sendUnrewardedMail(ActivityConst.ACT_GIFT_PAY, now, jobKeyName);
        sendUnrewardedMail(ActivityConst.ACT_SUPPLY_DORP, now, jobKeyName);
    }

    /**
     * 7????????? ??????????????????
     *
     * @param player
     */
    public void checkAndSendPay7DayMail(Player player) {
        Date now = new Date();
        int day = playerDataManager.getCreateRoleDay(player, now);
        if (day == 8) {// ???8??????????????????
            int nowInt = (int) (now.getTime() / 1000);
            sendUnrewardedMailByNormal(player, ActivityConst.ACT_PAY_7DAY, nowInt);
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param actType
     */
    public void sendUnrewardedMail(int actType, String jobKeyName) {
        int now = TimeHelper.getCurrentSecond();
        sendUnrewardedMail(actType, now, jobKeyName);
    }

    public void autoClearActData(Integer actType) {
        for (Player player : playerDataManager.getPlayers().values()) {
            clearActData(player, actType, true);
        }
    }

    /**
     * ??????11???59???,??????????????????
     *
     * @param player
     * @param actType
     * @param isClean
     */
    private void clearActData(Player player, Integer actType, boolean isClean) {
        Activity activity = activityDataManager.getActivityInfo(player, actType);
        if (activity == null) {
            return;
        }
        if (isClean) {
            activity.getStatusCnt().clear();
        }
    }

    /**
     * ???????????????,???????????????????????????
     *
     * @param actType
     */
    public void autoExchangUnrewardeMail(Integer actType) {
        int now = TimeHelper.getCurrentSecond();
        // ??????
        for (Player player : playerDataManager.getPlayers().values()) {
            if (actType == ActivityConst.ACT_LUCKY_TURNPLATE || actType == ActivityConst.FAMOUS_GENERAL_TURNPLATE || actType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                    || actType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR || actType == ActivityConst.ACT_SEASON_TURNPLATE) {// ??????/??????
                autoExchangUnrewardeMailByTurnplate(player, actType, now);
            }
            if (actType == ActivityConst.ACT_EQUIP_TURNPLATE) {
                autoExchangUnrewardeMailByEquipTurnplate(player, actType, now);
            }
//            if(actType == ActivityConst.ACT_DIAOCHAN){
//                activityDiaoChanService.handleOver(player);
//            }
            if (actType == ActivityConst.ACT_DROP_CONTROL) {
                this.autoConvertDropMail(player);
            }
        }
        if (actType == ActivityConst.ACT_GOOD_LUCK) {
            activityLotteryService.autoExchangeByGoodLuck();
        } else if (actType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
            // ???????????????, ???????????????????????????????????????, ????????????????????????????????????
            chatDataManager.getActivityChat(ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR).clear();
        }
    }

    private void autoConvertDropMail(Player player) {
        Activity activity = player.activitys.get(ActivityConst.ACT_DROP_CONTROL);
        if (Objects.isNull(activity)) {
            return;
        }
        int activityId = activity.getActivityId();
        List<StaticActBandit> sActBandits = StaticActivityDataMgr.getActBanditList().stream().filter(sab -> sab.getActivityId() == activityId).collect(Collectors.toList());
        if (Objects.isNull(sActBandits)) {
            return;
        }
        List<Award> awards = new ArrayList<>();
        sActBandits.forEach(sab -> {
            List<List<Integer>> costList = null;
            int type = sab.getType();
            List<List<Integer>> drop = sab.getDrop();
            if (CheckNull.isEmpty(drop)) {
                return;
            }
            if (type == StaticActBandit.ACT_HIT_DROP_TYPE_1 || type == StaticActBandit.ACT_HIT_DROP_TYPE_4 || type == StaticActBandit.ACT_HIT_DROP_TYPE_5) {
                costList = drop;
            } else if (type == StaticActBandit.ACT_HIT_DROP_TYPE_2) {
                costList = drop.stream().map(list -> list.stream().skip(2).collect(Collectors.toList())).collect(Collectors.toList());
            } else if (type == StaticActBandit.ACT_HIT_DROP_TYPE_3) {
                costList = drop.stream().map(list -> list.stream().skip(2).collect(Collectors.toList())).collect(Collectors.toList());
            }
            costList.forEach(cost -> {
                long cnt = rewardDataManager.getRoleResByType(player, cost.get(0), cost.get(1));
                if (cnt <= 0) {
                    return;
                }
                try {
                    rewardDataManager.checkAndSubPlayerAllRes(player, cost.get(0), cost.get(1), AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
                } catch (MwException e) {
                    LogUtil.error(e);
                    return;
                }
                List<List<Integer>> convert = sab.getConvert();
                convert.forEach(list -> awards.add(PbHelper.createAwardPb(list.get(0), list.get(1), (int) (list.get(2) * cnt))));
            });
        });
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_BANDIT_CONVERT_AWARD, AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond());
        }
    }

    /**
     * ???????????????,???????????????????????????,?????????????????????
     *
     * @param player
     * @param actType
     * @param now
     */
    private void autoExchangUnrewardeMailByTurnplate(Player player, Integer actType, int now) {
        try {
            ActTurnplat activity = (ActTurnplat) player.activitys.get(actType);
            if (CheckNull.isNull(activity)) {
                return;
            }
            List<StaticTurnplateConf> turnplateConfs = StaticActivityDataMgr
                    .getActTurnPlateListByActId(activity.getActivityId());
            if (CheckNull.isEmpty(turnplateConfs)) {
                return;
            }
            StaticTurnplateConf turnplateConf = turnplateConfs.get(0);
            if (CheckNull.isNull(turnplateConf)) {
                return;
            }
            ChangeInfo change = ChangeInfo.newIns();
            // 207????????????
            if (actType == ActivityConst.FAMOUS_GENERAL_TURNPLATE && !CheckNull.isEmpty(Constant.FAMOUS_GENERAL_EXCHANGE_PROP)) {
                for (List<Integer> exchangeProp : Constant.FAMOUS_GENERAL_EXCHANGE_PROP) {
                    // ?????????????????????activityId
                    if (activity.getActivityId() != exchangeProp.get(3)) {
                        continue;
                    }
                    int roleResByType = (int) rewardDataManager.getRoleResByType(player, exchangeProp.get(0), exchangeProp.get(1));
                    if (roleResByType > 0) {
                        try {
                            rewardDataManager.subProp(player, exchangeProp.get(1), roleResByType, AwardFrom.EXCHANGE_FAMOUS_EXPIRED_EXCHANGE_REWARDE);
                            // ?????????????????????????????????
                            change.addChangeType(exchangeProp.get(0), exchangeProp.get(1));
                            rewardDataManager.syncRoleResChanged(player, change);
                            ArrayList<CommonPb.Award> awards = new ArrayList<>();
                            List<Integer> convertTargetList = null;
                            for (List<Integer> list : ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD) {
                                if (list.get(3) == activity.getActivityId()) {
                                    convertTargetList = list;
                                    break;
                                }
                            }
                            Optional.ofNullable(convertTargetList).ifPresent(tmps -> awards.add(PbHelper.createAwardPb(tmps.get(0), tmps.get(1), roleResByType * tmps.get(2))));

                            if (!awards.isEmpty()) {
                                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_EXCHANGE_REWARD,
                                        AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
                            }
                        } catch (MwException e) {
                            LogUtil.error(e, "??????/?????? ??????????????????,?????????????????????????????????");
                            return;
                        }
                    }
                }
            }
            List<List<Integer>> onlyAward = null;
            switch (actType) {
                case ActivityConst.ACT_LUCKY_TURNPLATE:
                    onlyAward = turnplateConf.getOnlyAward();
                    break;
                case ActivityConst.FAMOUS_GENERAL_TURNPLATE:
                    // onlyAward = Constant.FAMOUS_GENERAL_EXCHANGE_PROP;
                    // break;
                case ActivityConst.ACT_LUCKY_TURNPLATE_NEW:
                case ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR:
                case ActivityConst.ACT_SEASON_TURNPLATE:
                    onlyAward = turnplateConf.getSubstitute();
                    break;
            }
            if (CheckNull.isEmpty(onlyAward)) {
                return;
            }

            List<Integer> award = onlyAward.get(0);// ????????????????????????
            if (CheckNull.isEmpty(award)) {
                return;
            }
            // ????????????
            int chipCnt = (int) rewardDataManager.getRoleResByType(player, award.get(0), award.get(1));
            if (chipCnt > 0 && !CheckNull.isEmpty(isLuckyTurn(actType) ? ActParamConstant.ACT_TURNPLATE_EXCHANGE_AWRAD
                    : ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD)) {
                try {
                    rewardDataManager.subProp(player, award.get(1), chipCnt,
                            isLuckyTurn(actType) ? AwardFrom.TURNPLATE_EXPIRED_EXCHANGE_REWARDE
                                    : AwardFrom.EXCHANGE_FAMOUS_EXPIRED_EXCHANGE_REWARDE);// "??????????????????????????????"
                    // ?????????????????????????????????
                    change.addChangeType(award.get(0), award.get(1));
                    rewardDataManager.syncRoleResChanged(player, change);
                } catch (MwException e) {
                    LogUtil.error(e, "??????/?????? ??????????????????,?????????????????????????????????");
                    return;
                }
                List<Award> awards = new ArrayList<>();
                if (isLuckyTurn(actType)) {// ????????????
                    awards.add(PbHelper.createAwardPb(ActParamConstant.ACT_TURNPLATE_EXCHANGE_AWRAD.get(0),
                            ActParamConstant.ACT_TURNPLATE_EXCHANGE_AWRAD.get(1),
                            chipCnt * ActParamConstant.ACT_TURNPLATE_EXCHANGE_AWRAD.get(2)));
                }/* else if (actType == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
                awards.add(PbHelper.createAwardPb(ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD.get(0),
                        ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD.get(1),
                        chipCnt * ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD.get(2)));
            }*/ else {
                    List<List<Integer>> returnAward = turnplateConf.getReturnAward();
                    if (CheckNull.isEmpty(returnAward)) {
                        return;
                    }
                    awards.add(PbHelper.createAwardPb(returnAward.get(0).get(0), returnAward.get(0).get(1), chipCnt * returnAward.get(0).get(2)));
                }

                if (!awards.isEmpty()) {
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_EXCHANGE_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }


    /**
     * ???????????????,???????????????????????????,?????????????????????
     *
     * @param player
     * @param actType
     * @param now
     */
    private void autoExchangUnrewardeMailByEquipTurnplate(Player player, Integer actType, int now) {
        try {
            ActTurnplat activity = (ActTurnplat) player.activitys.get(actType);
            if (CheckNull.isNull(activity)) {
                return;
            }
            List<StaticEquipTurnplateConf> turnplateConfs = StaticActivityDataMgr
                    .getEquipTurnPlateListByActId(activity.getActivityId());
            if (CheckNull.isEmpty(turnplateConfs)) {
                return;
            }
            StaticEquipTurnplateConf turnplateConf = turnplateConfs.get(0);
            if (CheckNull.isNull(turnplateConf)) {
                return;
            }
            List<List<Integer>> onlyAward = turnplateConf.getSubstitute();
            if (CheckNull.isEmpty(onlyAward)) {
                return;
            }

            List<Integer> award = onlyAward.get(0);// ????????????????????????
            if (CheckNull.isEmpty(award)) {
                return;
            }
            ChangeInfo change = ChangeInfo.newIns();
            // ????????????
            int chipCnt = (int) rewardDataManager.getRoleResByType(player, award.get(0), award.get(1));
            if (chipCnt > 0) {
                try {
                    rewardDataManager.subProp(player, award.get(1), chipCnt, AwardFrom.EQUIP_TURNPLATE_EXPIRED_EXCHANGE_REWARDE);// "??????????????????????????????"
                    // ?????????????????????????????????
                    change.addChangeType(award.get(0), award.get(1));
                    rewardDataManager.syncRoleResChanged(player, change);
                } catch (MwException e) {
                    LogUtil.error(e, "??????/?????? ??????????????????,?????????????????????????????????");
                    return;
                }
                List<Award> awards = new ArrayList<>();
                List<List<Integer>> returnAward = turnplateConf.getReturnAward();
                if (CheckNull.isEmpty(returnAward)) {
                    return;
                }
                awards.add(PbHelper.createAwardPb(returnAward.get(0).get(0), returnAward.get(0).get(1), chipCnt * returnAward.get(0).get(2)));

                if (!awards.isEmpty()) {
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_EXCHANGE_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    /**
     * @param actType
     * @return boolean
     * @Title: isLuckyTurn
     * @Description: ?????????????????????
     */
    public boolean isLuckyTurn(int actType) {
        return actType == ActivityConst.ACT_LUCKY_TURNPLATE;
    }

    public void sendUnrewardedMail(int actType, int now, String jobKeyName) {
        if (actType == ActivityConst.ACT_VIP || actType == ActivityConst.ACT_GIFT_PAY
                || actType == ActivityConst.ACT_COST_GOLD || actType == ActivityConst.ACT_WAR_ROAD
                || actType == ActivityConst.ACT_CHARGE_TOTAL || actType == ActivityConst.ACT_WAR_PLANE_SEARCH
                || actType == ActivityConst.ACT_CHALLENGE_COMBAT || actType == ActivityConst.ACT_TRAINED_SOLDIERS
                || actType == ActivityConst.ACT_BIG_KILL || actType == ActivityConst.ACT_COLLECT_RESOURCES
                || actType == ActivityConst.ACT_RESOUCE_SUB || actType == ActivityConst.ACT_EQUIP_MATERIAL
                || actType == ActivityConst.ACT_ELIMINATE_BANDIT) {// ????????????????????????????????????????????????????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByNormal(player, actType, now);
            }
        } else if (actType == ActivityConst.ACT_CHARGE_CONTINUE
                || actType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {// ??????????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                activityChargeContinueService.sendUnrewardedMailByChargeContinue(player, actType, now);
            }
        } else if (actType == ActivityConst.ACT_SUPPLY_DORP) { // ????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailBySupplyDorp(player, now);
            }
        } else if (actType != ActivityConst.ACT_GESTAPO_RANK && actType != ActivityConst.ACT_ROYAL_ARENA && StaticActivityDataMgr.isActTypeRank(actType)) { // ????????????,??????????????????
            if (actType == ActivityConst.ACT_CHRISTMAS || actType == ActivityConst.ACT_REPAIR_CASTLE) {
                activityChristmasService.overAndSendMail(jobKeyName);
            } else {
                for (Player player : playerDataManager.getPlayers().values()) {
                    sendUnrewardedMailByRank(player, actType, now);
                }
                sendCampFightRankMail(actType, now);// ???????????????????????????????????????
            }
        } else if (actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY) {// ????????????, ????????????
            sendUnrewardedMailByActAllCharge(actType, now);
        } else if (actType == ActivityConst.ACT_DAILY_PAY) {// ????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByDailyPay(player, actType, now);
            }
        } else if (actType == ActivityConst.ACT_ATK_GESTAPO) {// ????????????
            Map<Integer, Integer> campRank = calcGestapoCampAward();// ????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByGestapo(player, actType, now);
                sendUnrewardedMailByGestapoRank(player, now, campRank);// ?????????????????????
            }
            // ?????????????????????????????????
            Iterator<Gestapo> iterator = worldDataManager.getGestapoMap().values().iterator();
            List<Integer> posList = null;
            while (iterator.hasNext()) {
                Gestapo gestapo = iterator.next();
                int pos = gestapo.getPos();
                try {
                    // ??????????????????,???????????????,????????????
                    warService.cancelGestapoBattle(pos);

                    // ????????????????????????(????????????,????????????????????????,?????????)
                    worldDataManager.removeBandit(pos, 2);

                    if (CheckNull.isEmpty(posList)) {
                        posList = new ArrayList<>();
                    }
                    // ??????????????????
                    posList.add(gestapo.getPos());
                } catch (Exception e) {
                    LogUtil.error(e, "??????????????????????????????????????????, pos:", pos);
                }
            }
            if (!CheckNull.isEmpty(posList)) {
                // ??????????????????????????????
                EventBus.getDefault().post(
                        new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            }

        } else if (actType == ActivityConst.ACT_ATTACK_CITY_NEW) {// ?????????????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByAtkCity(player, now);
            }
        } else if (actType == ActivityConst.ACT_PROP_PROMOTION) { // ????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByPromotion(player, now);
            }
        } else if (actType == ActivityConst.ACT_THREE_REBATE) {// ????????????
            Collection<Player> values = playerDataManager.getPlayers().values();
            LogUtil.debug("-----------????????????????????????:" + values.size() + "--------------------");
            for (Player player : playerDataManager.getPlayers().values()) {
                sendThreeRebateReward(player, now);
            }
        }
        // ????????????
        else if (actType == ActivityConst.ACT_BANDIT_AWARD) {
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByBanditAward(player, now);
            }
        } else if (actType == ActivityConst.ACT_ROYAL_ARENA) {
            Map<Integer, Integer> campRank = calcCampRoyalArenaAward();// ????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByRoyalArena(player, actType, now, campRank);
            }
        } else if (actType == ActivityConst.ACT_HOT_PRODUCT) {
            activityHotProductService.sendUnrewardedMail();
        } else if (actType == ActivityConst.ACT_MONSTER_NIAN) {
            activityMonsterNianService.sendUnrewardedMail(jobKeyName);
        } else if (actType == ActivityConst.ACT_ANNIVERSARY_FIREWORK) {
            activityFireWorkService.activityFinish(jobKeyName);
        }
    }

    /**
     * ???????????????????????????
     *
     * @return key:camp val:??????
     */
    private Map<Integer, Integer> calcCampRoyalArenaAward() {
        Map<Integer, Integer> map = new HashMap<>();
        GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getActivityMap().get(ActivityConst.ACT_ROYAL_ARENA);
        if (CheckNull.isNull(globalRoyalArena)) return map;

        List<CampRoyalArena> royalArenas = globalRoyalArena.getCampCampRoyalArena().values().stream()
                .map(campData -> campData)
                .sorted(Comparator.comparingInt(CampRoyalArena::getContribution).reversed())
                .collect(Collectors.toList());
        for (int i = 0; i < royalArenas.size(); i++) {
            int rank = i + 1;
            map.put(royalArenas.get(i).getCamp(), rank);
        }
        return map;
    }

    /**
     * ??????
     *
     * @param player
     * @param actType
     * @param now
     * @param campRank
     */
    private void sendUnrewardedMailByRoyalArena(Player player, int actType, int now, Map<Integer, Integer> campRank) {
        // ?????????????????? getActivityInfo??????,??????????????????????????????????????????
        Activity activity = player.activitys.get(actType);
        if (activity == null) {
            return;
        }
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (activityMap == null) {
            return;
        }
        GlobalRoyalArena activityData = (GlobalRoyalArena) activityMap.get(ActivityConst.ACT_ROYAL_ARENA);
        // ????????????????????????
        if (CheckNull.isNull(activityData)) {
            return;
        }
        int rankAwardSchedule;
        ActRank rank = activityData.getPlayerRank(player, ActivityConst.ACT_ROYAL_ARENA, player.roleId);
        if (rank != null) {
            rankAwardSchedule = rank.getRank();
            // ??????????????????????????????100???
            int pCamp = player.lord.getCamp();
            // ????????????
            int pCampR = campRank.get(pCamp);
            List<StaticRoyalArenaAward> awardList = StaticActivityDataMgr.getRoyalArenaAwardByActId(activityData.getActivityId());
            if (!CheckNull.isEmpty(awardList)) {
                StaticRoyalArenaAward sCampAward = awardList.stream().filter(sraa -> sraa.getType() == 3 && pCampR == sraa.getCond()).findFirst().orElse(null);
                if (!CheckNull.isNull(sCampAward)) {
                    mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(sCampAward.getReward()), MailConstant.MOLD_ROYAL_ARENA_CAMP_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond(), pCampR);
                }
            }
            List<Award> awards = new ArrayList<>();
            Map<Integer, Integer> statusMap = activityData.getStatusMap(); // ???????????? ??????keyID,????????????1?????????
            // ??????????????????????????????
            StaticActAward myAward = StaticActivityDataMgr.findRankAward(activityData.getActivityId(), rankAwardSchedule);
            if (myAward == null) {
                return;
            }
            // ???????????????????????????
            sendRankAwardBetween(activity, rankAwardSchedule, awards, statusMap, myAward);
            if (!awards.isEmpty()) {
                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ROYAL_ARENA_PERSON_REWARD,
                        AwardFrom.ACT_UNREWARDED_RETURN, now, rankAwardSchedule);
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param now
     */
    private void sendUnrewardedMailByBanditAward(Player player, int now) {
        Activity activity = player.activitys.get(ActivityConst.ACT_BANDIT_AWARD);
        if (CheckNull.isNull(activity)) {
            return;
        }
        // ??????id
        int activityId = activity.getActivityId();
        List<StaticActBandit> actBandits = StaticActivityDataMgr.getActBanditList();
        // List<StaticActBandit> actBandits = StaticActivityDataMgr.getActBanditList(StaticActBandit.ACT_HIT_DROP_TYPE_1);
        if (CheckNull.isEmpty(actBandits)) {
            return;
        }

        // ????????????
        int lv = player.lord.getLevel();

        // ????????????
        List<StaticActBandit> sActBandits = actBandits.stream().filter(sab -> sab.getActivityId() == activityId).collect(Collectors.toList());
        if (CheckNull.isEmpty(sActBandits)) {
            return;
        }
        // ????????????
        List<Award> awards = new ArrayList<>();
        // ????????????
        sActBandits.forEach(sab -> {
            // ????????????
            List<List<Integer>> costList = null;
            int type = sab.getType();
            List<List<Integer>> drop = sab.getDrop();
            if (CheckNull.isEmpty(drop)) {
                return;
            }
            if (type == StaticActBandit.ACT_HIT_DROP_TYPE_1) {
                costList = drop;
            } else if (type == StaticActBandit.ACT_HIT_DROP_TYPE_2) {
                costList = drop.stream().filter(list -> lv >= list.get(0) && lv <= list.get(1)).map(list -> list.stream().skip(2).collect(Collectors.toList())).collect(Collectors.toList());
            }
            costList.forEach(cost -> {
                long cnt = rewardDataManager.getRoleResByType(player, cost.get(0), cost.get(1));
                if (cnt <= 0) {
                    return;
                }
                // ????????????
                try {
                    rewardDataManager.checkAndSubPlayerAllRes(player, cost.get(0), cost.get(1), AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
                } catch (MwException e) {
                    LogUtil.error(e);
                    return;
                }
                // ???????????????
                List<List<Integer>> convert = sab.getConvert();
                convert.forEach(list -> awards.add(PbHelper.createAwardPb(list.get(0), list.get(1), (int) (list.get(2) * cnt))));
            });
        });
        // ????????????
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_BANDIT_CONVERT_AWARD, AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond());
        }
    }

    /***
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param now
     */
    private void sendThreeRebateReward(Player player, int now) {
        Long roleId = player.roleId;
        Activity activity = player.activitys.get(ActivityConst.ACT_THREE_REBATE);
        if (CheckNull.isNull(activity)) {
            return;
        }
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (CheckNull.isEmpty(actAwardList)) {
            return;
        }
        if (activityDataManager.getIsHave(player) && activityDataManager.getIsPay(player, activity)
                && !activityDataManager.getIsGet(player, activity)) {// ??????????????????????????????
            for (StaticActAward staticActAward : actAwardList) {
                List<List<Integer>> awardList = staticActAward.getAwardList();
                List<Award> awards = new ArrayList<>();
                awards.addAll(PbHelper.createAwardsPb(awardList));
                if (!awards.isEmpty()) {
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(),
                            activity.getActivityType(), activity.getActivityId());
                }
            }
        }

    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param now
     */
    private void sendUnrewardedMailByPromotion(Player player, int now) {
        Long roleId = player.roleId;
        Activity activity = player.activitys.get(ActivityConst.ACT_PROP_PROMOTION);
        if (CheckNull.isNull(activity)) {
            return;
        }
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (CheckNull.isNull(activityMap)) {
            return;
        }
        GlobalActivityData activityData = activityMap.get(ActivityConst.ACT_PROP_PROMOTION);
        if (CheckNull.isNull(activityData)) { // ???????????????????????????
            return;
        }
        StaticActAward sAward = StaticActivityDataMgr.getActAwardById(ActivityConst.ACT_PROP_PROMOTION).get(0);
        if (CheckNull.isNull(sAward)) {
            return;
        }
        List<List<Integer>> awardList = sAward.getAwardList();
        int keyId = sAward.getKeyId();
        // ???????????????
        int schedule = activity.getSaveMap().containsKey(keyId) ? activity.getSaveMap().get(keyId) : 0;
        if (schedule >= sAward.getCond()) { // ??????????????????
            return;
        }
        long val = activityData.getCampValByCamp(player.lord.getCamp()); // ????????????
        int awardCnt = activityDataManager.getAwardCnt(sAward, activity, val); // ???????????????
        if (awardCnt <= 1) {
            return;
        }
        activity.getSaveMap().put(keyId, awardCnt + schedule);

        // ????????????
        for (int i = 0; i < awardList.size(); i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= awardCnt;// ????????????
        }
        if (awardList == null) return;
        List<Award> awards = new ArrayList<>();
        awards.addAll(PbHelper.createAwardsPb(awardList));
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
        }
    }

    /**
     * ???????????????????????????(?????????????????????,?????????????????????)
     *
     * @param player
     * @param now
     */
    private void sendUnrewardedMailByGestapoRank(Player player, int now, Map<Integer, Integer> campRank) {
        Activity gestapoAct = player.activitys.get(ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(gestapoAct)) {
            return;
        }
        Activity rankAct = player.activitys.get(ActivityConst.ACT_GESTAPO_RANK);
        if (CheckNull.isNull(rankAct)) {
            return;
        }
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (activityMap == null) {
            return;
        }
        GlobalActivityData activityData = activityMap.get(ActivityConst.ACT_GESTAPO_RANK);
        if (CheckNull.isNull(activityData)) { // ????????????????????????
            return;
        }
        int rankAwardSchedule = 0;
        long rankValue = 0;
        ActRank rank = activityData.getPlayerRank(player, ActivityConst.ACT_GESTAPO_RANK, player.roleId);
        if (rank != null) {
            rankAwardSchedule = rank.getRank();
            rankValue = rank.getRankValue();
        }
        // Long oldRank = rankAct.getStatusCnt().get(3);
        // if (oldRank != null) {
        // int oldRankInt = oldRank.intValue();
        // rankAwardSchedule = Math.min(oldRankInt, rankAwardSchedule);
        // }
        // ??????????????????
        gestapoKillAward(player, now, rankAct, rankAwardSchedule);
        if (Long.valueOf(rankValue).intValue() < ActParamConstant.ACT_GESTAPO_CAMP_RANK_LEVEL) {
            return;
        }
        // ????????????
        gestapoCampAward(player, now, gestapoAct, campRank);
    }

    /**
     * ?????????????????????????????????
     *
     * @return key:camp val:??????
     */
    private Map<Integer, Integer> calcGestapoCampAward() {
        Map<Integer, Integer> map = new HashMap<>();
        GlobalActivityData campData = activityDataManager.getActivityMap().get(ActivityConst.ACT_ATK_GESTAPO);
        if (CheckNull.isNull(campData)) return map;
        List<GestapoCampRank> rankList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            int[] timeVal = NumUtils.separateLong2int(campData.getCampValByCamp(i));
            if (timeVal[0] <= 0) continue;
            rankList.add(PbHelper.createGestapoCampRankPb(i, timeVal[0], timeVal[1]));
        }
        rankList = rankList.stream().sorted((r1, r2) -> r1.getVal() == r2.getVal()
                        ? Integer.compare(r1.getTime(), r2.getTime()) : -Integer.compare(r1.getVal(), r2.getVal()))
                .collect(Collectors.toList());
        for (int i = 0; i < rankList.size(); i++) {
            int rank = i + 1;
            map.put(rankList.get(i).getCamp(), rank);
        }
        return map;
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param now
     * @param gestapoAct
     * @param campRankMap
     */
    private void gestapoCampAward(Player player, int now, Activity gestapoAct, Map<Integer, Integer> campRankMap) {
        ArrayList<Award> campAwards = new ArrayList<>();
        List<StaticActAward> campAward = StaticActivityDataMgr.getActAwardById(gestapoAct.getActivityId());
        Map<Integer, Integer> campStatus = gestapoAct.getStatusMap();
        Integer campRank = campRankMap.get(player.lord.getCamp());
        if (campRank == null) return;
        for (StaticActAward sAward : campAward) {
            if (campRank > 0 && campRank == sAward.getCond() && !campStatus.containsKey(sAward.getKeyId())) {
                campAwards.addAll(PbHelper.createAwardsPb(sAward.getAwardList()));
                gestapoAct.getStatusMap().put(sAward.getKeyId(), 1);
            }
        }
        if (!campAwards.isEmpty()) {
            mailDataManager.sendAttachMail(player, campAwards, MailConstant.MOLD_GESTAPO_KILL_CAMP_REWARD,
                    AwardFrom.GESTAPO_RANK_CAMP_AWARD, now, player.lord.getCamp(), campRank);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param now
     * @param rankAct
     * @param rankAwardSchedule
     */
    private void gestapoKillAward(Player player, int now, Activity rankAct, int rankAwardSchedule) {
        if (rankAwardSchedule <= 0) {
            return;
        }
        List<Award> awards = new ArrayList<>();
        List<StaticActAward> sActAward = StaticActivityDataMgr.getRankActAwardByActId(rankAct.getActivityId());
        Map<Integer, Integer> rankStatus = rankAct.getStatusMap(); // ???????????? ??????keyID,????????????1?????????
        for (StaticActAward sAward : sActAward) {
            sendRankAwardBetween(rankAct, rankAwardSchedule, awards, rankStatus, sAward);
        }

        LogUtil.debug("SendGestapoRankMail roleId:", player.roleId, ", rank:", rankAwardSchedule, ", awards:", awards);
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_GESTAPO_KILL_REWARD,
                    AwardFrom.GESTAPO_RANK_AWARD, now, rankAwardSchedule);
        }
    }

    /**
     * ????????????????????????(??????????????????,???????????????)
     *
     * @param player
     * @param now
     */
    private void sendUnrewardedMailByAtkCity(Player player, int now) {
        Activity activity = player.activitys.get(ActivityConst.ACT_ATTACK_CITY_NEW);
        if (CheckNull.isNull(activity)) {
            return;
        }
        if (player.lord.getLevel() < ActParamConstant.ACT_ATK_CITY_LEVEL.get(0)) {
            return;
        }
        // ActivityBase actBase = StaticActivityDataMgr.getActivityByTypeIgnoreStep(ActivityConst.ACT_ATTACK_CITY_NEW);
        // if (actBase == null) {
        // return;
        // }
        AtkCityAct cityAct = player.atkCityAct;
        // int dayiy = DateHelper.dayiy(actBase.getBeginTime(), new Date()); // ????????????????????????
        for (StaticAtkCityAct atkCityAct : StaticActivityDataMgr.getAtkCityActList()) { // ???????????????
            int canRecvCnt = activityDataManager.getCanRecvCnt(player, atkCityAct, cityAct);
            if (canRecvCnt > 0) {
                activityDataManager.recvActiveCnt(player, cityAct, atkCityAct, canRecvCnt, activity);
            }
        }
        List<Award> awards = new ArrayList<>();
        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        for (StaticActAward actAward : condList) {
            if (activityDataManager.caluActRecvedState(player, activity, actAward) == 0) {
                awards.addAll(PbHelper.createAwardsPb(actAward.getAwardList()));
                activity.getStatusMap().put(actAward.getKeyId(), 1);
            }
        }
        if (!CheckNull.isEmpty(awards)) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param actType
     * @param now
     */
    private void sendUnrewardedMailByGestapo(Player player, int actType, int now) {
        Activity activity = player.activitys.get(actType);
        if (CheckNull.isNull(activity)) {
            return;
        }
        // ????????????
        int exchangeCnt = activityDataManager.currentActivity(player, activity, 0);
        // ????????????
        int summonCnt = worldDataManager.getCostCnt(player);
        int sumCnt = exchangeCnt + summonCnt;
        LogUtil.debug("roleId=" + player.roleId + " ,????????????=" + exchangeCnt + " ,????????????=" + summonCnt);
        if (sumCnt <= 0) {
            return;
        }
        try {
            List<StaticActExchange> staticActExchanges = StaticActivityDataMgr
                    .getActExchangeListById(activity.getActivityId());
            if (CheckNull.isEmpty(staticActExchanges)) return;
            List<List<Integer>> expendProps = staticActExchanges.get(0).getExpendProp();
            if (CheckNull.isEmpty(expendProps)) return;
            List<Integer> expendProp = expendProps.get(0);
            if (CheckNull.isNull(expendProp)) return;
            // ????????????
            rewardDataManager.checkAndSubPlayerResHasSync(player, expendProp.get(0), expendProp.get(1), exchangeCnt,
                    AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
            List<StaticGestapoPlan> gestapoPlans = StaticWorldDataMgr.getGestapoList();
            for (StaticGestapoPlan gestapoPlan : gestapoPlans) {
                List<List<Integer>> costProp = gestapoPlan.getCostProp();
                if (CheckNull.isEmpty(costProp)) continue;
                List<Integer> prop = costProp.get(0);
                if (CheckNull.isEmpty(prop)) continue;
                // ????????????
                rewardDataManager.checkAndSubPlayerAllRes(player, prop.get(0), prop.get(1),
                        AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
            }
        } catch (MwException e) {
            LogUtil.error(e, "????????????????????????,???????????????????????????");
            return;
        }
        ArrayList<List<Integer>> exchangeRes = getExchangeList(sumCnt);
        if (exchangeRes == null) return;
        List<Award> awards = new ArrayList<>();
        awards.addAll(PbHelper.createAwardsPb(exchangeRes));
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_PROP_RECYCLE,
                    AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond(), summonCnt, exchangeCnt, summonCnt,
                    exchangeCnt);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param sumCnt
     * @return
     */
    private ArrayList<List<Integer>> getExchangeList(int sumCnt) {
        List<List<Integer>> exchangRes = ActParamConstant.ACT_GESTAPO_EXCHANG_RES;
        if (CheckNull.isEmpty(exchangRes)) return null;
        ArrayList<List<Integer>> destExRes = new ArrayList<>();
        List<Integer> exchangeList = new ArrayList<>();
        if (destExRes.size() == 0) {
            destExRes.add(exchangeList);
        }
        List<Integer> exchaRes = exchangRes.get(0);
        for (Integer exchaRe : exchaRes) {
            exchangeList.add(exchaRe);
        }
        // ????????????
        BigDecimal baseCnt = new BigDecimal(exchangeList.get(2));
        // ????????????????????????
        int resCnt = baseCnt.multiply(new BigDecimal(sumCnt)).intValue();
        exchangeList.set(2, resCnt);
        return destExRes;
    }

    /**
     * ??????????????????
     *
     * @param actType
     */
    private void sendUnrewardedMailByActAllCharge(int actType, int now) {
        GlobalActivityData activityData = activityDataManager.getActivityMap().get(actType);
        if (activityData == null) {
            return;
        }
        // long camp1 = activityData.getTopupa().get();
        // long camp2 = activityData.getTopupb().get();
        // long camp3 = activityData.getTopupc().get();
        // long[] camps = {0, camp1, camp2, camp3};
        // int campIndex = 1;
        // for (int i = 1; i <= 3; i++) {
        //     if (camps[campIndex] <= camps[i]) {
        //         campIndex = i;
        //     }
        // }
        HashMap<Integer, Long> campVal = new HashMap<>();
        campVal.put(Constant.Camp.EMPIRE, activityData.getTopupa().get());
        campVal.put(Constant.Camp.ALLIED, activityData.getTopupb().get());
        campVal.put(Constant.Camp.UNION, activityData.getTopupc().get());
        // ????????????
        int campIndex = campVal.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .get();
        for (Player player : playerDataManager.getPlayers().values()) {
            if (player.lord.getLevel() < ActParamConstant.ACT_ALL_CHARGE_LORD_LV) {
                continue;
            }
            sendUnrewardedMailByNormal(player, actType, now);
            if (player.lord.getCamp() == campIndex) {
                List<Award> awards;
                if (actType == ActivityConst.ACT_ALL_CHARGE) {
                    awards = new ArrayList<>(PbHelper.createAwardsPb(ActParamConstant.ACT_ALL_CHARGE_REWARD));
                } else {
                    List<List<Integer>> actBravestArmyAward = ActParamConstant.ACT_BRAVEST_ARMY_AWARD;
                    // ??????id
                    int activityId = activityData.getActivityId();
                    // ?????????id?????????
                    awards = new ArrayList<>(PbHelper.createAwardsPb(actBravestArmyAward.stream()
                            .filter(conf -> conf.get(0) == activityId)
                            .map(conf -> conf.subList(1, conf.size()))
                            .collect(Collectors.toList())));
                }
                if (!awards.isEmpty()) {
                    mailDataManager.sendAttachMail(player, awards, actType == ActivityConst.ACT_ALL_CHARGE ? MailConstant.MOLD_ACT_ALL_CHARGE_REWARD : MailConstant.MOLD_ACT_BRAVEST_ARMY_AWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond(), campIndex, campIndex);
                }
            }
        }
    }

    /**
     * ???????????????(????????????)
     *
     * @param player
     */
    public void sendUnrewardedMailByNormal(Player player, int actType, int now) {
        if (actType == ActivityConst.ACT_CHARGE_CONTINUE
                || actType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {
            return;
        }
        if (ActivityConst.ACT_VIP == actType && player.lord.getLevel() < ActParamConstant.ACT_VIP_LORD_LV) {
            return;
        }
        Activity activity = player.activitys.get(actType);
        if (activity == null) {
            return;
        }
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (CheckNull.isEmpty(actAwardList)) {
            return;
        }
        // activityDataManager.currentActivity(player, activity, 0);
        List<Award> awards = new ArrayList<>();
        for (StaticActAward saa : actAwardList) {
            if (activity.getStatusMap().containsKey(saa.getKeyId())) {
                continue;
            }
            int sortId = saa.getSortId();
            if (actType == ActivityConst.ACT_VIP || actType == ActivityConst.ACT_CHALLENGE_COMBAT
                    || actType == ActivityConst.ACT_TRAINED_SOLDIERS || actType == ActivityConst.ACT_EQUIP_MATERIAL
                    || actType == ActivityConst.ACT_ELIMINATE_BANDIT) {
                sortId = saa.getParam().get(0);
            } else if (ActivityConst.ACT_COLLECT_RESOURCES == actType || ActivityConst.ACT_RESOUCE_SUB == actType) {
                sortId = saa.getParam().get(0) * 10000 + saa.getParam().get(1);
            }
            int schedule = actType == ActivityConst.ACT_WAR_PLANE_SEARCH ? activityDataManager.getWarPlaneSearchSchedule(saa, player, activity) : actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY ? activityDataManager.exceedActivity(player, activity, sortId) : activityDataManager.currentActivity(player, activity, sortId);
            // int schedule = actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY ? activityDataManager.exceedActivity(player, activity, sortId) : activityDataManager.currentActivity(player, activity, sortId);
            if (schedule >= saa.getCond()) {
                boolean flat = true;
                if (actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY) {// ???????????? ???????????? ????????????
                    // ???????????????????????????
                    int topup = activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                            : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD);
                    // ???????????? ???????????????????????????
                    int checkLordGlod = 0;
                    if (saa.getParam() != null && saa.getParam().size() > 0) {
                        checkLordGlod = saa.getParam().get(0);
                    }
                    if (topup < checkLordGlod) {// ?????????
                        flat = false;
                    }
                }
                if (flat) {
                    awards.addAll(PbHelper.createAwardsPb(saa.getAwardList()));
                    activity.getStatusMap().put(saa.getKeyId(), 1);
                }
            }
        }
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_UNREWARDED_RETURN, now, actType, activity.getActivityId(), actType, activity.getActivityId());
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param actType
     * @param now
     */
    private void sendCampFightRankMail(int actType, int now) {
        if (actType == ActivityConst.ACT_CAMP_FIGHT_RANK) {
            Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
            if (activityMap == null) {
                return;
            }
            GlobalActivityData gAct = activityMap.get(actType);
            if (gAct == null) { // ????????????????????????
                return;
            }
            if (CheckNull.isEmpty(ActParamConstant.ACT_CAMP_FIGHT_WIN_CAMP_AWARD)) {
                return;
            }
            final int activityId = gAct.getActivityId();
            List<Integer> actIdAndAward = ActParamConstant.ACT_CAMP_FIGHT_WIN_CAMP_AWARD.stream()
                    .filter(l -> l != null && l.size() >= 4 && l.get(0) == activityId).findFirst().orElse(null);
            if (actIdAndAward == null) {// ?????????????????????
                actIdAndAward = ActParamConstant.ACT_CAMP_FIGHT_WIN_CAMP_AWARD.get(0);
            }
            if (actIdAndAward.size() < 4) {
                return;
            }
            List<CommonPb.Award> awards = new ArrayList<>();
            awards.add(PbHelper.createAwardPb(actIdAndAward.get(1), actIdAndAward.get(2), actIdAndAward.get(3)));
            int maxCampVal = calcMaxCampValByGlobalActivityData(gAct); // ???????????????
            for (Player player : playerDataManager.getPlayers().values()) {
                if (player.lord.getCamp() == maxCampVal) {
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_WIND_CAMP_FIGHT_RANK_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, now, maxCampVal, maxCampVal);
                }
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param gAct
     * @return ????????????
     */
    private int calcMaxCampValByGlobalActivityData(GlobalActivityData gAct) {
        long camp1 = gAct.getTopupa().get();
        long camp2 = gAct.getTopupb().get();
        long camp3 = gAct.getTopupc().get();
        long[] camps = {0, camp1, camp2, camp3};
        int campIndex = 1;
        for (int i = 1; i <= 3; i++) {
            if (camps[campIndex] <= camps[i]) {
                campIndex = i;
            }
        }
        return campIndex;
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param actType
     */
    public void sendUnrewardedMailByRank(Player player, int actType, int now) {
        try {
            if (player == null) {
                return;
            }
            // ?????????????????? getActivityInfo??????,??????????????????????????????????????????
            Activity activity = player.activitys.get(actType);
            Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
            if (activityMap == null) {
                return;
            }
            GlobalActivityData globalActivityData = activityMap.get(actType);
            // ?????????????????????
            sendUnrewardedMailByRank(player, now, activity, globalActivityData);
        } catch (Exception e) {
            LogUtil.error("????????????????????????", e);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param now
     * @param activity
     * @param globalActivityData
     */
    public void sendUnrewardedMailByRank(Player player, int now, Activity activity, GlobalActivityData globalActivityData) {
        if (globalActivityData == null) { // ????????????????????????
            return;
        }
        if (activity == null) {
            return;
        }
        if (activity.getStatusCnt().isEmpty()) {// ?????????
            return;
        }
        int actType = activity.getActivityType();
        int rankAwardSchedule = 0;
        ActRank rank = globalActivityData.getPlayerRank(player, actType, player.roleId);
        if (rank != null) {
            rankAwardSchedule = rank.getRank();
        }
        Long oldRank = activity.getStatusCnt().get(3);
        if (oldRank != null) {
            int oldRankInt = oldRank.intValue();
            if (actType != ActivityConst.ACT_LUCKY_TURNPLATE && actType != ActivityConst.ACT_EQUIP_TURNPLATE
                    && actType != ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
                rankAwardSchedule = Math.min(oldRankInt, rankAwardSchedule);
            }
        }
        List<Award> awards = new ArrayList<>();
        List<StaticActAward> sActAward = StaticActivityDataMgr.getRankActAwardByActId(activity.getActivityId());
        Map<Integer, Integer> statusMap = activity.getStatusMap(); // ???????????? ??????keyID,????????????1?????????
        if (StaticActivityDataMgr.isOnlyRankAward(actType)) {
            if (rank == null) {
                return;
            }
            // ??????????????????????????????
            StaticActAward myAward = StaticActivityDataMgr.findRankAward(activity.getActivityId(), rank.getRank());
            if (myAward == null) {
                return;
            }
            // ???????????????????????????
            sendRankAwardRemain(activity, rankAwardSchedule, awards, statusMap, myAward);
            if (!awards.isEmpty()) {
                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_CAMPRANK_REWARD,
                        AwardFrom.RANK_CAMP_AWARD, now, actType, rankAwardSchedule);
            }
            return;
        } else {
            for (StaticActAward sAward : sActAward) {
                if (rankAwardSchedule > 0) {
                    if (actType == ActivityConst.ACT_LUCKY_TURNPLATE
                            || actType == ActivityConst.ACT_EQUIP_TURNPLATE
                            || actType == ActivityConst.FAMOUS_GENERAL_TURNPLATE
                            || actType == ActivityConst.ACT_PAY_RANK_NEW
                            || actType == ActivityConst.ACT_PAY_RANK_V_3
                            || actType == ActivityConst.ACT_MERGE_PAY_RANK
                            || ActivityConst.ACT_CONSUME_GOLD_RANK == actType
                            || actType == ActivityConst.ACT_TUTOR_RANK) {
                        sendRankAwardBetween(activity, rankAwardSchedule, awards, statusMap, sAward);
                    } else {
                        sendRankAwardRemain(activity, rankAwardSchedule, awards, statusMap, sAward);
                    }
                }
            }
        }
        if (!awards.isEmpty()) {
            if (actType == ActivityConst.ACT_MERGE_PAY_RANK) {
                mailDataManager.sendAttachMail(player, awards, MailConstant.WORLD_WAR_PERSONAL_RANK_REWARD,
                        AwardFrom.ACT_UNREWARDED_RETURN, now, actType, actType, rankAwardSchedule);
            } else {
                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                        AwardFrom.ACT_UNREWARDED_RETURN, now, actType, activity.getActivityId(), actType, activity.getActivityId());
            }
        }
    }

    /**
     * ????????????,??????????????????(rank > cond)
     *
     * @param activity
     * @param rankAwardSchedule
     * @param awards
     * @param statusMap
     * @param sAward
     */
    private void sendRankAwardRemain(Activity activity, int rankAwardSchedule, List<Award> awards,
                                     Map<Integer, Integer> statusMap, StaticActAward sAward) {
        if (rankAwardSchedule <= sAward.getCond()) {
            if (!statusMap.containsKey(sAward.getKeyId())) {// ????????????
                awards.addAll(PbHelper.createAwardsPb(sAward.getAwardList()));
                activity.getStatusMap().put(sAward.getKeyId(), 1);
            }
        }
    }

    /**
     * ????????????,??????????????????(??????[param[1] - cond])
     *
     * @param activity
     * @param rankAwardSchedule
     * @param awards
     * @param statusMap
     * @param sAward
     */
    private void sendRankAwardBetween(Activity activity, int rankAwardSchedule, List<Award> awards,
                                      Map<Integer, Integer> statusMap, StaticActAward sAward) {
        int start = sAward.getParam().get(1);
        int end = sAward.getCond();
        if (rankAwardSchedule >= start && rankAwardSchedule <= end) {
            if (!statusMap.containsKey(sAward.getKeyId())) {// ????????????
                awards.addAll(PbHelper.createAwardsPb(sAward.getAwardList()));
                activity.getStatusMap().put(sAward.getKeyId(), 1);
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param actType
     * @param now
     */
    private void sendUnrewardedMailByDailyPay(Player player, int actType, int now) {
        if (player == null) {
            return;
        }
        // ?????????????????? getActivityInfo??????,??????????????????????????????????????????
        Activity activity = player.activitys.get(actType);
        if (activity == null) {
            return;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (CheckNull.isNull(activityBase)) {
            return;
        }
        // if (activity.getStatusCnt().isEmpty()) {// ?????????
        // return;
        // }
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (CheckNull.isEmpty(actAwardList)) {
            return;
        }
        List<Award> awards = new ArrayList<>();
        int currentDay = TimeHelper.getCurrentDay();
        for (StaticActAward award : actAwardList) {
            if (activity.getStatusMap().containsKey(award.getKeyId())) {
                continue;
            }
            // ????????????
            if (award.getParam().get(0) == 0) {
                int lastDay = TimeHelper.getDay(player.lord.getOnTime());
                if (currentDay == lastDay) {
                    activity.getStatusMap().put(award.getKeyId(), 1);
                    awards.addAll(PbHelper.createAwardsPb(award.getAwardList()));
                }
                // ????????????
            } else {
                if (CheckNull.isNull(activity.getStatusCnt().get(0))) {
                    continue;
                }
                int status = activityDataManager.caluActCailyPayStatus(activityBase.getActivityId(), award, activity);
                if (activity.getStatusCnt().get(0) >= award.getCond() && status == 0) {
                    if (activity.getStatusMap().containsKey(award.getKeyId())
                            && activity.getStatusMap().get(award.getKeyId()) == currentDay) {
                        continue;
                    }
                    activity.getStatusMap().put(award.getKeyId(), currentDay);
                    awards.addAll(PbHelper.createAwardsPb(award.getAwardList()));
                }
            }
        }
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_UNREWARDED_RETURN, now, actType, activity.getActivityId(), actType, activity.getActivityId());
        }
    }

    /**
     * ??????????????????
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetOnLineAwardRs getOnLineAward(GetOnLineAwardRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int keyId = req.getKeyId();
        int activityType = req.getType();
        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
        }

        if (activityType != ActivityConst.ACT_ONLINE_GIFT && activityType != ActivityConst.ACT_GIFT_OL) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), "?????????????????????,????????????, roleId:,", roleId);
        }

        // Activity activity = player.activitys.get(activityType);
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        if (actAward.getActivityId() != activity.getActivityId()) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), "?????????????????????,????????????, roleId:,", roleId);
        }

        Integer awardStatus = activity.getStatusMap().get(keyId);
        if (awardStatus != null && awardStatus == 1) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????????, roleId:,", roleId);
        }
        int now = TimeHelper.getCurrentSecond();

        List<List<Integer>> awardList = actAward.getAwardList();
        // ????????????????????????
        rewardDataManager.checkBag(player, awardList);

        int schedule = activityDataManager.currentActivity(player, activity, 0, now);

        if (schedule == 0) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
        }
        LogUtil.debug("GetOnLineAwardRs activity=" + activity);

        if (actAward.getCond() <= 0 || schedule < actAward.getCond()) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "???????????????, roleId:,", roleId);
        }

        activity.getStatusMap().put(keyId, 1);

        // ????????????
        if (activityType == ActivityConst.ACT_ONLINE_GIFT) {
            activityDataManager.updActivity(player, activityType, now, 0, true);
        }

        // ???????????????????????????
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_ONLINE_AWARD_CNT, 1);

        int state = 0;

        GetOnLineAwardRs.Builder builder = GetOnLineAwardRs.newBuilder();

        int size = awardList.size();
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        for (int i = 0; i < size; i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// ????????????
            if (type == AwardType.EQUIP) {
                for (int c = 0; c < count; c++) {
                    int itemkey = rewardDataManager.addAward(player, type, itemId, 1, AwardFrom.ACTIVITY_AWARD, keyId,
                            activity.getActivityId(), activity.getActivityType());
                    builder.addAward(PbHelper.createAwardPb(type, itemId, 1, itemkey));
                }
            } else {
                int itemkey = rewardDataManager.addAward(player, type, itemId, count, AwardFrom.ACTIVITY_AWARD, keyId,
                        activity.getActivityId(), activity.getActivityType());
                builder.addAward(PbHelper.createAwardPb(type, itemId, count, itemkey));
            }
        }

        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());

        condList = filterAwardByOLAct(player, activity, condList);

        schedule = activityDataManager.currentActivity(player, activity, 0, now);

        if (condList != null) {
            for (StaticActAward e : condList) {
                int id = e.getKeyId();
                int Status = activity.getStatusMap().containsKey(id) ? 1 : 0;// 0 ??????????????? ,1 ???????????????
                if (ActivityConst.ACT_GIFT_OL == activity.getActivityType()
                        || ActivityConst.ACT_ONLINE_GIFT == activity.getActivityType() && Status == 0) {
                    schedule = e.getCond() > schedule ? e.getCond() - schedule + now : now;
                    builder.addActivityCond(PbHelper.createActivityCondPb(e, Status, schedule));
                    checkNextPhaseAward(builder, e, condList);
                    break;
                } else {
                    builder.addActivityCond(PbHelper.createActivityCondPb(e, Status, schedule));
                }
            }
        }
        for (CommonPb.ActivityCond cond : builder.getActivityCondList()) {
            if (cond.getStatus() == 0) {
                state = 1;
                break;
            }
        }
        // 0 : ????????????????????????, 1 : ??????????????????
        builder.setState(state);
        return builder.build();
    }

    // ==================================???????????????start==================================

    /**
     * ?????????????????????
     *
     * @param triggerId
     * @param player
     */
    public void checkTriggerGiftSync(int triggerId, Player player) {
        try {
            int now = TimeHelper.getCurrentSecond();
            List<StaticTriggerConf> triggerConf = StaticActivityDataMgr.getTriggerGiftConfById(triggerId, player);
            if (CheckNull.isEmpty(triggerConf)) {
                return;
            }
            Map<Integer, TriggerGift> triggerGiftInfo = activityDataManager.getTriggerGiftInfo(player, triggerId);
            List<TriggerGift> triggerGifts = new ArrayList<>(triggerGiftInfo.values());
            SyncTriggerGiftRs.Builder builder = SyncTriggerGiftRs.newBuilder();
            //??????????????????- ???????????????????????????????????????????????????????????????????????????
            if (triggerId == ActivityConst.TRIGGER_GIFT_TREASURE_OPEN) {
                List<TriggerGift> giftList = new ArrayList();
                for (TriggerGift tgift : triggerGifts) {
                    StaticTriggerConf conf = triggerConf.stream().filter(t -> t.getGiftId() == tgift.getGiftId())
                            .findFirst().orElse(null);
                    if (CheckNull.isNull(conf)) {
                        continue;
                    }
                    StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
                    if (CheckNull.isNull(triggerGiftConf)) {
                        throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " ??????????????????,????????????????????? roleId:",
                                player.roleId, ", giftId:", conf.getGiftId());
                    }
                    if (tgift.getState() != ActivityConst.NOT_TRIGGER_STATUS) { //??????????????????
                        continue;
                    }
                    giftList.add(tgift);
                }
                if (giftList.size() >= 1) {//??????????????????
                    triggerGifts = RandomUtil.getListRandom(giftList, 1);
                }
            }

            for (TriggerGift triggerGift : triggerGifts) {
                StaticTriggerConf conf = triggerConf.stream().filter(t -> t.getGiftId() == triggerGift.getGiftId())
                        .findFirst().orElse(null);
                if (CheckNull.isNull(conf)) {
                    continue;
                }
                StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
                if (CheckNull.isNull(triggerGiftConf)) {
                    throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " ??????????????????,????????????????????? roleId:",
                            player.roleId, ", giftId:", conf.getGiftId());
                }
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL) { // ??????????????????
                    //??????????????????7?????????
                    Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
                    long time = beginTime.getTime() + 7 * TimeHelper.DAY_S * 1000;
                    if (System.currentTimeMillis() > time) {
                        continue;
                    }
                    if (triggerGift.getStatus() != conf.getCond().get(0)) { // ??????????????????
                        continue;
                    }
                    //???????????????3?????????????????????????????????
                    long threeTime = beginTime.getTime() + 3 * TimeHelper.DAY_S * 1000;
                    if (System.currentTimeMillis() < threeTime) {
                        Activity activity = player.activitys.get(ActivityConst.ACT_FIRSH_CHARGE);
                        if (activity.getStatusMap().size() == 0) {
                            checkFirstChargeSync(player);
                            return;
                        }
                    }
                }
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_EXPEDITION_FAIL) { // ??????????????????????????????????????????
                    if (triggerGift.getStatus() != conf.getCond().get(0)) { // ??????????????????
                        continue;
                    }
                    //???????????????3?????????????????????????????????
                    Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
                    long time = beginTime.getTime() + 3 * TimeHelper.DAY_S * 1000;
                    if (System.currentTimeMillis() < time) {
                        Activity activity = player.activitys.get(ActivityConst.ACT_FIRSH_CHARGE);
                        if (activity.getStatusMap().size() == 0) {
                            checkFirstChargeSync(player);
                            return;
                        }
                    }
                }
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_REBUILD) { // ????????????
                    if (triggerGift.getStatus() != conf.getCond().get(0)) { // ??????????????????????????????
                        continue;
                    }
                }
                // ?????????
                if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
                    triggerGift.setState(ActivityConst.TRIGGER_STATUS);
                    triggerGift.setBeginTime(now);
                    triggerGift.setEndTime((int) (now + conf.getContinueTime()));
                    CommonPb.TriggerGiftInfo giftInfo = PbHelper.creteTriggerGiftsRs(conf, triggerGift, triggerGiftConf,
                            player);
                    builder.addTriggerGiftInfo(giftInfo);
                }
            }
            if (CheckNull.isEmpty(builder.getTriggerGiftInfoBuilderList())) {
                return;
            }
            Base.Builder msg = PbHelper.createRsBase(SyncTriggerGiftRs.EXT_FIELD_NUMBER, SyncTriggerGiftRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            LogUtil.debug("?????????????????????" + player.roleId);
        } catch (MwException mwException) {
            LogUtil.error(mwException);
        } catch (Exception e) {
            LogUtil.error("???????????????Error:", e);
        }

    }

    /**
     * ???????????????--??????????????????
     *
     * @param player
     */
    public void checkFirstChargeSync(Player player) {
        try {
            SyncFirstChargeRs.Builder builder = SyncFirstChargeRs.newBuilder();
            builder.setFirstCharge(1);
            Base.Builder msg = PbHelper.createRsBase(SyncFirstChargeRs.EXT_FIELD_NUMBER, SyncFirstChargeRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            LogUtil.debug("??????????????????" + player.roleId);

        } catch (Exception e) {
            LogUtil.error("??????????????????Error:", e);
        }
    }

    /**
     * ?????????????????????????????????Id
     *
     * @param giftId
     * @param player
     */
    public void checkTriggerGiftSyncByGiftId(int giftId, Player player) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        // ????????????id????????????
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        // ??????TriggerGift??????
        TriggerGift triggerGift = activityDataManager.getTriggerGiftInfoByGiftId(player, giftId, true);
        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        SyncTriggerGiftRs.Builder builder = SyncTriggerGiftRs.newBuilder();
        if (CheckNull.isNull(triggerGiftConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " ??????????????????,????????????????????? roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // ?????????
        if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
            triggerGift.setState(ActivityConst.TRIGGER_STATUS);
            triggerGift.setBeginTime(now);
            triggerGift.setEndTime((int) (now + conf.getContinueTime()));
            CommonPb.TriggerGiftInfo giftInfo = PbHelper.creteTriggerGiftsRs(conf, triggerGift, triggerGiftConf,
                    player);
            builder.addTriggerGiftInfo(giftInfo);
        }
        if (CheckNull.isEmpty(builder.getTriggerGiftInfoBuilderList())) {
            return;
        }
        Base.Builder msg = PbHelper.createRsBase(SyncTriggerGiftRs.EXT_FIELD_NUMBER, SyncTriggerGiftRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        LogUtil.debug("?????????????????????" + player.roleId);
    }

    /**
     * ???????????????????????????????????????Id
     *
     * @param player
     */
    public void checkTimeTriggerGiftSyncByGiftId(int giftId, int triggerPlanId, Player player) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        TriggerGift triggerGift = activityDataManager.getTimeTriggerGiftInfo(player, giftId, triggerPlanId, true);
        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        SyncTriggerGiftRs.Builder builder = SyncTriggerGiftRs.newBuilder();
        if (CheckNull.isNull(triggerGiftConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " ??????????????????,????????????????????? roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // ?????????
        if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
            triggerGift.setState(ActivityConst.TRIGGER_STATUS);
            triggerGift.setBeginTime(now);
            triggerGift.setEndTime((int) (now + conf.getContinueTime()));
            CommonPb.TriggerGiftInfo giftInfo = PbHelper.creteTriggerGiftsRs(conf, triggerGift, triggerGiftConf, player);
            builder.addTriggerGiftInfo(giftInfo);
        }
        if (CheckNull.isEmpty(builder.getTriggerGiftInfoBuilderList())) {
            return;
        }
        Base.Builder msg = PbHelper.createRsBase(SyncTriggerGiftRs.EXT_FIELD_NUMBER, SyncTriggerGiftRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        LogUtil.debug("?????????????????????" + player.roleId);
    }

    /**
     * ?????????????????????
     */
    public void putPlayerOfflineTriggerGift(int giftId, int triggerPlanId, Player player) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        TriggerGift triggerGift = activityDataManager.getTimeTriggerGiftInfo(player, giftId, triggerPlanId, true);
        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        if (CheckNull.isNull(triggerGiftConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " ??????????????????,????????????????????? roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // ?????????
        if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
            triggerGift.setBeginTime(now);
            triggerGift.setEndTime(now + conf.getContinueTime());
            triggerGift.setState(ActivityConst.TRIGGER_STATUS);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetTriggerGiftRs GetTriggerGift(GetTriggerGiftRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // ????????????????????????
        int now = TimeHelper.getCurrentSecond();
        GetTriggerGiftRs.Builder builder = GetTriggerGiftRs.newBuilder();
        List<CommonPb.TriggerGiftInfo> triggerGiftInfos = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, TriggerGift>> triggerGifts : player.triggerGifts.entrySet()) {
            // ?????????????????????, ?????????????????????, ?????????????????????????????????, VIP?????????
            List<StaticTriggerConf> triggerConf = StaticActivityDataMgr.getTriggerGiftConfById(triggerGifts.getKey(), player, false);
            // ???????????????????????????
            Map<Integer, TriggerGift> triggerGiftInfo = activityDataManager.getTriggerGiftInfo(player, triggerGifts.getKey());
            for (TriggerGift triggerGift : triggerGiftInfo.values()) {
                StaticTriggerConf conf = triggerConf.stream().filter(t -> t.getGiftId() == triggerGift.getGiftId())
                        .findFirst().orElse(null);
                if (CheckNull.isNull(conf)) {
                    continue;
                }
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_TIME_COND &&
                        StaticActivityDataMgr.getGiftPackTriggerPlan(conf.getId()) == null) {
                    continue;
                }
                StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
                if (CheckNull.isNull(triggerGiftConf)) {
                    continue;
                }
                if (!activityDataManager.checkGiftState(triggerGift, conf, triggerGiftConf, now)) {
                    continue;
                }
                triggerGiftInfos.add(PbHelper.creteTriggerGiftsRs(conf, triggerGift, triggerGiftConf, player));
            }

        }
        // ?????????????????????
        triggerGiftInfos = triggerGiftInfos.stream().sorted(Comparator.comparingInt(TriggerGiftInfo::getLastTime)).collect(Collectors.toList());
        builder.addAllTriggerGiftInfo(triggerGiftInfos);
        return builder.build();
    }

    // ==================================???????????????end==================================

    /**
     * ?????????????????????????????????
     */
    public void syncActListChg() {
        LogUtil.debug("----------??????????????????????????????????????????------------");
        playerDataManager.getPlayers().values().stream().filter(p -> p.isLogin && p.ctx != null).forEach(player -> {
            SyncActListChgRs.Builder builder = SyncActListChgRs.newBuilder();
            Base.Builder msg = PbHelper.createRsBase(SyncActListChgRs.EXT_FIELD_NUMBER, SyncActListChgRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        });
        LogUtil.debug("----------??????????????????????????????????????????------------");
    }

    /**
     * ?????????????????????????????????
     *
     * @param actType
     */
    public void onActBegin(int actType) {
        if (ActivityConst.ACT_CAMP_FIGHT_RANK == actType) {
            activityDataManager.initAndLoadActCampFightRank();
            LogUtil.debug("-------?????????????????????????????? actType:", actType);
        }
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetDayDiscountsRs getDayDiscounts(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_DAY_DISCOUNTS);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        // ??????????????????????????????????????????
        final int lvKey = ActivityConst.ACT_DAYDICOUNTS_LV_KEY;
        final int rankey = ActivityConst.ACT_DAYDICOUNTS_RANK_KEY;

        Long saveLv = activity.getStatusCnt().get(lvKey);
        final int actLv = saveLv == null ? player.lord.getLevel() : saveLv.intValue();// ??????
        activity.getStatusCnt().put(lvKey, (long) actLv);

        Long saveRank = activity.getStatusCnt().get(rankey);
        final int actRank = saveRank == null ? player.lord.getRanks() : saveRank.intValue();// ??????
        activity.getStatusCnt().put(rankey, (long) actRank);

        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (CheckNull.isEmpty(condList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:,", roleId);
        }
        GetDayDiscountsRs.Builder builder = GetDayDiscountsRs.newBuilder();
        StaticActAward actAward = condList.stream()
                .filter(s -> s.getParam().get(0) <= actLv && actLv <= s.getParam().get(1)).findFirst().orElse(null);
        if (actAward != null) {
            int freeKey = 0;// ??????????????????????????????0??????
            int status = activity.getStatusMap().containsKey(freeKey) ? 1 : 0;// ?????????????????????
            int state = activityDataManager.currentActivity(player, activity, 0);
            builder.setFreeActCond(PbHelper.createActivityCondPb(actAward, status, state));
        }
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        StaticActivityDataMgr.getActDaydiscountsMapByActId(activity.getActivityId()).stream()
                .filter(s -> s.getLevel().get(0) <= actLv && actLv <= s.getLevel().get(1)
                        && s.getPartyRanks().get(0) <= actRank && actRank <= s.getPartyRanks().get(1))
                .forEach(sad -> {
                    StaticActGiftpack sGift = StaticActivityDataMgr.getActGiftpackMapById(sad.getActGiftId());
                    Integer curCnt = statusMap.get(sad.getGrade());
                    int count = curCnt == null ? 0 : curCnt;
                    StaticPay payinfo = StaticVipDataMgr.getStaticPayByPayId(sad.getActGiftId());
                    builder.addDayDiscounts(PbHelper.createDayDiscountsPb(sad, sGift, payinfo, null, count));
                });
        return builder.build();
    }

    /**
     * ???????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetMonopolyRs getMonopoly(long roleId, GetMonopolyRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_MONOPOLY);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        int getType = req.getGetType();// 0. ??????????????????, 1. ????????????????????????
        long todayPayGold = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY,
                0L); // ????????????????????????
        long hasCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, 0L);// ??????????????????????????????

        final int curRound = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, 1)
                .intValue(); // ???????????????
        int curGrid = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, 0)
                .intValue();// ???????????????
        // int lastPayTime = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, 0);//
        // ????????????????????????

        GetMonopolyRs.Builder builder = GetMonopolyRs.newBuilder();
        if (0 != getType) {
            int actId = activity.getActivityId();
            List<StaticActMonopoly> sActMonoplyList = StaticActivityDataMgr.getActMonopolyListByActId(actId);
            if (CheckNull.isEmpty(sActMonoplyList)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????, roleId:,", roleId, ", activityId:",
                        actId);
            }
            List<MonopolyGrid> mgList = sActMonoplyList.stream().filter(sam -> sam.getRound() == curRound)
                    .sorted(Comparator.comparingInt(StaticActMonopoly::getGrid))
                    .map(sa -> PbHelper.createMonopolyGridPb(sa)).collect(Collectors.toList());
            builder.addAllGrid(mgList);
        }
        builder.setCurRound(curRound);
        builder.setCurGrid(curGrid);
        builder.setHasCnt((int) hasCnt);
        builder.setTodayPayGold((int) todayPayGold);
        // true?????????????????????
        builder.setIsYesterdayPay(activityDataManager.checkMonopolyYesterday(activity));
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public PlayMonopolyRs playMonopoly(long roleId, PlayMonopolyRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_MONOPOLY);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:", roleId);
        }
        // ????????????
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.MONOPOLY_LOCK_ID)) { // ??????????????????
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "?????????????????????????????????  roleId:", roleId);
        }
        long hasCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, 0L);// ??????????????????????????????
        if (hasCnt < 1) {
            throw new MwException(GameError.ACT_MONOPOLY_CNT_NOT_ENOUGH.getCode(), "??????????????????????????????  roleId:", roleId);
        }
        // ???????????????
        int round = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, 1)
                .intValue();
        // ??????????????????
        int actId = activity.getActivityId();
        List<StaticActMonopoly> sActMonoplyList = StaticActivityDataMgr.getActMonopolyListByActId(actId);
        if (CheckNull.isEmpty(sActMonoplyList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????, roleId:,", roleId, ", activityId:", actId);
        }
        List<StaticActMonopoly> sActMonopolyByRoundList = sActMonoplyList.stream()
                .filter(sam -> sam.getRound() == round).collect(Collectors.toList());
        if (CheckNull.isEmpty(sActMonopolyByRoundList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????, roleId:,", roleId, ", activityId:", actId,
                    ", round:", round);
        }
        final int maxGrid = sActMonopolyByRoundList.stream().max(Comparator.comparingInt(StaticActMonopoly::getGrid))
                .get().getGrid();
        // ????????????
        List<Integer> cntAndWeight = RandomUtil.getWeightByList(ActParamConstant.ACT_MONOPOLY_CNT_WEIGHT,
                l -> l.get(1));
        final int startGrid = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, 0)
                .intValue();// ???????????????
        final int points = cntAndWeight.get(0);// ???????????????
        final int endGrid = startGrid + points;// ?????????????????????
        // ?????????????????????
        List<StaticActMonopoly> throughActMonopoly = sActMonopolyByRoundList.stream()
                .filter(sam -> startGrid < sam.getGrid() && sam.getGrid() <= endGrid).collect(Collectors.toList());
        // ???????????? -1
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, hasCnt - 1);
        // ???????????? +1
        Long playCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_PLAY_CNT_KEY, 0L);
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_PLAY_CNT_KEY, playCnt + 1);

        PlayMonopolyRs.Builder builder = PlayMonopolyRs.newBuilder();
        // ?????????
        for (StaticActMonopoly sam : throughActMonopoly) {
            builder.addAllAward(
                    rewardDataManager.addAwardDelaySync(player, sam.getAward(), null, AwardFrom.PLAY_MONOPOLY));
            // ???????????????
            if (sam.getChatId() > 0) {
                chatDataManager.sendSysChat(sam.getChatId(), player.lord.getCamp(), 0, player.lord.getNick(),
                        sam.getAward().get(0).get(0), sam.getAward().get(0).get(1), sam.getAward().get(0).get(2));
            }
        }
        LogLordHelper.commonLog("playMonopoly", AwardFrom.PLAY_MONOPOLY, player, round, points, endGrid);

        int curRound = round; // ????????????
        int curGrid = endGrid; // ????????????
        if (endGrid >= maxGrid) {// ??????????????????????????????
            curGrid = maxGrid;
            final int nextRound = round + 1; // ?????????
            List<StaticActMonopoly> nextsActMonopolyByRoundList = sActMonoplyList.stream()
                    .filter(sam -> sam.getRound() == nextRound).collect(Collectors.toList());
            if (!CheckNull.isEmpty(nextsActMonopolyByRoundList)) { // ?????????????????????
                curRound = nextRound;
                curGrid = 0; // ????????? ,?????????0
                List<MonopolyGrid> mgList = nextsActMonopolyByRoundList.stream()
                        .sorted(Comparator.comparingInt(StaticActMonopoly::getGrid))
                        .map(sa -> PbHelper.createMonopolyGridPb(sa)).collect(Collectors.toList());
                builder.addAllGrid(mgList);
                activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY, 0L); // ??????????????????
            }
        }
        activity.getSaveMap().put(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, curRound);// ???????????????
        activity.getSaveMap().put(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, curGrid);// ??????????????????
        builder.setPoints(points);
        builder.setCurGrid(curGrid);
        builder.setCurRound(curRound);
        return builder.build();
    }

    /***
     * ??????????????????
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public ThreeRebateRs getThreeRebate(ThreeRebateRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_THREE_REBATE);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:", roleId);
        }
        player.activitys.put(ActivityConst.ACT_THREE_REBATE, activity);// ?????????????????????????????????????????????
        List<StaticPay> staticPays = null;
        staticPays = StaticVipDataMgr.getPayList();

        staticPays = staticPays.stream().filter(pay -> pay.getBanFlag() == PayService.FLAG_PAY_THREE_REBATE)
                .collect(Collectors.toList());
        Collection<CommonPb.PayInfo> payInfos = PbHelper.createPayInfo(staticPays);
        boolean isGet = false;
        boolean isHave = false;
        boolean isPay = false;

        isGet = activityDataManager.getIsGet(player, activity);
        isHave = activityDataManager.getIsHave(player);
        isPay = activityDataManager.getIsPay(player, activity);
        ThreeRebateRs.Builder builder = ThreeRebateRs.newBuilder();
        builder.setIsGet(isGet);
        builder.setIsHave(isHave);
        builder.setIsPay(isPay);
        builder.addAllPayInfo(payInfos);
        return builder.build();
    }

    /**
     * ??????????????? </br>
     * ??????????????????????????? activity???statusCnt???; key??????0,value??? ????????????????????????</br>
     * StaticActAward#param???????????? [1,188,288,110], 0?????? ?????????????????????, 1?????????2?????? ????????????????????????188~288???????????? ,3??????????????????????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public WishingRs wishing(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_WISHING_WELL);

        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????????, roleId:,", roleId);
        }
        int activityId = activityBase.getActivityId();
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityId);
        if (CheckNull.isEmpty(actAwardList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????? roleId:,", roleId, ", activityId:",
                    activityId);
        }
        // ????????????????????????????????????
        int curCnt = activity.getStatusCnt()
                .getOrDefault(ActivityConst.ActWishingWellKey.STATUSCNT_WISHING_CUR_CNT_KEY, 0L).intValue();
        final int nextCnt = curCnt + 1;
        StaticActAward nextActAward = actAwardList.stream().filter(
                        aa -> !CheckNull.isEmpty(aa.getParam()) && aa.getParam().size() >= 3 && aa.getParam().get(0) == nextCnt)
                .findFirst().orElse(null);
        if (nextActAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????(???????????????????????????) roleId:,", roleId,
                    ", activityId:", activityId, ", nextCnt:", nextCnt);
        }
        final int neeCostGold = nextActAward.getCond();// ?????????????????????
        int min = nextActAward.getParam().get(1);
        int max = nextActAward.getParam().get(2);
        if (min > max) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "????????????????????????????????? roleId:,", roleId,
                    ", activityId:", activityId, ", keyId:", nextActAward.getKeyId());
        }
        // ??????????????????
        int awardGoldCnt = RandomHelper.randomInArea(min, max + 1);
        // ??????????????????????????????, ?????????
        rewardDataManager.checkPlayerResIsEnough(player, AwardType.MONEY, AwardType.Money.GOLD, neeCostGold);
        rewardDataManager.subGold(player, neeCostGold, false, AwardFrom.WISHING_WELL_ACT);
        // ?????????
        activity.getStatusCnt().put(ActivityConst.ActWishingWellKey.STATUSCNT_WISHING_CUR_CNT_KEY, (long) nextCnt);
        activity.getStatusMap().put(nextActAward.getKeyId(), 1);
        // ?????????
        WishingRs.Builder builder = WishingRs.newBuilder();
        builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, awardGoldCnt,
                AwardFrom.WISHING_WELL_ACT));
        // ???????????????
        int extraAwardStatus = activity.getStatusMap()
                .getOrDefault(ActivityConst.ActWishingWellKey.STATUSMAP_WISHING_EXTRA_AWARD_KEY, 0).intValue(); // ?????????????????????
        if (extraAwardStatus != 1) {
            if (nextActAward.getParam().size() >= 4) {
                int w = nextActAward.getParam().get(3).intValue(); // ??????????????????
                if (RandomHelper.isHitRangeIn10000(w)) {
                    activity.getStatusMap().put(ActivityConst.ActWishingWellKey.STATUSMAP_WISHING_EXTRA_AWARD_KEY, 1);
                    List<Award> awardExra = rewardDataManager.addAwardDelaySync(player,
                            ActParamConstant.ACT_WISHING_AWARD, null, AwardFrom.WISHING_WELL_ACT);
                    chatDataManager.sendSysChat(ChatConst.CHAT_WISHING_AWARD, player.lord.getCamp(), 0,
                            player.lord.getNick()); // ???????????????
                    builder.addAllAward(awardExra);
                }
            }
        }
        builder.setRemainGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetSpecialActRs getSpecialAct(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GetSpecialActRs.Builder builder = GetSpecialActRs.newBuilder();
        List<StaticSpecialPlan> specialPlans = StaticActivityDataMgr.getSpecialPlans();
        for (StaticSpecialPlan plan : specialPlans) {
            builder.addActs(PbHelper.createSpecialAct(plan));
        }

        return builder.build();
    }

    /**
     * ????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetLuckyPoolRs getLuckyPool(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LUCKY_POOL);
        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(ActivityConst.ACT_LUCKY_POOL);
        if (activity == null || globalActivity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????, roleId:,", roleId);
        }

        activityDataManager.updateLuckyPoolLive(player, player.getDailyTaskLivenss(), activity);

        GetLuckyPoolRs.Builder builder = GetLuckyPoolRs.newBuilder();

        Integer cnt = activity.getSaveMap().get(1);
        builder.setPaySum(cnt == null ? 0 : cnt);
        builder.setPoolCnt(globalActivity.getGoal());

        // ???????????????
        int liveCount = activity.getSaveMap().getOrDefault(0, 0);
        // ????????????
        int payCount = activity.getSaveMap().getOrDefault(1, 0) / ActParamConstant.LUCKY_POOL_1.get(2);

        // ????????????????????????
        int costLiveCount = activity.getSaveMap().getOrDefault(2, 0);
        // ?????????????????????
        int costPayCount = activity.getSaveMap().getOrDefault(3, 0);

        builder.setRemainCnt(liveCount + payCount - costLiveCount - costPayCount);

        List<StaticTurnplateConf> turnplateConfs = StaticActivityDataMgr
                .getActTurnPlateListByActId(activity.getActivityId());
        if (CheckNull.isEmpty(turnplateConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ??????/?????? ?????????????????????, roleId:", roleId);
        }

        for (StaticTurnplateConf conf : turnplateConfs) {
            builder.addInfo(PbHelper.createTurnplateInfo(conf));
        }
        StaticTurnplateConf conf = turnplateConfs.get(0);
        List<List<Integer>> awardList = conf.getAwardList();
        for (List<Integer> awards : awardList) {
            if (awards.size() < 5) {
                continue;
            }
            builder.addDisplay(PbHelper.createAwardPb(awards.get(0), awards.get(1), awards.get(2), awards.get(4)));
        }

        return builder.build();
    }

    /**
     * ?????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public PlayLuckyPoolRs playLuckyPool(long roleId, int turnplateId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticTurnplateConf turnplateConf = StaticActivityDataMgr.getActTurnPlateById(turnplateId);
        if (CheckNull.isNull(turnplateConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " ???????????????????????????, roleId:", roleId);
        }

        if (turnplateConf.getType() != ActivityConst.ACT_LUCKY_POOL) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????? ?????????, roleId:,", roleId);
        }

        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LUCKY_POOL);
        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(ActivityConst.ACT_LUCKY_POOL);
        if (activity == null || globalActivity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????? ?????????, roleId:,", roleId);
        }

        // ???????????????
        int liveCount = activity.getSaveMap().getOrDefault(0, 0);
        // ????????????
        int payCount = activity.getSaveMap().getOrDefault(1, 0) / ActParamConstant.LUCKY_POOL_1.get(2);

        // ????????????????????????
        int costLiveCount = activity.getSaveMap().getOrDefault(2, 0);
        // ?????????????????????
        int costPayCount = activity.getSaveMap().getOrDefault(3, 0);
        // ??????????????????
        if (turnplateConf.getCount() > liveCount + payCount - costLiveCount - costPayCount) {
            throw new MwException(GameError.ACT_PAY_TURNPLATE_CNT_NOT_ENOUGH.getCode(), "???????????? ????????????, roleId:,", roleId);
        }

        int type = 0;
        int id = 0;
        int count = 0;
        int poolCount = globalActivity.getGoal();
        List<Integer> rList = null;
        PlayLuckyPoolRs.Builder builder = PlayLuckyPoolRs.newBuilder();
        for (int i = 0; i < turnplateConf.getCount(); i++) {
            // ???????????? ?????????????????????????????????????????????????????????
            if (i < liveCount - costLiveCount) {
                rList = RandomUtil.getWeightByList(turnplateConf.getAwardList().stream()
                        .filter(l -> l.get(0) != AwardType.MONEY_PECENT).collect(Collectors.toList()), l -> l.get(3));
                costLiveCount++;
            } else {
                rList = RandomUtil.getWeightByList(turnplateConf.getAwardList(), l -> l.get(3));
                costPayCount++;
            }
            if (rList == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "???????????? ????????????, roleId:,", roleId);
            }
            type = rList.get(0);
            id = rList.get(1);
            count = rList.get(2);
            // ????????????????????????
            if (type == AwardType.MONEY_PECENT) {
                count = (int) (poolCount * (count / 10000d));
                poolCount -= count;
                chatDataManager.sendSysChat(ChatConst.CHAT_LUCKY_POOL, 0, 0, player.lord.getNick(), rList.get(2) / 100);
                globalActivity.addLuckyPoolRank(
                        new SimpleRank(System.currentTimeMillis() / 1000, player.lord.getNick(), rList.get(2), count));
            }

            rewardDataManager.addAward(player, type, id, count, AwardFrom.LUCKY_POOL);
            builder.addAward(PbHelper.createAwardPb(type, id, count, rList.get(4)));
            LogLordHelper.commonLog("playLuckyPool", AwardFrom.LUCKY_POOL, player, turnplateConf.getTurnplateId());
        }

        // ??????????????????
        globalActivity.setGoal(poolCount);
        activityDataManager.syncAllPlayerActChange(player, ActivityConst.ACT_LUCKY_POOL);

        // ??????????????????
        activity.getSaveMap().put(2, costLiveCount);
        activity.getSaveMap().put(3, costPayCount);

        builder.setPoolCnt(poolCount);
        return builder.build();
    }

    /**
     * ????????????
     *
     * @return
     * @throws MwException
     */
    public GetLuckyPoolRankRs getLuckyPoolRank(long date) throws MwException {
        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(ActivityConst.ACT_LUCKY_POOL);
        if (globalActivity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "?????????????????????");
        }

        return globalActivity.getSimpleRankPb(date).build();
    }

    public Base.Builder shareRewardProcess(GetActivityAwardRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int keyId = req.getKeyId();// ????????????id
        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);// ????????????????????????Award
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
        }
        Activity activity = player.activitys.get(ActivityConst.ACT_SHARE_REWARD);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        Integer awardStatus = activity.getStatusMap().get(keyId);
        if (awardStatus != null && awardStatus != 0) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "???????????????, roleId:,", roleId);
        }
        HttpPb.ShareRewardRq.Builder builder = HttpPb.ShareRewardRq.newBuilder().setKeyId(player.account.getAccountKey()).setLordId(player.roleId).setPlatNo(player.account.getPlatNo()).setAwardId(keyId);

        // ??????????????????????????????
        return PbHelper.createRqBase(HttpPb.ShareRewardRq.EXT_FIELD_NUMBER, null, HttpPb.ShareRewardRq.ext,
                builder.build());
    }

    public void shareRewardRs(HttpPb.ShareRewardRs req) throws MwException {
        String result = req.getResult();
        // ????????????id
        int keyId = req.getKeyId();
        long roleId = req.getLordId();

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);// ????????????????????????Award
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????, roleId:,", roleId);
        }
        Activity activity = player.activitys.get(ActivityConst.ACT_SHARE_REWARD);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }

        List<List<Integer>> awardList = actAward.getAwardList();
        // ????????????????????????
        rewardDataManager.checkBag(player, awardList);

        JSONObject resJson = JSONObject.parseObject(result);
        int cnt = resJson.getInteger("count");
        // ????????????
        if (cnt >= actAward.getCond()) {
            LogUtil.debug("GetActivityAwardRs activity=" + activity);
            activity.getStatusMap().put(keyId, 1);

            int awardCnt = 1;
            GetActivityAwardRs.Builder builder = GetActivityAwardRs.newBuilder();
            int size = awardList.size();
            // ??????????????????
            int num = activityDataManager.getActDoubleNum(player);
            for (int i = 0; i < awardList.size(); i++) {
                List<Integer> e = awardList.get(i);
                int type = e.get(0);
                int itemId = e.get(1);
                int count = e.get(2);
                count *= num;// ????????????
                count *= awardCnt;// ????????????
                if (type == AwardType.EQUIP) {
                    for (int c = 0; c < count; c++) {
                        rewardDataManager.sendRewardSignle(player, type, itemId, 1, AwardFrom.ACTIVITY_AWARD, keyId, activity.getActivityId(), activity.getActivityType());
                    }
                } else {
                    rewardDataManager.sendRewardSignle(player, type, itemId, count, AwardFrom.ACTIVITY_AWARD, keyId, activity.getActivityId(), activity.getActivityType());
                }
            }
            // Base msg = PbHelper.createSynBase(GetActivityAwardRs.EXT_FIELD_NUMBER, GetActivityAwardRs.ext, builder.build()).build();
            // if (player.ctx != null && player.isLogin) {
            //     MsgDataManager.getIns().add(new Msg(player.ctx, msg, player.roleId));
            // }
        }

    }


    /**
     * ????????????????????????(????????????????????????)
     *
     * @param roleId ??????id
     * @return ???????????????????????????
     * @throws MwException ???????????????
     */
    public AdvertisementRewardRs advertisementReward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int cnt = player.getMixtureDataById(PlayerConstant.DAILY_ADVERTISEMENT_REWARD);

        int count = cnt + 1;
        if (count > Constant.ADVERTISEMENT_REWARD.size()) {
            throw new MwException(GameError.ADVERTISEMENT_REWARD_COUNT.getCode(), "????????????????????????????????????, roleId:", roleId);
        }

        AdvertisementRewardRs.Builder builder = AdvertisementRewardRs.newBuilder();
        List<Integer> reward = Constant.ADVERTISEMENT_REWARD.get(cnt);
        if (!CheckNull.isEmpty(reward)) {
            player.setMixtureData(PlayerConstant.DAILY_ADVERTISEMENT_REWARD, count);
            // ????????????null
            builder.addAward(rewardDataManager.sendRewardSignle(player, reward.get(0), reward.get(1), reward.get(2), AwardFrom.ADVERTISEMENT_REWARD));
            builder.setCount(count);
        }
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param req ????????????
     * @throws MwException ???????????????
     */
    public void webChatSignReward(HttpPb.WeChatSignRewardRq req) throws MwException {
        long roleId = req.getLordId();
        int days = req.getDays();

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Activity activity = player.activitys.get(ActivityConst.ACT_WECHAT_SIGNIN);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "???????????????, roleId:,", roleId);
        }
        List<StaticActAward> sActAwards = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (CheckNull.isEmpty(sActAwards)) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "??????????????????, roleId:,", roleId);
        }

        Date now = new Date();
        //?????????????????????????????????
        int lastSignTime = activity.getEndTime();
        boolean isTodaySign = DateHelper.dayiy(TimeHelper.secondToDate(lastSignTime), now) == 1;
        if (isTodaySign) {
            throw new MwException(GameError.SIGNATURE_ERR.getCode(), String.format("roleId :%d, days :%d, ????????????????????????, ???????????? :%d", roleId, days, lastSignTime));
        }

        // ????????????
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        int signTime = statusMap.getOrDefault(days, 0);//??????????????????
        int subDays = DateHelper.dayiy(TimeHelper.secondToDate(signTime), now);
        //2022-01-13 ????????????.  ?????????????????????,??????????????????????????????????????????????????????,
        // eg: ???1????????????????????????1?????????????????????1???, ???2????????????????????????2?????????????????????2???, ...???7??????????????????????????????7?????????7???
        if (subDays <= days) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), String.format("days :%d, ????????????????????????, roleId: %d, signTime :%d", days, roleId, signTime));
        }
        int nowSec = (int) (now.getTime() / 1000);
        // ????????????
        List<StaticActAward> signAward = sActAwards.stream().filter(sca -> sca.getParam().get(0) == 0 && sca.getCond() == days).collect(Collectors.toList());
        if (days == 7) {
            // ????????????
            signAward.add(sActAwards.stream().filter(sca -> sca.getParam().get(0) == 1).findFirst().orElse(null));
        }
        List<Award> awardList = signAward.stream().filter(Objects::nonNull).flatMap(aw -> aw.getAwardList().stream().map(al -> PbHelper.createAwardPb(al.get(0), al.get(1), al.get(2)))).collect(Collectors.toList());
        if (!CheckNull.isEmpty(awardList)) {
            // ??????????????????
            statusMap.put(days, nowSec);
            activity.setEndTime(nowSec);
            // ????????????
            mailDataManager.sendAttachMail(player, awardList, MailConstant.MOLD_WECHAT_SIGN_REWARD, AwardFrom.ACT_SUPPLY_DORP_RETURN, nowSec);
        } else {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "??????????????????, roleId :", roleId, " days :", days);
        }
    }

    public void autoConverActItems(int actType, String jobKey) {
        if (actType == ActivityConst.ACT_CHRISTMAS || actType == ActivityConst.ACT_REPAIR_CASTLE) {
            activityChristmasService.overAutoConver(jobKey);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param now
     */
    public void combineServerAct(Player player, int now) {
        if (CheckNull.isEmpty(player.activitys)) {
            return;
        }

        for (int actType : ActivityConst.COMBINED_SERVICE_REMOVED_ACT_TYPE) {
            Activity activity = player.activitys.get(actType);
            if (CheckNull.isNull(activity))
                continue;

            activityDataManager.processCombineServerAct(player, activity.getActivityType(), now);
        }
    }
}
