package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 
* @ClassName: StaticUptBuild
* @Description: 建筑改建配置表
* @author chenqi
* @date 2018年8月11日
*
 */
public class StaticUptBuild {
	private int keyId;// 改建id
	private String desc;// 描述
	private List<List<Integer>> resourceCost;// 本次改建消耗，格式：[[type,id,count]...]
	private int buildingType;// 改建类型 1资源田改建  2兵营改建
	private int level;// 建筑等级
	private int uptTime;// 改建所需时长  秒

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<List<Integer>> getResourceCost() {
		return resourceCost;
	}

	public void setResourceCost(List<List<Integer>> resourceCost) {
		this.resourceCost = resourceCost;
	}

	public int getBuildingType() {
		return buildingType;
	}

	public void setBuildingType(int buildingType) {
		this.buildingType = buildingType;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getUptTime() {
		return uptTime;
	}

	public void setUptTime(int uptTime) {
		this.uptTime = uptTime;
	}

	@Override
	public String toString() {
		return "StaticUptBuild [keyId=" + keyId + ", desc=" + desc + ", resourceCost=" + resourceCost + ", buildingType=" + buildingType
				+ ", level=" + level + ", uptTime=" + uptTime + "]";
	}

}
