package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * User:        zhoujie
 * Date:        2020/4/7 16:23
 * Description:
 */
public class GetEasterActAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.GetEasterActAwardRq req = msg.getExtension(GamePb4.GetEasterActAwardRq.ext);
        GamePb4.GetEasterActAwardRs resp = getService(ActivityService.class).getEasterActAward(getRoleId(), req.getType(), req.getKeyId());
        sendMsgToPlayer(GamePb4.GetEasterActAwardRs.EXT_FIELD_NUMBER, GamePb4.GetEasterActAwardRs.ext, resp);
    }
}
