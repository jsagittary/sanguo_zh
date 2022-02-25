package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.resource.constant.BuildingType;

public class Building implements Cloneable {
	private long lordId;
	private int command;// 司令部等级
	private int wall;// 围墙等级
	private int tech;// 科研所等级
	private int ware;// 仓库等级
	private int club;// 俱乐部等级
	private int refit;// 改造中心等级
	private int factory1;// 兵营等级
	private int factory2;// 坦克工厂等级
	private int factory3;// 装甲基地等级
	private int chemical;// 化工厂等级
	private int munition;// 军工厂等级
	private int college;// 军事学院 等级
	private int trade;// 贸易中心
	private int war;// 战争工厂
	private int train;// 训练基地
	private int train2;// 训练基地2
	private int air;// 空军基地

	public int getAir() {
		return air;
	}

	public void setAir(int air) {
		this.air = air;
	}

	public int getTrain2() {
		return train2;
	}

	public void setTrain2(int train2) {
		this.train2 = train2;
	}

	public int getTrade() {
		return trade;
	}

	public void setTrade(int trade) {
		this.trade = trade;
	}

	public int getWar() {
		return war;
	}

	public void setWar(int war) {
		this.war = war;
	}

	public int getTrain() {
		return train;
	}

	public void setTrain(int train) {
		this.train = train;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public int getWall() {
		return wall;
	}

	public void setWall(int wall) {
		this.wall = wall;
	}

	public int getTech() {
		return tech;
	}

	public void setTech(int tech) {
		this.tech = tech;
	}

	public int getWare() {
		return ware;
	}

	public void setWare(int ware) {
		this.ware = ware;
	}

	public int getClub() {
		return club;
	}

	public void setClub(int club) {
		this.club = club;
	}

	public int getRefit() {
		return refit;
	}

	public void setRefit(int refit) {
		this.refit = refit;
	}

	public int getFactory1() {
		return factory1;
	}

	public void setFactory1(int factory1) {
		this.factory1 = factory1;
	}

	public int getFactory2() {
		return factory2;
	}

	public void setFactory2(int factory2) {
		this.factory2 = factory2;
	}

	public int getFactory3() {
		return factory3;
	}

	public void setFactory3(int factory3) {
		this.factory3 = factory3;
	}

	public int getChemical() {
		return chemical;
	}

	public void setChemical(int chemical) {
		this.chemical = chemical;
	}

	public int getMunition() {
		return munition;
	}

	public void setMunition(int munition) {
		this.munition = munition;
	}

	public int getCollege() {
		return college;
	}

	public void setCollege(int college) {
		this.college = college;
	}

	public int getFactoryLvByBuildingId(int buildingId) {
		return buildingId == BuildingType.FACTORY_1 ? this.factory1 : buildingId == BuildingType.FACTORY_2 ? this.factory2 : this.factory3;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
