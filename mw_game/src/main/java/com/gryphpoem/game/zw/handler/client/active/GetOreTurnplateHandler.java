package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetOreTurnplateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 
* @ClassName: GetOreTurnplateHandler
* @Description: 获取矿石转盘
* @author chenqi
* @date 2018年8月18日
*
 */
public class GetOreTurnplateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetOreTurnplateRs resp = service.getOreTurnplate(roleId);
        if (null != resp) {
            sendMsgToPlayer(GetOreTurnplateRs.ext, resp);
        }
    }

}
