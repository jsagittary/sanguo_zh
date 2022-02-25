package com.gryphpoem.game.zw.mgr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.p.StaticParamDao;
import com.gryphpoem.game.zw.resource.domain.p.StaticParam;

/**
 * @ClassName ServerCfgMgr.java
 * @Description 服务器配置管理
 * @author QiuKun
 * @date 2019年5月9日
 */
@Component
public class ServerCfgMgr {

    @Autowired
    private StaticParamDao staticParamDao;

    // 自己的服务器id
    private int serverId;
    private String zkUrl;
    private int tcpPort;
    private String environment;

    public void init() {
        List<StaticParam> params = staticParamDao.selectStaticParams();
        Map<String, String> paramMap = params.stream()
                .collect(Collectors.toMap(StaticParam::getParamName, StaticParam::getParamValue));
        this.serverId = Integer.parseInt(paramMap.get("serverId"));
        this.zkUrl = paramMap.get("zkUrl").trim();
        this.tcpPort = Integer.parseInt(paramMap.get("tcpPort"));
        this.environment = paramMap.getOrDefault("environment", "test");
        DataResource.environment = this.environment;
    }

    /**
     * 是否是线上环境
     * 
     * @return true 线上环境
     */
    public boolean isReleaseEnv() {
        return "release".equals(environment);
    }

    public StaticParamDao getStaticParamDao() {
        return staticParamDao;
    }

    public int getServerId() {
        return serverId;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getEnvironment() {
        return environment;
    }

}
