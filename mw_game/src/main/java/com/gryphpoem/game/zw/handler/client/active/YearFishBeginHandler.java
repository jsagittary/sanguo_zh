package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.activity.Year2022FishService;

public class YearFishBeginHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb5.YearFishBeginRq req = this.msg.getExtension(GamePb5.YearFishBeginRq.ext);
        GamePb5.YearFishBeginRs resp = getService(Year2022FishService.class).beginFishing(getRoleId(),req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb5.YearFishBeginRs.ext, resp);
        }
    }
}
