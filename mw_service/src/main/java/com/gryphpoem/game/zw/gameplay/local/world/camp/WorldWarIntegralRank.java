package com.gryphpoem.game.zw.gameplay.local.world.camp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengshuo on 2019/3/23 11:55
 * <br>Description: 世界阵营 积分排行
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class WorldWarIntegralRank {
    /** 阵营排行 */
    private List<WorldWarIntegral> campRank = new ArrayList<>();
    /** 个人积分数据 */
    private WorldWarIntegral personalIntegral;

    public List<WorldWarIntegral> getCampRank() {
        return campRank;
    }

    public void setCampRank(List<WorldWarIntegral> campRank) {
        this.campRank = campRank;
    }

    public WorldWarIntegral getPersonalIntegral() {
        return personalIntegral;
    }

    public void setPersonalIntegral(WorldWarIntegral personalIntegral) {
        this.personalIntegral = personalIntegral;
    }

    @Override
    public String toString() {
        return "WorldWarIntegralRank{" +
                "campRank=" + campRank +
                ", personalIntegral=" + personalIntegral +
                '}';
    }
}
