package com.gryphpoem.game.zw.handler.client.newcross.crosswarfire;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.warfire.WarFireScoreLocalService;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb6.BuyCrossWarFireBuffRq;
import com.gryphpoem.game.zw.pb.GamePb6.BuyCrossWarFireBuffRs;
import com.gryphpoem.game.zw.resource.domain.Player;

import java.util.Objects;

public class BuyCrossWarFireBuffHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        BuyCrossWarFireBuffRq req = msg.getExtension(BuyCrossWarFireBuffRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        WarFireScoreLocalService service = DataResource.ac.getBean(WarFireScoreLocalService.class);
        BuyCrossWarFireBuffRs rsp = service.buyCrossWarFireBuff(player, req);
        if (Objects.nonNull(rsp)){
            sendMsgToPlayer(BuyCrossWarFireBuffRs.ext, rsp);
        }
    }
}
