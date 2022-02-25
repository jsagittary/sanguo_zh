package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetAreaCentreCityRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName GetAreaCentreCityHandler.java
 * @Description 获取每个区域中心城池状态
 * @author QiuKun
 * @date 2018年3月31日
 */
public class GetAreaCentreCityHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService worldService = getService(WorldService.class);
        GetAreaCentreCityRs resp = worldService.getAreaCentreCity(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GetAreaCentreCityRs.ext, resp);
        }
    }
}