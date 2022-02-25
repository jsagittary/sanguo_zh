package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticWallHeroLv;

/**
 * 守城npc
 * 
 * @author tyler
 *
 */
public class WallNpc {
	private int id; //城墙NPC的位置
	private int heroNpcId;
	private int level;
	private int exp;
	private int[] attr; // 将领属性，属性：1 攻击，2 防御，3 兵力
	private int count; // 如果是上阵将领，当前兵力
	private int autoArmy;
	private int addTime;//上次补兵时间

	public WallNpc() {
		attr = new int[4];// 三种属性，对应1-3位，0位为补位，不用
		level = 1;// 默认1级
		count = 0;
		exp = 0;
	}

	public WallNpc(int id, int heroNpcId, int level, int exp, int count, int autoArmy,
			int addTime) {
		this.id = id;
		this.heroNpcId = heroNpcId;
		this.level = level;
		this.exp = exp;
		this.count = count;
		this.autoArmy = autoArmy;
		this.addTime = addTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getHeroNpcId() {
		return heroNpcId;
	}
	
    public int getWallHeroLvId() {
        StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(getHeroNpcId(), getLevel());
        if (staticSuperEquipLv != null) {
            return staticSuperEquipLv.getId();
        }
        return 0;
    }

	public void setHeroNpcId(int heroNpcId) {
		this.heroNpcId = heroNpcId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int[] getAttr() {
		return attr;
	}

	public void setAttr(int[] attr) {
		this.attr = attr;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getAutoArmy() {
		return autoArmy;
	}

	public void setAutoArmy(int autoArmy) {
		this.autoArmy = autoArmy;
	}

	public int getAddTime() {
		return addTime;
	}

	public void setAddTime(int addTime) {
		this.addTime = addTime;
	}
	
	/**
     * 扣除兵力
     * 
     * @param sub
     */
    public int subArm(int sub) {
        if (sub <= 0) {
            return 0;
        }

        if (sub >= count) {
            sub = count;
        }
        setCount(count - sub);

        return sub;
    }
}
