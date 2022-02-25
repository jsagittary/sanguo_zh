package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @author zhangzxy
 * @date 创建时间:2021/12/15
 * @Description 称号
 */
public class StaticTitle {

	private int id;

	/**
	 * 任务id,对应s_diaochan_task表内taskid
	 */
	private Integer taskId;

	/**
	 * 任务条件参数
	 */
	private List<Integer> taskParam;

	/**
	 * 持续时间,时间大于0填写，秒
	 */
	private long duration;

	/**
	 * 属性[[属性id,属性值]]，对应s_attribute表内attributeId
	 */
	private Map<Integer, Integer> attr;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public List<Integer> getTaskParam() {
		return taskParam;
	}

	public void setTaskParam(List<Integer> taskParam) {
		this.taskParam = taskParam;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Map<Integer, Integer> getAttr() {
		return attr;
	}

	public void setAttr(Map<Integer, Integer> attr) {
		this.attr = attr;
	}

}
