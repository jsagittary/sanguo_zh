package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 突破
 * 
 * @author tyler
 *
 */
public class StaticHeroBreak {
	private int quality;//
	private int exp;
	private int step;
	private int itemNum;
	private int toQuality;//
	private List<List<Integer>> ratio;

	public List<List<Integer>> getRatio() {
        return ratio;
    }

    public void setRatio(List<List<Integer>> ratio) {
        this.ratio = ratio;
    }

    public int getToQuality() {
		return toQuality;
	}

	public void setToQuality(int toQuality) {
		this.toQuality = toQuality;
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

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getItemNum() {
		return itemNum;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	@Override
	public String toString() {
		return "StaticHeroBreak [quality=" + quality + ", exp=" + exp + ", step=" + step + ", itemNum=" + itemNum + "]";
	}

}
