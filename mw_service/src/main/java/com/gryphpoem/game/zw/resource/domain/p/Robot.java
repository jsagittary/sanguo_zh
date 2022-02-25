package com.gryphpoem.game.zw.resource.domain.p;

import com.hundredcent.game.ai.btree.HaiBehaviorTree;
import com.gryphpoem.game.zw.resource.constant.RobotConstant;

/**
 * @Description 机器人信息
 * @author TanDonghai
 * @date 创建时间：2017年9月20日 下午1:55:11
 *
 */
public class Robot {
    private long roleId;
    private int treeId;// 行为树id
    private int robotState;// 机器人状态，0 正常，1 失效
    private int guideIndex;// 当前已执行的新手指引奖励index最大纪录

    private HaiBehaviorTree defaultTree;// 对应的行为树实例
    private int nextTick;// 记录下一次遍历行为树的时间
    private int posArea = RobotConstant.ROBOT_NOT_HAVE_POS;//  是否分配区域坐标, 1 分配.0 未分配
    private int actionType = RobotConstant.ROBOT_INNER_BEHAVIOR;
    /**
     * 机器人状态是否正常
     * 
     * @return
     */
    public boolean isValid() {
        return robotState == RobotConstant.ROBOT_STATE_NORMAL;
    }

    /**
     * 判断机器人是否分配坐标
     * @return true 未分配 false 已经分配
     */
    public boolean hasPos() {
        return posArea != RobotConstant.ROBOT_NOT_HAVE_POS;
    }


    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }

    public int getRobotState() {
        return robotState;
    }

    public void setRobotState(int robotState) {
        this.robotState = robotState;
    }

    public int getGuideIndex() {
        return guideIndex;
    }

    public void setGuideIndex(int guideIndex) {
        this.guideIndex = guideIndex;
    }

    public HaiBehaviorTree getDefaultTree() {
        return defaultTree;
    }

    public void setDefaultTree(HaiBehaviorTree defaultTree) {
        this.defaultTree = defaultTree;
    }

    public int getNextTick() {
        return nextTick;
    }

    public void setNextTick(int nextTick) {
        this.nextTick = nextTick;
    }

    public int getPosArea() {
        return posArea;
    }

    public void setPosArea(int posArea) {
        this.posArea = posArea;
    }

    @Override
    public String toString() {
        return "Robot{" +
                "roleId=" + roleId +
                ", treeId=" + treeId +
                ", robotState=" + robotState +
                ", guideIndex=" + guideIndex +
                ", defaultTree=" + defaultTree +
                ", nextTick=" + nextTick +
                ", posArea=" + posArea +
                ", actionType=" + actionType +
                '}';
    }

}
