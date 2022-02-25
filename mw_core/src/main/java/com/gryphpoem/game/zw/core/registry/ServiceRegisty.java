package com.gryphpoem.game.zw.core.registry;

import java.io.IOException;
import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * @ClassName ServiceRegisty.java
 * @Description
 * @author QiuKun
 * @date 2019年5月10日
 */
public class ServiceRegisty {

    private ServiceDiscovery<ServerInfoDetails> serviceDiscovery;

    public ServiceRegisty(CuratorFramework client) throws Exception {
        JsonInstanceSerializer<ServerInfoDetails> serializer = new JsonInstanceSerializer<ServerInfoDetails>(
                ServerInfoDetails.class);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServerInfoDetails.class).client(client)
                .serializer(serializer).basePath(RegistryConstant.BASE_PATH).build();
        serviceDiscovery.start();
    }

    public void registerService(ServiceInstance<ServerInfoDetails> serviceInstance) throws Exception {
        serviceDiscovery.registerService(serviceInstance);
    }

    public void unregisterService(ServiceInstance<ServerInfoDetails> serviceInstance) throws Exception {
        serviceDiscovery.unregisterService(serviceInstance);
    }

    public void updateService(ServiceInstance<ServerInfoDetails> serviceInstance) throws Exception {
        serviceDiscovery.updateService(serviceInstance);
    }

    /**
     * 获取所有的
     * 
     * @param serviceName
     * @return
     * @throws Exception
     */
    public Collection<ServiceInstance<ServerInfoDetails>> getAllServiceByName(String serviceName) throws Exception {
        return serviceDiscovery.queryForInstances(serviceName);
    }

    public void closeServiceDiscovery() throws IOException {
        serviceDiscovery.close();
    }

}
