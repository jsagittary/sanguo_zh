package com.gryphpoem.game.zw.handler.client.rebellion;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.BuyRebelBuffRq;
import com.gryphpoem.game.zw.pb.GamePb4.BuyRebelBuffRs;
import com.gryphpoem.game.zw.service.RebelService;

/**
 * @ClassName BuyRebelBuffHandler.java
 * @Description 购买匪军叛乱buff
 * @author QiuKun
 * @date 2018年10月29日
 */
public class BuyRebelBuffHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyRebelBuffRq req = msg.getExtension(BuyRebelBuffRq.ext);
        BuyRebelBuffRs resp = getService(RebelService.class).buyRebelBuff(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(BuyRebelBuffRs.EXT_FIELD_NUMBER, BuyRebelBuffRs.ext, resp);
    }

}
