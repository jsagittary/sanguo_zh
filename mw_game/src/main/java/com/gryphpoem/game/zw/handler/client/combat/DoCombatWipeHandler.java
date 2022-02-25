package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatWipeRq;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatWipeRs;
import com.gryphpoem.game.zw.service.CombatService;

/**
 * 关卡扫荡
 * 
 * @author tyler
 *
 */
public class DoCombatWipeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		DoCombatWipeRq req = msg.getExtension(DoCombatWipeRq.ext);
		CombatService combatService = getService(CombatService.class);
		DoCombatWipeRs resp = combatService.doCombatWipe(getRoleId(), req.getType(), req.getCombatId(),req.getHeroIdList());
		sendMsgToPlayer(DoCombatWipeRs.EXT_FIELD_NUMBER, DoCombatWipeRs.ext, resp);
	}
}
