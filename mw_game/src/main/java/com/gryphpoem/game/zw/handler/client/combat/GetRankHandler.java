package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRs;
import com.gryphpoem.game.zw.service.RankService;

/**
 * 排行榜
 * 
 * @author tyler
 *
 */
public class GetRankHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetRankRq req = msg.getExtension(GetRankRq.ext);
        GetRankRs resp = getService(RankService.class).getRank(getRoleId(), req);
        sendMsgToPlayer(GetRankRs.EXT_FIELD_NUMBER, GetRankRs.ext, resp);
    }
}
