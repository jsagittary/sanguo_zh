package com.gryphpoem.game.zw.handler.client.chemical;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalRecruitRq;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalRecruitRs;
import com.gryphpoem.game.zw.service.ChemicalService;

/**
 * 化工厂生产
 * 
 * @author tyler
 *
 */
public class ChemicalRecruitHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ChemicalRecruitRq req = msg.getExtension(ChemicalRecruitRq.ext);
		ChemicalService chemicalService = getService(ChemicalService.class);
		ChemicalRecruitRs resp = chemicalService.chemicalRecruit(getRoleId(), req.getPos(), req.getId(), req.getItemId());
		sendMsgToPlayer(ChemicalRecruitRs.EXT_FIELD_NUMBER, ChemicalRecruitRs.ext, resp);
	}
}
