package com.gryphpoem.game.zw.jmx.mbean;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.handler.http.GmHandler;

/**
 * JMX动态监控
 * 
 * @author ericSong
 * 
 */
@Component
@ManagedResource(objectName = "A_GameServer:name=ServerMBean", description = "服务器MBean")
public class ServerMBean {

	@ManagedOperation(description = "初始化玩家数据")
	@ManagedOperationParameters({})
	public String test() {
		return "测试";
	}

	@ManagedOperation(description = "发送全部角色信息给账号服")
	public String sendAllRoleToAccount() {
		try {
			GmHandler.sendAllRoleToAccount();
		} catch (Exception e) {
			LogUtil.error("", e);
		}
		return "清理成功";
	}

}
