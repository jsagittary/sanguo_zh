package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.BerlinCityInfoRq;
import com.gryphpoem.game.zw.pb.GamePb4.BerlinCityInfoRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-25 14:08
 * @description: 获取指定柏林会战据点的详细信息
 * @modified By:
 */
public class BerlinCityInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BerlinCityInfoRq req = msg.getExtension(BerlinCityInfoRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        BerlinCityInfoRs resp = berlinWarService.berlinCityInfo(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(BerlinCityInfoRs.ext, resp);
        }
    }
}
