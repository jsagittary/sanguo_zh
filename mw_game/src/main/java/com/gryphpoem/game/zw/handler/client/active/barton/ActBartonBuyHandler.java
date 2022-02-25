package com.gryphpoem.game.zw.handler.client.active.barton;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ActBartonBuyRq;
import com.gryphpoem.game.zw.pb.GamePb4.ActBartonBuyRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.activity.ActivityBartonService;

/**
 * Created by pengshuo on 2019/4/14 10:09
 * <br>Description: 巴顿活动物品购买
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class ActBartonBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActBartonBuyRq req = msg.getExtension(ActBartonBuyRq.ext);
        ActivityBartonService service = getService(ActivityBartonService.class);
        ActBartonBuyRs rs = service.actBartonBuy(req.getActivityId(),getRoleId(),req.getKeyId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(ActBartonBuyRs.ext, rs);
        }
    }
}
