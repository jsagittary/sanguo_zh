package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.WarFactoryService;

/**
 * @author: ZhouJie
 * @date: Create in 2019-05-20 15:58
 * @description: 内阁特将领布置
 * @modified By:
 */
public class CommandoHeroSetHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb1.ComandoHeroSetRq req = msg.getExtension(GamePb1.ComandoHeroSetRq.ext);
        WarFactoryService service = getService(WarFactoryService.class);
        boolean swap = req.hasSwap() ? req.getSwap() : false;
        boolean swapPlane = req.hasSwapPlane() ? req.getSwapPlane() : false;
        GamePb1.ComandoHeroSetRs resp = service.commandoHeroSet(getRoleId(), req.getPos(), req.getHeroId(), req.getType(), swap, swapPlane);
        if (null != resp) sendMsgToPlayer(GamePb1.ComandoHeroSetRs.ext, resp);
    }
}
