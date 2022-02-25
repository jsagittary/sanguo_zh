package com.gryphpoem.game.zw.robot;

import com.hundredcent.game.ai.btree.HaiBtreeManager;
import com.hundredcent.game.ai.btree.base.IHaiNode;
import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.s.StaticBtreeNode;
import com.gryphpoem.game.zw.robot.action.AcquisitionAction;
import com.gryphpoem.game.zw.robot.action.ActivityRewardAction;
import com.gryphpoem.game.zw.robot.action.ArmRecruitAction;
import com.gryphpoem.game.zw.robot.action.AttackBanditAction;
import com.gryphpoem.game.zw.robot.action.BuildingUpAction;
import com.gryphpoem.game.zw.robot.action.CombatAction;
import com.gryphpoem.game.zw.robot.action.EquipForgeAction;
import com.gryphpoem.game.zw.robot.action.EquipRefitAction;
import com.gryphpoem.game.zw.robot.action.GainResourceAction;
import com.gryphpoem.game.zw.robot.action.GoldAddAction;
import com.gryphpoem.game.zw.robot.action.HeroRecruitAction;
import com.gryphpoem.game.zw.robot.action.MineCollectAction;
import com.gryphpoem.game.zw.robot.action.RoleLvUpAction;
import com.gryphpoem.game.zw.robot.action.TaskRewardAction;
import com.gryphpoem.game.zw.robot.action.TechUpAction;
import com.gryphpoem.game.zw.robot.action.VipUpAction;
import com.gryphpoem.game.zw.robot.base.AbstractActionNode;

/**
 * @Description AI行为树节点信息管理（注册和创建）
 * @author TanDonghai
 * @date 创建时间：2017年11月1日 下午4:29:43
 *
 */
public class BtreeNodeManager {
    private BtreeNodeManager() {
    }

    public static void init() {
        initNodeFactory();
        initActionNode();
    }

    /**
     * 初始化节点工厂
     */
    private static void initNodeFactory() {
        // 如果重写了某些节点的工厂类，需要在这里注册才能有效
    }

    /**
     * 注册action节点细分类型的执行类
     */
    private static void initActionNode() {
        registerActionNode(ActionNodeType.BUILDING_UP.getType(), BuildingUpAction.class);
        registerActionNode(ActionNodeType.TECH_UP.getType(), TechUpAction.class);
        registerActionNode(ActionNodeType.HERO_RECRUIT.getType(), HeroRecruitAction.class);
        registerActionNode(ActionNodeType.ARM_RECRUIT.getType(), ArmRecruitAction.class);
        registerActionNode(ActionNodeType.EQUIP_FORGE.getType(), EquipForgeAction.class);
        registerActionNode(ActionNodeType.EQUIP_REFIT.getType(), EquipRefitAction.class);
        registerActionNode(ActionNodeType.ACTIVITY.getType(), ActivityRewardAction.class);
        registerActionNode(ActionNodeType.VIP_UP.getType(), VipUpAction.class);
        registerActionNode(ActionNodeType.GOLD_ADD.getType(), GoldAddAction.class);
        registerActionNode(ActionNodeType.DO_COMBAT.getType(), CombatAction.class);
        registerActionNode(ActionNodeType.MINE_COLLECT.getType(), MineCollectAction.class);
        registerActionNode(ActionNodeType.ACQUISITION.getType(), AcquisitionAction.class);
        registerActionNode(ActionNodeType.ATTACK_BANDIT.getType(), AttackBanditAction.class);
        registerActionNode(ActionNodeType.TASK_REWARD.getType(), TaskRewardAction.class);
        registerActionNode(ActionNodeType.GAIN_RESOURCE.getType(), GainResourceAction.class);
        registerActionNode(ActionNodeType.ROLE_EXP.getType(), RoleLvUpAction.class);
    }

    /**
     * 注册行为节点类
     * 
     * @param actionNodeType
     * @param clazz
     */
    public static void registerActionNode(int actionNodeType, Class<? extends AbstractActionNode> clazz) {
        if (null != clazz) {
            HaiBtreeManager.registerActionNode(actionNodeType, clazz);
        }
    }

    /**
     * 创建行为树节点
     * 
     * @param sNode 节点配置信息
     * @param shared 是否共享节点
     * @return
     */
    public static IHaiNode createNode(StaticBtreeNode sNode, boolean shared) {
        if (null == sNode) {
            return null;
        }

        return HaiBtreeManager.createNode(sNode, shared);
    }

}
