package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetHerosRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * @author TanDonghai
 * @Description 获取所有将领
 */
public class GetHerosHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        HeroService heroService = getService(HeroService.class);
        GetHerosRs resp = heroService.getHeroList(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetHerosRs.ext, resp);
        }
    }

}
