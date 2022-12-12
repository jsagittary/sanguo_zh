package com.gryphpoem.game.zw.service.simulator;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacterReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.pojo.simulator.CityEvent;
import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 人生模拟器器
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/1 9:28
 */
@Service
public class LifeSimulatorService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;

    /**
     * 记录玩家人生模拟器选择
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.RecordLifeSimulatorRs RecordSimulator(long roleId, GamePb1.RecordLifeSimulatorRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int triggerMode = rq.getTriggerMode();
        int type = rq.getType();
        int bindType = rq.getBindType();
        int bindId = rq.getBindId();
        List<CommonPb.LifeSimulatorStep> simulatorStepList = rq.getLifeSimulatorStepList();
        // 标识模拟器是否结束
        boolean isEnd = false;
        List<List<Integer>> finalRewardList = new ArrayList<>();
        List<List<Integer>> finalCharacterFixList = new ArrayList<>();
        List<Integer> delay = null;
        for (CommonPb.LifeSimulatorStep lifeSimulatorStep : simulatorStepList) {
            int chooseId = lifeSimulatorStep.getChooseId();
            if (chooseId > 0) {
                StaticSimulatorChoose sSimulatorChoose = StaticBuildCityDataMgr.getStaticSimulatorChoose(chooseId);
                // 性格值变化
                List<List<Integer>> characterFix = sSimulatorChoose.getCharacterFix();
                finalCharacterFixList.addAll(characterFix);
                // 奖励变化
                List<List<Integer>> rewardList = sSimulatorChoose.getRewardList();
                finalRewardList.addAll(rewardList);
                // TODO buff增益
                List<List<Integer>> buff = sSimulatorChoose.getBuff();
            }
            long stepId = lifeSimulatorStep.getStepId();
            StaticSimulatorStep staticSimulatorStep = StaticBuildCityDataMgr.getStaticSimulatorStepById(stepId);
            if (staticSimulatorStep == null) {
                throw new MwException(GameError.NO_CONFIG, String.format("记录模拟器结果时, 未获取到模拟器的步骤配置, roleId:%s, stepId:%s", roleId, stepId));
            }
            // 根据配置, 如果没有下一步, 则模拟器结束
            long nextId = staticSimulatorStep.getNextId();
            List<List<Long>> staticChooseList = staticSimulatorStep.getChoose();
            List<Long> playerChoose = new ArrayList<>();
            if (CheckNull.nonEmpty(staticChooseList)) {
                playerChoose = staticChooseList.stream().filter(tmp -> tmp.size() == 3 && tmp.get(0) == (long) chooseId && tmp.get(1) != 0L).findFirst().orElse(null);
            }
            if (CheckNull.nonEmpty(playerChoose)) {
                nextId = playerChoose.get(1);
            }
            if (!isEnd) {
                isEnd = nextId == 0L;
            }
            // 如果该步有延时执行, 新增模拟器器延时任务
            delay = staticSimulatorStep.getDelay();
            if (CheckNull.nonEmpty(delay)) {
                LifeSimulatorInfo delaySimulator = new LifeSimulatorInfo();
                delaySimulator.setType(delay.get(1));// 延时后执行哪一个模拟器
                delaySimulator.setPauseTime(TimeHelper.getCurrentDay());
                delaySimulator.setDelay(delay.get(0));// 延时时间
                // DELAY_QUEUE.add(new LifeSimulatorDelayRun(delaySimulator, player));
                List<LifeSimulatorInfo> lifeSimulatorInfos = player.getLifeSimulatorRecordMap().computeIfAbsent(4, k -> new ArrayList<>());
                lifeSimulatorInfos.add(delaySimulator);
            }
        }

        if (!isEnd) {
            throw new MwException(GameError.SIMULATOR_IS_NOT_END, String.format("记录模拟器结果时, 模拟器未结束, roleId:%s, triggerMode:%s", roleId, type));
        }

        // 玩的是城镇事件的模拟器, 从城镇事件池子中移除
        if (triggerMode == 3) {
            if (CheckNull.isNull(player.getCityEvent())) {
                CityEvent cityEvent = new CityEvent();
                player.setCityEvent(cityEvent);
            }
            player.getCityEvent().getLifeSimulatorInfoList().removeIf(temp -> temp.getType() == type && temp.getBindType() == bindType && temp.getBindId() == bindId);
        }
        // 更新性格值并发送对应奖励
        if (CheckNull.nonEmpty(finalCharacterFixList)) {
            if (CheckNull.isEmpty(player.getCharacterData())) {
                player.setCharacterData(new HashMap<>(6));
            }
            if (CheckNull.isEmpty(player.getCharacterRewardRecord())) {
                player.setCharacterRewardRecord(new HashMap<>(8));
            }
            for (List<Integer> characterChange : finalCharacterFixList) {
                int index = characterChange.get(0);
                int value = characterChange.get(1);
                int addOrSub = characterChange.get(0);
                updateCharacterData(player.getCharacterData(), index, value, addOrSub);
            }
            checkAndSendCharacterReward(player);
            // 同步领主性格变化
            playerDataManager.syncRoleInfo(player);
        }
        // 更新对应奖励变化
        if (CheckNull.nonEmpty(finalRewardList)) {
            for (List<Integer> reward : finalRewardList) {
                int awardType = reward.get(0);
                int awardId = reward.get(1);
                int awardCount = reward.get(2);
                int addOrSub = reward.get(3);
                switch (addOrSub) {
                    case 1:
                        rewardDataManager.sendRewardSignle(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, "");
                        break;
                    case 0:
                        // 如果资源不足则扣减至0
                        rewardDataManager.subPlayerResCanSubCount(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, "");
                        break;
                }
            }
        }
        // TODO 处理buff增益

        // TODO 日志埋点

        GamePb1.RecordLifeSimulatorRs.Builder builder = GamePb1.RecordLifeSimulatorRs.newBuilder();
        return builder.build();
    }

    /**
     * 更新玩家性格值
     *
     * @param index
     * @param value
     * @param addOrSub
     */
    public void updateCharacterData(Map<Integer, Integer> characterData, int index, int value, int addOrSub) {
        int oldValue = characterData.get(index);
        List<Integer> characterRange = StaticBuildCityDataMgr.getCharacterRange(index);
        int minCharacterValue = 0;
        int maxCharacterValue = 0;
        if (characterRange == null || characterRange.size() < 2) {
            throw new MwException(GameError.NO_CONFIG, "性格配置错误");
        } else {
            minCharacterValue = characterRange.get(0);
            maxCharacterValue = characterRange.get(1);
        }
        switch (addOrSub) {
            case 1:
                characterData.put(index, Math.min(oldValue + value, maxCharacterValue));
                break;
            case 2:
                characterData.put(index, Math.max(oldValue - value, minCharacterValue));
                break;
        }
    }

    /**
     * 检测并发放性格奖励
     *
     * @param player
     */
    public void checkAndSendCharacterReward(Player player) {
        // 性格值
        Map<Integer, Integer> characterData = player.getCharacterData();
        // 性格对应奖励领取记录
        Map<Integer, Integer> characterRewardRecord = player.getCharacterRewardRecord();
        List<StaticCharacterReward> staticCharacterRewardList = StaticBuildCityDataMgr.getStaticCharacterRewardList();
        for (StaticCharacterReward staticCharacterReward : staticCharacterRewardList) {
            int characterRewardId = staticCharacterReward.getId();
            // 已获取奖励不再重复获取
            if (characterRewardRecord.get(characterRewardId) == 1) {
                continue;
            }
            boolean checkRewardCondition = true;
            for (List<Integer> need : staticCharacterReward.getNeed()) {
                int index = need.get(0);
                int needValue = need.get(1);
                if (characterData.get(index) < needValue) {
                    checkRewardCondition = false;
                    break;
                }
            }
            if (!checkRewardCondition) {
                // 当前奖励条件不满足
                continue;
            }
            List<Integer> reward = staticCharacterReward.getReward();
            characterRewardRecord.put(characterRewardId, 1);
            rewardDataManager.sendRewardSignle(player, reward.get(0), reward.get(1), reward.get(2), AwardFrom.CHARACTER_REWARD, "");
        }
    }

    /**
     * 分配城镇事件给玩家(转点执行)
     *
     * @param player
     */
    public void assignCityEventToPlayerJob(Player player) {
        // 校验领主是否达到城镇事件开启条件
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.CITY_EVENT)) {
            return;
        }

        if (CheckNull.isNull(player.getCityEvent())) {
            CityEvent cityEvent = new CityEvent();
            player.setCityEvent(cityEvent);
        }
        CityEvent cityEvent = player.getCityEvent();
        List<LifeSimulatorInfo> lifeSimulatorInfoList = new ArrayList<>(cityEvent.getLifeSimulatorInfoList());
        List<Integer> cityEventRefreshConfig = Constant.CITY_EVENT_REFRESH_CONFIG;
        // 获取当前周期开启时间, 当前周期结束时间, 周期次数
        int latestEndTime = cityEvent.getEndTime();
        int totalCountCurPeriod = cityEvent.getTotalCountCurPeriod();
        int curPeriodCount = cityEvent.getPeriodCount();

        int currentSecond = TimeHelper.getCurrentSecond();
        // 开启新一轮, 重新设置周期开始时间、结束时间, 更新周期次数
        if (currentSecond > latestEndTime) {
            int newStartTime = latestEndTime + 1;
            int newEndTime = DateHelper.afterDayTime(TimeHelper.getDate((long) newStartTime), cityEventRefreshConfig.get(0));
            totalCountCurPeriod = lifeSimulatorInfoList.size(); // 若进入新一轮, 则遗留的未完成的城镇事件累计到当前周期
            cityEvent.setStartTime(newStartTime);
            cityEvent.setEndTime(newEndTime);
            cityEvent.setPeriodCount(curPeriodCount + 1);
        }

        // 已达配置上限
        if (totalCountCurPeriod >= cityEventRefreshConfig.get(1)) {
            return;
        }

        // 随机城镇事件
        List<StaticSimCity> staticSimCityList = StaticBuildCityDataMgr.getCanRandomSimCityList(player);
        if (CheckNull.nonEmpty(staticSimCityList)) {
            int random = RandomHelper.randomInSize(staticSimCityList.size());
            if (random > 1) {
                StaticSimCity staticSimCity = staticSimCityList.get(random - 1);
                // 初始化模拟器信息
                LifeSimulatorInfo lifeSimulatorInfo = new LifeSimulatorInfo(staticSimCity.getType(), staticSimCity.getOpen().get(0), staticSimCity.getOpen().get(1));
                lifeSimulatorInfo.setAddDate(currentSecond);
                lifeSimulatorInfo.setPauseTime(0);
                lifeSimulatorInfo.setDelay(0);
                lifeSimulatorInfoList.add(lifeSimulatorInfo);
                cityEvent.setLifeSimulatorInfoList(lifeSimulatorInfoList);
                LogUtil.common(String.format("转点刷新玩家城镇事件成功, roleId:%s, simulatorType:%s", player.roleId, staticSimCity.getType()));
                cityEvent.setTotalCountCurPeriod(totalCountCurPeriod + 1);
                // 向客户端同步新增的模拟器
                SyncNewSimulatorToPlayer(player, lifeSimulatorInfo, 3);
                LogUtil.common(String.format("向客户端同步新增可玩的模拟器成功, roleId:%s, simulatorType:%s", player.roleId, lifeSimulatorInfo.getType()));
            }
        }
    }

    /*public void checkAndRefresh(Player player) {
        List<LifeSimulatorInfo> lifeSimulatorInfoList = player.getCityEvent().getLifeSimulatorInfoList();
        int currentSecond = TimeHelper.getCurrentSecond();
        for (LifeSimulatorInfo lifeSimulatorInfo : lifeSimulatorInfoList) {
            int pauseTime = lifeSimulatorInfo.getPauseTime();
            int delay = lifeSimulatorInfo.getDelay();
            if (currentSecond >= pauseTime + delay * 24 * 60 * 60) {
                lifeSimulatorInfo.setDelay(0);
            }
        }
    }*/

    /**
     * 向客户端同步新增可玩的模拟器
     *
     * @param player
     */
    public void SyncNewSimulatorToPlayer(Player player, LifeSimulatorInfo lifeSimulatorInfo, int triggerMode) {
        GamePb1.SyncSimulatorDelayStateRefreshRs.Builder builder = GamePb1.SyncSimulatorDelayStateRefreshRs.newBuilder();
        CommonPb.LifeSimulatorRecord.Builder lifeSimulatorRecordBuilder = CommonPb.LifeSimulatorRecord.newBuilder();
        lifeSimulatorRecordBuilder.setTriggerMode(triggerMode);
        lifeSimulatorInfo.setDelay(0);
        CommonPb.LifeSimulatorInfo lifeSimulatorInfoPb = lifeSimulatorInfo.ser();
        lifeSimulatorRecordBuilder.addLifeSimulatorInfo(lifeSimulatorInfoPb);
        builder.addLifeSimulatorRecord(lifeSimulatorRecordBuilder);
        // BasePb.Base msg = PbHelper.createSynBase(GamePb1.SyncSimulatorDelayStateRefreshRs.EXT_FIELD_NUMBER, GamePb1.SyncSimulatorDelayStateRefreshRs.ext, builder.build()).build();
        // playerService.syncMsgToPlayer(msg, player);
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SyncSimulatorDelayStateRefreshRs.EXT_FIELD_NUMBER, GamePb1.SyncSimulatorDelayStateRefreshRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 延时模拟器处理定时器逻辑
     */
    public void refreshDelayLifeSimulator() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                List<LifeSimulatorInfo> delayLifeSimulatorList = player.getLifeSimulatorRecordMap().get(4);
                if (CheckNull.nonEmpty(delayLifeSimulatorList)) {
                    for (LifeSimulatorInfo lifeSimulatorInfo : delayLifeSimulatorList) {
                        Date delayDate = TimeHelper.getSomeDayAfterOrBerfore(
                                TimeHelper.getDate(TimeHelper.getDay(lifeSimulatorInfo.getPauseTime())),
                                lifeSimulatorInfo.getDelay(),
                                8,
                                0,
                                0
                        );
                        if (TimeHelper.dateToSecond(delayDate) < now) {
                            SyncNewSimulatorToPlayer(player, lifeSimulatorInfo, 4);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error("延时模拟器处理定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }


    // private DelayQueue<LifeSimulatorDelayRun> DELAY_QUEUE = new DelayQueue<>(this);
    //
    // @Override
    // public DelayQueue getDelayQueue() {
    //     return DELAY_QUEUE;
    // }
}
