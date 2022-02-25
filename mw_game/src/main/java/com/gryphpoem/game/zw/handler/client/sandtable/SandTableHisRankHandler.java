package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableHisRankHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableHisRankRs resp = getService(SandTableContestService.class).getHisRank(getRoleId());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableHisRankRs.ext,resp);
        }
    }
}
