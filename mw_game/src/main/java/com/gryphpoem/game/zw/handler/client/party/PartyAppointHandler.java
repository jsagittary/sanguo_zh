package com.gryphpoem.game.zw.handler.client.party;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PartyAppointRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyAppointRs;
import com.gryphpoem.game.zw.service.CampService;

/**
 * 
 * @Description 军团官员任命
 * @author TanDonghai
 *
 */
public class PartyAppointHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PartyAppointRq req = msg.getExtension(PartyAppointRq.ext);
		CampService campService = getService(CampService.class);
		PartyAppointRs resp = campService.partyAppoint(getRoleId(), req.getRoleId(), req.getJob());

		if (null != resp) {
			sendMsgToPlayer(PartyAppointRs.ext, resp);
		}
	}

}
