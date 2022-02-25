package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

/**
 * 
 * @author tyler
 *
 */
public class StaticWallHeroLv {
	private int id;
	private int heroId;
	private int lv;
	private int type;
	private int quality;
	private int line;
	private Map<Integer, Integer> attr;// 属性，格式：[[attrId,value]...]
	private int trainNum;
	private int needFood;
	private int foodAddExp;
	private int needGold;
	private int goldAddExp;
	private int needWallLv;
	private int changeArmy;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	public Map<Integer, Integer> getAttr() {
		return attr;
	}

	public void setAttr(Map<Integer, Integer> attr) {
		this.attr = attr;
	}

	public int getTrainNum() {
		return trainNum;
	}

	public void setTrainNum(int trainNum) {
		this.trainNum = trainNum;
	}

	public int getNeedFood() {
		return needFood;
	}

	public void setNeedFood(int needFood) {
		this.needFood = needFood;
	}

	public int getFoodAddExp() {
		return foodAddExp;
	}

	public void setFoodAddExp(int foodAddExp) {
		this.foodAddExp = foodAddExp;
	}

	public int getNeedGold() {
		return needGold;
	}

	public void setNeedGold(int needGold) {
		this.needGold = needGold;
	}

	public int getGoldAddExp() {
		return goldAddExp;
	}

	public void setGoldAddExp(int goldAddExp) {
		this.goldAddExp = goldAddExp;
	}

	public int getNeedWallLv() {
		return needWallLv;
	}

	public void setNeedWallLv(int needWallLv) {
		this.needWallLv = needWallLv;
	}

	public int getChangeArmy() {
		return changeArmy;
	}

	public void setChangeArmy(int changeArmy) {
		this.changeArmy = changeArmy;
	}

}
