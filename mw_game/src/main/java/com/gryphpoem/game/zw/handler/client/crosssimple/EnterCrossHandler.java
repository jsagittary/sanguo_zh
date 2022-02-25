package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.GamePb5.EnterCrossRq;

/**
 * @ClassName EnterCrossHandler.java
 * @Description 进入跨服
 * @author QiuKun
 * @date 2019年5月16日
 */
public class EnterCrossHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        EnterCrossRq req = getMsg().getExtension(EnterCrossRq.ext);
        PlayerForCrossService service = getService(PlayerForCrossService.class);
        service.enterCross(getRoleId(), req); 
    }

}
