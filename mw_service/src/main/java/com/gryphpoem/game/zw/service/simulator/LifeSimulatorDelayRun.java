// package com.gryphpoem.game.zw.service.simulator;
//
// import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
// import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
// import com.gryphpoem.game.zw.resource.domain.Player;
// import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
// import com.gryphpoem.game.zw.resource.util.TimeHelper;
//
// import java.util.Date;
//
// /**
//  * @Author: GeYuanpeng
//  * @Date: 2022/11/9 17:50
//  */
// public class LifeSimulatorDelayRun implements DelayRun {
//
//     private LifeSimulatorInfo lifeSimulatorInfo;
//
//     private Player player;
//
//     public LifeSimulatorDelayRun(LifeSimulatorInfo lifeSimulatorInfo, Player player) {
//         this.lifeSimulatorInfo = lifeSimulatorInfo;
//         this.player = player;
//     }
//
//     @Override
//     public int deadlineTime() {
//         Integer pauseTime = lifeSimulatorInfo.getPauseTime();
//         Integer delay = lifeSimulatorInfo.getDelay();
//         Date delayDate = TimeHelper.getSomeDayAfterOrBerfore(TimeHelper.getDate(TimeHelper.getDay(pauseTime)), delay, 8, 0, 0);
//         return TimeHelper.dateToSecond(delayDate);
//     }
//
//     @Override
//     public void deadRun(int runTime, DelayInvokeEnvironment env) {
//         if (env instanceof LifeSimulatorService) {
//             LifeSimulatorService lifeSimulatorService = (LifeSimulatorService) env;
//             lifeSimulatorService.SyncNewSimulatorToPlayer(player, lifeSimulatorInfo, 4);
//         }
//     }
//
// }
