package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticScoutCost.java
 * @Description 侦查消耗配置表
 * @author TanDonghai
 * @date 创建时间：2017年4月18日 下午8:38:47
 *
 */
public class StaticScoutCost {
	private int cityLv;
	private List<List<Integer>> primary;// 初级侦查消耗，格式：[[type,id,count]...]
	private List<List<Integer>> middle;// 中级侦查消耗
	private List<List<Integer>> senior;// 高级侦查消耗

	public int getCityLv() {
		return cityLv;
	}

	public void setCityLv(int cityLv) {
		this.cityLv = cityLv;
	}

	public List<List<Integer>> getPrimary() {
		return primary;
	}

	public void setPrimary(List<List<Integer>> primary) {
		this.primary = primary;
	}

	public List<List<Integer>> getMiddle() {
		return middle;
	}

	public void setMiddle(List<List<Integer>> middle) {
		this.middle = middle;
	}

	public List<List<Integer>> getSenior() {
		return senior;
	}

	public void setSenior(List<List<Integer>> senior) {
		this.senior = senior;
	}

}
