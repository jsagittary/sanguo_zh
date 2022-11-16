package com.gryphpoem.game.zw.handler.client.economicOrder;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.economicOrder.EconomicOrderService;

/**
 * 提交经济订单
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/16 20:15
 */
public class SubmitEconomicOrderHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.SubmitEconomicOrderRq rq = msg.getExtension(GamePb1.SubmitEconomicOrderRq.ext);
        EconomicOrderService economicOrderService = getService(EconomicOrderService.class);
        GamePb1.SubmitEconomicOrderRs resp = economicOrderService.submitEconomicOrder(getRoleId(), rq);
        if (null != resp) {
            sendMsgToPlayer(GamePb1.SubmitEconomicOrderRs.ext, resp);
        }
    }

}
