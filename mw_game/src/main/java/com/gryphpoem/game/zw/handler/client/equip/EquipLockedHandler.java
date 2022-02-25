package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/10/25 16:31
 */
public class EquipLockedHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb2.EquipLockedRq req = msg.getExtension(GamePb2.EquipLockedRq.ext);
        EquipService equipService = getService(EquipService.class);
        GamePb2.EquipLockedRs resp = equipService.equipLocked(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GamePb2.EquipLockedRs.ext, resp);
        }
    }
}

