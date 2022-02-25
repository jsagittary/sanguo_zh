package com.gryphpoem.game.zw.resource.pojo;

/**
 * 超级武器(国器)
 * 
 * @author tyler
 *
 */
public class SuperEquip {
	private int type; // 类型
	private int lv; // 等级
	private int step; // 阶段
	private int bomb; // 下次暴击次数
	private int growLv;//进阶等级


	public SuperEquip(int type, int lv, int step, int bomb, int growLv) {
		this.type = type;
		this.lv = lv;
		this.step = step;
		this.bomb = bomb;
		this.growLv = growLv;
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

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getBomb() {
		return bomb;
	}

	public void setBomb(int bomb) {
		this.bomb = bomb;
	}

	public int getGrowLv() {
		return growLv;
	}

	public void setGrowLv(int growLv) {
		this.growLv = growLv;
	}

	@Override
	public String toString() {
		return "SuperEquip [type=" + type + ", lv=" + lv + ", step=" + step + ", bomb=" + bomb + "]";
	}

}
