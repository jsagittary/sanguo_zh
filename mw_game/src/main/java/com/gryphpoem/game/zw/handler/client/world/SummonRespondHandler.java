package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.SummonRespondRq;
import com.gryphpoem.game.zw.pb.GamePb2.SummonRespondRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName SummonRespondHandler.java
 * @Description 召唤响应
 * @author QiuKun
 * @date 2017年8月4日
 */
public class SummonRespondHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SummonRespondRq req = msg.getExtension(SummonRespondRq.ext);
        WorldService service = getService(WorldService.class);
        SummonRespondRs resp = service.summonRespond(getRoleId(), req.getSummonId());
        if (resp != null) sendMsgToPlayer(SummonRespondRs.ext, resp);
    }

}
