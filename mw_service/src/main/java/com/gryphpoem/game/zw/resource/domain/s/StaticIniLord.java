package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

public class StaticIniLord {
	private int keyId;
	private int level;// 等级
	private int sex;// 性别
	private int portrait;// 初始头像
	private int vip;// vip
	private int goldGive;// 初始金币
	private int food;// 初始补给资源
	private int elec;// 初始电力资源
	private int oil;// 初始石油资源
	private int ore;// 初始矿石资源
	private int ranks;// 初始军阶
	private int power;// 初始体力
	private int fight;// 初始战力
	private int combat;// 初始副本关卡
	private int newState;// 新手引导状态
	private Map<Integer, Integer> props;// 玩家初始道具

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getPortrait() {
		return portrait;
	}

	public void setPortrait(int portrait) {
		this.portrait = portrait;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public int getGoldGive() {
		return goldGive;
	}

	public void setGoldGive(int goldGive) {
		this.goldGive = goldGive;
	}

	public int getFood() {
		return food;
	}

	public void setFood(int food) {
		this.food = food;
	}

	public int getElec() {
		return elec;
	}

	public void setElec(int elec) {
		this.elec = elec;
	}

	public int getOil() {
		return oil;
	}

	public void setOil(int oil) {
		this.oil = oil;
	}

	public int getOre() {
		return ore;
	}

	public void setOre(int ore) {
		this.ore = ore;
	}

	public int getRanks() {
		return ranks;
	}

	public void setRanks(int ranks) {
		this.ranks = ranks;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getFight() {
		return fight;
	}

	public void setFight(int fight) {
		this.fight = fight;
	}

	public int getCombat() {
		return combat;
	}

	public void setCombat(int combat) {
		this.combat = combat;
	}

	public int getNewState() {
		return newState;
	}

	public void setNewState(int newState) {
		this.newState = newState;
	}

	public Map<Integer, Integer> getProps() {
		return props;
	}

	public void setProps(Map<Integer, Integer> props) {
		this.props = props;
	}
}
