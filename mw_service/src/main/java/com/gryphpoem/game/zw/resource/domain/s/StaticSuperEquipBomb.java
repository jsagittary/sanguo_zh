package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

/**
 * 超级武器暴击(国器)
 * 
 * @author tyler
 *
 */
public class StaticSuperEquipBomb {
	private int lv;//
	private Map<Integer, Integer> bomb;

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public Map<Integer, Integer> getBomb() {
		return bomb;
	}

	public void setBomb(Map<Integer, Integer> bomb) {
		this.bomb = bomb;
	}

}
