package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.JoinCombatTeamRq;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName JoinCombatTeamHandler.java
 * @Description 快速加入队伍(没有可加入就创建队伍);普通加入队伍
 * @author QiuKun
 * @date 2018年12月26日
 */
public class JoinCombatTeamHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        JoinCombatTeamRq req = msg.getExtension(JoinCombatTeamRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        combatService.joinCombatTeam(getRoleId(), req, (command, ext, msg) -> sendMsgToPlayer(command, ext, msg));

    }

}
