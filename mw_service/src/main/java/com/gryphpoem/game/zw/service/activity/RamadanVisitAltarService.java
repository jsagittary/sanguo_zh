package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.visitaltar.VisitAltarRefreshJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticAltarArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.world.Altar;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.util.*;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 斋月-拜访圣坛
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/7/21 15:27
 */
@Service
public class RamadanVisitAltarService extends AbsActivityService {

    private int[] actTypes = {ActivityConst.ACT_VISIT_ALTAR};

    /**
     * 拜访类型, 金币拜访
     */
    public static final int VISIT_ALTAR_TYPE_OIL = 1;
    /**
     * 拜访类型, 钻石拜访
     */
    public static final int VISIT_ALTAR_TYPE_GOLD = 2;
    /**
     * 活动拜访次数
     */
    private static final int VISIT_CNT_KEY = 0;
    /**
     * 拜访圣坛刷新定时器, name
     */
    private static final String VISIT_ALTAR_REFRESH_JOB_NAME = "VISIT_ALTAR_REFRESH_JOB_NAME";
    /**
     * 当前功能用到的半个区域
     */
    private static final List<Integer> halfArea = Arrays.asList(MapHelper.UP_HALF_IN_AREA, MapHelper.DOWN_HALF_IN_AREA);

    @Autowired
    private WorldDataManager worldDataManager;

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
     * 获取圣坛信息
     *
     * @param pos    坐标
     * @param roleId 玩家id
     * @throws MwException 自定义异常
     */
    public GamePb4.GetAltarRs getAltar(int pos, long roleId) throws MwException {

        playerDataManager.checkPlayerIsExist(roleId);

        Altar altar = worldDataManager.getAltarMap().get(pos);
        if (Objects.isNull(altar)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "该坐标为空闲坐标，不能获取圣坛详情, roleId:", roleId, ", pos:", pos);
        }

        return GamePb4.GetAltarRs.newBuilder().setAltar(altar.ser()).build();
    }

    /**
     * 拜访圣坛
     *
     * @param pos          圣坛坐标
     * @param roleId       玩家id
     * @param type         拜访类型, 1 黄金拜访, 2 钻石拜访
     * @param marchConsume 行军消耗, 可用于异常行军的消耗返回
     * @throws MwException 自定义异常
     */
    public void visitAltar(int pos, long roleId, int type, List<CommonPb.Award> marchConsume) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int tarArea = MapHelper.getAreaIdByPos(pos);
        if (tarArea != player.lord.getArea()) {
            throw new MwException(GameError.ACT_VISIT_AREA_ERROR.getCode(), String.format("禁止跨区拜访, roleId: %s, my area: %s, target area: %s", roleId, player.lord.getArea(), tarArea));
        }
        // 检测圣坛拜访
        checkAltarVisit(roleId, pos);
        int curVisitCnt = curVisitCnt(player);

        // 购买次数消耗
        int buyCntConsume = 0;
        // 拜访消耗
        int visitConsume = 0;

        if (curVisitCnt > ActParamConstant.ACT_VISIT_ALTAR_FREE_CNT) {
            // 第几次购买拜访次数
            int goldCnt = curVisitCnt - ActParamConstant.ACT_VISIT_ALTAR_FREE_CNT;
            // 购买消耗
            List<Integer> consume = ActParamConstant.ACT_VISIT_ALTAR_GOLD_CONSUME.stream().filter(list -> list.get(0) <= goldCnt && list.get(1) >= goldCnt).findFirst().orElse(null);
            if (CheckNull.isEmpty(consume)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "没有找到购买拜访次数的配置, roleId: ", roleId, ", pos: ", pos, ", type: ", type);
            }
            // 消耗钻石
            int consumeGold = consume.stream().skip(2).findFirst().orElse(0);
            if (consumeGold > 0) {
                buyCntConsume = consumeGold;
            }
        }
        if (type == VISIT_ALTAR_TYPE_OIL) {
            // 资源拜访
            if (!ActParamConstant.ACT_VISIT_OIL_CONSUME.isEmpty()) {
                rewardDataManager.checkAndSubPlayerRes(player, ActParamConstant.ACT_VISIT_OIL_CONSUME, AwardFrom.ACT_VISIT_ALTAR_CONSUME, true);
                marchConsume.addAll(PbHelper.createAwardsPb(ActParamConstant.ACT_VISIT_OIL_CONSUME));
            }
        } else if (type == VISIT_ALTAR_TYPE_GOLD) {
            // 钻石拜访
            if (!ActParamConstant.ACT_VISIT_GOLD_CONSUME.isEmpty()) {
                visitConsume = ActParamConstant.ACT_VISIT_GOLD_CONSUME.get(0).get(2);
            }
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "拜访圣坛类型错误, roleId: ", roleId, ", pos: ", pos, ", type: ", type);
        }
        // 检测消耗是否足够
        if (buyCntConsume + visitConsume > 0) {
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, buyCntConsume + visitConsume);
            if (buyCntConsume > 0) {
                marchConsume.add(PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, buyCntConsume));
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, buyCntConsume, AwardFrom.ACT_VISIT_ALTAR_BUY_CNT, true);
            }
            if (visitConsume > 0) {
                marchConsume.add(PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, visitConsume));
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, visitConsume, AwardFrom.ACT_VISIT_ALTAR_CONSUME, true);
            }
        }
        // 更新玩家的拜访次数
        activityDataManager.updActivity(player, ActivityConst.ACT_VISIT_ALTAR, 1, VISIT_CNT_KEY, true);
    }

    /**
     * 检测圣坛是否可以拜访
     *
     * @param roleId 玩家id
     * @param pos    坐标
     * @throws MwException 自定义异常
     */
    private void checkAltarVisit(long roleId, int pos) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 圣坛
        Altar altar = worldDataManager.getAltarMap().get(pos);
        if (Objects.isNull(altar)) {
            throw new MwException(GameError.ACT_ALTAR_LOST.getCode(), "该坐标为空闲坐标，不能拜访圣坛, roleId:", roleId, ", pos:", pos);
        }
        int curVisitCnt = curVisitCnt(player);
        if (curVisitCnt > ActParamConstant.ACT_VISIT_ALTAR_FREE_CNT + ActParamConstant.ACT_VISIT_ALTAR_GOLD_CNT) {
            // 超出可拜访次数
            throw new MwException(GameError.ACT_VISIT_MAX_COUNT.getCode(), "今天拜访的圣坛已经达到次数上限了, roleId: ", roleId, ", pos: ", pos);
        }
    }

    /**
     * 获取当前拜访次数
     *
     * @param player 玩家对象
     * @return 当前是第几次拜访
     * @throws MwException 自定义异常
     */
    private int curVisitCnt(Player player) throws MwException {
        // 拜访圣坛活动
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_VISIT_ALTAR);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "拜访圣坛活动未开启 roleId:", player.roleId);
        }
        // 当天的拜访次数
        int visitCnt = activityDataManager.currentActivity(player, activity, VISIT_CNT_KEY);
        // 当前拜访是第几次
        return visitCnt + 1;
    }

    /**
     * 获取地图上的圣坛
     *
     * @param block   地图块10 * 10
     * @param builder GetMap构造器
     */
    public void getMap(int block, GamePb2.GetMapRs.Builder builder) {
        Map<Integer, Altar> altarMap = worldDataManager.getAltarMap();
        altarMap.values().stream()
                .filter(altar -> MapHelper.block(altar.getPos()) == block)
                .forEach(altar -> {
                    CommonPb.MapForce.Builder forceBuilder = CommonPb.MapForce.newBuilder();
                    forceBuilder.setPos(altar.getPos());
                    forceBuilder.setType(WorldConstant.FORCE_TYPE_ALTAR);
                    forceBuilder.setParam(1);
                    builder.addForce(forceBuilder.build());
                });
    }

    /**
     * 刷新并删除无效的圣坛
     */
    public void refreshAndRemoveLogic(JobExecutionContext context) {
        LogUtil.world("---------------------拜访圣坛刷新定时器---------------------");
        String jobKeyName = context.getJobDetail().getKey().getName();
        //  活动期间的判断
        ActivityBase activityBase = ActivityUtil.getActivityBase(jobKeyName);
        if (activityBase == null) {
            return;
        }
        if (activityBase.getStep() != ActivityConst.OPEN_STEP) {
            LogUtil.error(String.format("刷新并删除无效的圣坛, 不在活动开启的时间内, activityType: %s", ActivityConst.ACT_VISIT_ALTAR));
            return;
        }
        // 圣坛的刷新配置
        Map<Integer, StaticAltarArea> altarAreaMap = StaticWorldDataMgr.getAltarAreaMap();
        if (CheckNull.isEmpty(altarAreaMap)) {
            return;
        }
        // 所有开放的区域
        List<Area> openArea = worldDataManager.getOpenArea();
        if (CheckNull.isEmpty(openArea)) {
            return;
        }
        // 所有圣坛
        Map<Integer, Altar> altarMap = rmAndGetAltar();
        // 每个区域，上下部分的圣坛
        HashMap<Integer, Map<Integer, List<Altar>>> areaGroupAltar = altarMap.values().stream().collect(Collectors.groupingBy(altar -> MapHelper.getAreaIdByPos(altar.getPos()), HashMap::new, Collectors.groupingBy(Altar::isAreaTop)));
        openArea.forEach(areaObj -> {
            int area = areaObj.getArea();
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(area);
            if (Objects.nonNull(staticArea)) {
                StaticAltarArea staticAltarArea = altarAreaMap.get(staticArea.getOpenOrder());
                if (Objects.nonNull(staticAltarArea)) {
                    Map<Integer, List<Altar>> areaGroup = areaGroupAltar.computeIfAbsent(area, (k) -> new HashMap<>());
                    halfArea.forEach(half -> {
                        int needCnt = staticAltarArea.getHalfInAreaConf(half);
                        List<Altar> areaAltars = areaGroup.computeIfAbsent(half, (k) -> new ArrayList<>());
                        if (!CheckNull.isEmpty(areaAltars)) {
                            needCnt = needCnt - areaAltars.size();
                        }
                        // 半区需要刷新的圣坛
                        if (needCnt > 0) {
                            LogUtil.world(String.format("斋月刷新圣坛, area: %s, half: %s, needCnt: %s", area, half, needCnt));
                            Set<Integer> refreshPosSet = Stream.iterate(0, i -> ++i)
                                    .limit(needCnt)
                                    .map(i -> worldDataManager.randomEmptyPosInArea(area, half))
                                    .filter(pos -> pos > 0)
                                    .collect(Collectors.toSet());
                            refreshPosSet.forEach(pos -> {
                                // 创建圣坛
                                LogUtil.world(String.format("斋月刷新圣坛, area: %s, half: %s, pos: %s, x: %s, y: %s", area, half, pos, MapHelper.reducePos(pos).getA(), MapHelper.reducePos(pos).getB()));
                                // 刷新圣坛跑马灯
                                chatDataManager.sendActivityChat(ChatConst.CHAT_REFRESH_ALTAR, ActivityConst.ACT_VISIT_ALTAR, 0, pos);
                                altarMap.put(pos, new Altar(pos));
                            });
                        }
                    });
                }
            }
        });
        // 同步地图数据给客户端
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(
                altarMap.values().stream()
                        .map(Altar::getPos)
                        .collect(Collectors.toList()),
                Events.AreaChangeNoticeEvent.MAP_TYPE));
        // 清除圣坛跑马灯
        Date nextFireTime = context.getNextFireTime();
        if (nextFireTime.before(activityBase.getEndTime())) {
            chatDataManager.sendActivityChat(ChatConst.CHAT_CLEAR_ALTAR, ActivityConst.ACT_VISIT_ALTAR, 0, TimeHelper.dateToSecond(nextFireTime));
        }
    }

    /**
     * 删除无效的圣坛, 并获取剩余的圣坛
     *
     * @return 剩余的圣坛
     */
    private Map<Integer, Altar> rmAndGetAltar() {
        Map<Integer, Altar> altarMap = worldDataManager.getAltarMap();
        if (!CheckNull.isEmpty(altarMap)) {
            // 同步地图数据给客户端
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(
                    altarMap.values().stream()
                            .map(Altar::getPos)
                            .peek(pos -> LogUtil.world(String.format("移除圣坛, pos: %s, area: %s, block: %s", pos, MapHelper.getAreaIdByPos(pos), MapHelper.block(pos))))
                            .peek(altarMap::remove)
                            .collect(Collectors.toList()),
                    Events.AreaChangeNoticeEvent.MAP_TYPE));
        }
        return altarMap;
    }

    /**
     * 拜访到达
     *
     * @param player 玩家
     * @param army   行军队列
     * @param now    时间
     */
    public void marchEnd(Player player, Army army, int now) {
        // 圣坛坐标
        int targetPos = army.getTarget();
        // 拜访类型
        int visitType = army.getSubType();

        try {
            // 圣坛
            Altar altar = worldDataManager.getAltarMap().get(targetPos);
            if (Objects.isNull(altar)) {
                throw new MwException(GameError.ACT_ALTAR_LOST.getCode(), "该坐标为空闲坐标，不能拜访圣坛, roleId:", player.roleId, ", pos:", targetPos);
            }
            // 校验拜访类型
            if (visitType != VISIT_ALTAR_TYPE_OIL && visitType != VISIT_ALTAR_TYPE_GOLD) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "拜访圣坛参数错误");
            }
        } catch (MwException e) {
            // 返还行军资源
            List<CommonPb.Award> marchConsume = army.getMarchConsume();
            if (!CheckNull.isEmpty(marchConsume)) {
                mailDataManager.sendAttachMail(player, marchConsume, MailConstant.MOLD_VISIT_ALTAR_FAIL, AwardFrom.ACT_VISIT_ALTAR_RETURN_RESOURCE, now, targetPos);
            }
            // 返回这次的拜访记录
            activityDataManager.updActivity(player, ActivityConst.ACT_VISIT_ALTAR, -1, VISIT_CNT_KEY, true);
            LogUtil.error("拜访圣坛失败, exception: ", e);
            return;
        }

        try {
            // 当前拜访次数
            curVisitCnt(player);
            // 圣坛
            Altar altar = worldDataManager.getAltarMap().get(targetPos);
            // 拜访奖励
            List<List<Integer>> visitAward = altar.getVisitAward(visitType);
            if (!CheckNull.isEmpty(visitAward)) {
                List<CommonPb.Award> awards = rewardDataManager.sendReward(player, visitAward, AwardFrom.ACT_VISIT_ALTAR_AWARD);
                if (!CheckNull.isEmpty(awards)) {
                    // 邮件发送拜访奖励
                    mailDataManager.sendReportMail(player, null, MailConstant.MOLD_VISIT_ALTAR_SUCCESS, awards, now, targetPos);
                }
            }
        } catch (MwException e) {
            LogUtil.error("获取本次拜访次数失败, exception: ", e);
        }
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        return null;
    }

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    /**
     * 活动开启定时器
     *
     * @param activityType
     * @param activityId
     * @param keyId
     */
    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {
        LogUtil.world("---------------------拜访圣坛开启刷新定时器---------------------");
        if (!ActParamConstant.ACT_VISIT_REFRESH_CRON.isEmpty()) {
            // 刷新定时器, 读取act_param里337配置
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), joinTriggerName(activityType, activityId, keyId), VISIT_ALTAR_REFRESH_JOB_NAME, VisitAltarRefreshJob.class, ActParamConstant.ACT_VISIT_REFRESH_CRON);
        }
    }

    /**
     * 拜访圣坛结束刷新定时器
     */
    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        LogUtil.world("---------------------拜访圣坛结束刷新定时器---------------------");
        // 移除该活动档位的定时器
        QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), joinTriggerName(activityType, activityId, keyId), VISIT_ALTAR_REFRESH_JOB_NAME);
        // 移除所有的圣坛
        worldDataManager.clearAllAltar();
        // 刷新圣坛活动结束跑马灯
        chatDataManager.sendSysChat(ChatConst.CHAT_VISIT_ALTAR_END, 0, 0);
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }
}
