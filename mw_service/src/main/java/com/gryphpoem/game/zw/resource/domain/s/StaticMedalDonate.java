package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 
* @ClassName: StaticMedalDonate
* @Description: 勋章捐献 配置
* @author chenqi
* @date 2018年9月14日
*
 */
public class StaticMedalDonate {
	
	/** 品质 */
	private Integer quality;
	
	/** 金条 */
	private Integer goldBar;
	
	/** 荣誉 */
	private Integer honor;
	
	/** 权重值[[升级，权重值],[获得资源，权重值]] */
	private List<List<Integer>> weight;

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public Integer getGoldBar() {
		return goldBar;
	}

	public void setGoldBar(Integer goldBar) {
		this.goldBar = goldBar;
	}

	public Integer getHonor() {
		return honor;
	}

	public void setHonor(Integer honor) {
		this.honor = honor;
	}

	public List<List<Integer>> getWeight() {
		return weight;
	}

	public void setWeight(List<List<Integer>> weight) {
		this.weight = weight;
	}
}
