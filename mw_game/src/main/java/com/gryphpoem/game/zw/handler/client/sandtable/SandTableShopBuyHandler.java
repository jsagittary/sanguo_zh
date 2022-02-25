package com.gryphpoem.game.zw.handler.client.sandtable;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

public class SandTableShopBuyHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SandTableShopBuyRq req = this.msg.getExtension(GamePb4.SandTableShopBuyRq.ext);
        GamePb4.SandTableShopBuyRs resp = getService(SandTableContestService.class).buy(this.getRoleId(),req.getConfId());
        if(resp != null){
            sendMsgToPlayer(GamePb4.SandTableShopBuyRs.ext,resp);
        }
    }
}
