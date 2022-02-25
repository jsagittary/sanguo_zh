package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 进阶分解宝石
 * @author: ZhouJie
 * @date: Create in 2019-03-23 17:28
 * @description:
 * @modified By:
 */
public class DoJewelImproveHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb1.DoJewelImproveRq req = msg.getExtension(GamePb1.DoJewelImproveRq.ext);
        EquipService service = getService(EquipService.class);
        GamePb1.DoJewelImproveRs resp = service.doJewelImprove(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb1.DoJewelImproveRs.ext, resp);
        }
    }
}
