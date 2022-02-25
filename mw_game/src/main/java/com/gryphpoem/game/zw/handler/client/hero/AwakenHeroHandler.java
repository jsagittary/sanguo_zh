package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.AwakenHeroRq;
import com.gryphpoem.game.zw.pb.GamePb1.AwakenHeroRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * @program: zombie_trunk
 * @description:
 * @author: zhou jie
 * @create: 2019-10-25 11:32
 */
public class AwakenHeroHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AwakenHeroRq req = msg.getExtension(AwakenHeroRq.ext);
        HeroService heroService = getService(HeroService.class);
        AwakenHeroRs resp = heroService.awakenHero(getRoleId(), req.getHeroId(), req.getType());

        if (null != resp) {
            sendMsgToPlayer(AwakenHeroRs.ext, resp);
        }
    }
}