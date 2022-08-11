package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.DrawCardService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-14 18:19
 */
public class GetDrawHeroCardHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb5.GetDrawHeroCardRs rsp = DataResource.ac.getBean(DrawCardService.class).getDrawHeroCard(getRoleId());
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.GetDrawHeroCardRs.ext, rsp);
        }
    }
}
