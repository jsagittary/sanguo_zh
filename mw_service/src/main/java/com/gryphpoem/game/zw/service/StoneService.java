package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.StoneInfo;
import com.gryphpoem.game.zw.resource.domain.s.StaticStone;
import com.gryphpoem.game.zw.resource.domain.s.StaticStoneHole;
import com.gryphpoem.game.zw.resource.domain.s.StaticStoneImprove;
import com.gryphpoem.game.zw.resource.pojo.Stone;
import com.gryphpoem.game.zw.resource.pojo.StoneHole;
import com.gryphpoem.game.zw.resource.pojo.StoneImprove;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

/**
 * @ClassName StoneService.java
 * @Description 宝石
 * @author QiuKun
 * @date 2018年5月8日
 */
@Service
public class StoneService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private ActivityRobinHoodService activityRobinHoodService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private ActivityTriggerService activityTriggerService;

    /**
     * 获取宝石相关信息
     * 
     * @param roleId
     * @return
     */
    public GetStoneInfoRs getStoneInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetStoneInfoRs.Builder builder = GetStoneInfoRs.newBuilder();
        for (Stone s : player.getStoneInfo().getStones().values()) {
            builder.addStone(PbHelper.createStonePb(s));
        }
        for (StoneHole s : player.getStoneInfo().getStoneHoles().values()) {
            builder.addStoneHole(PbHelper.createStoneHolePb(s));
        }
        for (StoneImprove s : player.getStoneInfo().getStoneImproves().values()) {
            builder.addStoneImprove(s.ser());
        }
        return builder.build();
    }

    /**
     * 宝石镶嵌
     * 
     * @param roleId
     * @param req
     * @return
     */
    public StoneMountingRs stoneMounting(long roleId, StoneMountingRq req) throws MwException {
        int hole = req.getHole();
        final int stoneId = req.getStoneId();
        int type = req.hasType() ? req.getType() : 0; // 0 镶嵌宝石 , 1 镶嵌进阶后宝石
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        for (Hero hero : player.getAllOnBattleHeros()) {
            // 非空闲状态
            if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
                        hero.getHeroId(), ", state:", hero.getState());
            }
        }

        // 孔位检测
        StaticStoneHole sStoneHole = checkStoneHoleIsOpen(player, hole);
        Map<Integer, StoneHole> stoneHoles = player.getStoneInfo().getStoneHoles();
        StoneHole stoneHole = stoneHoles.get(hole);
        StoneInfo stoneInfo = player.getStoneInfo();
        if (stoneHole == null) { // 初始化孔位
            stoneHole = new StoneHole(hole);
            stoneHoles.put(hole, stoneHole);
        }
        // 之前宝石id
        int preStoneId = stoneHole.getStoneId();
        int preType = stoneHole.getType();// 之前镶嵌宝石的类型

        StoneMountingRs.Builder builder = StoneMountingRs.newBuilder();
        if (type == 0) {// 镶嵌的是宝石
            StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
            if (sStone == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",没有此宝石 stoneId:", stoneId);
            }
            if (sStone.getType() != sStoneHole.getStoneType()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",宝石类型和孔的类型不符 stoneType:",
                        sStone.getType(), ", holeType:", sStoneHole.getStoneType());
            }
            // 等级检测
            int roleLv = player.lord.getLevel();
            if (roleLv < sStone.getNeedRoleLv()) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "roleId:", roleId, ", 玩家等级不够 roleLv:", roleLv,
                        ", stoneNeedLv:", sStone.getNeedRoleLv());
            }
            // 宝石数量检查
            rewardDataManager.checkStoneIsEnought(player, stoneId, 1);
            rewardDataManager.subStone(player, stoneId, 1, AwardFrom.MOUNTING_STONE);
            // 镶嵌操作
            stoneHole.setStoneId(stoneId);
            stoneHole.setType(StoneHole.TYPE_STONE);
            builder.addStone(getPlayerStoneById(player, stoneId));
        } else if (type == 1) {// 镶嵌的是进阶后的宝石
            StoneImprove stoneImprove = stoneInfo.getStoneImproves().get(stoneId);
            if (stoneImprove == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",未拥有该进阶宝石 keyId:", stoneId);
            }
            StaticStoneImprove sSi = StaticPropDataMgr.getStoneImproveById(stoneImprove.getStoneImproveId());
            if (sSi == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ",未找到宝石进阶配置 StoneImproveId:",
                        stoneImprove.getStoneImproveId());
            }
            if (sSi.getType() != sStoneHole.getStoneType()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",进阶的宝石类型和孔的类型不符 stoneType:",
                        sSi.getType(), ", holeType:", sStoneHole.getStoneType());
            }
            if (stoneImprove.getHoleIndex() != 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",此宝石已经穿戴了 keyId:",
                        stoneImprove.getKeyId(), ", holeIndex:", stoneImprove.getHoleIndex());
            }
            for (StoneHole sh : stoneHoles.values()) {
                if(sh.getType() == StoneHole.TYPE_STONE_IMPROVE && sh.getStoneId()==stoneId){
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",此宝石已经穿戴了 keyId:",
                            stoneImprove.getKeyId(), ", holeIndex:", sh.getHoleIndex());
                }
            }
            // 镶嵌操作
            stoneImprove.setHoleIndex(hole);
            stoneHole.setStoneId(stoneId);
            stoneHole.setType(StoneHole.TYPE_STONE_IMPROVE);
            builder.addStoneImprove(stoneImprove.ser());
        } else {// 错误参数
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",镶嵌类型错误 type:", type);
        }
        builder.setStoneHole(PbHelper.createStoneHolePb(stoneHole));

        // 卸下宝石操作
        if (preStoneId != 0) {
            if (preType == StoneHole.TYPE_STONE) { // 返还孔位上的宝石
                rewardDataManager.addAward(player, AwardType.STONE, preStoneId, 1, AwardFrom.MOUNTING_STONE);
                builder.addStone(getPlayerStoneById(player, preStoneId)); // 卸下的宝石
            } else if (preType == StoneHole.TYPE_STONE_IMPROVE) {
                // 卸下进阶后的宝石
                StoneImprove preStoneImprove = stoneInfo.getStoneImproves().get(preStoneId);
                if (preStoneImprove != null) {
                    preStoneImprove.setHoleIndex(0);
                    builder.addStoneImprove(preStoneImprove.ser());
                }
            }
        }

        LogLordHelper.stoneHole(AwardFrom.COMPOUND_STONE, player.account, player.lord, stoneHole.getHoleIndex(),
                stoneHole.getStoneId(), stoneHole.getType());
        // 重新计算战斗力
        CalculateUtil.reCalcAllHeroAttr(player);
        taskDataManager.updTask(player, TaskType.COND_STONE_HOLE_49, 1);
        return builder.build();
    }

    private CommonPb.Stone getPlayerStoneById(Player player, int stoneId) {
        Stone stone = player.getStoneInfo().getStones().get(stoneId);
        if (stone == null) {
            stone = new Stone(stoneId);
            stone.setCnt(0);
        }
        return PbHelper.createStonePb(stone);
    }

    /**
     * 检查孔位是否开启
     * 
     * @param player
     * @param hole
     * @return
     * @throws MwException
     */
    private StaticStoneHole checkStoneHoleIsOpen(Player player, int hole) throws MwException {
        int roleLv = player.lord.getLevel();
        long roleId = player.roleId;
        StaticStoneHole sStoneHole = StaticPropDataMgr.getStoneHoleByIndex(hole);
        if (sStoneHole == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",没有此孔位 hole:", hole);
        }
        if (roleLv < sStoneHole.getNeedRoleLv()) {
            throw new MwException(GameError.STONEHOLE_NOT_OPEN.getCode(), "roleId:", roleId, ",孔位没有开启 hole:", hole,
                    "roleLv:", roleLv, ", holeNeedLv:", sStoneHole.getNeedRoleLv());
        }
        return sStoneHole;
    }

    /**
     * 宝石合成升级
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public StoneUpLvRs stoneUpLv(long roleId, StoneUpLvRq req) throws MwException {
        int type = req.getType();
        int param = req.getParam();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StoneUpLvRs.Builder builder = StoneUpLvRs.newBuilder();
        int roleLv = player.lord.getLevel();
        Set<Integer> syncStonIds = new HashSet<>();
        if (1 == type) { // 在孔位上对宝石合成
            int hole = param;
            checkStoneHoleIsOpen(player, hole);
            StoneHole stoneHole = player.getStoneInfo().getStoneHoles().get(hole);
            if (stoneHole == null || stoneHole.getStoneId() == 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",此孔位没有宝石不能 进行升级");
            }
            int stoneId = stoneHole.getStoneId();
            StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
            if (sStone == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ",孔位宝石配置找不到 stoneId:", stoneId);
            }
            StaticStone nextLvStone = StaticPropDataMgr.getStoneByTypeAndLv(sStone.getType(), sStone.getLv() + 1);
            if (nextLvStone == null) {
                throw new MwException(GameError.STONEHOLE_IS_MAX_LV.getCode(), "roleId:", roleId,
                        ",宝石已经达到最大等级 stoneId:", stoneId);
            }
            if (roleLv < nextLvStone.getLv()) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "roleId:", roleId, ", 玩家等级不够 roleLv:", roleLv,
                        ", stoneNeedLv:", sStone.getNeedRoleLv());
            }
            // autoUpStone(player, nextLvStone, stoneHole, syncStonIds);
            upStone(player, nextLvStone, stoneHole, syncStonIds);
            CalculateUtil.reCalcAllHeroAttr(player); // 重新计算战斗力
            builder.setStoneHole(PbHelper.createStoneHolePb(stoneHole));

            //貂蝉任务-配饰合成
            ActivityDiaoChanService.completeTask(player, ETask.ORNAMENT_COUNT);
            TaskService.processTask(player, ETask.ORNAMENT_COUNT);
        } else if (2 == type || 3 == type) {// 在背包里面合成 2 单个合成 , 3 合成全部
            // 检测宝石够不够
            int stoneId = param;
            StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
            if (sStone == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ",宝石参数错误 stoneId:", stoneId);
            }
            StaticStone nextLvStone = StaticPropDataMgr.getStoneByTypeAndLv(sStone.getType(), sStone.getLv() + 1);
            if (nextLvStone == null) {
                throw new MwException(GameError.STONEHOLE_IS_MAX_LV.getCode(), "roleId:", roleId,
                        ",宝石已经达到最大等级 stoneId:", stoneId);
            }
            // 检测石头是否够 合成一个
            rewardDataManager.checkStoneIsEnought(player, stoneId, nextLvStone.getNeedNumber());
            int hasCnt = (int) rewardDataManager.getRoleResByType(player, AwardType.STONE, stoneId);
            int costCnt = 0; // 消耗当前等级宝石个数
            int obtainCnt = 0; // 获得下一等级宝石个数
            if (2 == type) {
                costCnt = nextLvStone.getNeedNumber();
                obtainCnt = 1;
            } else if (3 == type) {
                obtainCnt = hasCnt / nextLvStone.getNeedNumber();
                costCnt = nextLvStone.getNeedNumber() * obtainCnt;
            }
            rewardDataManager.subStone(player, stoneId, costCnt, AwardFrom.COMPOUND_STONE_IN_BAG);
            rewardDataManager.addAward(player, AwardType.STONE, nextLvStone.getId(), obtainCnt,
                    AwardFrom.COMPOUND_STONE_IN_BAG);
            // 任务进度更新
            taskDataManager.updTask(player, TaskType.COND_STONE_COMPOUND_COMBAT_48, obtainCnt);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMPOUND_COMBAT_48, obtainCnt);
            activityRobinHoodService.updateTaskSchedule(player, ActivityConst.COND_STONE_COMPOUND_COMBAT, obtainCnt);
            syncStonIds.add(stoneId);
            syncStonIds.add(nextLvStone.getId());
            //喜悦金秋-日出而作-合成x个任意等级配饰
            TaskService.processTask(player, ETask.ORNAMENT_COUNT, obtainCnt);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", 宝石合成参数错误 type:", type);
        }

        for (int stoneId : syncStonIds) {
            builder.addStone(getPlayerStoneById(player, stoneId));
        }

        return builder.build();
    }

    /**
     * 对宝石进阶
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DoStoneImproveRs doStoneImprove(long roleId, DoStoneImproveRq req) throws MwException {
        // 宝石进阶 功能开发开关
        int holeIndex = req.getHoleIndex();
        int stoneId = req.getStoneId();
        int stoneImproveId = req.getStoneImproveId();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.STONE_IMPROVE_FUNCTION)) {
            throw new MwException(GameError.FUNCTION_UNLOCK_NO_CONFIG.getCode(), "roleId:", roleId, "进阶宝石功能未开启");
        }
        // 宝石信息
        StoneInfo stoneInfo = player.getStoneInfo();

        DoStoneImproveRs.Builder builder = DoStoneImproveRs.newBuilder();

        if (stoneId != 0) {
            // 普通宝石进阶
            StaticStone sStone = StaticPropDataMgr.getStoneMapById(stoneId);
            if (sStone == null || !sStone.isCanImprove()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "进阶宝石Id不存在,或宝石不能进阶 stoneId:",
                        stoneId);
            }
            StaticStoneImprove sStoneImprove = StaticPropDataMgr.getStoneImproveById(sStone.getStoneImproveId());
            if (sStoneImprove == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "进阶宝石Id配置填写错误 stoneId:", stoneId);
            }
            StoneHole stoneHole = null;
            if (holeIndex != 0) { // 在孔位上进行
                stoneHole = stoneInfo.getStoneHoles().get(holeIndex);
                if (stoneHole == null) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "找不到此孔 holeIndex:",
                            holeIndex);
                }
                if (stoneHole.getStoneId() == 0) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, "此孔没有安装任何宝石 holeIndex:",
                            holeIndex);
                }
                if (stoneHole.getType() != StoneHole.TYPE_STONE || stoneHole.getStoneId() != stoneId) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId,
                            "孔内宝石与传过来进阶的宝石id不一致 holeIndex:", holeIndex);
                }
            }
            // 扣资源
            rewardDataManager.checkAndSubPlayerRes(player, Constant.STONE_IMPROVE_NEED_COST, AwardFrom.DO_STONE_IMPROVE);
            if (holeIndex == 0) {// 背包合成需要扣宝石
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.STONE, stoneId, 1, AwardFrom.DO_STONE_IMPROVE,
                        true);
            }
            // 给奖励
            int keyId = rewardDataManager.addStoneImprove(player, sStoneImprove.getId(), AwardFrom.DO_STONE_IMPROVE);
            StoneImprove stoneImprove = stoneInfo.getStoneImproves().get(keyId);
            if (stoneImprove == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "进阶宝石配置错误 keyId", keyId);
            }
            // 发送跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_DO_STONE_IMPROVE, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), sStoneImprove.getId());
            if (holeIndex != 0) {
                stoneImprove.setHoleIndex(holeIndex);
                stoneHole.setType(StoneHole.TYPE_STONE_IMPROVE);
                stoneHole.setStoneId(stoneImprove.getKeyId());
                builder.setStoneHole(PbHelper.createStoneHolePb(stoneHole));
                // 重新计算战斗力
                CalculateUtil.reCalcAllHeroAttr(player);
            }
            builder.setStoneImprove(stoneImprove.ser());
        } else if (stoneImproveId != 0) {
            // 进阶后的宝石突破
            StoneImprove stoneImprove = stoneInfo.getStoneImproves().get(stoneImproveId);
            if (Objects.isNull(stoneImprove)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, " 进阶宝石突破,未拥有此宝石 keyId:", stoneImproveId);
            }
            if (stoneImprove.isBreakThrough()) {
                throw new MwException(GameError.STONE_ALREADY_IMPROVES.getCode(), "roleId:", roleId, "宝石已经突破过了, keyId", stoneImproveId);
            }
            int improveId = stoneImprove.getStoneImproveId();
            StaticStoneImprove sStoneImprove = StaticPropDataMgr.getStoneImproveById(improveId);
            if (sStoneImprove == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, "进阶宝石Id配置填写错误 improveId:", improveId);
            }
            List<List<Integer>> breakThrough = sStoneImprove.getBreakThrough();
            if (CheckNull.isEmpty(breakThrough)) {
                throw new MwException(GameError.STONE_CANNOT_IMPROVES.getCode(), String.format("宝石不可以突破, role_id: %s, improveId: %s", roleId, improveId));
            }
            // 扣资源
            rewardDataManager.checkAndSubPlayerRes(player, sStoneImprove.getBreakThrough(), AwardFrom.DO_STONE_IMPROVE);
            stoneImprove.setBreakThrough(true);
            builder.setStoneImprove(stoneImprove.ser());
        }
        return builder.build();
    }

    /**
     * 进阶宝石升星
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public StoneImproveUpLvRs stoneImproveUpLv(long roleId, StoneImproveUpLvRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.STONE_IMPROVE_FUNCTION)) {
            throw new MwException(GameError.FUNCTION_UNLOCK_NO_CONFIG.getCode(), "roleId:", roleId, "进阶宝石功能未开启");
        }
        int keyId = req.getKeyId();
        List<CommonPb.Stone> costStoneList = req.getCostList();

        StoneImprove stoneImprove = player.getStoneInfo().getStoneImproves().get(keyId);
        if (stoneImprove == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, " 进阶宝石升星,未拥有此宝石 keyId:", keyId);
        }
        if (costStoneList.isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, " 消耗的宝石为空");
        }
        StaticStoneImprove sStoneIm = StaticPropDataMgr.getStoneImproveById(stoneImprove.getStoneImproveId());
        if (sStoneIm == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, " 未找到进阶宝石配置配 StoneImproveId:",
                    stoneImprove.getStoneImproveId());
        }
        if (sStoneIm.getNeedExp() == 0) { // 说明已经满级
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, " 该进阶宝石已经满星了");
        }
        if (!CheckNull.isEmpty(sStoneIm.getBreakThrough()) && !stoneImprove.isBreakThrough()) {
            throw new MwException(GameError.STONE_NEED_IMPROVES.getCode(), String.format("宝石需要突破才能升星, role_id: %s, stone_improve_id: %s", roleId, stoneImprove.getStoneImproveId()));
        }
        // 检测是否拥有这些宝石
        List<List<Integer>> costStone = mergeCostStone(costStoneList);
        // 检查是否足够
        rewardDataManager.checkPlayerResIsEnough(player, costStone);
        // 计算经验值
        int addExp = 0;
        for (CommonPb.Stone st : costStoneList) {
            StaticStone sStone = StaticPropDataMgr.getStoneMapById(st.getStoneId());
            addExp += (sStone.getNeedLv1Number() * st.getCnt());
        }
        // 大于本次升级需要的经验
        int addAfter = stoneImprove.getExp() + addExp;
        if (addAfter > sStoneIm.getNeedExp()) {
//            if (addAfter > (sStoneIm.getNeedExp() * (1 + (Constant.STONE_IMPROVE_UP_MAX_CONF / Constant.TEN_THROUSAND)))) {
//                // 大于百分之二十, 就不让升星了
//                throw new MwException(GameError.STONE_UP_MAX_ONCE.getCode(), String.format("超出单次吃经验书的上限, role_id: %s, stone_improve_id: %s, current_exp: %s, add_exp: %s", roleId, stoneImprove.getStoneImproveId(), stoneImprove.getExp(), addExp));
//            }
            // 丢失的经验
            int lostExp = addAfter - sStoneIm.getNeedExp();
            LogUtil.common("宝石升星, 丢失经验值, role_id: %s, lost_exp: %s", roleId, lostExp);
            addExp = sStoneIm.getNeedExp() - stoneImprove.getExp();
        }
        LogUtil.debug("宝石升星加的经验值 roleId:", roleId, ", addExp:", addExp);
        // 扣除玩家升星消耗
        rewardDataManager.checkAndSubPlayerRes(player, costStone, AwardFrom.STONE_IMPROVE_UP_LV);
        //打印日志
        LogLordHelper.stoneLvUp(AwardFrom.STONE_IMPROVE_UP_LV, player.account, player.lord,keyId, stoneImprove.getStoneImproveId(),stoneImprove.getExp(),addExp);
        // 加经验
        addStoneImproveExp(player, stoneImprove, addExp);
        StoneImproveUpLvRs.Builder builder = StoneImproveUpLvRs.newBuilder();
        builder.setStoneImprove(stoneImprove.ser());
        return builder.build();
    }

    /**
     * 升星
     *
     * @param stoneImprove
     * @param addExp
     */
    private void addStoneImproveExp(Player player, StoneImprove stoneImprove, int addExp) {
        int exp = addExp;
        boolean isUp = false;
        while (exp > 0) {
            StaticStoneImprove sStoneIm = StaticPropDataMgr.getStoneImproveById(stoneImprove.getStoneImproveId());
            if (sStoneIm == null || sStoneIm.getNeedExp() == 0) break;
            int needExp = sStoneIm.getNeedExp();
            if (stoneImprove.getExp() + exp >= needExp) {
                int incrExp = needExp - stoneImprove.getExp();
                if (incrExp < 0) {
                    incrExp = exp;
                }
                StaticStoneImprove nextsStoneIm = StaticPropDataMgr.getStoneImproveByTypeLv(sStoneIm.getType(),
                        sStoneIm.getLv() + 1);
                if (nextsStoneIm == null) break;
                stoneImprove.setStoneImproveId(nextsStoneIm.getId());
                if (nextsStoneIm.getLv() == 10) {
                    // 升级到10星的时候, 发送跑马灯
                    chatDataManager.sendSysChat(ChatConst.CHAT_STONE_IMPROVE_UP_LV, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), nextsStoneIm.getId());
                }
                stoneImprove.setExp(0);
                exp -= incrExp;
                isUp = true;
                activityTriggerService.stoneImproveUpStar(player, nextsStoneIm.getLv());
            } else {
                stoneImprove.setExp(stoneImprove.getExp() + exp);
                exp -= exp;
            }
        }
        if (isUp) { // 有升级
            stoneImprove.setBreakThrough(false);
            if (stoneImprove.getHoleIndex() > 0) {
                // 重新计算战斗力
                CalculateUtil.reCalcAllHeroAttr(player);
            }
        }
    }

    /**
     * 合并宝石
     * 
     * @param costStoneList
     * @return key:stoneId value:cnt
     */
    private List<List<Integer>> mergeCostStone(List<CommonPb.Stone> costStoneList) {
        List<List<Integer>> awardList = new ArrayList<>();
        Map<Integer, Integer> costStone = new HashMap<>();
        for (CommonPb.Stone st : costStoneList) {
            int oldCnt = costStone.getOrDefault(st.getStoneId(), 0);
            int newCnt = oldCnt + st.getCnt();
            costStone.put(st.getStoneId(), newCnt);
        }
        for (Entry<Integer, Integer> kv : costStone.entrySet()) {
            Integer cnt = kv.getValue();
            Integer stoneId = kv.getKey();
            List<Integer> award = new ArrayList<>(3);
            award.add(AwardType.STONE);
            award.add(stoneId);
            award.add(cnt);
            awardList.add(award);
        }
        return awardList;
    }

    /**
     * 单个等级升
     * 
     * @param player
     * @param nextLvStone
     * @param stoneHole
     * @param syncStonIds
     * @throws MwException
     */
    private void upStone(Player player, StaticStone nextLvStone, StoneHole stoneHole, Set<Integer> syncStonIds)
            throws MwException {
        int curStoneId = stoneHole.getStoneId();
        int needNumber = nextLvStone.getNeedNumber();
        Map<Integer, Stone> stones = player.getStoneInfo().getStones();
        Stone bagStone = stones.get(curStoneId);
        int hasCnt = bagStone == null ? 0 : bagStone.getCnt();
        if (hasCnt + 1 < needNumber) {
            throw new MwException(GameError.STONE_NOT_ENOUGH.getCode(), " 宝石数量不足, roleId:", player.roleId);
        }
        syncStonIds.add(curStoneId);
        rewardDataManager.subStone(player, curStoneId, needNumber - 1, AwardFrom.COMPOUND_STONE);
        // 升级成功
        stoneHole.setStoneId(nextLvStone.getId());
        taskDataManager.updTask(player, TaskType.COND_STONE_COMPOUND_COMBAT_48, 1);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMPOUND_COMBAT_48, 1);
        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.COND_STONE_COMPOUND_COMBAT, 1);
        // activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_STONE_CNT, nextLvStone.getLv());
        // activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_STONE_CNT,1 , nextLvStone.getLv());
        activityTriggerService.stoneUpLv(player, nextLvStone.getLv());
    }

    /**
     * 自动升级
     * @param player
     * @param nextLvStone
     * @param stoneHole
     * @param syncStonIds
     * @throws MwException
     */
    private void autoUpStone(Player player, StaticStone nextLvStone, StoneHole stoneHole, Set<Integer> syncStonIds)
            throws MwException {
        Map<Integer, Integer> needCost = calcNeedCostStone(player, nextLvStone, stoneHole.getStoneId());
        if (needCost == null) {
            throw new MwException(GameError.STONE_NOT_ENOUGH.getCode(), " 宝石数量不足, roleId:", player.roleId);
        }
        int compoundCnt = 0;
        while (nextLvStone != null && player.lord.getLevel() >= nextLvStone.getLv() && needCost != null) { // 升级到不能升
            syncStonIds.addAll(needCost.keySet());
            // 扣宝石
            needCost.forEach((stoneId, cnt) -> {
                try {
                    rewardDataManager.subStone(player, stoneId, cnt, AwardFrom.COMPOUND_STONE);
                } catch (MwException e) {
                    e.printStackTrace();
                }
            });
            compoundCnt += calcCompoundCnt(needCost, nextLvStone, stoneHole.getStoneId());
            // 升级成功
            stoneHole.setStoneId(nextLvStone.getId());
            LogLordHelper.stoneHole(AwardFrom.COMPOUND_STONE, player.account, player.lord, stoneHole.getHoleIndex(),
                    stoneHole.getStoneId(), stoneHole.getType());
            needCost = calcNeedCostStone(player, nextLvStone, stoneHole.getStoneId());
            nextLvStone = StaticPropDataMgr.getStoneByTypeAndLv(nextLvStone.getType(), nextLvStone.getLv() + 1);
        }
        taskDataManager.updTask(player, TaskType.COND_STONE_COMPOUND_COMBAT_48, compoundCnt);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMPOUND_COMBAT_48, compoundCnt);
        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.COND_STONE_COMPOUND_COMBAT, compoundCnt);
    }

    /**
     * 计算合成的次数
     * 
     * @param needCost
     * @return
     */
    private int calcCompoundCnt(Map<Integer, Integer> needCost, StaticStone nextLvStone, int holeonStoneId) {
        return 1;
    }

    /**
     * 计算需要消耗的宝石的数量
     * 
     * @param player
     * @param sStone 需要升级的到的等级
     * @param holeonStoneId 孔上面宝石的id
     * @return 返回null 宝石数量不够
     */
    private Map<Integer, Integer> calcNeedCostStone(Player player, StaticStone sStone, int holeonStoneId) {
        Map<Integer, Integer> needCost = new HashMap<>(); // key stoneId,value cnt
        int lv = sStone.getLv();
        int needSumExp = sStone.getNeedLv1Number();
        int stoneType = sStone.getType();
        Map<Integer, Stone> stones = player.getStoneInfo().getStones();
        while (--lv >= 1 && needSumExp > 0) {
            StaticStone curSstone = StaticPropDataMgr.getStoneByTypeAndLv(stoneType, lv);
            if (curSstone == null) continue;
            Stone bagStone = stones.get(curSstone.getId());
            int hasCnt = bagStone == null ? 0 : bagStone.getCnt();
            hasCnt = holeonStoneId == curSstone.getId() ? hasCnt += 1 : hasCnt; // 需要加上孔上的宝石进行计算
            if (hasCnt == 0) continue;
            int hasExp = hasCnt * curSstone.getNeedLv1Number();
            if (hasExp >= needSumExp) {
                int needCnt = needSumExp / curSstone.getNeedLv1Number();
                needCost.put(curSstone.getId(), holeonStoneId == curSstone.getId() ? needCnt -= 1 : needCnt); // 需要减去孔上的宝石,在外面就不用扣除
                return needCost;
            } else {
                needSumExp -= hasExp;
                needCost.put(curSstone.getId(), holeonStoneId == curSstone.getId() ? hasCnt -= 1 : hasCnt);
            }
        }
        return null;
    }

}
