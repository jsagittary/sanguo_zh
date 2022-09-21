package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.AcqHeroSetRq;
import com.gryphpoem.game.zw.pb.GamePb1.AcqHeroSetRs;
import com.gryphpoem.game.zw.service.WarFactoryService;

/**
 * @ClassName AcqHeroSetHandler.java
 * @Description 内阁采集将领布置
 * @author QiuKun
 * @date 2017年7月14日
 */
public class AcqHeroSetHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AcqHeroSetRq req = msg.getExtension(AcqHeroSetRq.ext);
        WarFactoryService service = getService(WarFactoryService.class);
        boolean swap = req.hasSwap() && req.getSwap();
        boolean swapTreasure = req.hasSwapTreasure() && req.getSwapTreasure();
        boolean swapMedal = req.hasSwapMedal() && req.getSwapMedal();
        AcqHeroSetRs resp = service.acqHeroSet(getRoleId(), req.getPos(), req.getHeroId(), req.getType(), swap, swapTreasure, swapMedal);
        if (null != resp) sendMsgToPlayer(AcqHeroSetRs.ext, resp);
    }

}
