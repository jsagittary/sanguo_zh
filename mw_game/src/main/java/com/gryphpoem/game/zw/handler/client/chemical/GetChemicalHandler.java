package com.gryphpoem.game.zw.handler.client.chemical;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetChemicalRs;
import com.gryphpoem.game.zw.service.ChemicalService;

/**
 * 化工厂信息
 * 
 * @author tyler
 *
 */
public class GetChemicalHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ChemicalService chemicalService = getService(ChemicalService.class);
		GetChemicalRs resp = chemicalService.getChemical(getRoleId());
		sendMsgToPlayer(GetChemicalRs.EXT_FIELD_NUMBER, GetChemicalRs.ext, resp);
	}
}
