package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipListRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipListRs;
import com.gryphpoem.game.zw.service.AirshipService;

/**
 * @ClassName GetAirshipListHandler.java
 * @Description 获取飞艇列表
 * @author QiuKun
 * @date 2019年1月21日
 */
public class GetAirshipListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetAirshipListRq req = msg.getExtension(GetAirshipListRq.ext);
        AirshipService service = getService(AirshipService.class);
        GetAirshipListRs resp = service.getAirshipList(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GetAirshipListRs.ext, resp);
        }
    }

}
