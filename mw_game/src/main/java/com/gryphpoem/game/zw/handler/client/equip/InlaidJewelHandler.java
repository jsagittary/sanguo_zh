package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 镶嵌卸下宝石
 * @author: ZhouJie
 * @date: Create in 2019-03-23 17:22
 * @description:
 * @modified By:
 */
public class InlaidJewelHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb1.InlaidJewelRq req = msg.getExtension(GamePb1.InlaidJewelRq.ext);
        EquipService service = getService(EquipService.class);
        GamePb1.InlaidJewelRs resp = service.inlaidJewel(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb1.InlaidJewelRs.ext, resp);
        }
    }
}
