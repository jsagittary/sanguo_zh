package com.gryphpoem.game.zw.resource.constant;
/**
 * 矿石转盘配置信息
 * @author ericSong
 *
 */
public enum StoneTurnType {
	Single(1,10),//单次转盘
	Ten(10,100),//十次转盘
	;
	private int num;
	private int gold;
	private StoneTurnType(int num, int gold) {
		this.num = num;
		this.gold = gold;
	}
	public int getNum() {
		return num;
	}
	public int getGold() {
		return gold;
	}
	
	/**
	 * 通过转盘数量获取所需要的金币数
	 * @param num
	 * @return
	 */
	public static int getGoldNeedByNum(int num)
	{
		StoneTurnType[]ss=StoneTurnType.values();
		for(StoneTurnType s:ss)
		{
			if(s.getNum()==num)
			{
				return s.getGold();
			}
		}
		return -1;
	}
	
}
