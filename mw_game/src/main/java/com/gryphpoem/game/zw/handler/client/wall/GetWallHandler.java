package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetWallRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetWallRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙
 * 
 * @author tyler
 *
 */
public class GetWallHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetWallRq req = msg.getExtension(GetWallRq.ext);
        GetWallRs resp = getService(WallService.class).getWall(getRoleId(),req);
        if (resp != null) sendMsgToPlayer(GetWallRs.EXT_FIELD_NUMBER, GetWallRs.ext, resp);
    }
}
