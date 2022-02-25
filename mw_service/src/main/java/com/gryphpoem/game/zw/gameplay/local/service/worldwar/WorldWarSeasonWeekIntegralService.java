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
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarWeekTask;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService.WORLD_WAR_WEEK_AWARD;

/**
 * Created by pengshuo on 2019/3/23 17:00
 * <br>Description: 世界争霸-赛季任务-周任务
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonWeekIntegralService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private WorldWarSeasonIntegralRankService seasonIntegralRankService;

    @Autowired
    private WorldWarSeasonDateService worldWarSeasonDateService;

    /**
     * 世界争霸 - 赛季任务 - 周任务奖励领取
     * @param lordId
     * @param rq
     * @return
     */
    public WorldWarAwardRs playerGetWeekTaskAward(long lordId, WorldWarAwardRq rq) throws MwException {
        int keyId = rq.getKeyId();
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Map<Integer, Integer> weekAward = Optional.ofNullable(player.getPlayerWorldWarData().getWeekAward()).orElse(new HashMap<>(2));
        // 已领取
        int hasChange = Optional.ofNullable(weekAward.get(keyId)).orElse(0);
        StaticWorldWarWeekTask award = StaticCrossWorldDataMgr.getWorldWarWeekTask(keyId);
        if (award == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId: ", lordId);
        }
        if(hasChange > 0){
            throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取奖励已达上限, roleId: ", lordId);
        }
        // 领取奖励所需活跃值
        int value = award.getValue();
        // 奖励
        List<List<Integer>> awardList = award.getAward();
        int integral = player.getPlayerWorldWarData().getWeekIntegral();
        if(integral < value){
            throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(),"世界争霸-赛季任务-周奖励-玩家积分不足, roleId: ",
                    lordId, ", need:", value, ", have:", integral);
        }
        if(awardList == null || awardList.isEmpty()){
            throw new MwException(GameError.NO_CONFIG.getCode(), "awardList 找不到配置, roleId: ", lordId);
        }
        // 检查玩家背包
        rewardDataManager.checkBag(player, awardList);
        // 发放奖励
        // 玩家获得装备 "[type,id,cnt]"
        List<CommonPb.Award> awards = rewardDataManager.sendReward(player, awardList, AwardFrom.WORLD_WAR_WEEK_TASK_AWARD, keyId);
        // 保存以获取状态
        weekAward.put(keyId,++hasChange);
        player.getPlayerWorldWarData().setWeekAward(weekAward);
        // 推送红点
        worldWarSeasonDateService.syncAwardChange(player,WORLD_WAR_WEEK_AWARD,getWeekTips(player));
        // 返回结果
        WorldWarAwardRs.Builder builder = WorldWarAwardRs.newBuilder();
        // 对返回结果进行处理
        builder.addAllAward(awards);
        builder.setKeyId(keyId);
        builder.setType(rq.getType());
        return builder.build();
    }

    /**
     * 增加玩家周积分 -> 增加玩家赛季积分
     * @param player
     * @param value
     */
    public void addWorldWarIntegral(Player player, int value) {
        if(worldWarSeasonDateService.isInSeason() && worldWarSeasonDateService.functionIsOpen(player)){
            // 增加玩家 每周赛季积分
            player.getPlayerWorldWarData().setWeekIntegral(
                    player.getPlayerWorldWarData().getWeekIntegral() + value
            );
            // 设置玩家获取积分的时间
            player.getPlayerWorldWarData().setIntegralSecond(TimeHelper.getCurrentSecond());
            LogLordHelper.commonLog("worldWar", AwardFrom.WORLD_WAR_WEEK_INTEGRAL,
                    player, player.lord.getCamp(), value,"weekIntegral");
            // 增加玩家整个赛季的积分
            seasonIntegralRankService.addWorldWarSeasonRankingIntegral(player, value);
            // 满足条件推送红点
            int tips = getWeekTips(player);
            if (tips > 0) {
                worldWarSeasonDateService.syncAwardChange(player, WORLD_WAR_WEEK_AWARD, tips);
            }
        }
    }

    /**
     * 赛季任务 每周结束，发放玩家未领取的周任务奖励,并清空玩家当前周的记录值
     */
    public void weekOverGiveAward(){
        if(worldWarSeasonDateService.isInSeason()){
            LogUtil.common("赛季任务-当前周结束（发放玩家当前周未领取奖励）starting");
            int now = TimeHelper.getCurrentSecond();
            // 赛季任务每周任务奖励
            List<StaticWorldWarWeekTask> weekTasks = StaticCrossWorldDataMgr
                    .getWorldWarWeekTaskList(worldWarSeasonDateService.worldWarType());
            playerDataManager.getPlayers().entrySet().stream().filter(
                    e-> worldWarSeasonDateService.functionIsOpen(e.getValue())).forEach(e->{
                int weekIntegral = e.getValue().getPlayerWorldWarData().getWeekIntegral();
                Map<Integer, Integer> weekAward = e.getValue().getPlayerWorldWarData().getWeekAward();
                List<List<Integer>> awards = new ArrayList<>();
                weekTasks.forEach(w ->{
                    // 未领取并且大于领奖条件发放奖励
                    if(!weekAward.containsKey(w.getId()) && weekIntegral >= w.getValue()){
                        // 汇总至一个邮件发送
                        awards.addAll(w.getAward());
                    }
                });
                if(!awards.isEmpty()){
                    mailDataManager.sendAttachMail(e.getValue(), PbHelper.createAwardsPb(awards),
                            MailConstant.WORLD_WAR_COMMON_REWARD, AwardFrom.WORLD_WAR_WEEK_TASK_AWARD,
                            now,"世界争霸周奖励","世界争霸周奖励"
                    );
                    // 日志记录(阵营、奖励)
                    LogLordHelper.commonLog("worldWar",AwardFrom.WORLD_WAR_WEEK_TASK_AWARD,
                            e.getValue(),e.getValue().lord.getCamp(),awards);
                }
                // 清除赛季任务 当前周 积分和领奖信息
                e.getValue().getPlayerWorldWarData().setWeekIntegral(0);
                e.getValue().getPlayerWorldWarData().getWeekAward().clear();
                // 每周 清除玩家 积分道具
                Prop prop = e.getValue().props.get(PropConstant.WORLD_WAR_INTEGRAL);
                if (null != prop && prop.getCount() > 0) {
                    int count = prop.getCount();
                    // 扣除道具
                    prop.setCount(0);
                    // 记录道具变更
                    LogLordHelper.prop(AwardFrom.WORLD_WAR_WEEK_CLEAR_INTEGRAL,
                            e.getValue().account,e.getValue().lord ,PropConstant.WORLD_WAR_INTEGRAL,
                            prop.getCount(), count, Constant.ACTION_SUB,"week_over_clear_integral");
                }
            });
        }
    }

    /**
     * 获取玩家周积分未领取数量
     * @param player
     * @return
     */
    public int getWeekTips(Player player){
        int value = player.getPlayerWorldWarData().getWeekIntegral();
        Map<Integer, Integer> weekAward = player.getPlayerWorldWarData().getWeekAward();
        List<StaticWorldWarWeekTask> staticWorldWarWeekTasks =
                StaticCrossWorldDataMgr.getWorldWarWeekTaskList(worldWarSeasonDateService.worldWarType());
        int tips = 0;
        for(StaticWorldWarWeekTask e : staticWorldWarWeekTasks){
            // 个人值大于条件值，未领取奖励
            if(value >= e.getValue() && !weekAward.containsKey(e.getId())){
                tips++;
            }
        }
        return tips;
    }

}
