package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-30 16:57
 * @description: 教官技能升级
 * @modified By:
 */
public class MentorsSkillUpHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.MentorsSkillUpRq req = msg.getExtension(GamePb4.MentorsSkillUpRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.MentorsSkillUpRs resp = service.mentorsSkillUp(getRoleId(), req.getType());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.MentorsSkillUpRs.ext, resp);
        }
    }
}
