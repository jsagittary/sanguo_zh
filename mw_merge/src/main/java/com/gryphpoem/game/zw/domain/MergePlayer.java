package com.gryphpoem.game.zw.domain;

import java.util.List;

import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Pay;

/**
 * @ClassName MergePlayer.java
 * @Description 合服用的player对象
 * @author QiuKun
 * @date 2018年9月17日
 */
public class MergePlayer extends Player {

	public MergePlayer(Lord lord, int nowTime, int oldServerId) {
		super(lord, nowTime);
		this.oldServerId = oldServerId;
	}

	/**
	 * 检测次改player对象是否有效
	 * 
	 * @return true 表示有效
	 */
	// public boolean checkPlayerIsValid(){
	//
	// }

	/**
	 * 充值的数据
	 */
	private List<Pay> payList;
	/**
	 * 原来的服
	 */
	private int oldServerId;
	/**
	 * 需要和到的服
	 */
	private int toServerId;

	public List<Pay> getPayList() {
		return payList;
	}

	public void setPayList(List<Pay> payList) {
		this.payList = payList;
	}

	public int getOldServerId() {
		return oldServerId;
	}

	public int getToServerId() {
		return toServerId;
	}

	public void setToServerId(int toServerId) {
		this.toServerId = toServerId;
	}

}
