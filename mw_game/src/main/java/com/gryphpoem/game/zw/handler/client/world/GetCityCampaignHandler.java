package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityCampaignRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetCityCampaignRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * @ClassName GetCityCampaignHandler.java
 * @Description 获取城池的竞选信息
 * @author QiuKun
 * @date 2017年9月26日
 */
public class GetCityCampaignHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCityCampaignRq req = msg.getExtension(GetCityCampaignRq.ext);
        CityService cityService = getService(CityService.class);
        GetCityCampaignRs resp = cityService.getCityCampaign(getRoleId(), req.getCityId());
        if (null != resp) {
            sendMsgToPlayer(GetCityCampaignRs.ext, resp);
        }
    }

}
