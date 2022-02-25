package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBerlinRankRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-25 19:29
 * @description: 获取柏林会战连续击杀排行榜数据
 * @modified By:
 */
public class BerlinRankInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        GetBerlinRankRs resp = berlinWarService.berlinRankInfo(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetBerlinRankRs.ext, resp);
        }
    }
}
