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
public class EquipBatchDecomposeHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb2.EquipBatchDecomposeRq req = msg.getExtension(GamePb2.EquipBatchDecomposeRq.ext);
        EquipService equipService = getService(EquipService.class);
        GamePb2.EquipBatchDecomposeRs resp = equipService.equipBatchDecompose(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GamePb2.EquipBatchDecomposeRs.ext, resp);
        }
    }
}
