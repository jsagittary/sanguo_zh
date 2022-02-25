package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ResumeImmediatelyRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-29 11:56
 * @description: 立即恢复复活CD
 * @modified By:
 */
public class ResumeImmediatelyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        ResumeImmediatelyRs resp = berlinWarService.resumeImmediately(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(ResumeImmediatelyRs.ext, resp);
        }
    }
}
