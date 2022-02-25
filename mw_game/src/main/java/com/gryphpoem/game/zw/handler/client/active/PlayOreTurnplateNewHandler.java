package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.PlayOreTurnplateNewRq;
import com.gryphpoem.game.zw.pb.GamePb4.PlayOreTurnplateNewRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 
* @ClassName: PlayOreTurnplateNewHandler
* @Description: 矿石转盘抽奖-新
* @author chenqi
* @date 2018年9月19日
*
 */
public class PlayOreTurnplateNewHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
    	PlayOreTurnplateNewRq req = msg.getExtension(PlayOreTurnplateNewRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        PlayOreTurnplateNewRs resp = service.playOreTurnplateNew(roleId,req.getNums());
        if (null != resp) {
            sendMsgToPlayer(PlayOreTurnplateNewRs.ext, resp);
        }
    }

}
