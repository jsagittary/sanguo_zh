package com.gryphpoem.game.zw.core.registry;

import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;

/**
 * @ClassName CuratorRegistry.java
 * @Description
 * @author QiuKun
 * @date 2019年5月10日
 */
public class ZookeeperClient {

    private String zkUrl;
    private final CuratorFramework client;
    private boolean isStart = false;

    public ZookeeperClient(String zkUrl) {
        this.zkUrl = zkUrl;
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000); // 重试策略
        this.client = CuratorFrameworkFactory.builder().connectString(this.zkUrl).sessionTimeoutMs(30000)
                .connectionTimeoutMs(15000).retryPolicy(retryPolicy).build();
    }

    public void start() throws InterruptedException {
        if (!isStart) {
            client.start();
            client.blockUntilConnected(10, TimeUnit.SECONDS);
            isStart = true;
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

    public boolean isStart() {
        return isStart;
    }

    public void close() {
        CloseableUtils.closeQuietly(client);
    }

}
