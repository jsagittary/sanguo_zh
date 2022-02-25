package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @ClassName StaticAreaBlock.java
 * @Description 世界地图行政分区区块配置表
 * @author TanDonghai
 * @date 创建时间：2017年3月31日 下午9:08:22
 *
 */
public class StaticAreaRange {
	private int areaOrder;// 分区等级
	private int range;// 矿点等级分段，用于找到同分段的分块
	private List<Integer> block;// 10*10的分块id
	private Map<Integer, Integer> mineLv;// 矿点等级配置，格式：[[lv,maxNum]...]

	public int getAreaOrder() {
		return areaOrder;
	}

	public void setAreaOrder(int areaOrder) {
		this.areaOrder = areaOrder;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public List<Integer> getBlock() {
		return block;
	}

	public void setBlock(List<Integer> block) {
		this.block = block;
	}

	public Map<Integer, Integer> getMineLv() {
		return mineLv;
	}

	public void setMineLv(Map<Integer, Integer> mineLv) {
		this.mineLv = mineLv;
	}

	@Override
	public String toString() {
		return "StaticAreaRange [areaOrder=" + areaOrder + ", range=" + range + ", block=" + block + ", mineLv="
				+ mineLv + "]";
	}
}
