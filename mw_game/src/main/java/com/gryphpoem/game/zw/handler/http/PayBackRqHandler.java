package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.HttpPb.PayBackRq;
import com.gryphpoem.game.zw.pb.HttpPb.PayConfirmRq;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PayService;

/**
 * 
 * @Description 订单支付成功（支付服务器发送到游戏服）
 * @author TanDonghai
 *
 */
public class PayBackRqHandler extends HttpHandler {

    @Override
    public void action() {
        final PayBackRq req = msg.getExtension(PayBackRq.ext);
        // PayDao payDao = AppGameServer.ac.getBean(PayDao.class);
        // Pay pay = payDao.selectPay(req.getPlatNo(), req.getOrderId());
        // if (pay != null) {
        // return;
        // }

        // pay = new Pay();
        // pay.setPlatNo(req.getPlatNo());
        // pay.setPlatId(req.getPlatId());
        // pay.setOrderId(req.getOrderId());
        // pay.setSerialId(req.getSerialId());
        // pay.setServerId(req.getServerId());
        // pay.setRoleId(req.getRoleId());
        // pay.setAmount(req.getAmount());
        // pay.setPayTime(new Date());
        // payDao.createPay(pay);

        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                long roleId = req.getRoleId();
                PayService payService = AppGameServer.ac.getBean(PayService.class);

                // 执行发货相关逻辑
                PayConfirmRq messge = payService.payBack(req, roleId);
                if (null != messge) {
                    messge = messge.toBuilder().setOrderId(req.getSerialId()).build();
                    // 发货成功，通知账号服
                    Base.Builder baseBuilder = PbHelper.createRqBase(PayConfirmRq.EXT_FIELD_NUMBER, null,
                            PayConfirmRq.ext, messge);
                    AppGameServer.getInstance().sendMsgToPublicPay(baseBuilder);
                }
            }
        }, DealType.MAIN);
    }

}
