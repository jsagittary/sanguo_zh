package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 
* @ClassName: MedalGeneralSkill
* @Description: 勋章-普通技能  配置
* @author chenqi
* @date 2018年9月11日
*
 */
public class StaticMedalGeneralSkill {
	
	/** 普通技能id */
	private Integer generalSkillId;
	
	/** 技能名称 */
	private String skillName;
	
	/** 技能品质 */
	private Integer quality;
	
	/** 技能等级 */
	private Integer level;
	
	/** 技能对应的属性 */
	private Integer attrId;
	
	/** 技能效果值  [属性加成值] 或  [将领类型,属性加成值] */
	private List<Integer> skillEffect;
	
	/** 激活所需的特工亲密等级 */
	private Integer intimateLv;
	
	/** 图标 */
	private String icon;
	
	/** 备注 */
	private String remarks;

	public Integer getGeneralSkillId() {
		return generalSkillId;
	}

	public void setGeneralSkillId(Integer generalSkillId) {
		this.generalSkillId = generalSkillId;
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

	public List<Integer> getSkillEffect() {
		return skillEffect;
	}

	public void setSkillEffect(List<Integer> skillEffect) {
		this.skillEffect = skillEffect;
	}

	public Integer getIntimateLv() {
		return intimateLv;
	}

	public void setIntimateLv(Integer intimateLv) {
		this.intimateLv = intimateLv;
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
