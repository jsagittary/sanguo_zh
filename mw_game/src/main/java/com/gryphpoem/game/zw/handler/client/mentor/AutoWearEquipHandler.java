package com.gryphpoem.game.zw.handler.client.mentor;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.MentorService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-03 10:48
 * @description: 自动穿戴装备
 * @modified By:
 */
public class AutoWearEquipHandler extends ClientHandler {

    @Override public void action() throws MwException {
        GamePb4.AutoWearEquipRq req = msg.getExtension(GamePb4.AutoWearEquipRq.ext);
        MentorService service = getService(MentorService.class);
        GamePb4.AutoWearEquipRs resp = service.autoWearEquip(getRoleId(), req.getType());

        if (null != resp) {
            sendMsgToPlayer(GamePb4.AutoWearEquipRs.ext, resp);
        }
    }
}
