package com.gryphpoem.game.zw.handler.client.newcross.crosswarfire;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientAsyncHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.warfire.WarFireScoreLocalService;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireRanksRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossWarFireRanksRs;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-20 19:27
 */
public class GetCrossWarFireRanksHandler extends ClientAsyncHandler {
    @Override
    public void action() throws Exception {
        GetCrossWarFireRanksRq req = msg.getExtension(GetCrossWarFireRanksRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        WarFireScoreLocalService service = DataResource.ac.getBean(WarFireScoreLocalService.class);
        service.getCrossWarFireRanks(player, req).whenComplete(this::complete);
    }

    @Override
    public <T> void sendMsgToPlayer(T rsp) {
        sendMsgToPlayer(GetCrossWarFireRanksRs.ext, (GetCrossWarFireRanksRs) rsp);
    }
}
