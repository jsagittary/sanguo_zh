package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossAreaRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossAreaRs;

/**
 * @ClassName GetCrossAreaHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月3日
 */
public class GetCrossAreaHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossAreaRq req = msg.getExtension(GetCrossAreaRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossAreaRs resp = service.getCrossArea(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossAreaRs.ext, resp);
        }
    }

}
