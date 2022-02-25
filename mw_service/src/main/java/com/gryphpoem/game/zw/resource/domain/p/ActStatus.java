package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @author ChenKui
 * @version 创建时间：2015-12-4 上午11:43:32
 * @declare 常规的活动值判断
 */

public class ActStatus {

	private int activityId;// 活动编号
	private int actAwardId;// 奖励ID
	private int cond;// 领奖条件值
	private long status;// 已完成值
	private int got;// 活动领取0未领取,1已领取

	public ActStatus() {
	}

	public ActStatus(int activityId, int keyId, int cond, long status, int got) {
		this.actAwardId = activityId;
		this.actAwardId = keyId;
		this.cond = cond;
		this.status = status;
		this.got = got;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getActAwardId() {
		return actAwardId;
	}

	public void setActAwardId(int actAwardId) {
		this.actAwardId = actAwardId;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public int getGot() {
		return got;
	}

	public void setGot(int got) {
		this.got = got;
	}

	public boolean isAcceptAward() {
		if (got == 1) {
			return false;
		}
		if (status >= cond) {
			return true;
		}
		return false;
	}

}
