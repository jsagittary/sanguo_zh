// package com.gryphpoem.game.zw.service.buildHomeCity;
//
// import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
// import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
// import com.gryphpoem.game.zw.resource.domain.Player;
// import com.gryphpoem.game.zw.service.BuildingService;
//
// import java.util.List;
//
// /**
//  * 收取经济作物的延时任务
//  *
//  * @Author: GeYuanpeng
//  * @Date: 2022/11/15 13:44
//  */
// public class GainEconomicCropDelayRun implements DelayRun {
//
//     private Player player;
//
//     private List<Integer> curProductCrop;
//
//     private int buildingId;
//
//     public GainEconomicCropDelayRun(Player player, List<Integer> curProductCrop, int buildingId) {
//         this.player = player;
//         this.curProductCrop = curProductCrop;
//         this.buildingId = buildingId;
//     }
//
//     @Override
//     public int deadlineTime() {
//         return curProductCrop.get(2);
//     }
//
//     @Override
//     public void deadRun(int runTime, DelayInvokeEnvironment env) {
//         if (env instanceof BuildingService) {
//             BuildingService buildingService = (BuildingService) env;
//             buildingService.gainEconomicCrop(player, curProductCrop, buildingId);
//         }
//     }
// }
