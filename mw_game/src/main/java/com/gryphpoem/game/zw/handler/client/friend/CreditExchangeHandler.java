package com.gryphpoem.game.zw.handler.client.friend;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.CreditExchangeRq;
import com.gryphpoem.game.zw.pb.GamePb4.CreditExchangeRs;
import com.gryphpoem.game.zw.service.FriendService;

/**
 * @ClassName CreditExchangeHandler.java
 * @Description 积分兑换
 * @author QiuKun
 * @date 2017年7月4日
 */
public class CreditExchangeHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CreditExchangeRq req = msg.getExtension(CreditExchangeRq.ext);
        FriendService service = getService(FriendService.class);
        long roleId = getRoleId();
        CreditExchangeRs resp = service.creditExchange(roleId, req.getProductId());

        if (null != resp) {
            sendMsgToPlayer(CreditExchangeRs.ext, resp);
        }
    }

}
