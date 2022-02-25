package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 化工厂扩建
 * 
 * @author tyler
 *
 */
public class StaticChemicalExpand {
	private int cnt;
	private List<List<Integer>> cost;
	private int num;

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

}
