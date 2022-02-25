package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ExchangeActAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.ExchangeActAwardRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-30 20:22
 * @Description: 兑换奖励
 * @Modified By:
 */
public class ExchangeActAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ExchangeActAwardRq req = msg.getExtension(ExchangeActAwardRq.ext);
        ExchangeActAwardRs resp = getService(ActivityService.class).exchangeActAward(getRoleId(), req.getKeyId());
        sendMsgToPlayer(ExchangeActAwardRs.EXT_FIELD_NUMBER, ExchangeActAwardRs.ext, resp);
    }
}
