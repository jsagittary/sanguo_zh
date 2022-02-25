package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossCityInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossCityInfoRs;

/**
 * 战火燎原城池详情
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-08 17:16
 */
public class GetCrossCityInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossCityInfoRq req = msg.getExtension(GetCrossCityInfoRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossCityInfoRs resp = service.getCrossCityInfo(getRoleId(), req.getCityId());
        if (null != resp) {
            sendMsgToPlayer(GetCrossCityInfoRs.ext, resp);
        }
    }

}
