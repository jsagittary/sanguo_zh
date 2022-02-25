package com.gryphpoem.game.zw.handler.client.newcross.crosswarfire;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.warfire.WarFireScoreLocalService;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCityOccupyRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCityOccupyRs;
import com.gryphpoem.game.zw.resource.domain.Player;

public class GetCrossWarFireCityOccupyHandler extends ClientAsyncHandler {

    @Override
    public void action() throws Exception {
        GetCrossWarFireCityOccupyRq req = msg.getExtension(GetCrossWarFireCityOccupyRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        WarFireScoreLocalService service = DataResource.ac.getBean(WarFireScoreLocalService.class);
        service.getCityOccupy(player, req).whenComplete(this::complete);
    }


    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(GetCrossWarFireCityOccupyRs.ext, (GetCrossWarFireCityOccupyRs)rsp);
    }
}
