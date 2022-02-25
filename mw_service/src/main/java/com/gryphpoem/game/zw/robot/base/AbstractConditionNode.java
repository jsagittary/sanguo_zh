package com.gryphpoem.game.zw.robot.base;

import com.hundredcent.game.ai.btree.base.AbstractHaiCondition;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBtreeNode;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @Description 行为树条件节点抽象类，条件节点具有以下特征：
 * 
 *              <p>
 *              由于条件节点一定是叶节点，所以不用考虑子节点的问题
 *              </p>
 * 
 * @author TanDonghai
 * @date 创建时间：2017年9月16日 下午5:27:11
 *
 */
public abstract class AbstractConditionNode extends AbstractHaiCondition {
    protected Player robot;

    public AbstractConditionNode(StaticBtreeNode config) throws MwException {
        setConfig(config);

        checkConditionParam();
    }

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

    /**
     * 检查配置条件信息参数是否正确
     * 
     * @throws MwException
     */
    private void checkConditionParam() throws MwException {
        if (!haveConfigParam()) {
            throw new MwException("行为树条件参数未配置, config:" + nodeConfig);
        }
    }

    public abstract void parseCondition();

    @Override
    public boolean tick() {
        try {
            if (checkCondition()) {
                succeed();
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(e, "行为树条件比较出错, config:", nodeConfig);
        }
        fail();
        return false;
    }

    @Override
    public boolean tick(Object... params) {
        if (haveConfigParam() && CheckNull.isEmpty(params)) {
            LogUtil.error("行为树条件参数未获取到, conditionType:", nodeConfig.getSubType());
            return false;
        }

        try {
            return checkCondition(params);
        } catch (Exception e) {
            LogUtil.error(e, "行为树条件（传入参数）比较出错, params:", params, ", config:", nodeConfig);
        }
        return false;
    }

}
