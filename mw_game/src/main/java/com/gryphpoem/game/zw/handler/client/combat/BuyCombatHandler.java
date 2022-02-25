package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.BuyCombatRq;
import com.gryphpoem.game.zw.pb.GamePb2.BuyCombatRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * 关卡资源副本购买
 * 
 * @author tyler
 *
 */
public class BuyCombatHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuyCombatRq req = msg.getExtension(BuyCombatRq.ext);
		CombatService combatService = getService(CombatService.class);
		BuyCombatRs resp = combatService.buyCombat(getRoleId(), req.getCombatId());
		sendMsgToPlayer(BuyCombatRs.EXT_FIELD_NUMBER, BuyCombatRs.ext, resp);
	}
}
