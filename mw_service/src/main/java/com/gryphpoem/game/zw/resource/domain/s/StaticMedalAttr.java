package com.gryphpoem.game.zw.resource.domain.s;
/**
 * 
* @ClassName: StaticMedalAttr
* @Description: 勋章-属性配置
* @author chenqi
* @date 2018年9月11日
*
 */
public class StaticMedalAttr {

	/** 品质 */
	private Integer quality;
	
	/** 属性id */
	private Integer attrId;
	
	/** 基础属性 */
	private Integer base;
	
	/** 每次强化一级提升的值 */
	private Integer grow;

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public Integer getAttrId() {
		return attrId;
	}

	public void setAttrId(Integer attrId) {
		this.attrId = attrId;
	}

	public Integer getBase() {
		return base;
	}

	public void setBase(Integer base) {
		this.base = base;
	}

	public Integer getGrow() {
		return grow;
	}

	public void setGrow(Integer grow) {
		this.grow = grow;
	}
}
