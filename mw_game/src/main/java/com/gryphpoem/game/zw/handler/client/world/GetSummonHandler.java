package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetSummonRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName GetSummonHandler.java
 * @Description 获取召唤次数
 * @author QiuKun
 * @date 2017年8月4日
 */
public class GetSummonHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService service = getService(WorldService.class);
        GetSummonRs resp = service.getSummon(getRoleId());
        if (resp != null) sendMsgToPlayer(GetSummonRs.ext, resp);
    }

}
