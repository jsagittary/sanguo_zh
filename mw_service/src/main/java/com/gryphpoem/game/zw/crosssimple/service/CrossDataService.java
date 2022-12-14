package com.gryphpoem.game.zw.crosssimple.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.crosssimple.util.CrossPlayerPbHelper;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.CrossHeroPb;
import com.gryphpoem.game.zw.pb.CrossPb.CrossHeroSyncRq;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossBuff;
import com.gryphpoem.game.zw.resource.domain.p.CrossPersonalData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossPersonalTrophy;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossServerRule;
import com.gryphpoem.game.zw.resource.pojo.PeriodTime;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @ClassName CrossBuffService.java
 * @Description ??????????????????
 * @author QiuKun
 * @date 2019???5???15???
 */
@Component
public class CrossDataService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossOpenTimeService crossOpenTimeService;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerForCrossService playerForCrossService;

    // ?????????????????????
    private boolean isJoinCross = false;
    // v1 ??????????????? v2 ??????????????????????????????id
    private CommonPb.IntListInt crossServerRulePb;

    /**
     * ???????????????
     * 
     * @param isStart ???????????????????????????
     */
    public void initAndRefresh(boolean isStart) {
        if (isStart) {
            EventBus.getDefault().register(this); // EventBus??????
        }
        StaticCrossServerRule sRule = StaticCrossDataMgr.getRuleList().stream()
                .filter(s -> s.getGameServerId() == serverSetting.getServerID()).findFirst().orElse(null);
        isJoinCross = sRule != null;
        crossOpenTimeService.refreshTime();
        // ????????????????????????
        if (isJoinCross) {
            CommonPb.IntListInt.Builder builder = CommonPb.IntListInt.newBuilder();
            List<Integer> gameServerList = StaticCrossDataMgr.getRuleList().stream()
                    .filter(s -> s.getCrossServerId() == sRule.getCrossServerId())
                    .map(StaticCrossServerRule::getGameServerId).collect(Collectors.toList());
            builder.setV1(sRule.getCrossServerId());
            builder.addAllV2(gameServerList);
            crossServerRulePb = builder.build();
        } else {
            crossServerRulePb = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onPlayerLogout(Events.PlayerLoginLogoutEvent event) {
        if (!event.isLogin) {
            Player player = event.player;
            // ????????????????????????????????????
//            playerForCrossService.offlinePlayerProcess(player);
            // ?????????????????????????????????
            DataResource.getBean(CrossGamePlayService.class).enterLeaveCrossMap(player);
        }
    }

    /**
     * ????????????????????????
     * 
     * @return
     */
    public boolean isCrossOpen() {
        // ?????????????????????????????????
        if (!isJoinCross) {
            return false;
        }
        return true;
    }

    /**
     * ??????????????????????????????,???????????????????????????
     * 
     * @return
     * @throws MwException
     */
    public void checkCrossIsOpenAndInTime() throws MwException {
        if (!(isCrossOpen() && crossOpenTimeService.isInCrossTimeCond())) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " ????????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????(buff ???????????????)
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCrossInfoRs getCrossInfo(long roleId, GetCrossInfoRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CrossPersonalData crossData = player.getAndCreateCrossPersonalData();
        int now = TimeHelper.getCurrentSecond();
        GetCrossInfoRs.Builder builder = GetCrossInfoRs.newBuilder();
        for (Iterator<Entry<Integer, CrossBuff>> it = crossData.getBuffs().entrySet().iterator(); it.hasNext();) {
            CrossBuff buff = it.next().getValue();
            if (now < buff.getStartTime() || now > buff.getEndTime()) {
                it.remove();
            } else {
                builder.addBuff(buff.toPb());
            }
        }
        PeriodTime curOpentTime = crossOpenTimeService.getCurOpentTime();
        if (curOpentTime != null) {
            builder.setStartTime(curOpentTime.getStartTime());
            builder.setEndTime(curOpentTime.getEndTime());
            builder.setPreTime(curOpentTime.getPreViewTime());
        }
        if (crossServerRulePb != null) {
            builder.setCrossServerRule(crossServerRulePb);
        }
        return builder.build();
    }

    /**
     * ????????????buff
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public BuyCrossBuffRs buyCrossBuff(long roleId, BuyCrossBuffRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int buffType = req.getBuffType();
        checkCrossIsOpenAndInTime();
        CrossPersonalData crossData = player.getAndCreateCrossPersonalData();
        CrossBuff buff = crossData.getBuffs().get(buffType);

        StaticCrossBuff sBuff = null;
        if (buff == null) {// ?????????????????????buff
            sBuff = StaticCrossDataMgr.getBuffByTypeLv(buffType, 1);
        } else {
            sBuff = StaticCrossDataMgr.getBuffByTypeLv(buffType, buff.getLv() + 1);
        }
        if (sBuff == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId,
                    ", ??????????????????buff????????????  buffType:" + buffType);
        }
        // ??????
        int needGood = sBuff.getCost();
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGood,
                AwardFrom.BUY_CROSSBUFF, false, sBuff.getBuffId());
        PeriodTime curOpentTime = crossOpenTimeService.getCurOpentTime();

        // ???buff
        if (buff == null) {
            buff = new CrossBuff(curOpentTime.getStartTime(), curOpentTime.getEndTime(), sBuff.getType(),
                    sBuff.getLv());
        } else {
            buff.setLv(sBuff.getLv());// ?????????
        }
        crossData.getBuffs().put(buffType, buff); // ??????buff
        // ?????????????????????
        CalculateUtil.reCalcBattleHeroAttr(player);
        sendSyncCrossHero(player); // ????????????????????????
        BuyCrossBuffRs.Builder builder = BuyCrossBuffRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setBuff(buff.toPb());
        return builder.build();
    }

    /**
     * ??????????????????
     * 
     * @param player
     */
    private void sendSyncCrossHero(Player player) {
        if (!player.getAndCreateCrossPersonalData().isInCross()) return;
        List<CrossHeroPb> crossHeroPbList = new ArrayList<>();
        for (Hero hero : player.getAllOnBattleHeros()) {
            if (hero.getState() == ArmyConstant.ARMY_STATE_CROSS
                    || hero.getState() == ArmyConstant.ARMY_STATE_CROSS_REVIVAL) {
                crossHeroPbList.add(CrossPlayerPbHelper.toCrossHeroPb(player, hero));
            }
        }
        if (!crossHeroPbList.isEmpty()) {
            CrossHeroSyncRq.Builder builder = CrossHeroSyncRq.newBuilder();
            builder.addAllHero(crossHeroPbList);
            DataResource.sendMsgToCross(PbCrossUtil.createBase(CrossHeroSyncRq.EXT_FIELD_NUMBER,
                    player.lord.getLordId(), CrossHeroSyncRq.ext, builder.build()).build());
        }
    }

    /**
     * ????????????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CrossTrophyInfoRs crossTrophyInfo(long roleId, CrossTrophyInfoRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CrossPersonalData crossData = player.getAndCreateCrossPersonalData();
        CrossTrophyInfoRs.Builder builder = CrossTrophyInfoRs.newBuilder();
        builder.setKillNum(crossData.getKillNum());
        builder.setCurKillNum(crossData.getCurKillNum());
        builder.addAllGainId(crossData.getGainTrophyId());
        return builder.build();
    }

    /**
     * ????????????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CrossTrophyAwardRs crossTrophyAward(long roleId, CrossTrophyAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int id = req.getId();
        StaticCrossPersonalTrophy trophyCfg = StaticCrossDataMgr.getPersonalTropyMap(id);
        if (trophyCfg == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????????????? id:", id);
        }
        CrossPersonalData crossData = player.getAndCreateCrossPersonalData();
        if (crossData.getGainTrophyId().contains(id)) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "????????????????????? id:", id, ",roleId:", roleId);
        }
        int killNum = crossData.getKillNum();
        if (killNum < trophyCfg.getKillNum()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????? id:", id, ",roleId:", roleId);
        }
        crossData.getGainTrophyId().add(id); // ??????????????????
        List<Award> awardPbList = rewardDataManager.addAwardDelaySync(player, trophyCfg.getAward(), null,
                AwardFrom.CROSS_TROPHY_AWARD, id);
        CrossTrophyAwardRs.Builder builder = CrossTrophyAwardRs.newBuilder();
        builder.addAllAward(awardPbList);
        return builder.build();
    }

}
