package com.gryphpoem.game.zw.handler.client.cross;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossBattleRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossBattleRs;

/**
 * @ClassName GetCrossBattleHandler.java
 * @Description 
 * @author QiuKun
 * @date 2019年4月3日
 */
public class GetCrossBattleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossBattleRq req = msg.getExtension(GetCrossBattleRq.ext);
        CrossWorldMapService service = getService(CrossWorldMapService.class);
        GetCrossBattleRs resp = service.getCrossBattle(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetCrossBattleRs.ext, resp);
        }
    }

}
