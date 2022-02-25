package com.gryphpoem.game.zw.handler.client.equip;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.EquipService;

/**
 * 获取所有装备宝石
 * @author: ZhouJie
 * @date: Create in 2019-03-23 17:26
 * @description:
 * @modified By:
 */
public class GetJewelsHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb1.GetJewelsRq req = msg.getExtension(GamePb1.GetJewelsRq.ext);
        EquipService service = getService(EquipService.class);
        GamePb1.GetJewelsRs resp = service.getJewels(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GamePb1.GetJewelsRs.ext, resp);
        }
    }
}
