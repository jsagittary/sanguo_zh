package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GiftShowRq;
import com.gryphpoem.game.zw.pb.GamePb3.GiftShowRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GiftShowHandler.java
 * @Description 礼包购买显示协议
 * @author QiuKun
 * @date 2017年8月14日
 */
public class GiftShowHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GiftShowRq req = msg.getExtension(GiftShowRq.ext);
        ActivityService service = getService(ActivityService.class);
        GiftShowRs resp = service.giftShow(req, getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GiftShowRs.ext, resp);
        }
    }

}
