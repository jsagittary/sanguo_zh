package com.gryphpoem.game.zw.handler.client.newcross.crosswarfire;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.warfire.WarFireScoreLocalService;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb6.RefreshGetCrossWarFirePlayerInfoRq;
import com.gryphpoem.game.zw.pb.GamePb6.RefreshGetCrossWarFirePlayerInfoRs;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-29 22:07
 */
public class RefreshGetPlayerInfoHandler extends ClientAsyncHandler {
    @Override
    public void action() throws Exception {
        RefreshGetCrossWarFirePlayerInfoRq req = msg.getExtension(RefreshGetCrossWarFirePlayerInfoRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        WarFireScoreLocalService service = DataResource.ac.getBean(WarFireScoreLocalService.class);
        service.refreshPlayerInfo(player, req).whenComplete(super::complete);
    }


    @Override
    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(RefreshGetCrossWarFirePlayerInfoRs.ext, (RefreshGetCrossWarFirePlayerInfoRs) rsp);
    }
}
