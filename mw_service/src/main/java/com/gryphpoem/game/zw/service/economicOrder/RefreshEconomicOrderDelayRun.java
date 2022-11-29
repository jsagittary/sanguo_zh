// package com.gryphpoem.game.zw.service.economicOrder;
//
// import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
// import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
// import com.gryphpoem.game.zw.resource.domain.Player;
//
// /**
//  * @Author: GeYuanpeng
//  * @Date: 2022/11/16 19:10
//  */
// public class RefreshEconomicOrderDelayRun implements DelayRun {
//
//     private Player player;
//
//     private int type; // 1-可提交订单; 2-预显示订单
//
//     private int keyId;
//
//     private int endTime;
//
//     public RefreshEconomicOrderDelayRun(Player player, int type, int keyId, int endTime) {
//         this.player = player;
//         this.type = type;
//         this.keyId = keyId;
//         this.endTime = endTime;
//     }
//
//     @Override
//     public int deadlineTime() {
//         return endTime;
//     }
//
//     @Override
//     public void deadRun(int runTime, DelayInvokeEnvironment env) {
//         if (env instanceof EconomicOrderService) {
//             EconomicOrderService economicOrderService = (EconomicOrderService)env;
//             economicOrderService.refreshEconomicOrder(player, type, keyId);
//         }
//     }
// }
