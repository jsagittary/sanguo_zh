package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 超级武器升级(国器)
 * 
 * @author tyler
 *
 */
public class StaticSuperEquipLv {
	private int id;
	private int type;//
	private int lv;
	private int needOre;//
	private int step;
	private List<List<Integer>> attrs;
	private List<List<Integer>> growCost;
	private int needGrowLv;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getNeedOre() {
		return needOre;
	}

	public void setNeedOre(int needOre) {
		this.needOre = needOre;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public List<List<Integer>> getAttrs() {
		return attrs;
	}

	public void setAttrs(List<List<Integer>> attrs) {
		this.attrs = attrs;
	}

	public List<List<Integer>> getGrowCost() {
		return growCost;
	}

	public void setGrowCost(List<List<Integer>> growCost) {
		this.growCost = growCost;
	}

	public int getNeedGrowLv() {
		return needGrowLv;
	}

	public void setNeedGrowLv(int needGrowLv) {
		this.needGrowLv = needGrowLv;
	}

}
