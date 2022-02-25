package com.gryphpoem.game.zw.resource.pojo.medal;
/**
 * 
* @ClassName: MedalSpecialSkill
* @Description: 勋章-特殊技能表实体
* @author chenqi
* @date 2018年9月11日
*
 */
public class MedalSpecialSkill {
	
	/** 特殊技能id */
	private Integer specialSkillId;
	
	/** 技能名称 */
	private String skillName;
	
	/** 技能品质 */
	private Integer quality;
	
	/** 技能等级 */
	private Integer level;
	
	/** 加成的属性id */
	private Integer attrId;
	
	/** 属性加成值 */
	private String attrEffect;
	
	/** 技能效果值 */
	private String skillEffect;
	
	/** 图标 */
	private String icon;
	
	/** 备注 */
	private String remarks;

	public Integer getSpecialSkillId() {
		return specialSkillId;
	}

	public void setSpecialSkillId(Integer specialSkillId) {
		this.specialSkillId = specialSkillId;
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

	public Integer getAttrId() {
		return attrId;
	}

	public void setAttrId(Integer attrId) {
		this.attrId = attrId;
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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
