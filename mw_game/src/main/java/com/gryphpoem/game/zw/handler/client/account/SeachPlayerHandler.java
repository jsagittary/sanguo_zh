package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SeachPlayerRq;
import com.gryphpoem.game.zw.pb.GamePb1.SeachPlayerRs;
import com.gryphpoem.game.zw.service.PlayerService;

public class SeachPlayerHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SeachPlayerRq req = msg.getExtension(SeachPlayerRq.ext);
		PlayerService playerService = getService(PlayerService.class);

		SeachPlayerRs resp = playerService.seachPlayer(req, getRoleId());
		sendMsgToPlayer(SeachPlayerRs.ext, resp);
	}

}
