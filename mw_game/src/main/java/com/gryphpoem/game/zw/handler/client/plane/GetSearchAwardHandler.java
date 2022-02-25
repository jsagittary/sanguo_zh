package com.gryphpoem.game.zw.handler.client.plane;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetSearchAwardRs;
import com.gryphpoem.game.zw.service.WarPlaneService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-19 18:42
 * @description: 获取寻访奖励
 * @modified By:
 */
public class GetSearchAwardHandler extends ClientHandler {

    @Override public void action() throws MwException {
        WarPlaneService service = getService(WarPlaneService.class);
        GetSearchAwardRs resp = service.getSearchAward(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetSearchAwardRs.ext, resp);
        }
    }
}
