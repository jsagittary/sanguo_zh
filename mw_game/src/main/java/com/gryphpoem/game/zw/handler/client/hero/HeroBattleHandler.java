package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroBattleRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroBattleRs;
import com.gryphpoem.game.zw.service.hero.HeroOnBattleService;

/**
 * @author TanDonghai
 * @Description 将领上阵
 */
public class HeroBattleHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        HeroBattleRq req = msg.getExtension(HeroBattleRq.ext);
        HeroOnBattleService heroService = getService(HeroOnBattleService.class);
        HeroBattleRs resp = heroService.heroBattle(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(HeroBattleRs.ext, resp);
        }
    }

}
