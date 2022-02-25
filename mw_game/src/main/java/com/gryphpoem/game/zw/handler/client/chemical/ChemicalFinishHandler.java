package com.gryphpoem.game.zw.handler.client.chemical;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalFinishRs;
import com.gryphpoem.game.zw.service.ChemicalService;

/**
 * 化工厂完成生产
 * 
 * @author tyler
 *
 */
public class ChemicalFinishHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ChemicalService chemicalService = getService(ChemicalService.class);
		ChemicalFinishRs resp = chemicalService.chemicalFinish(getRoleId());
		sendMsgToPlayer(ChemicalFinishRs.EXT_FIELD_NUMBER, ChemicalFinishRs.ext, resp);
	}
}
