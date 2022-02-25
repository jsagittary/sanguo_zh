package com.gryphpoem.game.zw.resource.domain;

public class SaveFlag {
	public static final int ACCOUNT = 0;
	public static final int LORD = 1;
	public static final int RESOURCE = 2;
	public static final int BUILDING = 3;
	public static final int TANK = 4;
	public static final int FORM = 5;
	public static final int BUILD_QUE = 6;
	public static final int TANK_QUE_1 = 7;
	public static final int TANK_QUE_2 = 8;
	public static final int PROP = 9;
	public static final int PROP_QUE = 10;
	public static final int REFIT_QUE = 11;
	public static final int EQUIP = 12;
	public static final int PART = 13;
	public static final int CHIP = 14;
	public static final int ARMY = 15;
	private boolean[] flags = new boolean[16];

	public void setFlag(int index) {
		flags[index] = true;
	}

	public void clearFlag(int index) {
		flags[index] = false;
	}

	public SaveFlag() {
		super();
	}
	
	public boolean needSave(int index){
		return flags[index];
	}

	public SaveFlag(SaveFlag src) {
		System.arraycopy(src.flags, 0, flags, 0, 16);
	}
}
