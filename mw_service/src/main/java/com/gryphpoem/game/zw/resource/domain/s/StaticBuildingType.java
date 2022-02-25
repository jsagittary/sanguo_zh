package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticBuildingLv.java
 * @Description 建筑类型配置
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 上午11:00:09
 *
 */
@Deprecated
public class StaticBuildingType {
	private int buildingType;//
	private int initLv;// 初始等级
	private int canUp;// 能否升级
	private int canResource;// 能否产资源

	public int getInitLv() {
		return initLv;
	}

	public void setInitLv(int initLv) {
		this.initLv = initLv;
	}

	public int getBuildingType() {
		return buildingType;
	}

	public void setBuildingType(int buildingType) {
		this.buildingType = buildingType;
	}

	public int getCanUp() {
		return canUp;
	}

	public void setCanUp(int canUp) {
		this.canUp = canUp;
	}

	public int getCanResource() {
		return canResource;
	}

	public void setCanResource(int canResource) {
		this.canResource = canResource;
	}

}
