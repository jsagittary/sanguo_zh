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
     * ????????????
     */
    public static final int FLAG_PAY_GOLD = 0;
    /**
     * ????????????
     */
    public static final int FLAG_PAY_MONTH = 1;
    /**
     * ????????????
     */
    public static final int FLAG_PAY_GIFT = 3;
    /**
     * ???????????????
     */
    public static final int FLAG_PAY_TRIGGER_GIFT = 4;
    /**
     * ??????????????????
     */
    public static final int FLAG_PAY_DAY_DISCOUNTS = 5;
    /**
     * ??????????????????
     */
    public static final int FLAG_PAY_THREE_REBATE = 6;
    /**
     * ???????????????
     */
    public static final int FLAG_PAY_FUN_CARD = 7;
    /**
     * ??????????????????
     */
    public static final int FLAG_PAY_GOLD_WEB = 8;
    /**
     * ??????????????????
     */
    public static final int FLAG_PAY_BATTLE_PASS = 9;

    /**
     * ????????????
     */
    public static final int FLAG_PAY_FIRST_PAY = 10;

    /**
     * ????????????
     */
    public static final int FLAG_BUILD_GIFT_PAY = 11;

    /**
     * ????????????
     */
    public static final int FLAG_AGENT_GIFT_PAY = 12;

    /**
     * ??????????????????(???????????????)
     */
    public static final int SKIN_ENCORE_PAY = 13;

    /**
     * ???????????????????????????
     */
    public static final int FLAG_UNLOCK_BOX_OFFICE = 14;

    /**
     * ??????????????????
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
     * ???????????????????????????
     */
    private final static int PAY_ACT_RATIO = 10;

    private boolean addPayGold(Player target, int topup, int extraGold, String serialId, int price, AwardFrom from) {
        if (topup <= 0) {
            return false;
        }
        Lord lord = target.lord;

        lord.setGold(lord.getGold() + topup + extraGold);
        lord.setGoldGive(lord.getGoldGive() + topup + extraGold);
        // ??????????????????
        LogLordHelper.gold(from, target.account, lord, topup + extraGold, topup);

        // ????????????????????????
        if (globalDataManager.getGameGlobal().getTrophy() != null) {
            globalDataManager.getGameGlobal().getTrophy().getGold().addAndGet(topup);
        }

        // activityDataManager.updActivity(target, ActivityConst.ACT_CHARGE_TOTAL, topup, 0, true);
        activityDataManager.updActivity(target, ActivityConst.ACT_CHARGE_CONTINUE, price, 0, true);
//        activityDataManager.updActivity(target, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, price, 0, true);

        // ????????????
        activityDataManager.updActivity(target, ActivityConst.ACT_LUCKY_POOL, topup, 0, true);
        activityDataManager.updGlobalActivity(target, ActivityConst.ACT_LUCKY_POOL, topup, 0);
        return true;
    }

    public PayConfirmRq payBack(final PayBackRq req, long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        return payLogic(req, player);
    }

    /**
     * GM??????,??????????????????,????????????????????????
     *
     * @param player
     * @param payT
     * @param type
     */
    public void gmPayNoEarn(Player player, long payT, int type) {
        gmPayDispatch(player, payT, type, false);
    }

    /**
     * GM??????
     *
     * @param player
     * @param payT
     * @param type
     */
    public void gmPay(Player player, long payT, int type) {
        gmPayDispatch(player, payT, type, true);
    }

    /**
     * gm??????
     *
     * @param player
     * @param payT
     * @param type
     * @param hasEarn ??????????????? true?????????
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
        // ??????????????????
        StaticPay sPay = null;
        sPay = StaticVipDataMgr.getStaticPayByPayId(payType);

        if (null == sPay) {
            LogUtil.error("GM ???????????????????????????????????????");
        }
        // ?????????????????????
        allPayProcess(player, pay, sPay);
        if (hasEarn) {
            dispatchPay(player, pay, sPay, type);
        } else {
            // vip??????
            vipProcess(player, sPay);
            if (3 == sPay.getBanFlag()) {
                // ??????????????????
                activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_VIP_BUY, 1);
            }
        }
    }

    /**
     * ???????????????
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
            // ???????????????????????????
            return null;
        }
        pay = new Pay();
        // ????????????,??????
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
        // ??????????????????
        int payType = pay.getPayType();
        int platCode = pay.getPlatNo();
        // StaticPayPlat plat = StaticVipDataMgr.getPayPlatByCode(platCode);
        /*if (plat == null) {
            LogUtil.error("??????????????????????????????????????? roleId:", req.getRoleId(), " platNo:", req.getPlatNo());
            return null;
        }*/
        StaticPay sPay = StaticVipDataMgr.getStaticPayByPayId(payType);
        if (null == sPay) {
            LogUtil.error("??????????????????????????????????????? roleId:", req.getRoleId(), " payType:", req.getPayType(), ", platNo:",
                    req.getPlatNo());
            return null;
        }
        allPayProcess(player, pay, sPay);
        // ?????????platType?????????0??? ???????????????????????????
        return dispatchPay(player, pay, sPay, 0);
    }

    /**
     * ????????????
     *
     * @param player
     * @param pay
     * @param sPay
     * @param platType
     * @return
     */
    private PayConfirmRq dispatchPay(Player player, Pay pay, StaticPay sPay, int platType) {
        PayConfirmRq rs = null;
        // ????????????
        int banFlag = sPay.getBanFlag();
        if (FLAG_PAY_GOLD == banFlag || FLAG_PAY_GOLD_WEB == banFlag) {
            // ??????????????????????????????
            rs = processPayGold(player, pay, sPay, platType);
        } else if (FLAG_PAY_MONTH == banFlag) {
            // ??????
            rs = processPayMonth(player, pay, sPay);
        } else if (FLAG_PAY_GIFT == banFlag) {
            // ??????
            rs = processPayGift(player, pay, sPay);
        } else if (FLAG_PAY_TRIGGER_GIFT == banFlag) {
            // ???????????????
            rs = processPayTriggerGift(player, pay, sPay);
        } else if (FLAG_PAY_DAY_DISCOUNTS == banFlag) {
            // ??????????????????
            rs = processPayDayDiscounts(player, pay, sPay);
        } else if (FLAG_PAY_THREE_REBATE == banFlag) {
            // ??????????????????
            rs = processPayThreeRebate(player, pay, sPay);
        } else if (FLAG_PAY_FUN_CARD == banFlag) {
            // ?????????
            rs = processPayFunCard(player, pay, sPay);
        } else if (FLAG_PAY_BATTLE_PASS == banFlag) {
            // ????????????
            rs = processPayBattlePass(player, pay, sPay);
        } else if (FLAG_PAY_FIRST_PAY == banFlag) {
            //????????????
            rs = processPayFirstPay(player, pay, sPay);
        } else if (FLAG_BUILD_GIFT_PAY == banFlag) {
            // ????????????
            rs = processPayBuildGift(player, pay, sPay);
        } else if (FLAG_AGENT_GIFT_PAY == banFlag) {
            // ????????????
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
     * ??????????????????
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processActGiftBgActivity(Player player, Pay pay, StaticPay sPay) {
//        //????????????????????????
//        if (sPay.getTopup() > 0) {
//            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup(),
//                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), pay.getAmount());
//        }
        // vip??????
        if (sPay.getVipexp() > 0) {
            vipProcess(player, sPay);
        }
        AbsGiftBagActivityService.buyActGiftBag(player, sPay);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), sPay.getTopup(), sPay.getPayId());
        return payBackReturn(player, pay, sPay, sPay.getTopup());
    }

    private PayConfirmRq processPayBoxOfficeActivity(Player player, Pay pay, StaticPay sPay) {
        //????????????????????????
        if (sPay.getTopup() > 0) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup(),
                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), pay.getAmount());
        }
        // vip??????
        if (sPay.getVipexp() > 0) {
            vipProcess(player, sPay);
        }
        activityBoxOfficeService.pay4BoxOffice(player, pay, sPay);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), sPay.getTopup(), sPay.getPayId());
        return payBackReturn(player, pay, sPay, sPay.getTopup());
    }


    /**
     * ??????????????????
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPaySkinEncoreActivity(Player player, Pay pay, StaticPay sPay) {
        //????????????????????????
        if (sPay.getTopup() > 0) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getTopup(),
                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), pay.getAmount());
        }
        // vip??????
        if (sPay.getVipexp() > 0) {
            vipProcess(player, sPay);
        }
        //??????????????????
        activitySkinEncoreService.pay4SkinEncore(player, pay, sPay);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), sPay.getTopup(), sPay.getPayId());
        return payBackReturn(player, pay, sPay, sPay.getTopup());
    }

    private PayConfirmRq processPayAgentGift(Player player, Pay pay, StaticPay sPay) {
        int giftPackId = sPay.getPayId(); // ??????????????????

        StaticActGiftpack giftPack = StaticActivityDataMgr.getActGiftpackMapById(giftPackId);
        if (giftPack == null) {
            LogUtil.error("????????????????????????  giftPackId:", giftPackId, ", serialId:", pay.getSerialId(), ", orderId:",
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


        // Vip?????????
        vipProcess(player, sPay);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        // ?????????
        // mailDataManager.sendAttachMail(player, showAwards, MailConstant.MOLD_BUILD_GIFT_REWARD, AwardFrom.BUILD_GIFT_AWARD, TimeHelper.getCurrentSecond());

        // ??????????????????
        List<Award> awardList = rewardDataManager.sendReward(player, giftPack.getAward(), AwardFrom.AGENT_GIFT_AWARD);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftPackId, giftPackId);
        return payBackReturn(player, pay, sPay, 0);
    }


    private PayConfirmRq processPayBuildGift(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId(); // ??????????????????

        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_BUILD_GIFT);
        if (activity == null) {
            LogUtil.error("???????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
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
        // Vip?????????
        vipProcess(player, sPay);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        // ?????????
        // mailDataManager.sendAttachMail(player, showAwards, MailConstant.MOLD_BUILD_GIFT_REWARD, AwardFrom.BUILD_GIFT_AWARD, TimeHelper.getCurrentSecond());

        // ??????????????????
        rewardDataManager.sendRewardByAwardList(player, showAwards, AwardFrom.BUILD_GIFT_AWARD);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_BUILD_GIFT_REWARD, showAwards, TimeHelper.getCurrentSecond());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * ????????????
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
            // vip??????
            vipProcess(player, sPay);
            // ???????????????????????????
            syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), topup, sPay.getPayId());
            Optional.ofNullable(battlePassDataManager.getPersonInfo(player.roleId)).ifPresent(personInfo -> {
                // ?????????????????????
                int staticKey = personInfo.getStaticKey();
                personInfo.setAdvanced(1);
                int upLv = 10;
                // ??????????????????????????????????????????10???
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
        // ????????????????????????????????????
        // 2019-11-26 ?????????????????????????????????????????????
        if (FLAG_PAY_DAY_DISCOUNTS == sPay.getBanFlag()) {
            return;
        }
        int actSchedule = sPay.getEventpts();
        // ????????????
        activityDataManager.firstPayAward(player, actSchedule);
        /** ???????????? */
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_CHARGE, actSchedule);
        // ??????????????????
        taskDataManager.updTask(player, TaskType.COND_PAY_46, actSchedule);
        /** ???????????? */
        activityDataManager.updGlobalActivity(player, ActivityConst.ACT_ALL_CHARGE, actSchedule, player.lord.getCamp());
        // ????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_DAILY_PAY, actSchedule, 0, true);
        // ????????????
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PAY_RANK, actSchedule);
        // ????????????-???
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PAY_RANK_NEW, actSchedule);
        // ????????????3??????
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PAY_RANK_V_3, actSchedule);
        // ??????????????????
        activityDataManager.updRankActivity(player, ActivityConst.ACT_MERGE_PAY_RANK, actSchedule);
        // ????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_GIFT_PAY, actSchedule, 0, true);
        // ????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_PAY_TURNPLATE, actSchedule, 0, true);
        // ???????????????????????????
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_GOLD_CNT, actSchedule);
        // ???????????????????????????
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_GOLD_CNT, actSchedule);
        // ???????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_EASTER, actSchedule, 0, true);
        // ????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_PAY_7DAY, actSchedule, 0, true);
        // ????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_CHARGE_TOTAL, actSchedule, 0, true);
        // ????????????????????????,banFlag=5???????????????????????????=5?????????????????????
        activityDataManager.updActivity(player, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, actSchedule, 0, true);
        //??????????????????
        DataResource.getBeans(RechargeService.class).forEach(service -> service.afterRecharge(player, sPay.getPrice(), sPay.getEventpts()));

        //???????????????????????????try  catch ??????????????????????????????????????????
        try {
            int price = sPay.getUsd() == 0.0 ? sPay.getPrice() : (int) Math.ceil(new BigDecimal(String.valueOf(sPay.getUsd())).doubleValue());
            // ?????????????????????????????????
            LocalDateTime now = LocalDateTime.now();
            int week = now.getDayOfWeek().getValue();
            // ????????????
            int history = player.getMixtureDataById(week + PlayerConstant.RECENTLY_PAY);
            player.setMixtureData(week + PlayerConstant.RECENTLY_PAY, history + price);
            // ????????????
            // activityRedPacketService.redPacketActivity(player,sPay.getPrice() );
        } catch (Exception e) {
            LogUtil.error(e);
        }

        try {
            // campService.addAndCheckPartySupply(player, PartyConstant.SupplyType.PAY_GOLD, sPay.getPrice());
        } catch (Exception e) {
            LogUtil.error("??????????????????????????????", e);
        }

    }

    /**
     * ???????????????
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
            LogUtil.error("????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        gold = giftpack.getTopup();
        StaticTriggerConf triggerConf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftpackId);
        if (CheckNull.isNull(triggerConf)) {
            LogUtil.error("??????????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
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
                // ?????????????????????????????????
                return transformGold(player, pay, sPay, gold, giftpackId);
            }
        } catch (Exception e) {
            LogUtil.error(e, "???????????????????????????");
            // ?????????????????????????????????
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        // Vip?????????
        vipProcess(player, sPay);

        // ?????????
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(giftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, now, giftpackId, giftpackId);

        // ??????????????????
        List<Award> awardList = rewardDataManager.sendReward(player, giftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // ??????????????????
        // activityService.syncGiftShow(player);

        // activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_VIP_BUY, 1);

        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * ????????????
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
            LogUtil.error("???????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        gold = giftpack.getTopup();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_FIRSH_CHARGE);
        if (activity == null) {
            LogUtil.error("?????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_FIRSH_CHARGE);
            // ?????????????????????
            return transformGold(player, pay, sPay, gold, giftpackId);
        }

        StaticActAward sad = StaticActivityDataMgr.getActAwardById(giftpackId).get(0);
        if (sad == null) {
            LogUtil.error("???????????????????????????   giftId?????????s_act_award?????????  activityId:", giftpackId,
                    ActivityConst.ACT_FIRSH_CHARGE);
            // ?????????????????????
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        int now = TimeHelper.getCurrentSecond();
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        if (statusMap.getOrDefault(sad.getKeyId(), 0) == 1) {
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        // ????????????
        statusMap.put(sad.getKeyId(), 1);
        // ??????????????????
        statusMap.put(0, now);
        // Vip?????????
        vipProcess(player, sPay);

        // ?????????
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(sad.getAwardList()),
        //         MailConstant.MOLD_FIRST_PAY_AWARD, AwardFrom.FIRST_PAY_AWARD, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);
        // ??????????????????
        List<Award> awardList = rewardDataManager.sendReward(player, sad.getAwardList(), AwardFrom.FIRST_PAY_AWARD);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_FIRST_PAY_AWARD, awardList, now, giftpackId, giftpackId);

        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /***
     * ??????????????????
     *
     * @param player
     * @param pay
     * @param sPay
     */
    private PayConfirmRq processPayThreeRebate(Player player, Pay pay, StaticPay sPay) {
        int giftpackId = sPay.getPayId(); // ??????????????????
        StaticActGiftpack actGiftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);// ??????id????????????
        if (actGiftpack == null) {// ?????????????????????null
            LogUtil.error("??????????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_THREE_REBATE);
        if (activity == null) {
            LogUtil.error("???????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_THREE_REBATE);
            return null;
        }

        // vip??????
        vipProcess(player, sPay);
        //
        Activity activitys = player.activitys.get(ActivityConst.ACT_THREE_REBATE);
        activitys.getStatusCnt().put(2, 0L);// ??????????????????????????????
        if (activity.getActivityType() == ActivityConst.ACT_THREE_REBATE) {
            activityDataManager.syncActChange(player, activity.getActivityType());
        }
        // ?????????
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(actGiftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);

        // ??????????????????
        List<Award> awardList = rewardDataManager.sendReward(player, actGiftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // ??????????????????
        activityService.syncGiftShow(player);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * ????????????
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
            LogUtil.error("????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_DAY_DISCOUNTS);
        if (activity == null) {
            LogUtil.error("?????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_DAY_DISCOUNTS);
            return null;
        }
        int gold = giftpack.getTopup();
        StaticActDaydiscounts sad = StaticActivityDataMgr.getActDaydiscountsMap().get(giftpackId);
        if (sad == null) {
            LogUtil.error("?????????????????????   giftId?????????s_act_daydiscounts?????????  giftpackId:", giftpackId,
                    ActivityConst.ACT_DAY_DISCOUNTS);
            // ?????????????????????
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        Integer curCnt = statusMap.get(sad.getGrade());
        int cnt = curCnt == null ? 0 : curCnt;
        if (cnt >= giftpack.getCount()) {// ????????????
            // ?????????????????????
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        // ??????????????????
        statusMap.put(sad.getGrade(), cnt + 1);
        // Vip?????????
        vipProcess(player, sPay);
        // ?????????
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(giftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);

        // ??????????????????
        List<Award> awardList = rewardDataManager.sendReward(player, giftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * ????????????
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
            LogUtil.error("????????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId);
            return null;
        }
        gold = giftpack.getTopup();
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_VIP_BAG);
        if (activity == null) {
            LogUtil.error("?????????????????????  giftpackId:", giftpackId, ", serialId:", pay.getSerialId(), ", orderId:",
                    pay.getOrderId(), ", roleId:", player.roleId, ", activityType:", ActivityConst.ACT_VIP_BAG);
            return null;
        }
        Date nowDate = new Date();
        List<StaticGiftpackPlan> planList = StaticActivityDataMgr.getGiftPackPlanByDate(player.account.getCreateDate(), serverSetting.getOpenServerDate(), nowDate);
        // ?????????????????????
        StaticGiftpackPlan plan = planList.stream().filter(p -> p.getGiftpackId() == giftpackId).findFirst()
                .orElse(null);
        // ??????????????????????????????
        if (plan == null) {
            // ??????????????????,???????????????
            return transformGold(player, pay, sPay, gold, giftpackId);
        }
        Map<Integer, Long> statusCnt = activity.getStatusCnt();
        Long cut = statusCnt.get(plan.getKeyId());
        if (cut == null) {
            // 0???
            statusCnt.put(plan.getKeyId(), 1L);// ?????????keyId
        } else {
            if (cut >= giftpack.getCount()) {
                return transformGold(player, pay, sPay, gold, giftpackId); // ????????????
            }
            statusCnt.put(plan.getKeyId(), cut.longValue() + 1);
        }
        // Vip?????????
        vipProcess(player, sPay);

        // ?????????
        // mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(giftpack.getAward()),
        //         MailConstant.MOLD_PAY_GIFT_SUC, AwardFrom.PAY_GIFT_SUC, TimeHelper.getCurrentSecond(), giftpackId,
        //         giftpackId);
        // ??????????????????
        List<Award> awardList = rewardDataManager.sendReward(player, giftpack.getAward(), AwardFrom.PAY_GIFT_SUC);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_PAY_GIFT_SUC, awardList, TimeHelper.getCurrentSecond(), giftpackId, giftpackId);
        // ??????????????????
        activityService.syncGiftShow(player);

        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_VIP_BUY, 1);
        // ???????????????????????????(??????????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * ?????????????????????????????????
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
        // ???????????????????????????(?????????????????????????????????)
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
     * ??????????????????????????????
     */
    public void monthCardRewardByLogic() {
        LogUtil.debug("??????????????????,???????????????");
        Date lastTimeDate = new Date(REWARD_ALL_PLAY_LAST_TIME * 1000L);
        if (DateHelper.isToday(lastTimeDate)) {
            // ????????????????????????????????????
            return;
        }
        REWARD_ALL_PLAY_LAST_TIME = TimeHelper.getCurrentSecond();
        for (Player player : playerDataManager.getPlayers().values()) {
            monthCardRewardBySingleLogic(player);
            funCardRewardBySingleLogic(player);

            //??????????????????????????????
            seasonService.handleMonthCardReward(player);
        }
    }

    /**
     * ?????????????????????????????????
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
                LogUtil.error("??????????????????????????????????????????  type:", fc.getType(), " roleId:", player.roleId);
                continue;
            }
            if (today != fc.getLastTime()
                    && (fc.getRemainCardDay() > 0 || fc.getRemainCardDay() == StaticFunCard.FOREVER_DAY)) {
                if (fc.getRemainCardDay() != StaticFunCard.FOREVER_DAY) {
                    fc.subRemainCardDay(); // ???1???
                }
                fc.setLastTime(today);
                StaticFunCard sfc = null;
                if (fc.getRemainCardDay() == StaticFunCard.FOREVER_DAY) { // ?????????????????????
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
                        // ???????????????
                        mailDataManager.sendAttachMail(player, awrdList, MailConstant.MOLD_FUN_CARD_AWARD,
                                AwardFrom.FUN_CARD_AWARD, now, payId, fc.getRemainCardDay(), payId, fc.getRemainCardDay());
                    }
                } else {
                    LogUtil.error("roleId:", player.roleId, ", ????????????????????????????????????  FunCard=" + fc);
                }
            }
        }
    }

    /**
     * ??????????????????????????????
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
            // ?????????????????????
            return;
        }
        int lastTime = lord.getMouthCLastTime();
        Date lastTimeDate = new Date(lastTime * 1000L);
        if (DateHelper.isToday(lastTimeDate)) {
            // ??????????????????????????????
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        // ?????????????????????
        lord.setMouthCardDay(curMouthCardDay - 1);
        // ????????????????????????
        lord.setMouthCLastTime(now);
        List<Award> awards = PbHelper.createAwardsPb(Constant.MONTH_CARD_REWARD);
        // ??????????????????
        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_MONTH_CARD_REWARD, AwardFrom.MONTH_CARD_REWARD,
                now, lord.getMouthCardDay(), lord.getMouthCardDay());
    }

    /**
     * ?????????
     *
     * @param player
     * @param pay
     * @param sPay
     * @return
     */
    private PayConfirmRq processPayFunCard(Player player, Pay pay, StaticPay sPay) {
        int payId = sPay.getPayId();
        int now = TimeHelper.getCurrentSecond();
        // ????????????????????????
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
            if (sfc.getType() == FunCard.CARD_TYPE[9]) {//???????????? ????????????
                seasonService.buyMonthCard(player, sPay, sfc);
            } else {
                // ???????????????
                FunCard funCard = player.funCards.get(sfc.getType());
                if (funCard == null) { // ?????????????????????
                    funCard = new FunCard(sfc.getType());
                    player.funCards.put(funCard.getType(), funCard);
                }
                if (sfc.getType() == FunCard.CARD_TYPE[8]) {//?????????
                    funCard.addExpireDay(sfc.getDay());
                } else {
                    funCard.incrRemainCardDay(sfc.getDay()); // ?????????,???????????????
                }
                funCardRewardBySingleLogic(player);
            }
        } else {
            LogUtil.error("???????????????????????????payId: ", payId, " roleId:", player.roleId);
        }
        //????????????
        taskDataManager.updTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);
        dailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);

        // vip??????
        vipProcess(player, sPay);
        // ???????????????????????????(???????????????)
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        activityDataManager.updActivity(player, ActivityConst.ACT_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);
//        activityDataManager.updActivity(player, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);
        return payBackReturn(player, pay, sPay, 0);
    }

    /**
     * ????????????
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
            // ????????????????????????????????????
            player.lord.setMouthCardDay(mouthCardDay + Constant.MONTH_CARD_DAY);// ??????????????????
        } else {
            player.lord.setMouthCardDay(mouthCardDay + Constant.MONTH_CARD_DAY);
            // ??????????????????
            monthCardRewardBySingleLogic(player);
        }
        taskDataManager.updTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);
        dailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);

        activityDataManager.updActivity(player, ActivityConst.ACT_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);
//        activityDataManager.updActivity(player, ActivityConst.ACT_MERGE_CHARGE_CONTINUE, sPay.getEventpts(), 0, true);

        // vip??????
        vipProcess(player, sPay);
        // ???????????????????????????
        syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), 0, sPay.getPayId());
        return payBackReturn(player, pay, sPay, 0);
    }

    private void vipProcess(Player player, StaticPay sPay) {
        // vip??????
        player.lord.setTopup(player.lord.getTopup() + sPay.getPrice());
        // ?????????????????????
        if (player.lord.getTopup() >= ActParamConstant.ACT_DEDICATED_CUSTOMER_SERVICE_CONF.get(0).get(0)) {
            activityDataManager.updActivity(player, ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE, TimeHelper.getCurrentSecond(), 0, true);
        }
        player.lord.setVipExp(player.lord.getVipExp() + sPay.getVipexp());
        vipDataManager.processVip(player);
        // ??????????????????
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
        change.addChangeType(AwardType.MONEY, AwardType.Money.VIP_EXP);
        rewardDataManager.syncRoleResChanged(player, change);

        LogLordHelper.vipExp(AwardFrom.PAY, player.account, player.lord, sPay.getVipexp());
    }

    /**
     * ????????????
     *
     * @param player
     * @param pay
     * @param sPay
     * @param platType ???????????? 0:Android 1:ios
     * @return
     */
    private PayConfirmRq processPayGold(Player player, Pay pay, StaticPay sPay, int platType) {
        int topUp = sPay.getTopup();
        int extraGold = (sPay.getExtraGold() * topUp) / 100;
        int price = sPay.getEventpts();

        // ????????????????????????
        String firstDouble = platType + "_" + sPay.getPayId();
        if (sPay.getBanFlag() == 0) {
            // ???????????????????????????????????????, ????????????_payId
            if (!player.firstPayDouble.contains(firstDouble)) {
                extraGold += topUp;
                LogUtil.debug("roleId:", player.roleId, ", ?????????????????????,", firstDouble);
            } else {
                extraGold += sPay.getExtraGold2();
                LogUtil.debug("roleId:", player.roleId, ", ???????????????????????????, ", sPay.getPayId());
            }
        }

        if (addPayGold(player, topUp, extraGold, pay.getSerialId(), price, AwardFrom.PAY)) {
            // vip??????
            vipProcess(player, sPay);
            int maillMoldId = MailConstant.MOLD_PAY_DONE;
            // ??????????????????
            if (sPay.getBanFlag() == 0 && !player.firstPayDouble.contains(firstDouble)) {
                player.firstPayDouble.add(firstDouble);
                maillMoldId = MailConstant.MOLD_FIRST_DOUBLE_PAY_DONE;
            }
            // ?????????
            mailDataManager.sendNormalMail(player, maillMoldId, TimeHelper.getCurrentSecond(),
                    String.valueOf(topUp + extraGold), String.valueOf(topUp + extraGold), extraGold);

            // ???????????????????????????
            syncPaybackSuc(player, pay.getSerialId(), pay.getAmount(), sPay.getBanFlag(), topUp + extraGold,
                    sPay.getPayId());

            activityDataManager.updActivity(player, ActivityConst.ACT_MONOPOLY, topUp, 0, true);// ?????????????????????

            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.RECHARGE_DIAMOND, topUp);
            TaskService.processTask(player, ETask.RECHARGE_DIAMOND, topUp);

            return payBackReturn(player, pay, sPay, topUp + extraGold);
        }
        return null;
    }

    // ===================== ??????????????????????????????????????? ===================

    public PayApplyRq payApplyRqProcess(GetPaySerialIdRq req) throws MwException {
        long roleId = req.getRoleId();
        // ??????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        String platName = req.getPlatName();
        /*if (CheckNull.isNullTrim(platName) || StaticVipDataMgr.getPayPlat(platName) == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????");
        }
        StaticPayPlat payPlat = StaticVipDataMgr.getPayPlat(platName);*/

        int lastPay = player.getMixtureDataById(PlayerConstant.LAST_PAY_TIME);
        int currentSecond = TimeHelper.getCurrentSecond();
        if (currentSecond == lastPay) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????");
        }
        int payType = req.getPayType();

        StaticPay sPay = null;
        // if (payPlat.getType() == 0) {
        sPay = StaticVipDataMgr.getStaticPayByPayId(payType);
        /*} else if (payPlat.getType() == 1) {
            sPay = StaticVipDataMgr.getStaticPayIosByPayId(payType);
        }*/
        if (null == sPay) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "?????????????????????");
        }
        if (sPay.getPrice() <= 0.0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????");
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
        // ??????????????????????????????
        // payDao.createSerialId(serialId, roleId, payType);
        GetPaySerialIdRs.Builder builder = GetPaySerialIdRs.newBuilder();
        builder.setSerialId(serialId);
        return builder.build();

    }

    /**
     * ??????????????????????????????????????????
     *
     * @param serialId    ??????????????????????????????(????????????)
     * @param amount      ???????????????(???)
     * @param type        ???????????? 0. ????????????; 1. ??????; 3. ??????; 4. ???????????????, 5. ??????????????????,10.????????????;
     * @param topup       ??????????????????(?????????????????????), ???????????????????????????????????????, ???????????????0
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
            // ??????????????????
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
