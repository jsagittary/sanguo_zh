package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetActBlackhawkRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GetActBlackhawkHandler.java
 * @Description 获取黑鹰计划活动
 * @author QiuKun
 * @date 2017年7月10日
 */
public class GetActBlackhawkHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
//        GetActBlackhawkRq req = msg.getExtension(GetActBlackhawkRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetActBlackhawkRs resp = service.getActBlackhawk(roleId);
        if (null != resp) {
            sendMsgToPlayer(GetActBlackhawkRs.ext, resp);
        }
    }

}
