package com.gryphpoem.game.zw.resource.domain.p;

import java.util.List;

/**
 * 日志操作
 * 
 * @author tyler
 *
 */
public class logQue {
	private int type;
	private int id;
	private List<Integer> param;
	private int endTime;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Integer> getParam() {
		return param;
	}

	public void setParam(List<Integer> param) {
		this.param = param;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

}
