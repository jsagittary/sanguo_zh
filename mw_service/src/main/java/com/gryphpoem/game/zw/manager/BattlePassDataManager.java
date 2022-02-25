package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBattlePassDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassTask;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.BattlePassPersonInfo;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.GlobalBattlePass;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.battlePassTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-04 15:13
 */
@Component
public class BattlePassDataManager {


    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 更新玩家的战令活动进度
     *
     * @param lordId   玩家的唯一Id
     * @param cond     对应的任务条件
     * @param schedule 完成的进度
     * @param param    参数
     */
    public void updTaskSchedule(long lordId, int cond, int schedule, int... param) {
        Player player = playerDataManager.getPlayer(lordId);
        if (player == null/* || !StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_BATTLE_PASS)*/) {
            // 玩家没找到, 或者战令功能未开放
            return;
        }
        updTask(getPersonInfo(lordId), lordId, cond, schedule, param);
    }

    /**
     * 更新玩家的战令活动进度
     *
     * @param personInfo 战令的个人数据
     * @param lordId     玩家的唯一Id
     * @param cond       对应的任务条件
     * @param schedule   完成的进度
     * @param param      参数
     */
    private void updTask(BattlePassPersonInfo personInfo, long lordId, int cond, int schedule, int[] param) {
        if (CheckNull.isNull(personInfo)) {
            return;
        }
        int staticKey = personInfo.getStaticKey();
        Collection<StaticBattlePassTask> sPassTasks = StaticBattlePassDataMgr.getTasksByPlanKey(staticKey);
        if (CheckNull.isEmpty(sPassTasks)) {
            return;
        }
        // 个人的战令任务记录
        Map<Integer, battlePassTask> passTaskMap = personInfo.getPassTaskMap();
        // 符合条件的任务
        List<StaticBattlePassTask> condTasks = sPassTasks.stream().filter(sTask -> sTask.getCond() == cond).distinct().collect(Collectors.toList());
        // 更新完进度，并过滤没有进度改变的任务
        condTasks = condTasks.stream().filter(sTask -> modifyTaskSchedule(sTask, passTaskMap.computeIfAbsent(sTask.getId(), task -> new battlePassTask(sTask.getId())), schedule, param)).collect(Collectors.toList());
        // 同步任务
        syncTaskSchedule(condTasks, personInfo, lordId);
    }

    // 同步战令任务的变动
    private void syncTaskSchedule(List<StaticBattlePassTask> condTasks, BattlePassPersonInfo personInfo, long lordId) {
        Player player = playerDataManager.getPlayer(lordId);
        if (player != null && personInfo != null) {
            Map<Integer, battlePassTask> passTaskMap = personInfo.getPassTaskMap();
            List<CommonPb.BattlePassTaskPb> collect = condTasks.stream().map(sPassTask -> passTaskMap.get(sPassTask.getId())).filter(bPassTask -> !CheckNull.isNull(bPassTask)).map(PbHelper::createBattlePassTaskPb).collect(Collectors.toList());
            if (CheckNull.isEmpty(collect)) {
                return;
            }
            GamePb4.SyncBattlePassTaskRs.Builder builder = GamePb4.SyncBattlePassTaskRs.newBuilder();
            builder.addAllTask(collect);
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb4.SyncBattlePassTaskRs.EXT_FIELD_NUMBER, GamePb4.SyncBattlePassTaskRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 修改任务进度
     *  @param sTask    任务的配置
     * @param passTask 任务的Bean
     * @param schedule 进度
     * @param param    参数
     * @return
     */
    private boolean modifyTaskSchedule(StaticBattlePassTask sTask, battlePassTask passTask, int schedule, int... param) {
        boolean modify = false;
        // 已经达成条件
        if (passTask.getSchedule() >= sTask.getSchedule()) {
            return modify;
        }
        int sCondId = sTask.getCondId();
        int paramId = param.length > 0 ? param[0] : 0;
        switch (sTask.getCond()) {
            case TaskType.COND_BANDIT_LV_CNT:
            case TaskType.COND_ARM_TYPE_CNT:
            case TaskType.COND_BATTLE_STATE_LV_CNT:
            case TaskType.COND_BATTLE_CITY_LV_CNT:
            case TaskType.COND_19:
            case TaskType.COND_LOGIN_36:
            case TaskType.COND_COMBAT_37:
            case TaskType.COND_CAMP_BUILD_38:
            case TaskType.COND_BUY_ACT_40:
            case TaskType.COND_JOIN_CAMP_BATTLE_41:
            case TaskType.COND_WASH_HERO_42:
            case TaskType.COND_BUILDING_UP_43:
            case TaskType.COND_STONE_COMBAT_47:
            case TaskType.COND_DAILY_LIVENSS:
            case TaskType.COND_RESOURCE_CNT:
            case TaskType.COND_SUB_GOLD_CNT:
            case TaskType.COND_STONE_COMBAT_BUY_CNT:
            case TaskType.COND_SUB_HERO_ARMY:
            case TaskType.COND_GOLD_CNT:
            case TaskType.COND_REBEL_ATK_CNT:
            case TaskType.COND_RES_AWARD:
            case TaskType.COND_EQUIP_BAPTIZE:
            case TaskType.COND_CHEMICAL:
            case TaskType.COND_SUPER_MINE_CNT:
            case TaskType.COND_UP_CITY_CNT:
            case TaskType.COND_BERLIN_WIN_CNT:
            case TaskType.COND_BERLIN_FRONT_CNT:
            case TaskType.COND_HERO_DECORATED_HAVE_CNT:
            case TaskType.COND_SEARCH_HERO_CNT:
            case TaskType.COND_EXPLOIT_CNT:
            case TaskType.COND_FISHING_MASTER://钓到一个鱼
                // 等于配置的条件, 就将进度+1
                if (sCondId == 0 || sCondId == paramId) {
                    passTask.setSchedule(passTask.getSchedule() + schedule, sTask);
                    modify = true;
                }
                break;
            // 条件是装备的类型
            case TaskType.COND_HERO_EQUIPID_QUALITY:
                // 大于装备的品质
                if (sCondId == 0 || paramId >= sCondId) {
                    passTask.setSchedule(passTask.getSchedule() + schedule, sTask);
                    modify = true;
                }
                break;
        }
        return modify;
    }


    /**
     * 获取当前开放的战令公共数据
     *
     * @return 战令的数据
     */
    public GlobalBattlePass getGlobalBattlePass() {
        int now = TimeHelper.getCurrentSecond();
        Date nowDate = new Date();
        StaticBattlePassPlan sPassPlan = StaticBattlePassDataMgr.currentOpenPlan();
        GameGlobal gameGlobal = globalDataManager.getGameGlobal();
        GlobalBattlePass globalBattlePass = gameGlobal.getGlobalBattlePass();
        if (!CheckNull.isNull(globalBattlePass)) {
            // 已经初始化过战令了
            if (!CheckNull.isNull(sPassPlan)) {
                if (!globalBattlePass.isSamePlan(sPassPlan)) {
                    // 开启模板不一致
                    globalBattlePass = new GlobalBattlePass(sPassPlan);
                    LogUtil.debug("重置战令的相关功能, ", globalBattlePass);
                } else if (DateHelper.dayiy(globalBattlePass.getBeginDate(), sPassPlan.getRealBeginDate()) != 1 || DateHelper.dayiy(globalBattlePass.getEndDate(), sPassPlan.getRealEndDate()) != 1) {
                    // 修改战令的开启和结束时间
                    LogUtil.debug("修改本次战令的开启或者结束时间, 修改前: ", globalBattlePass);
                    globalBattlePass.setBeginDate(sPassPlan.getRealBeginDate());
                    globalBattlePass.setEndDate(sPassPlan.getRealEndDate());
                    LogUtil.debug("修改本次战令的开启或者结束时间, 修改后: ", globalBattlePass);
                }
            } else {
                if (!CheckNull.isNull(globalBattlePass.getEndDate()) && nowDate.before(globalBattlePass.getEndDate())) {
                    // 现在不在战令的开放时间内
                    globalBattlePass.setEndDate(nowDate);
                    // 将结束时间设置成现在
                    LogUtil.debug("提前结束本次战令, 修改后: ", globalBattlePass);
                }
            }
            // 到了结束时间并且没有发送过邮件
            Map<Integer, Integer> mixtureDataById = gameGlobal.getMixtureDataById(GlobalConstant.BATTLE_PASS_SEND_MAIL_KEY);
            if (mixtureDataById.getOrDefault(0, 0) != globalBattlePass.getKey() && now > TimeHelper.dateToSecond(globalBattlePass.getEndDate())) {
                playerDataManager.getPlayers().values().stream()
                        // 过滤战令功能未开放的
                        .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_BATTLE_PASS))
                        .forEach(p -> {
                            BattlePassPersonInfo personInfo = p.getBattlePassPersonInfo();
                            List<List<Integer>> mergeAward = personInfo.receiveBPAward(p);
                            if (!CheckNull.isEmpty(mergeAward)) {
                                List<CommonPb.Award> awardsPb = PbHelper.createAwardsPb(mergeAward);
                                if (!CheckNull.isEmpty(awardsPb)) {
                                    // 发送未领取的战令的奖励
                                    mailDataManager.sendAttachMail(p, awardsPb, MailConstant.MOLD_BATTLE_PASS_REWARD,
                                            AwardFrom.ACT_UNREWARDED_RETURN, now);
                                }
                            }
                        });
                LogUtil.debug("结束时间发送战令奖励邮件, ", globalBattlePass);
                // 更新结束发邮件的id
                mixtureDataById.put(0, globalBattlePass.getKey());
                gameGlobal.setMixtureData(GlobalConstant.BATTLE_PASS_SEND_MAIL_KEY, mixtureDataById);
            }
        } else {
            // 战令没有初始化
            if (!CheckNull.isNull(sPassPlan)) {
                // 首次开启
                globalBattlePass = new GlobalBattlePass(sPassPlan);
                LogUtil.debug("初始化战令的相关功能, ", globalBattlePass);
            }
        }
        // 覆盖gameGlobal中的战令全局数据
        gameGlobal.setGlobalBattlePass(globalBattlePass);
        return globalBattlePass;
    }


    /**
     * 之前的战令初始化获取的代码
     */

   /* public GlobalBattlePass getGlobalBattlePass() {
        int now = TimeHelper.getCurrentSecond();
        StaticBattlePassPlan sPassPlan = StaticBattlePassDataMgr.currentOpenPlan();
        if (!CheckNull.isNull(sPassPlan)) {
            GameGlobal gameGlobal = globalDataManager.getGameGlobal();
            GlobalBattlePass globalBattlePass = gameGlobal.getGlobalBattlePass();
            // 没有开启过战令活动 或者 开启模板不一致
            if (CheckNull.isNull(globalBattlePass) || !globalBattlePass.isSamePlan(sPassPlan)) {
                playerDataManager.getPlayers().values().stream()
                        // 过滤战令功能未开放的
                        .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_BATTLE_PASS))
                        .forEach(p -> {
                            BattlePassPersonInfo personInfo = p.getBattlePassPersonInfo();
                            List<List<Integer>> mergeAward = personInfo.receiveBPAward();
                            if (!CheckNull.isEmpty(mergeAward)) {
                                List<CommonPb.Award> awardsPb = PbHelper.createAwardsPb(mergeAward);
                                if (!CheckNull.isEmpty(awardsPb)) {
                                    mailDataManager.sendAttachMail(p, awardsPb, MailConstant.MOLD_BATTLE_PASS_REWARD,
                                            AwardFrom.ACT_UNREWARDED_RETURN, now);
                                }
                            }
                        });
                globalBattlePass = new GlobalBattlePass(sPassPlan);
                LogUtil.debug("首次开启或重置战令的相关功能, ", globalBattlePass);
            } else if (DateHelper.dayiy(globalBattlePass.getBeginDate(), sPassPlan.getRealBeginDate()) != 1 || DateHelper.dayiy(globalBattlePass.getEndDate(), sPassPlan.getRealEndDate()) != 1) {
                LogUtil.debug("修改本次战令的开启或者结束时间, 修改前: ", globalBattlePass);
                globalBattlePass.setBeginDate(sPassPlan.getRealBeginDate());
                globalBattlePass.setEndDate(sPassPlan.getRealEndDate());
                LogUtil.debug("修改本次战令的开启或者结束时间, 修改后: ", globalBattlePass);
            }
            gameGlobal.setGlobalBattlePass(globalBattlePass);
            return globalBattlePass;
        }
        return null;
    }*/

    /**
     * 获取玩家身上的战令个人数据
     *
     * @param roleId 玩家唯一Id
     * @return 战令的个人数据
     */
    public BattlePassPersonInfo getPersonInfo(long roleId) {
        GlobalBattlePass globalBattlePass = getGlobalBattlePass();
        if (!CheckNull.isNull(globalBattlePass)) {
            Player player = playerDataManager.getPlayer(roleId);
            if (!CheckNull.isNull(player)) {
                BattlePassPersonInfo personInfo = player.getBattlePassPersonInfo();
                StaticBattlePassPlan sBattlePassPlan = StaticBattlePassDataMgr.getPlanById(globalBattlePass.getStaticKey());
                // 个人和公共数据中存储的KEY不一致
                if (sBattlePassPlan != null && personInfo.getStaticKey() != sBattlePassPlan.getPlanId()) {
                    personInfo.clearData(globalBattlePass.getKey(), sBattlePassPlan.getPlanId());
                }
                return personInfo;
            }
        }
        return null;
    }


    /**
     * 转点定时任务, 清除战令玩家数据
     */
    public void clearTaskAndData() {
        Date now = new Date();
        Java8Utils.invokeNoExceptionICommand(() ->
                // 这里的getGlobalBattlePass()会初始化和重置战令
                Optional.ofNullable(getGlobalBattlePass())
                        .ifPresent(globalBattlePass ->
                                Optional.ofNullable(globalBattlePass.needRefreshKey(now))
                                        .ifPresent(needRefreshKey ->
                                                playerDataManager.getPlayers().values().stream()
                                                        // 过滤战令功能未开放的
                                                        .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_BATTLE_PASS))
                                                        .forEach(p -> {
                                                            Optional.ofNullable(getPersonInfo(p.roleId)).ifPresent(personInfo -> personInfo.refreshTaskAndData(needRefreshKey));
                                                            Java8Utils.invokeNoExceptionICommand(() -> updTaskSchedule(p.roleId, TaskType.COND_LOGIN_36, 1));
                                                        }))));
    }

    /**
     * 主动刷新任务的进度
     *
     * @param sPassTask 任务的配置
     * @param passTask  任务
     * @param player    玩家对象
     */
    public void refreshTaskSchedule(StaticBattlePassTask sPassTask, battlePassTask passTask, Player player) {
        int condId = sPassTask.getCondId();
        int cond = sPassTask.getCond();
        switch (cond) {
            case TaskType.COND_HERO_DECORATED_HAVE_CNT:
                // 拥有x个觉醒(授勋)x次的将领
                long count = player.heros.values().stream().filter(hero -> hero.getDecorated() == condId).count();
                passTask.setSchedule((int) count, sPassTask);
                break;
            default:
                break;
        }
    }
}