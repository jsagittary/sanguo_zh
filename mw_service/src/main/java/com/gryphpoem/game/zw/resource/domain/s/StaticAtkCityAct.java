package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticAtkCityAct {
	private int keyId;
	private int taskType;
	private int day;
	private int gotoUi;
	private int cond;
	private int num;
	private int point;
	private List<Integer> param;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getTaskType() {
		return taskType;
	}

	public void setTaskType(int type) {
		this.taskType = type;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getGotoUi() {
		return gotoUi;
	}

	public void setGotoUi(int gotoUi) {
		this.gotoUi = gotoUi;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public List<Integer> getParam() {
		return param;
	}

	public void setParam(List<Integer> param) {
		this.param = param;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}
}
