package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetMultCombatRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName GetMultCombatHandler.java
 * @Description 获取多人副本信息
 * @author QiuKun
 * @date 2018年12月26日
 */
public class GetMultCombatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetMultCombatRq req = msg.getExtension(GetMultCombatRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        GetMultCombatRs resp = combatService.getMultCombat(getRoleId());
        if (resp != null) sendMsgToPlayer(GetMultCombatRs.EXT_FIELD_NUMBER, GetMultCombatRs.ext, resp);
    }

}
