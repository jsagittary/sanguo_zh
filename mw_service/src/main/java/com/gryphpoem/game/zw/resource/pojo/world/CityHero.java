package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

/**
 * @ClassName CityHero.java
 * @Description 城池守卫军
 * @author TanDonghai
 * @date 创建时间：2017年4月20日 下午9:51:11
 *
 */
public class CityHero {
	private int npcId;
	private int curArm;// 当前兵力
	private List<Integer> deputyIdList;

	public CityHero() {
	}

	public CityHero(int npcId, int curArm, List<Integer> deputyIdList) {
		this.npcId = npcId;
		this.curArm = curArm;
		this.deputyIdList = deputyIdList;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public int getCurArm() {
		return curArm;
	}

	public void setCurArm(int curArm) {
		this.curArm = curArm;
	}

	public void addArm(int add) {
		this.curArm += add;
	}

	public void subArm(int sub) {
		this.curArm -= sub;
		if (curArm < 0) {
			this.curArm = 0;
		}
	}

	public List<Integer> getDeputyIdList() {
		return deputyIdList;
	}

	public void setDeputyIdList(List<Integer> deputyIdList) {
		this.deputyIdList = deputyIdList;
	}

	public CommonPb.PartnerHeroIdPb createPartnerHeroIdPb() {
		CommonPb.PartnerHeroIdPb.Builder builder = CommonPb.PartnerHeroIdPb.newBuilder();
		builder.setPrincipleHeroId(npcId);
		builder.setCount(this.curArm);
		if (CheckNull.nonEmpty(this.deputyIdList))
			builder.addAllDeputyHeroId(this.deputyIdList);
		return builder.build();
	}

	@Override
	public String toString() {
		return "CityHero [npcId=" + npcId + ", curArm=" + curArm + "]";
	}
}
