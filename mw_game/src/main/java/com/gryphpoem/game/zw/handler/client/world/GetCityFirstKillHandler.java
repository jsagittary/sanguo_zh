package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetCityFirstKillRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-05-10 16:11
 * @Description: 获取城池首杀显示数据
 * @Modified By:
 */
public class GetCityFirstKillHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService worldService = getService(WorldService.class);
        GetCityFirstKillRs resp = worldService.getCityFirstKill(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GetCityFirstKillRs.ext, resp);
        }
    }
}
