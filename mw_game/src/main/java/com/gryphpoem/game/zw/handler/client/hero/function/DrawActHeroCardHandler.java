package com.gryphpoem.game.zw.handler.client.hero.function;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.plan.DrawCardPlanTemplateService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 16:24
 */
public class DrawActHeroCardHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb5.DrawActHeroCardRs rsp = DataResource.ac.getBean(DrawCardPlanTemplateService.class).drawActHeroCard(getRoleId(), msg.getExtension(GamePb5.DrawActHeroCardRq.ext));
        if (Objects.nonNull(rsp))
            sendMsgToPlayer(GamePb5.DrawActHeroCardRs.ext, rsp);
    }
}
