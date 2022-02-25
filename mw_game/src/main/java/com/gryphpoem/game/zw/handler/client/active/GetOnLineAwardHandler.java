package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetOnLineAwardRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetOnLineAwardRq;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-01-18 11:54
 * @Description: 领取在线奖励
 * @Modified By:
 */
public class GetOnLineAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetOnLineAwardRq req = msg.getExtension(GetOnLineAwardRq.ext);
        GetOnLineAwardRs resp = getService(ActivityService.class).getOnLineAward(req, getRoleId());
        sendMsgToPlayer(GetOnLineAwardRs.EXT_FIELD_NUMBER, GetOnLineAwardRs.ext, resp);
    }
}
