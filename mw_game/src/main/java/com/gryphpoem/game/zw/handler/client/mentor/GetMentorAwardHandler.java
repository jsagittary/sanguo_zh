package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-01 14:53
 * @description: 领取教官奖励
 * @modified By:
 */
public class GetMentorAwardHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.GetMentorAwardRq req = msg.getExtension(GamePb4.GetMentorAwardRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.GetMentorAwardRs resp = service.getMentorAward(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetMentorAwardRs.ext, resp);
        }
    }
}
