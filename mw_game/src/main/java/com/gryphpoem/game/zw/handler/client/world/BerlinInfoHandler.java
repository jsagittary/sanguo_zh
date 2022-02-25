package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.BerlinInfoRq;
import com.gryphpoem.game.zw.pb.GamePb4.BerlinInfoRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-25 14:08
 * @description: 获取柏林会战信息
 * @modified By:
 */
public class BerlinInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BerlinInfoRq req = msg.getExtension(BerlinInfoRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        BerlinInfoRs resp = berlinWarService.berlinInfo(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(BerlinInfoRs.ext, resp);
        }
    }
}
