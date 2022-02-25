package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetActRankRq;
import com.gryphpoem.game.zw.pb.GamePb3.GetActRankRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GetActRankHandler.java
 * @Description 获取排行活动
 * @author QiuKun
 * @date 2017年11月15日
 */
public class GetActRankHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetActRankRq req = msg.getExtension(GetActRankRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetActRankRs resp = service.getActRank(roleId, req.getActivityType());
        if (null != resp) {
            sendMsgToPlayer(GetActRankRs.ext, resp);
        }
    }

}
