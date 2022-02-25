package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapInfoRs;

/**
 * @author: ZhouJie
 * @date: Create in 2019-04-04 11:37
 * @description:
 * @modified By:
 */
public class GetCrossMapInfoHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GetCrossMapInfoRq req = msg.getExtension(GetCrossMapInfoRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossMapInfoRs resp = service.getCrossMapInfo(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossMapInfoRs.ext, resp);
        }
    }
}
