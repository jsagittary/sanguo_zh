package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.StartMultCombatRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName StartMultCombatHandler.java
 * @Description 开始多人副本
 * @author QiuKun
 * @date 2018年12月26日
 */
public class StartMultCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        MultCombatService combatService = getService(MultCombatService.class);
        StartMultCombatRs resp = combatService.startMultCombat(getRoleId() );
        if (resp != null) sendMsgToPlayer(StartMultCombatRs.EXT_FIELD_NUMBER, StartMultCombatRs.ext, resp);
    }

}
