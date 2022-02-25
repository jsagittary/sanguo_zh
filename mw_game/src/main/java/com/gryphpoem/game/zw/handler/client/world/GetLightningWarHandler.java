package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.LightningWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-17 15:41
 * @description: 获取所有区域的闪电战信息
 * @modified By:
 */
public class GetLightningWarHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.GetLightningWarRq req = msg.getExtension(GamePb4.GetLightningWarRq.ext);
        LightningWarService service = getService(LightningWarService.class);
        GamePb4.GetLightningWarRs resp = service.getLightningWar(getRoleId(), req.getPos());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetLightningWarRs.ext, resp);
        }
    }
}
