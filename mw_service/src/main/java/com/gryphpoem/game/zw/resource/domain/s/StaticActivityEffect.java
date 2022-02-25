package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticActivityEffect {
	private int activityId;// 活动ID
	private int day;// 活动开启的第几天
	private List<Integer> effectId;// 当天开启哪些effect

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public List<Integer> getEffectId() {
		return effectId;
	}

	public void setEffectId(List<Integer> effectId) {
		this.effectId = effectId;
	}

}
