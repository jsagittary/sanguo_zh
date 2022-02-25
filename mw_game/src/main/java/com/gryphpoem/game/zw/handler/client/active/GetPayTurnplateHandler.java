package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPayTurnplateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GetPayTurnplateHandler.java
 * @Description 获取充值转盘
 * @author QiuKun
 * @date 2018年6月22日
 */
public class GetPayTurnplateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetPayTurnplateRq req = msg.getExtension(GetPayTurnplateRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetPayTurnplateRs resp = service.getPayTurnplate(roleId);
        if (null != resp) {
            sendMsgToPlayer(GetPayTurnplateRs.ext, resp);
        }
    }

}
