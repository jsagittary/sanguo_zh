package com.gryphpoem.game.zw.resource.pojo.function.condition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;

/**
 * @Description 科研所等级条件,unlockType:6
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午9:14:14
 *
 */
public class TechLvCondition extends AbstractCondition {

    private int techLv;

    public TechLvCondition(StaticFunctionCondition config) throws MwException {
        super(config);
        setMustHaveParam(true);
    }

    @Override
    public void parseParam() throws MwException {
        techLv = Integer.valueOf(config.getParam().trim());
    }

    @Override
    public boolean reachCondition(Object param) throws MwException {
        return greaterThanCondition((int) param, techLv);
    }

}
