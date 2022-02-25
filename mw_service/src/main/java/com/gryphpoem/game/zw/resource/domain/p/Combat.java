package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 关卡
 * 
 * @author tyler
 *
 */
public class Combat {
	private int combatId;
	private int star;

	public Combat(int combatId, int star) {
		this.combatId = combatId;
		this.star = star;
	}

	public int getCombatId() {
		return combatId;
	}

	public void setCombatId(int combatId) {
		this.combatId = combatId;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}


}
