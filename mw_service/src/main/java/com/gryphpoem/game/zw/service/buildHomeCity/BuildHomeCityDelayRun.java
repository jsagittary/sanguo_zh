package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/7 17:53
 */
public class BuildHomeCityDelayRun implements DelayRun {

    private int type; // 1-探索; 2-开垦

    private int endTime;

    private int cellId;

    private int scoutIndex; // 探索时, 派遣的侦察兵索引

    private int farmerCnt; // 开垦时, 派遣的农名数量

    private Player player;

    public BuildHomeCityDelayRun(int type, int endTime, int cellId, int scoutIndex, int farmerCnt, Player player) {
        this.type = type;
        this.endTime = endTime;
        this.cellId = cellId;
        this.scoutIndex = scoutIndex;
        this.farmerCnt = farmerCnt;
        this.player = player;
    }

    @Override
    public int deadlineTime() {
        return endTime;
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        if (env instanceof BuildHomeCityService) {
            BuildHomeCityService buildHomeCityService = (BuildHomeCityService) env;
            switch (type) {
                case 1:
                    buildHomeCityService.doAtExploreEnd(cellId, scoutIndex, player);
                    break;
                case 2:
                    buildHomeCityService.doAtReclaimFoundationEnd(cellId, farmerCnt, player);
                    break;
            }
        }
    }

}
