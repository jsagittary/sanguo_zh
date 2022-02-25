package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.GiftCodeRq;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 使用兑换码
 * @author TanDonghai
 *
 */
public class GiftCodeHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GiftCodeRq req = msg.getExtension(GiftCodeRq.ext);
        Base.Builder baseBuilder = getService(PlayerService.class).giftCode(getRoleId(), req.getCode());

        sendMsgToPublic(baseBuilder);
    }
}
