package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticPartyBuild.java
 * @Description 军团建设配置表
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午4:08:08
 *
 */
public class StaticPartyBuild {
	private int partyLv;// 军团等级
	private int build;// 建设次数
	private List<List<Integer>> cost;// 本次建设消耗，格式：[[type,id,count]...]
	private int partyExp;// 增加国家经验
	private int exploit;// 增加玩家军功

	public int getPartyLv() {
		return partyLv;
	}

	public void setPartyLv(int partyLv) {
		this.partyLv = partyLv;
	}

	public int getBuild() {
		return build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public int getPartyExp() {
		return partyExp;
	}

	public void setPartyExp(int partyExp) {
		this.partyExp = partyExp;
	}

	public int getExploit() {
		return exploit;
	}

	public void setExploit(int exploit) {
		this.exploit = exploit;
	}

	@Override
	public String toString() {
		return "StaticPartyBuild [partyLv=" + partyLv + ", build=" + build + ", cost=" + cost + ", partyExp=" + partyExp
				+ ", exploit=" + exploit + "]";
	}

}
