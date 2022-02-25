package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.CreateLeadRs;
import com.gryphpoem.game.zw.service.WarFactoryService;

/**
 * @ClassName CreateLeadHandler.java
 * @Description 内阁创建点兵统领
 * @author QiuKun
 * @date 2017年7月15日
 */
public class CreateLeadHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
//        CreateLeadRq req = msg.getExtension(CreateLeadRq.ext);
        WarFactoryService service = getService(WarFactoryService.class);
        CreateLeadRs resp = service.createLead(getRoleId());
        if (null != resp) sendMsgToPlayer(CreateLeadRs.ext, resp);
    }

}
