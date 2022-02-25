package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossAttackService;
import com.gryphpoem.game.zw.pb.GamePb5.*;

/**
 * @ClassName AttackCrossPosHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年4月3日
 */
public class AttackCrossPosHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AttackCrossPosRq req = msg.getExtension(AttackCrossPosRq.ext);
        CrossAttackService service = getService(CrossAttackService.class);
        AttackCrossPosRs resp = service.attackCrossPos(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(AttackCrossPosRs.ext, resp);
        }
    }

}
