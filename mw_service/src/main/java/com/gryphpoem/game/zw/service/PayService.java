package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.GamePb3.GetPaySerialIdRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetPaySerialIdRs;
import com.gryphpoem.game.zw.pb.GamePb3.SyncPaybackSucRs;
import com.gryphpoem.game.zw.pb.HttpPb.PayApplyRq;
import com.gryphpoem.game.zw.pb.HttpPb.PayBackRq;
import com.gryphpoem.game.zw.pb.HttpPb.PayConfirmRq;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.dao.impl.p.PayDao;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.AbsGiftBagActivityService;
import com.gryphpoem.game.zw.service.activity.ActivityBoxOfficeService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.activity.anniversary.ActivitySkinEncoreService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PayService {

    /**
     * 金币充值
     */
    public static final int FLAG_PAY_GOLD = 0;
    /**
     * 月卡充值
     */
    public static final int FLAG_PAY_MONTH = 1;
    /**
     * 充值礼包
     */
    public static final int FLAG_PAY_GIFT = 3;
    /**
     * 触发式礼包
     */
    public static final int FLAG_PAY_TRIGGER_GIFT = 4;
    /**
     * 每日特惠充值
     */
    public static final int FLAG_PAY_DAY_DISCOUNTS = 5;
    /**
     * 三倍返利活动
     */
    public static final int FLAG_PAY_THREE_REBATE = 6;
    /**
     * 功能卡充值
     */
    public static final int FLAG_PAY_FUN_CARD = 7;
    /**
     * 网页大额充值
     */
    public static final int FLAG_PAY_GOLD_WEB = 8;
    /**
     * 战令支付进阶
     */
    public static final int FLAG_PAY_BATTLE_PASS = 9;

    /**
     * 首充礼包
     */
    public static final int FLAG_PAY_FIRST_PAY = 10;

    /**
     * 建筑礼包
     */
    public static final int FLAG_BUILD_GIFT_PAY = 11;

    /**
     * 特工礼包
     */
    public static final int FLAG_AGENT_GIFT_PAY = 12;

    /**
     * 皮肤返场活动(充值送皮肤)
     */
    public static final int SKIN_ENCORE_PAY = 13;

    /**
     * 音乐活动解锁售票处
     */
    public static final int FLAG_UNLOCK_BOX_OFFICE = 14;

    /**
     * 神兵宝具礼包
     */
    public static final int MAGIC_TREASURE_WARE_GIFT_BAG = 15;


    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private PayDao payDao;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private WorldWarSeasonDailyRestrictTaskService dailyRestrictTaskService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private PropService propService;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private ActivitySkinEncoreService activitySkinEncoreService;
    @Autowired
    private ActivityBoxOfficeService activityBoxOfficeService;

    private static int REWARD_ALL_PLAY_LAST_TIME = 0;
    /**
     * 支付时活动额度系数
     */
    private final static int PAY_ACT_RATIO = 10;

    private boolean addPayGold(Player target, int topup, int extraGold, String serialId, int price, AwardFrom from) {
        if (topup <= 0) {
            return false;
        }
        Lord lord = target.lord;

        lord.setGold(lord.getGold() + topup + extraGold);
        lord.setGoldGive(lord.getGoldGive() + topup + extraGold);
        // 记录金币走向
        LogLordHelper.gold(from, target.account, lord, topup + extraGold, topup);

        // 全服累计充值活动
        if (globalDataManager.getGameGlobal().getTrophy() != null) {
            globalDataManager.getGameGlobal().getTrophy().getGold().addAndGet(topup);
        }

        // activityDataManager.updActivity(target, ActivityConst.ACT_CHARGE_TOTAL, topup, 0, true);
        activityDataManager.updActivity(target, ActivityConst.ACT_CHARGE_CONTINUE, price, 0, true);
//        activityDataManager.updActivity(target, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, price, 0, true);

        // 幸运奖池
        activityDataManager.updActivity(target, ActivityConst.ACT_LUCKY_POOL, topup, 0, true);
        activityDataManager.updGlobalActivity(target, ActivityConst.ACT_LUCKY_POOL, topup, 0);
        return true;
    }

    public PayConfirmRq payBack(final PayBackRq req, long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        return payLogic(req, player);
    }

    /**
     * GM充值,只会获得进度,不会发礼包或金币
     *
     * @param player
     * @param payT
     * @param type
     */
    public void gmPayNoEarn(Player player, long payT, int type) {
        gmPayDispatch(player, payT, type, false);
    }

    /**
     * GM充值
     *
     * @param player
     * @param payT
     * @param type
     */
    public void gmPay(Player player, long payT, int type) {
        gmPayDispatch(player, payT, type, true);
    }

    /**
     * gm充值
     *
     * @param player
     * @param payT
     * @param type
     * @param hasEarn 是否有收益 true有收益
     */
    private void gmPayDispatch(Player player, long payT, int type, boolean hasEarn) {
        int payType = (int) payT;
        Date nowDate = new Date();
        Pay pay = new Pay();
        int platNo = hasEarn ? 10086 : 10010;
        pay.setPlatNo(platNo);
        pay.setPlatId(String.valueOf(platNo));
        pay.setOrderId(UUID.randomUUID().toString());
        pay.setSerialId("gm_" + UUID.randomUUID().toString());
        pay.setServerId(player.account.getServerId());
        pay.setRoleId(player.lord.getLordId());
        pay.setAmount(0);
        pay.setPayTime(nowDate);
        pay.setOrderTime(nowDate);
        pay.setState(1);
        pay.setPayType(payType);

        // payDao.createPay(pay);
        // 获取支付类型
        StaticPay sPay = null;
        sPay = StaticVipDataMgr.getStaticPayByPayId(payType);

        if (null == sPay) {
            LogUtil.error("GM 【支付回调】没有此支付类型");
        }
        // 所有的充值处理
        allPayProcess(player, pay, sPay);
        if (hasEarn) {
            dispatchPay(player, pay, sPay, type);
        } else {
            // vip经验
            vipProcess(player, sPay);
            if (3 == sPay.getBanFlag()) {
                // 同步礼包数据
                activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_VIP_BUY, 1);
            }
        }
    }

    /**
     * 真实的充值
     *
     * @param req
     * @param player
     * @return
     */
    private PayConfirmRq payLogic(final PayBackRq req, Player player) {
        if (player == null) {
            return null;
        }

        Pay pay = payDao.selectPay(req.getPlatNo(), req.getOrderId());
        if (pay != null) {
            // 说明该订单已经发货
            return null;
        }
        pay = new Pay();
        // 补全数据,保存
        Date nowDate = new Date();
        pay.setPlatNo(req.getPlatNo());
        pay.setPlatId(req.getPlatId());
        pay.setOrderId(req.getOrderId());
        pay.setSerialId(req.getSerialId());
        pay.setServerId(req.getServerId());
        pay.setRoleId(req.getRoleId());
        pay.setAmount(req.getAmount());
        if (req.hasUsd()) {
            pay.setUsd(req.getUsd());
        }
        pay.setPayTime(nowDate);
        pay.setOrderTime(nowDate);
        pay.setState(1);
        pay.setPayType(req.getPayType());
        payDao.createPay(pay);
        player.addPaySumAmount(req.getAmount());
        // 获取支付类型
        int payType = pay.getPayType();
        int platCode = pay.getPlatNo();
        // StaticPayPlat plat = StaticVipDataMgr.getPayPlatByCode(platCode);
        /*if (plat == null) {
            LogUtil.error("【支付回调】没有此支付平台 roleId:", req.getRoleId(), " platNo:", req.getPlatNo());
            return null;
        }*/
        StaticPay sPay = StaticVipDataMgr.getStaticPayByPayId(payType);
        if (null == sPay) {
            LogUtil.error("【支付回调】没有此支付类型 roleId:", req.getRoleId(), " payType:", req.getPayType(), ", platNo:",
                    req.getPlatNo());
            return null;
        }
        allPayProcess(player, pay, sPay);
        // 这里的platType默认传0， 为了兼容线上的玩家
        return dispatchPay(player, pay, sPay, 0);
    }

    /**
     * 分发支付
     *
     * @param player
     * @param pay
     * @param sPay
     * @param platType
     * @return
     */
    private PayConfirmRq dispatchPay(Player player, Pay pay, StaticPay sPay, int platType) {
        PayConfirmRq rs = null;
        // 购买类型
        int banFlag = sPay.getBanFlag();
        if (FLAG_PAY_GOLD == banFlag || FLAG_PAY_GOLD_WEB == banFlag) {
            // 金币或者网页大额充值
            rs = processPayGold(player, pay, sPay, platType);
        } else if (FLAG_PAY_MONTH == banFlag) {
            // 月卡
            rs = processPayMonth(player, pay, sPay);
        } else if (FLAG_PAY_GIFT == banFlag) {
            // 礼包
            rs = processPayGift(player, pay, sPay);
        } else if (FLAG_PAY_TRIGGER_GIFT == banFlag) {
            // 触发式礼包
            rs = processPayTriggerGift(player, pay, sPay);
        } else if (FLAG_PAY_DAY_DISCOUNTS == banFlag) {
            // 每日特惠充值
            rs = processPayDayDiscounts(player, pay, sPay);
        } else if (FLAG_PAY_THREE_REBATE == banFlag) {
            // 三倍返利活动
            rs = processPayThreeRebate(player, pay, sPay);
        } else if (FLAG_PAY_FUN_CARD == banFlag) {
            // 功能卡
            rs = processPayFunCard(player, pay, sPay);
        } else if (FLAG_PAY_BATTLE_PASS == banFlag) {
            // 战令进阶
            rs = processPayBattlePass(player, pay, sPay);
        } else if (FLAG_PAY_FIRST_PAY == banFlag) {
            //首充礼包
            rs = processPayFirstPay(player, pay, sPay);
        } else if (FLAG_BUILD_GIFT_PAY == banFlag) {
            // 建筑礼包
            rs = processPayBuildGift(player, pay, sPay);
        } else if (FLAG_AGENT_GIFT_PAY == banFlag) {
            // 特工礼包
            rs = processPayAgentGift(player, pay, sPay);
        } else if (SKIN_ENCORE_PAY == banFlag) {
            rs = processPaySkinEncoreActivity(player, pay, sPay);
        } else if (FLAG_UNLOCK_BOX_OFFICE == banFlag) {
            rs = processPayBoxOfficeActivity(player, pay, sPay);
        } else if (MAGIC_TREASURE_WARE_GIFT_BAG == banFlag) {
            rs = processActGiftBgActivity(player, pay, sPay);
        }
        return rs;
    }

    /**
     * 购买活动礼包
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processActGiftBgActivity(Player player, Pay pay, StaticPay sPay) {
//        //充值获得钻石处理
//        if (sPay.getTopup() > 0) {
//            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup(),
//                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), pay.getAmount());
//        }
        // vip处理
        if (sPay.getVipexp() > 0) {
            vipProcess(player, sPay);
        }
        AbsGiftBagActivityService.buyActGiftBag(player, sPay);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), sPay.getTopup(), sPay.getPayId());
        return payBackReturn(player, pay, sPay, sPay.getTopup());
    }

    private PayConfirmRq processPayBoxOfficeActivity(Player player, Pay pay, StaticPay sPay) {
        //充值获得钻石处理
        if (sPay.getTopup() > 0) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup(),
                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), pay.getAmount());
        }
        // vip处理
        if (sPay.getVipexp() > 0) {
            vipProcess(player, sPay);
        }
        activityBoxOfficeService.pay4BoxOffice(player, pay, sPay);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), sPay.getTopup(), sPay.getPayId());
        return payBackReturn(player, pay, sPay, sPay.getTopup());
    }


    /**
     * 皮肤返场活动
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPaySkinEncoreActivity(Player player, Pay pay, StaticPay sPay) {
        //充值获得钻石处理
        if (sPay.getTopup() > 0) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup(),
                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), pay.getAmount());
        }
        // vip处理
        if (sPay.getVipexp() > 0) {
            vipProcess(player, sPay);
        }
        //获得皮肤处理
        activitySkinEncoreService.pay4SkinEncore(player, pay, sPay);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), sPay.getTopup(), sPay.getPayId());
        return payBackReturn(player, pay, sPay, sPay.getTopup());
    }

    private PayConfirmRq processPayAgentGift(Player player, Pay pay, StaticPay sPay) {
        int giftPackId = sPay.getPayId(); // 获取充值段位

        StaticActGiftpack giftPack = StaticActivityDataMgr.getActGiftpackMapById(giftPackId);
        if (giftPack == null) {
            LogUtil.error("未找到此礼包配置  giftPackId:", giftPackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }

        Optional.ofNullable(player.getCia())
                .ifPresent(cia -> {
                    Map<Integer, FemaleAgent> femaleAngets = cia.getFemaleAngets();
                    Optional.ofNullable(StaticCiaDataMgr.getAgentUnlockMap())
                            .ifPresent(unlockMap -> {
                                unlockMap.entrySet().stream()
                                        .filter(en -> {
                                            List<Integer> unlockConf = en.getValue();
                                            if (!CheckNull.isEmpty(unlockConf)) {
                                                int unlockType = unlockConf.get(0);
                                                int cond = unlockConf.get(1);
                                                return unlockType == CiaConstant.AGENT_UNLOCK_TYPE_2 && cond == giftPackId;
                                            }
                                            return false;
                                        })
                                        .findAny()
                                        .ifPresent(en -> {
                                            int agentId = en.getKey();
                                            FemaleAgent agent = femaleAngets.computeIfAbsent(agentId, (k) -> new FemaleAgent(agentId));
                                            if (agent.getStatus() == CiaConstant.AGENT_UNLOCK_STATUS_0) {
                                                agent.setStatus(CiaConstant.AGENT_UNLOCK_STATUS_1);
                                            }
                                        });
                            });
                });


        // Vip的处理
        vipProcess(player, sPay);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        // 发奖励
        // mailDataManager.sendAttachMail(player, showAwards, MailConstant.MOLD_BUILD_GIFT_REWARD, AwardFrom.BUILD_GIFT_AWARD, TimeHelper.getCurrentSecond());

        // 直接同步奖励
        List<Award> awardList = rewardDataManager.sendReward(player, giftPack.getAward(), AwardFrom.AGENT_GIFT_AWARD);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftPackId, giftPackId);
        return payBackReturn(player, pay, sPay, 0);
    }


    private PayConfirmRq processPayBuildGift(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId(); // 获取充值段位

        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_BUILD_GIFT);
        if (activity == null) {
            LogUtil.error("未配置建筑礼包活动  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_BUILD_GIFT);
            return null;
        }
        List<StaticActAward> actAwards = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        List<Award> showAwards = new ArrayList<>();
        if (!CheckNull.isEmpty(actAwards)) {
            StaticActAward sActAward = actAwards.stream().filter(saa -> saa.getParam().get(0) == 1).findFirst().orElse(null);
            if (!CheckNull.isNull(sActAward)) {
                if (!CheckNull.isEmpty(sActAward.getAwardList())) {
                    for (List<Integer> sAward : sActAward.getAwardList()) {
                        if (!CheckNull.isNull(sAward)) {
                            if (sAward.get(0) == AwardType.PROP && sAward.get(1) >= 40000 && sAward.get(1) <= 40010) {
                                StaticProp staticProp = StaticPropDataMgr.getPropMap().get(sAward.get(1));
                                if (staticProp != null) {
                                    propService.processEffect(player, staticProp);
                                    propService.syncBuffRs(player, player.getEffect().get(EffectConstant.BUILD_CNT));
                                    activityDataManager.updActivity(player, ActivityConst.ACT_BUILD_GIFT, 1, 0, true);
                                }
                                activity.getStatusMap().put(sActAward.getKeyId(), 1);
                            } else {
                                showAwards.add(PbHelper.createAwardPb(sAward.get(0), sAward.get(1), sAward.get(2)));
                            }
                        }
                    }
                }
            }
        }
        // Vip的处理
        vipProcess(player, sPay);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        // 发奖励
        // mailDataManager.sendAttachMail(player, showAwards, MailConstant.MOLD_BUILD_GIFT_REWARD, AwardFrom.BUILD_GIFT_AWARD, TimeHelper.getCurrentSecond());

        // 直接同步奖励
        rewardDataManager.sendRewardByAwardList(player, showAwards, AwardFrom.BUILD_GIFT_AWARD);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_BUILD_GIFT_REWARD, showAwards, TimeHelper.getCurrentSecond());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * 战令进阶
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPayBattlePass(Player player, Pay pay, StaticPay sPay) {

        int topup = sPay.getTopup();
        int price = sPay.getEventpts();

        if (addPayGold(player, topup, 0, pay.getSerialId(), price, AwardFrom.PAY)) {
            // vip处理
            vipProcess(player, sPay);
            // 充值成功的同步数据
            syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), topup, sPay.getPayId());
            Optional.ofNullable(battlePassDataManager.getPersonInfo(player.roleId)).ifPresent(personInfo -> {
                // 设置已进阶战令
                int staticKey = personInfo.getStaticKey();
                personInfo.setAdvanced(1);
                int upLv = 10;
                // 进阶可以直接把战令等级提升到10级
                if (personInfo.getLv() < upLv) {
                    personInfo.setLv(upLv);
                    StaticBattlePassLv sPassLv = StaticBattlePassDataMgr.getLvAwardByPlanKey(staticKey, upLv - 1);
                    personInfo.setExp(CheckNull.isNull(sPassLv) ? 0 : sPassLv.getNeedExp());
                }
            });
            return payBackReturn(player, pay, sPay, topup);
        }
        return null;
    }

    private void allPayProcess(Player player, Pay pay, StaticPay sPay) {
        // 每日特惠不在充值进度之列
        // 2019-11-26 雷亦江说每日特惠要算上充值进度
        if (FLAG_PAY_DAY_DISCOUNTS == sPay.getBanFlag()) {
            return;
        }
        int actSchedule = sPay.getEventpts();
        // 首充礼包
        activityDataManager.firstPayAward(player, actSchedule);
        /** 累计充值 */
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_CHARGE, actSchedule);
        // 充值金额任务
        taskDataManager.updTask(player, TaskType.COND_PAY_46, actSchedule);
        /** 全军返利 */
        activityDataManager.updGlobalActivity(player, ActivityConst.ACT_ALL_CHARGE, actSchedule, player.lord.getCamp());
        // 每日充值
        activityDataManager.updActivity(player, ActivityConst.ACT_DAILY_PAY, actSchedule, 0, true);
        // 充值排行
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PAY_RANK, actSchedule);
        // 充值排行-新
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PAY_RANK_NEW, actSchedule);
        // 充值排行3版本
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PAY_RANK_V_3, actSchedule);
        // 合服充值排行
        activityDataManager.updRankActivity(player, ActivityConst.ACT_MERGE_PAY_RANK, actSchedule);
        // 充值有礼
        activityDataManager.updActivity(player, ActivityConst.ACT_GIFT_PAY, actSchedule, 0, true);
        // 充值转盘
        activityDataManager.updActivity(player, ActivityConst.ACT_PAY_TURNPLATE, actSchedule, 0, true);
        // 战令的钻石任务进度
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_GOLD_CNT, actSchedule);
        // 战令的钻石任务进度
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_GOLD_CNT, actSchedule);
        // 复活节活动
        activityDataManager.updActivity(player, ActivityConst.ACT_EASTER, actSchedule, 0, true);
        // 七日充值
        activityDataManager.updActivity(player, ActivityConst.ACT_PAY_7DAY, actSchedule, 0, true);
        // 累计充值
        activityDataManager.updActivity(player, ActivityConst.ACT_CHARGE_TOTAL, actSchedule, 0, true);
        // 合服每日连续充值,banFlag=5的不增加进度（原来=5的都不会处理）
        activityDataManager.updActivity(player, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, actSchedule, 0, true);
        //充值后的处理
        DataResource.getBeans(RechargeService.class).forEach(service -> service.afterRecharge(player, sPay.getPrice(), sPay.getEventpts()));

        //该地方活动最好都要try  catch 否则一个出现问题影响其他活动
        try {
            int price = sPay.getUsd() == 0.0 ? sPay.getPrice() : (int) Math.ceil(new BigDecimal(String.valueOf(sPay.getUsd())).doubleValue());
            // 记录最近一周的充值金额
            LocalDateTime now = LocalDateTime.now();
            int week = now.getDayOfWeek().getValue();
            // 历史充值
            int history = player.getMixtureDataById(week + PlayerConstant.RECENTLY_PAY);
            player.setMixtureData(week + PlayerConstant.RECENTLY_PAY, history + price);
            // 红包活动
            // activityRedPacketService.redPacketActivity(player,sPay.getPrice() );
        } catch (Exception e) {
            LogUtil.error(e);
        }

        try {
            // campService.addAndCheckPartySupply(player, PartyConstant.SupplyType.PAY_GOLD, sPay.getPrice());
        } catch (Exception e) {
            LogUtil.error("添加并且检测军团补给", e);
        }

    }

    /**
     * 触发式礼包
     *
     * @param player
     * @param pay
     * @param sPay
     */
    private PayConfirmRq processPayTriggerGift(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId();
        int gold = 0;
        StaticActGiftpack giftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);
        if (giftpack == null) {
            LogUtil.error("未找到此礼包配置  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        gold = giftpack.getTopup();
        StaticTriggerConf triggerConf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftpackId);
        if (CheckNull.isNull(triggerConf)) {
            LogUtil.error("未找到此触发礼包配置  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
        }
        int now = TimeHelper.getCurrentSecond();
        try {
            TriggerGift triggerGift = activityDataManager.getTriggerGiftInfoByGiftId(player, triggerConf.getGiftId(),
                    false);
            if (activityDataManager.checkGiftState(triggerGift, triggerConf, giftpack, now)) {
                triggerGift.maxCount();
                triggerGift.setState(ActivityConst.STATUS_HAS_GAIN);
            } else {
                // 礼包充值失败转换成金币
                return transformGold(player, pay, sPay, gold, giftpackId);
            }
        } catch (Exception e) {
            LogUtil.error(e, "触发式礼包充值失败");
            // 礼包充值失败转换成金币
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        // Vip的处理
        vipProcess(player, sPay);

        // 发奖励
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(giftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, now, giftpackId, giftpackId);

        // 直接同步奖励
        List<Award> awardList = rewardDataManager.sendReward(player, giftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // 同步礼包数据
        // activityService.syncGiftShow(player);

        // activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_VIP_BUY, 1);

        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * 首充礼包
     *
     * @param player
     * @param pay
     * @param sPay
     */
    private PayConfirmRq processPayFirstPay(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId();
        int gold = 0;
        StaticActGiftpack giftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);
        if (giftpack == null) {
            LogUtil.error("未找到首充礼包配置  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        gold = giftpack.getTopup();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_FIRSH_CHARGE);
        if (activity == null) {
            LogUtil.error("未配置首充礼包  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_FIRSH_CHARGE);
            // 转换成金币发放
            return transformGold(player, pay, sPay, gold, giftpackId);
        }

        StaticActAward sad = StaticActivityDataMgr.getActAwardById(giftpackId).get(0);
        if (sad == null) {
            LogUtil.error("未配置首充礼包奖励   giftId未能和s_act_award表对应  activityId:", giftpackId,
                    ActivityConst.ACT_FIRSH_CHARGE);
            // 转换成金币发放
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        int now = TimeHelper.getCurrentSecond();
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        if (statusMap.getOrDefault(sad.getKeyId(), 0) == 1) {
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        // 购买完成
        statusMap.put(sad.getKeyId(), 1);
        // 记录购买时间
        statusMap.put(0, now);
        // Vip的处理
        vipProcess(player, sPay);

        // 发奖励
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(sad.getAwardList()),
        //         MailConstant.MOLD_FIRST_PAY_AWARD, AwardFrom.FIRST_PAY_AWARD, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);
        // 直接同步奖励
        List<Award> awardList = rewardDataManager.sendReward(player, sad.getAwardList(), AwardFrom.FIRST_PAY_AWARD);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_FIRST_PAY_AWARD, awardList, now, giftpackId, giftpackId);

        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /***
     * 三倍返利活动
     *
     * @param player
     * @param pay
     * @param sPay
     */
    private PayConfirmRq processPayThreeRebate(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId(); // 获取充值段位
        StaticActGiftpack actGiftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);// 通过id获取礼包
        if (actGiftpack == null) {// 判断礼包是否为null
            LogUtil.error("未找到此三倍返利配置  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_THREE_REBATE);
        if (activity == null) {
            LogUtil.error("未配置三倍返利活动  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_THREE_REBATE);
            return null;
        }

        // vip处理
        vipProcess(player, sPay);
        //
        Activity activitys = player.activitys.get(ActivityConst.ACT_THREE_REBATE);
        activitys.getStatusCnt().put(2, 0L);// 充值记录在用户的身上
        if (activity.getActivityType() == ActivityConst.ACT_THREE_REBATE) {
            activityDataManager.syncActChange(player, activity.getActivityType());
        }
        // 发奖励
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(actGiftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);

        // 直接同步奖励
        List<Award> awardList = rewardDataManager.sendReward(player, actGiftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // 同步礼包数据
        activityService.syncGiftShow(player);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * 每日特惠
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPayDayDiscounts(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId();
        StaticActGiftpack giftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);
        if (giftpack == null) {
            LogUtil.error("未找到此特惠配置  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_DAY_DISCOUNTS);
        if (activity == null) {
            LogUtil.error("未配置特惠活动  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_DAY_DISCOUNTS);
            return null;
        }
        int gold = giftpack.getTopup();
        StaticActDaydiscounts sad = StaticActivityDataMgr.getActDaydiscountsMap().get(giftpackId);
        if (sad == null) {
            LogUtil.error("未配置特惠活动   giftId未能和s_act_daydiscounts表对应  giftpackId:", giftpackId,
                    ActivityConst.ACT_DAY_DISCOUNTS);
            // 转换成金币发放
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        Integer curCnt = statusMap.get(sad.getGrade());
        int cnt = curCnt == null ? 0 : curCnt;
        if (cnt >= giftpack.getCount()) {// 次数超过
            // 转换成金币发放
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        // 购买次数存储
        statusMap.put(sad.getGrade(), cnt + 1);
        // Vip的处理
        vipProcess(player, sPay);
        // 发奖励
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(giftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);

        // 直接同步奖励
        List<Award> awardList = rewardDataManager.sendReward(player, giftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * 充值礼包
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPayGift(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId();
        int gold = 0;
        StaticActGiftpack giftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);
        if (giftpack == null) {
            LogUtil.error("未找到此礼包配置  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        gold = giftpack.getTopup();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_VIP_BAG);
        if (activity == null) {
            LogUtil.error("未配置礼包活动  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_VIP_BAG);
            return null;
        }
        Date nowDate = new Date();
        List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftPackPlanByDate(player.account.getCreateDate(), serverSetting.getOpenServerDate(), nowDate);
        // 获取对应的礼包
        StaticGiftpackPlan plan = planList.stream().filter(p -> p.getGiftpackId() == giftpackId).findFirst()
                .orElse(null);
        // 获取当可以购买的礼包
        if (plan == null) {
            // 礼包可能过期,转换成金币
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        Map<Integer, Long> statusCnt = activity.getStatusCnt();
        Long cut = statusCnt.get(plan.getKeyId());
        if (cut == null) {
            // 0次
            statusCnt.put(plan.getKeyId(), 1L);// 存的是keyId
        } else {
            if (cut >= giftpack.getCount()) {
                return transformGold(player, pay, sPay, gold, giftpackId); // 次数满了
            }
            statusCnt.put(plan.getKeyId(), cut.longValue() + 1);
        }
        // Vip的处理
        vipProcess(player, sPay);

        // 发奖励
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(giftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);
        // 直接同步奖励
        List<Award> awardList = rewardDataManager.sendReward(player, giftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // 同步礼包数据
        activityService.syncGiftShow(player);

        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_VIP_BUY, 1);
        // 充值成功的同步数据(礼包购买成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * 礼包充值失败转换成金币
     *
     * @param player
     * @param pay
     * @param gold
     * @param giftpackId
     * @return
     */
    private PayConfirmRq transformGold(Player player, Pay pay, StaticPay sPay, int gold, int giftpackId) {
        List<List<Integer>> awards = new ArrayList<>();
        List<Integer> award = new ArrayList<>();
        award.add(AwardType.MONEY);
        award.add(AwardType.Money.GOLD);
        award.add(gold);
        awards.add(award);
        mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(awards), MailConstant.MOLD_PAY_GIFT_FAIL,
                AwardFrom.PAY_GIFT_FAIL_T_GOLD, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // 充值成功的同步数据(礼包购买失败转换成金币)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), gold, giftpackId);
        return payBackReturn(player, pay, sPay, gold);
    }

    private PayConfirmRq payBackReturn(Player player, Pay pay, StaticPay sPay, int gold) {
        PayConfirmRq.Builder builder = PayConfirmRq.newBuilder();
        builder.setPlatNo(pay.getPlatNo());
        builder.setOrderId(pay.getOrderId());
        builder.setAddGold(gold);
        builder.setAccountKey(player.account.getAccountKey());
        LogLordHelper.logPay(player.lord, player.account, pay.getOrderId(), pay.getSerialId(), pay.getAmount(),
                sPay.getPayId(), pay.getUsd());
        return builder.build();
    }

    /**
     * 所有玩家进行邮件奖励
     */
    public void monthCardRewardByLogic() {
        LogUtil.debug("检测所有玩家,并发放月卡");
        Date lastTimeDate = new Date(REWARD_ALL_PLAY_LAST_TIME * 1000L);
        if (DateHelper.isToday(lastTimeDate)) {
            // 说明已经发送了邮件，跳过
            return;
        }
        REWARD_ALL_PLAY_LAST_TIME = TimeHelper.getCurrentSecond();
        for (Player player : playerDataManager.getPlayers().values()) {
            monthCardRewardBySingleLogic(player);
            funCardRewardBySingleLogic(player);

            //处理赛季月卡每日奖励
            seasonService.handleMonthCardReward(player);
        }
    }

    /**
     * 单个玩家功能卡奖励逻辑
     *
     * @param player
     */
    private void funCardRewardBySingleLogic(Player player) {
        if (player == null) return;
        int today = TimeHelper.getCurrentDay();
        int now = TimeHelper.getCurrentSecond();
        for (FunCard fc : player.funCards.values()) {
//            if(fc.getType() == FunCard.CARD_TYPE[8]){
//                continue;
//            }
            List<StaticFunCard> sfcGroupType = StaticVipDataMgr.getFunCardByGroupType(fc.getType());
            if (CheckNull.isEmpty(sfcGroupType)) {
                LogUtil.error("发邮件奖励时未找到功能卡配置  type:", fc.getType(), " roleId:", player.roleId);
                continue;
            }
            if (today != fc.getLastTime()
                    && (fc.getRemainCardDay() > 0 || fc.getRemainCardDay() == StaticFunCard.FOREVER_DAY)) {
                if (fc.getRemainCardDay() != StaticFunCard.FOREVER_DAY) {
                    fc.subRemainCardDay(); // 减1天
                }
                fc.setLastTime(today);
                StaticFunCard sfc = null;
                if (fc.getRemainCardDay() == StaticFunCard.FOREVER_DAY) { // 找永久卡的配置
                    sfc = sfcGroupType.stream().filter(s -> s.getDay() == StaticFunCard.FOREVER_DAY).findFirst()
                            .orElse(null);
                } else {
                    sfc = sfcGroupType.stream().filter(s -> s.getDay() != StaticFunCard.FOREVER_DAY).findFirst()
                            .orElse(null);
                }
                if (sfc != null) {
                    int payId = sfc.getPayId();
                    List<Award> awrdList = PbHelper.createAwardsPb(sfc.getAward());
                    if (ListUtils.isNotBlank(awrdList)) {
                        // 发奖励邮件
                        mailDataManager.sendAttachMail(player, awrdList, MailConstant.MOLD_FUN_CARD_AWARD,
                                AwardFrom.FUN_CARD_AWARD, now, payId, fc.getRemainCardDay(), payId, fc.getRemainCardDay());
                    }
                } else {
                    LogUtil.error("roleId:", player.roleId, ", 发送功能月卡时配置未找到  FunCard=" + fc);
                }
            }
        }
    }

    /**
     * 单个玩家月卡奖励逻辑
     *
     * @param player
     */
    private void monthCardRewardBySingleLogic(Player player) {
        if (null == player) {
            return;
        }
        Lord lord = player.lord;
        int curMouthCardDay = lord.getMouthCardDay();
        if (curMouthCardDay <= 0) {
            // 月卡天数已耗尽
            return;
        }
        int lastTime = lord.getMouthCLastTime();
        Date lastTimeDate = new Date(lastTime * 1000L);
        if (DateHelper.isToday(lastTimeDate)) {
            // 今天已经发送奖励邮件
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        // 月卡天数减一天
        lord.setMouthCardDay(curMouthCardDay - 1);
        // 更新月卡奖励时间
        lord.setMouthCLastTime(now);
        List<Award> awards = PbHelper.createAwardsPb(Constant.MONTH_CARD_REWARD);
        // 发邮件和附件
        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_MONTH_CARD_REWARD, AwardFrom.MONTH_CARD_REWARD,
                now, lord.getMouthCardDay(), lord.getMouthCardDay());
    }

    /**
     * 功能卡
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPayFunCard(Player player, Pay pay, StaticPay sPay) {
        int payId = sPay.getPayId();
        int now = TimeHelper.getCurrentSecond();
        // 发对应的金币数量
        if (sPay.getTopup() > 0) {
            Award gold = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup());
            List<Award> goldAward = new ArrayList<>(1);
            goldAward.add(gold);
            mailDataManager.sendAttachMail(player, goldAward, MailConstant.MOLD_FUN_CARD_GOLD, AwardFrom.FUN_CARD_AWARD,
                    now, payId, payId);
        }
        Map<Integer, StaticFunCard> funCardCnf = StaticVipDataMgr.getFunCardMap();
        StaticFunCard sfc = funCardCnf.values().stream().filter(fc -> fc.getPayId() == payId).findFirst().orElse(null);
        if (sfc != null) {
            if (sfc.getType() == FunCard.CARD_TYPE[9]) {//赛季月卡 特殊处理
                seasonService.buyMonthCard(player, sPay, sfc);
            } else {
                // 给予功能卡
                FunCard funCard = player.funCards.get(sfc.getType());
                if (funCard == null) { // 从来没有购买过
                    funCard = new FunCard(sfc.getType());
                    player.funCards.put(funCard.getType(), funCard);
                }
                if (sfc.getType() == FunCard.CARD_TYPE[8]) {//特权卡
                    funCard.addExpireDay(sfc.getDay());
                } else {
                    funCard.incrRemainCardDay(sfc.getDay()); // 加天数,内部有判断
                }
                funCardRewardBySingleLogic(player);
            }
        } else {
            LogUtil.error("未找到对应功能卡的payId: ", payId, " roleId:", player.roleId);
        }
        //触发任务
        taskDataManager.updTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);
        dailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);

        // vip处理
        vipProcess(player, sPay);
        // 充值成功的同步数据(功能卡成功)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        activityDataManager.updActivity(player, ActivityConst.ACT_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);
//        activityDataManager.updActivity(player, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * 月卡充值
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPayMonth(Player player, Pay pay, StaticPay sPay) {
        int mouthCardDay = player.lord.getMouthCardDay();
        int lastTime = player.lord.getMouthCLastTime();

        Date lastTimeDate = new Date(lastTime * 1000L);
        if (DateHelper.isToday(lastTimeDate)) {
            // 说明今天已经发送奖励邮件
            player.lord.setMouthCardDay(mouthCardDay + Constant.MONTH_CARD_DAY);// 累加月卡天数
        } else {
            player.lord.setMouthCardDay(mouthCardDay + Constant.MONTH_CARD_DAY);
            // 发送月卡奖励
            monthCardRewardBySingleLogic(player);
        }
        taskDataManager.updTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);
        dailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);

        activityDataManager.updActivity(player, ActivityConst.ACT_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);
//        activityDataManager.updActivity(player, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);

        // vip处理
        vipProcess(player, sPay);
        // 充值成功的同步数据
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    private void vipProcess(Player player, StaticPay sPay) {
        // vip处理
        player.lord.setTopup(player.lord.getTopup() + sPay.getPrice());
        // 配置的充值金额
        if (player.lord.getTopup() >= ActParamConstant.ACT_DEDICATED_CUSTOMER_SERVICE_CONF.get(0).get(0)) {
            activityDataManager.updActivity(player, ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE, TimeHelper.getCurrentSecond(), 0, true);
        }
        player.lord.setVipExp(player.lord.getVipExp() + sPay.getVipexp());
        vipDataManager.processVip(player);
        // 同步到客户端
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
        change.addChangeType(AwardType.MONEY, AwardType.Money.VIP_EXP);
        rewardDataManager.syncRoleResChanged(player, change);

        LogLordHelper.vipExp(AwardFrom.PAY, player.account, player.lord, sPay.getVipexp());
    }

    /**
     * 金币充值
     *
     * @param player
     * @param pay
     * @param sPay
     * @param platType 平台类型 0:Android 1:ios
     * @return
     */
    private PayConfirmRq processPayGold(Player player, Pay pay, StaticPay sPay, int platType) {
        int topUp = sPay.getTopup();
        int extraGold = (sPay.getExtraGold() * topUp) / 100;
        int price = sPay.getEventpts();

        // 档位首充翻倍判断
        String firstDouble = platType + "_" + sPay.getPayId();
        if (sPay.getBanFlag() == 0) {
            // 只有金币充值才会有首充翻倍, 平台类型_payId
            if (!player.firstPayDouble.contains(firstDouble)) {
                extraGold += topUp;
                LogUtil.debug("roleId:", player.roleId, ", 享受了首充翻倍,", firstDouble);
            } else {
                extraGold += sPay.getExtraGold2();
                LogUtil.debug("roleId:", player.roleId, ", 享受了额外玉璧赠送, ", sPay.getPayId());
            }
        }

        if (addPayGold(player, topUp, extraGold, pay.getSerialId(), price, AwardFrom.PAY)) {
            // vip处理
            vipProcess(player, sPay);
            int maillMoldId = MailConstant.MOLD_PAY_DONE;
            // 记录首充翻倍
            if (sPay.getBanFlag() == 0 && !player.firstPayDouble.contains(firstDouble)) {
                player.firstPayDouble.add(firstDouble);
                maillMoldId = MailConstant.MOLD_FIRST_DOUBLE_PAY_DONE;
            }
            // 发邮件
            mailDataManager.sendNormalMail(player, maillMoldId, TimeHelper.getCurrentSecond(),
                    String.valueOf(topUp + extraGold), String.valueOf(topUp + extraGold), extraGold);

            // 充值成功的同步数据
            syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), topUp + extraGold,
                    sPay.getPayId());

            activityDataManager.updActivity(player, ActivityConst.ACT_MONOPOLY, topUp, 0, true);// 大富翁进度更新

            //貂蝉任务-充值钻石
            ActivityDiaoChanService.completeTask(player, ETask.RECHARGE_DIAMOND, topUp);
            TaskService.processTask(player, ETask.RECHARGE_DIAMOND, topUp);

            return payBackReturn(player, pay, sPay, topUp + extraGold);
        }
        return null;
    }

    // ===================== 游戏服向支付服务器请求订单 ===================

    public PayApplyRq payApplyRqProcess(GetPaySerialIdRq req) throws MwException {
        long roleId = req.getRoleId();
        // 玩家是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        String platName = req.getPlatName();
        /*if (CheckNull.isNullTrim(platName) || StaticVipDataMgr.getPayPlat(platName) == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "支付平台名称错误");
        }
        StaticPayPlat payPlat = StaticVipDataMgr.getPayPlat(platName);*/

        int lastPay = player.getMixtureDataById(PlayerConstant.LAST_PAY_TIME);
        int currentSecond = TimeHelper.getCurrentSecond();
        if (currentSecond == lastPay) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "请求频率过高");
        }
        int payType = req.getPayType();

        StaticPay sPay = null;
        // if (payPlat.getType() == 0) {
        sPay = StaticVipDataMgr.getStaticPayByPayId(payType);
        /*} else if (payPlat.getType() == 1) {
            sPay = StaticVipDataMgr.getStaticPayIosByPayId(payType);
        }*/
        if (null == sPay) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "没有此支付类型");
        }
        if (sPay.getPrice() <= 0.0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "支付配置错误");
        }
        PayApplyRq.Builder builder = PayApplyRq.newBuilder();
        builder.setRoleId(roleId);
        builder.setPayType(payType);
        builder.setPlatName(platName);
        builder.setServerId(player.account.getServerId());
        builder.setAmount(sPay.getPrice());
        builder.setUsd(sPay.getUsd());
        builder.setAccountKey(player.account.getAccountKey());
        player.setMixtureData(PlayerConstant.LAST_PAY_TIME, currentSecond);
        return builder.build();

    }

    public GetPaySerialIdRs payApplyRsProcess(String serialId, long roleId, int payType, String platName) {
        // 保存到自己的数据库中
        // payDao.createSerialId(serialId, roleId, payType);
        GetPaySerialIdRs.Builder builder = GetPaySerialIdRs.newBuilder();
        builder.setSerialId(serialId);
        return builder.build();

    }

    /**
     * 充值成功后推送给客户端的数据
     *
     * @param serialId    充值支付成功后订单号(非渠道的)
     * @param amount      充值的金额(元)
     * @param type        充值类型 0. 充值金币; 1. 月卡; 3. 礼包; 4. 触发式礼包, 5. 每日特惠充值,10.首充礼包;
     * @param topup       获得金币总数(包含额外赠送的), 若充值礼包失败转换的金币数, 月卡全部为0
     * @param staticPayId
     */
    private void syncPaybackSuc(Player player, String serialId, int amount, int type, int topup, int staticPayId) {
        if (player != null && player.ctx != null && player.isLogin) {
            SyncPaybackSucRs.Builder builder = SyncPaybackSucRs.newBuilder();
            builder.setSerialId(serialId);
            builder.setAmount(amount);
            builder.setType(type);
            builder.setTopup(topup);
            builder.setPayId(staticPayId);
            builder.setPaySumAmoumt(player.getPaySumAmoumt());
            // 用掉首充翻倍
            for (String f : player.firstPayDouble) {
                String[] platAndPayid = f.split("_");
                if (platAndPayid.length != 2) {
                    continue;
                }
                int plat = NumberHelper.strToInt(platAndPayid[0], 0);
                int payId = NumberHelper.strToInt(platAndPayid[1], 0);
                if (payId == 0) {
                    continue;
                }
                builder.addPayIds(PbHelper.createTwoIntPb(plat, payId));
            }
            Base.Builder msg = PbHelper.createSynBase(SyncPaybackSucRs.EXT_FIELD_NUMBER, SyncPaybackSucRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

}
