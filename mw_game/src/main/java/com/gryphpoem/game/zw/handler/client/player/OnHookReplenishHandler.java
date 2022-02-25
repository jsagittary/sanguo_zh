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
public class OnHookReplenishHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.OnHookReplenishRq req = msg.getExtension(GamePb4.OnHookReplenishRq.ext);
        GamePb4.OnHookReplenishRs resp = getService(PlayerService.class).onHookReplenishRs(getRoleId(),req.getArmyType());
        sendMsgToPlayer(GamePb4.OnHookReplenishRs.EXT_FIELD_NUMBER, GamePb4.OnHookReplenishRs.ext, resp);
    }

}
