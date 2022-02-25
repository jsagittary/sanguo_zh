package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.HelpShengYuService;

/**
 * xwind
 */
public class HelpShengYuGetAwardHandler extends ClientHandler
{
    @Override
    public void action() throws MwException
    {
        GamePb4.HelpShengYuGetAwardRq req = this.msg.getExtension(GamePb4.HelpShengYuGetAwardRq.ext);
        GamePb4.HelpShengYuGetAwardRs resp = getService(HelpShengYuService.class).getAward(getRoleId(),req.getActivityType(),req.getKeyId());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.HelpShengYuGetAwardRs.ext, resp);
        }
    }
}
