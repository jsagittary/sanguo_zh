package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AppointBerlinJobRq;
import com.gryphpoem.game.zw.pb.GamePb4.AppointBerlinJobRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @ClassName AppointBerlinJob.java
 * @Description 任命柏林官职
 * @author QiuKun
 * @date 2018年8月9日
 */
public class AppointBerlinJobHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AppointBerlinJobRq req = msg.getExtension(AppointBerlinJobRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        AppointBerlinJobRs resp = berlinWarService.appointBerlinJob(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(AppointBerlinJobRs.ext, resp);
        }
    }

}
