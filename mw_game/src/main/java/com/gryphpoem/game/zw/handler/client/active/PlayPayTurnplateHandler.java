package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PlayPayTurnplateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName PlayPayTurnplateHandler.java
 * @Description 充值转盘抽奖
 * @author QiuKun
 * @date 2018年6月22日
 */
public class PlayPayTurnplateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        PlayPayTurnplateRs resp = service.playPayTurnplate(roleId);
        if (null != resp) {
            sendMsgToPlayer(PlayPayTurnplateRs.ext, resp);
        }
    }

}
