package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBerlinIntegralRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-25 19:56
 * @description: 获取累积击杀数据
 * @modified By:
 */
public class BerlinIntegralHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        GetBerlinIntegralRs resp = berlinWarService.berlinIntegral(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetBerlinIntegralRs.ext, resp);
        }
    }
}
