package com.gryphpoem.game.zw.handler.client.royalarena;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.RoyalArenaService;

/**
 * User:        zhoujie
 * Date:        2020/4/3 19:34
 * Description:
 */
public class RoyalArenaTaskHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.RoyalArenaTaskRq req = msg.getExtension(GamePb4.RoyalArenaTaskRq.ext);

        RoyalArenaService service = getService(RoyalArenaService.class);
        GamePb4.RoyalArenaTaskRs resp = service.royalArenaTask(getRoleId(), req.getType(), req.getUseGold());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.RoyalArenaTaskRs.ext, resp);
        }
    }
}
