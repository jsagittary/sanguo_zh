package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticPartyLv.java
 * @Description 军团等级配置表
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午4:07:03
 *
 */
public class StaticPartyLv {
	private int lv;
	private int needExp;// 升到本级需要的经验

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getNeedExp() {
		return needExp;
	}

	public void setNeedExp(int needExp) {
		this.needExp = needExp;
	}

	@Override
	public String toString() {
		return "StaticPartyLv [lv=" + lv + ", needExp=" + needExp + "]";
	}
}
