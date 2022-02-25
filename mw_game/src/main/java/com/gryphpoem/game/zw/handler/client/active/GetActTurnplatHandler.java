package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetActTurnplatRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActTurnplatRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-06-07 19:17
 * @description: 获取幸运转盘的信息
 * @modified By:
 */
public class GetActTurnplatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
    	GetActTurnplatRq req = msg.getExtension(GetActTurnplatRq.ext);
        ActivityService service = getService(ActivityService.class);
        GetActTurnplatRs res = service.getActTurnplat(getRoleId(),req.getActType());
        if (res != null) sendMsgToPlayer(GetActTurnplatRs.ext, res);
    }
}
