package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ModifySloganRq;
import com.gryphpoem.game.zw.pb.GamePb3.ModifySloganRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 修改军团公告
 * @author TanDonghai
 *
 */
public class ModifySloganHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ModifySloganRq req = msg.getExtension(ModifySloganRq.ext);
		CampService campService = getService(CampService.class);
		ModifySloganRs resp = null;
		if (req.getOpt() == 1) {
			resp = campService.modifySlogan(getRoleId(), req.getSlogan());
		}else if(req.getOpt() == 2){
			resp = campService.modifyBbs(getRoleId(), req.getQq(),req.getWx());
		}
		if (null != resp) {
			sendMsgToPlayer(ModifySloganRs.ext, resp);
		}
	}

}
