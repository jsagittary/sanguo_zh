package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableHisContestHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableHisContestRs resp = getService(SandTableContestService.class).getHisContest(getRoleId());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableHisContestRs.ext,resp);
        }
    }
}
