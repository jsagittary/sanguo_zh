package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.WishingRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName WishingHandler.java
 * @Description 许愿池许愿
 * @author QiuKun
 * @date 2018年12月17日
 */
public class WishingHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        WishingRs resp = service.wishing(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(WishingRs.EXT_FIELD_NUMBER, WishingRs.ext, resp);
        }
    }

}
