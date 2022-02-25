package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticEquipQualityExtra.java
 * @Description 装备品质与额外属性配置表
 * @author TanDonghai
 * @date 创建时间：2017年3月28日 下午2:03:03
 *
 */
public class StaticEquipQualityExtra {
	private int quality; // 装备品质
	private int extraNum; // 初始额外属性个数
	private int extraLv; // 初始额外属性初始等级
	private int maxLv; // 等级上限

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getExtraNum() {
		return extraNum;
	}

	public void setExtraNum(int extraNum) {
		this.extraNum = extraNum;
	}

	public int getExtraLv() {
		return extraLv;
	}

	public void setExtraLv(int extraLv) {
		this.extraLv = extraLv;
	}

	public int getMaxLv() {
		return maxLv;
	}

	public void setMaxLv(int maxLv) {
		this.maxLv = maxLv;
	}

	@Override
	public String toString() {
		return "StaticEquipQualityExtra [quality=" + quality + ", extraNum=" + extraNum + ", extraLv=" + extraLv
				+ ", maxLv=" + maxLv + "]";
	}
}
