package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.CabinetLvFinishRs;
import com.gryphpoem.game.zw.service.WarFactoryService;

/**
 * @ClassName CabinetLvFinishHandler.java
 * @Description
 * @author QiuKun
 * @date 2017年7月20日
 */
public class CabinetLvFinishHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // CabinetLvFinishRq req = msg.getExtension(CabinetLvFinishRq.ext);
        WarFactoryService service = getService(WarFactoryService.class);
        CabinetLvFinishRs resp = service.cabinetLvFinish(getRoleId());
        if (null != resp) sendMsgToPlayer(CabinetLvFinishRs.ext, resp);
    }

}
