package com.gryphpoem.game.zw.service.cityBuild;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/7 9:48
 */
public class ExploreFogDelayRun implements DelayRun {

    private int startTime;

    private StaticHomeCityCell staticHomeCityCell;

    private Player player;


    @Override
    public int deadlineTime() {
        return startTime + staticHomeCityCell.getExploreTime();
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        if (env instanceof HomeCityBuildService) {
            HomeCityBuildService homeCityBuildService = (HomeCityBuildService) env;
            homeCityBuildService.doAtExploreFogEnd(startTime, staticHomeCityCell, player);
        }
    }
}
