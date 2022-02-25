package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.WipeMultCombatRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName WipeMultCombatHandler.java
 * @Description 多人副本扫荡
 * @author QiuKun
 * @date 2018年12月26日
 */
public class WipeMultCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        MultCombatService combatService = getService(MultCombatService.class);
        WipeMultCombatRs resp = combatService.wipeMultCombat(getRoleId());
        if (resp != null) sendMsgToPlayer(WipeMultCombatRs.EXT_FIELD_NUMBER, WipeMultCombatRs.ext, resp);
    }

}
