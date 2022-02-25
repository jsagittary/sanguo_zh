package com.gryphpoem.game.zw.handler.client.chemical;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalExpandRs;
import com.gryphpoem.game.zw.service.ChemicalService;

/**
 * 化工厂扩建
 * 
 * @author tyler
 *
 */
public class ChemicalExpandHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ChemicalService chemicalService = getService(ChemicalService.class);
		ChemicalExpandRs resp = chemicalService.chemicalExpand(getRoleId());
		sendMsgToPlayer(ChemicalExpandRs.EXT_FIELD_NUMBER, ChemicalExpandRs.ext, resp);
	}
}
