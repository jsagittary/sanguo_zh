package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBattleByIdRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetBattleByIdRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName GetBattleByIdHandler.java
 * @Description 根据id获取战斗详情
 * @author QiuKun
 * @date 2018年8月3日
 */
public class GetBattleByIdHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetBattleByIdRq req = msg.getExtension(GetBattleByIdRq.ext);
        WorldService worldService = getService(WorldService.class);
        GetBattleByIdRs resp = worldService.getBattleById(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(GetBattleByIdRs.ext, resp);
        }
    }

}
