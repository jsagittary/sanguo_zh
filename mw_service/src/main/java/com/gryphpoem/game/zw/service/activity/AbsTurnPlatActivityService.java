package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ActTurnplat;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticTurnplateConf;
import com.gryphpoem.game.zw.resource.domain.s.StaticTurnplateExtra;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;

public interface AbsTurnPlatActivityService {
    /**
     * 获取转盘信息pb
     *
     * @param player
     * @param activity
     * @return
     */
    default CommonPb.ActTurnPlatInfo.Builder getTurnPlatPb(Player player, Activity activity) {
        long roleId = player.lord.getLordId();
        ActTurnplat actTurnPlat = (ActTurnplat) activity;
        if (CheckNull.isNull(actTurnPlat)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启, roleId:,", player.lord.getLordId(), ", type:",
                    activity.getActivityType());
        }

        CommonPb.ActTurnPlatInfo.Builder builder = CommonPb.ActTurnPlatInfo.newBuilder();
        List<StaticTurnplateConf> tpConfList = getTurnPlateConfList(activity.getActivityId(), roleId);
        builder.setFreeCount(actTurnPlat.getRefreshCount());
        for (StaticTurnplateConf conf : tpConfList) {
            builder.addInfo(PbHelper.createTurnplateInfo(conf));
        }

        StaticTurnplateConf conf = tpConfList.get(0);
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

        List<List<Integer>> getItem = conf.getGetItem();
        for (List<Integer> item : getItem) {
            if (item.size() < 3) {
                continue;
            }
            builder.addGetItem(PbHelper.createAwardPb(item.get(0), item.get(1), item.get(2)));
        }
        addCntStatus(actTurnPlat, builder);
        return builder;
    }

    /**
     * 获取转盘配置
     *
     * @param actId
     * @param roleId
     * @return
     */
    default List<StaticTurnplateConf> getTurnPlateConfList(int actId, long roleId) {
        List<StaticTurnplateConf> turnPlateConfList = StaticActivityDataMgr
                .getActTurnPlateListByActId(actId);
        if (CheckNull.isEmpty(turnPlateConfList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "转盘找不到配置, roleId:", roleId);
        }

        return turnPlateConfList;
    }

    /**
     * 获取单个奖励配置信息
     *
     * @param id
     * @param roleId
     * @return
     */
    default StaticTurnplateConf getTurntableConf(int id, long roleId) {
        StaticTurnplateConf turntableConf = StaticActivityDataMgr.getActTurnPlateById(id);
        if (CheckNull.isNull(turntableConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "转盘找不到配置, roleId:", roleId);
        }

        return turntableConf;
    }

    default void doSearchWinCnt(ActTurnplat turntable, StaticTurnplateConf conf) {
        // 清除之前记录的次数节点
        turntable.getWinCnt().clear();
        try {
            List<Integer> downProbability = conf.getDownProbability();
            RandomHelper.randomWinCnt(downProbability, turntable.getWinCnt());
            List<Integer> upProbability = conf.getUpProbability();
            RandomHelper.randomWinCnt(upProbability, turntable.getWinCnt());
        } catch (Exception e) {
            LogUtil.error("计算获取特殊道具的次数节点，初始节点发生异常, ", e);
        }
    }

    /**
     * 检验转盘次数
     *
     * @param turntable
     * @param costType
     * @param turntableConf
     * @param player
     * @return
     */
    boolean checkDrawCount(ActTurnplat turntable, int costType, StaticTurnplateConf turntableConf, Player player);

    /**
     * 随机奖励
     *
     * @param costType
     * @param turntable
     * @param conf
     * @param player
     * @param integral
     * @return
     */
    default List<Integer> doSweepstakes(int costType, ActTurnplat turntable, StaticTurnplateConf conf, Player player, int integral) {
        boolean flag = false;// 是否发生跑马灯
        int goldCnt = turntable.getGoldCnt();
        Set<Integer> winCnt = turntable.getWinCnt();// 抽中特殊奖励的节点
        List<Integer> awardList = new ArrayList<>();
        ActivityService activityService = DataResource.ac.getBean(ActivityService.class);
        if (ActivityConst.LUCKY_TURNPLATE_FREE == costType || ActivityConst.LUCKY_TURNPLATE_PROP == costType) { // 免费抽奖
            // 根据权重获取奖励
            awardList = activityService.doSweepstakesAwards(conf, player, turntable);
            activityService.getTurnplatePoint(integral, awardList, 4);
        } else if (ActivityConst.LUCKY_TURNPLATE_GOLD == costType) { // 金币抽奖
            if (winCnt.contains(goldCnt)) { // 达到特殊奖励条件
                awardList = !CheckNull.isEmpty(conf.getOnlyAward()) ? conf.getOnlyAward().get(0) : new ArrayList<>();
                activityService.getTurnplatePoint(integral, awardList, 3);
                DataResource.ac.getBean(ActivityDataManager.class).updActivity(player, conf.getType(), 1, ActTurnplat.SPECIAL_SORT, false); // 特殊道具在活动期间获取次数
                flag = true;// 抽中特殊道具 发送跑马灯
            } else { // 普通金币抽奖
                // 根据权重获取奖励
                awardList = activityService.doSweepstakesAwards(conf, player, turntable);
                activityService.getTurnplatePoint(integral, awardList, 4);
            }
        } else if (ActivityConst.LUCKY_TURNTABLE_ACT_EXCLUSIVE_TIMES == costType) {
            //活动专属次数
            // 根据权重获取奖励
            awardList = activityService.doSweepstakesAwards(conf, player, turntable);
            flag = true;
        }

        // 如果抽到的奖励是将领 且玩家已有该将领 则奖励给6张劵
        checkAwardList(awardList, activityService, player, turntable);

        // 判断抽中的奖励是否需要发生跑马灯
        if (flag)
            sendChat(awardList, conf, player, turntable);
        return awardList;
    }

    /**
     * 检验随机出的奖励
     *
     * @param awardList
     * @param activityService
     * @param player
     * @param turntable
     * @return
     */
    default List<Integer> checkAwardList(List<Integer> awardList, ActivityService activityService, Player player, ActTurnplat turntable) {
        if (awardList.size() > 1 && awardList.get(0) == AwardType.HERO) {
            if (activityService.checkAwardHasHero(awardList, player)) {
                int heroId = awardList.get(1);
                awardList.clear();
                awardList.addAll(Arrays.asList(AwardType.HERO_FRAGMENT, heroId, HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS));
            }
        }
        return awardList;
    }

    /**
     * 发送跑马灯
     *
     * @param awardList
     * @param conf
     * @param player
     * @param turntable
     */
    default void sendChat(List<Integer> awardList, StaticTurnplateConf conf, Player player, ActTurnplat turntable) {
        // 判断抽中的奖励是否需要发生跑马灯
        if (checkAwardChat(awardList)) {
            ChatDataManager chatDataManager = DataResource.ac.getBean(ChatDataManager.class);
            // 发送跑马灯
            chatDataManager.sendSysChat(getChatConstId(), player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), awardList.get(0),
                    awardList.get(1), awardList.get(2), turntable.getActivityId());
            //活动消息推送
            int chatId = getChatId();
            chatDataManager.sendActivityChat(chatId, conf.getType(), 0,
                    player.lord.getCamp(), player.lord.getNick(), awardList.get(0), awardList.get(1), awardList.get(2), turntable.getActivityId());
        }
    }

    default boolean checkAwardChat(List<Integer> awardList) {
        if (awardList == null || awardList.size() < 2) {
            return false;
        }
        for (List<Integer> list : getPropIdsInChat()) {
            if (list.size() > 1 && list.get(0).intValue() == awardList.get(0).intValue()
                    && list.get(1).intValue() == awardList.get(1).intValue()) {
                return true;
            }
        }
        return false;
    }

    int getChatId();

    int getChatConstId();

    /**
     * 发送转盘奖励
     *
     * @param turntableConf
     * @param turntable
     * @param player
     * @param change
     */
    default List<CommonPb.Award> sendTurntableAward(StaticTurnplateConf turntableConf, ActTurnplat turntable, Player player, ChangeInfo change) {
        // 转盘奖励
        List<CommonPb.Award> awardPbList = null;
        List<List<Integer>> awards = new ArrayList<>();
        for (int i = 0; i < turntableConf.getCount(); i++) {
            // 记录累计抽取次数
            awards.add(doSweepstakes(ActivityConst.LUCKY_TURNTABLE_ACT_EXCLUSIVE_TIMES, turntable, turntableConf, player, 0));
            turntable.setRefreshCount(turntable.getRefreshCount() - 1);
            turntable.setCnt(turntable.getCnt() + 1);
        }
        if (!CheckNull.isEmpty(awards)) {
            awardPbList = DataResource.ac.getBean(RewardDataManager.class).
                    addAwardDelaySync(player, awards, change, AwardFrom.DRAW_MAGIC_TW_TURNTABLE_AWARD,
                            DataResource.ac.getBean(ServerSetting.class).getServerID(), turntable.getActivityKeyId());
        }

        return awardPbList;
    }

    /**
     * 领取转盘次数箱子奖励
     *
     * @param roleId
     * @param actType
     * @param keyId
     * @return
     */
    default CommonPb.ReceiveTurntableCntAwardResult.Builder receiveCntAward(long roleId, int actType, int keyId) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticTurnplateExtra sTurnPlateExtra = StaticActivityDataMgr.getActTurnPlateExtraById(keyId);
        if (CheckNull.isNull(sTurnPlateExtra)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 转盘找不到配置, roleId:", roleId);
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 转盘活动未开启,或未初始化 roleId:", roleId);
        }

        ActTurnplat turntable = (ActTurnplat) activityDataManager.getActivityInfo(player, actType);
        if (CheckNull.isNull(turntable)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 转盘活动未开启, roleId:,", roleId, ", type:",
                    actType);
        }
        // 累计次数
        int cnt = turntable.getCnt();
        if (cnt < sTurnPlateExtra.getTimes()) {
            throw new MwException(GameError.TURNPLATE_CNT_AWARD_ERROR.getCode(), "领取转盘次数奖励错误, 次数没达到, roleId:", roleId, ", cnt:", cnt, ", needCnt:", sTurnPlateExtra.getTimes());
        }
        int status = turntable.getStatusMap().getOrDefault(keyId, 0);
        if (status != 0) {
            throw new MwException(GameError.TURNPLATE_CNT_AWARD_ERROR.getCode(), "领取转盘次数奖励错误, 已结领取过了, roleId:", roleId, ", status:", status);
        }

        CommonPb.ReceiveTurntableCntAwardResult.Builder builder = CommonPb.ReceiveTurntableCntAwardResult.newBuilder();
        List<List<Integer>> awardList = sTurnPlateExtra.getAwardList();
        if (!CheckNull.isEmpty(awardList)) {
            for (List<Integer> award : awardList) {
                builder.addAward(rewardDataManager.addAwardSignle(player, award, AwardFrom.TURNPLATE_CNT_AWARD));
            }
        }

        // 设置奖励为已领取状态
        turntable.getStatusMap().put(keyId, 1);
        builder.addAllBoxStatus(turntable.getStatusMap().entrySet().stream().
                map(entrySet -> PbHelper.createTwoIntPb(entrySet.getKey(), entrySet.getValue())).collect(Collectors.toList()));
        return builder;
    }

    void addCntStatus(ActTurnplat turntable, CommonPb.ActTurnPlatInfo.Builder builder);

    void updateActDrawCount(Player player, long progress, Object... param);

    /**
     * 获取转盘信息
     *
     * @param activityType
     * @param roleId
     * @param player
     * @return
     */
    default ActTurnplat getActTurntable(int activityType, long roleId, Player player) {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (CheckNull.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启,或未初始化 roleId:", roleId);
        }

        ActTurnplat turntable = (ActTurnplat) DataResource.ac.getBean(ActivityDataManager.class).getActivityInfo(player, activityType);
        if (CheckNull.isNull(turntable)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 幸运/名将 转盘活动未开启, roleId:,", roleId, ", type:",
                    activityType);
        }

        return turntable;
    }

    /**
     * 其他功能增加转盘次数
     *
     * @param player
     * @param progress
     * @param param
     */
    static void updateDrawCount(Player player, long progress, Object... param) {
        Map<String, AbsTurnPlatActivityService> resultMap = DataResource.ac.getBeansOfType(AbsTurnPlatActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return;

        resultMap.values().forEach(service -> {
            service.updateActDrawCount(player, progress, param);
        });
    }

    List<List<Integer>> getPropIdsInChat();

}

