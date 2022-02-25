package com.gryphpoem.game.zw.handler.client.wall;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.WallGetOutRq;
import com.gryphpoem.game.zw.pb.GamePb1.WallGetOutRs;
import com.gryphpoem.game.zw.service.WallService;

/**
 * 城墙驻防遣返
 * 
 * @author tyler
 *
 */
public class WallGetOutHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WallGetOutRq req = msg.getExtension(WallGetOutRq.ext);
        WallGetOutRs resp = getService(WallService.class).doWallGetOut(getRoleId(), req.getKeyId(), req.getRoleId());
        sendMsgToPlayer(WallGetOutRs.EXT_FIELD_NUMBER, WallGetOutRs.ext, resp);
    }
}
