package com.gryphpoem.game.zw.handler.client.royalarena;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.RoyalArenaService;

/**
 * User:        zhoujie
 * Date:        2020/4/3 19:29
 * Description:
 */
public class RoyalArenaSkillHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.RoyalArenaSkillRq req = msg.getExtension(GamePb4.RoyalArenaSkillRq.ext);

        RoyalArenaService service = getService(RoyalArenaService.class);
        GamePb4.RoyalArenaSkillRs resp = service.royalArenaSkill(getRoleId(), req.getType());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.RoyalArenaSkillRs.ext, resp);
        }
    }
}
