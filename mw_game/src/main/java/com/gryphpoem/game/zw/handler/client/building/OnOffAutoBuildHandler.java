package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.OnOffAutoBuildRq;
import com.gryphpoem.game.zw.pb.GamePb4.OnOffAutoBuildRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * @ClassName OnOffAutoBuildHandler.java
 * @Description 开启,关闭自动建造
 * @author QiuKun
 * @date 2017年8月30日
 */
public class OnOffAutoBuildHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        OnOffAutoBuildRq req = msg.getExtension(OnOffAutoBuildRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        OnOffAutoBuildRs resp = buildingService.onOffAutoBuild(getRoleId(), req.getAutoBuildOnOff());
        if (resp != null) {
            sendMsgToPlayer(OnOffAutoBuildRs.EXT_FIELD_NUMBER, OnOffAutoBuildRs.ext, resp);
        }
    }

}
