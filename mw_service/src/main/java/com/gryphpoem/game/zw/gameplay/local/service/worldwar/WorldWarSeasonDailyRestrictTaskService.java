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
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarTask;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService.WORLD_WAR_DAILY_TASK_AWARD;

/**
 * Created by pengshuo on 2019/4/1 17:20
 * <br>Description: 世界征战-赛季任务-每日限定任务
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonDailyRestrictTaskService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private WorldWarSeasonDateService worldWarSeasonDateService;

    /**
     * 世界争霸 - 赛季任务 - 每日计时任务奖励领取
     * @param lordId
     * @param rq
     * @return
     */
    public WorldWarAwardRs playerGetDailyTaskAward(long lordId, WorldWarAwardRq rq) throws MwException {
        int keyId = rq.getKeyId();
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Map<Integer, Integer> restrictTaskAward = Optional.ofNullable(player.getPlayerWorldWarData().getRestrictTaskAward()).orElse(new HashMap<>(2));
        // 已领取
        int hasChange = Optional.ofNullable(restrictTaskAward.get(keyId)).orElse(0);
        if(hasChange > 0){
            throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取奖励已达上限, roleId: ", lordId);
        }
        StaticWorldWarTask award = StaticCrossWorldDataMgr.getWorldWarTask(keyId);
        if (award == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId: ", lordId);
        }
        // 检查当前任务是否完成
        checkDailyRestrictTaskIsCompleted(player,award);
        // 奖励
        List<List<Integer>> awardList = award.getAward();
        if(awardList == null || awardList.isEmpty()){
            throw new MwException(GameError.NO_CONFIG.getCode(), "awardList 找不到配置, roleId: ", lordId);
        }
        // 检查玩家背包
        rewardDataManager.checkBag(player, awardList);
        // 发放奖励
        // 玩家获得装备 "[type,id,cnt]"
        List<CommonPb.Award> awards = rewardDataManager.sendReward(player, awardList, AwardFrom.WORLD_WAR_DAILY_TASK_AWARD, keyId);
        // 保存以获取状态
        restrictTaskAward.put(keyId,++hasChange);
        player.getPlayerWorldWarData().setRestrictTaskAward(restrictTaskAward);
        // 推送红点
        worldWarSeasonDateService.syncAwardChange(player,WORLD_WAR_DAILY_TASK_AWARD,getDailyTips(player));
        // 返回结果
        WorldWarAwardRs.Builder builder = WorldWarAwardRs.newBuilder();
        // 对返回结果进行处理
        builder.addAllAward(awards);
        builder.setKeyId(keyId);
        builder.setType(rq.getType());
        return builder.build();
    }

    /**
     * 更新玩家每日计时任务的数值(攻打匪军)
     * @param player
     * @param bandit
     */
    public void updatePlayerDailyRestrictTaskAttackBandit(Player player, StaticBandit bandit){
        updatePlayerDailyRestrictTask(player, TaskType.COND_BANDIT_LV_CNT,1,bandit.getLv());
    }

    /**
     * 更新玩家每日计时任务的数值（非跨服）
     * @param player
     * @param taskType
     * @param value
     */
    public void updatePlayerDailyRestrictTask(Player player,int taskType,int value){
        updatePlayerDailyRestrictTask(player, taskType, value,0);
    }

    /**
     * gm 更新玩家每日计时任务的数值
     * @param player
     * @param keyId
     * @param value
     */
    public void gmUpdatePlayerDailyRestrictTask(Player player,int keyId,int value){
        if(player != null && worldWarSeasonDateService.isInSeason() && worldWarSeasonDateService.functionIsOpen(player)){
            List<StaticWorldWarTask> worldWarTasks = StaticCrossWorldDataMgr
                    .getWorldWarTaskList(worldWarSeasonDateService.worldWarType());
            StaticWorldWarTask staticWorldWarTask = worldWarTasks.stream().filter(w -> w.getId() == keyId)
                    .findFirst().orElse(null);
            if(staticWorldWarTask != null){
                int src = Optional.ofNullable(
                        player.getPlayerWorldWarData().getRestrictTask().get(keyId)
                ).orElse(0);
                player.getPlayerWorldWarData().getRestrictTask().put(keyId,value + src);
                // 每日杀敌数量
                LogLordHelper.commonLog("worldWar",AwardFrom.DO_SOME
                        ,player,player.lord.getCamp(),value,"dailyRestrict",keyId);
                // 异步推送红点
                int tips = getDailyTips(player);
                if(tips > 0){
                    worldWarSeasonDateService.syncAwardChange(player,WORLD_WAR_DAILY_TASK_AWARD,tips);
                }
            }
        }
    }

    /**
     * 更新玩家每日计时任务的数值 带条件 （非跨服）
     * @param player
     * @param taskType
     * @param value
     * @param condId
     */
    public void updatePlayerDailyRestrictTask(Player player,int taskType,int value,int condId){
        if(player != null && worldWarSeasonDateService.isInSeason() && worldWarSeasonDateService.functionIsOpen(player)){
            List<StaticWorldWarTask> worldWarTasks = StaticCrossWorldDataMgr
                    .getWorldWarTaskList(worldWarSeasonDateService.worldWarType());
            StaticWorldWarTask staticWorldWarTask = worldWarTasks.stream().filter(w -> w.getCond() == taskType)
                    .findFirst().orElse(null);
            // 当前阶段有同类型任务，则更新
            if(staticWorldWarTask != null && condId >= Optional.ofNullable(staticWorldWarTask.getCondId()).orElse(0)){
                Integer id = staticWorldWarTask.getId();
                int src = Optional.ofNullable(
                        player.getPlayerWorldWarData().getRestrictTask().get(id)
                ).orElse(0);
                player.getPlayerWorldWarData().getRestrictTask().put(id,value + src);
                // 每日杀敌数量
                LogLordHelper.commonLog("worldWar",AwardFrom.WORLD_WAR_DAILY_TIME_INTEGRAL
                        ,player,player.lord.getCamp(),value,"dailyRestrict",id);
                // 异步推送红点
                int tips = getDailyTips(player);
                if(tips > 0){
                    worldWarSeasonDateService.syncAwardChange(player,WORLD_WAR_DAILY_TASK_AWARD,tips);
                }
            }
        }
    }

    /**
     * 赛季任务 每日结束，发放玩家未领取的每日限定任务奖励,并清空玩家当前每日的记录值
     */
    public void dailyOverGiveAward(){
        if(worldWarSeasonDateService.isInSeason()){
            LogUtil.common("赛季任务-每日限定任务结束（发放玩家当天未领取奖励）starting");
            int now = TimeHelper.getCurrentSecond();
            // 赛季任务每周任务奖励
            List<StaticWorldWarTask> dailyTasks
                    = StaticCrossWorldDataMgr.getWorldWarTaskList(worldWarSeasonDateService.worldWarType());
            if(dailyTasks != null && !dailyTasks.isEmpty()){
                playerDataManager.getPlayers().entrySet().stream().filter(
                        e->worldWarSeasonDateService.functionIsOpen(e.getValue())).forEach(e->{
                    Map<Integer, Integer> restrictTask = e.getValue().getPlayerWorldWarData().getRestrictTask();
                    Map<Integer, Integer> restrictTaskAward = e.getValue().getPlayerWorldWarData().getRestrictTaskAward();
                    List<List<Integer>> awards = new ArrayList<>();
                    dailyTasks.forEach(w ->{
                        Integer id = w.getId();
                        // 未领奖，且完成
                        if(!restrictTaskAward.containsKey(id) && restrictTask.containsKey(id)
                                && restrictTask.get(id) >= w.getSchedule()){
                            // 汇总至一个邮件发送
                            awards.addAll(w.getAward());
                        }
                    });
                    if(!awards.isEmpty()){
                        mailDataManager.sendAttachMail(e.getValue(), PbHelper.createAwardsPb(awards),
                                MailConstant.WORLD_WAR_COMMON_REWARD, AwardFrom.WORLD_WAR_DAILY_TIME_AWARD
                                ,now,"世界争霸计时任务","世界争霸计时任务"
                        );
                        // 日志记录(阵营、奖励)
                        LogLordHelper.commonLog("worldWar",AwardFrom.WORLD_WAR_DAILY_TIME_AWARD,
                                e.getValue(),e.getValue().lord.getCamp(),awards,"dailyRestrictAward");
                    }
                    // 清除赛季任务 每日任务 积分和领奖信息
                    e.getValue().getPlayerWorldWarData().getRestrictTask().clear();
                    e.getValue().getPlayerWorldWarData().getRestrictTaskAward().clear();
                });
            }
        }
    }

    /**
     * 获取玩家 每日任务 未领取数量
     * @param player
     * @return
     */
    public int getDailyTips(Player player){
        Map<Integer, Integer> restrictTask = player.getPlayerWorldWarData().getRestrictTask();
        Map<Integer, Integer> restrictTaskAward = player.getPlayerWorldWarData().getRestrictTaskAward();
        List<StaticWorldWarTask> worldWarTasks = StaticCrossWorldDataMgr
                .getWorldWarTaskList(worldWarSeasonDateService.worldWarType());
        int tips = 0;
        for (StaticWorldWarTask e : worldWarTasks) {
            Integer id = e.getId();
            // 未领奖，且完成
            if(!restrictTaskAward.containsKey(id) && restrictTask.containsKey(id)
                    && restrictTask.get(id) >= e.getSchedule()){
                tips++;
            }
        }
        return tips;
    }

    /**
     * 检查当前每日计时任务是否完成
     * @param player
     * @param task
     * @throws MwException
     */
    private void checkDailyRestrictTaskIsCompleted(Player player,StaticWorldWarTask task) throws MwException{
        // 判断当前世界争霸有无该任务配置
        List<StaticWorldWarTask> worldWarTasks = StaticCrossWorldDataMgr
                .getWorldWarTaskList(worldWarSeasonDateService.worldWarType());
        int id = task.getId();
        StaticWorldWarTask staticWorldWarTask = worldWarTasks.stream().filter(w -> w.getId() == id)
                .findFirst().orElse(null);
        if(staticWorldWarTask == null){
            throw new MwException(GameError.NO_CONFIG.getCode(), "世界争霸 每日计时任务 找不到配置, roleId: ", player.lord.getLordId());
        }
        Map<Integer, Integer> restrictTask = player.getPlayerWorldWarData().getRestrictTask();
        if(!restrictTask.containsKey(id) || restrictTask.get(id) < task.getSchedule()){
            throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(),"世界争霸-赛季任务-每日计时任务-玩家未完成, roleId: ",
                    player.lord.getLordId(), ", need:", task.getSchedule(), ", have:",restrictTask.get(id),"id:",id);
        }
    }
}
