package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

import com.gryphpoem.game.zw.resource.constant.Constant;

/**
 * @ClassName StaticNpc.java
 * @Description NPC配置信息
 * @author TanDonghai
 * @date 创建时间：2017年4月1日 下午2:03:14
 *
 */
public class StaticNpc {
	private int npcId;
	private int exp;// 击败该NPC可以获得的最高经验
	private int lv;// 等级
	private int quality;// 品质
	private int line;// 兵力排数
	private int armType;// 兵种类型
	private int armLv;// 兵种品质、等级
	private Map<Integer, Integer> attr;// 属性，格式：[[attrId,value]...]

	private int totalArm = -1;

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getArmType() {
		return armType;
	}

	public void setArmType(int armType) {
		this.armType = armType;
	}

	public int getArmLv() {
		return armLv;
	}

	public void setArmLv(int armLv) {
		this.armLv = armLv;
	}

	public Map<Integer, Integer> getAttr() {
		return attr;
	}

	public void setAttr(Map<Integer, Integer> attr) {
		this.attr = attr;
	}

	public int getTotalArm() {
		if (totalArm < 0) {
			Integer count = getAttr().get(Constant.AttrId.LEAD);
//			totalArm = null == count ? 0 : count * getLine();
			totalArm = null == count ? 0 : count;
		}
		return totalArm;
	}

	@Override
	public String toString() {
		return "StaticNpc [npcId=" + npcId + ", exp=" + exp + ", lv=" + lv + ", quality=" + quality + ", line=" + line
				+ ", armType=" + armType + ", armLv=" + armLv + ", attr=" + attr + "]";
	}
}
