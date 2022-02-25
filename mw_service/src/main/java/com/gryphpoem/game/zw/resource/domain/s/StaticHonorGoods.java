package com.gryphpoem.game.zw.resource.domain.s;
/**
 * 
* @ClassName: StaticHonorGoods
* @Description: 荣誉商品-配置
* @author chenqi
* @date 2018年9月13日
*
 */
public class StaticHonorGoods {
	
	/** 荣誉商品id */
	private Integer honorGoodsId;
	
	/** 荣誉数 */
	private Integer honorNum;
	
	/** 显示折扣 */
	private Integer discount;
	
	/** 金币价格（折后价） */
	private Integer gold;

	public Integer getHonorGoodsId() {
		return honorGoodsId;
	}

	public void setHonorGoodsId(Integer honorGoodsId) {
		this.honorGoodsId = honorGoodsId;
	}

	public Integer getHonorNum() {
		return honorNum;
	}

	public void setHonorNum(Integer honorNum) {
		this.honorNum = honorNum;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	public Integer getGold() {
		return gold;
	}

	public void setGold(Integer gold) {
		this.gold = gold;
	}
}
