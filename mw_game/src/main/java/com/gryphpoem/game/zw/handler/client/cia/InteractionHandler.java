package com.gryphpoem.game.zw.handler.client.cia;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.InteractionRq;
import com.gryphpoem.game.zw.pb.GamePb4.InteractionRs;
import com.gryphpoem.game.zw.service.CiaService;

/**
 * @ClassName InteractionHandler.java
 * @Description
 * @author QiuKun
 * @date 2018年6月6日
 */
public class InteractionHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        InteractionRq req = msg.getExtension(InteractionRq.ext);
        CiaService service = getService(CiaService.class);
        InteractionRs resp = service.interaction(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(InteractionRs.ext, resp);
        }
    }

}
