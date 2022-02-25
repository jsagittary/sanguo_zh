package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ActRobinHood;
import com.gryphpoem.game.zw.resource.domain.p.RobinHoodTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActRobinHood;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquip;
import com.gryphpoem.game.zw.resource.domain.s.StaticStone;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User:        zhoujie
 * Date:        2020/2/14 16:06
 * Description:
 */
@Service
public class ActivityRobinHoodService {

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private TechDataManager techDataManager;


    /**
     * 获取罗宾汉活动的协议
     *
     * @param roleId     玩家id
     * @param activityId 活动id
     * @return
     * @throws MwException
     */
    public GamePb4.GetActRobinHoodRs getActRobinHoodHandler(long roleId, int activityId) throws MwException {
        return GamePb4.GetActRobinHoodRs.newBuilder().setRobinHood(getActRobinHood(roleId, activityId).ser()).build();
    }

    /**
     * 领取活动或者任务奖励
     *
     * @param roleId 玩家id
     * @param req    请求参数
     * @return
     */
    public GamePb4.ActRobinHoodAwardRs actRobinHoodAward(long roleId, GamePb4.ActRobinHoodAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ROBIN_HOOD);
        if (activityBase == null || activityBase.getPlan() == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        GamePb4.ActRobinHoodAwardRs.Builder builder = GamePb4.ActRobinHoodAwardRs.newBuilder();
        ActRobinHood actRobinHood = getActRobinHood(roleId, activityBase.getActivityId());
        if (req.hasActivityId()) {
            // 领取活动奖励
            int activityId = req.getActivityId();
            if (activityBase.getActivityId() != activityId) {
                throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
            }
            int status = actRobinHood.getStatus();
            if (status == ActRobinHood.AWARD_STATUS_1) {
                throw new MwException(GameError.ROBIN_HOOD_ALREADY_AWARD.getCode(), "罗宾汉活动奖励已经领取");
            }
            // 有任务没有完成
            if (actRobinHood.getRobinHoodTaskMap().values().stream().filter(rht -> rht.getStatus() != RobinHoodTask.AWARD_STATUS_0).count() < ActParamConstant.BLACKHAWK_NEED_TOKEN) {
                throw new MwException(GameError.ROBIN_HOOD_ALREADY_AWARD.getCode(), "罗宾汉活动奖励条件未达成");
            }
            List<StaticActAward> actAwardById = StaticActivityDataMgr.getActAwardById(activityId);
            if (CheckNull.isEmpty(actAwardById)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "罗宾汉活动没有配置奖励");
            }
            // 设置为领取状态
            actRobinHood.setStatus(ActRobinHood.AWARD_STATUS_1);
            StaticActAward actAward = actAwardById.get(0);
            for (List<Integer> award : actAward.getAwardList()) {
                builder.addAward(rewardDataManager.addAwardSignle(player, award, AwardFrom.ROBIN_HOOD_ACT_AWARD));
            }
            // 把所有任务奖励给领取
            List<StaticActRobinHood> taskConfList = StaticActivityDataMgr.getActRobinHoodByActId(activityBase.getActivityId());
            if (!CheckNull.isEmpty(taskConfList)) {
                actRobinHood.getRobinHoodTaskMap().values().stream()
                        .filter(rht -> rht.getStatus() == RobinHoodTask.AWARD_STATUS_1)
                        .forEach(rht -> {
                            StaticActRobinHood sConf = taskConfList.stream().filter(conf -> rht.getTaskId() == conf.getTaskId()).findFirst().orElse(null);
                            if (sConf != null) {
                                rht.setStatus(RobinHoodTask.AWARD_STATUS_2);
                                for (List<Integer> award : sConf.getAward()) {
                                    builder.addAward(rewardDataManager.addAwardSignle(player, award, AwardFrom.ROBIN_HOOD_ACT_AWARD));
                                }
                            }
                        });
            }

        }
        if (req.hasTaskId()) {
            // 领取任务奖励
            int taskId = req.getTaskId();
            RobinHoodTask robinHoodTask = actRobinHood.getRobinHoodTaskMap().get(taskId);
            if (robinHoodTask == null) {
                throw new MwException(GameError.ROBIN_HOOD_TASK_STAUS_ERROR.getCode(), "没有这个任务");
            }
            // 任务未完成，或者已经领取过了
            if (robinHoodTask.getStatus() != RobinHoodTask.AWARD_STATUS_1) {
                throw new MwException(GameError.ROBIN_HOOD_TASK_STAUS_ERROR.getCode(), "任务未完成，或者已经领取过了");
            }
            List<StaticActRobinHood> taskConfList = StaticActivityDataMgr.getActRobinHoodByActId(activityBase.getActivityId());
            if (CheckNull.isEmpty(taskConfList)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "罗宾汉活动没有找到任务配置");
            }
            StaticActRobinHood sConf = taskConfList.stream().filter(rht -> rht.getTaskId() == taskId).findFirst().orElse(null);
            if (sConf == null) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "罗宾汉活动没有找到任务配置");
            }
            robinHoodTask.setStatus(RobinHoodTask.AWARD_STATUS_2);
            for (List<Integer> award : sConf.getAward()) {
                builder.addAward(rewardDataManager.addAwardSignle(player, award, AwardFrom.ROBIN_HOOD_ACT_AWARD));
            }
        }
        return builder.build();
    }


    /**
     * 获取罗宾汉活动数据
     *
     * @param roleId     玩家id
     * @param activityId 活动id
     * @throws MwException
     */
    private ActRobinHood getActRobinHood(long roleId, int activityId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 检测活动是否截止
        activityService.checkBlackhawkIsOver(player, TimeHelper.getCurrentSecond());
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ROBIN_HOOD);
        if (activityBase == null || activityBase.getPlan() == null || activityBase.getPlan().getActivityId() != activityId) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        int begin = TimeHelper.getDay(activityBase.getBeginTime());
        ActRobinHood actRobinHood = Optional.ofNullable(player.actRobinHood.get(activityId)).orElse(new ActRobinHood(activityId, begin));
        if (actRobinHood.getRobinHoodTaskMap().isEmpty()) {
            Map<Integer, RobinHoodTask> hoodTask = robinHoodTask(activityId);
            if (CheckNull.isEmpty(hoodTask)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "罗宾汉活动配置错误");
            }
            actRobinHood.setRobinHoodTaskMap(hoodTask);
            player.actRobinHood.put(activityId, actRobinHood);
        }
        // 主动刷新任务进度
        refreshTaskSchedule(actRobinHood, player, StaticActivityDataMgr.getActRobinHoodByActId(activityId));
        return actRobinHood;
    }

    /**
     * 罗宾汉任务
     *
     * @param activityId
     * @return
     */
    private Map<Integer, RobinHoodTask> robinHoodTask(int activityId) {
        List<StaticActRobinHood> list = StaticActivityDataMgr.getActRobinHoodByActId(activityId);
        if (CheckNull.isEmpty(list)) {
            return null;
        }
        return list.stream().collect(Collectors.toMap(StaticActRobinHood::getTaskId, RobinHoodTask::new));
    }

    /**
     * 更新玩家的任务进度
     *
     * @param player
     * @param cond
     * @param schedule
     * @param param
     */
    public void updateTaskSchedule(Player player, int cond, int schedule, int... param) {
        if (player == null) {
            return;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_ROBIN_HOOD);
        if (activityBase == null || activityBase.getPlan() == null) {
            return;
        }
        try {
            ActRobinHood actRobinHood = getActRobinHood(player.roleId, activityBase.getActivityId());
            if (actRobinHood == null) {
                return;
            }
            Map<Integer, RobinHoodTask> taskMap = actRobinHood.getRobinHoodTaskMap();
            List<StaticActRobinHood> sTaskConf = StaticActivityDataMgr.getActRobinHoodByActIdAndTaskCond(activityBase.getActivityId(), cond);
            if (CheckNull.isEmpty(sTaskConf)) {
                return;
            }
            sTaskConf.forEach(sConf -> modifyTaskSchedule(sConf, taskMap.get(sConf.getTaskId()), schedule, param));
            // 同步任务
            syncTaskSchedule(sTaskConf, actRobinHood, player);
        } catch (MwException e) {
            LogUtil.common("更新玩家的罗宾汉任务进度报错", e.getMessage());
        }
    }

    /**
     * 同步任务的变动
     *
     * @param sTaskConf
     * @param actRobinHood
     * @param player
     */
    private void syncTaskSchedule(List<StaticActRobinHood> sTaskConf, ActRobinHood actRobinHood, Player player) {
        if (player != null && actRobinHood != null) {
            List<CommonPb.RobinHoodTask> collect = sTaskConf.stream().map(sConf -> actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).ser()).collect(Collectors.toList());
            GamePb4.SyncActRobinHoodRs.Builder builder = GamePb4.SyncActRobinHoodRs.newBuilder();
            builder.addAllTask(collect);
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb4.SyncActRobinHoodRs.EXT_FIELD_NUMBER, GamePb4.SyncActRobinHoodRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 修改任务的变动
     *
     * @param sConf
     * @param task
     * @param schedule
     * @param param
     */
    private void modifyTaskSchedule(StaticActRobinHood sConf, RobinHoodTask task, int schedule, int[] param) {
        // 已经完成条件
        if (task.getSchedule() >= sConf.getSchedule()) {
            return;
        }
        // 条件Id
        int sCondId = sConf.getCondId();
        int paramId = param.length > 0 ? param[0] : 0;
        switch (sConf.getCond()) {
                // 主城升到xx等级
            case ActivityConst.ACT_TASK_BUILDING:
                // 使用指定xx道具
            case ActivityConst.ACT_TASK_USE_PROP:
                // 大学升到xx等级
            case ActivityConst.ACT_TASK_UNIVERSITY_LV:
                // 参与攻打据点
            case ActivityConst.ACT_TASK_JOIN_ATK:
                // 参与攻下几座指定类型的城
            case ActivityConst.ACT_TASK_ATTACK:
                // 装备改造xx次
            case ActivityConst.ACT_TASK_EQUIP_WASH_CNT:
                // 宝石合成次数
            case ActivityConst.COND_STONE_COMPOUND_COMBAT:
                if (sCondId == 0 || sCondId == paramId) {
                    task.setSchedule(task.getSchedule() + schedule, sConf);
                }
                break;
                // 击杀几级以上武装力量
            case ActivityConst.ACT_TASK_ATK_BANDIT:
                // 累计消费多少金额
            case ActivityConst.ACT_TASK_COST_GOLD:
                if (sCondId == 0 || paramId >= sCondId) {
                    task.setSchedule(task.getSchedule() + schedule, sConf);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 主动更新
     *
     * @param actRobinHood
     * @param player
     * @param sConfList
     */
    private void refreshTaskSchedule(ActRobinHood actRobinHood, Player player, List<StaticActRobinHood> sConfList) {
        if (CheckNull.isEmpty(sConfList)) {
            return;
        }
        sConfList.forEach(sConf -> {
            int condId = sConf.getCondId();
            switch (sConf.getCond()) {
                // 上阵将领指定部位(param里面填)穿戴xx品质的装备
                case ActivityConst.ACT_TASK_EQUIP_QUALITY_CNT:
                    int schedule = (int) player.getAllOnBattleHeros().stream()
                            .filter(hero -> {
                                Equip equip;
                                StaticEquip staticEquip;
                                for (int i = 1; i < hero.getEquip().length; i++) {
                                    int equipKey = hero.getEquip()[i];
                                    // 说明没有穿戴装备
                                    if (equipKey == 0) {
                                        return false;
                                    }
                                    equip = player.equips.get(equipKey);
                                    // 没有找到这个装备
                                    if (equip == null) {
                                        return false;
                                    }
                                    staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
                                    if (staticEquip == null) {
                                        return false;
                                    }
                                    if (staticEquip.getQuality() < condId) {
                                        return false;
                                    }
                                }
                                // 说明所有部位的装备都满足
                                return true;
                            }).count();
                    // 更新任务进度
                    actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).setSchedule(schedule, sConf);
                    break;
                case ActivityConst.ACT_TASK_BUILDING:
                    int command = player.building.getCommand();
                    // 更新任务进度
                    actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).setSchedule(command, sConf);
                    break;
                case ActivityConst.ACT_TASK_UNIVERSITY_LV:
                    int tech = player.building.getTech();
                    // 更新任务进度
                    actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).setSchedule(tech, sConf);
                    break;
                // 拥有x个x级配饰(原宝石)
                case ActivityConst.ACT_TASK_STONE_CNT:
                    // 未穿戴
                    int cnt = player.getStoneInfo().getStones().entrySet()
                            .stream()
                            .filter(en -> {
                                int id = en.getKey();
                                StaticStone sStone = StaticPropDataMgr.getStoneMapById(id);
                                if (sStone == null) {
                                    return false;
                                }
                                // 2020-04-07 秦岭说算上大于等于佩饰等级
                                return sStone.getLv() >= condId;
                            }).mapToInt(en -> en.getValue().getCnt()).sum();
                    // 已穿戴
                    cnt += (int) player.getStoneInfo().getStoneHoles().values()
                            .stream()
                            .filter(en -> {
                                int stoneId = en.getStoneId();
                                StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
                                if (sStone == null) {
                                    return false;
                                }
                                // 2020-04-07 秦岭说算上大于等于佩饰等级
                                return sStone.getLv() >= condId;
                            }).count();
                    actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).setSchedule(cnt, sConf);
                    break;
                // 收集指定xx品质及以上的某部位装备
                case ActivityConst.ACT_TASK_EQUIP:
                    int equipCnt = (int) player.equips.values().stream()
                            .filter(equip -> {
                                int equipId = equip.getEquipId();
                                StaticEquip sEquip = StaticPropDataMgr.getEquip(equipId);
                                if (sEquip == null) {
                                    return false;
                                }
                                return sEquip.getQuality() >= condId;
                            }).count();
                    actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).setSchedule(equipCnt, sConf);
                    break;
                // 完成某个科技
                case ActivityConst.ACT_TASK_TECH:
                    int techLv = techDataManager.getTechLv(player, condId);
                    // 更新任务进度
                    actRobinHood.getRobinHoodTaskMap().get(sConf.getTaskId()).setSchedule(techLv, sConf);
                    break;
                default:
                    break;
            }
        });
    }


}
