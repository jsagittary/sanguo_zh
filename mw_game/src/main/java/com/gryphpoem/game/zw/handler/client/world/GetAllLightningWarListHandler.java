package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.LightningWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-17 15:39
 * @description: 获取玩家当前区域的闪电战信息
 * @modified By:
 */
public class GetAllLightningWarListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        LightningWarService service = getService(LightningWarService.class);
        GamePb4.GetAllLightningWarListRs resp = service.getAllLightningWarList();

        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetAllLightningWarListRs.ext, resp);
        }
    }
}
