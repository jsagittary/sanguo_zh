package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetHeroBattlePosRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * @ClassName GetHeroBattlePosHandler.java
 * @Description 获取上阵将领在其他位置的映射
 * @author QiuKun
 * @date 2017年12月21日
 */
public class GetHeroBattlePosHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetHeroBattlePosRq req = msg.getExtension(GetHeroBattlePosRq.ext);
        HeroService heroService = getService(HeroService.class);
        GetHeroBattlePosRs resp = heroService.getHeroBattlePos(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetHeroBattlePosRs.ext, resp);
        }
    }

}
