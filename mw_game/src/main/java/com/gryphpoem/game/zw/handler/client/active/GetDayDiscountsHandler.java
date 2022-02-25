package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetDayDiscountsRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GetDayDiscountsHandler.java
 * @Description 每日特惠信息
 * @author QiuKun
 * @date 2018年7月3日
 */
public class GetDayDiscountsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetDayDiscountsRs resp = service.getDayDiscounts(roleId);
        if (null != resp) {
            sendMsgToPlayer(GetDayDiscountsRs.ext, resp);
        }
    }

}
