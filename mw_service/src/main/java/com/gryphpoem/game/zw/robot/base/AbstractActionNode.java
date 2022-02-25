package com.gryphpoem.game.zw.robot.base;

import com.hundredcent.game.ai.btree.base.AbstractHaiAction;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.RobotConstant.ActionNodeType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.robot.RobotService;

/**
 * @Description 行为树action节点
 * @author TanDonghai
 * @date 创建时间：2017年9月19日 下午5:03:56
 *
 */
public abstract class AbstractActionNode extends AbstractHaiAction {
    /** action节点具体类型 */
    protected ActionNodeType actionNodeType;

    /** 如果是非共享行为树节点，节点所属的机器人 */
    protected Player robot;

    protected RobotService robotService = DataResource.ac.getBean(RobotService.class);

    public AbstractActionNode(ActionNodeType actionNodeType) {
        this.actionNodeType = actionNodeType;
        setShared(true);// 默认为共享
    }

    public abstract boolean action();

    public abstract boolean action(Object... params);

    @Override
    public void setRobotRoleId(long roleId) {
        super.setRobotRoleId(roleId);
        Player player = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(roleId);
        fillData(player);
    }

    public void fillData(Player robot) {
        if (null != robot) {
            this.robot = robot;
            setShared(false);
        }
    }

    @Override
    public boolean tick() {
        // long start = System.nanoTime();
        try {
            if (action()) {
                succeed();
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(e, "行为树action处理出错, actionNodeType:", actionNodeType.getType());
        } finally {
            // long end = System.nanoTime();
            // LogUtil.debug("行为节点:", actionNodeType.getName(), ", 执行时时间(微秒):", (end - start) / 1000);
        }
        fail();
        return false;
    }

    @Override
    public boolean tick(Object... params) {
        // long start = System.nanoTime();
        try {
            if (action(params)) {
                succeed();
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(e, "行为树（传入参数）action处理出错, actionNodeType:", actionNodeType.getType());
        } finally {
            // long end = System.nanoTime();
            // LogUtil.debug("行为节点:", actionNodeType.getName(), ", 执行时时间(微秒):", (end - start) / 1000);
        }
        fail();
        return false;
    }

    /**
     * 解析节点配置信息中的参数信息，需要的子节点可以实现该方法
     */
    protected void parseNodeParam() {
    }

    public ActionNodeType getActionNodeType() {
        return actionNodeType;
    }

    public void setActionNodeType(ActionNodeType actionNodeType) {
        this.actionNodeType = actionNodeType;
    }

}
