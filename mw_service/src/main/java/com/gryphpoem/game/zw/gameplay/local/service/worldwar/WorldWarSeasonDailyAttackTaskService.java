package com.gryphpoem.game.zw.gameplay.local.service.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRs;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarDailyTask;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService.WORLD_WAR_DAILY_ATTACK_AWARD;

/**
 * Created by pengshuo on 2019/4/1 17:19
 * <br>Description: 世界争霸-赛季任务-每日杀敌任务
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonDailyAttackTaskService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private WorldWarSeasonDateService worldWarSeasonDateService;

    /**
     * 世界争霸 - 赛季任务 - 每日杀敌奖励领取
     *
     * @param lordId
     * @param rq
     * @return
     */
    public WorldWarAwardRs playerGetDailyTaskAward(long lordId, WorldWarAwardRq rq) throws MwException {
        int keyId = rq.getKeyId();
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Map<Integer, Integer> dailyTaskAward = Optional.ofNullable(player.getPlayerWorldWarData().getDailyAttackAward()).orElse(new HashMap<>(2));
        // 已领取
        int hasChange = Optional.ofNullable(dailyTaskAward.get(keyId)).orElse(0);
        StaticWorldWarDailyTask award = StaticCrossWorldDataMgr.getWorldWarDailyTask(keyId);
        if (award == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId: ", lordId);
        }
        if (hasChange > 0) {
            throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取奖励已达上限, roleId: ", lordId);
        }
        // 领取奖励所需活跃值
        int value = award.getValue();
        // 奖励
        List<List<Integer>> awardList = award.getAward();
        int integral = player.getPlayerWorldWarData().getDailyAttack();
        if (integral < value) {
            throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(), "世界争霸-赛季任务-每日任务-玩家积分不足, roleId: ",
                    lordId, ", need:", value, ", have:", integral);
        }
        if (awardList == null || awardList.isEmpty()) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "awardList 找不到配置, roleId: ", lordId);
        }
        // 检查玩家背包
        rewardDataManager.checkBag(player, awardList);
        // 发放奖励
        // 玩家获得装备 "[type,id,cnt]"
        List<CommonPb.Award> awards = rewardDataManager.sendReward(player, awardList, AwardFrom.WORLD_WAR_DAILY_TASK_AWARD, keyId);
        // 保存以获取状态
        dailyTaskAward.put(keyId, ++hasChange);
        player.getPlayerWorldWarData().setDailyAttackAward(dailyTaskAward);
        // 推送红点
        worldWarSeasonDateService.syncAwardChange(player, WORLD_WAR_DAILY_ATTACK_AWARD, getDailyTips(player));
        // 返回结果
        WorldWarAwardRs.Builder builder = WorldWarAwardRs.newBuilder();
        // 对返回结果进行处理
        builder.addAllAward(awards);
        builder.setKeyId(keyId);
        builder.setType(rq.getType());
        return builder.build();
    }


    /**
     * 增加玩家每日的杀敌数量 （跨服）
     *
     * @param player
     */
    public void addPlayerDailyAttackOther(Player player, int value) {
        if (player != null && worldWarSeasonDateService.isInSeason() && worldWarSeasonDateService.functionIsOpen(player)) {
            // 个人积分
            player.getPlayerWorldWarData().setDailyAttack(
                    player.getPlayerWorldWarData().getDailyAttack() + value
            );
            // 每日杀敌数量
            LogLordHelper.commonLog("worldWar", AwardFrom.WORLD_WAR_DAILY_TASK_INTEGRAL
                    , player, player.lord.getCamp(), value, "dailyAttack");
            // 异步推送红点
            int tips = getDailyTips(player);
            if (tips > 0) {
                worldWarSeasonDateService.syncAwardChange(player, WORLD_WAR_DAILY_ATTACK_AWARD, tips);
            }
        }
    }

    /**
     * 增加玩家每日的杀敌数量 （跨服）
     *
     * @param forces
     */
    public void addPlayerDailyAttackOther(List<Force> forces) {
        try {
            if (forces != null) {
                forces.stream().filter(f -> f.roleType == 1 && f.ownerId != 0).forEach(f ->
                        addPlayerDailyAttackOther(playerDataManager.getPlayer(f.ownerId), f.killed)
                );
            }
        } catch (Exception e) {
            LogUtil.error("增加玩家每日的杀敌数量 error", e);
        }
    }

    /**
     * 赛季任务 每日结束，发放玩家未领取的每日任务奖励,并清空玩家当前每日的记录值
     */
    public void dailyOverGiveAward() {
        if (worldWarSeasonDateService.isInSeason()) {
            LogUtil.common("赛季任务-每日杀敌任务结束（发放玩家当天未领取奖励）starting");
            int now = TimeHelper.getCurrentSecond();
            // 赛季任务每周任务奖励
            List<StaticWorldWarDailyTask> dailyTasks = StaticCrossWorldDataMgr
                    .getWorldWarDailyTaskList(worldWarSeasonDateService.worldWarType());
            if (dailyTasks != null && !dailyTasks.isEmpty()) {
                playerDataManager.getPlayers().entrySet().stream().filter(
                        e -> worldWarSeasonDateService.functionIsOpen(e.getValue())).forEach(e -> {
                    int dailyTask = e.getValue().getPlayerWorldWarData().getDailyAttack();
                    Map<Integer, Integer> dailyTaskAward = e.getValue().getPlayerWorldWarData().getDailyAttackAward();
                    List<List<Integer>> awards = new ArrayList<>();
                    dailyTasks.forEach(w -> {
                        // 未领取并且大于领奖条件发放奖励
                        if (!dailyTaskAward.containsKey(w.getId()) && dailyTask >= w.getValue()) {
                            // 汇总至一个邮件发送
                            awards.addAll(w.getAward());
                        }
                    });
                    if (!awards.isEmpty()) {
                        mailDataManager.sendAttachMail(e.getValue(), PbHelper.createAwardsPb(awards),
                                MailConstant.WORLD_WAR_COMMON_REWARD, AwardFrom.WORLD_WAR_DAILY_TASK_AWARD,
                                now, "世界争霸每日任务", "世界争霸每日任务"
                        );
                        // 日志记录(阵营、奖励)
                        LogLordHelper.commonLog("worldWar", AwardFrom.WORLD_WAR_DAILY_TASK_AWARD,
                                e.getValue(), e.getValue().lord.getCamp(), awards, "dailyAttackAward");
                    }
                    // 清除赛季任务 每日任务 积分和领奖信息
                    e.getValue().getPlayerWorldWarData().setDailyAttack(0);
                    e.getValue().getPlayerWorldWarData().getDailyAttackAward().clear();
                });
            }
        }
    }


    /**
     * 获取玩家 每日任务 未领取数量
     *
     * @param player
     * @return
     */
    public int getDailyTips(Player player) {
        int value = player.getPlayerWorldWarData().getDailyAttack();
        Map<Integer, Integer> dailyTaskAward = player.getPlayerWorldWarData().getDailyAttackAward();
        List<StaticWorldWarDailyTask> worldWarCampCityAward =
                StaticCrossWorldDataMgr.getWorldWarDailyTaskList(worldWarSeasonDateService.worldWarType());
        int tips = 0;
        for (StaticWorldWarDailyTask e : worldWarCampCityAward) {
            // 个人值大于条件值，未领取奖励
            if (value >= e.getValue() && !dailyTaskAward.containsKey(e.getId())) {
                tips++;
            }
        }
        return tips;
    }
}
