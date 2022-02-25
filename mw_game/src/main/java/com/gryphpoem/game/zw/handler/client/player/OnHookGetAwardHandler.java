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
public class OnHookGetAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.OnHookGetAwardRq req = msg.getExtension(GamePb4.OnHookGetAwardRq.ext);
        GamePb4.OnHookGetAwardRs resp = getService(PlayerService.class).onHookGetAwardRs(getRoleId());
        sendMsgToPlayer(GamePb4.OnHookGetAwardRs.EXT_FIELD_NUMBER, GamePb4.OnHookGetAwardRs.ext, resp);
    }

}
