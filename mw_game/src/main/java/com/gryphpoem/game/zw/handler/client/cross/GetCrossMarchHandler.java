package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMarchRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossMarchRs;

/**
 * @ClassName GetCrossMarchHandler.java
 * @Description 
 * @author QiuKun
 * @date 2019年4月3日
 */
public class GetCrossMarchHandler extends ClientHandler {

    @Override
    public void action() throws MwException {

        GetCrossMarchRq req = msg.getExtension(GetCrossMarchRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossMarchRs resp = service.getCrossMarch(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossMarchRs.ext, resp);
        }
    }

}
