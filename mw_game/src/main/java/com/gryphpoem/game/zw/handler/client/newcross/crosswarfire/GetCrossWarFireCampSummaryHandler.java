package com.gryphpoem.game.zw.handler.client.newcross.crosswarfire;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.warfire.WarFireScoreLocalService;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCampSummaryRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireCampSummaryRs;
import com.gryphpoem.game.zw.resource.domain.Player;

public class GetCrossWarFireCampSummaryHandler extends ClientAsyncHandler {

    @Override
    public void action() throws Exception {
        GetCrossWarFireCampSummaryRq req = msg.getExtension(GetCrossWarFireCampSummaryRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        WarFireScoreLocalService service = DataResource.ac.getBean(WarFireScoreLocalService.class);
        service.getAllCampSummary(player, req).whenComplete(this::complete);
    }

    @Override
    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(GetCrossWarFireCampSummaryRs.ext, (GetCrossWarFireCampSummaryRs) rsp);
    }
}
