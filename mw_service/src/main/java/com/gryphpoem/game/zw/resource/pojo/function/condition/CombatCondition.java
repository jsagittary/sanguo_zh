package com.gryphpoem.game.zw.resource.pojo.function.condition;

import java.util.Collection;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;

/**
 * @Description 关卡条件,unlockType:5
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午9:12:14
 *
 */
public class CombatCondition extends AbstractCondition {

    private int combatId;

    public CombatCondition(StaticFunctionCondition config) throws MwException {
        super(config);
        setMustHaveParam(true);
    }

    @Override
    public void parseParam() throws MwException {
        combatId = Integer.valueOf(config.getParam().trim());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean reachCondition(Object param) throws MwException {
        return contains((Collection<Integer>) param, combatId);
    }

}
