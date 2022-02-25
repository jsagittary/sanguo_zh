package com.gryphpoem.game.zw.resource.pojo.function.condition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;

/**
 * @Description 角色等级条件,unlockType:2
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午7:47:29
 *
 */
public class RoleLvCondition extends AbstractCondition {

    private int roleLv;

    public RoleLvCondition(StaticFunctionCondition config) throws MwException {
        super(config);
        setMustHaveParam(true);
    }

    @Override
    public void parseParam() throws MwException {
        roleLv = Integer.valueOf(config.getParam().trim());
    }

    @Override
    public boolean reachCondition(Object param) throws MwException {
        return greaterThanCondition((int) param, roleLv);
    }

}
