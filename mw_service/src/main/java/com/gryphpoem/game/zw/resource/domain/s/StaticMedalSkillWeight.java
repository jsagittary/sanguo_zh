package com.gryphpoem.game.zw.resource.domain.s;
/**
 * 
* @ClassName: StaticMedalSkillWeight
* @Description: 勋章-初始化技能权重配置
* @author chenqi
* @date 2018年9月11日
*
 */
public class StaticMedalSkillWeight {
	
	/** 勋章id */
	private Integer medalId;
	
	/** 类型 1、光环 2、特技 3、普通 */
	private Integer type;
	
	/** 技能id */
	private Integer skillId;
	
	/** 权重值 */
	private Integer weightValue;

	public Integer getMedalId() {
		return medalId;
	}

	public void setMedalId(Integer medalId) {
		this.medalId = medalId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getSkillId() {
		return skillId;
	}

	public void setSkillId(Integer skillId) {
		this.skillId = skillId;
	}

	public Integer getWeightValue() {
		return weightValue;
	}

	public void setWeightValue(Integer weightValue) {
		this.weightValue = weightValue;
	}
}
