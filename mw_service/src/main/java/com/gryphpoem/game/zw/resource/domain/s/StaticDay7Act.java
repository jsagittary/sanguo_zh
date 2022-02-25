package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

public class StaticDay7Act {
	private int keyId;
	/**
	 * 活动任务类型，查看s_act_tasktype表
	 */
	private int taskType;
	/**
	 * 天数
	 */
	private int day;
	/**
	 * 客户端跳转使用
	 */
	private int gotoUi;
	/**
	 * 次数, 数量
	 */
	private int cond;
	/**
	 * 奖励
	 */
	private List<List<Integer>> awardList;
	/**
	 * 条件,9和15指[装备品质,部位],3指[雇佣NPC类型,品质]
	 */
	private List<Integer> param;
	/**
	 * 开启的服务器id
	 */
	private List<List<Integer>> serverId;

	/**
	 * 判断是否该服务器的战令活动
	 *
	 * @param sid 服务器id
	 * @return true 说明是自己的服务器的plan
	 */
	public boolean checkServerPlan(int sid) {
		if (!CheckNull.isEmpty(this.serverId)) {
			// 这里配置的conf[0]和[1] 是起始的serverId和结束的serverId
			List<Integer> startEndConf = this.serverId.stream().filter(conf -> conf.get(0) <= conf.get(1) && sid >= conf.get(0) && sid <= conf.get(1)).findFirst().orElse(null);
			return !CheckNull.isEmpty(startEndConf);
		}
		return true;
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getTaskType() {
		return taskType;
	}

	public void setTaskType(int type) {
		this.taskType = type;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getGotoUi() {
		return gotoUi;
	}

	public void setGotoUi(int gotoUi) {
		this.gotoUi = gotoUi;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

	public List<Integer> getParam() {
		return param;
	}

	public void setParam(List<Integer> param) {
		this.param = param;
	}

	public List<List<Integer>> getServerId() {
		return serverId;
	}

	public void setServerId(List<List<Integer>> serverId) {
		this.serverId = serverId;
	}
}
