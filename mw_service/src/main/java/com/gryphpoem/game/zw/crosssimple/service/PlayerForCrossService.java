package com.gryphpoem.game.zw.crosssimple.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.constant.SimpleCrossConstant;
import com.gryphpoem.game.zw.crosssimple.util.CrossPlayerPbHelper;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.CrossAwardPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossPlayerPb.Builder;
import com.gryphpoem.game.zw.pb.CommonPb.FortHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.CrossPb.ChoiceHeroRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRs;
import com.gryphpoem.game.zw.pb.CrossPb.CrossHeroReviveRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRs;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossPersonalData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossWarRank;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.ArmyService;

/**
 * @ClassName PlayerForCrossService.java
 * @Description 游戏服中使用, 操作玩家跨服相关操作
 * @author QiuKun
 * @date 2019年5月15日
 */
@Component
public class PlayerForCrossService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossDataService crossDataService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ArmyService armyService;
    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 客户端 ->游戏服 -> 跨服 进入跨服
     * 
     * @param roleId
     * @param req
     * @throws MwException
     */
    public void enterCross(long roleId, EnterCrossRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 时间检测
        crossDataService.checkCrossIsOpenAndInTime();
        // 检测玩家是否能进入的跨服条件
        checkEnterCrossCond(player);
        int opType = req.getOpType();
        if (opType == SimpleCrossConstant.OP_TYPE_LOGIN) {// 1. 报名跨服
            loginCrossProcess(player);
        } else if (opType == SimpleCrossConstant.OP_TYPE_CLOSE) { // 2. 关闭跨服界面
            closeCrossProcess(player);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "进入跨服没有此操作");
        }
    }

    /**
     * 登陆跨服
     * 
     * @param player
     */
    public void loginCrossProcess(Player player) {
        Builder crossPlayerPb = CrossPlayerPbHelper.toCrossPlayerPb(player); // 传入基础数据
        CrossLoginRq.Builder baseBuilder = CrossLoginRq.newBuilder();
        baseBuilder.setOpType(SimpleCrossConstant.OP_TYPE_LOGIN);
        player.getAndCreateCrossPersonalData().enterCross();
        baseBuilder.setPlayer(crossPlayerPb.build());
        DataResource.sendMsgToCross(PbCrossUtil.createBase(CrossLoginRq.EXT_FIELD_NUMBER, player.lord.getLordId(),
                CrossLoginRq.ext, baseBuilder.build()).build());
    }

    /**
     * 关闭跨服界面
     * 
     * @param player
     * @throws MwException
     */
    public void closeCrossProcess(Player player) throws MwException {
        crossCloseAndLogoutOperate(player, SimpleCrossConstant.OP_TYPE_CLOSE);
    }

    /**
     * 玩家离线
     * 
     * @param player
     */
    public void offlinePlayerProcess(Player player) {
        crossCloseAndLogoutOperate(player, SimpleCrossConstant.OP_TYPE_OFFLINE);
    }

    private void crossCloseAndLogoutOperate(Player player, int opType) {
        if (player.getAndCreateCrossPersonalData().isInCross()) { // 在跨服中
            CrossLoginRq.Builder baseBuilder = CrossLoginRq.newBuilder();
            baseBuilder.setOpType(opType);
            DataResource.sendMsgToCross(PbCrossUtil.createBase(CrossLoginRq.EXT_FIELD_NUMBER, player.lord.getLordId(),
                    CrossLoginRq.ext, baseBuilder.build()).build());
        }
    }

    /**
     * 检测玩家是否在跨服中
     * 
     * @param player
     * @throws MwException
     */
    private void checkPlayerInCross(Player player) throws MwException {
        if (!player.getAndCreateCrossPersonalData().isInCross()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "不在跨服中不能进行操作 roleId:" + player.roleId);
        }
    }

    /**
     * 检测跨服条件
     * 
     * @param player
     */
    private void checkEnterCrossCond(Player player) throws MwException {
        // 检测玩家进入跨服
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ENTER_CROSS_WAR)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "跨服战条件未达到不能进入, roleId:", player.roleId);
        }
    }

    /**
     * 选将领加入战斗
     * 
     * @param roleId
     * @param req
     * @throws MwException
     */
    public void choiceHeroJoin(long roleId, ChoiceHeroJoinRq req) throws MwException {
        final Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkPlayerInCross(player);
        List<Integer> heroIdList = req.getHeroIdList();
        if (heroIdList.isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "没有选择任何将领 roleId:" + player.roleId);
        }
        List<Hero> heroList = new ArrayList<>(4);
        for (int heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            if (hero == null) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "不存在此将领 roleId:" + player.roleId, ", heroId:",
                        heroId);
            }
            if (!hero.isOnBattle()) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "非上阵将领不能参加跨服 roleId:" + player.roleId,
                        ", heroId:", heroId);
            }
            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲状态 roleId:" + player.roleId,
                        ", heroId:", heroId);
            }
            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_COUNT_ERROR.getCode(), "将领兵力不足 roleId:" + player.roleId,
                        ", heroId:", heroId);
            }
            heroList.add(hero);
        }
        List<CrossHeroPb> crossHeroPbList = heroList.stream().map(hero -> {
            hero.setState(ArmyConstant.ARMY_STATE_CROSS); // 修改本服的将领状态
            return CrossPlayerPbHelper.toCrossHeroPb(player, hero);
        }).collect(Collectors.toList());

        // 发消息给跨服
        ChoiceHeroRq.Builder builder = ChoiceHeroRq.newBuilder();
        builder.addAllHeros(crossHeroPbList);
        DataResource.sendMsgToCross(PbCrossUtil
                .createBase(ChoiceHeroRq.EXT_FIELD_NUMBER, roleId, ChoiceHeroRq.ext, builder.build()).build());
    }

    /**
     * cross服 -> 游戏服 CrossLoginRq 的回调
     * 
     * @param lordId
     */
    public void crossLoginCallBack(long lordId, CrossLoginRs msg) {
        Player player = playerDataManager.getPlayer(lordId);
        if (player == null) return;
        if (msg.getOpType() == SimpleCrossConstant.OP_TYPE_LOGOUT) {
            // 退出跨服
            player.getAndCreateCrossPersonalData().exitCross();
        }
    }

    /**
     * 跨服扣除资源处理
     * 
     * @param lordId
     * @param msg
     * @return
     * @throws MwException
     */
    public CrossAwardOpRq crossAwardOp(long lordId, CrossAwardOpRs msg, Consumer<Integer> consumer) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        CrossAwardPb reqAwards = msg.getReqAwards();
        List<Award> awardList = reqAwards.getAwardList();
        if (CheckNull.isEmpty(awardList)) {
            LogUtil.error("资源同步操作award为空");
            return null;
        }
        boolean rollBack = msg.getRollBack();
        boolean isAdd = rollBack ? !reqAwards.getIsAdd() : reqAwards.getIsAdd();
        AwardFrom awardFrom = AwardFrom.getAwardFrom(reqAwards.getFrom());
        CrossAwardOpRq.Builder rs = CrossAwardOpRq.newBuilder();
        String param = String.valueOf(rollBack);
        boolean success = true;
        if (isAdd) {
            rewardDataManager.sendRewardByAwardList(player, awardList, awardFrom, param);
        } else {
            List<List<Integer>> subList = awardList.stream().map(awardPb -> {
                List<Integer> a = new ArrayList<>(3);
                a.add(awardPb.getType());
                a.add(awardPb.getId());
                a.add(awardPb.getCount());
                return a;
            }).collect(Collectors.toList());
            try {
                // 扣除资源
                rewardDataManager.checkAndSubPlayerRes(player, subList, awardFrom, param);
            } catch (MwException e) {
                LogUtil.error("跨服请求操作,资源不够,lordId:" + lordId + ", rollBack:" + rollBack, e);
                success = false;
                consumer.accept(e.getCode()); // 向客户端发送错误消息
            } catch (Exception e) {
                LogUtil.error("跨服请求操作资源 异常,lordId:" + lordId + ", rollBack:" + rollBack, e);
                success = false;
            }
        }
        rs.setSuccess(success);
        rs.setReqAwards(reqAwards);
        rs.setTaskId(msg.getTaskId());
        return rs.build();
    }

    /**
     * 将领复活的前置条件判断
     * 
     * @param roleId
     * @param req
     * @throws MwException
     */
    public void heroRevive(long roleId, OpFortHeroRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int heroId = req.getHeroId();
        Hero hero = player.heros.get(heroId);
        if (hero == null) {
            throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "不存在此将领 roleId:" + player.roleId, ", heroId:",
                    req.getHeroId());
        }
        if (hero.getState() != ArmyConstant.ARMY_STATE_CROSS_REVIVAL) {
            throw new MwException(GameError.BERLIN_HERO_STATUS_ERROR.getCode(), "将领状态不是复活中 roleId:" + player.roleId,
                    ", heroId:", req.getHeroId());
        }
        // 检测是否有兵
        rewardDataManager.checkPlayerResIsEnough(player, AwardType.ARMY, hero.getType(), 1);
        // 复活扣钱逻辑
        List<Integer> costCfg = Constant.CROSS_REVIVE_HERO_COST;
        Map<Integer, Integer> reiveHeroCnt = player.getAndCreateCrossPersonalData().getReiveHeroCnt();
        int cnt = reiveHeroCnt.getOrDefault(heroId, 0).intValue();
        int cost = 10;
        if (cnt >= costCfg.size()) {
            cost = costCfg.get(costCfg.size() - 1); // 取最后一个
        } else {
            cost = costCfg.get(cnt);
        }
        // 扣钱
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, cost,
                AwardFrom.CROSS_HERO_REVIVE, true);
        reiveHeroCnt.put(heroId, cnt + 1);
        // 补兵
        armyService.autoAddArmySingle(player, hero);
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.ARMY, hero.getType());
        change.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
        hero.setState(ArmyConstant.ARMY_STATE_CROSS); // 改变将领陈为跨服
        rewardDataManager.syncRoleResChanged(player, change);
        // 发给跨服进行复活操作
        CrossHeroReviveRq.Builder builder = CrossHeroReviveRq.newBuilder();
        CommonPb.CrossHeroPb.Builder crossHeroBuilderPb = CrossPlayerPbHelper.toCrossHeroBuilderPb(player, hero);
        crossHeroBuilderPb.setRevivalCnt(cnt);
        builder.setHero(crossHeroBuilderPb);
        Base base = PbCrossUtil
                .createBase(CrossHeroReviveRq.EXT_FIELD_NUMBER, roleId, CrossHeroReviveRq.ext, builder.build()).build();
        DataResource.sendMsgToCross(base);
    }

    /**
     * 同步将领信息
     * 
     */
    public void syncFortHeroProcess(long roleId, SyncFortHeroRs req) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            return;
        }
        FortHeroPb fortHeroPb = req.getHero();
        Hero hero = player.heros.get(fortHeroPb.getHeroId());
        if (hero == null) {
            LogUtil.error("跨服同步将领时,将领不存在... ");
            return;
        }
        if (fortHeroPb.getState() == ArmyConstant.ARMY_STATE_CROSS) { // 说明没有死，只用更新兵力
            hero.setState(fortHeroPb.getState());
            hero.setCount(fortHeroPb.getCount());
        } else if (fortHeroPb.getState() == ArmyConstant.ARMY_STATE_CROSS_REVIVAL) { // 说明死了 复活中
            hero.setState(fortHeroPb.getState());
            hero.setCount(0);
        } else if (fortHeroPb.getState() == ArmyConstant.ARMY_STATE_IDLE) { // 已经复活,需要变成空闲状态
            hero.setState(fortHeroPb.getState());
            // 移除复活次数
            player.getAndCreateCrossPersonalData().getReiveHeroCnt().remove(hero.getHeroId());
            // 触发自动补兵
            playerDataManager.autoAddArmy(player);
        }
        if (req.hasKillNum()) {
            int killNum = req.getKillNum();
            CrossPersonalData crossData = player.getAndCreateCrossPersonalData();
            crossData.addCurKillNum(killNum);
        }
    }

    /**
     * 跨服结束玩家数据数据清除
     * 
     * @param player
     */
    public void crossWarFinishClear(Player player) {
        if (player == null) return;
        // 将领状态清除
        for (Hero hero : player.getAllOnBattleHeros()) {
            if (hero != null && (hero.getState() == ArmyConstant.ARMY_STATE_CROSS
                    || hero.getState() == ArmyConstant.ARMY_STATE_CROSS_REVIVAL)) {
                hero.setState(ArmyConstant.ARMY_STATE_IDLE); // 设置空闲状态
            }
        }
        // 将领复活次数重置
        player.getAndCreateCrossPersonalData().crossFinishClear();
    }

    /**
     * 跨服战结束处理
     * 
     * @param roleId
     * @param req
     */
    public void syncCrossWarFinish(long roleId, SyncCrossWarFinishRs req) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            return;
        }
        crossWarFinishClear(player);
        // 需要重新计算一下战斗力
        CalculateUtil.reCalcBattleHeroAttr(player);
        // 跨服结束发邮件奖励
        if (req.getKillNum() > 0) {// 有杀敌才能有奖励
            int now = TimeHelper.getCurrentSecond();
            int winCamp = req.getWinCamp();
            if (player.lord.getCamp() == winCamp) { // 优胜奖
                mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(Constant.CROSS_WIN_AWARD),
                        MailConstant.MOLD_CROSS_WAR_WIN_AWARD, AwardFrom.COMMON, now, winCamp);
            } else {// 参与将
                mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(Constant.CROSS_JOIN_AWARD),
                        MailConstant.MOLD_CROSS_WAR_JOIN_AWARD, AwardFrom.COMMON, now, winCamp);
            }
            // 排行奖励
            for (TwoInt kv : req.getRankResList()) {
                int rankType = kv.getV1();
                Map<Integer, StaticCrossWarRank> rankCfg = StaticCrossDataMgr.getRankByType(rankType);
                if (rankCfg == null) continue;
                int ranking = kv.getV2();
                StaticCrossWarRank rankItemCfg = rankCfg.get(ranking);
                if (rankItemCfg == null) continue;
                if (StaticCrossWarRank.RANK_TYPE_CAMP == rankType) {
                    mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(rankItemCfg.getAward()),
                            MailConstant.MOLD_CROSS_WAR_CAMP_AWARD, AwardFrom.COMMON, now, player.lord.getCamp(),
                            ranking);
                } else if (StaticCrossWarRank.RANK_TYPE_PERSONAL == rankType) {
                    mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(rankItemCfg.getAward()),
                            MailConstant.MOLD_CROSS_WAR_PERSONAL_AWARD, AwardFrom.COMMON, now, ranking);
                }
            }
        }
    }

}
