package com.gryphpoem.game.zw.handler.client.economicOrder;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.economicOrder.EconomicOrderService;

/**
 * 请求订单信息
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/17 18:48
 */
public class GetEconomicOrderHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.GetEconomicOrderRq rq = msg.getExtension(GamePb1.GetEconomicOrderRq.ext);
        EconomicOrderService economicOrderService = getService(EconomicOrderService.class);
        GamePb1.GetEconomicOrderRs resp = economicOrderService.getEconomicOrder(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GamePb1.GetEconomicOrderRs.ext, resp);
        }
    }

}
