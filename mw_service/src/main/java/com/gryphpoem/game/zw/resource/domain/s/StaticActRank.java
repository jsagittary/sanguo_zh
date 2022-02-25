package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author ChenKui
 * @version 创建时间：2015-11-30 下午5:26:20
 * @declare
 */

public class StaticActRank {

	private int keyId;
	private int activityId;
	private int sortId;
	private int rankBegin;
	private int rankEnd;
	private List<List<Integer>> awardList;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getSortId() {
		return sortId;
	}

	public void setSortId(int sortId) {
		this.sortId = sortId;
	}

	public int getRankBegin() {
		return rankBegin;
	}

	public void setRankBegin(int rankBegin) {
		this.rankBegin = rankBegin;
	}

	public int getRankEnd() {
		return rankEnd;
	}

	public void setRankEnd(int rankEnd) {
		this.rankEnd = rankEnd;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

}
