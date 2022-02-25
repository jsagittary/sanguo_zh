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
 * @Description 跨服数据相关
 * @author QiuKun
 * @date 2019年5月15日
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

    // 是否参加了跨服
    private boolean isJoinCross = false;
    // v1 跨服的战场 v2 参加该跨服战场的主服id
    private CommonPb.IntListInt crossServerRulePb;

    /**
     * 初始和刷新
     * 
     * @param isStart 是否是启动时初始化
     */
    public void initAndRefresh(boolean isStart) {
        if (isStart) {
            EventBus.getDefault().register(this); // EventBus注册
        }
        StaticCrossServerRule sRule = StaticCrossDataMgr.getRuleList().stream()
                .filter(s -> s.getGameServerId() == serverSetting.getServerID()).findFirst().orElse(null);
        isJoinCross = sRule != null;
        crossOpenTimeService.refreshTime();
        // 计算该跨服的规则
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
            // 向跨服发送玩家退出的消息
//            playerForCrossService.offlinePlayerProcess(player);
            // 发送新跨服地图移除焦点
            DataResource.getBean(CrossGamePlayService.class).enterLeaveCrossMap(player);
        }
    }

    /**
     * 检测跨服是否开放
     * 
     * @return
     */
    public boolean isCrossOpen() {
        // 该服务器是否参加了跨服
        if (!isJoinCross) {
            return false;
        }
        return true;
    }

    /**
     * 检测该服是否开启跨服,并且在跨服战期间内
     * 
     * @return
     * @throws MwException
     */
    public void checkCrossIsOpenAndInTime() throws MwException {
        if (!(isCrossOpen() && crossOpenTimeService.isInCrossTimeCond())) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " 该服没有开启跨服或不在活动期间内");
        }
    }

    /**
     * 获取跨服信息(buff 和时间信息)
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
     * 购买跨服buff
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
        if (buff == null) {// 第一次购买这个buff
            sBuff = StaticCrossDataMgr.getBuffByTypeLv(buffType, 1);
        } else {
            sBuff = StaticCrossDataMgr.getBuffByTypeLv(buffType, buff.getLv() + 1);
        }
        if (sBuff == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId,
                    ", 配置未找到或buff等级已满  buffType:" + buffType);
        }
        // 扣钱
        int needGood = sBuff.getCost();
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGood,
                AwardFrom.BUY_CROSSBUFF, false, sBuff.getBuffId());
        PeriodTime curOpentTime = crossOpenTimeService.getCurOpentTime();

        // 给buff
        if (buff == null) {
            buff = new CrossBuff(curOpentTime.getStartTime(), curOpentTime.getEndTime(), sBuff.getType(),
                    sBuff.getLv());
        } else {
            buff.setLv(sBuff.getLv());// 升一级
        }
        crossData.getBuffs().put(buffType, buff); // 更新buff
        // 重新计算战斗力
        CalculateUtil.reCalcBattleHeroAttr(player);
        sendSyncCrossHero(player); // 同步跨服将领信息
        BuyCrossBuffRs.Builder builder = BuyCrossBuffRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setBuff(buff.toPb());
        return builder.build();
    }

    /**
     * 同步将领信息
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
     * 获取跨服成就信息
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
     * 领取跨服成就奖励
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "没有找到跨服个人成就配置 id:", id);
        }
        CrossPersonalData crossData = player.getAndCreateCrossPersonalData();
        if (crossData.getGainTrophyId().contains(id)) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "奖励已经领取过 id:", id, ",roleId:", roleId);
        }
        int killNum = crossData.getKillNum();
        if (killNum < trophyCfg.getKillNum()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "领取条件未达成 id:", id, ",roleId:", roleId);
        }
        crossData.getGainTrophyId().add(id); // 领取进度加入
        List<Award> awardPbList = rewardDataManager.addAwardDelaySync(player, trophyCfg.getAward(), null,
                AwardFrom.CROSS_TROPHY_AWARD, id);
        CrossTrophyAwardRs.Builder builder = CrossTrophyAwardRs.newBuilder();
        builder.addAllAward(awardPbList);
        return builder.build();
    }

}
