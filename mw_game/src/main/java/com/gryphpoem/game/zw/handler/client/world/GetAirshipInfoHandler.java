package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipInfoRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipInfoRs;
import com.gryphpoem.game.zw.service.AirshipService;

/**
 * @ClassName GetAirshipInfoHandler.java
 * @Description 获取飞艇活动信息
 * @author QiuKun
 * @date 2019年1月21日
 */
public class GetAirshipInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetAirshipInfoRq req = msg.getExtension(GetAirshipInfoRq.ext);
        AirshipService service = getService(AirshipService.class);
        // GetAirshipInfoRs resp = service.getAirshipInfo(getRoleId(), req);

        // if (null != resp) {
        //     sendMsgToPlayer(GetAirshipInfoRs.ext, resp);
        // }
    }

}
