package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.GetChristmasInfoRs;
import com.gryphpoem.game.zw.service.activity.ActivityChristmasService;

public class ChristmasGetInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.GetChristmasInfoRq req = this.msg.getExtension(GamePb4.GetChristmasInfoRq.ext);
        GetChristmasInfoRs resp = getService(ActivityChristmasService.class).getInfo(getRoleId(),req.getActivityType());
        if(resp != null){
            sendMsgToPlayer(GetChristmasInfoRs.ext, resp);
        }
    }
}
