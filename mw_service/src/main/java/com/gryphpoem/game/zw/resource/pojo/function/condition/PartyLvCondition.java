package com.gryphpoem.game.zw.resource.pojo.function.condition;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;

/**
 * @Description 军团等级条件,unlockType:7
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午9:16:18
 *
 */
public class PartyLvCondition extends AbstractCondition {

    private int partyLv;

    public PartyLvCondition(StaticFunctionCondition config) throws MwException {
        super(config);
        setMustHaveParam(true);
    }

    @Override
    public void parseParam() throws MwException {
        partyLv = Integer.valueOf(config.getParam().trim());
    }

    @Override
    public boolean reachCondition(Object param) throws MwException {
        return greaterThanCondition((int) param, partyLv);
    }

}
