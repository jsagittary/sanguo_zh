package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.PlayMonopolyRq;
import com.gryphpoem.game.zw.pb.GamePb4.PlayMonopolyRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName PlayMonopolyHandler.java
 * @Description 大富翁摇色子
 * @author QiuKun
 * @date 2018年9月13日
 */
public class PlayMonopolyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PlayMonopolyRq req = msg.getExtension(PlayMonopolyRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        PlayMonopolyRs resp = service.playMonopoly(roleId, req);
        if (null != resp) {
            sendMsgToPlayer(PlayMonopolyRs.ext, resp);
        }
    }

}
