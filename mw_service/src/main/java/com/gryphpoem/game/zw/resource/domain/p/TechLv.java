package com.gryphpoem.game.zw.resource.domain.p;

public class TechLv {
	private int id;
	private int lv;
	private int step;

	public TechLv(int id, int lv, int step) {
		this.id = id;
		this.lv = lv;
		this.step = step;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}
}
