package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.structs.TasksQueue;
import com.gryphpoem.game.zw.dataMgr.StaticCiaDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.Barrage;
import com.gryphpoem.game.zw.pb.GamePb3.SyncBarrageRs;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Cia;
import com.gryphpoem.game.zw.resource.domain.p.FemaleAgent;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgent;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgentGift;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgentStar;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @ClassName CiaService.java
 * @Description 情报部
 * @author QiuKun
 * @date 2018年6月4日
 */
@Component
public class CiaService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private TitleService titleService;

    private LinkedList<Barrage> barrageList = new LinkedList<>(); // 用于存储全局弹幕使用
    private final static int BARRAGE_LIST_MAX = 100;

    /**
     * 添加弹幕
     *
     * @param barrage
     */
    private void addBarrage(Barrage barrage) {
        barrageList.add(barrage);
        if (barrageList.size() > BARRAGE_LIST_MAX) {
            barrageList.removeFirst();
        }
    }

    /**
     * 获取情报相关
     *
     * @param roleId 玩家id
     * @return
     * @throws MwException
     */
    public GetCiaRs getCia(long roleId, int param) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.CIA)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "情报部未解锁 roleId:", roleId);
        }
        Cia cia = player.getCia();
        if (cia == null) {// 开启情报部
            cia = new Cia(Constant.CIA_INTERACTION_MAX_CNT);
            player.setCia(cia);
        }
        // 刷新操作
        refreshCntAndTime(cia);
        // 检测开启特工开启
        checkAndOpenFemaleAgent(player);
        GetCiaRs.Builder builder = GetCiaRs.newBuilder();
        builder.setInteractionCnt(cia.getInteractionCnt());
        Collection<FemaleAgent> ciaCollection = cia.getFemaleAngets().values();
        builder.setMaxFavorQuality(getMaxFavor(ciaCollection));
        builder.setMaxStar(getMaxStar(ciaCollection));
        // if (cia.getInteractionCnt() < Constant.CIA_INTERACTION_MAX_CNT) {
        //     builder.setRefreshTime(cia.getLastTime() + Constant.CIA_INTERACTION_RECOVERY_CD);
        // }
        cia.getFemaleAngets().values().forEach(fa -> builder.addFemaleAgent(PbHelper.createFemaleAgent(fa)));
        // if (param == 0) {// 1.只获取互动次数和特工数据
        //     builder.addAllBarrage(RandomUtil.getListRandom(barrageList, 10)); // 获取10条
        // }
        return builder.build();
    }

    /**
     * 解锁特工
     *
     * @param roleId
     * @return
     */
    public UnlockAgentRs unlockAgent(long roleId, int agentId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.CIA)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "情报部未解锁 roleId:", roleId);
        }
        Cia cia = player.getCia();
        if (cia == null) {// 开启情报部
            cia = new Cia(Constant.CIA_INTERACTION_MAX_CNT);
            player.setCia(cia);
        }
        // 检测开启特工开启
        checkAndOpenFemaleAgent(player);
        FemaleAgent femaleAgent = cia.getFemaleAngets().get(agentId);

        if (CheckNull.isNull(femaleAgent)) {
            throw new MwException(GameError.CIA_AGENT_NOT_HAVE.getCode(), "特工未获取 roleId:", roleId, ", agentId:", agentId);
        }
        List<StaticAgent> staticAgentList = StaticCiaDataMgr.getAgentConfById(agentId);
        if (ListUtils.isBlank(staticAgentList)) {
            throw new MwException(GameError.CIA_AGENT_UNLOCK_NON_CONFIG.getCode(), "解锁佳人配置不存在 roleId:", roleId, ", agentId:", agentId);
        }
        if (CiaConstant.AGENT_UNLOCK_STATUS_2 == femaleAgent.getStatus()) {
            throw new MwException(GameError.CIA_AGENT_ALREADY_UNLOCK.getCode(), "特工已经解锁了 roleId:", roleId, ", agentId:", agentId);
        }
        if (CiaConstant.AGENT_UNLOCK_STATUS_0 == femaleAgent.getStatus()) {
            throw new MwException(GameError.CIA_AGENT_CANT_UNLOCK.getCode(), "特工解锁任务未达成 roleId:", roleId, ", agentId:", agentId);
        }
        StaticAgent staticAgent = staticAgentList.get(0);
        boolean isUseItem = true;
        if(!CheckNull.isEmpty(staticAgent.getUnlock())){//check 2
            //check unlock1
            if(this.checkCondition(staticAgent.getUnlock(),player)){
                isUseItem = false;
            }
        }
        if(isUseItem){
            if(!CheckNull.isEmpty(staticAgent.getUnlock1())){
                if(checkCondition(staticAgent.getUnlock1(),player)){
                    List<List<Integer>> consumeList = ListUtils.createItems(staticAgent.getUnlock1().get(1), staticAgent.getUnlock1().get(2), staticAgent.getUnlock1().get(3));
                    rewardDataManager.checkAndSubPlayerRes(player, consumeList, AwardFrom.UNLOCK_AGENT);
                }
            }
        }

        if (agentId == 2) {
            chatDataManager.sendSysChat(ChatConst.CHAT_CIA_UNLOCK_AGENT_2, player.lord.getCamp(), 0,
                    player.lord.getCamp(), player.lord.getNick(), 4);
        } else if (agentId == 3) {
            chatDataManager.sendSysChat(ChatConst.CHAT_CIA_UNLOCK_AGENT_3, player.lord.getCamp(), 0,
                    player.lord.getCamp(), player.lord.getNick(), 5);
        }
        femaleAgent.setStatus(CiaConstant.AGENT_UNLOCK_STATUS_2);
        femaleAgent.setAppointmentCnt(Constant.CIA_APPOINTMENT_MAX_CNT);
        taskDataManager.updTask(player, TaskType.COND_UNLOCK_AGENT, 1,agentId);
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_AGENT_STAR_CNT);

        //貂蝉任务-拥有佳人X个X星
        ActivityDiaoChanService.completeTask(player, ETask.OWN_BEAUTY);
        TaskService.processTask(player, ETask.OWN_BEAUTY);

        UnlockAgentRs.Builder builder = UnlockAgentRs.newBuilder();
        builder.setFemaleAgent(PbHelper.createFemaleAgent(femaleAgent));
        return builder.build();
    }

    /**
     * 开启情报部
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    // @Deprecated
    // public OpenCiaRs openCia(long roleId) throws MwException {
    //     // Player player = playerDataManager.checkPlayerIsExist(roleId);
    //     // if (player.getCia() != null) {
    //     // throw new MwException(GameError.PARAM_ERROR.getCode(), "情报部已经开启 roleId:", roleId);
    //     // }
    //     // int openTime = DateHelper.afterDayTime(player.account.getCreateDate(), Constant.OPEN_CIA_TIME);
    //     // int now = TimeHelper.getCurrentSecond();
    //     // if (now < openTime || !StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.CIA)) {
    //     // throw new MwException(GameError.FUNCTION_LOCK.getCode(), "情报部未解锁 roleId:", roleId);
    //     // }
    //     //
    //     // Cia cia = new Cia(Constant.CIA_INTERACTION_MAX_CNT);
    //     // player.setCia(cia); // 开启情报部
    //     // checkAndOpenFemaleAgent(player);
    //     // LogLordHelper.commonLog("openCia", AwardFrom.COMMON, player);
    //     // OpenCiaRs.Builder builder = OpenCiaRs.newBuilder();
    //     // builder.setInteractionCnt(cia.getInteractionCnt());
    //     // cia.getFemaleAngets().values().forEach(fa -> builder.addFemaleAgent(PbHelper.createFemaleAgent(fa)));
    //     // return builder.build();
    //     return null;
    // }

    /**
     * 推送使用
     */
    public void interactionCntTimer() {
        playerDataManager.getPlayers().values().stream().forEach(p -> {
            Cia cia = p.getCia();
            if (cia != null && cia.getInteractionCnt() < Constant.CIA_INTERACTION_MAX_CNT) {
                refreshCntAndTime(p.getCia());// 刷新时间
                if (cia.getInteractionCnt() >= Constant.CIA_INTERACTION_MAX_CNT) {
                    // p.putPushRecord(PushConstant.INTERACTION_CNT_FULL, PushConstant.PUSH_HAS_PUSHED);
                    // PushMessageUtil.pushMessage(p.account, PushConstant.INTERACTION_CNT_FULL);
                }
            }
        });
    }


    /**
     * 刷新时间
     *
     * @param cia
     */
    private void refreshCntAndTime(Cia cia) {
        if (cia == null) {
            return;
        }
        // 如果不是同一天
        Date beginDate = TimeHelper.getDate(Long.valueOf(cia.getLastTime()));
        if (cia.getLastTime() > 0 && DateHelper.dayiy(beginDate, new Date()) > 1) {
            cia.setInteractionCnt(Constant.CIA_INTERACTION_MAX_CNT);
            for (FemaleAgent fa : cia.getFemaleAngets().values()) {
                fa.setAppointmentCnt(Constant.CIA_APPOINTMENT_MAX_CNT);
            }
        }
    }

    /**
     * 情报部转点处理
     * @param player 玩家对象
     */
    public void refreshCntAndTime(Player player) {
        Optional.ofNullable(player.getCia())
                .ifPresent(cia -> {
                    // 重置每日情报部互动次数
                    cia.setInteractionCnt(Constant.CIA_INTERACTION_MAX_CNT);
                    cia.getFemaleAngets().values().forEach(fa -> {
                        // 重置每日约会次数
                        fa.setAppointmentCnt(Constant.CIA_APPOINTMENT_MAX_CNT);
                        // 清除每日特工提升好感度次数
                        fa.setDailyFree(0);
                    });
                });
    }

    /**
     * 检测并解锁特工
     *
     * @param player 玩家对象
     */
    public void checkAndOpenFemaleAgent(Player player) {
        Cia cia = player.getCia();
        if (cia != null) {
            Map<Integer, FemaleAgent> femaleAngets = cia.getFemaleAngets();
            StaticCiaDataMgr.getAgentIds()
                    .forEach(agentId -> {
                        FemaleAgent agent = femaleAngets.computeIfAbsent(agentId, (k) -> new FemaleAgent(agentId));
                        // 解锁状态
                        int status = agent.getStatus();
                        if (status != CiaConstant.AGENT_UNLOCK_STATUS_0) {
                            return;
                        }
                        // 检测任务是否达成
                        if (checkUnlockTask(player, agentId)) {
                            agent.setStatus(CiaConstant.AGENT_UNLOCK_STATUS_1);
                            //第1位佳丽默认解锁
                            if (agentId == 1) {
                                agent.setStatus(CiaConstant.AGENT_UNLOCK_STATUS_2);
                                agent.setAppointmentCnt(Constant.CIA_APPOINTMENT_MAX_CNT);
                            }
                        }
                    });
        }
    }

    /**
     * 检测玩家解锁任务是否达成
     *
     * @param player  玩家对象
     * @param agentId 特工id
     * @return true 达成
     */
    private boolean checkUnlockTask(Player player, int agentId) {
        List<Integer> unlockConf = StaticCiaDataMgr.getUnlockById(agentId);
        List<Integer> unlockConf1 = StaticCiaDataMgr.getUnlockById1(agentId);
        // 如果不配置解锁条件, 则默认就解锁
        if(CheckNull.isEmpty(unlockConf) && CheckNull.isEmpty(unlockConf1)){
            return true;
        }else {
            boolean b1 = false,b2 = false;
            if(!CheckNull.isEmpty(unlockConf)){
                b1 = checkCondition(unlockConf,player);
            }
            if(!CheckNull.isEmpty(unlockConf1)){
                b2 = checkCondition(unlockConf1,player);
            }
            return b1 || b2;
        }
    }

    private boolean checkCondition(List<Integer> unlockConf,Player player){
        int unlockType = unlockConf.get(0);
        switch (unlockType) {
            case CiaConstant.AGENT_UNLOCK_TYPE_1:
                int command = player.building.getCommand();
                if (command >= unlockConf.get(1)) {
                    return true;
                }
                break;
            case CiaConstant.AGENT_UNLOCK_TYPE_2:
                break;
            case CiaConstant.AGENT_UNLOCK_TYPE_3:
                int vip = player.lord.getVip();
                if (vip >= unlockConf.get(1)) {
                    return true;
                }
                break;
            case CiaConstant.AGENT_UNLOCK_TYPE_4:
                long count = rewardDataManager.getRoleResByType(player,unlockConf.get(1),unlockConf.get(2));
                if(count >= unlockConf.get(3)){
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * 互动
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public InteractionRs interaction(long roleId, InteractionRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int agentId = req.getId(); //特工id
        int type = req.getType();  //互动类型-翻牌选女王，0为选择错误，1位选择正确
        int count = req.getCount();  // 互动次数，1次或者5次
        checkAgentExistAndUnlock(player, agentId);
        Cia cia = player.getCia();
        FemaleAgent femaleAgent = cia.getFemaleAngets().get(agentId);

        int maxFavor = getMaxFavor(cia.getFemaleAngets().values());
        StaticAgent sAgent = StaticCiaDataMgr.getStaticAgentByQuality(femaleAgent, maxFavor);
        if (CheckNull.isNull(sAgent)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "特工未配置 roleId:", roleId, ", agentId:", agentId);
        }
//        if (femaleAgent.getExp() >= sAgent.getIntimacyVal()) {
//            throw new MwException(GameError.CIA_AGENT_IS_MAX_INTI_EXP.getCode(), "特工好感度已满 roleId:", roleId,
//                    ", agentId:", agentId, ", exp:", femaleAgent.getExp());
//        }
        //        refreshCntAndTime(cia);
        if (cia.getInteractionCnt() < count) {
            throw new MwException(GameError.CIA_INTERACTION_CNT_NOT_ENOUGH.getCode(), "互动次数不足 roleId:", roleId,
                    ", cnt:", cia.getInteractionCnt());
        }

        // 扣次数和设置时间
        int now = TimeHelper.getCurrentSecond();
        cia.setLastTime(now);
        cia.setInteractionCnt(cia.getInteractionCnt() - count);

        int val = femaleAgent.getExp(); //增加前经验
        int addExp = 0;
        if (type == CiaConstant.AGENT_INTERACTION_STATUS_1) {
            addExp = Constant.CIA_INTERACTION_WIN * count;
        } else {
            addExp = Constant.CIA_INTERACTION_FAIL * count;
        }
        addAgentExp(player, femaleAgent, addExp, sAgent);// 加亲密度
        cia.getFemaleAngets().put(agentId, femaleAgent);
        // 额外奖励
        //        if (RandomHelper.isHitRangeIn10000(Constant.CIA_INTERACT_REAWARD_PROBABILITY)) {
        //            List<Integer> awardList = RandomUtil.getRandomByWeight(Constant.CIA_INTERACT_AWARD, 3, false);
        //            if (!CheckNull.isEmpty(awardList)) {
        //                award = rewardDataManager.addAwardSignle(player, awardList, AwardFrom.CIA_INTERACTION_GIFT);
        //            }
        //        }
        LogLordHelper.commonLog("femaleAgentAddExp", AwardFrom.COMMON, player, femaleAgent.getId(),
                femaleAgent.getStar(), femaleAgent.getExp() - val,femaleAgent.getExp());
        taskDataManager.updTask(player, TaskType.COND_INTERACTION_AGENT, 1);
        // 累计互动次数
        taskDataManager.updTask(player, TaskType.COND_999, 1);

        InteractionRs.Builder builder = InteractionRs.newBuilder();
        builder.setInteractionCnt(cia.getInteractionCnt());
        //        if (cia.getInteractionCnt() < Constant.CIA_INTERACTION_MAX_CNT) {
        //            builder.setRefreshTime(cia.getLastTime() + Constant.CIA_INTERACTION_RECOVERY_CD);
        // player.removePushRecord(PushConstant.INTERACTION_CNT_FULL);// 移除推送状态
        //        }
        builder.setFemaleAgent(PbHelper.createFemaleAgent(femaleAgent));
        builder.setAddExp(femaleAgent.getExp() - val);

        builder.setAddExp(femaleAgent.getExp() - val);
        builder.setMaxFavorQuality(getMaxFavor(cia.getFemaleAngets().values()));
        //        if (award != null) {
        //            builder.addAward(award);
        //        }
        return builder.build();
    }

    public int getMaxFavor(Collection<FemaleAgent> femaleAgents) {
        int maxQuality = 5;
        Map<Integer, Integer> countMap = new HashMap<>();
        femaleAgents.forEach(femaleAgent -> {
            for (Integer lv : Constant.CIA_FAVORABILITY_QUALITY_LIST) {
                if (femaleAgent.getQuality() >= lv) {
                    int count = Optional.ofNullable(countMap.get(lv)).orElse(0);
                    countMap.put(lv, ++count);
                }
            }
        });

        if (countMap.isEmpty()) {
            return maxQuality;
        }

        for (List<Integer> list : Constant.CIA_FAVORABILITY_QUALITY) {
            int count = Optional.ofNullable(countMap.get(list.get(1))).orElse(0);
            if (count >= list.get(0)) {
                maxQuality = maxQuality < list.get(2) ? list.get(2) : maxQuality;
            }
        }

        return maxQuality;
    }

    /**
     * 检测特工是否存在和解锁状态
     *
     * @param player  玩家对象
     * @param agentId 特工id
     * @throws MwException 自定义异常
     */
    private void checkAgentExistAndUnlock(Player player, int agentId) throws MwException {
        Cia cia = player.getCia();
        long roleId = player.roleId;
        if (cia == null) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "情报部未解锁 roleId:", roleId);
        }

        FemaleAgent femaleAgent = cia.getFemaleAngets().get(agentId);
        if (femaleAgent == null) {
            throw new MwException(GameError.CIA_AGENT_NOT_HAVE.getCode(), "特工未获取 roleId:", roleId, ", agentId:",
                    agentId);
        }
        List<StaticAgent> agentConfs = StaticCiaDataMgr.getAgentConfById(agentId);
        if (CheckNull.isEmpty(agentConfs)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "特工未配置 roleId:", roleId, ", agentId:", agentId);
        }
        int status = femaleAgent.getStatus();
        if (CiaConstant.AGENT_UNLOCK_STATUS_2 != status) {
            throw new MwException(GameError.CIA_AGENT_NOT_UNLOCK.getCode(), "特工未解锁 roleId:", roleId, ", agentId:", agentId, ", status:", status);
        }
    }

    /**
     * 特工升级星级
     *
     * @param roleId
     * @param req
     * @return
     */
    public AgentUpgradeStarRs agentUpgradeStar(long roleId, AgentUpgradeStarRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int agentId = req.getId();
        checkAgentExistAndUnlock(player, agentId);
        Cia cia = player.getCia();
        FemaleAgent femaleAgent = cia.getFemaleAngets().get(agentId);
        int curStar = femaleAgent.getStar();
        int maxStar = getMaxStar(cia.getFemaleAngets().values());
        if (curStar >= maxStar) {
            throw new MwException(GameError.CIA_AGENT_IS_MAX_STAR.getCode(), "特工已经是最高星级:", roleId, ", agentId:", agentId,
                    ", star:", femaleAgent.getStar());
        }
        StaticAgentStar nextStarConf = StaticCiaDataMgr.getStaticAgentStar(agentId, curStar + 1);
        if (nextStarConf == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "特工下一档位星级未配置:", roleId, ", agentId:", agentId,
                    ", star:", femaleAgent.getStar());
        }
        // 扣除消耗
        List<List<Integer>> cost = nextStarConf.getCost();
        rewardDataManager.checkAndSubPlayerRes(player, cost, AwardFrom.AGENT_STAR_COST);

        femaleAgent.setStar(nextStarConf.getStar());
        cia.getFemaleAngets().put(agentId, femaleAgent);

        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_AGENT_STAR_CNT);
        LogLordHelper.commonLog("femaleAgentUp", AwardFrom.COMMON, player, femaleAgent.getId(),
                         femaleAgent.getStar(),femaleAgent.getExp());
        // 重新计算战斗力
        CalculateUtil.reCalcBattleHeroAttr(player);

        AgentUpgradeStarRs.Builder builder = AgentUpgradeStarRs.newBuilder();
        builder.setFemaleAgent(PbHelper.createFemaleAgent(femaleAgent));
        builder.setMaxStar(getMaxStar(cia.getFemaleAngets().values()));
        return builder.build();
    }

    /**
     * 获取可以升级到最大的星级数
     * @param femaleAgents
     * @return
     */
    private int getMaxStar(Collection<FemaleAgent> femaleAgents) {
        int maxStar = 5;
        Map<Integer, Integer> countMap = new HashMap<>();
        femaleAgents.forEach(femaleAgent -> {
            for (Integer lv : Constant.CIA_STAR_QUALITY_LIST) {
                if (femaleAgent.getStar() >= lv) {
                    int count = Optional.ofNullable(countMap.get(lv)).orElse(0);
                    countMap.put(lv, ++count);
                }
            }
        });

        if (countMap.isEmpty()) {
            return maxStar;
        }

        for (List<Integer> list : Constant.CIA_STAR_QUALITY) {
            int count = Optional.ofNullable(countMap.get(list.get(1))).orElse(0);
            if (count >= list.get(0)) {
                maxStar = maxStar < list.get(2) ? list.get(2) : maxStar;
            }
        }

        return maxStar;
    }

    /**
     * 检测特工升品质
     *
     * @param femaleAgent
     */
    @Deprecated
    private boolean checkAndUpFemaleAgent(Player player, FemaleAgent femaleAgent) {
        // StaticAgent sAgent = StaticCiaDataMgr.getAgentIdQualityByIdAndQuality(femaleAgent.getId(),
        //         femaleAgent.getQuality());
        // int intimacyVal = sAgent.getIntimacyVal();
        boolean isUp = false;
        // if (femaleAgent.getExp() >= intimacyVal) {
        //     // 升级操作
        //     StaticAgent sNextAgent = StaticCiaDataMgr.getAgentIdQualityByIdAndQuality(femaleAgent.getId(),
        //             femaleAgent.getQuality() + 1);
        //     if (sNextAgent != null) {
        //         femaleAgent.setQuality(sNextAgent.getQuality());
        //         int attrVal = RandomHelper.randomInArea(sNextAgent.getAttributeVal().get(0),
        //                 sNextAgent.getAttributeVal().get(1) + 1);
        //         femaleAgent.setAttrVal(attrVal);
        //         int skillVal = RandomHelper.randomInArea(sNextAgent.getSkillVal().get(0),
        //                 sNextAgent.getSkillVal().get(1));
        //         femaleAgent.setSkillVal(skillVal);
        //         femaleAgent.setExp(0);// 当前经验清0
        //         // 重新计算上阵将领属性
        //         CalculateUtil.reCalcBattleHeroAttr(player);
        //         LogLordHelper.commonLog("femaleAgentUp", AwardFrom.COMMON, player, femaleAgent.getId(),
        //                 femaleAgent.getQuality(), femaleAgent.getAttrVal(), femaleAgent.getSkillVal());
        //         // 提示亲密等级公告消息
        //         if (sNextAgent.getQuality() > 1) {
        //             chatDataManager.sendSysChat(ChatConst.CHAT_CIA_AGENT_UP, player.lord.getCamp(), 0,
        //                     player.lord.getNick(), String.valueOf(sNextAgent.getId()),
        //                     String.valueOf(sNextAgent.getIntimacyLv()));
        //         }
        //         isUp = true;
        //     }
        // }
        return isUp;
    }

    /**
     * gm加经验
     * @param player
     * @param femaleAgent
     * @param addExp
     */
    public void addGmAgentExp(Player player, FemaleAgent femaleAgent, int addExp) {
        int attrVal = femaleAgent.getAttrVal();
        int maxFavor = getMaxFavor(player.getCia().getFemaleAngets().values());
        StaticAgent sAgent = StaticCiaDataMgr.getStaticAgentByQuality(femaleAgent, maxFavor);

        if (femaleAgent.getExp() + addExp >= sAgent.getIntimacyVal()) {
            femaleAgent.setExp(sAgent.getIntimacyVal());
        } else {
            femaleAgent.setExp(femaleAgent.getExp() + addExp);
        }
        //刷新属性
        StaticAgent sNextAgent = StaticCiaDataMgr.getAgentConfByAgent(femaleAgent);
        if (sNextAgent != null) {
            femaleAgent.setAttrVal(sNextAgent.getAttributeVal());
            femaleAgent.setSkillVal(sNextAgent.getSkillVal());
            femaleAgent.setQuality(sNextAgent.getQuality());
            if (attrVal != femaleAgent.getAttrVal()) {
                // 重新计算上阵将领属性
                CalculateUtil.reCalcBattleHeroAttr(player);
            }
        }

        //貂蝉任务-佳人好感度
        ActivityDiaoChanService.completeTask(player,ETask.BEAUTY_INTIMACY);//,femaleAgent.getId(),femaleAgent.getExp()
        TaskService.processTask(player,ETask.BEAUTY_INTIMACY);
    }

    /**
     * 加经验
     *
     * @param player
     * @param femaleAgent
     * @param addExp
     */
    public void addAgentExp(Player player, FemaleAgent femaleAgent, int addExp, StaticAgent maxFavorAgent) {
        int attrVal = femaleAgent.getAttrVal();
        if (femaleAgent.getExp() + addExp >= maxFavorAgent.getIntimacyVal()) {
            femaleAgent.setExp(maxFavorAgent.getIntimacyVal());
        } else {
            femaleAgent.setExp(femaleAgent.getExp() + addExp);
        }
        //刷新属性
        StaticAgent sNextAgent = StaticCiaDataMgr.getAgentConfByAgent(femaleAgent);
        if (sNextAgent != null) {
            femaleAgent.setAttrVal(sNextAgent.getAttributeVal());
            femaleAgent.setSkillVal(sNextAgent.getSkillVal());
            femaleAgent.setQuality(sNextAgent.getQuality());
            if (attrVal != femaleAgent.getAttrVal()) {
                // 重新计算上阵将领属性
                CalculateUtil.reCalcBattleHeroAttr(player);
            }
        }

        //貂蝉任务-佳人好感度
        ActivityDiaoChanService.completeTask(player,ETask.BEAUTY_INTIMACY);//,femaleAgent.getId(),femaleAgent.getExp()
        TaskService.processTask(player,ETask.BEAUTY_INTIMACY);
        taskDataManager.updTask(player,TaskType.COND_515,1,femaleAgent.getExp());
    }

    /**
     * 送礼给特工
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public PresentGiftRs presentGift(long roleId, PresentGiftRq req) throws MwException {
        int agentId = req.getId();
        int giftId = req.getGiftId();
        int num = req.getNum() <= 0 ? 1 : req.getNum();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkAgentExistAndUnlock(player, agentId);
        Cia cia = player.getCia();
        StaticAgentGift sGift = StaticCiaDataMgr.getAgentGiftById(giftId);
        if (sGift == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "礼物未配置:", roleId, ", giftId:", giftId);
        }

        FemaleAgent femaleAgent = cia.getFemaleAngets().get(agentId);
        int maxFavor = getMaxFavor(cia.getFemaleAngets().values());
        StaticAgent sAgent = StaticCiaDataMgr.getStaticAgentByQuality(femaleAgent, maxFavor);
        if (femaleAgent.getExp() >= sAgent.getIntimacyVal()) {
            throw new MwException(GameError.CIA_AGENT_IS_MAX_INTI_EXP.getCode(), "特工好感度已满 roleId:", roleId,
                    ", agentId:", agentId, ", exp:", femaleAgent.getExp());
        }
        // if (femaleAgent.getQuality() >= StaticCiaDataMgr.MAX_QUALITY) {
        // throw new MwException(GameError.CIA_AGENT_IS_MAX_QUALITY.getCode(), "特工已经是最大品质 roleId:", roleId,
        // ", agentId:", agentId);
        // }
        // 使用道具
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, giftId, num, AwardFrom.CIA_AGENT_PRESENT_GIFT,
                false);

        int val = femaleAgent.getExp(); //增加前经验
        // 加经验
        addAgentExp(player, femaleAgent, sGift.getIntimacyValue() * num, sAgent);
        cia.getFemaleAngets().put(agentId, femaleAgent);
        LogLordHelper.commonLog("femaleAgentAddExp", AwardFrom.CIA_AGENT_PRESENT_GIFT, player, femaleAgent.getId(),
                femaleAgent.getStar(), femaleAgent.getExp() - val,femaleAgent.getExp());

        //貂蝉任务-佳人送礼
        ActivityDiaoChanService.completeTask(player, ETask.BEAUTY_GIFT,agentId,giftId,num);

        //喜悦金秋-日出而作- 佳人送礼
        TaskService.processTask(player, ETask.BEAUTY_GIFT,agentId,giftId,num);

        // 送礼排行
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PRESENT_GIFT_RANK, sGift.getIntimacyValue() * num);
        // 荣耀日报数据添加
        honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_15, sGift.getIntimacyValue() * num);
//        LogLordHelper.commonLog("presendGift", AwardFrom.CIA_AGENT_PRESENT_GIFT, player, agentId, giftId);
        taskDataManager.updTask(player, TaskType.COND_PRESENT_GIFT_AGENT, num);
        PresentGiftRs.Builder builder = PresentGiftRs.newBuilder();
        builder.setFemaleAgent(PbHelper.createFemaleAgent(femaleAgent));
        builder.setAddExp(femaleAgent.getExp() - val);
        builder.setMaxFavorQuality(getMaxFavor(cia.getFemaleAngets().values()));
        Prop prop = player.props.get(giftId);
        if (prop != null) {
            builder.setProp(PbHelper.createPropPb(prop));
        }

        return builder.build();
    }

    /**
     * 获取特技能加成值
     *
     * @param player
     * @param skillId
     * @return
     */
    public int getAgentSkillVal(Player player, final int skillId) {
        Cia cia = player.getCia();
        if (cia == null) {
            return 0;
        }
        int val = 0;
        for (FemaleAgent fa : cia.getFemaleAngets().values()) {
            if (fa.getQuality() > 0) {
                StaticAgent sAgent = StaticCiaDataMgr.getAgentConfByAgent(fa);
                if (sAgent != null && sAgent.getSkillId() == skillId) {
                    val += fa.getSkillVal();
                }
            }
        }
        return val;
    }

    /**
     * 同步弹幕
     *
     * @param barrage
     */
    @Deprecated
    private void syncBarrage(Barrage barrage) {
        SyncBarrageRs.Builder syncBarrageRs = SyncBarrageRs.newBuilder();
        syncBarrageRs.setBarrage(barrage);
        Base.Builder builder = PbHelper.createSynBase(SyncBarrageRs.EXT_FIELD_NUMBER, SyncBarrageRs.ext,
                syncBarrageRs.build());
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> {
            if (p.ctx != null) {
                MsgDataManager.getIns().add(new Msg(p.ctx, builder.build(), p.roleId));
            }
        });
    }

    /**
     * 约会
     *
     * @param roleId 角色id
     * @param req    请求参数
     * @return 约会后Agent的信息
     * @throws MwException 自定义异常
     */
    public AppointmentAgentRs appointmentAgent(long roleId, AppointmentAgentRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int agentId = req.getId(); //特工id
        int type = req.getType(); // 1 约会 2 每日点击提升好感度
        // 检测特工是否存在和解锁状态
        checkAgentExistAndUnlock(player, agentId);

        Cia cia = player.getCia();
        FemaleAgent femaleAgent = cia.getFemaleAngets().get(agentId);
        AppointmentAgentRs.Builder builder = AppointmentAgentRs.newBuilder();
        // 需要添加的好感度
        AtomicInteger aiExp = new AtomicInteger();
        //减次数和设置时间
        int now = TimeHelper.getCurrentSecond();
        if (1 == type) {
            if (femaleAgent.getAppointmentCnt() <= 0) {
                throw new MwException(GameError.CIA_APPOINTMENT_CNT_NOT_ENOUGH.getCode(), "特工约会次数不足 roleId:", roleId,
                        ", agentId:", agentId, ", appointmentCnt:", femaleAgent.getAppointmentCnt());
            }
            StaticAgentStar sAgentStar = StaticCiaDataMgr.getStaticAgentStar(agentId, femaleAgent.getStar());
            if (sAgentStar == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "特工对应星级未配置:", roleId, ", agentId:", agentId,
                        ", star:", femaleAgent.getQuality());
            }
            //固定奖励
            Award fixAward;
            List<List<Integer>> fixedAward = sAgentStar.getFixedAward();
            if (CheckNull.isNull(fixedAward)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "特工约会固定奖励未配置:", roleId, ", agentId:", agentId,
                        ", star:", femaleAgent.getQuality());
            }
            //随机奖励
            Award rAward;
            List<List<Integer>> randomAward = sAgentStar.getRandomAward();
            if (CheckNull.isNull(randomAward)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "特工约会随机奖励未配置:", roleId, ", agentId:", agentId,
                        ", star:", femaleAgent.getQuality());
            }
            for (List<Integer> faw : fixedAward) {
                fixAward = rewardDataManager.addAwardSignle(player, faw, AwardFrom.CIA_INTERACTION_GIFT);
                if (fixAward != null) {
                    builder.addAward(fixAward);
                }
            }
            //随机奖励，不能重复
            List<List<Integer>> awardList = RandomUtil.getListRandomWeight(randomAward, sAgentStar.getRandomCount(), 3);
            if (!CheckNull.isEmpty(awardList)) {
                for (List<Integer> award : awardList) {
                    rAward = rewardDataManager.addAwardSignle(player, award, AwardFrom.CIA_INTERACTION_GIFT);
                    if (rAward != null) {
                        builder.addAward(rAward);
                    }
                }
            }
            // 更新任务进度
            taskDataManager.updTask(player, TaskType.COND_APPOINTMENT_AGENT, 1);
            // 扣次数
            femaleAgent.setAppointmentCnt(femaleAgent.getAppointmentCnt() - 1);
            // 设置需要加的经验值
            aiExp.set(Constant.CIA_APPOINTMENT_CRIT);

            //貂蝉任务-约会
            ActivityDiaoChanService.completeTask(player,ETask.APPOINTMENT,agentId);
            TaskService.processTask(player,ETask.APPOINTMENT,agentId);
            //称号-约会
            titleService.processTask(player,ETask.APPOINTMENT);

        } else if (2 == type) {
            int dailyFree = femaleAgent.getDailyFree();
            StaticAgent sAgent = StaticCiaDataMgr.getAgentConfByAgent(femaleAgent);
            if (sAgent == null) {
                List<StaticAgent> sAgents = StaticCiaDataMgr.getAgentConfById(femaleAgent.getId());
                sAgent = !CheckNull.isEmpty(sAgents) ? sAgents.get(0) : null;
            }
            if (sAgent == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "特工对应未配置:", roleId, ", agentId:", agentId, ", IntimacyVal:", femaleAgent.getExp());
            }
            if (dailyFree >= sAgent.getDailyFree()) {
                throw new MwException(GameError.CIA_DAILY_FREE_NOT_ENOUGH.getCode(), "特工每日提升好感度次数不足, roleId: ", roleId, ", agentId:", agentId, ", dailyFree:", dailyFree);
            }
            // 加次数
            femaleAgent.setDailyFree(++dailyFree);
            // 设置需要加的经验值
            aiExp.set(sAgent.getAddIntimacy());
        }
        int addExp = aiExp.get();
        int val = femaleAgent.getExp(); //增加前经验
        if (addExp >= 0) {
            // 加经验
            int maxFavor = getMaxFavor(cia.getFemaleAngets().values());
            StaticAgent sAgent = StaticCiaDataMgr.getStaticAgentByQuality(femaleAgent, maxFavor);
            addAgentExp(player, femaleAgent, addExp, sAgent);
            cia.getFemaleAngets().put(agentId, femaleAgent);
            // 最后修改时间
            cia.setLastTime(now);
            // 日志
            LogLordHelper.commonLog("femaleAgentAddExp", AwardFrom.CIA_INTERACTION_GIFT, player, femaleAgent.getId(),
                    femaleAgent.getStar(), femaleAgent.getExp() - val,femaleAgent.getExp());
        }

        builder.setMaxFavorQuality(getMaxFavor(cia.getFemaleAngets().values()));
        builder.setAddExp(femaleAgent.getExp() - val);
        builder.setFemaleAgent(PbHelper.createFemaleAgent(femaleAgent));
        return builder.build();
    }

}
