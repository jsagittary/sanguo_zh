package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.service.CrossDataService;
import com.gryphpoem.game.zw.pb.GamePb5.BuyCrossBuffRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyCrossBuffRs;

/**
 * @ClassName BuyCrossBuffHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年5月16日
 */
public class BuyCrossBuffHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyCrossBuffRq req = getMsg().getExtension(BuyCrossBuffRq.ext);
        CrossDataService service = getService(CrossDataService.class);
        BuyCrossBuffRs res = service.buyCrossBuff(getRoleId(), req);
        if (res != null) {
            sendMsgToPlayer(BuyCrossBuffRs.ext, res);
        }
    }

}
