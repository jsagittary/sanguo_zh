package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 
* @ClassName: Medal
* @Description: 勋章配置表
* @author chenqi
* @date 2018年9月11日
*
 */
public class StaticMedal {
	
	/** 勋章id */
	private Integer medalId;
	
	/** 勋章名称 */
	private String medalName;
	
	/** 品质  2绿色 3蓝色 4紫色 5橙色*/
	private Integer quality;
	
	/** 勋章等级 */
	private Integer level;
	
	/** 最大强化等级 */
	private Integer maxLevel;
	
	/** 可激活的技能数量 */
	private Integer skillNum;
	
	/** 技能格数量 */
	private Integer skillGrid;
	
	/** 初始携带的最少技能数 */
	private Integer initSkillMin;
	
	/** 初始携带的最多技能数 */
	private Integer initSkillMax;
	
	/** 该勋章绑定的特技 */
	private Integer medalSpecialId;
	
	/** 普通技能的最大等级 */
	private Integer generalSkillMaxLv;
	
	/** 初始化属性范围和权重[[属性id,权重值],...] */
	private List<List<Integer>> initAttr;
	
	/** 初始化技能数量权重配置[[技能数量,权重值],...] */
	private List<List<Integer>> initSkillNum;
	
	/** 初始化技能范围和权重[[1,权重值],[2,权重值],[3,权重值]]
	 * 1、光环 2、特技 3、普通技能 */
	private List<List<Integer>> initGeneralSkill;
	
	/** 备注 */
	private String remarks;

	public Integer getMedalId() {
		return medalId;
	}

	public void setMedalId(Integer medalId) {
		this.medalId = medalId;
	}

	public String getMedalName() {
		return medalName;
	}

	public void setMedalName(String medalName) {
		this.medalName = medalName;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	public Integer getSkillNum() {
		return skillNum;
	}

	public void setSkillNum(Integer skillNum) {
		this.skillNum = skillNum;
	}

	public Integer getSkillGrid() {
		return skillGrid;
	}

	public void setSkillGrid(Integer skillGrid) {
		this.skillGrid = skillGrid;
	}

	public Integer getInitSkillMin() {
		return initSkillMin;
	}

	public void setInitSkillMin(Integer initSkillMin) {
		this.initSkillMin = initSkillMin;
	}

	public Integer getInitSkillMax() {
		return initSkillMax;
	}

	public void setInitSkillMax(Integer initSkillMax) {
		this.initSkillMax = initSkillMax;
	}

	public Integer getMedalSpecialId() {
		return medalSpecialId;
	}

	public void setMedalSpecialId(Integer medalSpecialId) {
		this.medalSpecialId = medalSpecialId;
	}

	public Integer getGeneralSkillMaxLv() {
		return generalSkillMaxLv;
	}

	public void setGeneralSkillMaxLv(Integer generalSkillMaxLv) {
		this.generalSkillMaxLv = generalSkillMaxLv;
	}

	public List<List<Integer>> getInitAttr() {
		return initAttr;
	}

	public void setInitAttr(List<List<Integer>> initAttr) {
		this.initAttr = initAttr;
	}

	public List<List<Integer>> getInitSkillNum() {
		return initSkillNum;
	}

	public void setInitSkillNum(List<List<Integer>> initSkillNum) {
		this.initSkillNum = initSkillNum;
	}

	public List<List<Integer>> getInitGeneralSkill() {
		return initGeneralSkill;
	}

	public void setInitGeneralSkill(List<List<Integer>> initGeneralSkill) {
		this.initGeneralSkill = initGeneralSkill;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
