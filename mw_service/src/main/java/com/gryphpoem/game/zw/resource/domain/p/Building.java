package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.resource.constant.BuildingType;

public class Building implements Cloneable {
	private long lordId;
	private int command;// 君王殿等级
	private int wall;// 城墙等级
	private int tech;// 太史院等级
	private int storeHouse;// 仓库等级
	private int mall;// 集市等级
	private int remakeWeaponHouse;// 冶炼铺等级
	private int factory1;// 步兵营等级
	private int factory2;// 骑兵营等级
	private int factory3;// 弓兵营等级
	private int ferry;// 渡口等级
	private int makeWeaponHouse;// 铁匠铺等级
	private int warCollege;// 群贤馆等级
	private int tradeCentre;// 军备堂等级
	private int warFactory;// 尚书台等级
	private int trainFactory1;// 可改造兵营
	private int train2;// 训练基地2(没用)
	private int airBase;// 空军基地(没用)
	private int seasonTreasury; // 赛季宝库建筑(没用)
	private int cia; // 行宫等级
	private int smallGameHouse; // 戏台等级
	private int drawHeroHouse; // 寻访台等级
	private int superEquipHouse; // 铸星台等级
	private int statute; // 雕像等级
	private int medalHouse; // 天录阁等级

	public int getAirBase() {
		return airBase;
	}

	public void setAirBase(int airBase) {
		this.airBase = airBase;
	}

	public int getTrain2() {
		return train2;
	}

	public void setTrain2(int train2) {
		this.train2 = train2;
	}

	public int getTradeCentre() {
		return tradeCentre;
	}

	public void setTradeCentre(int tradeCentre) {
		this.tradeCentre = tradeCentre;
	}

	public int getWarFactory() {
		return warFactory;
	}

	public void setWarFactory(int warFactory) {
		this.warFactory = warFactory;
	}

	public int getTrainFactory1() {
		return trainFactory1;
	}

	public void setTrainFactory1(int trainFactory1) {
		this.trainFactory1 = trainFactory1;
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

	public int getStoreHouse() {
		return storeHouse;
	}

	public void setStoreHouse(int storeHouse) {
		this.storeHouse = storeHouse;
	}

	public int getMall() {
		return mall;
	}

	public void setMall(int mall) {
		this.mall = mall;
	}

	public int getRemakeWeaponHouse() {
		return remakeWeaponHouse;
	}

	public void setRemakeWeaponHouse(int remakeWeaponHouse) {
		this.remakeWeaponHouse = remakeWeaponHouse;
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

	public int getFerry() {
		return ferry;
	}

	public void setFerry(int ferry) {
		this.ferry = ferry;
	}

	public int getMakeWeaponHouse() {
		return makeWeaponHouse;
	}

	public void setMakeWeaponHouse(int makeWeaponHouse) {
		this.makeWeaponHouse = makeWeaponHouse;
	}

	public int getWarCollege() {
		return warCollege;
	}

	public void setWarCollege(int warCollege) {
		this.warCollege = warCollege;
	}

	public int getSeasonTreasury() {
		return seasonTreasury;
	}

	public void setSeasonTreasury(int seasonTreasury) {
		this.seasonTreasury = seasonTreasury;
	}

	public int getCia() {
		return cia;
	}

	public void setCia(int cia) {
		this.cia = cia;
	}

	public int getSmallGameHouse() {
		return smallGameHouse;
	}

	public void setSmallGameHouse(int smallGameHouse) {
		this.smallGameHouse = smallGameHouse;
	}

	public int getDrawHeroHouse() {
		return drawHeroHouse;
	}

	public void setDrawHeroHouse(int drawHeroHouse) {
		this.drawHeroHouse = drawHeroHouse;
	}

	public int getSuperEquipHouse() {
		return superEquipHouse;
	}

	public void setSuperEquipHouse(int superEquipHouse) {
		this.superEquipHouse = superEquipHouse;
	}

	public int getStatute() {
		return statute;
	}

	public void setStatute(int statute) {
		this.statute = statute;
	}

	public int getMedalHouse() {
		return medalHouse;
	}

	public void setMedalHouse(int medalHouse) {
		this.medalHouse = medalHouse;
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
