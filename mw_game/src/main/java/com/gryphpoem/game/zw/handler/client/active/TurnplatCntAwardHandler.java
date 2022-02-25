package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * User:        zhoujie
 * Date:        2020/2/19 16:11
 * Description:
 */
public class TurnplatCntAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb3.TurnplatCntAwardRq req = msg.getExtension(GamePb3.TurnplatCntAwardRq.ext);
        ActivityService service = getService(ActivityService.class);
        GamePb3.TurnplatCntAwardRs res = service.turnplatCntAward(getRoleId(), req);
        if (res != null) sendMsgToPlayer(GamePb3.TurnplatCntAwardRs.ext, res);
    }
}
