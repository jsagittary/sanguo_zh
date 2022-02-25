package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticHeroLv.java
 * @Description 武将升级经验配置
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 下午4:26:53
 *
 */
public class StaticHeroLv {
	private int level;// 武将等级
	private int quality;// 将领品质
	private int exp;// 升到本级需要的经验

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	@Override
	public String toString() {
		return "StaticHeroLv [level=" + level + ", quality=" + quality + ", exp=" + exp + "]";
	}
}
