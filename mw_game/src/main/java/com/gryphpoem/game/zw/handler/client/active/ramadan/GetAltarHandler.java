package com.gryphpoem.game.zw.handler.client.active.ramadan;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.RamadanVisitAltarService;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/7/21 14:57
 */
public class GetAltarHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.GetAltarRq req = msg.getExtension(GamePb4.GetAltarRq.ext);
        RamadanVisitAltarService service = DataResource.ac.getBean(RamadanVisitAltarService.class);
        GamePb4.GetAltarRs rsb = service.getAltar(req.getPos(), getRoleId());
        sendMsgToPlayer(GamePb4.GetAltarRs.EXT_FIELD_NUMBER, GamePb4.GetAltarRs.ext, rsb);
    }
}
