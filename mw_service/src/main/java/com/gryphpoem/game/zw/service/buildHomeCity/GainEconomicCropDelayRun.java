package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicCrop;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 收取经济作物的延时任务
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/15 13:44
 */
public class GainEconomicCropDelayRun implements DelayRun {

    private Player player;

    private int startTime;

    private StaticEconomicCrop staticEconomicCrop;

    private int buildingId;

    public GainEconomicCropDelayRun(Player player, int startTime, StaticEconomicCrop staticEconomicCrop, int buildingId) {
        this.player = player;
        this.startTime = startTime;
        this.staticEconomicCrop = staticEconomicCrop;
        this.buildingId = buildingId;
    }

    @Override
    public int deadlineTime() {
        return startTime + staticEconomicCrop.getProductTime();
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        if (env instanceof BuildingService) {
            BuildingService buildingService = (BuildingService) env;
            buildingService.gainEconomicCrop(player, staticEconomicCrop, buildingId);
        }
    }
}
