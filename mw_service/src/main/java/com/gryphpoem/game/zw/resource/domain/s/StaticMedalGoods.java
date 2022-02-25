package com.gryphpoem.game.zw.resource.domain.s;
/**
 * 
* @ClassName: StaticMedalGoods
* @Description: 勋章商品配置
* @author chenqi
* @date 2018年9月12日
*
 */
public class StaticMedalGoods {
	
	/** 勋章商品id */
	private Integer medalGoodsId;
	
	/** 商品类型 1勋章  2免费奖励 */
	private Integer type;
	
	/** 勋章id */
	private Integer medalId;
	
	/** 指定的光环 */
	private Integer auraSkillId;
	
	/** 购买所需的荣耀点数/可领取的荣誉点数 */
	private Integer buyHonor;
	
	/** 权重 */
	private Integer weight;
	
	/** 备注 */
	private String remarks;

	public Integer getMedalGoodsId() {
		return medalGoodsId;
	}

	public void setMedalGoodsId(Integer medalGoodsId) {
		this.medalGoodsId = medalGoodsId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getMedalId() {
		return medalId;
	}

	public void setMedalId(Integer medalId) {
		this.medalId = medalId;
	}

	public Integer getAuraSkillId() {
		return auraSkillId;
	}

	public void setAuraSkillId(Integer auraSkillId) {
		this.auraSkillId = auraSkillId;
	}

	public Integer getBuyHonor() {
		return buyHonor;
	}

	public void setBuyHonor(Integer buyHonor) {
		this.buyHonor = buyHonor;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
