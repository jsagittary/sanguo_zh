package com.gryphpoem.game.zw.rpc.filter;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Objects;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * 将ServerId 带到跨服服务器
 * @Description
 * @Author zhangdh
 * @Date 2021-08-23 20:32
 */
@Activate(group = CONSUMER)
public class ServerIdFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        ServerSetting serverSetting = DataResource.getBean(ServerSetting.class);
        if (Objects.nonNull(serverSetting)) {
            String serverId = String.valueOf(serverSetting.getServerID());
            RpcContext.getContext().setAttachment("serverId", serverId);
//            LogUtil.common("serverId filter 触发 serverId : " + serverId);
        }
        return invoker.invoke(invocation);
    }
}
