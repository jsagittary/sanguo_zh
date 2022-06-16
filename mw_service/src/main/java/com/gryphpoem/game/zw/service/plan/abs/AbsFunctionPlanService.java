package com.gryphpoem.game.zw.service.plan.abs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticDrawHeroDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.Turple;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 11:00
 */
public abstract class AbsFunctionPlanService {

    /**
     * 活动更新
     */
    protected static final int ACT_UPDATE = 0;
    /**
     * 活动新增
     */
    protected static final int ACT_NEW = 1;
    /**
     * 活动删除
     */
    protected static final int ACT_DELETE = 2;

    @Autowired
    protected PlayerDataManager playerDataManager;
    @Autowired
    protected FunctionPlanDataManager functionPlanDataManager;
    @Autowired
    protected RewardDataManager rewardDataManager;
    @Autowired
    protected MailDataManager mailDataManager;

    /**
     * 功能类型
     *
     * @return
     */
    public abstract PlanFunction[] functionId();

    /**
     * 活动previewTime处理
     *
     * @param keyId
     */
    public abstract void handleOnPreviewTime(int keyId);

    /**
     * 活动beginTime时刻的处理
     *
     * @param keyId
     */
    public abstract void handleOnBeginTime(int keyId);

    /**
     * 活动endTime时刻的处理
     *
     * @param keyId
     */
    public abstract void handleOnEndTime(int keyId);

    /**
     * 活动displayTime时刻的处理
     *
     * @param keyId
     */
    public abstract void handleOnDisplayTime(int keyId);

    /**
     * 每天跨天的处理
     *
     * @param player
     */
    public abstract void handleOnDay(Player player);

    /**
     * 某些情况校验条件
     *
     * @param player
     * @throws MwException
     */
    public abstract void checkCondition(Player player, Object... params) throws MwException;

    /**
     * 更新功能数据
     *
     * @param player
     * @param params
     */
    public abstract void updateFunctionData(Player player, Object... params);

    /**
     * 获取plan
     *
     * @param keyId
     * @param player
     * @return
     * @throws MwException
     */
    public Turple<PlanFunction, StaticDrawHeoPlan> checkAndGetPlan(int keyId, Date now, Player player, PlanFunction.PlanStatus... statuses) throws MwException {
        StaticDrawHeoPlan plan = DataResource.ac.getBean(StaticDrawHeroDataMgr.class).getDrawHeoPlanMap().get(keyId);
        if (CheckNull.isNull(plan) || !ArrayUtils.contains(statuses, plan.planStatus(now))) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, no plan config, keyId:%d", player.lord.getLordId(), keyId));
        }
        PlanFunction function = PlanFunction.convertTo(plan.getFunctionId());
        if (CheckNull.isNull(function)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, no plan config, keyId:%d", player.lord.getLordId(), keyId));
        }
        return new Turple<>(function, plan);
    }
}
