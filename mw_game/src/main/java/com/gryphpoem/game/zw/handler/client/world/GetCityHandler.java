package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 获取单个城池信息
 * 
 * @author tyler
 *
 */
public class GetCityHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCityRq req = msg.getExtension(GetCityRq.ext);
        CityService cityService = getService(CityService.class);
        GetCityRs resp = cityService.getCity(getRoleId(), req.getCityId());
        if (null != resp) {
            sendMsgToPlayer(GetCityRs.ext, resp);
        }
    }

}
