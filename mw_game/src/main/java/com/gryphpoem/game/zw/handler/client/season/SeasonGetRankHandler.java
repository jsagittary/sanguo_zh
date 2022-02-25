package com.gryphpoem.game.zw.handler.client.season;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.session.SeasonService;

/**
 *
 * @author xwind
 * @date 2021/4/16
 */
public class SeasonGetRankHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SeasonService service = getService(SeasonService.class);
        GamePb4.SeasonGetRankRq req = this.msg.getExtension(GamePb4.SeasonGetRankRq.ext);
        GamePb4.SeasonGetRankRs resp = service.getSeasonRank(getRoleId(),req.getPage());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SeasonGetRankRs.EXT_FIELD_NUMBER, GamePb4.SeasonGetRankRs.ext,resp);
        }
    }
}
