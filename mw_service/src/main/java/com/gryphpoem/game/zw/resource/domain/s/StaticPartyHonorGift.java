package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticPartyHonorGift.java
 * @Description 军团荣誉礼包配置信息
 * @author TanDonghai
 * @date 创建时间：2017年5月4日 下午8:43:37
 *
 */
public class StaticPartyHonorGift {
	private int id;//表id
	private int honorIndex;// 礼包索引，1 初级礼包，2 中级礼包，3 高级礼包
	private int partyLv;//阵营等级
	private int build;// 达成领取条件的建设次数
	private int cityBattle;// 达成领取条件的城战次数
	private int campBattle;// 达成领取条件的国战次数
	private List<List<Integer>> rewardList;// 礼包奖励，格式：[[type,id,count]...]

	public int getHonorIndex() {
		return honorIndex;
	}

	public void setHonorIndex(int honorIndex) {
		this.honorIndex = honorIndex;
	}

	public int getBuild() {
		return build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public int getCityBattle() {
		return cityBattle;
	}

	public void setCityBattle(int cityBattle) {
		this.cityBattle = cityBattle;
	}

	public int getCampBattle() {
		return campBattle;
	}

	public void setCampBattle(int campBattle) {
		this.campBattle = campBattle;
	}

	public List<List<Integer>> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<List<Integer>> rewardList) {
		this.rewardList = rewardList;
	}

	@Override
	public String toString() {
		return "StaticPartyHonorGift [honorIndex=" + honorIndex + ", build=" + build + ", cityBattle=" + cityBattle
				+ ", campBattle=" + campBattle + ", rewardList=" + rewardList + "]";
	}

	public int getPartyLv() {
		return partyLv;
	}

	public void setPartyLv(int partyLv) {
		this.partyLv = partyLv;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
