package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.HeroDecoratedRq;
import com.gryphpoem.game.zw.pb.GamePb1.HeroDecoratedRs;
import com.gryphpoem.game.zw.service.HeroService;

/**
 * @ClassName HeroDecoratedHandler.java
 * @Description 将领授勋
 * @author QiuKun
 * @date 2018年8月13日
 */
public class HeroDecoratedHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        HeroDecoratedRq req = msg.getExtension(HeroDecoratedRq.ext);
        HeroService heroService = getService(HeroService.class);
        HeroDecoratedRs resp = heroService.heroDecorated(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(HeroDecoratedRs.ext, resp);
        }

    }

}
