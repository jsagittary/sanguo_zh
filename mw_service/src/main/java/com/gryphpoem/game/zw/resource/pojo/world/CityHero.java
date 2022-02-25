package com.gryphpoem.game.zw.resource.pojo.world;

/**
 * @ClassName CityHero.java
 * @Description 城池守卫军
 * @author TanDonghai
 * @date 创建时间：2017年4月20日 下午9:51:11
 *
 */
public class CityHero {
	private int npcId;
	private int curArm;// 当前兵力

	public CityHero() {
	}

	public CityHero(int npcId, int curArm) {
		this.npcId = npcId;
		this.curArm = curArm;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public int getCurArm() {
		return curArm;
	}

	public void setCurArm(int curArm) {
		this.curArm = curArm;
	}

	public void addArm(int add) {
		this.curArm += add;
	}

	public void subArm(int sub) {
		this.curArm -= sub;
		if (curArm < 0) {
			this.curArm = 0;
		}
	}

	@Override
	public String toString() {
		return "CityHero [npcId=" + npcId + ", curArm=" + curArm + "]";
	}
}
