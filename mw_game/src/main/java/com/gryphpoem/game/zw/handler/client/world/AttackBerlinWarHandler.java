package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AttackBerlinWarRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackBerlinWarRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-26 17:40
 * @description: 加入柏林会战
 * @modified By:
 */
public class AttackBerlinWarHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AttackBerlinWarRq req = msg.getExtension(AttackBerlinWarRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        AttackBerlinWarRs resp = berlinWarService.attackBerlinWar(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(AttackBerlinWarRs.ext, resp);
        }
    }
}
