package com.gryphpoem.game.zw.resource.domain.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day7Act {
	/** 已领取奖励id */
	private Set<Integer> recvAwardIds = new HashSet<>();
	/** 类型，达成条件值 */
	private Map<Integer, Integer> status = new HashMap<Integer, Integer>();
	/** 达成条件值 */
	private Map<String, Integer> tankTypes = new HashMap<String, Integer>();
	/** 装备品质等级 */
	private List<int[]> equips = new ArrayList<>();
	/** 参与击飞4座至少8级的玩家基地 key1:类型 key2:玩家基地等级*/
	private Map<Integer, Map<Integer, Integer>> typeCnt = new HashMap<Integer, Map<Integer, Integer>>();
	
	private int lvUpDay;
	
	/** 活动过期了 */
	private boolean expired;//临时变量
	
	/** 可以领取 */
	private Set<Integer> canRecvKeyId = new HashSet<>();//临时变量

	public Set<Integer> getRecvAwardIds() {
		return recvAwardIds;
	}

	public void setRecvAwardIds(Set<Integer> recvAwardIds) {
		this.recvAwardIds = recvAwardIds;
	}

	public Map<Integer, Integer> getStatus() {
		return status;
	}

	public void setStatus(Map<Integer, Integer> status) {
		this.status = status;
	}

	public Map<String, Integer> getTankTypes() {
		return tankTypes;
	}

	public void setTankTypes(Map<String, Integer> tankTypes) {
		this.tankTypes = tankTypes;
	}

	public List<int[]> getEquips() {
		return equips;
	}

	public void setEquips(List<int[]> equips) {
		this.equips = equips;
	}

	public int getLvUpDay() {
		return lvUpDay;
	}

	public void setLvUpDay(int lvUpDay) {
		this.lvUpDay = lvUpDay;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public Set<Integer> getCanRecvKeyId() {
		return canRecvKeyId;
	}

	public void setCanRecvKeyId(Set<Integer> canRecvKeyId) {
		this.canRecvKeyId = canRecvKeyId;
	}

	public Map<Integer, Map<Integer, Integer>> getTypeCnt() {
		return typeCnt;
	}
	
	public Map<Integer, Integer> getTypeMap(int type) {
		Map<Integer, Integer> map = typeCnt.get(type);
		if (map == null) {
			map = new HashMap<>();
			typeCnt.put(type, map);
		}
		return typeCnt.get(type);
	}

	public void setTypeCnt(Map<Integer, Map<Integer, Integer>> typeCnt) {
		this.typeCnt = typeCnt;
	}

}
