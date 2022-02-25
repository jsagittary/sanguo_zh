package com.gryphpoem.game.zw.resource.domain.p;

import java.util.LinkedHashMap;

import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * 聚宝盆
 * 
 * @author tyler
 *
 */
public class Treasure {
	private LinkedHashMap<Integer, Integer> idStatus;// 够买的id,状态(0未购买 1已购买)
	private int status; // 0隐藏,1已金币购买
	private int endTime; // 下次翻牌时间
	private int resTime; // 资源兑换红名时间 时间差大于4小时不允许兑换
	private boolean red;// 红名
	private int updTime;// 刷新时间

	public Treasure() {
		idStatus = new LinkedHashMap<>();
		updTime = TimeHelper.getCurrentSecond();
	}

	public LinkedHashMap<Integer, Integer> getIdStatus() {
		return idStatus;
	}

	public void setIdStatus(LinkedHashMap<Integer, Integer> idStatus) {
		this.idStatus = idStatus;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getResTime() {
		return resTime;
	}

	public void setResTime(int resTime) {
		this.resTime = resTime;
	}

	public boolean isRed() {
		return red;
	}

	public void setRed(boolean red) {
		this.red = red;
	}

	public int getUpdTime() {
		return updTime;
	}

	public void setUpdTime(int updTime) {
		this.updTime = updTime;
	}

}