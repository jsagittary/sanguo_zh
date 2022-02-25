package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-01-02 11:01
 * @description: 获取特殊活动
 * @modified By:
 */
public class GetSpecialActHandler extends ClientHandler {

    @Override public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        GamePb4.GetSpecialActRs resp = service.getSpecialAct(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetSpecialActRs.EXT_FIELD_NUMBER, GamePb4.GetSpecialActRs.ext, resp);
        }
    }
}
