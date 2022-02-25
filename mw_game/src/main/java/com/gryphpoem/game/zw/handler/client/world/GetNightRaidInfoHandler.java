package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetNightRaidInfoRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName GetNightRaidInfoHandler.java
 * @Description 获取夜袭活动相关信息
 * @author QiuKun
 * @date 2018年3月1日
 */
public class GetNightRaidInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService service = getService(WorldService.class);
        GetNightRaidInfoRs resp = service.getNightRaidInfo(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GetNightRaidInfoRs.ext, resp);
        }
    }

}
