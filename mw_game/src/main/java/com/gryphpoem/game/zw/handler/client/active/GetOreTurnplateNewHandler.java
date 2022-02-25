package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetOreTurnplateNewRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 
* @ClassName: GetOreTurnplateNewHandler
* @Description: 获取矿石转盘-新
* @author chenqi
* @date 2018年9月19日
*
 */
public class GetOreTurnplateNewHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetOreTurnplateNewRs resp = service.getOreTurnplateNew(roleId);
        if (null != resp) {
            sendMsgToPlayer(GetOreTurnplateNewRs.ext, resp);
        }
    }

}
