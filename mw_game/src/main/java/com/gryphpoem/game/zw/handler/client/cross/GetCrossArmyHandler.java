package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossArmyService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossArmyRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossArmyRs;

/**
 * @ClassName GetCrossArmyHandler.java
 * @Description 
 * @author QiuKun
 * @date 2019年4月3日
 */
public class GetCrossArmyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossArmyRq req = msg.getExtension(GetCrossArmyRq.ext);
        CrossArmyService service = getService(CrossArmyService.class);
        GetCrossArmyRs resp = service.getCrossArmy(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossArmyRs.ext, resp);
        }
    }

}
