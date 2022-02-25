package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMineRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMineRs;

/**
 * @ClassName GetCrossMineHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月3日
 */
public class GetCrossMineHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossMineRq req = msg.getExtension(GetCrossMineRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossMineRs resp = service.getCrossMine(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossMineRs.ext, resp);
        }
    }

}
