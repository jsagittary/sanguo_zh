package com.gryphpoem.game.zw.service.activity;

import com.alibaba.fastjson.JSONObject;
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
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.anniversary.ActivityFireWorkService;
import com.gryphpoem.game.zw.service.activity.cross.CrossRechargeLocalActivityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

    /**
     * 活动开启列表
     *
     * @param roleId
     */
    public GetActivityListRs getActivityList(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // int platFlag = 1;// 默认为安卓玩家
        // int platNo = player.account.getPlatNo();
        List<ActivityBase> list = StaticActivityDataMgr.getActivityList();
        GetActivityListRs.Builder builder = GetActivityListRs.newBuilder();
        Date now = new Date();
        for (ActivityBase actBase : list) {
            try {
                int activityType = actBase.getActivityType();
                if (ActivityConst.ACT_LIGHTNING_WAR == activityType) {// 闪电战活动
                    actBase = changeActivityTime(actBase);
                }
                int open = actBase.getBaseOpen();
                if (open == ActivityConst.OPEN_CLOSE || actBase.isBaseDisplay()) {// 活动未开启
                    continue;
                }
                Activity activity = activityDataManager.getActivityInfo(player, activityType);
                if (activity == null) {
                    continue;
                }
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
                    /*|| ActivityConst.ACT_DAILY_PAY == activityType*/) { // 通用活动会走此处
                    ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                    int activityKeyId = activityBase.getActivityId();
                    List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                    int schedule = 0;// 某类活动当前的进度值，或者排名活动的当前名次
                    if (condList != null) {
                        schedule = activityDataManager.currentActivity(player, activity, 0);
                        for (StaticActAward saa : condList) {
                            int keyId = saa.getKeyId();
                            if (!activity.getStatusMap().containsKey(keyId) && schedule >= saa.getCond()) {// 未领取奖励
                                if (activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY) {// 全军返利的 额外逻辑
                                    // 玩家充值的金币总数
                                    int topup = activity.getSaveMap()
                                            .get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                                            : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD);
                                    // 全军返利 个人需充值的金币数
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
                        if (activityType == ActivityConst.ACT_LEVEL && null == activity.getStatusCnt().get(0)) {// 说明没有购买成长计划
                            tips = 0; // 可领取清空
                        }
                        if ((activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY)
                                && player.lord.getLevel() < ActParamConstant.ACT_ALL_CHARGE_LORD_LV) {
                            tips = 0; // 可领取清空
                        }
                    }
                } else if (ActivityConst.ACT_LUCKY_TURNPLATE == activityType) { // 幸运 转盘
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.FAMOUS_GENERAL_TURNPLATE == activityType) {// 名将转盘
                    tips = activityDataManager.getExchangeActCnt(player, activity);
                } else if (ActivityConst.ACT_EQUIP_TURNPLATE == activityType) { // 装备转盘
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (StaticActivityDataMgr.isActTypeRank(activityType)) {// 排行活动
                    int activityKeyId = actBase.getActivityId();
                    List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                    if (!CheckNull.isEmpty(condList)) {
                        int schedule = activityDataManager.getRankAwardSchedule(player, activityType);
                        for (StaticActAward saa : condList) {
                            int keyId = saa.getKeyId();
                            // 新 充值排行领奖, 阵营排行奖励, 阵营战斗力排行
                            if (ActivityConst.ACT_PAY_RANK_NEW == activityType
                                    || ActivityConst.ACT_CAMP_RANK == activityType
                                    || ActivityConst.ACT_CAMP_FIGHT_RANK == activityType
                                    || ActivityConst.ACT_PAY_RANK_V_3 == activityType
                                    || ActivityConst.ACT_MERGE_PAY_RANK == activityType
                                    || ActivityConst.ACT_CONSUME_GOLD_RANK == activityType
                                    || ActivityConst.ACT_TUTOR_RANK == activityType) {
                                int startRank = 0;// 起始名次
                                if (!CheckNull.isEmpty(saa.getParam()) && saa.getParam().size() > 1) {
                                    startRank = saa.getParam().get(1);
                                }
                                if (!activity.getStatusMap().containsKey(keyId) && schedule > 0 && schedule >= startRank
                                        && schedule <= saa.getCond() && now.getTime() >= awardBeginTime.getTime()) {// 未领取奖励
                                    tips++;
                                }
                            } else {
                                // 通用活动排行tips规则
                                if (!activity.getStatusMap().containsKey(keyId) && schedule > 0 && schedule <= saa.getCond()
                                        && now.getTime() >= awardBeginTime.getTime()) {// 未领取奖励
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
                ) {// 大咖带队,全服VIP等级, 挑战战役, 训练士兵, 装备物资
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (activityType == ActivityConst.ACT_VIP_BAG) { // 支付礼包活动
                    Date nowDate = new Date();
                    // int createLordDay = DateHelper.dayiy(player.account.getCreateDate(), nowDate); // 创建角色的第几天
                    // int openServerDay = serverSetting.getOpenServerDay(nowDate);// 开服第几天
                    // List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftpackPlanMapByDay(createLordDay,
                    //         openServerDay, nowDate);

                    List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftPackPlanByDate(player.account.getCreateDate(), serverSetting.getOpenServerDate(), nowDate);
                    if (CheckNull.isEmpty(planList)) {
                        continue;
                    }
                } else if (activityType == ActivityConst.ACT_FIRSH_CHARGE) {// 首充礼包
                    long createTime = player.account.getCreateDate().getTime();
                    long disPlayTime = createTime + (ActParamConstant.ACT_FIRSH_CHARGE_TIME * 1000L);
                    // Activity act = activityDataManager.getActivityInfo(player, activityType);
                    // actBase.setEndTime(new Date(disPlayTime));
                    actBase = copyActiviyBase(actBase, player.account.getCreateDate(), new Date(disPlayTime));
                    if (now.getTime() > disPlayTime) {
                        continue;
                    }
                } else if (activityType == ActivityConst.ACT_BLACK || activityType == ActivityConst.ACT_ROBIN_HOOD) { // 黑鹰计划或者新黑鹰计划
                    try {
                        int endTime = checkBlackhawkIsOver(player, TimeHelper.getCurrentSecond());
                        actBase = copyActiviyBase(actBase, player.account.getCreateDate(), new Date(endTime * TimeHelper.SECOND_MS));
                        // actBase.setEndTime(new Date(endTime * 1000L));
                    } catch (MwException mwe) { // 有异常说明黑鹰计划结束
                        LogUtil.common("---------警告黑鹰计划或者罗宾汉活动已经结束-----------", mwe.getMessage());
                        continue;
                    }
                } else if (activityType == ActivityConst.ACT_7DAY) {// 七日活动
                    int cRoleDay = playerDataManager.getCreateRoleDay(player, now);
                    if (cRoleDay > 7) continue; // 过期不显示
                    if (player.day7Act == null) continue; // 没有初始化也不显示
                    tips = player.day7Act.getCanRecvKeyId().size();
                } else if (ActivityConst.ACT_PAY_7DAY == activityType) {// 七日充值
                    int cRoleDay = playerDataManager.getCreateRoleDay(player, now);
                    if (cRoleDay > 7) continue; // 过期不显示
                    int afterDayTime = DateHelper.afterDayTime(player.account.getCreateDate(), 8);
                    Date endTime = new Date((afterDayTime - 1) * 1000L);
                    actBase = copyActiviyBase(actBase, player.account.getCreateDate(), endTime);
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_CHARGE_TOTAL == activityType) {// 累计充值
                    tips = activityDataManager.getCurActTips(player, activityType);// 无需拷贝直接读表
                } else if (activityType == ActivityConst.ACT_CHARGE_CONTINUE || activityType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {// 连续充值
                    // 连续充值奖励tips
                    tips = activityChargeContinueService.getChargeContinueTips(player, activityType);
                } else if (activityType == ActivityConst.ACT_FOOD) {// 体力赠送
                    try {
                        refreshPowerState(player.roleId, activity);
                    } catch (MwException mwe) {
                        continue; // 异常就过滤掉
                    }
                    for (Long state : activity.getStatusCnt().values()) {
                        if (state == 1) {
                            tips++;
                        }
                    }
                } else if (activityType == ActivityConst.ACT_SUPPLY_DORP) {// 空降补给
                    List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
                    for (StaticActAward actAward : awardList) {
                        if (isSupplyDorpGet(actAward, activity, now.getTime())) {
                            tips++;
                        }
                    }
                    if (checkSupplyIsAllGot(activity) && actBase.getBaseOpen() == ActivityConst.OPEN_AWARD) {
                        continue;
                    }
                } else if (ActivityConst.ACT_DAILY_PAY == activityType) { // 每日充值
                    ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
                    int activityKeyId = activityBase.getActivityId();
                    List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                    if (!CheckNull.isNull(condList)) {
                        int schedule = activityDataManager.currentActivity(player, activity, 0);
                        for (StaticActAward sa : condList) {
                            int keyId = sa.getKeyId();
                            if (!activity.getStatusMap().containsKey(keyId) && schedule >= sa.getCond()
                                    && activityDataManager.caluActCailyPayStatus(activityBase.getActivityId(), sa,
                                    activity) == 0) {// 未领取奖励
                                tips++;
                            }
                            // 之前每日充值,免费领奖的特殊处理
                            if (sa.getParam().get(0) == 0 && !activity.getStatusMap().containsKey(keyId)) {
                                tips++;
                            }
                        }
                    }
                } else if (ActivityConst.ACT_LOGIN_EVERYDAY == activityType) {// 每日登陆活动
                    if (activity.getStatusCnt().isEmpty()) {
                        int pLv = player.lord.getLevel();
                        activity.getStatusCnt().put(0, (long) pLv); // 存储当时等级
                    }
                    if (activity.getStatusMap().isEmpty()) {
                        tips++;
                    }
                } else if (ActivityConst.ACT_ATK_GESTAPO == activityType) {// 盖世太保活动
                    tips = activityDataManager.getExchangeActCnt(player, activity);
                } else if (ActivityConst.ACT_ATTACK_CITY_NEW == activityType) {// 新攻城掠地活动
                    int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // 活动开启的第几天
                    if (CheckNull.isNull(player.atkCityAct)) continue;
                    List<StaticAtkCityAct> actList = StaticActivityDataMgr.getAtkCityActList();
                    // 进度红点
                    if (!CheckNull.isEmpty(actList)) {
                        for (StaticAtkCityAct staticAtkCityAct : actList) {
                            int canRecvCnt = activityDataManager.getCanRecvCnt(player, staticAtkCityAct,
                                    player.atkCityAct);
                            if (canRecvCnt > 0 && staticAtkCityAct.getDay() <= dayiy) {
                                tips++;
                            }
                        }
                    }
                    // 活跃度红点
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
                        tips = 0; // 可领取清空
                    }
                } else if (ActivityConst.ACT_PROP_PROMOTION == activityType) { // 军备促销
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_PAY_TURNPLATE == activityType) {// 充值转盘
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_LUCKY_POOL == activityType) {// 幸运奖池
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_ORE_TURNPLATE == activityType) {// 矿石转盘
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_DAY_DISCOUNTS == activityType) {// 每日特惠
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_GIFT_PROMOTION == activityType) {// 礼物促销
                    Date[] dateArr = activityHelpService.getGiftPromotionDate();
                    if (dateArr == null) continue;// 不在活动期间内
                    actBase = copyActiviyBase(actBase, dateArr[0], dateArr[1]);
                } else if (ActivityConst.ACT_MONOPOLY == activityType) {// 大富翁
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_THREE_REBATE == activityType) { // 三倍返利
                    tips = activityDataManager.ThreeRebateTips(player, activityType);
                } else if (ActivityConst.ACT_SIGIN == activityType) { // 签到
                    tips = signInService.getRedPoint(player, activity);
                } else if (ActivityConst.ACT_WAR_PLANE_SEARCH == activityType) {
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_EASTER == activityType) {
                    // 复活节活动
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_BUILD_GIFT == activityType) {
                    // 如果已经领取了免费奖励, 兼容线上
                    if (activityDataManager.currentActivity(player, activity, 0) == 2) {
                        int freeAwardTime = activity.getSaveMap().getOrDefault(1, 0);
                        // 没有记录下免费领取的时间
                        if (freeAwardTime == 0) {
                            // 记录免费礼包的领取时间
                            activity.getSaveMap().put(1, TimeHelper.getCurrentSecond());
                        }
                    }
                    int freeTime = activity.getSaveMap().getOrDefault(1, 0);
                    if (freeTime > 0) {
                        long disPlayTime = (freeTime + ActParamConstant.BUILD_GIFT_CHARGE_TIME) * TimeHelper.SECOND_MS;
                        actBase = copyActiviyBase(actBase, actBase.getBeginTime(), new Date(disPlayTime));
                        // 过期就不显示了
                        if (now.getTime() > disPlayTime) {
                            continue;
                        }
                    }
                } else if (ActivityConst.ACT_HOT_PRODUCT == activityType) { // 热销商品
                    tips = activityDataManager.getCurActTips(player, activityType);
                } else if (ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE == activityType) {
                    long createTime = activity.getStatusCnt().getOrDefault(0, 0L);
                    if (createTime > 0L) {
                        long disPlayTime = TimeHelper.getSomeDayAfter(TimeHelper.secondToDate((int) createTime), ActParamConstant.ACT_DEDICATED_CUSTOMER_SERVICE_CONF.get(1).get(0) - 1, 23, 59, 59) * TimeHelper.SECOND_MS;
                        actBase = copyActiviyBase(actBase, player.account.getCreateDate(), new Date(disPlayTime));
                        if (now.getTime() > disPlayTime) {
                            continue;
                        }
                    }
                } else if(ActivityConst.ACT_CHRISTMAS == activityType || ActivityConst.ACT_REPAIR_CASTLE == activityType){
                    tips = 1;
                }else if (ActivityConst.ACT_ANNIVERSARY_FIREWORK == activity.getActivityType()){
                    tips = activityFireWorkService.getTips(activity, actBase);
                }else if (ActivityConst.CROSS_ACT_RECHARGE_RANK==activity.getActivityType()){
                    tips = crossRechargeLocalActivityService.getActivityTips(actBase, activity);
                } else if (ActivityConst.ACT_MUSIC_FESTIVAL_BOX_OFFICE == activity.getActivityType()) {
                    tips = activityBoxOfficeService.getActivityTips(actBase, activity);
                }
                // 如果都完成了，隐藏次活动
                if (isAllGainActivity(player, actBase, activity)) {
                    continue;
                }
                if (open != ActivityConst.OPEN_STEP && !ActivityConst.isEndDisplayAct(activityType)) { // 部分活动结束后还显示
                    continue;
                }
                boolean cangetAward = true;// 可领奖，不可领奖
                if(ActivityConst.ACT_CHRISTMAS == activityType || ActivityConst.ACT_REPAIR_CASTLE == activityType){
                    builder.addActivity(activityChristmasService.buildActivityPb(activity,actBase,cangetAward,tips));
                }else {
                    builder.addActivity(PbHelper.createActivityPb(actBase, cangetAward, tips));
                }
            } catch (Exception error) {
                LogUtil.error("---------警告活动列表出错----------- actType:", actBase.getActivityType());
                LogUtil.error("---------警告活动列表出错-----------", error);
                continue;
            }
        }
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        int dayiy = DateHelper.dayiy(beginTime, now);
        builder.setDay(dayiy);
        builder.addAllPreVieAct(getDisplayActList(list));
        // 预显示列表
        return builder.build();
    }

    /**
     * 拷贝 ActivityBase
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
     * 闪电战活动,将beginDate和endDate加上详细的时间点
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
     * 获取预显示的活动列表
     *
     * @param list
     * @return
     */
    private List<CommonPb.Activity> getDisplayActList(List<ActivityBase> list) {
        List<CommonPb.Activity> actList = new ArrayList<>(list.size());
        for (ActivityBase actBase : list) {
            try {
                int activityType = actBase.getActivityType();
                if (ActivityConst.ACT_LIGHTNING_WAR == activityType) { // 闪电战
                    actBase = changeActivityTime(actBase);
                }
                if (!actBase.isBaseDisplay()) continue;
                // int display = actBase.getBaseDisplay();
                // if (display != ActivityConst.DISPLAY_OPEN) continue;
                boolean cangetAward = false;// 可领奖，不可领奖
                actList.add(PbHelper.createActivityPb(actBase, cangetAward, 0));
            } catch (Exception e) {
                LogUtil.error(e, "获取活动显示列表出错: actType:", actBase.getActivityType(), ", actId:",
                        actBase.getActivityId());
            }
        }
        return actList;
    }

    /**
     * 获取所有活动状态在DISPLAY-OPEN阶段的活动
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
     * 获取某个活动的数据,通用活动才会走此处
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", type);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", type);
        }
        LogUtil.debug("getActivity activity=" + activity);
        int activityKeyId = activityBase.getActivityId();
        GetActivityRs.Builder builder = GetActivityRs.newBuilder();
        int state = 0;
        if (activity.getOpen() == ActivityConst.OPEN_STEP) {
            state = activityDataManager.currentActivity(player, activity, 0, now);
        }
        // 同一类型任务条件
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
            // 某类活动当前的进度值，或者排名活动的当前名次
            int schedule = activityDataManager.currentActivity(player, activity, 0, now);
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            condList = filterAwardByOLAct(player, activity, condList);
            if (condList != null) {
                for (StaticActAward e : condList) {
                    int keyId = e.getKeyId();
                    int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
                    if (ActivityConst.ACT_GIFT_OL == activity.getActivityType()
                            || ActivityConst.ACT_ONLINE_GIFT == activity.getActivityType() && status == 0) {
                        schedule = e.getCond() > schedule ? e.getCond() - schedule + now : now;
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, schedule));
                        checkNextPhaseAward(builder, e, condList);
                        break;
                    } else if (ActivityConst.ACT_DAILY_PAY == activity.getActivityType()) {
                        if (e.getParam().get(0) <= 0) {// 每日充值 首次免费
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
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, chargeGold));// 添加活动时长:天
                    } else {
                        builder.addActivityCond(PbHelper.createActivityCondPb(e, status, schedule));
                    }
                }
            }
            if (type == ActivityConst.ACT_ALL_CHARGE || type == ActivityConst.ACT_BRAVEST_ARMY) {// 全军返利
                // 3个阵营的充值
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
            } else if (type == ActivityConst.ACT_LEVEL) { // 成长基金
                Long val = activity.getStatusCnt().get(0);
                state = val != null ? val.intValue() : 0;
            } else if (type == ActivityConst.ACT_CHARGE_CONTINUE
                    || type == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {
                builder.addParam(activityBase.getDayiyBegin());// 添加活动时长:天
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_VIP
                || ActivityConst.ACT_ATTACK_CITY == activity.getActivityType()
                || ActivityConst.ACT_CHALLENGE_COMBAT == activity.getActivityType()
                || ActivityConst.ACT_TRAINED_SOLDIERS == activity.getActivityType()
                || ActivityConst.ACT_TRAINED_SOLDIERS_DAILY == activity.getActivityType()
                || ActivityConst.ACT_EQUIP_MATERIAL == activity.getActivityType()
                || ActivityConst.ACT_ELIMINATE_BANDIT == activity.getActivityType()
        ) {// 大咖带队 攻占据点 挑战战役 训练士兵 消灭匪军
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            if (condList != null) {
                for (StaticActAward saa : condList) {
                    int schedule = activityDataManager.currentActivity(player, activity, saa.getParam().get(0));
                    int keyId = saa.getKeyId();
                    int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
                    builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
                }
            }
        } else if (ActivityConst.ACT_LOGIN_EVERYDAY == activity.getActivityType()) {// 每日登陆活动
            Long lv = activity.getStatusCnt().get(0);
            if (lv != null) {
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                int lvInt = lv.intValue();
                for (StaticActAward saa : condList) {
                    List<Integer> lvRegion = saa.getParam();
                    if (lvRegion != null && lvRegion.size() > 1 && lvRegion.get(0) <= lvInt
                            && lvRegion.get(1) >= lvInt) {
                        int keyId = saa.getKeyId();
                        int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
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
                    // int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
                    // 兑换次数
                    int status = activity.getStatusCnt().containsKey(keyId)
                            ? new Long(activity.getStatusCnt().get(keyId)).intValue() : 0;
                    builder.addActivityCond(PbHelper.createActivityCondPb(exchange, status, schedule));
                }
            }
        } else if (ActivityConst.ACT_PROP_PROMOTION == activity.getActivityType()) {
            // 3个阵营的充值积分
            GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activity.getActivityType());
            if (!CheckNull.isNull(globalActivity)) {
                long integral = globalActivity.getCampValByCamp(player.lord.getCamp());
                for (StaticPromotion promotion : StaticActivityDataMgr
                        .getStaticPromotionListByActId(activity.getActivityId())) {
                    int keyId = promotion.getPromotionId();
                    // 购买打折礼包次数
                    int cnt = activity.getStatusCnt().containsKey(keyId)
                            ? new Long(activity.getStatusCnt().get(keyId)).intValue() : 0;
                    int status = cnt > 0 ? 1 : 0;
                    builder.addActivityCond(PbHelper.createActivityCondPb(promotion, status, cnt));
                }
                // 积分箱
                List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
                for (StaticActAward sAward : condList) {
                    int keyId = sAward.getKeyId();
                    int awardCnt = activityDataManager.getAwardCnt(sAward, activity, integral); // 可领取次数
                    builder.addActivityCond(PbHelper.createActivityCondPb(sAward, -1, awardCnt));
                }
                builder.addParam(new Long(integral).intValue()); // 军团积分
            }
        } else if (ActivityConst.ACT_FIRSH_CHARGE == activity.getActivityType()) {// 首充礼包
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            for (StaticActAward saa : condList) {
                int status = activity.getStatusMap().containsKey(saa.getKeyId()) ? 1 : 0;
                int schedule = activityDataManager.currentActivity(player, activity, saa.getSortId());
                builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
            }
        } else if (ActivityConst.ACT_GIFT_PROMOTION == activity.getActivityType()) { // 礼物特惠
            if (activityHelpService.getGiftPromotionDate() == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                        type);
            }
            for (StaticPromotion promotion : StaticActivityDataMgr
                    .getStaticPromotionListByActId(activity.getActivityId())) {
                int keyId = promotion.getPromotionId();
                // 购买打折礼包次数
                int cnt = activity.getStatusCnt().containsKey(keyId)
                        ? activity.getStatusCnt().get(keyId).intValue() : 0;
                int status = cnt > 0 ? 1 : 0;
                builder.addActivityCond(PbHelper.createActivityCondPb(promotion, status, cnt));
            }
        }else if (ActivityConst.ACT_MERGE_PROP_PROMOTION == activity.getActivityType()){
            activityMergePromotionService.buildActivity(builder, player, activity);
        }else if (ActivityConst.ACT_CAMP_RANK == activity.getActivityType()) {// 开服阵营排行

        } else if (ActivityConst.ACT_WISHING_WELL == activity.getActivityType()) { // 许愿池活动
            List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            int curCnt = activity.getStatusCnt()
                    .getOrDefault(ActivityConst.ActWishingWellKey.STATUSCNT_WISHING_CUR_CNT_KEY, 0L).intValue();
            for (StaticActAward saa : actAwardList) {
                int status = !CheckNull.isEmpty(saa.getParam()) && saa.getParam().size() >= 3
                        && saa.getParam().get(0) <= curCnt ? 1 : 0;
                builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, 0));
            }
        }
        // 兑换活动和周年庆礼包
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
            // 每日获取掉落道具的数量
            if (activity.getActivityType() == ActivityConst.ACT_BANDIT_AWARD) {
                builder.addParam(activityDataManager.getActHitAwardCnt(player, activity.getActivityType()));
            }
        }
        // 战机开发活动
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
                    int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
                    builder.addActivityCond(PbHelper.createActivityCondPb(saa, status, schedule));
                }
            }
        } else if (ActivityConst.ACT_EASTER == activity.getActivityType()) {
            Map<Integer, List<StaticEasterAward>> condAwardMap = StaticActivityDataMgr.getEasterAwardCondMap(activityKeyId);
            if (!CheckNull.isEmpty(condAwardMap)) {
                // 复活节活动
                int schdeule = activityDataManager.currentActivity(player, activity, 0);
                // 活动进度
                Map<Integer, Integer> statusMap = activity.getStatusMap();
                condAwardMap.forEach((key, value) -> {
                    int cond = key;
                    // 已经砸蛋的数量
                    int actAward = 0;
                    // 取出keyId
                    List<Integer> listSort = value.stream().map(StaticEasterAward::getKeyId).collect(Collectors.toList());
                    // 打乱顺序
                    Collections.shuffle(listSort, new Random(roleId + activityKeyId));
                    List<StaticEasterAward> easterAwardList = listSort
                            .stream()
                            .map(StaticActivityDataMgr::getEasterAwardListByKey)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    for (StaticEasterAward sea : easterAwardList) {
                        // 这个蛋是否被砸了
                        int status = statusMap.containsKey(sea.getKeyId()) ? 1 : 0;
                        if (status == 1) {
                            ++actAward;
                        }
                        builder.addActivityCond(PbHelper.createActivityCondPb(sea, status, schdeule));
                    }
                    // 活动档位奖励
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
            //实名认证和手机绑定，服务端不做校验，只记录领取奖励状态
            List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
            for (StaticActAward saa : condList) {
                int keyId = saa.getKeyId();
                state = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
                break;
            }
        } else if (ActivityConst.ACT_HOT_PRODUCT == activity.getActivityType()) {
            activityHotProductService.getActivity(player, activity, builder);
        } else if (ActivityConst.ACT_GOOD_LUCK == activity.getActivityType()) {
            activityLotteryService.getGoodLuckActivity(player, activity, builder);
        }else if(activity.getActivityType() == ActivityConst.ACT_CHRISTMAS
                || activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE){
            List<StaticActExchange> exchangeList = StaticActivityDataMgr.getActExchangeListById(activityKeyId);
            int schedule = activityDataManager.currentActivity(player, activity, 0);
            if (!CheckNull.isEmpty(exchangeList)) {
                for (StaticActExchange exchange : exchangeList) {
                    Integer keyId = exchange.getKeyId();
                    // 已兑换次数
                    int status = activity.getStatusCnt().containsKey(keyId) ? new Long(activity.getStatusCnt().get(keyId)).intValue() : 0;
                    builder.addActivityCond(PbHelper.createActivityCondPb(exchange, status, schedule));
                }
            }
        } else if (activity.getActivityType() == ActivityConst.ACT_MONSTER_NIAN) {
            state = activityMonsterNianService.buildActivity(builder, activityBase, activity);
        }else if (activity.getActivityType() == ActivityConst.ACT_ANNIVERSARY_FIREWORK){
            state = activityFireWorkService.buildActivity(builder, activityBase, activity);
        }
        builder.setState(state);
        return builder.build();
    }

    /**
     * Type:301. 在线奖励根据等级区间来获取具体的奖励
     *
     * @param player
     * @param activity
     * @param condList
     * @return
     */
    private List<StaticActAward> filterAwardByOLAct(Player player, Activity activity, List<StaticActAward> condList) {
        if (ActivityConst.ACT_ONLINE_GIFT == activity.getActivityType()) {
            int[] params = new int[2];
            // 未领取奖励
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
            // 转点获取最新等级区间,并存入propMap
            if (CheckNull.isEmpty(activity.getPropMap())) {
                activity.getPropMap().put(0, player.lord.getLevel());
            }
            if (!CheckNull.isEmpty(activity.getPropMap())) {// 当天首次登陆的等级区间
                params[0] = activity.getPropMap().get(0);
                params[1] = activity.getPropMap().get(0);
            } else if (CheckNull.isEmpty(activity.getStatusMap())) {// 转点获取最新等级区间
                params[0] = player.lord.getLevel();
                params[1] = player.lord.getLevel();
            } else {// 获取已领取的区间
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
     * 检查下阶段奖励
     *
     * @param builder
     * @param curSaa
     * @param condList
     */
    private void checkNextPhaseAward(GetActivityRs.Builder builder, StaticActAward curSaa,
                                     List<StaticActAward> condList) {
        final int nextSortId = curSaa.getSortId() + 1;
        StaticActAward nextAward = condList.stream().filter(saa -> saa.getSortId() == nextSortId)// 下阶段奖励
                .findFirst().orElse(null);
        if (!CheckNull.isNull(nextAward)) {
            builder.addActivityCond(PbHelper.createActivityCondPb(nextAward, 2, 0));
        }
    }

    /**
     * 检查下阶段奖励
     *
     * @param builder
     * @param curSaa
     * @param condList
     */
    private void checkNextPhaseAward(GetOnLineAwardRs.Builder builder, StaticActAward curSaa,
                                     List<StaticActAward> condList) {
        final int nextSortId = curSaa.getSortId() + 1;
        StaticActAward nextAward = condList.stream().filter(saa -> saa.getSortId() == nextSortId)// 下阶段奖励
                .findFirst().orElse(null);
        if (!CheckNull.isNull(nextAward)) {
            builder.addActivityCond(PbHelper.createActivityCondPb(nextAward, 2, 0));
        }
    }

    /**
     * @param actId
     * @param curSaa
     * @param act
     * @return 0未领 1已领 2未解锁
     */
    /*private int caluActCailyPayStatus(int actId, StaticActAward curSaa, Activity act) {
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
    }*/


    /**
     * 领取复活节活动奖励
     *
     * @param roleId 角色id
     * @param type   奖励类型
     * @param keyId  奖励key
     * @return 协议返回
     * @throws MwException 自定义异常
     */
    public GetEasterActAwardRs getEasterActAward(long roleId, int type, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 活动数据
        Activity activity = player.activitys.get(ActivityConst.ACT_EASTER);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_EASTER);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        // 活动id
        int activityId = activity.getActivityId();
        // 活动奖励
        List<StaticEasterAward> easterAwardList = StaticActivityDataMgr.getEasterAwardList(activityId);
        if (CheckNull.isEmpty(easterAwardList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }

        List<List<Integer>> awardList;
        if (type == 1) {
            // 砸蛋
            StaticEasterAward sEasterAward = StaticActivityDataMgr.getEasterAwardListByKey(keyId);
            if (sEasterAward == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId, ", keyId:", keyId);
            }
            Integer awardStatus = activity.getStatusMap().get(keyId);
            if (awardStatus != null && awardStatus != 0) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId, ", keyId:", keyId);
            }
            int status = activityDataManager.currentActivity(player, activity, 0);
            // 还剩多少进度可以使用
            int recharge = sEasterAward.getRecharge();
            if (status < recharge) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
            }
            // 检测前置条件
            int cond = sEasterAward.getParam().get(0);
            if (Stream.iterate(1, i -> ++i).limit(cond - 1).anyMatch(condition -> checkEasterAward(condition, easterAwardList, activity))) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
            }
            // 记录使用的活动进度
            activityDataManager.updActivity(player, ActivityConst.ACT_EASTER, recharge, 1, false);
            // 记录领取进度
            activity.getStatusMap().put(keyId, 1);
            // 砸蛋奖励
            awardList = sEasterAward.getAwardList();
        } else {
            Integer awardStatus = activity.getStatusMap().get(activityId);
            if (awardStatus != null && awardStatus != 0) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId, ", activityId:", activityId);
            }
            // 检测需要领取档位
            if (checkEasterAward(keyId, easterAwardList, activity)) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
            }
            StaticEasterAward staticEA = easterAwardList.stream().filter(sea -> sea.getParam().get(0) == keyId).findAny().orElse(null);
            if (Objects.isNull(staticEA)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId, ", keyId:", keyId);
            }
            // 档位已砸蛋数量
            int actAward = Math.toIntExact(easterAwardList.stream().filter(sea -> sea.getParam().get(0) == keyId && activity.getStatusMap().containsKey(sea.getKeyId())).count());
            if (actAward < staticEA.getProgress()) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
            }
            // 记录领取进度
            activity.getStatusMap().put(keyId, 1);
            // 活动奖励
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
     * 检测砸蛋条件是否达成, 只有青铜砸完, 才能砸白银
     * @param condition 条件
     * @return true
     */
    private boolean checkEasterAward(int condition, List<StaticEasterAward> easterAwards, Activity activity) {
        if (!CheckNull.isEmpty(easterAwards)) {
            easterAwards = easterAwards.stream().filter(sea -> sea.getParam().get(0) == condition).collect(Collectors.toList());
            if (!CheckNull.isEmpty(easterAwards)) {
                // 记录领取进度
                Map<Integer, Integer> statusMap = activity.getStatusMap();
                return !easterAwards.stream().allMatch(sea -> statusMap.containsKey(sea.getKeyId()));
            }
        }
        return false;
    }

    /**
     * 领取奖励
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetActivityAwardRs getActivityAward(GetActivityAwardRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int activityType = req.getActivityType();// 获取活动类型
        int keyId = req.getKeyId();// 获取奖励id

        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);// 通过奖励获取奖励Award
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }

        Activity activity = player.activitys.get(activityType);// 通过活动类型获取活动
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        if(activityType == ActivityConst.ACT_NEWYEAR_2022_FISH){
            throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"非法参数 此活动类型不可领奖",activityType));
        }

        // if (!activityBase.isReceiveAwardTime()) {// 领奖时间的判断
        // throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未在领取奖励时间内, roleId:,", roleId);
        // }

        Integer awardStatus = activity.getStatusMap().get(keyId);
        if (awardStatus != null && awardStatus != 0) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId);
        }

        List<List<Integer>> awardList = actAward.getAwardList();
        // 检测背包是否已满
        rewardDataManager.checkBag(player, awardList);

        int sortId = actAward.getSortId();
        if (activityType == ActivityConst.ACT_VIP || activityType == ActivityConst.ACT_ATTACK_CITY
                || activityType == ActivityConst.ACT_CHALLENGE_COMBAT
                || activityType == ActivityConst.ACT_TRAINED_SOLDIERS
                || activityType == ActivityConst.ACT_TRAINED_SOLDIERS_DAILY
                || activityType == ActivityConst.ACT_EQUIP_MATERIAL
                || activityType == ActivityConst.ACT_ELIMINATE_BANDIT
        ) {// 大咖带队,攻占据点, 挑战战役
            sortId = actAward.getParam().get(0); // vip等级
        }
        if (activityType == ActivityConst.ACT_COLLECT_RESOURCES || activityType == ActivityConst.ACT_RESOUCE_SUB) {
            sortId = actAward.getParam().isEmpty() ? 0 : actAward.getParam().get(0) * 10000 + actAward.getParam().get(1);
        }
        int status = activityDataManager.currentActivity(player, activity, sortId);
        if (activityType == ActivityConst.ACT_CHARGE_CONTINUE
                || activityType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {
            // 连续充值奖励需要拿玩家选择的那天的充值数额，而不能拿当天的
            status = activityDataManager.getChargeContinueStatValueByAward(player, activity, actAward, sortId,
                    TimeHelper.getCurrentSecond());
        } else if (activityType == ActivityConst.ACT_WAR_PLANE_SEARCH) {
            status = activityDataManager.getWarPlaneSearchSchedule(actAward, player, activity);
        } else if (activityType == ActivityConst.ACT_ANNIVERSARY_FIREWORK) {
            status = activityFireWorkService.checkDrawSubActivityAward(player, activity, activityBase, actAward);
        }
        if (status == 0 && activityType != ActivityConst.ACT_DAILY_PAY
                && activityType != ActivityConst.ACT_PROP_PROMOTION) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
        }
        LogUtil.debug("GetActivityAwardRs activity=" + activity);

        // List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        int awardCnt = 1;

        // int serverId = player.account.getServerId();
        // 领奖条件判定(排名类条件值)
        if (activityType == ActivityConst.ACT_GIFT_PAY) {
            if (status < actAward.getCond()) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
            }
            activity.getStatusMap().put(keyId, 1);
        } else if (activityType == ActivityConst.ACT_LOGIN_EVERYDAY) {// 每日登陆活动
            if (!activity.getStatusMap().isEmpty()) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "登陆奖励 已领取奖励, roleId:,", roleId);
            }
            activity.getStatusMap().put(keyId, 1);
        } else if (activityType == ActivityConst.ACT_DAY_DISCOUNTS) {// 每日特惠免费领取奖励
            int freeKey = 0;
            if (activity.getStatusMap().containsKey(freeKey)) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "每日特惠免费 已领取奖励, roleId:,", roleId);
            }
            int lvKey = 1;
            Long saveLv = activity.getStatusCnt().get(lvKey);
            if (saveLv == null) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
            }
            int actLv = saveLv.intValue();
            if (!(actAward.getParam().get(0) <= actLv && actLv <= actAward.getParam().get(1))) {
                throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "每日特惠领取奖励错误, roleId:,", roleId);
            }
            activity.getStatusMap().put(freeKey, 1);
        } else if (activityType == ActivityConst.ACT_THREE_REBATE) { // 三倍返利奖励
            player.activitys.get(ActivityConst.ACT_THREE_REBATE).getStatusMap().put(1, 0);
        }else {
            if (activityType == ActivityConst.ACT_LEVEL) { // 成长基金
                // 是否购买V4
                Long isBuy = activity.getStatusCnt().get(0);
                if (isBuy == null || isBuy == 0) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), " 成长计划未购买 roleId:", roleId);
                }
                if (actAward.getCond() > player.lord.getLevel()) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "成长计划未达到等级 roleId:", roleId);
                }
                activity.getStatusMap().put(keyId, 1);
            } else if (StaticActivityDataMgr.isActTypeRank(activityType)) {// 排行活动
                status = activityDataManager.getRankAwardSchedule(player, activityType);
                if (StaticActivityDataMgr.isOnlyRankAward(activityType)) {
                    // 只能领取对应档次的奖励
                    // StaticActAward myAward = StaticActivityDataMgr.findRankAward(activityType, status);
                    if (actAward.getParam().get(1) > status || status > actAward.getCond()) {
                        throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "不能领取和自己排行不对应的奖励, roleId:,",
                                roleId, ", myRank:", status, ", needRank:", actAward.getCond());
                    }
                    // 判断领奖时间
                    if (!activityBase.isReceiveAwardTime()) {// 领奖时间的判断
                        throw new MwException(GameError.ATK_ACT_TIME.getCode(), "活动未在领取奖励时间内, roleId:,", roleId);
                    }
                }
                // 注意, 只能领取当前的档位, 并且可以在end-award期间内领取奖励
                if (ActivityConst.ACT_PAY_RANK_NEW == activityType
                        || ActivityConst.ACT_PAY_RANK_V_3 == activityType
                        || ActivityConst.ACT_MERGE_PAY_RANK == activityType
                        || ActivityConst.ACT_CONSUME_GOLD_RANK == activityType
                        || ActivityConst.ACT_TUTOR_RANK == activityType) {// 新
                    // 充值排行
                    // 要验证名次区间
                    // 判断领奖时间
                    int open = activityBase.getBaseOpen();
                    if (open != ActivityConst.OPEN_AWARD && open != ActivityConst.OPEN_STEP) {
                        throw new MwException(GameError.ATK_ACT_TIME.getCode(), "活动未在领取奖励时间内, roleId:,", roleId);
                    }
                    if (status < actAward.getParam().get(1)) {// 不能小于起始名次
                        throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "不能领取和自己排行不对应的奖励, roleId:,",
                                roleId, ", myRank:", status, ", startRank:", actAward.getParam().get(1), ", endRank:",
                                actAward.getCond());
                    }
                }
                if (status <= 0 || status > actAward.getCond()) {// 不能大于结束名次
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "排行榜活动未达到名城, roleId:,", roleId,
                            ", myRank:", status, ", needRank:", actAward.getCond());
                }
                activity.getStatusMap().put(keyId, 1);
            } else if (activityType == ActivityConst.ACT_DAILY_PAY) { // 每日充值
                List<StaticActAward> collect = StaticActivityDataMgr.getDailyPayAward(activityBase.getActivityId());
                for (StaticActAward staticActAward : collect) {
                    if (CheckNull.isEmpty(actAward.getParam())) {
                        continue;
                    }
                    if (staticActAward.getParam().get(0) < actAward.getParam().get(0)) {
                        if (!activity.getStatusMap().containsKey(staticActAward.getKeyId())) {
                            throw new MwException(GameError.SUPER_AWARD_NOT_OPEN.getCode(), "上级活动未完成, roleId:,",
                                    roleId);
                        }
                    }
                }
                int currentDay = TimeHelper.getCurrentDay();
                for (Integer day : activity.getStatusMap().values()) {
                    if (day == currentDay && actAward.getParam().get(0) != 0) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "检测活动,活动未开启 roleId:", roleId);
                    }
                }
                int val = actAward.getParam().get(0) == 0 ? 1 : currentDay;
                activity.getStatusMap().put(keyId, val);
            } else if (activityType == ActivityConst.ACT_PROP_PROMOTION) { // 军备促销活动
                // 已领取次数
                int schedule = activity.getSaveMap().containsKey(keyId) ? activity.getSaveMap().get(keyId) : 0;
                if (schedule >= actAward.getCond()) {
                    throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), " 领取积分箱次数已达上限, roleId:", roleId,
                            ", cnt:", schedule);
                }
                // 3个阵营的充值积分
                GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activity.getActivityType());
                if (!CheckNull.isNull(globalActivity)) {
                    long val = globalActivity.getCampValByCamp(player.lord.getCamp()); // 当前阵营的总积分
                    int ceil = (int) Math.ceil(val / ActParamConstant.ACT_PROP_PROMOTION_AWARD_NUM);
                    int cnt = ceil >= actAward.getCond() ? actAward.getCond() : ceil;
                    awardCnt = cnt - schedule; // 领奖次数
                    if (awardCnt < 1) {
                        throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取积分箱次数不足, roleId:", roleId,
                                ", cnt:", awardCnt);
                    }
                    activity.getSaveMap().put(keyId, schedule + awardCnt);
                }
            } else {// 余下活动
                if ((activityType == ActivityConst.ACT_ALL_CHARGE || activityType == ActivityConst.ACT_BRAVEST_ARMY)
                        && player.lord.getLevel() < ActParamConstant.ACT_ALL_CHARGE_LORD_LV) {// 全军返利/勇冠三军的等级判断
                    throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "全军返利玩家等级不够, 活动未完成, roleId:,", roleId);
                }
                if ((activityType == ActivityConst.ACT_VIP)
                        && player.lord.getLevel() < ActParamConstant.ACT_VIP_LORD_LV) {// 精英部队的等级判断
                    throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "全军返利玩家等级不够, 活动未完成, roleId:,", roleId);
                }
                if (actAward.getCond() <= 0 || status < actAward.getCond()) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
                }
                if (activityType == ActivityConst.ACT_PAY_7DAY) {// 7日充值
                    int cRoleDay = playerDataManager.getCreateRoleDay(player, new Date());
                    if (cRoleDay > 7) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
                    }
                }

                if (activityType == ActivityConst.ACT_CHARGE_TOTAL) {// 累计充值
                    if (activityBase.getStep() == ActivityConst.OPEN_CLOSE) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
                    }
                }

                if (activityType == ActivityConst.ACT_CHARGE_CONTINUE
                        || activityType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {// 连续充值
                    if (activityBase.getStep() == ActivityConst.OPEN_CLOSE) {
                        throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
                    }

                    Integer s = activity.getStatusMap().get(actAward.getKeyId());
                    if (s != null && s == 1) {
                        throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "活动已经领取, roleId:,", roleId);
                    }
                    int day = actAward.getParam().get(0);
                    int chargeGold = activity.getSaveMap().get(day) == null ? 0 : activity.getSaveMap().get(day);
                    if (chargeGold < actAward.getCond()) {
                        throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
                    }
                }

                activity.getStatusMap().put(keyId, 1);
            }
        }
        GetActivityAwardRs.Builder builder = GetActivityAwardRs.newBuilder();

        int size = awardList.size();
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        for (int i = 0; i < size; i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// 翻倍活动
            count *= awardCnt;// 奖励次数
            if (type == AwardType.EQUIP) {
                for (int c = 0; c < count; c++) {
                    int itemkey = rewardDataManager.addAward(player, type, itemId, 1, AwardFrom.ACTIVITY_AWARD, keyId,
                            activity.getActivityId(), activity.getActivityType());
                    builder.addAward(PbHelper.createAwardPb(type, itemId, 1, itemkey));
                }
            } else {

                // 首冲 领取 喀秋莎 将领 发送世界消息
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
                        // 记录免费礼包的领取时间
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
     * 检查是否全部领取奖励
     *
     * @param player
     * @param activityBase
     * @param activity
     * @return
     */
    public boolean isAllGainActivity(Player player, ActivityBase activityBase, Activity activity) {
        if (activityBase.getStaticActivity().getIsDisappear() == 0) {// 不消失的活动
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

    // ===============================盖世太保活动相关的
    // start========================================

    /**
     * 兑换活动奖励
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId,"keyId=" + keyId);
        }

        Activity activity = activityDataManager.getActivityInfo(player, actExchange.getType());
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",actExchange.getType());
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    actExchange.getType());
        }
        List<List<Integer>> awardList = actExchange.getAwardList();
        // 检测背包是否已满
        rewardDataManager.checkBag(player, awardList);

        // 判断兑换的是否是将领 并且已拥有
        if (awardList.get(0).get(0) == AwardType.HERO && checkAwardHasHero(awardList.get(0), player)) {
            throw new MwException(GameError.HERO_EXISTS.getCode(), "将领已拥有, roleId:,", roleId, ", heroId:",
                    awardList.get(0).get(1));
        }

        //判断兑换是否是皮肤且已拥有
        if(awardList.get(0).get(0) == AwardType.CASTLE_SKIN && castleSkinService.checkSkinHaving(player,awardList.get(0).get(1))){
            throw new MwException(GameError.EXCHANGE_SKIN_HAVING.getCode(), "兑换皮肤已拥有, roleId:,", roleId, ", activityType=" + activity.getActivityType(), ", activityId=" + activity.getActivityId(), ", skinId=" + awardList.get(0).get(1));
        }

        List<Integer> prop = actExchange.getProp();
        if(ActivityConst.ACT_CHRISTMAS == activity.getActivityType() || ActivityConst.ACT_REPAIR_CASTLE == activity.getActivityType()){

        }else {
            if (CheckNull.isNull(prop)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "活动配置错误, roleId:", roleId, ", type:",
                        activity.getActivityId());
            }

            // 判断是否消耗资源兑换活动
            if (ActivityConst.isExchangePropAct(activity.getActivityType())) {
                if (!rewardDataManager.checkPlayerResourceIsEnough(player, actExchange.getExpendProp())) {
                    throw new MwException(
                            GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(),
                            "兑换奖励道具不足, roleId:", roleId, ", need:",
                            actExchange.getExpendProp()
                    );
                }
            } else {
                int schedule = activityDataManager.currentActivity(player, activity, 0);
                if (schedule < prop.get(2)) {
                    throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(), "兑换奖励道具不足, roleId:", roleId, ", need:",
                            prop.get(2), ", have:", schedule);
                }
            }
        }

        int cnt = activity.getStatusCnt().get(keyId) == null ? 0
                : new Long(activity.getStatusCnt().get(keyId)).intValue();
        if (cnt < 0 || actExchange.getNumberLimit() <= cnt) {
            throw new MwException(GameError.EXCHANGE_AWARD_MAX.getCode(), "兑换奖励次数已达上限, roleId:", roleId, ", max:",
                    actExchange.getNumberLimit(), ", cnt:", cnt);
        }
        if (player.lord.getLevel() < actExchange.getLvLimit()) {
            throw new MwException(GameError.EXCHANGE_AWARD_LEVEL_ERR.getCode(), "兑换奖励等级未达到, roleId:", roleId, ", min:",
                    actExchange.getLvLimit(), ", level:", player.lord.getLevel());
        }
        if(activity.getActivityType() == ActivityConst.ACT_CHRISTMAS || activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE){
            activityChristmasService.checkAndSubScore4Exchange(player,activity,actExchange.getNeedPoint(),AwardFrom.ACT_CHRISTMAS_SCORE_EXCHANGE);
        }else {
            // 扣除对应的道具,并且向客户端同步
            rewardDataManager.checkAndSubPlayerRes(player, actExchange.getExpendProp(),
                    actExchange.getType() == ActivityConst.ACT_ATK_GESTAPO ? AwardFrom.EXCHANGE_GESTAPO_COST
                            : AwardFrom.EXCHANGE_FAMOUS_GENERAL_COST,
                    keyId);
        }
        // rewardDataManager.checkPropIsEnough(player, prop.get(1), prop.get(2),
        // "兑换盖世太保");
        // rewardDataManager.subProp(player, prop.get(1), prop.get(2),
        // AwardFrom.EXCHANGE_GESTAPO_COST, "兑换盖世太保");

        // 设置领取状态
        activity.getStatusMap().put(keyId, 1);
        activity.getStatusCnt().put(keyId, Long.valueOf(cnt + 1));

        ExchangeActAwardRs.Builder builder = ExchangeActAwardRs.newBuilder();

        int size = awardList.size();
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        // 活动类型
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
            count *= num;// 翻倍活动
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
        // 名将转盘 更新红点
        if (activity.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE) {
            activityDataManager.syncActChange(player, activity.getActivityType());
        }
        if (awardList.get(0).get(0).equals(AwardType.HERO)) {
            // 发送跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_FAMOUS_GENERAL_EXCHANGE_GLOBAL_NUM, player.lord.getCamp(), 0,
                    player.lord.getCamp(), player.lord.getNick(), awardList.get(0).get(0), awardList.get(0).get(1),
                    awardList.get(0).get(2), activity.getActivityId());
        }
        if(awardList.get(0).get(0) == AwardType.CASTLE_SKIN){
            chatDataManager.sendSysChat(ChatConst.CHAT_EXCHANGE_SKIN, player.lord.getCamp(), 0,player.lord.getCamp(), player.lord.getNick(), awardList.get(0).get(0), awardList.get(0).get(1),awardList.get(0).get(2), activity.getActivityId());
        }
        if(activity.getActivityType() == ActivityConst.ACT_CHRISTMAS || activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE){
            builder.addParam(PbHelper.createTwoIntPb(activity.getActivityType(),activityChristmasService.getMyScore(activity)));
        }
        return builder.build();
    }

    // ===============================盖世太保活动相关的 end========================================

    // ===============================攻城掠地相关的 start========================================

    /**
     * 获取攻城掠地活动
     *
     * @param roleId
     * @throws MwException
     */
    public GetAtkCityActRs getAtkCityAct(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Date now = new Date();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATTACK_CITY_NEW);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        if (activity.getOpen() == ActivityConst.OPEN_CLOSE) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        ActivityBase actBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATTACK_CITY_NEW);
        if (actBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        AtkCityAct cityAct = player.atkCityAct;
        List<CommonPb.AtkCityActActive> atkCityActActives = new ArrayList<>();
        List<CommonPb.AtkCityActTask> atkCityActTasks = new ArrayList<>();
        int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // 活动开启的第几天
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
     * 领取目标的活跃度
     *
     * @param roleId
     * @throws MwException
     */
    public RecvActiveRs recvActive(Long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Date now = new Date();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ATTACK_CITY_NEW);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        if (activity.getOpen() == ActivityConst.OPEN_CLOSE) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        if (player.lord.getLevel() < ActParamConstant.ACT_ATK_CITY_LEVEL.get(0)) {
            throw new MwException(GameError.TRIGGER_LEVEL_ERR.getCode(), "领取等级不足, roleId:,", roleId);
        }
        ActivityBase actBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ATTACK_CITY_NEW);
        if (actBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:",
                    ActivityConst.ACT_ATTACK_CITY_NEW);
        }
        AtkCityAct cityAct = player.atkCityAct;
        int dayiy = DateHelper.dayiy(actBase.getBeginTime(), now); // 活动开启的第几天
        StaticAtkCityAct staticAtkCityAct = StaticActivityDataMgr.getAtkCityAct(keyId);
        if (CheckNull.isNull(staticAtkCityAct)) {
            throw new MwException(GameError.ATK_CONFIG_NOT_FOUND.getCode(), "活动配置未找到, roleId:,", roleId, ", keyId:",
                    keyId);
        }
        if (dayiy < staticAtkCityAct.getDay()) {
            throw new MwException(GameError.ATK_ACT_TIME.getCode(), "领取时间未到, roleId:,", roleId, ", keyId:", keyId);
        }
        int canRecvCnt = activityDataManager.getCanRecvCnt(player, staticAtkCityAct, cityAct);
        if (canRecvCnt == 0) {
            throw new MwException(GameError.ATK_NOT_AWARD.getCode(), "领取次数不足,目标未达成, roleId:,", roleId, ", keyId:",
                    keyId);
        }
        // 更新活跃度
        activityDataManager.recvActiveCnt(player, cityAct, staticAtkCityAct, canRecvCnt);
        RecvActiveRs.Builder builder = RecvActiveRs.newBuilder();
        int activie = activityDataManager.currentActivity(player, activity, 0);
        builder.setActice(activie);
        builder.setActTask(PbHelper.createAtkCityActTask(cityAct, staticAtkCityAct,
                activityDataManager.getCanRecvCnt(player, staticAtkCityAct, cityAct)));
        return builder.build();
    }

    // ===============================攻城掠地相关的 end========================================

    // ===============================七日数据相关的 start========================================

    /**
     * 获取七日数据
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
        if(staticDay7ActList.isEmpty()){
//            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
            dayiy = 0;
        } else {
            if (dayiy <= 7) {
                for (StaticDay7Act e : staticDay7ActList) {
                    if (e.getDay() > dayiy) {
                        continue;
                    }
                    // 已经领取过奖励的
                    if (day7Act.getRecvAwardIds().contains(e.getKeyId())) {
                        if (e.getTaskType() == ActivityConst.ACT_TASK_CHARGE) {
                            // 累计充值特殊处理, 领取完奖励也获取最新的进度
                            int status = activityDataManager.getDay7ActStatus(player, e);
                            listDay7Act.add(PbHelper.createDay7ActPb(e.getKeyId(), status, ActivityConst.ACT_7_STATUS_HAS_GAIN));
                        } else {
                            listDay7Act.add(PbHelper.createDay7ActPb(e.getKeyId(), e.getCond(),ActivityConst.ACT_7_STATUS_HAS_GAIN));
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
        return builder.build();
    }

    /**
     * 领取7日活动奖励
     */
    public RecvDay7ActAwardRs recvDay7ActAward(Long roleId, int keyId) throws MwException {
        StaticDay7Act staticDay7Act = StaticActivityDataMgr.getAct7DayMap().get(keyId);
        if (staticDay7Act == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }

        Player player = playerDataManager.getPlayer(roleId);
        Day7Act day7Act = player.day7Act;

        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        long time = beginTime.getTime() + (7 + 3) * TimeHelper.DAY_S * 1000;
        if (System.currentTimeMillis() > time) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "传入参数错误, roleId:,", roleId);
        }

        Date now = new Date();
        int dayiy = DateHelper.dayiy(beginTime, now);
        if (staticDay7Act.getDay() > dayiy) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
        }

        if (day7Act.getRecvAwardIds().contains(staticDay7Act.getKeyId())) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId);
        }
        // 检测背包是否已满
        rewardDataManager.checkBag(player, staticDay7Act.getAwardList());

        RecvDay7ActAwardRs.Builder builder = RecvDay7ActAwardRs.newBuilder();

        switch (staticDay7Act.getTaskType()) {
            case 15:// 免费赠送1
                break;
            // case 18://半价限购1
            // if(player.lord.getGold() < staticDay7Act.getParam().get(1)){
            // throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(), "找不到配置, roleId:,", roleId);
            // }
            // builder.addAtom2(playerDataManager.subProp(player, AwardType.GOLD, 0, staticDay7Act.getParam().get(1),
            // AwardFrom.RECV_DAY_7_ACT_AWARD));
            // break;
            default:
                int status = activityDataManager.getDay7ActStatus(player, staticDay7Act);
                if (status < staticDay7Act.getCond()) {
                    throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
                }
                break;
        }

        day7Act.getRecvAwardIds().add(staticDay7Act.getKeyId());
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        for (List<Integer> e : staticDay7Act.getAwardList()) {
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// 翻倍活动
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

    // ===============================七日数据相关的 start========================================

    // ===========================黑鹰计划活动 start===========================

    /**
     * 获取黑鹰计划活动
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetActBlackhawkRs getActBlackhawk(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 检测活动是否截止
        int now = TimeHelper.getCurrentSecond();
        int actEndTime = checkBlackhawkIsOver(player, now);// 活动截止日期
        ActBlackhawk act = player.blackhawkAct;
        // 检测是否首次获取黑鹰计划表
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            // 首次获取
            // 初始化数据
            Map<Integer, ActBlackhawkItem> itemsMap = activityDataManager.blanckhawkRefresh(true);
            if (CheckNull.isEmpty(itemsMap)) {
                // 配置错误
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "黑鹰计划活动配置有错误");
            }
            // 初始化
            act.setRefreshCount(ActParamConstant.BLACKHAWK_FREE_COUNT);
            act.setRecvHero(false);
            act.setRefreshTime(now);
            act.setBlackhawkItemMap(itemsMap);
        } else {
            // 设置刷新时间
            resetBlackCount(now, act);
        }
        GetActBlackhawkRs.Builder builder = GetActBlackhawkRs.newBuilder();
        builder.addAllItems(PbHelper.createBlackhawkItemList(act.getBlackhawkItemMap().values()));
        builder.setRefreshCount(act.getRefreshCount());
        builder.setIsRecvHero(act.isRecvHero());
        builder.setTokenCount(activityDataManager.getBlanckhawkTokenCount(player)); // 拥有信物个数
        if (act.getRefreshCount() < ActParamConstant.BLACKHAWK_FREE_COUNT) {
            builder.setRefreshEndTime(act.getRefreshTime() + ActParamConstant.BLACKHAWK_REFRESH_INTERVAL);// 最近一次刷新重置结束时间
        }
        builder.setRefreshGold( // 当前付费刷新的费用
                ActParamConstant.BLACKHAWK_INIT_PAY_GOLD
                        + (act.getPayRefreshCount() * ActParamConstant.BLACKHAWK_INCR_GOLD));
        builder.setActEndTime(actEndTime);
        return builder.build();
    }

    /**
     * 重新设置时间
     *
     * @param now
     * @param act
     */
    private void resetBlackCount(int now, ActBlackhawk act) {
        // 检测刷新次数,并重新赋值
        // 次数小于最大次数才进行检测
        if (act.getRefreshCount() < ActParamConstant.BLACKHAWK_FREE_COUNT) {
            int limit = now - act.getRefreshTime();
            int count = limit / ActParamConstant.BLACKHAWK_REFRESH_INTERVAL;
            if (count > 0) {
                // 加起来超过最大,按照最大值算
                if (act.getRefreshCount() + count >= ActParamConstant.BLACKHAWK_FREE_COUNT) {
                    count = ActParamConstant.BLACKHAWK_FREE_COUNT - act.getRefreshCount();
                }
                act.setRefreshCount(act.getRefreshCount() + count);// 更新刷新次数
                act.setRefreshTime(act.getRefreshTime() + (count * ActParamConstant.BLACKHAWK_REFRESH_INTERVAL));// 更新刷新时间
            }
        } else {
            // 时间
            act.setRefreshTime(now);
        }

    }

    /**
     * 检测黑鹰计划活动是否结束
     *
     * @param player
     * @throws MwException
     */
    public int checkBlackhawkIsOver(Player player, int now) throws MwException {
        // int createRoleTime = (int) (player.account.getCreateDate().getTime() / 1000); // 创建角色时间
        final int sec4Day = ActParamConstant.ACT_BLACK_TIME; // 4天的秒数 60 * 60 * 24 * 4
        // int actEndTime = createRoleTime + sec4Day;
        // 活动截止日期
        int actEndTime = TimeHelper.afterSecondTime(player.account.getCreateDate(), sec4Day);
        if (now > actEndTime) {
            // 活动已结束
            throw new MwException(GameError.ACTIVITY_IS_OVER.getCode(), "黑鹰计划活动已结束, roleId:", player.roleId, ", actEndTime:", actEndTime, ", now:", now);
        }
        return actEndTime;
    }

    /**
     * 黑鹰计划刷新
     *
     * @param roleId
     * @param isPay  是否付费刷新,true为付费
     * @return
     * @throws MwException
     */
    public BlackhawkRefreshRs blackhawkRefresh(long roleId, boolean isPay) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        // 检测活动是否截止
        checkBlackhawkIsOver(player, now);
        ActBlackhawk act = player.blackhawkAct;
        // 检测是否初始化过物品
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "黑鹰计划活动没有初始化 roleId:", roleId);
        }

        if (isPay) {
            int needGold = ActParamConstant.BLACKHAWK_INIT_PAY_GOLD
                    + (act.getPayRefreshCount() * ActParamConstant.BLACKHAWK_INCR_GOLD);
            if (needGold > 108) {
                throw new MwException(GameError.REFRESH_CNT_IS_OVER.getCode(), "刷新次数已达到上限", roleId, "count:",
                        act.getPayRefreshCount(), ", needGold:", needGold);
            }
            // 扣除金币
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.BLACKHAWK_REFRESH);
            // 付费次数+1
            act.setPayRefreshCount(act.getPayRefreshCount() + 1);
        } else {
            if (act.getRefreshCount() <= 0) {
                if (now - act.getRefreshTime() < ActParamConstant.BLACKHAWK_REFRESH_INTERVAL) {
                    // CD时间也未到
                    throw new MwException(GameError.ACT_BLACKHAWK_REFRESH_NOT_ENOUGH.getCode(), "黑鹰计划刷新次数不够 roleId:",
                            roleId);
                }
            }
            // 重置时间达到,重新计算次数
            resetBlackCount(now, act);
            // 时间
            if (act.getRefreshCount() >= ActParamConstant.BLACKHAWK_FREE_COUNT) {
                act.setRefreshTime(now);
            }
            // 次数-1
            act.setRefreshCount(act.getRefreshCount() - 1);
        }

        // 刷新格子操作
        boolean tokenCanBuy = act.isRecvHero()// 已经招募过,或者信物数量达到,都不允许购买信物
                || activityDataManager.getBlanckhawkTokenCount(player) >= ActParamConstant.BLACKHAWK_NEED_TOKEN ? false
                : true;
        Map<Integer, ActBlackhawkItem> itemsMap = activityDataManager.blanckhawkRefresh(tokenCanBuy);
        act.setBlackhawkItemMap(itemsMap);
        BlackhawkRefreshRs.Builder builder = BlackhawkRefreshRs.newBuilder();
        builder.addAllItems(PbHelper.createBlackhawkItemList(act.getBlackhawkItemMap().values()));
        if (act.getRefreshCount() < ActParamConstant.BLACKHAWK_FREE_COUNT) {
            builder.setRefreshEndTime(act.getRefreshTime() + ActParamConstant.BLACKHAWK_REFRESH_INTERVAL);// 最近一次刷新重置结束时间
        }
        builder.setRefreshCount(act.getRefreshCount());
        builder.setRefreshGold( // 当前付费刷新的费用
                ActParamConstant.BLACKHAWK_INIT_PAY_GOLD
                        + (act.getPayRefreshCount() * ActParamConstant.BLACKHAWK_INCR_GOLD));
        return builder.build();
    }

    /**
     * 购买黑鹰计划物品
     *
     * @param roleId
     * @param keyId  商品活动的id
     * @return
     * @throws MwException
     */
    public BlackhawkBuyRs blackhawkBuy(long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        // 检测活动是否截止
        checkBlackhawkIsOver(player, now);
        ActBlackhawk act = player.blackhawkAct;
        // 检测是否初始化过物品
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            throw new MwException(GameError.ACQUISITE_NO_OPEN.getCode(), "黑鹰计划活动没有初始化");
        }
        ActBlackhawkItem buyItem = act.getBlackhawkItemMap().get(keyId);
        // 检测该商品是否已购买
        if (buyItem.isPurchased()) {
            throw new MwException(GameError.ACT_BLACKHAWK_HAS_BUY.getCode(), "黑鹰计划活该商品已经购买过");
        }
        // 检测并扣除金币
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                buyItem.getDiscountPrice(), AwardFrom.BLACKHAWK_PROP_BUY);
        // 获得物品
        Award award = rewardDataManager.addAwardSignle(player, buyItem.getAward(), AwardFrom.BLACKHAWK_PROP_BUY);
        if (CheckNull.isNull(award)) {
            throw new MwException(GameError.ACT_BLACKHAWK_BUY_ERR.getCode(), " 黑鹰计划商品已购买失败 roleId:", roleId, ", keyId:",
                    keyId);
        }
        // 设置已购买
        buyItem.setPurchased(true);
        // 对其他商品进行打折
        for (ActBlackhawkItem it : act.getBlackhawkItemMap().values()) {
            int price = it.getPrice(); // 原价
            int discount = it.getDiscount() - 10;// 折扣
            discount = discount <= 0 ? 0 : discount;
            int discountPrice = (int) ((discount * 1.0f) / 100 * (price * 1.0f));
            it.setDiscountPrice(discountPrice);
            it.setDiscount(discount);
        }
        BlackhawkBuyRs.Builder builder = BlackhawkBuyRs.newBuilder();
        builder.setAward(award);
        builder.addAllItems(PbHelper.createBlackhawkItemList(act.getBlackhawkItemMap().values()));
        builder.setTokenCount(activityDataManager.getBlanckhawkTokenCount(player)); // 拥有信物个数
        return builder.build();
    }

    /**
     * 黑鹰计划招募将领
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public BlackhawkHeroRs blackhawkHero(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        // 检测活动是否截止
        checkBlackhawkIsOver(player, now);
        ActBlackhawk act = player.blackhawkAct;
        // 检测是否初始化过物品
        if (CheckNull.isEmpty(act.getBlackhawkItemMap())) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "黑鹰计划活动没有初始化 roleId:", roleId);
        }
        // 检测是否兑换过
        if (act.isRecvHero()) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "黑鹰计划活动将领已经兑换过");
        }
        ActBlackhawkItem blackhawkItem = act.getBlackhawkItemMap().get(ActParamConstant.BLACKHAWK_TOKEN_KEYID);
        List<Integer> award = blackhawkItem.getAward();
        // 检测道具是否足够,减道具
        rewardDataManager.checkAndSubPlayerResHasSync(player, award.get(0), award.get(1),
                ActParamConstant.BLACKHAWK_NEED_TOKEN, AwardFrom.ACT_BLACKHAWK_ADD_HERO);
        // 招募将领
        rewardDataManager.addAwardSignle(player, AwardType.HERO, ActParamConstant.BLACKHAWK_HERO_ID, 1,
                AwardFrom.ACT_BLACKHAWK_ADD_HERO);
        act.setRecvHero(true);// 设置黑鹰计划已招募将领
        act.getBlackhawkItemMap().get(ActParamConstant.BLACKHAWK_TOKEN_KEYID).setPurchased(true);// 设置信物已购买
        BlackhawkHeroRs.Builder builder = BlackhawkHeroRs.newBuilder();
        builder.setIsRecvHero(true);
        builder.setAward(PbHelper.createAwardPb(AwardType.HERO, ActParamConstant.BLACKHAWK_HERO_ID, 1));
        return builder.build();
    }

    // ===========================黑鹰计划活动 end===========================

    /**
     * 获取排行榜数据
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
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "获取排行榜错误，活动未开启(ActivityBase=null), roleId:", roleId, "activityType=" + activityType);
            }
            int actId = activityBase.getActivityId();
            // int step = activityBase.getStep();
            // if (step != ActivityConst.OPEN_STEP) {
            // throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "排行活动为开启 roleId:", roleId);
            // }

            Activity activity = activityDataManager.getActivityInfo(player, activityType);
            if (activity == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "获取排行榜错误, 活动未开启(Player.Activity=null) roleId:", roleId, "activityType=" + activityType);
            }

            GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
            if (gActDate == null) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "排行活动未开启 roleId:", roleId);
            }
            List<StaticActAward> sActAward ;
            if(activityType == ActivityConst.ACT_NEWYEAR_2022_FISH){
                sActAward = StaticActivityDataMgr.getActAwardById(actId);
            }else {
                sActAward = StaticActivityDataMgr.getRankActAwardByActId(actId);
            }
            if (CheckNull.isEmpty(sActAward)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "排行活动未开启配置错误 roleId:", roleId, ", actId:", actId,
                        ", actType:", activityType);
            }

            final int showSize = sActAward.size() + 1; // 显示的个数,总档位个数+1
            LinkedList<ActRank> rankList = gActDate.getPlayerRanks(player, activityType);
            final int rankSize = rankList.size(); // 排行榜的长度
            ActRank myRank = gActDate.getPlayerRank(player, activityType, roleId);// 自己的排行, null说明压根没有排行
            StaticActAward myAward = null;// 自己所在的奖励档位, 如果为null说明在排行榜之外
            // 计算自己在第几档
            if (myRank != null) {
                myAward = StaticActivityDataMgr.findRankAward(actId, myRank.getRank());
            }

            List<ActRank> showRankList = new ArrayList<>();// 客户端显示的排行
            if (rankSize <= showSize) {
                // 1.当排行榜人数小于显示个数时
                for (int i = 0; i < rankList.size(); i++) {
                    ActRank ar = rankList.get(i);
                    ar.setRank(i + 1);
                    showRankList.add(ar);
                }
                if (myRank == null) { // 没有档位说明压根没有加入排行
                    if (rankSize >= showSize) {// 移除最后一个
                        showRankList.remove(rankSize - 1);
                    }
                    showRankList.add(new ActRank(roleId, activityType,
                            activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0));// 添加自己
                }
            } else {
                // 2. 排行榜人数足够显示时
                // 实际最多能显示的档位
                List<StaticActAward> sRealActAward = sActAward.stream().filter(saa -> rankSize >= saa.getParam().get(1))
                        .collect(Collectors.toList());
                Set<Integer> rankingSet = new HashSet<>(); // 去重使用
                if (myAward != null) {
                    // 3 当自己有档位时在排行榜中
                    for (StaticActAward saa : sRealActAward) {
                        if (saa.getParam().get(0) > myAward.getParam().get(0)) { // 在自己之前的档位显示档位的显示第一名
                            rankingSet.add(saa.getParam().get(1));
                        } else if (saa.getParam().get(0) < myAward.getParam().get(0)) {// 在自己之后的档位显示档位的显示最后一名
                            rankingSet.add(saa.getCond() > rankSize ? rankSize : saa.getCond());
                        } else {
                            rankingSet.add(myRank.getRank());// 与自己档位相等显示自己名次
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
                    // 4 自己没有档位不再旁行榜
                    for (StaticActAward saa : sRealActAward) {
                        rankingSet.add(saa.getCond() > rankSize ? rankSize : saa.getCond());
                    }
                    for (int i = 1; rankingSet.size() < showSize - 1; i++) { // showSize-1要改自己留个位置
                        rankingSet.add(i);
                    }
                    rankingSet.stream().sorted(Comparator.comparingInt(i -> i)).forEach(rank -> {
                        ActRank actRank = rankList.get(rank - 1);
                        actRank.setRank(rank);
                        showRankList.add(actRank);
                    });
                    showRankList.add(myRank != null ? myRank
                            : new ActRank(roleId, activityType,
                            activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0)); // 无档位在最后,无档位当可能有名次
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
            // 奖励
            int myRankSchedule = activityDataManager.getRankAwardSchedule(player, activityType);
            for (StaticActAward e : sActAward) {
                int keyId = e.getKeyId();
                int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
                builder.addActivityCond(PbHelper.createActivityCondPb(e, status, myRankSchedule));
            }
            int awardTime = (int) (activityBase.getAwardBeginTime().getTime() / 1000);
            builder.setAwardTime(awardTime);
            // 额外参数
            if (activityType == ActivityConst.ACT_CAMP_FIGHT_RANK) {
                builder.addExtParam(PbHelper.createIntLongPc(1, gActDate.getTopupa().get()));
                builder.addExtParam(PbHelper.createIntLongPc(2, gActDate.getTopupb().get()));
                builder.addExtParam(PbHelper.createIntLongPc(3, gActDate.getTopupc().get()));
            }
            return builder.build();
        }
    }

    // ===========================幸运转盘活动 start===========================

//    public void refreshData4AcrossDay(Player player,int activityType){
//        Activity activity = activityDataManager.getActivityInfo(player,activityType);
//        if(Objects.nonNull(activity)){
//            if(activityType == ActivityConst.ACT_MERGE_PROP_PROMOTION){//合服活动每日特惠，清除购买次数
//                activity.getStatusMap().clear();
//            }
//        }
//    }

    /**
     * 转点根据vip刷新免费次数
     *
     * @param player
     * @param activityType
     */
    public void refreshTurnplateCnt(Player player, int activityType) {
        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) { // 活动结束
            return;
        }
        turnplat.refreshFreeCnt(player);
    }

    /**
     * 获取幸运/名将 转盘活动
     *
     * @param roleId
     * @throws MwException
     */
    public GetActTurnplatRs getActTurnplat(long roleId, int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 活动类型校验
        if (activityType != ActivityConst.ACT_LUCKY_TURNPLATE
                && activityType != ActivityConst.FAMOUS_GENERAL_TURNPLATE
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                && activityType != ActivityConst.ACT_SEASON_TURNPLATE) {
            throw new MwException(GameError.ACT_TYPE_ERROR.getCode(), " 活动类型错误 activityType:", activityType);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启,或未初始化 roleId:", roleId);
        }

        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        List<StaticTurnplateConf> turnplateConfs = StaticActivityDataMgr
                .getActTurnPlateListByActId(turnplat.getActivityId());
        if (CheckNull.isEmpty(turnplateConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 幸运/名将 转盘找不到配置, roleId:", roleId);
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
        // 新幸运转盘
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
            // 累计抽取次数
            builder.setCnt(turnplat.getCnt());
            builder.setTodayCnt(conf.getDailyLimited() - turnplat.getTodayCnt());
        }
        return builder.build();
    }

    /**
     * 幸运转盘抽奖
     *
     * @param id       turnplateId
     * @param costType 抽奖消耗，1 免费次数，2 金币
     */
    public LuckyTurnplateRs luckyTurnplate(long roleId, int id, int costType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticTurnplateConf turnplateConf = StaticActivityDataMgr.getActTurnPlateById(id);
        if (CheckNull.isNull(turnplateConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 幸运/名将 转盘找不到配置, roleId:", roleId);
        }

        int activityType = turnplateConf.getType();

        // 活动类型校验
        if (activityType != ActivityConst.ACT_LUCKY_TURNPLATE
                && activityType != ActivityConst.FAMOUS_GENERAL_TURNPLATE
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                && activityType != ActivityConst.ACT_SEASON_TURNPLATE
                && activityType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
            throw new MwException(GameError.ACT_TYPE_ERROR.getCode(), " 活动类型错误 activityType:", activityType);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启,或未初始化 roleId:", roleId);
        }

        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        if(activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR){
            if(ActivityConst.LUCKY_TURNPLATE_GOLD == costType && turnplat.getTodayCnt() + turnplateConf.getCount() > turnplateConf.getDailyLimited()){
                throw new MwException(GameError.ACT_TURNPLAT_NEW_YEAR_TODAY_LIMIT.getCode(), " 新年转盘, 今日达到次数限制, roleId:,", roleId, ", type:",
                        activityType);
            }
        }

        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activityType);
        if (CheckNull.isNull(globalActivity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        boolean initWinCnt = false;

        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType) {// 免费抽奖
            if (turnplat.getRefreshCount() <= 0) {
                throw new MwException(GameError.ACT_LUCKY_TURNPLATE_MAX_COUNT.getCode(), " 幸运/名将 转盘免费抽奖, 次数不足 roleId:",
                        roleId, ", cnt:", turnplat.getRefreshCount());
            }
            turnplat.subRefreshCount();
            LogLordHelper.commonLog("freeLuckyTurnplate", activityType == ActivityConst.ACT_LUCKY_TURNPLATE
                    ? AwardFrom.LUCKY_TURNPLATE_FREE : AwardFrom.FAMOUS_GENERAL_TURNPLATE_FREE, player);
            if(activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR){
                initWinCnt = true;
            }
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {// 金币抽奖
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, turnplateConf.getPrice(),
                    activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? "幸运转盘抽奖" : "名将转盘抽奖");
            rewardDataManager.subGold(player, turnplateConf.getPrice(),
                    activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_GOLD
                            : AwardFrom.FAMOUS_GENERAL_TURNPLATE_GOLD, turnplat.getActivityId(), turnplat.getActivityType());
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
//            if (turnplat.getGoldCnt() == 0) {
//                doSearchWinCnt(turnplat, turnplateConf);
//            }
            initWinCnt = true;
        } else if (ActivityConst.LUCKY_TURNPLATE_PROP == costType) {// 道具抽奖
            rewardDataManager.checkAndSubPlayerRes(player, turnplateConf.getSubstitute(), activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_PROP
                    : AwardFrom.FAMOUS_GENERAL_TURNPLATE_PROP, turnplat.getActivityId(), turnplat.getActivityType());
            if(activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR){
                initWinCnt = true;
            }
        }

        if(initWinCnt && turnplat.getGoldCnt() == 0){
            doSearchWinCnt(turnplat, turnplateConf);
        }

        LuckyTurnplateRs.Builder builder = LuckyTurnplateRs.newBuilder();

        builder.setFreeCount(turnplat.getRefreshCount());
        builder.setGold(player.lord.getGold());
        // 转盘奖励
        int integral = 0;
        List<List<Integer>> awards = new ArrayList<>();
        for (int i = 0; i < turnplateConf.getCount(); i++) {
            if(activityType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR){
                turnplat.setGoldCnt(turnplat.getGoldCnt() + 1);
                if(ActivityConst.LUCKY_TURNPLATE_GOLD == costType){
                    turnplat.setTodayCnt(turnplat.getTodayCnt() + 1);
                }
            }else {
                if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
                    turnplat.setGoldCnt(turnplat.getGoldCnt() + 1);
                }
            }
            // 记录累计抽取次数
            turnplat.setCnt(turnplat.getCnt() + 1);
            awards.add(doSweepstakes(costType, turnplat, turnplateConf, player, integral));
        }
        // 荣耀转盘：判断是否第一次就抽中两个将领
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

        // 积分奖励
        integral = turnplateConf.getPoint() > 0 ? turnplateConf.getPoint() : integral;
        if (integral > 0) {
            activityDataManager.updRankActivity(player, activityType, integral); // 更新积分奖励
        }

        // 金币抽奖奖励
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

        // 拥有的特殊道具
        builder.addAllSpecial(getPlayerSpecial(player, turnplateConf));

        // 拥有的特殊道具碎片次数
        builder.setSpecialCnt(turnplat.currentSpecialCnt());

        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);

        LogUtil.debug("特殊道具的次数节点, roleId:", roleId, ", winCntPoint:", LogUtil.getSetValStr(turnplat.getWinCnt()),
                ", goldCnt:", turnplat.getGoldCnt(), ", 拥有的碎片数量:", turnplat.currentSpecialCnt());

        // 检测碎片是否足够,并兑换道具
        if (activityType == ActivityConst.ACT_LUCKY_TURNPLATE) {// 幸运转盘
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
     * 领取转盘的次数奖励
     *
     * @param roleId 玩家id
     * @param req    请求参数
     * @return
     * @throws MwException
     */
    public TurnplatCntAwardRs turnplatCntAward(long roleId, TurnplatCntAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int actType = req.getActType();
        int keyId = req.getKeyId();

        StaticTurnplateExtra sTurnPlateExtra = StaticActivityDataMgr.getActTurnPlateExtraById(keyId);
        if (CheckNull.isNull(sTurnPlateExtra)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 幸运/名将 转盘找不到配置, roleId:", roleId);
        }

        // 活动类型校验
        if (actType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                && actType != ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                && actType != ActivityConst.ACT_SEASON_TURNPLATE) {
            throw new MwException(GameError.ACT_TYPE_ERROR.getCode(), " 活动类型错误 activityType:", actType);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启,或未初始化 roleId:", roleId);
        }

        ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, actType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启, roleId:,", roleId, ", type:",
                    actType);
        }
        // 累计次数
        int cnt = turnplat.getCnt();
        if (cnt < sTurnPlateExtra.getTimes()) {
            throw new MwException(GameError.TURNPLATE_CNT_AWARD_ERROR.getCode(), "领取转盘次数奖励错误, 次数没达到, roleId:", roleId, ", cnt:", cnt, ", needCnt:", sTurnPlateExtra.getTimes());
        }
        int status = turnplat.getStatusMap().getOrDefault(keyId, 0);
        if (status != 0) {
            throw new MwException(GameError.TURNPLATE_CNT_AWARD_ERROR.getCode(), "领取转盘次数奖励错误, 已结领取过了, roleId:", roleId, ", status:", status);
        }

        TurnplatCntAwardRs.Builder builder = TurnplatCntAwardRs.newBuilder();
        List<List<Integer>> awardList = sTurnPlateExtra.getAwardList();
        if (!CheckNull.isEmpty(awardList)) {
            for (List<Integer> award : awardList) {
                builder.addAward(rewardDataManager.addAwardSignle(player, award, AwardFrom.TURNPLATE_CNT_AWARD));
            }
        }

        // 设置奖励为已领取状态
        turnplat.getStatusMap().put(keyId, 1);

        return builder.build();
    }


    // 名将转盘：如果第一次直接抽取两个将领，将一个转换成碎片
    private void twoGeneralChange(List<List<Integer>> awards) {
        ArrayList<Integer> indexList = new ArrayList<>();// 记录索引
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
                listChange.add(Constant.FAMOUS_GENERAL_EXCHANGE_PROP.get(0).get(0));
                listChange.add(Constant.FAMOUS_GENERAL_EXCHANGE_PROP.get(0).get(1));
                listChange.add(6);// 劝过策划做配置 不过策划执意说写死就好
                awards.set(index, listChange);
            }
        }
    }

    /**
     * 检测碎片是否足够,并兑换道具
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
                    rewardDataManager.checkPropIsEnough(player, propId, staticProp.getChip(), "道具合成");
                    rewardDataManager.subProp(player, propId, staticProp.getChip(), AwardFrom.SYNTHETIC_PROP);
                } catch (Exception e) {
                    LogUtil.error("碎片合成失败", e);
                    return;
                }
                // 兑换道具
                for (List<Integer> award : staticProp.getRewardList()) {
                    rewardDataManager.addAward(player, award.get(0), award.get(1), award.get(2),
                            AwardFrom.SYNTHETIC_PROP_AWARD);
                    builder.addChipAward(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2)));
                }
            }
        }
    }

    /**
     * 拥有的特殊道具奖励
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
     * 单次抽奖的逻辑
     *
     * @param costType
     * @param turnplat
     * @param player
     * @param integral 幸运转盘配置中是否有积分配置
     */
    private List<Integer> doSweepstakes(int costType, ActTurnplat turnplat, StaticTurnplateConf conf, Player player,
                                        int integral) {
        boolean falg = false;// 是否发生跑马灯
        int goldCnt = turnplat.getGoldCnt();
        Set<Integer> winCnt = turnplat.getWinCnt();// 抽中特殊奖励的节点
        List<Set<Integer>> winCnt211 = turnplat.getWinCnt211();//新春转盘的特殊奖励节点
        List<Integer> awardList = new ArrayList<>();
        if(conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {//新春转盘
            int awardIdx = turnplat.getSetIdxByCnt(goldCnt);
            if(awardIdx != -1){
                awardList = conf.getProbAward().get(awardIdx);
                getTurnplatePoint(integral, awardList, 3);

                falg = true;// 抽中特殊道具 发送跑马灯
            }else {
                // 根据权重获取奖励
                awardList = doSweepstakesAwards(conf, player, turnplat);
                getTurnplatePoint(integral, awardList, 4);
            }
        }else {
            if (ActivityConst.LUCKY_TURNPLATE_FREE == costType || ActivityConst.LUCKY_TURNPLATE_PROP == costType) { // 免费抽奖
                // 根据权重获取奖励
                awardList = doSweepstakesAwards(conf, player, turnplat);
                getTurnplatePoint(integral, awardList, 4);
            }else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) { // 金币抽奖
                if(conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR){//新春转盘

                }else {
                    if (winCnt.contains(goldCnt)) { // 达到特殊奖励条件
                        awardList = !CheckNull.isEmpty(conf.getOnlyAward()) ? conf.getOnlyAward().get(0) : new ArrayList<>();
                        getTurnplatePoint(integral, awardList, 3);
                        activityDataManager.updActivity(player, conf.getType(), 1, ActTurnplat.SPECIAL_SORT, false); // 特殊道具在活动期间获取次数

                        falg = true;// 抽中特殊道具 发送跑马灯
                    } else { // 普通金币抽奖
                        // 根据权重获取奖励
                        awardList = doSweepstakesAwards(conf, player, turnplat);
                        getTurnplatePoint(integral, awardList, 4);
                    }
                }
            }
        }

        // 如果抽到的奖励是将领 且玩家已有该将领 则奖励给6张劵
        if (awardList.size() > 1 && awardList.get(0) == AwardType.HERO) {
            List<Integer> list = new ArrayList<Integer>();
            if (checkAwardHasHero(awardList, player)) {
                if (!CheckNull.isEmpty(Constant.FAMOUS_GENERAL_EXCHANGE_PROP)) {
                    for (List<Integer> exchangeProp : Constant.FAMOUS_GENERAL_EXCHANGE_PROP) {
                        if (exchangeProp.get(3) == turnplat.getActivityId()) {
                            list.add(exchangeProp.get(0));
                            list.add(exchangeProp.get(1));
                            list.add(exchangeProp.get(2));
                            // list.add(6);// 劝过策划做配置 不过策划执意说写死就好
                        }
                    }
                } else {// 配置异常，劝过策划做配置 不过策划执意说写死就好
                    list.add(4);
                    list.add(3001);
                    list.add(6);
                }
                awardList = list;
            }
        }

        // 判断抽中的奖励是否需要发生跑马灯
        if (falg || checkAwardChat(awardList)) {
            // 发送跑马灯
            chatDataManager.sendSysChat(
                    conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE ? ChatConst.CHAT_LUCKY_TURNPLATE_GLOBAL_NUM
                            : ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM,
                    player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), awardList.get(0),
                    awardList.get(1), awardList.get(2), turnplat.getActivityId());
            //活动消息推送
            int chatId;
            if(conf.getType() == ActivityConst.ACT_LUCKY_TURNPLATE){
                chatId = ChatConst.CHAT_LUCKY_TURNPLATE_GLOBAL_NUM;
            }else {
                chatId = ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM;
            }
            chatDataManager.sendActivityChat(chatId, conf.getType(), 0,
                    player.lord.getCamp(), player.lord.getNick(), awardList.get(0), awardList.get(1), awardList.get(2), turnplat.getActivityId());
        }
        return awardList;
    }

    private boolean checkCntInBetween(int cnt,StaticTurnplateConf conf){
        for (List<Integer> list : conf.getProbList()) {
            if(cnt >= list.get(0) && cnt <= list.get(1)){
                return true;
            }
        }
        return false;
    }

    /**
     * @param awardList
     * @return boolean
     * @Title: checkAwardChat
     * @Description: 校验获得的奖励是否是已经拥有的英雄
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
     * @Description: 校验获得的奖励是否需要发送跑马灯
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
     * 获取指定奖励位置上的积分
     *
     * @param integral
     * @param awardList
     * @param integralIndex
     */
    private void getTurnplatePoint(int integral, List<Integer> awardList, int integralIndex) {
        if (awardList.size() >= integralIndex + 1) {
            integral += awardList.get(integralIndex);
        }
    }

    /**
     * 根据权重获取奖励
     *
     * @param conf
     * @param player
     * @param turnplat
     */
    private List<Integer> doSweepstakesAwards(StaticTurnplateConf conf, Player player, ActTurnplat turnplat) {
        List<Integer> awardList = null;
        boolean falg = true;
        while (falg) {
            // 金币普通抽奖
            awardList = RandomUtil.getRandomByWeight(conf.getAwardList(), 3, false);
            if (checkSpecialOnlyAward(awardList, turnplat, conf)) { // 唯一道具
                if (!player.checkHaveSpecial(awardList.get(0), awardList.get(1))) { // 如果未拥有
                    player.upSpecialProp(awardList.get(0), awardList.get(1));
                    falg = false;
                }
            } else { // 普通道具
                falg = false;
            }
        }
        return awardList;
    }

    /**
     * 抽中的是否是幸运转盘的唯一道具
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
     * 计算获取特殊道具的次数节点
     *
     * @param turnplat
     * @param conf
     */
    private void doSearchWinCnt(ActTurnplat turnplat, StaticTurnplateConf conf) {
        // 清除之前记录的次数节点
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
        }catch (Exception e) {
            LogUtil.error("计算获取特殊道具的次数节点，初始节点发生异常, ",e);
        }
    }

    private void checkAndRandomWinCnt(List<Set<Integer>> winCnts,List<Integer> tmpList,int awardIdx){
        Set<Integer> all = new HashSet<>();
        winCnts.forEach(set -> all.addAll(set));
        int n = tmpList.get(2);
        if(n <= 0){
            return;
        }
        int i = 0;
        while(true){
            int cnt = RandomUtil.randomIntIncludeEnd(tmpList.get(0),tmpList.get(1));
            if(all.add(cnt)){
                winCnts.get(awardIdx).add(cnt);
                i ++;
            }
            if(i>=n){
                break;
            }
        }
    }

    // ===========================幸运转盘活动 end===========================

    // ===========================装备转盘活动 start===========================

    /**
     * @param player
     * @return void
     * @Title: refreshEquipTurnplateCnt
     * @Description: 转点根据vip刷新免费次数
     */
    public void refreshEquipTurnplateCnt(Player player) {
        int activityType = ActivityConst.ACT_EQUIP_TURNPLATE;
        EquipTurnplat turnplat = (EquipTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) { // 活动结束
            return;
        }
        turnplat.refreshFreeCnt(player);
    }

    /**
     * @param roleId
     * @return GetActTurnplatRs
     * @throws MwException
     * @Title: getActTurnplat
     * @Description: 获取装备转盘活动
     */
    public GetEquipTurnplatRs getEquipTurnplat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int activityType = ActivityConst.ACT_EQUIP_TURNPLATE;

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 装备转盘活动未开启,或未初始化 roleId:", roleId);
        }

        EquipTurnplat turnplat = (EquipTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 装备转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        List<StaticEquipTurnplateConf> turnplateConfs = StaticActivityDataMgr
                .getEquipTurnPlateListByActId(turnplat.getActivityId());
        if (CheckNull.isEmpty(turnplateConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 装备转盘找不到配置, roleId:", roleId);
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
     * @Description: 装备转盘抽奖
     */
    public EquipTurnplateRs equipTurnplate(long roleId, int id, int costType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int activityType = ActivityConst.ACT_EQUIP_TURNPLATE;

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 装备转盘活动未开启,或未初始化 roleId:", roleId);
        }

        EquipTurnplat turnplat = (EquipTurnplat) activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(turnplat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 装备转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        StaticEquipTurnplateConf turnplateConf = StaticActivityDataMgr.getEquipTurnPlateById(id);
        if (CheckNull.isNull(turnplateConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 装备转盘找不到配置, roleId:", roleId);
        }

        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(activityType);
        if (CheckNull.isNull(globalActivity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 装备转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType) {// 免费抽奖
            if (turnplat.getRefreshCount() <= 0) {
                throw new MwException(GameError.ACT_EQUIP_TURNPLATE_MAX_COUNT.getCode(), " 装备转盘免费抽奖, 次数不足 roleId:",
                        roleId, ", cnt:", turnplat.getRefreshCount());
            }
            turnplat.subRefreshCount();
            LogLordHelper.commonLog("freeEquipTurnplate", AwardFrom.EQUIP_TURNPLATE_FREE, player);
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {// 金币抽奖
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, turnplateConf.getPrice(), "装备转盘抽奖");
            rewardDataManager.subGold(player, turnplateConf.getPrice(), AwardFrom.EQUIP_TURNPLATE_GOLD, turnplat.getActivityId(), turnplat.getActivityType());
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
            if (turnplat.getGoldCnt() == 0) {
                equipDoSearchWinCnt(turnplat, turnplateConf);
            }
        } else if (ActivityConst.LUCKY_TURNPLATE_PROP == costType) {// 道具抽奖
            rewardDataManager.checkAndSubPlayerRes(player, turnplateConf.getSubstitute(), activityType == ActivityConst.ACT_LUCKY_TURNPLATE ? AwardFrom.LUCKY_TURNPLATE_PROP : AwardFrom.FAMOUS_GENERAL_TURNPLATE_PROP, turnplat.getActivityId(), turnplat.getActivityType());
        }

        EquipTurnplateRs.Builder builder = EquipTurnplateRs.newBuilder();

        builder.setFreeCount(turnplat.getRefreshCount());
        builder.setGold(player.lord.getGold());
        // 转盘奖励
        int integral = 0;
        List<List<Integer>> awards = new ArrayList<>();
        for (int i = 0; i < turnplateConf.getCount(); i++) {
            if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
                turnplat.setGoldCnt(turnplat.getGoldCnt() + 1);
            }
            awards.add(equipDoSweepstakes(costType, turnplat, turnplateConf, player, integral, activityType));
            // 统计全服抽奖次数
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

        // 积分奖励
        integral = turnplateConf.getPoint() > 0 ? turnplateConf.getPoint() : integral;
        if (integral > 0) {
            activityDataManager.updRankActivity(player, activityType, integral); // 更新积分奖励
        }

        // 金币抽奖奖励
        if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) {
            if (!CheckNull.isEmpty(ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD)) {
                rewardDataManager.addAward(player, ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(0),
                        ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(1), turnplateConf.getCount(),
                        AwardFrom.EQUIP_TURNPLATE_GOLD_AWARD, turnplat.getActivityId(), turnplat.getActivityType());
                builder.addAward(PbHelper.createAwardPb(ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(0),
                        ActParamConstant.EQUIP_TURNPLATE_GOLD_AWRAD.get(1), turnplateConf.getCount()));
            }
        }

        // 拥有的特殊道具碎片次数
        builder.setSpecialCnt(turnplat.currentSpecialCnt());

        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);

        LogUtil.debug("装备转盘-特殊道具的次数节点, roleId:", roleId, ", winCntPoint:", LogUtil.getSetValStr(turnplat.getWinCnt()),
                ", goldCnt:", turnplat.getGoldCnt(), ",globalNum:",
                activityDataManager.getGlobalActivity(activityType).getEquipTurLuckNums(), ", 拥有的碎片数量:",
                turnplat.currentSpecialCnt());

        // 检测碎片是否足够,并兑换道具
        equipCheckSpecialChipAward(turnplateConf.getOnlyAward(), builder, player);

        return builder.build();
    }

    /**
     * @param onlyAward
     * @param builder
     * @param player
     * @return void
     * @Title: equipCheckSpecialChipAward
     * @Description: 检测碎片是否足够, 并兑换道具
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
                    rewardDataManager.checkPropIsEnough(player, propId, staticProp.getChip(), "道具合成");
                    rewardDataManager.subProp(player, propId, staticProp.getChip(), AwardFrom.SYNTHETIC_PROP);
                } catch (Exception e) {
                    LogUtil.error("碎片合成失败", e);
                    return;
                }
                // 兑换道具
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
     * @Description: 装备转盘-单次抽奖的逻辑
     */
    private List<Integer> equipDoSweepstakes(int costType, EquipTurnplat turnplat, StaticEquipTurnplateConf conf,
                                             Player player, int integral, int activityType) {
        int goldCnt = turnplat.getGoldCnt();// 金币抽奖当前次数
        Set<Integer> winCnt = turnplat.getWinCnt();// 抽中特殊奖励的节点
        List<Integer> awardList = new ArrayList<>();// 中奖的道具

        List<Integer> onlyAwardList = new ArrayList<>();// 需要加入权重的特殊道具
        boolean falg = true;// 权重抽奖是否加入特殊道具
        for (int i : winCnt) {
            if (i >= goldCnt) {
                falg = false;// 有一个命中节点 大于或等于当前次数 则权重抽奖不加入特殊道具
                break;
            }
        }
        // 判断玩家是否已经完成保底的中奖次数
        if (falg && goldCnt > 0) {// goldCnt > 0 是为了防止 一直免费抽
            onlyAwardList = conf.getOnlyAward().get(0);
        }
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType || ActivityConst.LUCKY_TURNPLATE_PROP == costType) { // 免费抽奖
            // 根据权重获取奖励
            awardList = equipDoSweepstakesAwards(conf, player, turnplat, onlyAwardList);
            getTurnplatePoint(integral, awardList, 4);
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) { // 金币抽奖
            // 配置的全服抽奖次数
            List<Integer> confGlobalNums = ActParamConstant.EQUIP_TURNPLATE_GLOBAL_NUM;
            // 全服当前次数
            int globalNum = activityDataManager.getGlobalActivity(activityType).getEquipTurLuckNums();
            if (winCnt.contains(goldCnt) || confGlobalNums.contains(globalNum + 1)) { // 达到特殊奖励条件 或 达到全服配置的次数
                awardList = conf.getOnlyAward().get(0);
                getTurnplatePoint(integral, awardList, 3);
                activityDataManager.updActivity(player, ActivityConst.ACT_EQUIP_TURNPLATE, 1,
                        EquipTurnplat.SPECIAL_SORT, false); // 特殊道具在活动期间获取次数
            } else { // 普通金币抽奖
                // 根据权重获取奖励
                awardList = equipDoSweepstakesAwards(conf, player, turnplat, onlyAwardList);
                getTurnplatePoint(integral, awardList, 4);
            }

            // 判断是否抽到特殊奖励
            for (List<Integer> sendChat : ActParamConstant.EQUIP_TURNPLATE_SEND_CHAT_AWARD) {
                if (sendChat.get(0).equals(awardList.get(0)) && sendChat.get(1).equals(awardList.get(1))) {
                    // 发送跑马灯
                    chatDataManager.sendSysChat(ChatConst.CHAT_EQUIP_TURNPLATE_GLOBAL_NUM, player.lord.getCamp(), 0,
                            player.lord.getCamp(), player.lord.getNick(), awardList.get(0), awardList.get(1));
                    //活动消息推送
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
     * @param onlyAwardList 特殊道具
     * @return List<Integer>
     * @Title: EquipDoSweepstakesAwards
     * @Description: 装备转盘-根据权重获取奖励
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
        // 清除添加到权重的装备
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
     * @Description: 装备转盘-计算获取特殊道具的次数节点
     */
    private void equipDoSearchWinCnt(EquipTurnplat equipTurnplat, StaticEquipTurnplateConf equipConf) {
        // 清除之前记录的次数节点
        equipTurnplat.getWinCnt().clear();
        List<Integer> probability = equipConf.getProbability();
        RandomHelper.randomWinCnt(probability, equipTurnplat.getWinCnt());
    }

    // ===========================装备转盘活动 end===========================

    /* ---------------------------充值转盘start----------------------------- **/

    /**
     * 充值转盘获取
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPayTurnplateRs getPayTurnplate(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_PAY_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "充值转盘活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "充值转盘活动未开启, roleId:,", roleId);
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
     * 充值转盘抽奖
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public PlayPayTurnplateRs playPayTurnplate(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_PAY_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "充值转盘活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "充值转盘活动未开启, roleId:,", roleId);
        }
        int payTurnplateCnt = activityDataManager.getPayTurnplateCnt(player, activity);
        if (payTurnplateCnt < 1) {
            throw new MwException(GameError.ACT_PAY_TURNPLATE_CNT_NOT_ENOUGH.getCode(), "充值转盘活动次数不足, roleId:,", roleId);
        }
        int activityId = activity.getActivityId();
        List<StaticActPayTurnplate> actPayTurnplateList = StaticActivityDataMgr
                .getActPayTurnplateListByActId(activityId);
        Map<Integer, Integer> statusMap = activity.getStatusMap();// 已经领取的奖励
        final int curCnt = statusMap.size();

        // 筛选出加入权重的转盘奖励
        List<StaticActPayTurnplate> payTurnplateList = actPayTurnplateList.stream()
                .filter(sapt -> sapt.getDownFrequency() <= curCnt && !statusMap.containsKey(sapt.getId()))
                .collect(Collectors.toList());

        if (CheckNull.isEmpty(payTurnplateList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "充值转盘活动配置出错, roleId:,", roleId);
        }
        // 命中的奖励
        StaticActPayTurnplate hitActPayTurnplate = RandomUtil.getWeightByList(payTurnplateList,
                sapt -> sapt.getWeight());
        if (hitActPayTurnplate == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "充值转盘活动随机配置出错, roleId:,", roleId);
        }

        // 更新领奖状态
        int gainId = hitActPayTurnplate.getId();
        statusMap.put(gainId, 1);
        Award awardRes = rewardDataManager.addAwardSignle(player, hitActPayTurnplate.getAward(),
                AwardFrom.PAY_TURNPLATE);
        LogLordHelper.commonLog("payTurnplate", AwardFrom.PAY_TURNPLATE, player, curCnt, gainId);

        PlayPayTurnplateRs.Builder builder = PlayPayTurnplateRs.newBuilder();
        builder.addAward(awardRes);
        builder.setGainId(gainId);
        return builder.build();
    }

    /* ----------------------------充值转盘end---------------------------- **/
    /* ---------------------------矿石转盘start----------------------------- **/

    /**
     * @param roleId
     * @return GetOreTurnplateRs
     * @throws MwException
     * @Title: getOreTurnplate
     * @Description: 矿石转盘获取
     */
    public GetOreTurnplateRs getOreTurnplate(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        GetOreTurnplateRs.Builder builder = GetOreTurnplateRs.newBuilder();
        builder.setSurplus(activity.getStatusMap().get(ActivityConst.SURPLUS_GOLD) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_GOLD));// 当前档位已消费的金币数

        builder.setRemainCnt(activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM));// 矿石转盘剩余次数
        // 获取玩家当前档位
        int gear = activityDataManager.getOreTurnplateGear(activity);
        builder.setGear(gear);
        // 获取活动配置
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
     * @Description: 矿石转盘抽奖
     */
    public PlayOreTurnplateRs playOreTurnplate(long roleId, int nums) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        // 获取剩余次数
        int oreTurnplateCnt = activity.getStatusMap().get(ActivityConst.SURPLUS_NUM) == null ? 0
                : activity.getStatusMap().get(ActivityConst.SURPLUS_NUM);
        if (oreTurnplateCnt < 0 || oreTurnplateCnt < nums) {
            throw new MwException(GameError.ACT_ORE_TURNPLATE_CNT_NOT_ENOUGH.getCode(), "矿石转盘活动次数不足, roleId:,", roleId);
        }

        int activityId = activity.getActivityId();
        // 获取矿石转盘活动配置
        List<StaticActOreTurnplate> actOreTurnplateList = StaticActivityDataMgr
                .getActOreTurnplateListByActId(activityId);

        // 筛选出加入权重的转盘奖励
        List<StaticActOreTurnplate> oreTurnplateList = actOreTurnplateList.stream().collect(Collectors.toList());

        if (CheckNull.isEmpty(oreTurnplateList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "矿石转盘活动配置出错, roleId:,", roleId);
        }

        PlayOreTurnplateRs.Builder builder = PlayOreTurnplateRs.newBuilder();

        while (nums > 0) {
            // 命中的奖励
            StaticActOreTurnplate hitActOreTurnplate = RandomUtil.getWeightByList(oreTurnplateList,
                    sapt -> sapt.getWeight());
            if (hitActOreTurnplate == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "矿石转盘活动随机配置出错, roleId:,", roleId);
            }
            // 抽奖次数+1
            int alreadyNum = activity.getStatusMap().get(ActivityConst.ALREADY_NUM) == null ? 0
                    : activity.getStatusMap().get(ActivityConst.ALREADY_NUM);
            activity.getStatusMap().put(ActivityConst.ALREADY_NUM, alreadyNum + 1);

            // 剩余次数-1
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

    /* ----------------------------矿石转盘end---------------------------- **/

    /* ---------------------------矿石转盘-新 start----------------------------- **/

    /**
     * @param roleId
     * @return GetOreTurnplateNewRs
     * @throws MwException
     * @Title: getOreTurnplateNew
     * @Description: 矿石转盘获取-新
     */
    public GetOreTurnplateNewRs getOreTurnplateNew(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE_NEW);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        GetOreTurnplateNewRs.Builder builder = GetOreTurnplateNewRs.newBuilder();

        // 获取活动配置
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
     * @Description: 矿石转盘抽奖-新
     */
    public PlayOreTurnplateNewRs playOreTurnplateNew(long roleId, int nums) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ORE_TURNPLATE_NEW);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "矿石转盘活动未开启, roleId:,", roleId);
        }
        // 抽奖次数判断
        if (nums != 1 && nums != 10) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "抽奖次数错误, roleId:,", roleId, ",nums:", nums);
        }

        int activityId = activity.getActivityId();
        // 获取矿石转盘活动配置
        List<StaticActOreTurnplate> actOreTurnplateList = StaticActivityDataMgr
                .getActOreTurnplateListByActId(activityId);

        // 筛选出加入权重的转盘奖励
        List<StaticActOreTurnplate> oreTurnplateList = actOreTurnplateList.stream().collect(Collectors.toList());

        if (CheckNull.isEmpty(oreTurnplateList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "矿石转盘活动配置出错, roleId:,", roleId);
        }

        // 获取金币消耗配置
        List<List<Integer>> goldLists = ActParamConstant.ORE_TRUNPLATE_GOLD;
        if (CheckNull.isEmpty(goldLists)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "矿石转盘金币消耗配置出错, roleId:,", roleId, ",goldLists:",
                    goldLists);
        }
        List<Integer> goldList = new ArrayList<Integer>();
        for (List<Integer> list : goldLists) {
            if (list.size() > 2 && list.get(0) == activityId) {// 根据活动id 获取对应的金币消耗 配置
                goldList = list;
            }
        }
        if (CheckNull.isEmpty(goldList)) {// 配置不存在
            throw new MwException(GameError.NO_CONFIG.getCode(), "矿石转盘金币消耗配置出错, roleId:,", roleId, ",activityId:",
                    activityId);
        }

        PlayOreTurnplateNewRs.Builder builder = PlayOreTurnplateNewRs.newBuilder();
        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
        int needGold = 0;
        if (nums == 1) {// 单抽
            needGold = goldList.get(1);
        } else {// 批量抽
            needGold = goldList.get(2);
        }

        if (needGold > 0) {
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "矿石转盘抽奖");
            rewardDataManager.subGold(player, needGold, AwardFrom.ORE_TURNPLATE_GOLD);
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
        }
        int count = nums;
        while (count > 0) {
            // 命中的奖励
            StaticActOreTurnplate hitActOreTurnplate = RandomUtil.getWeightByList(oreTurnplateList,
                    sapt -> sapt.getWeight());
            if (hitActOreTurnplate == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "矿石转盘活动随机配置出错, roleId:,", roleId);
            }

            int gainId = hitActOreTurnplate.getId();
            Award awardRes = rewardDataManager.addAwardSignle(player, hitActOreTurnplate.getAward(),
                    AwardFrom.ORE_TURNPLATE);
            LogLordHelper.commonLog("oreTurnplateNew", AwardFrom.ORE_TURNPLATE, player, gainId);
            builder.addAward(awardRes);
            builder.addGainId(gainId);
            count--;
        }

        // 金币抽奖奖励
        if (!CheckNull.isEmpty(ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW)) {
            rewardDataManager.addAward(player, ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(0),
                    ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(1), nums, AwardFrom.ORE_TURNPLATE_GOLD_AWARD_NEW);
            builder.addLotteryAward(PbHelper.createAwardPb(ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(0),
                    ActParamConstant.ORE_TURNPLATE_GOLD_AWRAD_NEW.get(1), nums));
        }

        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);

        return builder.build();
    }

    /* ----------------------------矿石转盘-新 end---------------------------- **/

    // ==================================空降补给活动start==================================

    /**
     * 获取空降补给活动
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 空降补给活动未开启,或未初始化 roleId:", roleId);
        }
        StaticActivityPlan plan = activityBase.getPlan();
        long now = System.currentTimeMillis();
        long beginTime = activityBase.getBeginTime().getTime();
        long mailTime = activityBase.getSendMailTime() == null ? 0 : activityBase.getSendMailTime().getTime();
        long endTime = activityBase.getEndTime().getTime();
        boolean isBuy = now > beginTime && now < endTime;
        if (now < beginTime || (mailTime != 0 && now > mailTime)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 空降补给活动未开启,或过期 roleId:", roleId);
        }
        // 判断是否是否过期
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "空降补给活动未开启,或未初始化 roleId:", roleId);
        }
        List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        List<CommonPb.TwoInt> actAwardList = new ArrayList<>();

        for (StaticActAward actAward : awardList) {
            int state = 0;
            int keyId = actAward.getKeyId();
            if (activity.getStatusMap().containsKey(keyId)) {
                state = 2;// 已领取
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
     * 空降补给活动是否可领取奖励
     *
     * @param actAward
     * @param activity
     * @return true 可领取
     */
    private boolean isSupplyDorpGet(StaticActAward actAward, Activity activity, long now) {
        if (CheckNull.isEmpty(actAward.getParam())) {
            return false;
        }
        if (activity.getStatusMap().containsKey(actAward.getKeyId())) {
            return false;// 被领取过
        }
        int param = actAward.getParam().get(0);
        if (!activity.getStatusCnt().containsKey(param)) {
            return false; // 没有购买
        }
        Date nowDate = new Date(now);
        Date buyDate = new Date(activity.getStatusCnt().get(param));// 获取的毫秒数
        int dayiy = DateHelper.dayiy(buyDate, nowDate);
        int cond = actAward.getCond();
        if (dayiy >= cond) {
            return true; // 可领
        }
        return false;
    }

    /**
     * 检测屯田是否全部领取
     *
     * @param activity
     * @param param
     * @return true表示全部领取
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
     * 空降补给活动领取
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 空降补给活动未开启,或未初始化 , roleId:", roleId);
        }
        StaticActivityPlan plan = activityBase.getPlan();
        long now = System.currentTimeMillis();
        long beginTime = activityBase.getBeginTime().getTime();
        long mailTime = activityBase.getSendMailTime() == null ? 0 : activityBase.getSendMailTime().getTime();
        if (now < beginTime || (mailTime != 0 && now > mailTime)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 空降补给活动未开启,或过期  , roleId:", roleId);
        }
        // 判断是否是否过期
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "空降补给活动未开启,或未初始化 , roleId:", roleId);
        }
        if (!activity.getStatusCnt().containsKey(param)) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "空降补给活动,未购买 , roleId:", roleId, ", param:",
                    param);
        }

        List<StaticActAward> sAwardList = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        if (CheckNull.isEmpty(sAwardList)) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), "空降补给活动,没有此奖励类型 roleId:", roleId, ", param:",
                    param);
        }
        SupplyDorpAwardRs.Builder builder = SupplyDorpAwardRs.newBuilder();
        for (StaticActAward sAward : sAwardList) {
            if (keyId != sAward.getKeyId()) {
                continue;
            }
            // 检测是否可领取
            if (!isSupplyDorpGet(sAward, activity, now)) {
                continue;
            }
            // 设置已领取状态
            activity.getStatusMap().put(sAward.getKeyId(), 1);
            // 发放奖励
            builder.addAllAward(
                    rewardDataManager.sendReward(player, sAward.getAwardList(), AwardFrom.ACT_SUPPLY_DORP_AWARD));// "空降补给计划领取奖励"
        }

        for (StaticActAward actAward : sAwardList) {
            int state = 0;
            if (activity.getStatusMap().containsKey(actAward.getKeyId())) {
                state = 2;// 已领取
            } else {
                state = isSupplyDorpGet(actAward, activity, now) ? 1 : 0;
            }
            TwoInt twoInt = PbHelper.createTwoIntPb(actAward.getKeyId(), state);
            builder.addAwardList(twoInt);
        }
        // 检测是否全部领取,通过邮件返还金币
        //        if (hasAwardGet && checkSupplyIsAllGot(activity, param)) {
        //            List<StaticActAward> awardByParam = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        //            int needGold = awardByParam.get(0).getTaskType(); //需要返还的金币
        //            Award awrad = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, needGold);
        //            List<Award> awards = new ArrayList<>();
        //            awards.add(awrad);
        //            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_SUPPLY_DORP_RETURN,
        //                    AwardFrom.ACT_SUPPLY_DORP_RETURN, TimeHelper.getCurrentSecond(), param, param);
        //        }
        return builder.build();
    }

    /**
     * 空降补给活动购买
     *
     * @param roleId
     * @param param  类型 1 资源 2 军备 3 军备
     * @return
     * @throws MwException
     */
    public SupplyDorpBuyRs supplyDorpBuy(long roleId, int param) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (param < 1 || param > 3) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "空降补给活动购买参数错误  param:", param, ", roleId:", roleId);
        }
        int activityType = ActivityConst.ACT_SUPPLY_DORP;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 空降补给活动未开启,或未初始化 , roleId:", roleId);
        }
        StaticActivityPlan plan = activityBase.getPlan();
        long now = System.currentTimeMillis();
        long beginTime = activityBase.getBeginTime().getTime();
        long endTime = activityBase.getEndTime().getTime();
        if (now < beginTime || now > endTime) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 空降补给活动未开启,或过期 , roleId:", roleId);
        }
        // 判断活动是否过期
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "空降补给活动未开启,或未初始化 , roleId:", roleId);
        }
        // 检查是否购买过
        // 购买记录存储在Activity中的 StatusCnt中, key为param种类的id, value为都是购买时的的秒数
        if (activity.getStatusCnt().containsKey(param)) {
            throw new MwException(GameError.ACTIVITY_IS_JOIN.getCode(), "空降补给活动,已经参购买过物质 roleId:", roleId, ", param:",
                    param);
        }
        // 获取所需要的金额
        List<StaticActAward> awardByParam = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(), param);
        if (CheckNull.isEmpty(awardByParam)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "空降补给活动，奖励配置错误, roleId:", roleId);
        }
        int needGold = awardByParam.get(0).getTaskType();

        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                AwardFrom.ACTIVITY_BUY);
        activity.getStatusCnt().put(param, System.currentTimeMillis());
        SupplyDorpBuyRs.Builder builder = SupplyDorpBuyRs.newBuilder();
        builder.addAllParams(activity.getStatusCnt().keySet());

        // 返回购买奖励的状态
        List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        List<CommonPb.TwoInt> actAwardList = new ArrayList<>();
        awardList.stream().filter(saa -> saa.getParam().get(0) == param).forEach(actAward -> {
            int state = 0;
            int keyId = actAward.getKeyId();
            if (activity.getStatusMap().containsKey(keyId)) {
                state = 2;// 已领取
            } else {
                state = isSupplyDorpGet(actAward, activity, now) ? 1 : 0;
            }
            TwoInt twoInt = PbHelper.createTwoIntPb(keyId, state);
            actAwardList.add(twoInt);

        });
        builder.addAllAwardList(actAwardList);
        return builder.build();
    }

    // ==================================空降补给活动end==================================

    // ==================================特价礼包start==================================

    /**
     * 礼包显示
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "礼包活动,未开启 roleId:", roleId);
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
        // 不区分IOS和安卓
        List<StaticPay> staticPays = StaticVipDataMgr.getPayList().stream().filter(pay -> pay.getBanFlag() == PayService.FLAG_PAY_GIFT)
                .collect(Collectors.toList());
        Collection<CommonPb.PayInfo> payInfos = PbHelper.createPayInfo(staticPays);
        builder.addAllPayInfo(payInfos);
        return builder.build();
    }

    /**
     * 同步礼包
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
        //     Long cut = activity.getStatusCnt().get(plan.getKeyId());// 存的是keyId
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

    // ==================================特价礼包end==================================

    public Activity checkActivity(Player player, int activityType) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 检测活动,活动未开启 roleId:", player.roleId,
                    ", activityType:", activityType);
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.OPEN_STEP) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 检测活动,活动未开启 roleId:", player.roleId,
                    ", activityType:", activityType);
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            // sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 检测活动,活动未开启 roleId:", player.roleId,
                    ", activityType:", activityType);
        }
        return activity;
    }

    /**
     * 领取能量赠送活动能晶
     */
    public GetFreePowerRs getFreePower(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkActivity(player, ActivityConst.ACT_FOOD);

        final int createRoleDate = DateHelper.dayiy(player.account.getCreateDate(), new Date());
        final StaticActivityTime[] staticActivityTime = {null};
        if (createRoleDate == 1) {
            // 创角第一天
            Optional.ofNullable(StaticActivityDataMgr.getActivityTimeById(activity.getActivityId()))
                    .ifPresent(sats -> {
                        // 已结领取了不处理
                        staticActivityTime[0] = sats.stream()
                                .filter(sat -> {
                                    int key = sat.getKeyId();
                                    Long status = activity.getStatusCnt().get(key);
                                    // 已结领取了不处理
                                    return status != null && status == 1;
                                })
                                .min(Comparator.comparingInt(StaticActivityTime::getKeyId))
                                .orElse(null);


                    });

        } else {
            staticActivityTime[0] = ActivityDataManager.getCurActivityTime(activity.getActivityId());
        }
        if (null == staticActivityTime[0]) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "领取能量赠送活动能晶,未开启 roleId:", roleId);
        }

        int state = activity.getStatusCnt().get(staticActivityTime[0].getKeyId()).intValue();
        if (state == 2) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), " 领取能量赠送活动能晶 roleId:", roleId);
        }

        // 更新活动状态，1 未领取，2 已领取
        activity.setEndTime(TimeHelper.getCurrentDay());// 记录时间
        activity.getStatusCnt().put(staticActivityTime[0].getKeyId(), 2L);

        // 发送奖励
        GetFreePowerRs.Builder builder = GetFreePowerRs.newBuilder();
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        List<List<Integer>> awardList = new ArrayList<>();
        // 基础奖励
        for (List<Integer> award : staticActivityTime[0].getAwardList()) {
            awardList.add(award);
        }
        // 额外奖励
        for (List<Integer> rAward : staticActivityTime[0].getRandomAward()) {
            if (rAward.size() != 4) {
                continue;// 配置数据有问题
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
     * 获取能量赠送活动数据
     */
    public GetPowerGiveDataRs getPowerGiveData(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkActivity(player, ActivityConst.ACT_FOOD);

        refreshPowerState(roleId, activity); // 刷新能量赠送状态

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
     * 刷新能量赠送状态
     *
     * @param roleId
     * @param activity
     * @throws MwException
     */
    private void refreshPowerState(long roleId, Activity activity) throws MwException {
        List<StaticActivityTime> list = StaticActivityDataMgr.getActivityTimeById(activity.getActivityId());
        if (CheckNull.isEmpty(list)) {// || !list.get(0).getOpenWeekDay().contains(weekDay)
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 获取能量赠送活动数据 roleId:", roleId);
        }
        int totday = TimeHelper.getCurrentDay();
        if (activity.getEndTime() != totday) {// 隔天后清空记录
            activity.setEndTime(totday);// 用endTime参数记录玩家最后领取能晶补给活动奖励的日期
            activity.getStatusCnt().clear();
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        Date now = new Date();
        // 创角第一天
        if (DateHelper.dayiy(beginTime, now) == 1) {
            list.forEach(sat -> {
                int key = sat.getKeyId();
                Long status = activity.getStatusCnt().get(key);
                // 已结领取了不处理
                if (status != null && status == 2) {
                    return;
                }
                Date date = DateHelper.afterStringTime(now, sat.getEndTime());
                // 过了领取时间
                boolean state = DateHelper.inThisTime(new Date(), sat.getStartTime(), sat.getEndTime()) || DateHelper.isAfterTime(now, date);
                // 变为可领取
                activity.getStatusCnt().put(sat.getKeyId(), state ? 1L : 0L);
            });
        } else {
            StaticActivityTime curSat = ActivityDataManager.getCurActivityTime(activity.getActivityId());
            for (StaticActivityTime sat : list) {
                int key = sat.getKeyId();
                Long status = activity.getStatusCnt().get(key);
                if (status != null && status == 2) {// 已结领取了不处理
                    continue;
                }
                boolean state = curSat != null && curSat.getKeyId() == sat.getKeyId();
                activity.getStatusCnt().put(sat.getKeyId(), state ? 1L : 0L);// 能量补给活动状态，0 不能领取，1 未领取，2 已领取
            }
        }
    }

    /**
     * 购买成长计划
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
            throw new MwException(GameError.ACTIVITY_IS_JOIN.getCode(), " 重复购买成长计划 roleId:", roleId);
        }
        List<Integer> need = ActParamConstant.OPEN_ACT_GROW_NEED;
        if (need.isEmpty() || need.size() < 2) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 重复购买成长计划 roleId:", roleId);
        }
        if (need.get(0) > player.lord.getVip() || need.get(1) <= 0) {
            throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), " 重复购买成长计划 roleId:", roleId);
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
     * 推送所有在线玩家的 能量赠送
     */
    public void syncAllPlayerActPower() {
        // 应用内推送
        for (Player player : playerDataManager.getPlayers().values()) {
            if (player.isLogin) {
                activityDataManager.syncActChange(player, ActivityConst.ACT_FOOD);
            }
        }
        // 保留应用内推送
        //  判断当前小时是否是中午12点, 获取推送id
        // int pushId = TimeHelper.getHour() == 12 ? PushConstant.ACT_POWER_TWELVE : PushConstant.ACT_POWER_SIX;
        //  应用外推送
        // playerDataManager.getPlayers().values().stream().filter(Player::canPushActPower).forEach(p -> {
        //     PushMessageUtil.pushMessage(p.account, pushId);
        // });
    }

    /**
     * 空降补给领奖邮件
     */
    public void sendUnrewardedMailBySupplyDorp(Player player, int now) {
        if (player == null) {
            return;
        }
        // 此处不能使用 getActivityInfo方法,因为此时活动可能是未开启状态
        Activity activity = player.activitys.get(ActivityConst.ACT_SUPPLY_DORP);
        if (activity == null) {
            return;
        }
        if (activity.getStatusCnt().isEmpty()) {// 未购买
            return;
        }
        List<Award> awards = new ArrayList<>();
        int glod = 0;
        for (Integer param : activity.getStatusCnt().keySet()) {
            //            if (checkSupplyIsAllGot(activity, param)) { // 全部领奖的过掉
            //                continue;
            //            }
            List<StaticActAward> sAwardList = StaticActivityDataMgr.getSupplyDorpByParam(activity.getActivityId(),
                    param);
            //判断日期是否达到发送钻石返利的要求
            Date nowDate = new Date(now * 1000L);
            Date buyDate = new Date(activity.getStatusCnt().get(param));
            int dayiy = DateHelper.dayiy(buyDate, nowDate); //已购买天数
            StaticActAward maxAward = StaticActivityDataMgr.getSupplyMaxByParam(activity.getActivityId(), param);
            if (dayiy == maxAward.getCond() + 1 && !activity.getStatusMap().containsKey(maxAward.getParam().get(0))) {
                glod += maxAward.getTaskType();
                // 设置已领取状态
                activity.getStatusMap().put(maxAward.getParam().get(0), 1);
            }
            if (dayiy > maxAward.getCond()) {
                for (StaticActAward sAward : sAwardList) {
                    if (!activity.getStatusMap().containsKey(sAward.getKeyId())) {// 未领取的
                        // 设置已领取状态
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
     * 排行活动领奖处理
     *
     * @param actType
     */
    public void actAwardTimeProcess(int actType) {
        LogUtil.debug("---------排行活动领奖处理:", actType);
        if (!StaticActivityDataMgr.isActTypeRank(actType)) {
            return;
        }
        GlobalActivityData usualActivityData = activityDataManager.getGlobalActivity(actType);
        if (usualActivityData == null) {
            return;
        }
        // 结算一次排行的名次
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
                // 排行活动结算红点推送
                // try {
                // activityDataManager.syncActChange(p, actType);
                // } catch (Exception e) {
                // LogUtil.error(e);
                // }
            }
        }
    }

    /**
     * 每日清除获得发未领取邮件
     */
    public void sendUnrewardedMailForCleanDay(String jobKeyName) {
        LogUtil.debug("每日清理活动,自动领奖处理");
        // 营造一下第二天凌晨的假象,实际是头一天的23:59:50触发(获取Activity对象时,autoDayClean()会主动清除状态)
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
     * 7日充值 邮件领取奖励
     *
     * @param player
     */
    public void checkAndSendPay7DayMail(Player player) {
        Date now = new Date();
        int day = playerDataManager.getCreateRoleDay(player, now);
        if (day == 8) {// 第8天才领取奖励
            int nowInt = (int) (now.getTime() / 1000);
            sendUnrewardedMailByNormal(player, ActivityConst.ACT_PAY_7DAY, nowInt);
        }
    }

    /**
     * 未领取奖励的活动通过邮件发送
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
     * 每天11点59分,清除活动数据
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
     * 活动结束前,自动兑换成指定奖励
     *
     * @param actType
     */
    public void autoExchangUnrewardeMail(Integer actType) {
        int now = TimeHelper.getCurrentSecond();
        // 转盘
        for (Player player : playerDataManager.getPlayers().values()) {
            if (actType == ActivityConst.ACT_LUCKY_TURNPLATE || actType == ActivityConst.FAMOUS_GENERAL_TURNPLATE || actType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                    || actType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR || actType == ActivityConst.ACT_SEASON_TURNPLATE) {// 幸运/名将
                autoExchangUnrewardeMailByTurnplate(player, actType, now);
            }
            if (actType == ActivityConst.ACT_EQUIP_TURNPLATE) {
                autoExchangUnrewardeMailByEquipTurnplate(player, actType, now);
            }
//            if(actType == ActivityConst.ACT_DIAOCHAN){
//                activityDiaoChanService.handleOver(player);
//            }
            if(actType == ActivityConst.ACT_DROP_CONTROL){
                this.autoConvertDropMail(player);
            }
        }
        if (actType == ActivityConst.ACT_GOOD_LUCK) {
            activityLotteryService.autoExchangeByGoodLuck();
        } else if (actType == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR) {
            // 已告知策划, 如果没有配置结束发邮件配置, 就不会清楚历史的活动消息
            chatDataManager.getActivityChat(ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR).clear();
        }
    }

    private void autoConvertDropMail(Player player){
        Activity activity = player.activitys.get(ActivityConst.ACT_DROP_CONTROL);
        if(Objects.isNull(activity)){
            return;
        }
        int activityId = activity.getActivityId();
        List<StaticActBandit> sActBandits = StaticActivityDataMgr.getActBanditList().stream().filter(sab -> sab.getActivityId() == activityId).collect(Collectors.toList());
        if(Objects.isNull(sActBandits)){
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
            if (type == StaticActBandit.ACT_HIT_DROP_TYPE_1) {
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
                }catch (MwException e) {
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
     * 活动结束前,自动将特殊道具碎片,兑换成指定奖励
     *
     * @param player
     * @param actType
     * @param now
     */
    private void autoExchangUnrewardeMailByTurnplate(Player player, Integer actType, int now) {
        try{
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
                // 207特殊处理
                if (actType == ActivityConst.FAMOUS_GENERAL_TURNPLATE && !CheckNull.isEmpty(Constant.FAMOUS_GENERAL_EXCHANGE_PROP)) {
                    for (List<Integer> exchangeProp : Constant.FAMOUS_GENERAL_EXCHANGE_PROP) {
                        // 判断活动兑换的activityId
                        if (activity.getActivityId() != exchangeProp.get(3)) {
                            continue;
                        }
                        int roleResByType = (int) rewardDataManager.getRoleResByType(player, exchangeProp.get(0), exchangeProp.get(1));
                        if (roleResByType > 0) {
                            try {
                                rewardDataManager.subProp(player, exchangeProp.get(1), roleResByType, AwardFrom.EXCHANGE_FAMOUS_EXPIRED_EXCHANGE_REWARDE);
                                // 通知玩家消耗的资源类型
                                change.addChangeType(exchangeProp.get(0), exchangeProp.get(1));
                                rewardDataManager.syncRoleResChanged(player, change);
                                ArrayList<CommonPb.Award> awards = new ArrayList<>();
                                List<Integer> convertTargetList = null;
                                for (List<Integer> list : ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD) {
                                    if(list.get(3) == activity.getActivityId()){
                                        convertTargetList = list;
                                        break;
                                    }
                                }
                                Optional.ofNullable(convertTargetList).ifPresent(tmps -> awards.add(PbHelper.createAwardPb(tmps.get(0),tmps.get(1),roleResByType * tmps.get(2))));

                                if (!awards.isEmpty()) {
                                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_EXCHANGE_REWARD,
                                            AwardFrom.ACT_UNREWARDED_RETURN, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
                                }
                            } catch (MwException e) {
                                LogUtil.error(e, "幸运/名将 转盘活动结束,道具碎片兑换成指定奖励");
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

                List<Integer> award = onlyAward.get(0);// 能够被兑换的道具
                if (CheckNull.isEmpty(award)) {
                    return;
                }
                // 兑换次数
                int chipCnt = (int) rewardDataManager.getRoleResByType(player, award.get(0), award.get(1));
                if (chipCnt > 0 && !CheckNull.isEmpty(isLuckyTurn(actType) ? ActParamConstant.ACT_TURNPLATE_EXCHANGE_AWRAD
                        : ActParamConstant.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD)) {
                    try {
                        rewardDataManager.subProp(player, award.get(1), chipCnt,
                                isLuckyTurn(actType) ? AwardFrom.TURNPLATE_EXPIRED_EXCHANGE_REWARDE
                                        : AwardFrom.EXCHANGE_FAMOUS_EXPIRED_EXCHANGE_REWARDE);// "幸运转盘过期道具回收"
                        // 通知玩家消耗的资源类型
                        change.addChangeType(award.get(0), award.get(1));
                        rewardDataManager.syncRoleResChanged(player, change);
                    } catch (MwException e) {
                        LogUtil.error(e, "幸运/名将 转盘活动结束,道具碎片兑换成指定奖励");
                        return;
                    }
                    List<Award> awards = new ArrayList<>();
                    if (isLuckyTurn(actType)) {// 幸运转盘
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
            LogUtil.error("",e);
        }
    }


    /**
     * 活动结束前,自动将特殊道具碎片,兑换成指定奖励
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

            List<Integer> award = onlyAward.get(0);// 能够被兑换的道具
            if (CheckNull.isEmpty(award)) {
                return;
            }
            ChangeInfo change = ChangeInfo.newIns();
            // 兑换次数
            int chipCnt = (int) rewardDataManager.getRoleResByType(player, award.get(0), award.get(1));
            if (chipCnt > 0) {
                try {
                    rewardDataManager.subProp(player, award.get(1), chipCnt, AwardFrom.EQUIP_TURNPLATE_EXPIRED_EXCHANGE_REWARDE);// "幸运转盘过期道具回收"
                    // 通知玩家消耗的资源类型
                    change.addChangeType(award.get(0), award.get(1));
                    rewardDataManager.syncRoleResChanged(player, change);
                } catch (MwException e) {
                    LogUtil.error(e, "幸运/名将 转盘活动结束,道具碎片兑换成指定奖励");
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
            LogUtil.error("",e);
        }
    }

    /**
     * @param actType
     * @return boolean
     * @Title: isLuckyTurn
     * @Description: 是否是幸运转盘
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
                || actType == ActivityConst.ACT_ELIMINATE_BANDIT) {// 通用部分精英部队充值有礼消费有礼战火试炼
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByNormal(player, actType, now);
            }
        } else if (actType == ActivityConst.ACT_CHARGE_CONTINUE
                || actType == ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {// 连续充值奖励
            for (Player player : playerDataManager.getPlayers().values()) {
                activityChargeContinueService.sendUnrewardedMailByChargeContinue(player, actType, now);
            }
        } else if (actType == ActivityConst.ACT_SUPPLY_DORP) { // 空降补给
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailBySupplyDorp(player, now);
            }
        } else if (actType != ActivityConst.ACT_GESTAPO_RANK && actType != ActivityConst.ACT_ROYAL_ARENA && StaticActivityDataMgr.isActTypeRank(actType)) { // 排行活动,排除盖世太保
            if(actType == ActivityConst.ACT_CHRISTMAS || actType == ActivityConst.ACT_REPAIR_CASTLE) {
                activityChristmasService.overAndSendMail(jobKeyName);
            }else {
                for (Player player : playerDataManager.getPlayers().values()) {
                    sendUnrewardedMailByRank(player, actType, now);
                }
                sendCampFightRankMail(actType, now);// 发送阵营战斗力排行箱子奖励
            }
        } else if (actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY) {// 全局返利, 勇冠三军
            sendUnrewardedMailByActAllCharge(actType, now);
        } else if (actType == ActivityConst.ACT_DAILY_PAY) {// 每日充值
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByDailyPay(player, actType, now);
            }
        } else if (actType == ActivityConst.ACT_ATK_GESTAPO) {// 盖世太保
            Map<Integer, Integer> campRank = calcGestapoCampAward();// 阵营名次
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByGestapo(player, actType, now);
                sendUnrewardedMailByGestapoRank(player, now, campRank);// 盖世太保排行榜
            }
            // 活动结束后清除所有太保
            Iterator<Gestapo> iterator = worldDataManager.getGestapoMap().values().iterator();
            List<Integer> posList = null;
            while (iterator.hasNext()) {
                Gestapo gestapo = iterator.next();
                int pos = gestapo.getPos();
                try {
                    // 取消太保战斗,并返回队列,发送邮件
                    warService.cancelGestapoBattle(pos);

                    // 移除这个盖世太保(最后移除,在返回队伍加速时,有用到)
                    worldDataManager.removeBandit(pos, 2);

                    if (CheckNull.isEmpty(posList)) {
                        posList = new ArrayList<>();
                    }
                    // 通知周围玩家
                    posList.add(gestapo.getPos());
                } catch (Exception e) {
                    LogUtil.error(e, "执行盖世太保定时任务出现错误, pos:", pos);
                }
            }
            if (!CheckNull.isEmpty(posList)) {
                // 通知其他玩家数据改变
                EventBus.getDefault().post(
                        new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            }

        } else if (actType == ActivityConst.ACT_ATTACK_CITY_NEW) {// 新攻城掠地活动
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByAtkCity(player, now);
            }
        } else if (actType == ActivityConst.ACT_PROP_PROMOTION) { // 军备促销
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByPromotion(player, now);
            }
        } else if (actType == ActivityConst.ACT_THREE_REBATE) {// 三倍返利
            Collection<Player> values = playerDataManager.getPlayers().values();
            LogUtil.debug("-----------三倍返利参与人数:" + values.size() + "--------------------");
            for (Player player : playerDataManager.getPlayers().values()) {
                sendThreeRebateReward(player, now);
            }
        }
        // 奖章活动
        else if (actType == ActivityConst.ACT_BANDIT_AWARD) {
            for (Player player : playerDataManager.getPlayers().values()) {
                sendUnrewardedMailByBanditAward(player, now);
            }
        } else if (actType == ActivityConst.ACT_ROYAL_ARENA) {
            Map<Integer, Integer> campRank = calcCampRoyalArenaAward();// 阵营名次
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
     * 计算阵营对拼奖励值
     *
     * @return key:camp val:名次
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
     * 发送
     *
     * @param player
     * @param actType
     * @param now
     * @param campRank
     */
    private void sendUnrewardedMailByRoyalArena(Player player, int actType, int now, Map<Integer, Integer> campRank) {
        // 此处不能使用 getActivityInfo方法,因为此时活动可能是未开启状态
        Activity activity = player.activitys.get(actType);
        if (activity == null) {
            return;
        }
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (activityMap == null) {
            return;
        }
        GlobalRoyalArena activityData = (GlobalRoyalArena) activityMap.get(ActivityConst.ACT_ROYAL_ARENA);
        // 该排行榜没有数据
        if (CheckNull.isNull(activityData)) {
            return;
        }
        int rankAwardSchedule;
        ActRank rank = activityData.getPlayerRank(player, ActivityConst.ACT_ROYAL_ARENA, player.roleId);
        if (rank != null) {
            rankAwardSchedule = rank.getRank();
            // 能上榜说明贡献值大于100了
            int pCamp = player.lord.getCamp();
            // 阵营排名
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
            Map<Integer, Integer> statusMap = activityData.getStatusMap(); // 活动领取 活动keyID,领取状态1已领取
            // 按照档次发奖励的排行
            StaticActAward myAward = StaticActivityDataMgr.findRankAward(activityData.getActivityId(), rankAwardSchedule);
            if (myAward == null) {
                return;
            }
            // 发放相应档次的奖励
            sendRankAwardBetween(activity, rankAwardSchedule, awards, statusMap, myAward);
            if (!awards.isEmpty()) {
                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ROYAL_ARENA_PERSON_REWARD,
                        AwardFrom.ACT_UNREWARDED_RETURN, now, rankAwardSchedule);
            }
        }
    }

    /**
     * 奖章兑换活动结束道具回收
     *
     * @param player
     * @param now
     */
    private void sendUnrewardedMailByBanditAward(Player player, int now) {
        Activity activity = player.activitys.get(ActivityConst.ACT_BANDIT_AWARD);
        if (CheckNull.isNull(activity)) {
            return;
        }
        // 活动id
        int activityId = activity.getActivityId();
        List<StaticActBandit> actBandits = StaticActivityDataMgr.getActBanditList();
        // List<StaticActBandit> actBandits = StaticActivityDataMgr.getActBanditList(StaticActBandit.ACT_HIT_DROP_TYPE_1);
        if (CheckNull.isEmpty(actBandits)) {
            return;
        }

        // 玩家等级
        int lv = player.lord.getLevel();

        // 活动配置
        List<StaticActBandit> sActBandits = actBandits.stream().filter(sab -> sab.getActivityId() == activityId).collect(Collectors.toList());
        if (CheckNull.isEmpty(sActBandits)) {
            return;
        }
        // 兑换奖励
        List<Award> awards = new ArrayList<>();
        // 活动配置
        sActBandits.forEach(sab -> {
            // 消耗道具
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
                // 召唤道具
                try {
                    rewardDataManager.checkAndSubPlayerAllRes(player, cost.get(0), cost.get(1), AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
                } catch (MwException e) {
                    LogUtil.error(e);
                    return;
                }
                // 兑换的道具
                List<List<Integer>> convert = sab.getConvert();
                convert.forEach(list -> awards.add(PbHelper.createAwardPb(list.get(0), list.get(1), (int) (list.get(2) * cnt))));
            });
        });
        // 发送邮件
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_BANDIT_CONVERT_AWARD, AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond());
        }
    }

    /***
     * 三倍返利活动结束未领取奖励玩家通过邮件发送奖励
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
                && !activityDataManager.getIsGet(player, activity)) {// 条件满足未奖励的玩家
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
     * 军备促销活动奖励发送
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
        if (CheckNull.isNull(activityData)) { // 该全服记录没有数据
            return;
        }
        StaticActAward sAward = StaticActivityDataMgr.getActAwardById(ActivityConst.ACT_PROP_PROMOTION).get(0);
        if (CheckNull.isNull(sAward)) {
            return;
        }
        List<List<Integer>> awardList = sAward.getAwardList();
        int keyId = sAward.getKeyId();
        // 已领取次数
        int schedule = activity.getSaveMap().containsKey(keyId) ? activity.getSaveMap().get(keyId) : 0;
        if (schedule >= sAward.getCond()) { // 领取达到上限
            return;
        }
        long val = activityData.getCampValByCamp(player.lord.getCamp()); // 阵营积分
        int awardCnt = activityDataManager.getAwardCnt(sAward, activity, val); // 可领取次数
        if (awardCnt <= 1) {
            return;
        }
        activity.getSaveMap().put(keyId, awardCnt + schedule);

        // 领奖次数
        for (int i = 0; i < awardList.size(); i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= awardCnt;// 奖励次数
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
     * 盖世太保排行榜活动(先发送个人奖励,再发送阵营奖励)
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
        if (CheckNull.isNull(activityData)) { // 该排行榜没有数据
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
        // 个人排行奖励
        gestapoKillAward(player, now, rankAct, rankAwardSchedule);
        if (Long.valueOf(rankValue).intValue() < ActParamConstant.ACT_GESTAPO_CAMP_RANK_LEVEL) {
            return;
        }
        // 阵营奖励
        gestapoCampAward(player, now, gestapoAct, campRank);
    }

    /**
     * 计算盖世太保阵营奖励值
     *
     * @return key:camp val:名次
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
     * 盖世太保击杀阵营奖励发送
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
     * 盖世太保个人奖励发送
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
        Map<Integer, Integer> rankStatus = rankAct.getStatusMap(); // 活动领取 活动keyID,领取状态1已领取
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
     * 攻城掠地活动邮件(先领取活跃度,再发送奖励)
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
        // int dayiy = DateHelper.dayiy(actBase.getBeginTime(), new Date()); // 活动开启的第几天
        for (StaticAtkCityAct atkCityAct : StaticActivityDataMgr.getAtkCityActList()) { // 领取活跃度
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
     * 盖世太保邮件
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
        // 兑换道具
        int exchangeCnt = activityDataManager.currentActivity(player, activity, 0);
        // 召唤道具
        int summonCnt = worldDataManager.getCostCnt(player);
        int sumCnt = exchangeCnt + summonCnt;
        LogUtil.debug("roleId=" + player.roleId + " ,兑换道具=" + exchangeCnt + " ,召唤道具=" + summonCnt);
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
            // 兑换道具
            rewardDataManager.checkAndSubPlayerResHasSync(player, expendProp.get(0), expendProp.get(1), exchangeCnt,
                    AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
            List<StaticGestapoPlan> gestapoPlans = StaticWorldDataMgr.getGestapoList();
            for (StaticGestapoPlan gestapoPlan : gestapoPlans) {
                List<List<Integer>> costProp = gestapoPlan.getCostProp();
                if (CheckNull.isEmpty(costProp)) continue;
                List<Integer> prop = costProp.get(0);
                if (CheckNull.isEmpty(prop)) continue;
                // 召唤道具
                rewardDataManager.checkAndSubPlayerAllRes(player, prop.get(0), prop.get(1),
                        AwardFrom.GESTAPO_EXPIRED_EXCHANGE_RESOURCES);
            }
        } catch (MwException e) {
            LogUtil.error(e, "盖世太保活动过期,兑换道具兑换成资源");
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
     * 获取过期后兑换的资源
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
        // 兑换比例
        BigDecimal baseCnt = new BigDecimal(exchangeList.get(2));
        // 兑换后的资源数量
        int resCnt = baseCnt.multiply(new BigDecimal(sumCnt)).intValue();
        exchangeList.set(2, resCnt);
        return destExRes;
    }

    /**
     * 全军返利邮件
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
        // 获胜阵营
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
                    // 活动id
                    int activityId = activityData.getActivityId();
                    // 该活动id的奖励
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
     * 未领奖邮件(部分通用)
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
                if (actType == ActivityConst.ACT_ALL_CHARGE || actType == ActivityConst.ACT_BRAVEST_ARMY) {// 全军返利 勇冠三军 额外逻辑
                    // 玩家充值的金币总数
                    int topup = activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD) == null ? 0
                            : activity.getSaveMap().get(ActivityConst.ACT_ALL_CHARGE_LORD_GOLD);
                    // 全军返利 个人需充值的金币数
                    int checkLordGlod = 0;
                    if (saa.getParam() != null && saa.getParam().size() > 0) {
                        checkLordGlod = saa.getParam().get(0);
                    }
                    if (topup < checkLordGlod) {// 不满足
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
     * 发送阵营战斗力排行箱子奖励
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
            if (gAct == null) { // 该排行榜没有数据
                return;
            }
            if (CheckNull.isEmpty(ActParamConstant.ACT_CAMP_FIGHT_WIN_CAMP_AWARD)) {
                return;
            }
            final int activityId = gAct.getActivityId();
            List<Integer> actIdAndAward = ActParamConstant.ACT_CAMP_FIGHT_WIN_CAMP_AWARD.stream()
                    .filter(l -> l != null && l.size() >= 4 && l.get(0) == activityId).findFirst().orElse(null);
            if (actIdAndAward == null) {// 没找到取第一个
                actIdAndAward = ActParamConstant.ACT_CAMP_FIGHT_WIN_CAMP_AWARD.get(0);
            }
            if (actIdAndAward.size() < 4) {
                return;
            }
            List<CommonPb.Award> awards = new ArrayList<>();
            awards.add(PbHelper.createAwardPb(actIdAndAward.get(1), actIdAndAward.get(2), actIdAndAward.get(3)));
            int maxCampVal = calcMaxCampValByGlobalActivityData(gAct); // 胜利的阵营
            for (Player player : playerDataManager.getPlayers().values()) {
                if (player.lord.getCamp() == maxCampVal) {
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_WIND_CAMP_FIGHT_RANK_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, now, maxCampVal, maxCampVal);
                }
            }
        }
    }

    /**
     * 返回最大值的阵营
     *
     * @param gAct
     * @return 返回阵营
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
     * 排行活动领奖邮件
     *
     * @param player
     * @param actType
     */
    public void sendUnrewardedMailByRank(Player player, int actType, int now) {
        try {
            if (player == null) {
                return;
            }
            // 此处不能使用 getActivityInfo方法,因为此时活动可能是未开启状态
            Activity activity = player.activitys.get(actType);
            Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
            if (activityMap == null) {
                return;
            }
            GlobalActivityData globalActivityData = activityMap.get(actType);
            // 发送排行榜奖励
            sendUnrewardedMailByRank(player, now, activity, globalActivityData);
        } catch (Exception e) {
            LogUtil.error("邮件发送奖励异常", e);
        }
    }

    /**
     * 发送排行榜奖励的重载方法
     *
     * @param player
     * @param now
     * @param activity
     * @param globalActivityData
     */
    public void sendUnrewardedMailByRank(Player player, int now, Activity activity, GlobalActivityData globalActivityData) {
        if (globalActivityData == null) { // 该排行榜没有数据
            return;
        }
        if (activity == null) {
            return;
        }
        if (activity.getStatusCnt().isEmpty()) {// 未购买
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
        Map<Integer, Integer> statusMap = activity.getStatusMap(); // 活动领取 活动keyID,领取状态1已领取
        if (StaticActivityDataMgr.isOnlyRankAward(actType)) {
            if (rank == null) {
                return;
            }
            // 按照档次发奖励的排行
            StaticActAward myAward = StaticActivityDataMgr.findRankAward(activity.getActivityId(), rank.getRank());
            if (myAward == null) {
                return;
            }
            // 发放相应档次的奖励
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
     * 排行奖励,余下所有档位(rank > cond)
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
            if (!statusMap.containsKey(sAward.getKeyId())) {// 未领取的
                awards.addAll(PbHelper.createAwardsPb(sAward.getAwardList()));
                activity.getStatusMap().put(sAward.getKeyId(), 1);
            }
        }
    }

    /**
     * 排行奖励,当前区间档位(区间[param[1] - cond])
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
            if (!statusMap.containsKey(sAward.getKeyId())) {// 未领取的
                awards.addAll(PbHelper.createAwardsPb(sAward.getAwardList()));
                activity.getStatusMap().put(sAward.getKeyId(), 1);
            }
        }
    }

    /**
     * 每日充值领奖邮件
     *
     * @param player
     * @param actType
     * @param now
     */
    private void sendUnrewardedMailByDailyPay(Player player, int actType, int now) {
        if (player == null) {
            return;
        }
        // 此处不能使用 getActivityInfo方法,因为此时活动可能是未开启状态
        Activity activity = player.activitys.get(actType);
        if (activity == null) {
            return;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (CheckNull.isNull(activityBase)) {
            return;
        }
        // if (activity.getStatusCnt().isEmpty()) {// 未购买
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
            // 每日领奖
            if (award.getParam().get(0) == 0) {
                int lastDay = TimeHelper.getDay(player.lord.getOnTime());
                if (currentDay == lastDay) {
                    activity.getStatusMap().put(award.getKeyId(), 1);
                    awards.addAll(PbHelper.createAwardsPb(award.getAwardList()));
                }
                // 每日充值
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
     * 领取在线奖励
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }

        if (activityType != ActivityConst.ACT_ONLINE_GIFT && activityType != ActivityConst.ACT_GIFT_OL) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), "领取在线奖励时,无此活动, roleId:,", roleId);
        }

        // Activity activity = player.activitys.get(activityType);
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        if (actAward.getActivityId() != activity.getActivityId()) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), "领取在线奖励时,无此活动, roleId:,", roleId);
        }

        Integer awardStatus = activity.getStatusMap().get(keyId);
        if (awardStatus != null && awardStatus == 1) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId);
        }
        int now = TimeHelper.getCurrentSecond();

        List<List<Integer>> awardList = actAward.getAwardList();
        // 检测背包是否已满
        rewardDataManager.checkBag(player, awardList);

        int schedule = activityDataManager.currentActivity(player, activity, 0, now);

        if (schedule == 0) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
        }
        LogUtil.debug("GetOnLineAwardRs activity=" + activity);

        if (actAward.getCond() <= 0 || schedule < actAward.getCond()) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
        }

        activity.getStatusMap().put(keyId, 1);

        // 在线奖励
        if (activityType == ActivityConst.ACT_ONLINE_GIFT) {
            activityDataManager.updActivity(player, activityType, now, 0, true);
        }

        // 在线奖励的领取次数
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_ONLINE_AWARD_CNT, 1);

        int state = 0;

        GetOnLineAwardRs.Builder builder = GetOnLineAwardRs.newBuilder();

        int size = awardList.size();
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        for (int i = 0; i < size; i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            count *= num;// 翻倍活动
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
                int Status = activity.getStatusMap().containsKey(id) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
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
        // 0 : 没有下个阶段活动, 1 : 还有下个阶段
        builder.setState(state);
        return builder.build();
    }

    // ==================================触发式礼包start==================================

    /**
     * 触发式礼包推送
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
            //翻牌触发礼包- 特殊处理（从配置中随机一个且与之前已触发的不重复）
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
                        throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:",
                                player.roleId, ", giftId:", conf.getGiftId());
                    }
                    if (tgift.getState() != ActivityConst.NOT_TRIGGER_STATUS) { //不可重复触发
                        continue;
                    }
                    giftList.add(tgift);
                }
                if (giftList.size() >= 1) {//随机取出一个
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
                    throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:",
                            player.roleId, ", giftId:", conf.getGiftId());
                }
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL) { // 战役战斗失败
                    //仅帐号注册前7天触发
                    Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
                    long time = beginTime.getTime() + 7 * TimeHelper.DAY_S * 1000;
                    if (System.currentTimeMillis() > time) {
                        continue;
                    }
                    if (triggerGift.getStatus() != conf.getCond().get(0)) { // 不是指定次数
                        continue;
                    }
                    //帐号注册前3天不触发这个，触发首充
                    long threeTime = beginTime.getTime() + 3 * TimeHelper.DAY_S * 1000;
                    if (System.currentTimeMillis() < threeTime) {
                        Activity activity = player.activitys.get(ActivityConst.ACT_FIRSH_CHARGE);
                        if (activity.getStatusMap().size() == 0) {
                            checkFirstChargeSync(player);
                            return;
                        }
                    }
                }
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_EXPEDITION_FAIL) { // 帝国远征战斗失败（宝石副本）
                    if (triggerGift.getStatus() != conf.getCond().get(0)) { // 不是指定次数
                        continue;
                    }
                    //帐号注册前3天不触发这个，触发首充
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
                if (conf.getTriggerId() == ActivityConst.TRIGGER_GIFT_REBUILD) { // 重建家园
                    if (triggerGift.getStatus() != conf.getCond().get(0)) { // 不是被击飞的指定次数
                        continue;
                    }
                }
                // 未触发
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
            LogUtil.debug("推送触发式礼包" + player.roleId);
        } catch (MwException mwException) {
            LogUtil.error(mwException);
        } catch (Exception e) {
            LogUtil.error("触发式礼包Error:", e);
        }

    }

    /**
     * 触发式礼包--首充礼包推送
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
            LogUtil.debug("推送首充礼包" + player.roleId);

        } catch (Exception e) {
            LogUtil.error("推送首充礼包Error:", e);
        }
    }

    /**
     * 触发式礼包推送根据礼包Id
     *
     * @param giftId
     * @param player
     */
    public void checkTriggerGiftSyncByGiftId(int giftId, Player player) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        // 根据礼包id获取配置
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        // 获取TriggerGift对象
        TriggerGift triggerGift = activityDataManager.getTriggerGiftInfoByGiftId(player, giftId, true);
        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        SyncTriggerGiftRs.Builder builder = SyncTriggerGiftRs.newBuilder();
        if (CheckNull.isNull(triggerGiftConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // 未触发
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
        LogUtil.debug("推送触发式礼包" + player.roleId);
    }

    /**
     * 时间触发式礼包推送根据礼包Id
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
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // 未触发
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
        LogUtil.debug("推送触发式礼包" + player.roleId);
    }

    /**
     * 针对不在线用户
     */
    public void putPlayerOfflineTriggerGift(int giftId, int triggerPlanId, Player player) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftId);
        TriggerGift triggerGift = activityDataManager.getTimeTriggerGiftInfo(player, giftId, triggerPlanId, true);
        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        if (CheckNull.isNull(triggerGiftConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // 未触发
        if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
            triggerGift.setBeginTime(now);
            triggerGift.setEndTime(now + conf.getContinueTime());
            triggerGift.setState(ActivityConst.TRIGGER_STATUS);
        }
    }

    /**
     * 上线时获取所有触发时礼包
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetTriggerGiftRs GetTriggerGift(GetTriggerGiftRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 现在的服务器时间
        int now = TimeHelper.getCurrentSecond();
        GetTriggerGiftRs.Builder builder = GetTriggerGiftRs.newBuilder();
        List<CommonPb.TriggerGiftInfo> triggerGiftInfos = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, TriggerGift>> triggerGifts : player.triggerGifts.entrySet()) {
            // 已经触发的礼包, 不判断触发条件, 有可能出现玩家跨了等级, VIP等情况
            List<StaticTriggerConf> triggerConf = StaticActivityDataMgr.getTriggerGiftConfById(triggerGifts.getKey(), player, false);
            // 获取触发式礼包信息
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
        // 触发式礼包排序
        triggerGiftInfos = triggerGiftInfos.stream().sorted(Comparator.comparingInt(TriggerGiftInfo::getLastTime)).collect(Collectors.toList());
        builder.addAllTriggerGiftInfo(triggerGiftInfos);
        return builder.build();
    }

    // ==================================触发式礼包end==================================

    /**
     * 通知活动列表数据有改变
     */
    public void syncActListChg() {
        LogUtil.debug("----------活动列表数据发生改变推送开始------------");
        playerDataManager.getPlayers().values().stream().filter(p -> p.isLogin && p.ctx != null).forEach(player -> {
            SyncActListChgRs.Builder builder = SyncActListChgRs.newBuilder();
            Base.Builder msg = PbHelper.createRsBase(SyncActListChgRs.EXT_FIELD_NUMBER, SyncActListChgRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        });
        LogUtil.debug("----------活动列表数据发生改变推送完毕------------");
    }

    /**
     * 每个活动开始的时候触发
     *
     * @param actType
     */
    public void onActBegin(int actType) {
        if (ActivityConst.ACT_CAMP_FIGHT_RANK == actType) {
            activityDataManager.initAndLoadActCampFightRank();
            LogUtil.debug("-------初始化阵营战斗力排行 actType:", actType);
        }
    }

    /**
     * 每日特惠活动获取
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetDayDiscountsRs getDayDiscounts(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_DAY_DISCOUNTS);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        // 需要记录当天第一次获取的等级
        final int lvKey = ActivityConst.ACT_DAYDICOUNTS_LV_KEY;
        final int rankey = ActivityConst.ACT_DAYDICOUNTS_RANK_KEY;

        Long saveLv = activity.getStatusCnt().get(lvKey);
        final int actLv = saveLv == null ? player.lord.getLevel() : saveLv.intValue();// 等级
        activity.getStatusCnt().put(lvKey, (long) actLv);

        Long saveRank = activity.getStatusCnt().get(rankey);
        final int actRank = saveRank == null ? player.lord.getRanks() : saveRank.intValue();// 军阶
        activity.getStatusCnt().put(rankey, (long) actRank);

        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (CheckNull.isEmpty(condList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "活动奖励未配置, roleId:,", roleId);
        }
        GetDayDiscountsRs.Builder builder = GetDayDiscountsRs.newBuilder();
        StaticActAward actAward = condList.stream()
                .filter(s -> s.getParam().get(0) <= actLv && actLv <= s.getParam().get(1)).findFirst().orElse(null);
        if (actAward != null) {
            int freeKey = 0;// 免费奖励领取状态存在0位置
            int status = activity.getStatusMap().containsKey(freeKey) ? 1 : 0;// 是否已经领过奖
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
     * 大富翁获取
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        int getType = req.getGetType();// 0. 获取所有信息, 1. 不会返回格子数据
        long todayPayGold = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY,
                0L); // 今日充值金币数量
        long hasCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, 0L);// 今日拥有玩色子的次数

        final int curRound = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, 1)
                .intValue(); // 当前第几轮
        int curGrid = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, 0)
                .intValue();// 当前在第格
        // int lastPayTime = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, 0);//
        // 上一次充值是时间

        GetMonopolyRs.Builder builder = GetMonopolyRs.newBuilder();
        if (0 != getType) {
            int actId = activity.getActivityId();
            List<StaticActMonopoly> sActMonoplyList = StaticActivityDataMgr.getActMonopolyListByActId(actId);
            if (CheckNull.isEmpty(sActMonoplyList)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "大富翁活动为配置, roleId:,", roleId, ", activityId:",
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
        // true表示昨天有充值
        builder.setIsYesterdayPay(activityDataManager.checkMonopolyYesterday(activity));
        return builder.build();
    }

    /**
     * 大富翁摇色子
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:", roleId);
        }
        // 检测解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.MONOPOLY_LOCK_ID)) { // 判断是否解锁
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "大富翁功能未解锁不能玩  roleId:", roleId);
        }
        long hasCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, 0L);// 今日拥有玩色子的次数
        if (hasCnt < 1) {
            throw new MwException(GameError.ACT_MONOPOLY_CNT_NOT_ENOUGH.getCode(), "大富翁次数不足不能玩  roleId:", roleId);
        }
        // 当前第几轮
        int round = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, 1)
                .intValue();
        // 获取轮的数据
        int actId = activity.getActivityId();
        List<StaticActMonopoly> sActMonoplyList = StaticActivityDataMgr.getActMonopolyListByActId(actId);
        if (CheckNull.isEmpty(sActMonoplyList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "大富翁活动未配置, roleId:,", roleId, ", activityId:", actId);
        }
        List<StaticActMonopoly> sActMonopolyByRoundList = sActMonoplyList.stream()
                .filter(sam -> sam.getRound() == round).collect(Collectors.toList());
        if (CheckNull.isEmpty(sActMonopolyByRoundList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "大富翁活动未配置, roleId:,", roleId, ", activityId:", actId,
                    ", round:", round);
        }
        final int maxGrid = sActMonopolyByRoundList.stream().max(Comparator.comparingInt(StaticActMonopoly::getGrid))
                .get().getGrid();
        // 计算点数
        List<Integer> cntAndWeight = RandomUtil.getWeightByList(ActParamConstant.ACT_MONOPOLY_CNT_WEIGHT,
                l -> l.get(1));
        final int startGrid = activity.getSaveMap().getOrDefault(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, 0)
                .intValue();// 当前在第格
        final int points = cntAndWeight.get(0);// 得出的点数
        final int endGrid = startGrid + points;// 现在走到的位置
        // 中途经过的奖励
        List<StaticActMonopoly> throughActMonopoly = sActMonopolyByRoundList.stream()
                .filter(sam -> startGrid < sam.getGrid() && sam.getGrid() <= endGrid).collect(Collectors.toList());
        // 拥有次数 -1
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_HASCNT_KEY, hasCnt - 1);
        // 已玩次数 +1
        Long playCnt = activity.getStatusCnt().getOrDefault(ActivityConst.ActMonopolyKey.STATUSCNT_PLAY_CNT_KEY, 0L);
        activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_PLAY_CNT_KEY, playCnt + 1);

        PlayMonopolyRs.Builder builder = PlayMonopolyRs.newBuilder();
        // 发奖励
        for (StaticActMonopoly sam : throughActMonopoly) {
            builder.addAllAward(
                    rewardDataManager.addAwardDelaySync(player, sam.getAward(), null, AwardFrom.PLAY_MONOPOLY));
            // 发聊天消息
            if (sam.getChatId() > 0) {
                chatDataManager.sendSysChat(sam.getChatId(), player.lord.getCamp(), 0, player.lord.getNick(),
                        sam.getAward().get(0).get(0), sam.getAward().get(0).get(1), sam.getAward().get(0).get(2));
            }
        }
        LogLordHelper.commonLog("playMonopoly", AwardFrom.PLAY_MONOPOLY, player, round, points, endGrid);

        int curRound = round; // 当前轮数
        int curGrid = endGrid; // 当前格数
        if (endGrid >= maxGrid) {// 说明需要换下一个棋盘
            curGrid = maxGrid;
            final int nextRound = round + 1; // 下一轮
            List<StaticActMonopoly> nextsActMonopolyByRoundList = sActMonoplyList.stream()
                    .filter(sam -> sam.getRound() == nextRound).collect(Collectors.toList());
            if (!CheckNull.isEmpty(nextsActMonopolyByRoundList)) { // 有下一轮的情况
                curRound = nextRound;
                curGrid = 0; // 换棋盘 ,格数清0
                List<MonopolyGrid> mgList = nextsActMonopolyByRoundList.stream()
                        .sorted(Comparator.comparingInt(StaticActMonopoly::getGrid))
                        .map(sa -> PbHelper.createMonopolyGridPb(sa)).collect(Collectors.toList());
                builder.addAllGrid(mgList);
                activity.getStatusCnt().put(ActivityConst.ActMonopolyKey.STATUSCNT_TODAY_PAY_KEY, 0L); // 清空充值金额
            }
        }
        activity.getSaveMap().put(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_ROUND_KEY, curRound);// 当前轮更新
        activity.getSaveMap().put(ActivityConst.ActMonopolyKey.SAVEMAP_CUR_GRID_KEY, curGrid);// 当前格数更新
        builder.setPoints(points);
        builder.setCurGrid(curGrid);
        builder.setCurRound(curRound);
        return builder.build();
    }

    /***
     * 三倍返利活动
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:", roleId);
        }
        player.activitys.put(ActivityConst.ACT_THREE_REBATE, activity);// 每次加载活动将活动加到个人身上
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
     * 许愿池许愿 </br>
     * 许愿池进度会存储到 activity的statusCnt中; key值为0,value为 当前已领取的次数</br>
     * StaticActAward#param值的说明 [1,188,288,110], 0位置 表示第几次许愿, 1位置和2位置 给予的金币奖励在188~288之间区间 ,3位置表示获得头像的万分比概率
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public WishingRs wishing(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_WISHING_WELL);

        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "许愿池活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "许愿池活动未开启, roleId:,", roleId);
        }
        int activityId = activityBase.getActivityId();
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityId);
        if (CheckNull.isEmpty(actAwardList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "许愿池配置未找到 roleId:,", roleId, ", activityId:",
                    activityId);
        }
        // 获取自己的已经许愿的次数
        int curCnt = activity.getStatusCnt()
                .getOrDefault(ActivityConst.ActWishingWellKey.STATUSCNT_WISHING_CUR_CNT_KEY, 0L).intValue();
        final int nextCnt = curCnt + 1;
        StaticActAward nextActAward = actAwardList.stream().filter(
                aa -> !CheckNull.isEmpty(aa.getParam()) && aa.getParam().size() >= 3 && aa.getParam().get(0) == nextCnt)
                .findFirst().orElse(null);
        if (nextActAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "许愿池配置未找到(未找到对应次数配置) roleId:,", roleId,
                    ", activityId:", activityId, ", nextCnt:", nextCnt);
        }
        final int neeCostGold = nextActAward.getCond();// 需要消耗的金币
        int min = nextActAward.getParam().get(1);
        int max = nextActAward.getParam().get(2);
        if (min > max) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "许愿池配置获得配置出错 roleId:,", roleId,
                    ", activityId:", activityId, ", keyId:", nextActAward.getKeyId());
        }
        // 随机金币数量
        int awardGoldCnt = RandomHelper.randomInArea(min, max + 1);
        // 检查玩家资源是否足够, 并扣除
        rewardDataManager.checkPlayerResIsEnough(player, AwardType.MONEY, AwardType.Money.GOLD, neeCostGold);
        rewardDataManager.subGold(player, neeCostGold, false, AwardFrom.WISHING_WELL_ACT);
        // 改进度
        activity.getStatusCnt().put(ActivityConst.ActWishingWellKey.STATUSCNT_WISHING_CUR_CNT_KEY, (long) nextCnt);
        activity.getStatusMap().put(nextActAward.getKeyId(), 1);
        // 给奖励
        WishingRs.Builder builder = WishingRs.newBuilder();
        builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, awardGoldCnt,
                AwardFrom.WISHING_WELL_ACT));
        // 给额外奖励
        int extraAwardStatus = activity.getStatusMap()
                .getOrDefault(ActivityConst.ActWishingWellKey.STATUSMAP_WISHING_EXTRA_AWARD_KEY, 0).intValue(); // 额外奖励的状态
        if (extraAwardStatus != 1) {
            if (nextActAward.getParam().size() >= 4) {
                int w = nextActAward.getParam().get(3).intValue(); // 万分比的概率
                if (RandomHelper.isHitRangeIn10000(w)) {
                    activity.getStatusMap().put(ActivityConst.ActWishingWellKey.STATUSMAP_WISHING_EXTRA_AWARD_KEY, 1);
                    List<Award> awardExra = rewardDataManager.addAwardDelaySync(player,
                            ActParamConstant.ACT_WISHING_AWARD, null, AwardFrom.WISHING_WELL_ACT);
                    chatDataManager.sendSysChat(ChatConst.CHAT_WISHING_AWARD, player.lord.getCamp(), 0,
                            player.lord.getNick()); // 发送跑马灯
                    builder.addAllAward(awardExra);
                }
            }
        }
        builder.setRemainGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 获取特殊活动
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
     * 幸运奖池
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
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "幸运奖池, roleId:,", roleId);
        }

        activityDataManager.updateLuckyPoolLive(player, player.getDailyTaskLivenss(), activity);

        GetLuckyPoolRs.Builder builder = GetLuckyPoolRs.newBuilder();

        Integer cnt = activity.getSaveMap().get(1);
        builder.setPaySum(cnt == null ? 0 : cnt);
        builder.setPoolCnt(globalActivity.getGoal());

        // 活跃度次数
        int liveCount = activity.getSaveMap().getOrDefault(0, 0);
        // 充值次数
        int payCount = activity.getSaveMap().getOrDefault(1, 0) / ActParamConstant.LUCKY_POOL_1.get(2);

        // 已抽取活跃度次数
        int costLiveCount = activity.getSaveMap().getOrDefault(2, 0);
        // 已抽取充值次数
        int costPayCount = activity.getSaveMap().getOrDefault(3, 0);

        builder.setRemainCnt(liveCount + payCount - costLiveCount - costPayCount);

        List<StaticTurnplateConf> turnplateConfs = StaticActivityDataMgr
                .getActTurnPlateListByActId(activity.getActivityId());
        if (CheckNull.isEmpty(turnplateConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 幸运/名将 转盘找不到配置, roleId:", roleId);
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
     * 幸运奖池抽奖
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public PlayLuckyPoolRs playLuckyPool(long roleId, int turnplateId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticTurnplateConf turnplateConf = StaticActivityDataMgr.getActTurnPlateById(turnplateId);
        if (CheckNull.isNull(turnplateConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 幸运奖池找不到配置, roleId:", roleId);
        }

        if (turnplateConf.getType() != ActivityConst.ACT_LUCKY_POOL) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "幸运奖池 未开启, roleId:,", roleId);
        }

        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LUCKY_POOL);
        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(ActivityConst.ACT_LUCKY_POOL);
        if (activity == null || globalActivity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "幸运奖池 未开启, roleId:,", roleId);
        }

        // 活跃度次数
        int liveCount = activity.getSaveMap().getOrDefault(0, 0);
        // 充值次数
        int payCount = activity.getSaveMap().getOrDefault(1, 0) / ActParamConstant.LUCKY_POOL_1.get(2);

        // 已抽取活跃度次数
        int costLiveCount = activity.getSaveMap().getOrDefault(2, 0);
        // 已抽取充值次数
        int costPayCount = activity.getSaveMap().getOrDefault(3, 0);
        // 检测剩余次数
        if (turnplateConf.getCount() > liveCount + payCount - costLiveCount - costPayCount) {
            throw new MwException(GameError.ACT_PAY_TURNPLATE_CNT_NOT_ENOUGH.getCode(), "幸运奖池 次数不足, roleId:,", roleId);
        }

        int type = 0;
        int id = 0;
        int count = 0;
        int poolCount = globalActivity.getGoal();
        List<Integer> rList = null;
        PlayLuckyPoolRs.Builder builder = PlayLuckyPoolRs.newBuilder();
        for (int i = 0; i < turnplateConf.getCount(); i++) {
            // 优先使用 活跃度次数，活跃度次数抽奖不会抽中奖池
            if (i < liveCount - costLiveCount) {
                rList = RandomUtil.getWeightByList(turnplateConf.getAwardList().stream()
                        .filter(l -> l.get(0) != AwardType.MONEY_PECENT).collect(Collectors.toList()), l -> l.get(3));
                costLiveCount++;
            } else {
                rList = RandomUtil.getWeightByList(turnplateConf.getAwardList(), l -> l.get(3));
                costPayCount++;
            }
            if (rList == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "幸运奖池 奖励异常, roleId:,", roleId);
            }
            type = rList.get(0);
            id = rList.get(1);
            count = rList.get(2);
            // 金币从奖池中获取
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

        // 修改奖池基数
        globalActivity.setGoal(poolCount);
        activityDataManager.syncAllPlayerActChange(player, ActivityConst.ACT_LUCKY_POOL);

        // 修改抽奖次数
        activity.getSaveMap().put(2, costLiveCount);
        activity.getSaveMap().put(3, costPayCount);

        builder.setPoolCnt(poolCount);
        return builder.build();
    }

    /**
     * 幸运奖池
     *
     * @return
     * @throws MwException
     */
    public GetLuckyPoolRankRs getLuckyPoolRank(long date) throws MwException {
        GlobalActivityData globalActivity = activityDataManager.getGlobalActivity(ActivityConst.ACT_LUCKY_POOL);
        if (globalActivity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "幸运奖池未开启");
        }

        return globalActivity.getSimpleRankPb(date).build();
    }

    public Base.Builder shareRewardProcess(GetActivityAwardRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int keyId = req.getKeyId();// 获取奖励id
        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);// 通过奖励获取奖励Award
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }
        Activity activity = player.activitys.get(ActivityConst.ACT_SHARE_REWARD);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        Integer awardStatus = activity.getStatusMap().get(keyId);
        if (awardStatus != null && awardStatus != 0) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId);
        }
        HttpPb.ShareRewardRq.Builder builder = HttpPb.ShareRewardRq.newBuilder().setKeyId(player.account.getAccountKey()).setLordId(player.roleId).setPlatNo(player.account.getPlatNo()).setAwardId(keyId);

        // 向账号服请求分享数据
        return PbHelper.createRqBase(HttpPb.ShareRewardRq.EXT_FIELD_NUMBER, null, HttpPb.ShareRewardRq.ext,
                builder.build());
    }

    public void shareRewardRs(HttpPb.ShareRewardRs req) throws MwException {
        String result = req.getResult();
        // 获取奖励id
        int keyId = req.getKeyId();
        long roleId = req.getLordId();

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);// 通过奖励获取奖励Award
        if (actAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }
        Activity activity = player.activitys.get(ActivityConst.ACT_SHARE_REWARD);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }

        List<List<Integer>> awardList = actAward.getAwardList();
        // 检测背包是否已满
        rewardDataManager.checkBag(player, awardList);

        JSONObject resJson = JSONObject.parseObject(result);
        int cnt = resJson.getInteger("count");
        // 领取条件
        if (cnt >= actAward.getCond()) {
            LogUtil.debug("GetActivityAwardRs activity=" + activity);
            activity.getStatusMap().put(keyId, 1);

            int awardCnt = 1;
            GetActivityAwardRs.Builder builder = GetActivityAwardRs.newBuilder();
            int size = awardList.size();
            // 获取活动翻倍
            int num = activityDataManager.getActDoubleNum(player);
            for (int i = 0; i < awardList.size(); i++) {
                List<Integer> e = awardList.get(i);
                int type = e.get(0);
                int itemId = e.get(1);
                int count = e.get(2);
                count *= num;// 翻倍活动
                count *= awardCnt;// 奖励次数
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
     * 观看广告领取奖励(服务器没办法校验)
     *
     * @param roleId 玩家id
     * @return 广告奖励的返回协议
     * @throws MwException 自定义异常
     */
    public AdvertisementRewardRs advertisementReward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int cnt = player.getMixtureDataById(PlayerConstant.DAILY_ADVERTISEMENT_REWARD);

        int count = cnt + 1;
        if (count > Constant.ADVERTISEMENT_REWARD.size()) {
            throw new MwException(GameError.ADVERTISEMENT_REWARD_COUNT.getCode(), "当天领取广告奖励达到上限, roleId:", roleId);
        }

        AdvertisementRewardRs.Builder builder = AdvertisementRewardRs.newBuilder();
        List<Integer> reward = Constant.ADVERTISEMENT_REWARD.get(cnt);
        if (!CheckNull.isEmpty(reward)) {
            player.setMixtureData(PlayerConstant.DAILY_ADVERTISEMENT_REWARD, count);
            // 奖励不为null
            builder.addAward(rewardDataManager.sendRewardSignle(player, reward.get(0), reward.get(1), reward.get(2), AwardFrom.ADVERTISEMENT_REWARD));
            builder.setCount(count);
        }
        return builder.build();
    }

    /**
     * 微信签到奖励
     *
     * @param req 请求参数
     * @throws MwException 自定义异常
     */
    public void webChatSignReward(HttpPb.WeChatSignRewardRq req) throws MwException {
        long roleId = req.getLordId();
        int days = req.getDays();

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Activity activity = player.activitys.get(ActivityConst.ACT_WECHAT_SIGNIN);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId);
        }
        List<StaticActAward> sActAwards = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (CheckNull.isEmpty(sActAwards)) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "活动配置错误, roleId:,", roleId);
        }

        Date now = new Date();
        //今天已经领取过签到奖励
        int lastSignTime = activity.getEndTime();
        boolean isTodaySign = DateHelper.dayiy(TimeHelper.secondToDate(lastSignTime), now) == 1;
        if (isTodaySign) {
            throw new MwException(GameError.SIGNATURE_ERR.getCode(), String.format("roleId :%d, days :%d, 今天已经签到过了, 签到时间 :%d", roleId, days, lastSignTime));
        }

        // 领奖记录
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        int signTime = statusMap.getOrDefault(days, 0);//上次签到时间
        int subDays = DateHelper.dayiy(TimeHelper.secondToDate(signTime), now);
        //2022-01-13 策划需求.  重新签到的天数,与距离上次签到相隔的天数需要保持一致,
        // eg: 第1天签到距离上次第1天签到需要相隔1天, 第2天签到距离上次第2天签到需要相隔2天, ...第7天签到需要距离上次第7天签到7天
        if (subDays <= days) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), String.format("days :%d, 奖励已经领取过了, roleId: %d, signTime :%d", days, roleId, signTime));
        }
        int nowSec = (int) (now.getTime() / 1000);
        // 签到奖励
        List<StaticActAward> signAward = sActAwards.stream().filter(sca -> sca.getParam().get(0) == 0 && sca.getCond() == days).collect(Collectors.toList());
        if (days == 7) {
            // 最后一天
            signAward.add(sActAwards.stream().filter(sca -> sca.getParam().get(0) == 1).findFirst().orElse(null));
        }
        List<Award> awardList = signAward.stream().filter(Objects::nonNull).flatMap(aw -> aw.getAwardList().stream().map(al -> PbHelper.createAwardPb(al.get(0), al.get(1), al.get(2)))).collect(Collectors.toList());
        if (!CheckNull.isEmpty(awardList)) {
            // 记录签到状态
            statusMap.put(days, nowSec);
            activity.setEndTime(nowSec);
            // 奖励邮件
            mailDataManager.sendAttachMail(player, awardList, MailConstant.MOLD_WECHAT_SIGN_REWARD, AwardFrom.ACT_SUPPLY_DORP_RETURN, nowSec);
        } else {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "活动配置错误, roleId :", roleId, " days :", days);
        }
    }

    public void autoConverActItems(int actType,String jobKey) {
        if (actType == ActivityConst.ACT_CHRISTMAS || actType == ActivityConst.ACT_REPAIR_CASTLE) {
            activityChristmasService.overAutoConver(jobKey);
        }
    }

}
