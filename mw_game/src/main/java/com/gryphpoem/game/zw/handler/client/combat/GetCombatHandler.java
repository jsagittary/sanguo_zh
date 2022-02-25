package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * 关卡信息
 * 
 * @author tyler
 *
 */
public class GetCombatHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CombatService combatService = getService(CombatService.class);
		GetCombatRs resp = combatService.getCombat(getRoleId());
		sendMsgToPlayer(GetCombatRs.EXT_FIELD_NUMBER, GetCombatRs.ext, resp);
	}
}
