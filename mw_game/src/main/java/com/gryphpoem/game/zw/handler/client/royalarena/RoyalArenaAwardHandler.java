package com.gryphpoem.game.zw.handler.client.royalarena;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.RoyalArenaService;

/**
 * User:        zhoujie
 * Date:        2020/4/3 19:36
 * Description:
 */
public class RoyalArenaAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.RoyalArenaAwardRq req = msg.getExtension(GamePb4.RoyalArenaAwardRq.ext);

        RoyalArenaService service = getService(RoyalArenaService.class);
        GamePb4.RoyalArenaAwardRs resp = service.royalArenaAward(getRoleId(), req.getType(), req.getId());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.RoyalArenaAwardRs.ext, resp);
        }
    }
}
