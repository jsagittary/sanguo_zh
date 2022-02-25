package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 戒指强化
 * @author: ZhouJie
 * @date: Create in 2019-03-23 17:21
 * @description:
 * @modified By:
 */
public class RingUpLvHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb1.RingUpLvRq req = msg.getExtension(GamePb1.RingUpLvRq.ext);
        EquipService service = getService(EquipService.class);
        GamePb1.RingUpLvRs resp = service.ringUpLv(getRoleId(), req.getKeyId());

        if (null != resp) {
            sendMsgToPlayer(GamePb1.RingUpLvRs.ext, resp);
        }
    }
}
