package com.gryphpoem.game.zw.resource.pojo.function.condition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;

/**
 * @Description 玩家主城等级条件,unlockType:3
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午9:04:05
 *
 */
public class CityLvCondition extends AbstractCondition {

    private int cityLv;

    public CityLvCondition(StaticFunctionCondition config) throws MwException {
        super(config);
        setMustHaveParam(true);
    }

    @Override
    public void parseParam() throws MwException {
        cityLv = Integer.valueOf(config.getParam().trim());
    }

    @Override
    public boolean reachCondition(Object param) throws MwException {
        return greaterThanCondition((int) param, cityLv);
    }

}
