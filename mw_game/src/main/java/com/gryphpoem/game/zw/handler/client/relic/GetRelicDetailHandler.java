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
public class GetRelicDetailHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb6.GetRelicDetailRq req = msg.getExtension(GamePb6.GetRelicDetailRq.ext);
        GamePb6.GetRelicDetailRs resp = getService(RelicService.class).getRelicDetail(getRoleId(),req.getPos());
        sendMsgToPlayer(GamePb6.GetRelicDetailRs.ext, resp);
    }
}
