package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 兵营加时
 * 
 * @author tyler
 *
 */
public class StaticFactoryRecruit {
	private int id;
	private int lv;//
	private int upTime;//
	private List<List<Integer>> cost;//
	private int armNum;//
	private int needRoleLv;//
	private int buildingId;

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getUpTime() {
		return upTime;
	}

	public void setUpTime(int upTime) {
		this.upTime = upTime;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public int getArmNum() {
		return armNum;
	}

	public void setArmNum(int armNum) {
		this.armNum = armNum;
	}

	public int getNeedRoleLv() {
		return needRoleLv;
	}

	public void setNeedRoleLv(int needRoleLv) {
		this.needRoleLv = needRoleLv;
	}

	public int getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
