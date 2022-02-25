package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBerlinJobRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @ClassName GetBerlinJobHandler.java
 * @Description 获取柏林官职
 * @author QiuKun
 * @date 2018年8月9日
 */
public class GetBerlinJobHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetBerlinJobRq req = msg.getExtension(GetBerlinJobRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        GetBerlinJobRs resp = berlinWarService.getBerlinJob(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetBerlinJobRs.ext, resp);
        }
    }

}
