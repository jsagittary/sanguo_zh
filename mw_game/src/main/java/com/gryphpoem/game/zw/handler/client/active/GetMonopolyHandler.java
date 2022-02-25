package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetMonopolyRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetMonopolyRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GetMonopolyHandler.java
 * @Description 大富翁获取
 * @author QiuKun
 * @date 2018年9月13日
 */
public class GetMonopolyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetMonopolyRq req = msg.getExtension(GetMonopolyRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        GetMonopolyRs resp = service.getMonopoly(roleId, req);
        if (null != resp) {
            sendMsgToPlayer(GetMonopolyRs.ext, resp);
        }
    }

}
