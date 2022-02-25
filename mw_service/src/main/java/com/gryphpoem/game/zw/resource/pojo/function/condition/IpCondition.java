package com.gryphpoem.game.zw.resource.pojo.function.condition;

import java.util.HashSet;
import java.util.Set;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;
import com.gryphpoem.game.zw.resource.pojo.function.ICondition;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @Description IP白名单,unlockType:1
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午8:37:18
 *
 */
public class IpCondition extends AbstractCondition {

    private Set<String> ipSet;

    public IpCondition(StaticFunctionCondition config) throws MwException {
        super(config);
        setMustHaveParam(true);
    }

    @Override
    public void parseParam() throws MwException {
        String param = config.getParam();
        // ip白名单参数格式：ip1,ip2,ip3...
        String[] ips = param.trim().split(ICondition.SEPARATOR);
        if (null != ips) {
            ipSet = new HashSet<>();
            for (String ip : ips) {
                ipSet.add(ip.trim());
            }
        }
    }

    @Override
    public boolean reachCondition(Object param) throws MwException {
        if (CheckNull.isEmpty(ipSet)) {// 没有设置白名单，默认为不进行IP检查
            return true;
        }
        return contains(ipSet, (String) param);
    }

}
