package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableEnrollHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableEnrollRq req = this.msg.getExtension(GamePb4.SandTableEnrollRq.ext);
        GamePb4.SandTableEnrollRs resp = getService(SandTableContestService.class).enroll(getRoleId(),req.getLine(),req.getHeroIdList());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableEnrollRs.ext,resp);
        }
    }
}
