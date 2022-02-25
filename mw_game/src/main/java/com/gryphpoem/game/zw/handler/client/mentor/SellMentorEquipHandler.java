package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-03 15:08
 * @description: 贩卖教官装备
 * @modified By:
 */
public class SellMentorEquipHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.SellMentorEquipRq req = msg.getExtension(GamePb4.SellMentorEquipRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.SellMentorEquipRs resp = service.sellMentorEquip(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GamePb4.SellMentorEquipRs.ext, resp);
        }
    }
}
