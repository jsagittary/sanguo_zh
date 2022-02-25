package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTablePlayerFightDetailHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTablePlayerFightDetailRq req = this.msg.getExtension(GamePb4.SandTablePlayerFightDetailRq.ext);
        GamePb4.SandTablePlayerFightDetailRs resp = getService(SandTableContestService.class).getPlayerFightDetail(this.getRoleId(), req.getHisDate(), req.getRound(), req.getOnlyId());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.SandTablePlayerFightDetailRs.ext, resp);
        }
    }
}
