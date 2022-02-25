package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossArmyService;
import com.gryphpoem.game.zw.pb.GamePb5.RetreatCrossRq;
import com.gryphpoem.game.zw.pb.GamePb5.RetreatCrossRs;

/**
 * @ClassName RetreatCrossHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月3日
 */
public class RetreatCrossHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        RetreatCrossRq req = msg.getExtension(RetreatCrossRq.ext);
        CrossArmyService service = getService(CrossArmyService.class);
        RetreatCrossRs resp = service.retreatCross(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(RetreatCrossRs.ext, resp);
        }
    }

}
