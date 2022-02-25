package com.gryphpoem.game.zw.domain;

import java.util.Date;

/**
 * 
 * @ClassName MasterServerSetting.java
 * @Description 主服的serverSetting信息
 * @author QiuKun
 * @date 2018年10月16日
 *
 */
public class MasterServerSetting {
	private final int serverId;
	/**
	 * 开发时间
	 */
	private Date openTime;
	/**
	 * 活动模板
	 */
	private int actMold;

	public MasterServerSetting(int serverId) {
		this.serverId = serverId;
	}

	public Date getOpenTime() {
		return openTime;
	}

	public void setOpenTime(Date openTime) {
		this.openTime = openTime;
	}

	public int getActMold() {
		return actMold;
	}

	public void setActMold(int actMold) {
		this.actMold = actMold;
	}

	public int getServerId() {
		return serverId;
	}

	@Override
	public String toString() {
		return "MasterServerSetting [serverId=" + serverId + ", openTime=" + openTime + ", actMold=" + actMold + "]";
	}

}
