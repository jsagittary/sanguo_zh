package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossCityService;
import com.gryphpoem.game.zw.pb.GamePb6;

public class GetNewCrossCityInfoHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetCrossWarFireCityInfoRq req = msg.getExtension(GamePb6.GetCrossWarFireCityInfoRq.ext);
        Game2CrossCityService service = getService(Game2CrossCityService.class);
        service.getNewCrossCityInfo(getRoleId(), req);
    }
}
