package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.TreasureWareService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangdh
 * createTime: 2022-03-02 18:07
 */
public class TreasureWareTrainHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.TreasureWareTrainRq req = msg.getExtension(GamePb4.TreasureWareTrainRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        TreasureWareService service = DataResource.ac.getBean(TreasureWareService.class);
        GamePb4.TreasureWareTrainRs rsp = service.trainTreasureWare(player, req);
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb4.TreasureWareTrainRs.ext, rsp);
        }
    }
}
