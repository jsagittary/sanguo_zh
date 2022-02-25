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
public class SeasonGetTreasuryAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SeasonService service = getService(SeasonService.class);
        GamePb4.SeasonGetTreasuryAwardRq req = this.msg.getExtension(GamePb4.SeasonGetTreasuryAwardRq.ext);
        GamePb4.SeasonGetTreasuryAwardRs resp = service.getTreasuryAward(getRoleId(),req);
        if (resp != null) sendMsgToPlayer(GamePb4.SeasonGetTreasuryAwardRs.EXT_FIELD_NUMBER, GamePb4.SeasonGetTreasuryAwardRs.ext, resp);
    }
}
