package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkRefreshRq;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkRefreshRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName BlackhawkRefreshHandler.java
 * @Description 黑鹰计划刷新
 * @author QiuKun
 * @date 2017年7月10日
 */
public class BlackhawkRefreshHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BlackhawkRefreshRq req = msg.getExtension(BlackhawkRefreshRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        BlackhawkRefreshRs resp = service.blackhawkRefresh(roleId, req.getIsPay());
        if (null != resp) {
            sendMsgToPlayer(BlackhawkRefreshRs.ext, resp);
        }
    }

}
