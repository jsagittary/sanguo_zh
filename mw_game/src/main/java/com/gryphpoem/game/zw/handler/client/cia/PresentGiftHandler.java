package com.gryphpoem.game.zw.handler.client.cia;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.PresentGiftRq;
import com.gryphpoem.game.zw.pb.GamePb4.PresentGiftRs;
import com.gryphpoem.game.zw.service.CiaService;

/**
 * @ClassName PresentGiftHandler.java
 * @Description 送礼
 * @author QiuKun
 * @date 2018年6月6日
 */
public class PresentGiftHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PresentGiftRq req = msg.getExtension(PresentGiftRq.ext);
        CiaService service = getService(CiaService.class);
        PresentGiftRs resp = service.presentGift(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(PresentGiftRs.ext, resp);
        }
    }

}
