package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-26 16:51
 * @description: 教官专业技能激活
 * @modified By:
 */
public class MentorActivateHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.MentorActivateRq req = msg.getExtension(GamePb4.MentorActivateRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.MentorActivateRs resp = service.planeActivate(getRoleId(), req.getType());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.MentorActivateRs.ext, resp);
        }
    }
}
