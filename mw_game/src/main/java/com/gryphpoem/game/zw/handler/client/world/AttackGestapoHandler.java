package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AttackGestapoRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackGestapoRs;
import com.gryphpoem.game.zw.service.GestapoService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-29 11:02
 * @Description: 对盖世太保发起进攻
 * @Modified By:
 */
public class AttackGestapoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AttackGestapoRq req = msg.getExtension(AttackGestapoRq.ext);
        GestapoService worldService = getService(GestapoService.class);
        AttackGestapoRs resp = worldService.AttackGestapo(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(AttackGestapoRs.ext, resp);
        }
    }
}
