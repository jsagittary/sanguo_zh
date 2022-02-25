package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.CastleSkinStarUpRq;
import com.gryphpoem.game.zw.pb.GamePb4.CastleSkinStarUpRs;
import com.gryphpoem.game.zw.service.CastleSkinService;

;

/**
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-11-12 10:16
 */
public class CastleSkinStarUpHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.CastleSkinStarUpRq req = msg.getExtension(CastleSkinStarUpRq.ext);
        CastleSkinStarUpRs resp = getService(CastleSkinService.class).starUp(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(CastleSkinStarUpRs.ext, resp);
    }
}