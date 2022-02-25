package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.SummonGestapoRq;
import com.gryphpoem.game.zw.pb.GamePb4.SummonGestapoRs;
import com.gryphpoem.game.zw.service.GestapoService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-28 19:45
 * @Description: 召唤盖世太保
 * @Modified By:
 */
public class SummonGestapoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SummonGestapoRq req = msg.getExtension(SummonGestapoRq.ext);
        GestapoService service = getService(GestapoService.class);
        SummonGestapoRs resp = service.summonGestapo(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(SummonGestapoRs.ext, resp);
        }
    }
}
