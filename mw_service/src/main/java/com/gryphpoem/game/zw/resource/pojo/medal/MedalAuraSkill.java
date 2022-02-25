package com.gryphpoem.game.zw.resource.pojo.medal;
/**
 * 
* @ClassName: MedalAuraSkill
* @Description: 勋章-光环技能表实体
* @author chenqi
* @date 2018年9月11日
*
 */
public class MedalAuraSkill {

	/** 光环技能id */
	private Integer auraSkillId;
	
	/** 技能名称 */
	private String skillName;
	
	/** 技能品质 */
	private Integer quality;
	
	/** 技能等级 */
	private Integer level;
	
	/** 作用于兵种的类型 */
	private Integer armType;
	
	/** 属性加成效果值[[属性id,加成值],..] */
	private String attrEffect;
	
	/** 技能效果值 */
	private String skillEffect;
	
	/** 备注 */
	private String remarks;

	public Integer getAuraSkillId() {
		return auraSkillId;
	}

	public void setAuraSkillId(Integer auraSkillId) {
		this.auraSkillId = auraSkillId;
	}

	public String getSkillName() {
		return skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
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

	public Integer getArmType() {
		return armType;
	}

	public void setArmType(Integer armType) {
		this.armType = armType;
	}

	public String getAttrEffect() {
		return attrEffect;
	}

	public void setAttrEffect(String attrEffect) {
		this.attrEffect = attrEffect;
	}

	public String getSkillEffect() {
		return skillEffect;
	}

	public void setSkillEffect(String skillEffect) {
		this.skillEffect = skillEffect;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
