package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 超级武器(国器)
 * 
 * @author tyler
 *
 */
public class StaticSuperEquip {
	private int type;//
	private int needOil;//
	private int needElec;//
	private int needTime;//
	private int needRoleLv;//
	private List<List<Integer>> material;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNeedOil() {
		return needOil;
	}

	public void setNeedOil(int needOil) {
		this.needOil = needOil;
	}

	public int getNeedElec() {
		return needElec;
	}

	public void setNeedElec(int needElec) {
		this.needElec = needElec;
	}

	public int getNeedTime() {
		return needTime;
	}

	public void setNeedTime(int needTime) {
		this.needTime = needTime;
	}

	public int getNeedRoleLv() {
		return needRoleLv;
	}

	public void setNeedRoleLv(int needRoleLv) {
		this.needRoleLv = needRoleLv;
	}

	public List<List<Integer>> getMaterial() {
		return material;
	}

	public void setMaterial(List<List<Integer>> material) {
		this.material = material;
	}
}
