package com.gryphpoem.game.zw.handler.client.player;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 *
 * @author xwind
 * @date 2021/3/17
 */
public class OnHookGetInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.OnHookGetInfoRq req = msg.getExtension(GamePb4.OnHookGetInfoRq.ext);
        GamePb4.OnHookGetInfoRs resp = getService(PlayerService.class).onHookGetInfoRs(getRoleId());
        sendMsgToPlayer(GamePb4.OnHookGetInfoRs.EXT_FIELD_NUMBER, GamePb4.OnHookGetInfoRs.ext, resp);
    }

}
