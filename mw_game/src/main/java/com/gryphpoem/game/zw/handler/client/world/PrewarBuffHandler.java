package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.PrewarBuffRq;
import com.gryphpoem.game.zw.pb.GamePb4.PrewarBuffRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-09-07 20:09
 * @description: 战前Buff
 * @modified By:
 */
public class PrewarBuffHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PrewarBuffRq req = msg.getExtension(PrewarBuffRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        PrewarBuffRs resp = berlinWarService.prewarBuff(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(PrewarBuffRs.ext, resp);
        }
    }
}
