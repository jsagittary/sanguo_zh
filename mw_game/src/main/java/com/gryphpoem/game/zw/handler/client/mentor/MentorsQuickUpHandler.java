package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-30 16:56
 * @description: 教官升级
 * @modified By:
 */
public class MentorsQuickUpHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.MentorsQuickUpRq req = msg.getExtension(GamePb4.MentorsQuickUpRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.MentorsQuickUpRs resp = service.mentorsQuickUp(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.MentorsQuickUpRs.ext, resp);
        }
    }
}
