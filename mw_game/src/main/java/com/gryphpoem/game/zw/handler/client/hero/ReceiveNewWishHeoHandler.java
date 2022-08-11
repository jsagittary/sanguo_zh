package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.DrawCardService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-14 18:24
 */
public class ReceiveNewWishHeoHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb5.ReceiveNewWishHeoRs rsp = DataResource.ac.getBean(DrawCardService.class).receiveNewWishHeo(getRoleId());
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.ReceiveNewWishHeoRs.ext, rsp);
        }
    }
}
