package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActVoucher;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 抽奖活动
 *
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-09-10 15:27
 */
@Service
public class ActivityLotteryService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;
    
    @Autowired
    private ChatDataManager chatDataManager;


    /**
     * 好运道活动抽奖
     *
     * @param roleId 玩家id
     * @param req    好运道活动
     * @return 响应协议
     * @throws MwException 自定义异常
     */
    public GamePb4.ActGoodLuckAwardRs actGoodLuckAward(long roleId, GamePb4.ActGoodLuckAwardRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_GOOD_LUCK);
        if (activityBase == null || activityBase.getPlan() == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_GOOD_LUCK);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", ActivityConst.ACT_GOOD_LUCK);
        }

        // 活动id
        int activityId = activity.getActivityId();

        List<StaticActAward> actAwards = StaticActivityDataMgr.getActAwardById(activityId);
        if (CheckNull.isEmpty(actAwards)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId, ", actId:", activityId);
        }

        // 对应s_act_award里的cond
        int awardType = req.getAwardType();
        // 次数
        int count = req.getCount();
        // 消耗的类型：0 道具 1 钻石
        // int costType = req.getCostType();

        // 奖励类型
        int randomType = activityDataManager.currentActivity(player, activity, 0);

        // 根据奖励类型过滤本次奖池
        StaticActAward staticActAward = actAwards.stream().filter(saa -> saa.getTaskType() == randomType && saa.getCond() == awardType).sorted(Comparator.comparingInt(StaticActAward::getKeyId)).findAny().orElse(null);
        if (Objects.isNull(staticActAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId, ", actId:", activityId);
        }

        // 代币转换
        StaticActVoucher sActVoucher = StaticActivityDataMgr.getActVoucherByActId(activityId);
        if (Objects.isNull(sActVoucher)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId, ", actId:", activityId);
        }

        // 道具消耗
        List<Integer> consume = sActVoucher.getConsume();
        if (CheckNull.isEmpty(consume)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId, ", actId:", activityId);
        }

        // 需要消耗的道具次数
        int propCost;
        // 需要消耗的钻石次数
        int goldCost = 0;
        // 拥有的道具数量
        long haveProp = rewardDataManager.getRoleResByType(player, consume.get(0), consume.get(1));
        // 需要消耗的道具
        int needProp = consume.get(2) * count;
        if (haveProp > needProp) {
            propCost = count;
        } else {
            // 道具不够
            propCost = (int) haveProp / consume.get(2);
            // 需要消耗的钻石次数
            goldCost = count - propCost;
            // 检查钻石够不够
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, goldCost * sActVoucher.getPrice());
        }
        // 资源和道具的消耗
        if (propCost > 0) {
            rewardDataManager.checkAndSubPlayerRes(player, Collections.singletonList(sActVoucher.getConsume()), propCost, AwardFrom.ACT_GOOD_LUCK_CONSUME, activityId, randomType, awardType);
        }
        if (goldCost > 0) {
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, sActVoucher.getPrice() * goldCost, AwardFrom.ACT_GOOD_LUCK_CONSUME, true, activityId, randomType, awardType);
        }

        GamePb4.ActGoodLuckAwardRs.Builder builder = GamePb4.ActGoodLuckAwardRs.newBuilder();
        List<List<Integer>> awardList = staticActAward.getAwardList();
        if (!CheckNull.isEmpty(awardList)) {
            List<List<Integer>> randomAward = Stream.iterate(0, i -> ++i)
                    .limit(count)
                    .map((cnt) -> RandomUtil.getRandomByWeight(awardList, 3, false))
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(randomAward)) {
                //检测奖励并发跑马灯
                checkAwardChat(player,randomAward,activity);
                // 随机下一次的奖池
                randomGoodLuckType(player, activity);
                // 发送奖励
                // randomAward = RewardDataManager.mergeAward(randomAward);
                List<CommonPb.Award> awards = rewardDataManager.sendReward(player, randomAward, AwardFrom.ACT_GOOD_LUCK_AWARD);;
                if (!CheckNull.isEmpty(awards)) {
                    builder.addAllAward(awards);
                }
            }

        }
        return builder.build();
    }

    /**
     * 获取好运道活动的信息
     *
     * @param player   玩家对象
     * @param activity 活动对象
     * @param builder  响应数据的构造器
     */
    void getGoodLuckActivity(Player player, Activity activity, GamePb3.GetActivityRs.Builder builder) {
        // award配置
        int activityId = activity.getActivityId();
        List<StaticActAward> actAwards = StaticActivityDataMgr.getActAwardById(activityId);
        // randomType
        int randomType = activityDataManager.currentActivity(player, activity, 0);
        Optional.ofNullable(StaticActivityDataMgr.getActVoucherByActId(activityId))
                .ifPresent(sActVoucher ->
                        actAwards.stream()
                                .filter(saa -> saa.getTaskType() == randomType)
                                .forEach(saa -> builder.addActivityCond(PbHelper.createActGoodLuck(saa, sActVoucher, 0, randomType))));
    }

    /**
     * 随机好运道活动的奖池, 该方法会直接修改活动的奖池类型
     *
     * @param player   玩家对象
     * @param activity 活动对象
     * @return 奖池类型
     */
    public int randomGoodLuckType(Player player, Activity activity) {
        int randomType = 0;
        // award配置
        List<StaticActAward> actAwards = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        // 活动配置
        List<Integer> randomTypeList = actAwards.stream().map(StaticActAward::getTaskType).distinct().collect(Collectors.toList());
        if (!CheckNull.isEmpty(randomTypeList)) {
            List<Integer> listRandom = RandomUtil.getListRandom(randomTypeList, 1);
            if (!CheckNull.isEmpty(listRandom)) {
                randomType = listRandom.get(0);
            }
        }
        if (randomType > 0) {
            activity.getStatusCnt().put(0, (long) randomType);
        }
        return randomType;
    }

    /**
     * 活动结束前,自动将特殊道具碎片,兑换成指定奖励
     */
    void autoExchangeByGoodLuck() {
        int actType = ActivityConst.ACT_GOOD_LUCK;
        playerDataManager.getPlayers().values().stream()
                .filter(p -> Objects.nonNull(p.activitys.get(actType)))
                .forEach(player -> {
                    Activity activity = player.activitys.get(actType);
                    int activityId = activity.getActivityId();
                    Optional.ofNullable(StaticActivityDataMgr.getActVoucherByActId(activityId))
                            .ifPresent(aVoucher -> {
                                // 抽奖道具
                                List<Integer> cost = aVoucher.getConsume();
                                if (!CheckNull.isEmpty(cost)) {
                                    int chipCnt = Math.toIntExact(rewardDataManager.getRoleResByType(player, cost.get(0), cost.get(1)));
                                    if (chipCnt > 0) {
                                        try {
                                            rewardDataManager.checkAndSubPlayerRes(player, cost.get(0), cost.get(1), chipCnt, AwardFrom.ACT_GOOD_LUCK_EXPIRED_EXCHANGE_SONSUME, true, actType, activityId);
                                        } catch (MwException e) {
                                            LogUtil.error("好运道活动结束时，道具兑换异常", e);
                                            return;
                                        }
                                        // 兑换次数
                                        List<Integer> award = aVoucher.getAwardList();
                                        if (!CheckNull.isEmpty(award)) {
                                            List<CommonPb.Award> awards = Collections.singletonList(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2) * chipCnt));
                                            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD, AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond(), actType, activityId, actType, activityId);
                                        }
                                    }
                                }
                            });
                });
    }

    /**
     * 检查校验获得的奖励是否需要发送跑马灯
     * @param awardList
     * @return
     */
    public void checkAwardChat(Player player,List<List<Integer>> awardList,Activity activity) {
        for(List<Integer> award:awardList) {
            for (List<Integer> sendChat : ActParamConstant.ACT_GOLD_LUCK_SEND_CHAT_AWARD) {
                if (sendChat.get(0).equals(award.get(0)) && sendChat.get(1).equals(award.get(1))
                        && sendChat.get(2).equals(award.get(2))) {
                    // 发送跑马灯
                    chatDataManager.sendSysChat(ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM, player.lord.getCamp(), 0,
                            player.lord.getCamp(), player.lord.getNick(), award.get(0), award.get(1),
                            award.get(2),activity.getActivityId());
                    //活动消息推送
                    chatDataManager.sendActivityChat(ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM, activity.getActivityType(), 0,
                            player.lord.getCamp(), player.lord.getNick(), award.get(0), award.get(1),
                            award.get(2),activity.getActivityId());
                }
            }
        }
    }

}
