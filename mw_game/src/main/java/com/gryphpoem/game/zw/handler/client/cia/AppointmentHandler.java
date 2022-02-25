package com.gryphpoem.game.zw.handler.client.cia;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AppointmentAgentRq;
import com.gryphpoem.game.zw.pb.GamePb4.AppointmentAgentRs;
import com.gryphpoem.game.zw.service.CiaService;

/**
 * @ClassName AppointmentHandler.java
 * @Description 约会
 * @author shi.pei
 * @date 2020年7月20日
 */
public class AppointmentHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AppointmentAgentRq req = msg.getExtension(AppointmentAgentRq.ext);
        CiaService service = getService(CiaService.class);
        AppointmentAgentRs resp = service.appointmentAgent(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(AppointmentAgentRs.ext, resp);
        }
    }

}
