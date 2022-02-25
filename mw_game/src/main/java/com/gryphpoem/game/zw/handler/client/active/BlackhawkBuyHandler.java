package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkBuyRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName BlackhawkBuyHandler.java
 * @Description 购买黑鹰计划物品
 * @author QiuKun
 * @date 2017年7月10日
 */
public class BlackhawkBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BlackhawkBuyRq req = msg.getExtension(BlackhawkBuyRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        BlackhawkBuyRs resp = service.blackhawkBuy(roleId, req.getKeyId());
        if (null != resp) {
            sendMsgToPlayer(BlackhawkBuyRs.ext, resp);
        }
     }

}
