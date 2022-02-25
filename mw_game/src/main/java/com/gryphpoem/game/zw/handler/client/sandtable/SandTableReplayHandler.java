package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableReplayHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableReplayRq req = this.msg.getExtension(GamePb4.SandTableReplayRq.ext);
        GamePb4.SandTableReplayRs resp = getService(SandTableContestService.class).getReplay(this.getRoleId(),req.getHisDate(),req.getRound());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableReplayRs.ext,resp);
        }
    }
}
