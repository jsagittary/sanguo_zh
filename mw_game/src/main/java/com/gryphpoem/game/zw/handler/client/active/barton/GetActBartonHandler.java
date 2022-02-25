package com.gryphpoem.game.zw.handler.client.active.barton;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetActBartonRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetActBartonRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.activity.ActivityBartonService;

/**
 * Created by pengshuo on 2019/4/14 10:06
 * <br>Description: 巴顿活动玩家数据
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class GetActBartonHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetActBartonRq req = msg.getExtension(GetActBartonRq.ext);
        ActivityBartonService service = getService(ActivityBartonService.class);
        GetActBartonRs rs = service.getActBartonRs(req.getActivityId(),getRoleId(),req.getRefresh());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(GetActBartonRs.ext, rs);
        }
    }
}
