package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBerlinWinnerListRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @ClassName GetBerlinWinnerListHandler.java
 * @Description 获取历届霸主
 * @author QiuKun
 * @date 2018年8月11日
 */
public class GetBerlinWinnerListHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetBerlinJobRq req = msg.getExtension(GetBerlinJobRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        GetBerlinWinnerListRs resp = berlinWarService.getBerlinWinnerList(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetBerlinWinnerListRs.ext, resp);
        }
    }

}
