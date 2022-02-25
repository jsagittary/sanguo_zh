package com.gryphpoem.game.zw.mgr;

import java.io.IOException;
import java.util.Collection;

import org.apache.curator.x.discovery.ServiceInstance;

import com.gryphpoem.game.zw.core.InnerMessageHandler;
import com.gryphpoem.game.zw.core.common.ChannelAttr;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.net.InnerServer;
import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.core.registry.RegistryConstant;
import com.gryphpoem.game.zw.core.registry.ServerInfoDetails;
import com.gryphpoem.game.zw.core.registry.ServiceRegisty;
import com.gryphpoem.game.zw.core.registry.ZookeeperClient;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.work.IRWork;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossServerRule;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName InnerServerMgr.java
 * @Description 用于连接跨服逻辑处理
 * @author QiuKun
 * @date 2019年5月11日
 */
public class InnerServerMgr {

    public static InnerServer createInnerServer() throws MwException {
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        final int selfServerId = serverSetting.getServerID();
        StaticCrossServerRule rule = StaticCrossDataMgr.getRuleList().stream()
                .filter(s -> s.getGameServerId() == selfServerId).findFirst().orElse(null);
        if (rule == null) {
            LogUtil.start("没有参加合服 不需要连接");
            return null;
        }
        // 判断zkUrl是否存在,如果不存在也不参加跨服
        String zkUrl = serverSetting.getZkUrl();
        if (zkUrl == null) {
            throw new MwException("没有配置 zkUrl 不进行连接跨服 退出程序");
        }
        // 查找配置
        ServiceInstance<ServerInfoDetails> serviceInstance = findCrossInfo(zkUrl, rule.getCrossServerId());
        if (serviceInstance == null) {
            throw new MwException("跨服服务器配置未找到,不能进行跨服 crossSeverId:" + rule.getCrossServerId());
        }

        return new InnerServer(serviceInstance.getAddress(), serviceInstance.getPort(), selfServerId,
                rule.getCrossServerId()) {

            @Override
            protected BaseChannelHandler initInnerHandler() {
                return new InnerMessageHandler(this);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Base msg) {
                LogUtil.innerMessage(msg);
                Long id = ctx.channel().attr(ChannelAttr.ID).get();
                this.recvExcutor.addTask(id, new IRWork(ctx, msg));
            }
        };
    }

    /**
     * 查找跨服的配置
     * 
     * @param zkUrl zk地址
     * @param crossServerId 跨服服务器id
     * @return
     */
    private static ServiceInstance<ServerInfoDetails> findCrossInfo(String zkUrl, int crossServerId) {
        // 连接配置中心
        ZookeeperClient client = new ZookeeperClient(zkUrl);
        ServiceRegisty serviceRegisty = null;
        ServiceInstance<ServerInfoDetails> res = null;
        try {
            client.start(); // 此处是阻塞的
            serviceRegisty = new ServiceRegisty(client.getClient());
            Collection<ServiceInstance<ServerInfoDetails>> allService = serviceRegisty
                    .getAllServiceByName(RegistryConstant.CROSS_SERVER_NAME);
            res = allService.stream().filter(si -> si.getPayload().getServerId() == crossServerId).findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LogUtil.error(e);
        } finally { // 找到服务器就关闭连接
            if (serviceRegisty != null) {
                try {
                    serviceRegisty.closeServiceDiscovery();
                } catch (IOException e) {
                    LogUtil.error(e);
                }
            }
            client.close();
        }
        return res;
    }

}
