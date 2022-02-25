package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetDisplayActListRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-04-09 14:20
 * @Description: 获取所有状态在DISPLAY-OPEN阶段的活动
 * @Modified By:
 */
public class GetDisplayActListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetDisplayActListRs resp = getService(ActivityService.class).getDisplayActivityList(getRoleId());
        sendMsgToPlayer(GetDisplayActListRs.EXT_FIELD_NUMBER, GetDisplayActListRs.ext, resp);
    }
}
