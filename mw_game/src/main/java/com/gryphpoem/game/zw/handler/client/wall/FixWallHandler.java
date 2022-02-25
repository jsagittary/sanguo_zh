package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.FixWallRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 修复城内城墙
 * 
 * @author qiukun
 * @date 2017年7月1日
 */
public class FixWallHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // FixWallRq req = msg.getExtension(FixWallRq.ext);
        WallService service = getService(WallService.class);
        FixWallRs resp = service.fixWall(getRoleId());
        if (resp != null) sendMsgToPlayer(FixWallRs.EXT_FIELD_NUMBER, FixWallRs.ext, resp);
    }
}
