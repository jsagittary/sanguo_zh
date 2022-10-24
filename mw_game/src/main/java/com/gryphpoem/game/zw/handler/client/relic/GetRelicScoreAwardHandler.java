package com.gryphpoem.game.zw.handler.client.relic;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.service.relic.RelicService;

/**
 *
 * @author xwind
 * @date 2022/8/2
 */
public class GetRelicScoreAwardHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb6.GetRelicScoreAwardRq req = msg.getExtension(GamePb6.GetRelicScoreAwardRq.ext);
        GamePb6.GetRelicScoreAwardRs resp = getService(RelicService.class).getRelicScoreAward(getRoleId(),req.getCfgId());
        sendMsgToPlayer(GamePb6.GetRelicScoreAwardRs.ext, resp);
    }
}
