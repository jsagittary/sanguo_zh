package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 关卡副本
 * 
 * @author tyler
 *
 */
public class CombatFb {
	private int combatId;
	private int status;//完成状态(0未完成,1已完成,2已过期)
	private int cnt;// 剩余次数
	private int endTime;//剩余时间(-1无限制)
	private int gain;//已获得ID(类型4专属)
	private int buyCnt;//已购买资源副本次数

	public CombatFb(int combatId, int status, int cnt, int endTime) {
		this.combatId = combatId;
		this.status = status;
		this.cnt = cnt;
		this.endTime = endTime;
	}

	public CombatFb(int combatId, int status, int cnt, int endTime, int gain, int buyCnt) {
		this.combatId = combatId;
		this.status = status;
		this.cnt = cnt;
		this.endTime = endTime;
		this.gain = gain;
		this.buyCnt = buyCnt;
	}



	public int getCombatId() {
		return combatId;
	}

	public void setCombatId(int combatId) {
		this.combatId = combatId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getGain() {
		return gain;
	}

	public void setGain(int gain) {
		this.gain = gain;
	}

	public int getBuyCnt() {
		return buyCnt;
	}

	public void setBuyCnt(int buyCnt) {
		this.buyCnt = buyCnt;
	}

}
