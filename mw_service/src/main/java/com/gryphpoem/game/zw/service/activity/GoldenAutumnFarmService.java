package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticAnniversaryMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomLibrary;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 喜悦金秋活动 - 帝国农场
 *
 * @author liuyc
 * @date 2021/9/11
 */
@Service
public class GoldenAutumnFarmService extends GoldenAutumnService {

    /**
     * 构建帝国农场数据信息
     * @return
     */
    @Override
    public GeneratedMessage buildGoldenAutumnInfo(Player player, Activity activity) {
        CommonPb.GoldenAutumnFarm.Builder goldenAutumnFarm = CommonPb.GoldenAutumnFarm.newBuilder();
        goldenAutumnFarm.setBookmark(1);
        Prop seedProp = player.props.get(PropConstant.PROP_ID_SEED);
        goldenAutumnFarm.setSeedId(PropConstant.PROP_ID_SEED);
        goldenAutumnFarm.setSeedCount(Objects.isNull(seedProp) ? 0 : seedProp.getCount());
        Prop earRicProp = player.props.get(PropConstant.PROP_ID_EARRICE);
        goldenAutumnFarm.setEarRiceId(PropConstant.PROP_ID_EARRICE);
        goldenAutumnFarm.setEarRiceCount(Objects.isNull(earRicProp) ? 0 : earRicProp.getCount());
        goldenAutumnFarm.setSowingSchedule(activity.getStatusMap().getOrDefault(1, 0));
        goldenAutumnFarm.setSurplusBoxNum(activity.getStatusMap().getOrDefault(0, 0));
        return goldenAutumnFarm.build();
    }

    /**
     * 处理农场播种
     */
    public GamePb4.EmpireFarmRs handlerEmpireFarm(long roleId, GamePb4.EmpireFarmRq rq) throws MwException {
        if (Objects.isNull(rq) || rq.getActType() == 0 || rq.getSeedId() == 0 || (rq.getSowingCount() != 1 && rq.getSowingCount() != 10)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "参数错误"));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, rq.getActType());
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(rq.getActType());
        if (null == activity || !super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "活动未开放", rq.getActType(), activityBase.getStep0()));
        }
        //根据喜悦金秋获种子果实比例换算获得的果实数量
        List<List<Integer>> proportionList = ActParamConstant.ACT_GOLDEN_AUTUMN_SEED_FRUIT_PROPORTION;//喜悦金秋获种子果实比例
        if (null == proportionList || proportionList.size() != 2)
        {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "金秋活动-帝国农场, 喜悦金秋获种子果实比例未配置, roleId:" + roleId);
        }
        int seedProportion = proportionList.get(0).get(1);//种子比例
        int earRiceId = proportionList.get(1).get(0);//稻穗id
        int earRiceProportion = proportionList.get(1).get(1);//稻穗兑换比例
        StaticProp seedStaticProp = StaticPropDataMgr.getPropMap(rq.getSeedId());//获取种子道具基础配置
        StaticProp earRiceStaticProp = StaticPropDataMgr.getPropMap(earRiceId);//获取稻穗道具基础配置
        if (null == seedStaticProp || null == earRiceStaticProp) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "金秋活动-帝国农场,无此配置道具, roleId:" + roleId + ",seedId=" + rq.getSeedId());
        }
        //statusMap中key=1存储播种进度
        int sowingSchedule = activity.getStatusMap().getOrDefault(1, 0);
        //如果领取的宝箱次数达到了上限则可以继续播种，如没有达到上限播种进度又达到阈值的话需要先领取宝箱
        int maxCount = activity.getStatusMap().getOrDefault(0, 0);//statusMap中key=0存储领取的宝箱次数
        if (maxCount < ActParamConstant.ACT_GOLDEN_AUTUMN_AWARD_UPPER_LIMIT)
        {
            //判断播种进度是否到达阈值
            if (sowingSchedule >= ActParamConstant.ACT_GOLDEN_AUTUMN_AWARD_SOWING)
            {
                throw new MwException(GameError.SOWINGS_CHEDULE_REACHED_UPPER_LIMIT.getCode(), "金秋活动-帝国农场, 播种进度已达上限, roleId:" + roleId + ",seedId=" + rq.getSeedId());
            }
        }
        //计算最大可兑换的稻穗数量 如果当前播种次数不能整除种子比例则丢弃精度
        BigDecimal sowingCountBig = new BigDecimal(rq.getSowingCount());//播种次数
        //获取可兑换稻穗个数(播种次数/种子比例)
        BigDecimal convertibleBig = sowingCountBig.divide(new BigDecimal(seedProportion), BigDecimal.ROUND_DOWN);
        //得到最终可兑换的稻穗数量(convertibleBig*稻穗比例)
        int resultEarRice = convertibleBig.multiply(new BigDecimal(earRiceProportion)).intValue();
        //消耗种子
        rewardDataManager.checkAndSubPlayerResHasSync(player, seedStaticProp.getPropType(), rq.getSeedId(), rq.getSowingCount(), AwardFrom.SEED_CONSUMPTION);
        //奖励稻穗
        rewardDataManager.sendRewardSignle(player, earRiceStaticProp.getPropType(), earRiceId, resultEarRice, AwardFrom.EAR_RICE_EXCHANGE);
        //记录帝国农场播种日志埋点
        LogLordHelper.commonLog("GoldenAutumnFarmSowing", AwardFrom.SEED_CONSUMPTION, player, rq.getSowingCount());
        //更新播種進度
        sowingSchedule = sowingSchedule + rq.getSowingCount();
        activity.getStatusMap().put(1, sowingSchedule);
        //组装协议
        GamePb4.EmpireFarmRs.Builder rs = GamePb4.EmpireFarmRs.newBuilder();
        rs.setActType(rq.getActType());
        CommonPb.GoldenAutumnFarm.Builder goldenAutumnFarm = CommonPb.GoldenAutumnFarm.newBuilder();
        goldenAutumnFarm.setBookmark(1);
        goldenAutumnFarm.setSeedId(rq.getSeedId());
        goldenAutumnFarm.setSeedCount(player.props.get(rq.getSeedId()).getCount());
        goldenAutumnFarm.setEarRiceId(earRiceId);
        goldenAutumnFarm.setEarRiceCount(player.props.get(earRiceId).getCount());
        goldenAutumnFarm.setSowingSchedule(sowingSchedule);
        rs.setGoldenAutumnFarm(goldenAutumnFarm);
        return rs.build();
    }

    /**
     * 处理开宝箱
     */
    public GamePb4.EmpireFarmOpenTreasureChestRs handlerOpenTreasureChest(long roleId, GamePb4.EmpireFarmOpenTreasureChestRq rq) throws MwException {
        if (Objects.isNull(rq)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "参数错误"));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, rq.getActType());
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(rq.getActType());
        if (null == activity || !super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "活动未开放", rq.getActType(), activityBase.getStep0()));
        }
        //判斷領取的寶箱是否達到最大上限
        int maxCount = activity.getStatusMap().getOrDefault(0, 0);//statusMap中key=0存储领取的宝箱次数
        if (maxCount >= ActParamConstant.ACT_GOLDEN_AUTUMN_AWARD_UPPER_LIMIT)
        {
            throw new MwException(GameError.TREASURE_CHEST_REACHED_UPPER_LIMIT.getCode(), "金秋活动-帝国农场, 宝箱领取已达上限, roleId:" + roleId);
        }
        //判断播种进度是否达到阈值
        int sowingSchedule = activity.getStatusMap().getOrDefault(1, 0);//statusMap中key=1存储播种进度
        if (sowingSchedule < ActParamConstant.ACT_GOLDEN_AUTUMN_AWARD_SOWING)
        {
            throw new MwException(GameError.SOWING_PROGRESS_NOT_UPPER_LIMIT.getCode(), "金秋活动-帝国农场, 播种进度未达到, 无法领取宝箱, roleId:" + roleId);
        }
        //获取s_random_library的randomid
        List<Integer> randomLibraryList = ActParamConstant.ACT_GOLDEN_AUTUMN_RANDOM_AWARD_LIBRARY.stream()
                .filter(e -> e.get(0) == activityBase.getActivityId()).findFirst().orElse(null);
        if (null == randomLibraryList || randomLibraryList.isEmpty())
        {
            throw new MwException(GameError.NO_CONFIG.getCode(), "金秋活动-帝国农场, 未匹配到帝国农场随机奖励库配置, roleId:" + roleId, ",ActivityId:" + activityBase.getActivityId());
        }
        StaticRandomLibrary staticRandomLibrary = StaticAnniversaryMgr.getRandomLibrary(randomLibraryList.get(1), player.lord.getLevel());
        if (Objects.isNull(staticRandomLibrary)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "未匹配到帝国农场随机奖励库配置, roleId:" + roleId, ",randomId:" + randomLibraryList.get(1));
        }
        //随机获得其中一种奖励并发送
        LinkedList<Integer> linkedList = new LinkedList<>(Objects.requireNonNull(RandomUtil.getWeightByList(staticRandomLibrary.getAwardList(), tmps -> tmps.get(3))));
        linkedList.removeLast();
        List<List<Integer>> getProps = new ArrayList<>();
        getProps.add(new ArrayList<>(linkedList));
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, getProps, AwardFrom.EMPIRE_FARM_OPEN);
        //更新宝箱开启次数
        activity.getStatusMap().put(0, maxCount + 1);
        //更新播种进度
        activity.getStatusMap().put(1, sowingSchedule - ActParamConstant.ACT_GOLDEN_AUTUMN_AWARD_SOWING);
        GamePb4.EmpireFarmOpenTreasureChestRs.Builder rs = GamePb4.EmpireFarmOpenTreasureChestRs.newBuilder();
        rs.setActType(rq.getActType());
        rs.setSurplusBoxNum(activity.getStatusMap().get(0));
        rs.setSowingSchedule(activity.getStatusMap().getOrDefault(1, 0));
        Optional.ofNullable(awardList).ifPresent(tmpList -> tmpList.forEach(rs::addGetAward));
        return rs.build();
    }


    // <editor-fold desc="GM命令" defaultstate="collapsed">
    public void test_handlerEmpireFarm(Player player,int actType, int count) throws MwException {
        GamePb4.EmpireFarmRq.Builder rq = GamePb4.EmpireFarmRq.newBuilder();
        rq.setActType(actType);
        rq.setSeedId(PropConstant.PROP_ID_SEED);
        rq.setSowingCount(count);
        this.handlerEmpireFarm(player.roleId, rq.build());
    }

    public void test_OpenTreasureChest(Player player,int actType) throws MwException {
        GamePb4.EmpireFarmOpenTreasureChestRq.Builder rq = GamePb4.EmpireFarmOpenTreasureChestRq.newBuilder();
        rq.setActType(actType);
        this.handlerOpenTreasureChest(player.roleId, rq.build());
    }

    public void test_GoldenAutumnInfo(Player player,int actType) throws MwException {
        this.buildGoldenAutumnInfo(player, super.checkAndGetActivity(player, actType));
    }

// </editor-fold>

}