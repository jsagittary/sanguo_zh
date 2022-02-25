package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.TriggerGift;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by pengshuo on 2019/4/29 17:18
 * <br>Description: 触发式礼包-新添加
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class ActivityTriggerService {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 时间触发是否满足
     */
    public void checkTimeTriggerActivity() {
        Date nowDate = new Date();
        // 开服第几天
        int openServerDay = serverSetting.getOpenServerDay(nowDate);
        playerDataManager.getPlayers().values().forEach(player -> {
            // 创建角色的第几天
            int createLordDay = DateHelper.dayiy(player.account.getCreateDate(), nowDate);
            List<StaticGiftPackTriggerPlan> plans = StaticActivityDataMgr.getGiftPackTriggerPlan(openServerDay, createLordDay, nowDate);
            Optional.ofNullable(plans).ifPresent(triggerPlans ->
                    triggerPlans.forEach(plan -> {
                        StaticTriggerConf conf = StaticActivityDataMgr.getTriggerConf(plan.getTriggerId());
                        Optional.ofNullable(conf).ifPresent(c -> {
                            try {
                                // 触发类型为时间触发
                                if (c.getTriggerId() == ActivityConst.TRIGGER_GIFT_TIME_COND) {
                                    int giftId = c.getGiftId();
                                    int keyId = plan.getKeyId();
                                    StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(giftId);
                                    if (CheckNull.isNull(triggerGiftConf)) {
                                        LogUtil.error(String.format("检测礼包配置, 没有该礼包配置, role_id: %s, gift_id: %s", player.roleId, giftId));
                                        return;
                                    }
                                    if (!conf.checkTriggerOpenCnf(player)) {
                                        throw new MwException(GameError.TRIGGER_LEVEL_ERR.getCode(), "检测触发配置,触发条件不满足 roleId:", player.roleId, ", giftId: ", giftId, ", triggerPlanId=", keyId);
                                    }
                                    // 推送礼包
                                    syncTriggerGift(player, conf, plan, getTriggerGiftInfo(player, conf, keyId));

                                }
                            } catch (Exception e) {
                                LogUtil.common("推送时间触发礼包 error ", e.getMessage());
                            }
                        });
                    })
            );
        });
    }

    /**
     * @param player 玩家对象
     * @param conf   触发式礼包配置
     * @param key    礼包主键
     * @return 触发式礼包数据
     */
    public TriggerGift getTriggerGiftInfo(Player player, StaticTriggerConf conf, int key) {
        player.triggerGifts.computeIfAbsent(conf.getTriggerId(), k -> new HashMap<>()).compute(conf.getGiftId(), (k, v) -> {
            if (Objects.isNull(v)) {
                v = new TriggerGift(conf.getGiftId(), key);
            } else if (v.getKeyId() != key) {
                v.isRestart(true);
                v.setKeyId(key);
            }
            return v;
        });
        return player.triggerGifts.get(conf.getTriggerId()).get(conf.getGiftId());
    }

    /**
     * 触发式礼包定时器
     */
    public void checkTriggerByEndTime() {
        List<StaticTriggerConf> triggerConfList = StaticActivityDataMgr.getTriggerConfList().stream()
                // 过滤triggerId等于0
                .filter(conf -> conf.getTriggerId() == 0)
                // 并且配置了TriGift
                .filter(conf -> !CheckNull.isEmpty(conf.getTriGift()))
                .collect(Collectors.toList());
        // 如果没有需要触发的礼包
        if (CheckNull.isEmpty(triggerConfList)) {
            return;
        }
        // 现在的时间
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values()
                .stream()
                .filter(p -> triggerConfList.stream().anyMatch(conf -> conf.checkTriggerOpenCnf(p)))
                .forEach(p -> {
                    triggerConfList.stream()
                            // 过滤判断条件
                            .filter(conf -> conf.checkTriggerOpenCnf(p))
                            .filter(conf -> {
                                // 需要触发的礼包id
                                int giftId = conf.getGiftId();
                                List<Integer> triGift = conf.getTriGift();
                                // 这里不判断null了, 上面已经判断过了
                                int payId = triGift.get(0);
                                // 是否需要购买 0 不需要购买 1 需要购买
                                int needBuy = triGift.get(1);
                                // CD时间
                                int triGiftTime = conf.getTriGiftTime();
                                // 支付配置
                                StaticPay sPay = StaticVipDataMgr.getPayById(payId);
                                if (CheckNull.isNull(sPay)) {
                                    return false;
                                }
                                TriggerGift needTriGift = p.triggerGifts.values()
                                        .stream()
                                        .flatMap(en -> en.values().stream())
                                        .filter(tg -> tg.getGiftId() == giftId)
                                        .findFirst()
                                        .orElse(null);
                                if (needTriGift != null) {
                                    // 已经触发了
                                    if (needTriGift.getState() == ActivityConst.TRIGGER_STATUS) {
                                        return false;
                                    }
                                }
                                if (sPay.getBanFlag() == PayService.FLAG_PAY_FIRST_PAY) {
                                    // 首充礼包
                                    Activity activity = activityDataManager.getActivityInfo(p, ActivityConst.ACT_FIRSH_CHARGE);
                                    if (CheckNull.isNull(activity)) {
                                        return false;
                                    }
                                    // 购买的话, 算购买的时间
                                    int buyTime = activity.getStatusMap().getOrDefault(0, 0);
                                    // 需要购买
                                    if (needBuy == 1) {
                                        StaticActAward sad = StaticActivityDataMgr.getActAwardById(payId).get(0);
                                        // 没有查到这个奖励
                                        if (CheckNull.isNull(sad)) {
                                            return false;
                                        }
                                        // 购买状态
                                        int status = activity.getStatusMap().getOrDefault(sad.getKeyId(), 0);
                                        if (status == 0) {
                                            return false;
                                        }
                                        if (buyTime == 0) {
                                            // 兼容线上, 没有购买时间就不弹出触发式礼包
                                            return false;
                                        }
                                        // 未配置CD间隔或者现在已经过了CD间隔
                                        return now >= buyTime + (triGiftTime >= 0 ? triGiftTime : 0);
                                    } else {
                                        if (buyTime != 0) {
                                            // 不需要购买, 但是购买了, 就不触发新的礼包
                                            return false;
                                        }
                                        long createTime = p.account.getCreateDate().getTime();
                                        long disPlayTime = createTime + (ActParamConstant.ACT_FIRSH_CHARGE_TIME * 1000L);
                                        // 未配置CD间隔或者现在已经过了CD间隔
                                        return now >= disPlayTime + (triGiftTime >= 0 ? triGiftTime : 0);
                                    }
                                } else if (sPay.getBanFlag() == PayService.FLAG_PAY_TRIGGER_GIFT) {
                                    StaticTriggerConf sTConf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(payId);
                                    // 没找到这个触发式礼包的配置
                                    if (CheckNull.isNull(sTConf)) {
                                        return false;
                                    }
                                    // 触发式礼包
                                    TriggerGift triggerGift = p.triggerGifts.values()
                                            .stream()
                                            .flatMap(en -> en.values().stream())
                                            .filter(tg -> tg.getGiftId() == payId)
                                            .findAny()
                                            .orElse(null);
                                    // 先找到这个触发式礼包
                                    if (CheckNull.isNull(triggerGift)) {
                                        return false;
                                    }
                                    // 礼包状态为未触发
                                    if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
                                        return false;
                                    }
                                    // 触发式礼包还未结束
                                    if (now < triggerGift.getEndTime()) {
                                        return false;
                                    }
                                    int count = triggerGift.getCount();
                                    if (needBuy == 1 && count == 0) {
                                        // 需要购买而未购买的过滤掉
                                        return false;
                                    } else if (needBuy == 0 && count != 0){
                                        // 不需要购买，但是购买了过滤掉
                                        return false;
                                    }
                                    return now >= triggerGift.getEndTime() + (triGiftTime >= 0 ? triGiftTime : 0);
                                }
                                return false;
                            })
                            .forEach(conf -> {
                                try {
                                    // 触发式礼包推送
                                    activityService.checkTriggerGiftSyncByGiftId(conf.getGiftId(), p);
                                } catch (Exception e) {
                                    LogUtil.error("推送触发礼包 error ", e.getMessage());
                                }
                            });
                });


        // playerDataManager.getPlayers().values()
        //         .stream()
        //         // 过滤触发式礼包配置了triggerGift字段，并且过了结束时间礼包的玩家
        //         .filter(p -> p.triggerGifts.values()
        //                 .stream()
        //                 .flatMap(en -> en.values().stream())
        //                 .anyMatch(tg -> {
        //                     StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(tg.getGiftId());
        //                     if (conf == null) {
        //                         return false;
        //                     }
        //                     if (CheckNull.isEmpty(conf.getTriGift())) {
        //                         return false;
        //                     }
        //                     if (tg.getEndTime() < now) {
        //                         return true;
        //                     }
        //                     return true;
        //                 }))
        //         .forEach(p -> {
        //             List<TriggerGift> triggerGifts = p.triggerGifts.values()
        //                     .stream()
        //                     .flatMap(en -> en.values().stream())
        //                     .collect(Collectors.toList());
        //
        //             triggerGifts.stream()
        //                     .filter(tg -> {
        //                         // 上面已经过滤了conf，这里就不判断了
        //                         StaticTriggerConf conf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(tg.getGiftId());
        //                         List<Integer> triGift = conf.getTriGift();
        //                         // 要触发的礼包
        //                         int tGiftId = triGift.get(0);
        //                         // 是否需要购买
        //                         int buyFlag = triGift.get(1);
        //                         // 如果已经有触发过
        //                         if (triggerGifts.stream().anyMatch(t -> t.getGiftId() == tGiftId && t.getState() == ActivityConst.TRIGGER_STATUS)) {
        //                             return false;
        //                         }
        //                         if (buyFlag == 1) {
        //                             int count = tg.getCount();
        //                             // 需要购买而未购买的过滤掉
        //                             if (count == 0) {
        //                                 return false;
        //                             }
        //                         }
        //                         // 如果有触发间隔时间，但是还没到，过滤掉
        //                         return conf.getTriGiftTime() <= 0 || tg.getEndTime() + conf.getTriGiftTime() >= now;
        //                     })
        //                     // 转换成StaticTriggerConf
        //                     .map(tg -> StaticActivityDataMgr.getTriggerGiftConfByGiftId(tg.getGiftId()).getTriGift().get(0))
        //                     // 过滤掉没找到的礼包配置
        //                     .filter(Objects::nonNull)
        //                     .forEach(giftId -> {
        //                         try {
        //                             // 触发式礼包推送
        //                             activityService.checkTriggerGiftSyncByGiftId(giftId, p);
        //                         } catch (Exception e) {
        //                             LogUtil.error("推送触发礼包 error ", e.getMessage());
        //                         }
        //                     });
        //         });
    }

    /**
     * 玩家升级触发
     *
     * @param player
     * @param lvThroughList
     */
    public void roleLevelUpTriggerGift(Player player, List<Integer> lvThroughList) {
        if (player == null) {
            LogUtil.error("玩家升级触发 error: player is null");
            return;
        }
        List<StaticTriggerConf> triggers =
                StaticActivityDataMgr.getTriggerGiftConfById(ActivityConst.TRIGGER_GIFT_ROLE_LEVEL, player);
        Optional.ofNullable(triggers)
                .ifPresent(ts -> {
                    ts.stream()
                            // 本次加经验刮经历过的等级列表
                            .filter(stc -> lvThroughList.contains(stc.getCond().get(0)))
                            .forEach(stc -> {
                                try {
                                    activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                                } catch (MwException e) {
                                    LogUtil.error("玩家升级检测触发式礼包出错：", e);
                                }
                            });
                });

    }

    /**
     * 建筑升级没有对应的资源触发 点升级发现资源不足触发
     */
    public void buildLevelUpNoResourceTriggerGift(Player player, int buildType, int level) {
        if (player == null) {
            LogUtil.error("建筑升级,资源不足触发 error: player is null");
            return;
        }
        try {
            List<StaticTriggerConf> triggers =
                    StaticActivityDataMgr.getTriggerGiftConfById(ActivityConst.TRIGGER_GIFT_REBUILD_7_8, player);
            syncTriggerGift(player, buildType, level, triggers);
        } catch (Exception e) {
            LogUtil.error("建筑升级,资源不足触发 error: ", e.getMessage());
        }
    }

    /**
     * 建筑升级成功触发
     */
    public void buildLevelUpTriggerGift(Player player, int buildType, int level) {
        if (player == null) {
            LogUtil.error("建筑升级成功触发 error: player is null");
            return;
        }
        try {
            List<StaticTriggerConf> triggers =
                    Optional.of(ActivityConst.TRIGGER_GIFT_REBUILD_8_9_10)
                            .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player)).orElse(new ArrayList<>());
            syncTriggerGift(player, buildType, level, triggers);
        } catch (Exception e) {
            LogUtil.error("建筑升级成功触发 error: ", e.getMessage());
        }
    }

    /**
     * 首次通关关卡
     *
     * @param player   玩家
     * @param combatId 副本id
     */
    public void doCombatTriggerGift(Player player, int combatId) {
        if (player == null) {
            LogUtil.error("获取首次通关关卡触发式礼包时 error: player is null!");
            return;
        }
        // 过滤出开启的conf
        List<StaticTriggerConf> triggers =
                Optional.of(ActivityConst.TRIGGER_GIFT_DO_COMBAT)
                        .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                        .orElse(new ArrayList<>());
        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == combatId)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("首次通关关卡检测触发式礼包出错：", e);
                    }
                });
    }


    /**
     * 首次攻打指定城池, 会给全服的玩家推送触发式礼包
     *
     * @param cityType 城池类型
     */
    public void battleCampTriggerGift(int cityType) {
        playerDataManager.getAllPlayer().values().forEach(player -> {
            // 过滤出开启的conf
            List<StaticTriggerConf> triggers =
                    Optional.of(ActivityConst.TRIGGER_GIFT_ATK_CITY_SUC)
                            .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                            .orElse(new ArrayList<>());
            triggers.stream()
                    .filter(stc -> stc.getCond().get(0) == cityType)
                    .forEach(stc -> {
                        try {
                            activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                        } catch (MwException e) {
                            LogUtil.error("首次攻打指定城池检测触发式礼包出错：", e);
                        }
                    });
        });
    }

    /**
     * 首次通关帝国远征关卡
     *
     * @param player   玩家
     * @param combatId 副本id
     */
    public void doExpeditionCombatTriggerGift(Player player, int combatId) {
        if (player == null) {
            LogUtil.error("获取首次通关关卡触发式礼包时 error: player is null!");
            return;
        }
        // 过滤出开启的conf
        List<StaticTriggerConf> triggers =
                Optional.of(ActivityConst.TRIGGER_GIFT_EMPIRE_EXPEDITION_COMBAT)
                        .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                        .orElse(new ArrayList<>());
        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == combatId)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("首次通关帝国远征关卡检测触发式礼包出错：", e);
                    }
                });
    }


    /**
     * 首次授勋将领
     *
     * @param player     玩家对象
     * @param armyType   兵种类型
     * @param isActivate 是否觉醒
     */
    public void heroDecoratedTriggerGift(Player player, int armyType, boolean isActivate) {
        // 0 未觉醒, 1 已觉醒
        int activate = isActivate ? 1 : 0;
        if (player == null) {
            LogUtil.error("获取首次授勋将领触发式礼包时 error: player is null!");
            return;
        }
        // 过滤出开启的conf
        List<StaticTriggerConf> triggers =
                Optional.of(ActivityConst.TRIGGER_GIFT_HERO_DECORATED)
                        .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                        .orElse(new ArrayList<>());
        triggers.stream()
                // 根据兵种和觉醒状态过滤礼包
                .filter(stc -> stc.getCond().get(0) == armyType && stc.getCond().get(1) == activate)
                // 检测触发式礼包
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("首次授勋将领检测触发式礼包出错：", e);
                    }
                });

    }


    /**
     * 第一次获取 品质将领触发
     */
    public void awardHeroTriggerGift(Player player, int heroQuality) {
        if (player == null) {
            LogUtil.error("获取品质将领成功触发 error: player is null");
            return;
        }
        try {
            List<StaticTriggerConf> triggers =
                    Optional.of(ActivityConst.TRIGGER_GIFT_FIRST_RARE_HERO)
                            .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                            .orElse(new ArrayList<>());
            List<Integer> giftIds = triggers.stream()
                    // cond [1,4]  第几次获得，品质
                    .filter(t -> player.heros.values().stream()
                            // hero的品质之前没有存起来
                            .map(hero -> StaticHeroDataMgr.getHeroMap().get(hero.getHeroId()))
                            .filter(shero -> shero != null && !ActParamConstant.ACT_TRIGGER_HERO_IGNORE.contains(shero.getHeroId()) && shero.getQuality() == heroQuality)
                            .count() == t.getCond().get(0) && t.getCond().get(1) == heroQuality)
                    .map(StaticTriggerConf::getGiftId).collect(Collectors.toList());
            for (Integer giftId : giftIds) {
                activityService.checkTriggerGiftSyncByGiftId(giftId, player);
            }
        } catch (Exception e) {
            LogUtil.error("获取品质将领成功触发 error: ", e.getMessage());
        }
    }

    /**
     * 配件提升星级
     * @param player 玩家
     * @param star 星级
     */
    public void stoneImproveUpStar(Player player, int star) {
        if (Objects.isNull(player) || star == 0) {
            LogUtil.error("stone improve up star, trigger gift error: some entity is null");
            return;
        }
        List<StaticTriggerConf> triggers = Optional.of(ActivityConst.TRIGGER_STONE_IMPROVE_UP)
                        .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                        .orElse(new ArrayList<>());

        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == star)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("配饰升星检测触发式礼包出错：", e);
                    }
                });
    }


    /**
     * 通关某关卡的宝具副本
     *
     * @param player
     * @param combatId
     */
    public void doTreasureCombat(Player player, int combatId) {
        if (Objects.isNull(player) || combatId <= 0) {
            LogUtil.error("doTreasureCombat, trigger gift error: some entity is null");
            return;
        }

        List<StaticTriggerConf> triggers = Optional.of(ActivityConst.TRIGGER_DO_TREASURE_COMBAT)
                .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                .orElse(new ArrayList<>());

        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == combatId)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("宝具副本通关检测触发式礼包出错：", e);
                    }
                });
    }

    /**
     * 打造某品质的宝具
     *
     * @param player
     * @param quality
     */
    public void makeTreasureWare(Player player, int quality) {
        if (Objects.isNull(player) || quality <= 0) {
            LogUtil.error("makeTreasureWare, trigger gift error: some entity is null");
            return;
        }

        List<StaticTriggerConf> triggers = Optional.of(ActivityConst.TRIGGER_MAKE_TREASURE_WARE)
                .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                .orElse(new ArrayList<>());

        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == quality)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("打造某品质宝具检测触发式礼包出错：", e);
                    }
                });
    }

    /**
     * 强化某品质宝具到某等级
     *
     * @param player
     * @param quality
     * @param level
     */
    public void strengthTreasureWare(Player player, int quality, int level) {
        if (Objects.isNull(player) || quality <= 0 || level < 0) {
            LogUtil.error("strengthTreasureWare, trigger gift error: some entity is null");
            return;
        }

        List<StaticTriggerConf> triggers = Optional.of(ActivityConst.TRIGGER_STRENGTH_TREASURE_WARE)
                .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                .orElse(new ArrayList<>());

        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == quality && level == stc.getCond().get(1))
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("强化某品质宝具到某等级检测触发式礼包出错：", e);
                    }
                });
    }

    /**
     * 获得某个英雄
     *
     * @param player
     * @param heroId
     */
    public void getAnyHero(Player player, int heroId) {
        if (Objects.isNull(player) || heroId <= 0) {
            LogUtil.error("getAnyHero, trigger gift error: some entity is null");
            return;
        }

        List<StaticTriggerConf> triggers = Optional.of(ActivityConst.TRIGGER_GET_ANY_HERO)
                .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                .orElse(new ArrayList<>());

        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == heroId)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("获得某个英雄检测触发式礼包出错：", e);
                    }
                });
    }

    /**
     * 指定等级配饰
     * @param player 玩家
     * @param lv 配件等级
     */
    public void stoneUpLv(Player player, int lv) {
        if (Objects.isNull(player) || lv == 0) {
            LogUtil.error("stone up lv, trigger gift error: some entity is null");
            return;
        }
        List<StaticTriggerConf> triggers = Optional.of(ActivityConst.TRIGGER_ADD_STONE)
                .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
                .orElse(new ArrayList<>());
        triggers.stream()
                .filter(stc -> stc.getCond().get(0) == lv)
                .forEach(stc -> {
                    try {
                        activityService.checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
                    } catch (MwException e) {
                        LogUtil.error("配饰升级或获取检测触发式礼包出错：", e);
                    }
                });
    }

    /**
     * 圣域预显示触发式礼包
     */
    public void berlinWarPreViewTriggerGift() {
//        playerDataManager.getAllPlayer().values().forEach(player -> {
//            // 过滤出开启的conf
//            List<StaticTriggerConf> triggers =
//                    Optional.of(ActivityConst.TRIGGER_BERLIN_WAR_PRE)
//                            .map(i -> StaticActivityDataMgr.getTriggerGiftConfById(i, player))
//                            .orElse(new ArrayList<>());
//            triggers.forEach(stc -> {
//                try {
//                    checkTriggerGiftSyncByGiftId(stc.getGiftId(), player);
//                } catch (MwException e) {
//                    LogUtil.error("首次攻打指定城池检测触发式礼包出错：", e);
//                }
//            });
//        });
        // 记录世界进程阶段
        BerlinWar berlinWar = DataResource.ac.getBean(GlobalDataManager.class).getGameGlobal().getBerlinWar();
        int scheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
        if (Objects.nonNull(berlinWar)) {
            berlinWar.updateScheduleId(scheduleId);
        }
    }

    /**
     * 满足条件触发
     */
    private void syncTriggerGift(Player player, int buildType, int level, List<StaticTriggerConf> triggers) throws MwException {
        if (CheckNull.isEmpty(triggers)) {
            return;
        }
        List<Integer> giftIds = triggers.stream()
                // cond [1,8] [建筑类型，等级]
                .filter(t -> t.getCond().get(0) == buildType && t.getCond().get(1) == level)
                .map(StaticTriggerConf::getGiftId).collect(Collectors.toList());
        for (Integer giftId : giftIds) {
            activityService.checkTriggerGiftSyncByGiftId(giftId, player);
        }
    }

    /**
     * 推送触发式礼包
     *
     * @param player      玩家
     * @param conf        触发式礼包配置
     * @param plan        按时间出发的礼包配置
     * @param triggerGift 触发式礼包数据
     * @throws MwException 自定义异常
     */
    public void syncTriggerGift(Player player, StaticTriggerConf conf, StaticGiftPackTriggerPlan plan, TriggerGift triggerGift) throws MwException {

        StaticActGiftpack triggerGiftConf = StaticActivityDataMgr.getActGiftpackMapById(conf.getGiftId());
        GamePb3.SyncTriggerGiftRs.Builder builder = GamePb3.SyncTriggerGiftRs.newBuilder();
        if (CheckNull.isNull(triggerGiftConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:", player.roleId,
                    ", giftId:", conf.getGiftId());
        }
        // 未触发
        if (triggerGift.getState() == ActivityConst.NOT_TRIGGER_STATUS) {
            int now = TimeHelper.getCurrentSecond();
            triggerGift.setState(ActivityConst.TRIGGER_STATUS);
            triggerGift.setBeginTime(now);
            // 礼包的结束时间
            int endTime = Objects.isNull(plan) ? 0 : plan.getGiftEndTime(serverSetting.getOpenServerDate(), player.account.getCreateDate());
            if (endTime <= 0) {
                endTime = now + conf.getContinueTime();
            }
            triggerGift.setEndTime(endTime);
            builder.addTriggerGiftInfo(PbHelper.creteTriggerGiftsRs(conf, triggerGift, triggerGiftConf, player));
        }
        if (CheckNull.isEmpty(builder.getTriggerGiftInfoBuilderList())) {
            return;
        }
        if (player.isLogin && player.ctx != null) {
            BasePb.Base.Builder msg = PbHelper.createRsBase(GamePb3.SyncTriggerGiftRs.EXT_FIELD_NUMBER, GamePb3.SyncTriggerGiftRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            LogUtil.debug("推送触发式礼包" + player.roleId);
        }
    }

}
