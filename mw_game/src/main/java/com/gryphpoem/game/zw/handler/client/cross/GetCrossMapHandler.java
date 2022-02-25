package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMapRs;

/**
 * @ClassName GetCrossMapHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月3日
 */
public class GetCrossMapHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossMapRq req = msg.getExtension(GetCrossMapRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossMapRs resp = service.getCrossMap(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossMapRs.ext, resp);
        }
    }

}
