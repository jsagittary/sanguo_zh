package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.AcqHeroSetRq;
import com.gryphpoem.game.zw.pb.GamePb1.AcqHeroSetRs;
import com.gryphpoem.game.zw.service.hero.HeroOnBattleService;

/**
 * @author QiuKun
 * @ClassName AcqHeroSetHandler.java
 * @Description 内阁采集将领布置
 * @date 2017年7月14日
 */
public class AcqHeroSetHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AcqHeroSetRq req = msg.getExtension(AcqHeroSetRq.ext);
        HeroOnBattleService service = getService(HeroOnBattleService.class);
        AcqHeroSetRs resp = service.acqHeroSet(getRoleId(), req);
        if (null != resp) sendMsgToPlayer(AcqHeroSetRs.ext, resp);
    }

}
