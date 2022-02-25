package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

/**
 * 
 * @author tyler
 *
 */
public class StaticWallHero {
	private int id;
	private Map<Integer,Integer> gainHero;
	private int needWallLv;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<Integer, Integer> getGainHero() {
		return gainHero;
	}

	public void setGainHero(Map<Integer, Integer> gainHero) {
		this.gainHero = gainHero;
	}

	public int getNeedWallLv() {
		return needWallLv;
	}

	public void setNeedWallLv(int needWallLv) {
		this.needWallLv = needWallLv;
	}

}
