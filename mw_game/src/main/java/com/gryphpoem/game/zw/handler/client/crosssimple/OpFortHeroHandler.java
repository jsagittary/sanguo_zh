package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.constant.SimpleCrossConstant;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb5.OpFortHeroRq;
import com.gryphpoem.game.zw.server.AppGameServer;

/**
 * @ClassName OpFortHeroHandler.java
 * @Description 跨服中堡垒将领操作
 * @author QiuKun
 * @date 2019年5月25日
 */
public class OpFortHeroHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        Base.Builder baseBuilder = Base.newBuilder(msg);
        OpFortHeroRq req = msg.getExtension(OpFortHeroRq.ext);
        if (req.getOpType() == SimpleCrossConstant.HERO_OPERATE_REVIVE) { // 对复活进行拦截
            getService(PlayerForCrossService.class).heroRevive(getRoleId(), req);
        } else {
            AppGameServer.getInstance().sendMsgToCross(baseBuilder, getRoleId().longValue());
        }
    }
}
