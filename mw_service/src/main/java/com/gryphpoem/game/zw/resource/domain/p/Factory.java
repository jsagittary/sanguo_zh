package com.gryphpoem.game.zw.resource.domain.p;

import java.util.LinkedList;

public class Factory {
	private int fctLv;// 兵营招募等级
	private int fctExpLv;// 兵营扩建等级
	//募兵队列
	private LinkedList<ArmQue> addList = new LinkedList<>();

	public LinkedList<ArmQue> getAddList() {
		return addList;
	}

	public void setAddList(LinkedList<ArmQue> addList) {
		this.addList = addList;
	}

	public int getFctLv() {
		return fctLv;
	}

	public void setFctLv(int fctLv) {
		this.fctLv = fctLv;
	}

	public int getFctExpLv() {
		return fctExpLv;
	}

	public void setFctExpLv(int fctExpLv) {
		this.fctExpLv = fctExpLv;
	}

}
