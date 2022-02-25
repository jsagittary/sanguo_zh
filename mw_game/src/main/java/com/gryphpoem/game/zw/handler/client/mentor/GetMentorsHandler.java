package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-30 16:52
 * @description: 获取教官信息
 * @modified By:
 */
public class GetMentorsHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.GetMentorsRq req = msg.getExtension(GamePb4.GetMentorsRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.GetMentorsRs resp = service.getMentors(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetMentorsRs.ext, resp);
        }
    }
}
