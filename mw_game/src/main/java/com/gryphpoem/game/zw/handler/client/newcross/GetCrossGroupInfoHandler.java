package com.gryphpoem.game.zw.handler.client.newcross;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.pb.GamePb6;

import java.util.Objects;

public class GetCrossGroupInfoHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb6.GetCrossGroupInfoRq req = msg.getExtension(GamePb6.GetCrossGroupInfoRq.ext);
        GamePb6.GetCrossGroupInfoRs resp = DataResource.getBean(CrossGamePlayService.class).getCrossGroupInfo(getRoleId(), req.getFunctionId());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb6.GetCrossGroupInfoRs.ext, resp);
        }
    }
}
