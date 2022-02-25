package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.pb.GamePb2.SummonTeamRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName SummonTeamHandler.java
 * @Description 发起召唤
 * @author QiuKun
 * @date 2017年8月4日
 */
public class SummonTeamHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb2.SummonTeamRq req = msg.getExtension(GamePb2.SummonTeamRq.ext);
        WorldService service = getService(WorldService.class);
        SummonTeamRs resp = service.summonTeam(req, getRoleId());
        if (resp != null) sendMsgToPlayer(SummonTeamRs.ext, resp);
    }

}
