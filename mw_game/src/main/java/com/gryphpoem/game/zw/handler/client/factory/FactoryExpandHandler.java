package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.FactoryExpandRq;
import com.gryphpoem.game.zw.pb.GamePb1.FactoryExpandRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 兵营扩建
 * 
 * @author tyler
 *
 */
public class FactoryExpandHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        FactoryExpandRq req = msg.getExtension(FactoryExpandRq.ext);
        FactoryService fctoryService = getService(FactoryService.class);
        FactoryExpandRs resp = fctoryService.getFactoryExpandRs(getRoleId(), req.getId());
        if (resp != null) sendMsgToPlayer(FactoryExpandRs.EXT_FIELD_NUMBER, FactoryExpandRs.ext, resp);
    }
}
