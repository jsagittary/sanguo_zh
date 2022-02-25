package com.gryphpoem.game.zw.resource.domain.p;

import java.util.Date;

public class SmallId {
	private long accountKey;//账号id
	private long lordId;     //角色id
	private Date createTime;//创建时间

	public long getLordId() {
		return lordId;
	}
	
	public long getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(long accountKey) {
		this.accountKey = accountKey;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
