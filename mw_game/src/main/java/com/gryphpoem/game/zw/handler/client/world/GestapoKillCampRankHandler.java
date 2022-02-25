package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GestapoKillCampRankRq;
import com.gryphpoem.game.zw.pb.GamePb4.GestapoKillCampRankRs;
import com.gryphpoem.game.zw.service.GestapoService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-24 17:44
 * @description:
 * @modified By:
 */
public class GestapoKillCampRankHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GestapoKillCampRankRq req = msg.getExtension(GestapoKillCampRankRq.ext);
        GestapoService service = getService(GestapoService.class);
        GestapoKillCampRankRs resp = service.getGestapoKillRank(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GestapoKillCampRankRs.ext, resp);
        }
    }
}
