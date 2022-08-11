package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.DrawCardService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-14 18:16
 */
public class DrawHeroCardHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb5.DrawHeroCardRq req = msg.getExtension(GamePb5.DrawHeroCardRq.ext);
        GamePb5.DrawHeroCardRs rsp = DataResource.ac.getBean(DrawCardService.class).drawHeroCard(getRoleId(), req);
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.DrawHeroCardRs.ext, rsp);
        }
    }
}
