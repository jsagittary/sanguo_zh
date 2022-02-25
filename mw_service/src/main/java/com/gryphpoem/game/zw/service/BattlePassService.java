package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticBattlePassDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.BattlePassDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassTask;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.BattlePassPersonInfo;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.GlobalBattlePass;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.battlePassTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 战令相关功能
 *
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-02 11:35
 */
@Service
public class BattlePassService {


    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private BattlePassDataManager battlePassDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 获取战令的所有数据
     *
     * @param roleId 玩家的唯一ID
     * @return 战令的数据
     */
    public GamePb4.GetBattlePassRs getBattlePass(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_BATTLE_PASS)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "获取战令的所有数据,功能未解锁, roleId:", player.roleId);
        }
        GamePb4.GetBattlePassRs.Builder rs = GamePb4.GetBattlePassRs.newBuilder();
        GlobalBattlePass globalBattlePass = battlePassDataManager.getGlobalBattlePass();
        if (!CheckNull.isNull(globalBattlePass)) {
            CommonPb.BattlePass.Builder builder = CommonPb.BattlePass.newBuilder();
            builder.setPlan(PbHelper.createBattlePassPlanPb(globalBattlePass));
            BattlePassPersonInfo personInfo = battlePassDataManager.getPersonInfo(roleId);
            if (!CheckNull.isNull(personInfo)) {
                builder.addAllTasks(PbHelper.createBattlePassTaskPb(personInfo, battlePassDataManager, player));
                builder.addAllAwards(PbHelper.createBattleAwardPb(personInfo));
                builder.setInfo(PbHelper.createBattlePassInfoPb(personInfo));
            }
            rs.setBattlePass(builder.build());
        }
        return rs.build();
    }


    /**
     * 领取战令的任务
     *
     * @param roleId 玩家的唯一Id
     * @param taskId 任务的Id
     * @return 领取战令任务后的个人信息
     * @throws MwException 自定义异常
     */
    public GamePb4.ReceiveTaskAwardRs receiveTaskAward(long roleId, int taskId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_BATTLE_PASS)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "领取任务奖励时,功能未解锁, roleId:", player.roleId);
        }
        BattlePassPersonInfo personInfo = battlePassDataManager.getPersonInfo(roleId);
        if (CheckNull.isNull(personInfo)) {
            throw new MwException(GameError.BATTLE_PASS_NOT_OPEN.getCode(), "领取任务奖励时,活动没有开放, roleId:" + roleId + ",taskId=" + taskId);
        }
        int staticKey = personInfo.getStaticKey();
        StaticBattlePassTask sTaskConf = StaticBattlePassDataMgr.getTaskById(staticKey, taskId);
        if (CheckNull.isNull(sTaskConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "领取任务奖励时,配置找不到, roleId:" + roleId + ",taskId=" + taskId);
        }
        Map<Integer, battlePassTask> passTaskMap = personInfo.getPassTaskMap();
        battlePassTask task = passTaskMap.get(taskId);
        if (CheckNull.isNull(task)) {
            throw new MwException(GameError.NO_TASK.getCode(), "领取任务奖励时,无此任务, roleId:" + roleId + ",taskId=" + taskId);

        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "领取任务奖励时,重复领取, roleId:" + roleId);
        }
        if (task.getSchedule() < sTaskConf.getSchedule()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        // task增加日志记录 (基本信息、战力、关卡类型、关卡id)
        LogLordHelper.commonLog("battlePassTask", AwardFrom.COMMON, player, player.lord.getFight(), sTaskConf.getType(), sTaskConf.getId());

        int addExp = sTaskConf.getAward();
        // 加经验
        personInfo.addExp(addExp);
        task.setStatus(TaskType.TYPE_STATUS_REWARD);
        GamePb4.ReceiveTaskAwardRs.Builder builder = GamePb4.ReceiveTaskAwardRs.newBuilder();
        builder.setInfo(PbHelper.createBattlePassInfoPb(personInfo));
        return builder.build();
    }


    /**
     * 一键领取所有可领取的战令奖励
     *
     * @param roleId 玩家唯一的角色ID
     * @return 所有可领取的奖励
     * @throws MwException 自定义异常
     */
    public GamePb4.ReceiveBPAwardRs receiveBPAward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_BATTLE_PASS)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "领取战令奖励时,功能未解锁, roleId:", player.roleId);
        }
        BattlePassPersonInfo personInfo = battlePassDataManager.getPersonInfo(roleId);
        if (CheckNull.isNull(personInfo)) {
            throw new MwException(GameError.BATTLE_PASS_NOT_OPEN.getCode(), "领取战令奖励时,活动没有开放, roleId:" + roleId);
        }
        // 领取奖励
        List<List<Integer>> mergeAward = personInfo.receiveBPAward(player);
        List<CommonPb.Award> awards = rewardDataManager.addAwardDelaySync(player, mergeAward, null, AwardFrom.RECEIVE_BP_AWARD);
        // 埋点日志
        LogLordHelper.commonLog("battlePass", AwardFrom.RECEIVE_BP_AWARD, player, personInfo.getStaticKey(), personInfo.getLv());
        GamePb4.ReceiveBPAwardRs.Builder builder = GamePb4.ReceiveBPAwardRs.newBuilder();
        builder.addAllAwardsPb(PbHelper.createBattleAwardPb(personInfo));
        if (!CheckNull.isEmpty(awards)) {
            builder.addAllAward(awards);
        }
        return builder.build();
    }

    /**
     * 购买等级和每日领取
     *
     * @param roleId 玩家的唯一ID
     * @param req    请求参数
     * @return 领取后的玩家数据
     * @throws MwException 自定义异常
     */
    public GamePb4.BuyBattlePassLvRs buyBattlePassLv(long roleId, GamePb4.BuyBattlePassLvRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_BATTLE_PASS)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "购买等级和每日领取,功能未解锁, roleId:", player.roleId);
        }
        BattlePassPersonInfo personInfo = battlePassDataManager.getPersonInfo(roleId);
        if (CheckNull.isNull(personInfo)) {
            throw new MwException(GameError.BATTLE_PASS_NOT_OPEN.getCode(), "购买等级和每日领取,活动没有开放, roleId:" + roleId);
        }
        // 1 每日经验领取 2 购买等级
        int type = req.getType();
        // plan配表里的key
        int staticKey = personInfo.getStaticKey();
        GlobalBattlePass globalBattlePass = battlePassDataManager.getGlobalBattlePass();
        if (!CheckNull.isNull(globalBattlePass)) {
            int keyId = globalBattlePass.getStaticKey();

            StaticBattlePassPlan sPassPlan = StaticBattlePassDataMgr.getPlanById(keyId);
            if (CheckNull.isNull(sPassPlan)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "购买等级和每日领取,配置找不到, roleId:" + roleId + ",keyId=" + keyId);
            }
            if (type == 1) {
                // 未进阶也不能领取
                if (personInfo.getDailyExp() == 1 || personInfo.getAdvanced() == 0) {
                    throw new MwException(GameError.BATTLE_PASS_DAILY_EXP_HAD_GOLD.getCode(), "每日领取， 今天已经领取过经验了， roleId:" + roleId);
                }
                // 每日领取的经验
                int addExp = sPassPlan.getDailyExp();
                // 添加经验
                personInfo.addExp(addExp);
                // 设置为已领取
                personInfo.setDailyExp(1);
            } else if (type == 2) {
                // 要升级到的等级
                int lv = req.getLv();
                // 当前等级
                int curLv = personInfo.getLv();
                if (lv < 1 || lv > sPassPlan.getBuyLevel() || lv <= curLv) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "购买等级， 没有这个配置等级， roleId:" + roleId + ", lv:" + lv + ", curLv:" + curLv);
                }
                // 总消耗金币
                int sumCostGold = Stream.iterate(curLv, i -> ++i).limit(lv - curLv).map(i -> StaticBattlePassDataMgr.getLvAwardByPlanKey(staticKey, i)).filter(sPassLv -> !CheckNull.isNull(sPassLv)).mapToInt(StaticBattlePassLv::getSubGold).sum();
                // 消耗金币
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, sumCostGold, AwardFrom.BUY_BATTLEPASS_LV_COST, true);
                // 设置到想要的等级，经验清零
                personInfo.setLv(lv);
                StaticBattlePassLv sPassLv = StaticBattlePassDataMgr.getLvAwardByPlanKey(staticKey, lv - 1);
                personInfo.setExp(CheckNull.isNull(sPassLv) ? 0 : sPassLv.getNeedExp());
            }
        }
        GamePb4.BuyBattlePassLvRs.Builder builder = GamePb4.BuyBattlePassLvRs.newBuilder();
        builder.setInfo(PbHelper.createBattlePassInfoPb(personInfo));
        return builder.build();
    }
}