package com.gryphpoem.game.zw.gameplay.local.world.newyork;

import com.gryphpoem.game.zw.resource.pojo.IntegralRank;

/**
 * Created by pengshuo on 2019/5/13 10:51
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkPlayerIntegralRank extends IntegralRank {
    /** 损兵 */
    private long lostArmy;
    /** 指挥官经验 */
    private long commanderExp;

    public long getLostArmy() {
        return lostArmy;
    }

    public void setLostArmy(long lostArmy) {
        this.lostArmy = lostArmy;
    }

    public long getCommanderExp() {
        return commanderExp;
    }

    public void setCommanderExp(long commanderExp) {
        this.commanderExp = commanderExp;
    }

    public NewYorkPlayerIntegralRank(int camp, long roleId, long value, int now) {
        super(camp,roleId,value,now);
    }
}
