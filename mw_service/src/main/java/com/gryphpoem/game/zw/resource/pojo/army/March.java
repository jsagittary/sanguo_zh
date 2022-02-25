package com.gryphpoem.game.zw.resource.pojo.army;

import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @ClassName March.java
 * @Description 行军路线
 * @author TanDonghai
 * @date 创建时间：2017年4月1日 下午1:08:39
 *
 */
public class March {
	private Player player;
	private Army army;

	public March() {
	}

	public March(Player player, Army army) {
		this.player = player;
		this.army = army;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Army getArmy() {
		return army;
	}

	public void setArmy(Army army) {
		this.army = army;
	}

	/**
	 * 获取行军开始坐标
	 * 
	 * @return
	 */
	public int getStartPos() {
		if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
			return army.getTarget();
		}
		return player.lord.getPos();
	}

	/**
	 * 获取行军目标坐标
	 * 
	 * @return
	 */
	public int getTargetPos() {
		if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
			return player.lord.getPos();
		}
		return army.getTarget();
	}

	/**
	 * 获取行军开始时间
	 * 
	 * @return
	 */
	public int getStartTime() {
		return army.getEndTime() - army.getDuration();
	}

	/**
	 * 获取行军结束时间
	 * 
	 * @return
	 */
	public int getEndTime() {
		return army.getEndTime();
	}

	/**
	 * 当前是否处于行军中
	 * 
	 * @return
	 */
	public boolean isInMarch() {
		return army.getState() == ArmyConstant.ARMY_STATE_MARCH || army.getState() == ArmyConstant.ARMY_STATE_RETREAT;
	}

	/**
	 * 获取玩家阵营
	 * 
	 * @return
	 */
	public int getCamp() {
		return player.lord.getCamp();
	}

	public int getKeyId() {
		return army.getKeyId();
	}
}
