package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticBandit {
	private int banditId;                           // 流寇id
	private int lv;                                 // 流寇等级
	private List<List<Integer>> form;                     // 兵力阵型，格式：[npcId,npcId...]
	private List<List<Integer>> awardBase;          // 匪军掉落的奖励：资源（奖励分类配置为了活动掉落翻倍功能）
	private List<List<Integer>> awardDrawing;       // 匪军掉落的奖励：图纸
	private List<List<Integer>> awardProp;          // 匪军掉落的奖励：道具
	private List<List<Integer>> awardOthers;        // 匪军掉落的奖励：其他
	private List<List<Integer>> awardPlanePieces;   // 掉落的飞机碎片权重

    public int getBanditId() {
		return banditId;
	}

	public void setBanditId(int banditId) {
		this.banditId = banditId;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public List<List<Integer>> getForm() {
		return form;
	}

	public void setForm(List<List<Integer>> form) {
		this.form = form;
	}

	public List<List<Integer>> getAwardBase() {
        return awardBase;
    }

    public void setAwardBase(List<List<Integer>> awardBase) {
        this.awardBase = awardBase;
    }

    public List<List<Integer>> getAwardDrawing() {
        return awardDrawing;
    }

    public void setAwardDrawing(List<List<Integer>> awardDrawing) {
        this.awardDrawing = awardDrawing;
    }

    public List<List<Integer>> getAwardProp() {
        return awardProp;
    }

    public void setAwardProp(List<List<Integer>> awardProp) {
        this.awardProp = awardProp;
    }

    public List<List<Integer>> getAwardOthers() {
        return awardOthers;
    }

    public void setAwardOthers(List<List<Integer>> awardOthers) {
        this.awardOthers = awardOthers;
    }

    public List<List<Integer>> getAwardPlanePieces() {
        return awardPlanePieces;
    }

    public void setAwardPlanePieces(List<List<Integer>> awardPlanePieces) {
        this.awardPlanePieces = awardPlanePieces;
    }

    @Override public String toString() {
        return "StaticBandit{" + "banditId=" + banditId + ", lv=" + lv + ", form=" + form + ", awardBase=" + awardBase
                + ", awardDrawing=" + awardDrawing + ", awardProp=" + awardProp + ", awardOthers=" + awardOthers
                + ", awardPlanePieces=" + awardPlanePieces + '}';
    }

}
