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
public class SeasonGetInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SeasonService service = getService(SeasonService.class);
        GamePb4.SeasonGetInfoRs resp = service.getSeasonInfo(getRoleId());
        if (resp != null) sendMsgToPlayer(GamePb4.SeasonGetInfoRs.EXT_FIELD_NUMBER, GamePb4.SeasonGetInfoRs.ext, resp);
    }
}
