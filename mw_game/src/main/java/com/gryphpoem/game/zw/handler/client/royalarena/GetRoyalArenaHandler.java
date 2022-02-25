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
public class GetRoyalArenaHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        RoyalArenaService service = getService(RoyalArenaService.class);
        GamePb4.GetRoyalArenaRs resp = service.getRoyalArena(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetRoyalArenaRs.ext, resp);
        }
    }
}
