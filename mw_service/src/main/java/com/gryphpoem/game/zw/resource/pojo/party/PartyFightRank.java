package com.gryphpoem.game.zw.resource.pojo.party;

/**
 * @ClassName PartyFightRank.java
 * @Description 军团战力排行榜
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午3:57:27
 *
 */
public class PartyFightRank implements Comparable<PartyFightRank> {
	private long roleId;
	private long fight;

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public long getFight() {
		return fight;
	}

	public void setFight(long fight) {
		this.fight = fight;
	}

	@Override
	public String toString() {
		return "PartyRank [roleId=" + roleId + ", fight=" + fight + "]";
	}

	@Override
	public int compareTo(PartyFightRank o) {
		if (getFight() > o.getFight()) {
			return -1;
		} else if (getFight() < o.getFight()) {
			return 1;
		}
		return 0;
	}

}
