package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.PlayOreTurnplateRq;
import com.gryphpoem.game.zw.pb.GamePb4.PlayOreTurnplateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 
* @ClassName: PlayOreTurnplateHandler
* @Description: 矿石转盘抽奖
* @author chenqi
* @date 2018年8月18日
*
 */
public class PlayOreTurnplateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
    	PlayOreTurnplateRq req = msg.getExtension(PlayOreTurnplateRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        PlayOreTurnplateRs resp = service.playOreTurnplate(roleId,req.getNums());
        if (null != resp) {
            sendMsgToPlayer(PlayOreTurnplateRs.ext, resp);
        }
    }

}
