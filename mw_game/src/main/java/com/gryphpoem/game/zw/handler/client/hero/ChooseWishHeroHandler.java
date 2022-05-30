package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * 选择心愿英雄
 * @description:
 * @author: zhou jie
 * @time: 2022/2/28 18:32
 */
public class ChooseWishHeroHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.ChooseWishHeroRq req = msg.getExtension(GamePb1.ChooseWishHeroRq.ext);
        HeroService heroService = getService(HeroService.class);
        GamePb1.ChooseWishHeroRs resp = heroService.chooseWishHero(getRoleId(), req.getSearchId());

        if (null != resp) {
            sendMsgToPlayer(GamePb1.ChooseWishHeroRs.ext, resp);
        }
    }
}
