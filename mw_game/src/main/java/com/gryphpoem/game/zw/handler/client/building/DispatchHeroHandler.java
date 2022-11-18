package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 委任武将
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/19 0:42
 */
public class DispatchHeroHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.DispatchHeroRq rq = msg.getExtension(GamePb1.DispatchHeroRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        GamePb1.DispatchHeroRs rs = buildingService.dispatchHero(getRoleId(), rq);
        sendMsgToPlayer(GamePb1.DispatchHeroRs.EXT_FIELD_NUMBER, GamePb1.DispatchHeroRs.ext, rs);
    }

}
