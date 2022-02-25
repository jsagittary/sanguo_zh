package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCDRq;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCDRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 行军加速
 * 
 * @author tyler
 *
 */
public class MoveCDHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		MoveCDRq req = msg.getExtension(MoveCDRq.ext);
		WorldService worldService = getService(WorldService.class);
		MoveCDRs resp = worldService.moveCd(getRoleId(), req.getType(), req.getKeyId());

		if (null != resp) {
			sendMsgToPlayer(MoveCDRs.ext, resp);
		}
	}

}
