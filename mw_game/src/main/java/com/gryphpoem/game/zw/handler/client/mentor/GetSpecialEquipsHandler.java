package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-30 16:54
 * @description: 获取装备信息
 * @modified By:
 */
public class GetSpecialEquipsHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.GetSpecialEquipsRq req = msg.getExtension(GamePb4.GetSpecialEquipsRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.GetSpecialEquipsRs resp = service.getSpecialEquips(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetSpecialEquipsRs.ext, resp);
        }
    }
}
