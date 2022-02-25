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
public class OnHookOperateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.OnHookOperateRq req = msg.getExtension(GamePb4.OnHookOperateRq.ext);
        GamePb4.OnHookOperateRs resp = getService(PlayerService.class).onHookOperateRs(getRoleId(),req.getType(),req.getRebelLv(), req.getAnniNumberThreshold());
        sendMsgToPlayer(GamePb4.OnHookOperateRs.EXT_FIELD_NUMBER, GamePb4.OnHookOperateRs.ext, resp);
    }

}
