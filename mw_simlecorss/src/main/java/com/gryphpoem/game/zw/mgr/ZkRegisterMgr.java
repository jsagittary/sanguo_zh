package com.gryphpoem.game.zw.mgr;

import java.io.IOException;
import java.util.Collection;

import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.registry.RegistryConstant;
import com.gryphpoem.game.zw.core.registry.ServerInfoDetails;
import com.gryphpoem.game.zw.core.registry.ServiceRegisty;
import com.gryphpoem.game.zw.core.registry.ZookeeperClient;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.NetUtils;

/**
 * @ClassName ZkrRegisterMgr.java
 * @Description
 * @author QiuKun
 * @date 2019年5月10日
 */
@Component
public class ZkRegisterMgr {
    private ZookeeperClient client;
    private ServiceRegisty serviceRegisty;

    @Autowired
    private ServerCfgMgr serverCfgMgr;

    public boolean init() {
        client = new ZookeeperClient(serverCfgMgr.getZkUrl());
        try {
            client.start();
            serviceRegisty = new ServiceRegisty(client.getClient());
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }

        return true;
    }

    private boolean register(String zkUrl, int serverId, int port) {
        try {
            // 已经注册过就不能启动
            String serverIdStr = RegistryConstant.CROSS_SERVER_ID_PREFIX + serverId;
            Collection<ServiceInstance<ServerInfoDetails>> allService = serviceRegisty
                    .getAllServiceByName(RegistryConstant.CROSS_SERVER_NAME);
            if (allService != null && !allService.isEmpty()) {
                for (ServiceInstance<ServerInfoDetails> s : allService) {
                    if (serverIdStr.equals(s.getId())) {
                        LogUtil.error("该服务已经存在 ip:" + s.getAddress() + ", prot:" + s.getPort());
                        return false;
                    }
                }
            }
            String localIp = NetUtils.getLocalIpByZkUrl(zkUrl);
            ServerInfoDetails details = new ServerInfoDetails(serverId, localIp, port,
                    RegistryConstant.SERVER_TYPE_CROSS);

            ServiceInstance<ServerInfoDetails> serviceInstance = ServiceInstance.<ServerInfoDetails> builder()
                    .id(serverIdStr).name(RegistryConstant.CROSS_SERVER_NAME).port(port).address(localIp)
                    .payload(details)
                    // .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                    .build();
            serviceRegisty.registerService(serviceInstance);
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
        return true;
    }

    public boolean register() {
        return register(serverCfgMgr.getZkUrl(), serverCfgMgr.getServerId(), serverCfgMgr.getTcpPort());
    }

    public void stop() {
        if (serviceRegisty != null) {
            try {
                serviceRegisty.closeServiceDiscovery();
            } catch (IOException e) {
                LogUtil.error(e);
            }
        }
        if (client != null) {
            client.close();
        }
    }
}
