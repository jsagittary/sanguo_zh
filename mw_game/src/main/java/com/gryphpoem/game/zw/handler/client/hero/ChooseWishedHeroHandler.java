package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.DrawCardService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-14 18:20
 */
public class ChooseWishedHeroHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb5.ChooseNewWishHeroRq req = msg.getExtension(GamePb5.ChooseNewWishHeroRq.ext);
        GamePb5.ChooseNewWishHeroRs rsp = DataResource.ac.getBean(DrawCardService.class).chooseNewWishHero(getRoleId(), req.getHeroId());
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.ChooseNewWishHeroRs.ext, rsp);
        }
    }
}
