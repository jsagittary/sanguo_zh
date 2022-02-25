package com.gryphpoem.game.zw.intercept;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.intercept.IMessageIntercept;
import com.gryphpoem.game.zw.core.intercept.InterceptType;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionUnlock;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;
import com.gryphpoem.game.zw.resource.util.FunctionConditionUtil;

/**
 * @Description 功能解锁检查前置拦截器
 * @author TanDonghai
 * @date 创建时间：2017年6月19日 上午10:22:34
 *
 */
public class FunctionUnlockInterceptor implements IMessageIntercept {

    @Override
    public InterceptType getInterceptType() {
        return InterceptType.FUNCTION_UNLOCK;
    }

    @Override
    public void doIntercept(long roleId, Base msg, Object functionId) throws MwException {
//        LogUtil.debug("功能解锁拦截器执行， roleId:", roleId, ", functionId:", functionId, ", msg:", msg);

        StaticFunctionUnlock sfu = StaticFunctionDataMgr.getUnlockMap().get(functionId);
        if (null == sfu) {
            throw new MwException(GameError.FUNCTION_UNLOCK_NO_CONFIG.getCode(), "功能解锁未配置, roleId:", roleId,
                    ", functionId:", functionId, ", msg:", msg);
        }

        AbstractCondition condition;
        for (Integer conditionId : sfu.getCondition()) {
            condition = StaticFunctionDataMgr.getConditionById(conditionId);
            if (null == condition) {
                LogUtil.error("功能解锁条件未初始化, conditonId:", conditionId, ", function:", sfu);
                continue;
            }

            Object checkData = FunctionConditionUtil.getConditionCheckData(roleId, condition.getUnlockType());
            condition.checkFunction(checkData);
        }
    }

}
