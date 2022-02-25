package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * User:        zhoujie
 * Date:        2020/2/7 17:15
 * Description: 阵营城池征收
 */
public class GetCampLevyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService worldService = getService(WorldService.class);
        GamePb2.GetCampCityLevyRs resp = worldService.getCampLevy(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GamePb2.GetCampCityLevyRs.ext, resp);
        }
    }
}
