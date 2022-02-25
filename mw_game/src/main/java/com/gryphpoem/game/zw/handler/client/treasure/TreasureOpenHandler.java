package com.gryphpoem.game.zw.handler.client.treasure;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.TreasureOpenRq;
import com.gryphpoem.game.zw.pb.GamePb1.TreasureOpenRs;
import com.gryphpoem.game.zw.service.TreasureService;

/**
 * 
 * @Description 聚宝盆开启
 * @author tyler
 *
 */
public class TreasureOpenHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TreasureOpenRq req = msg.getExtension(TreasureOpenRq.ext);
		TreasureService service = getService(TreasureService.class);
		TreasureOpenRs resp = service.treasureOpen(getRoleId(), req.getId(), req.getBuy());

		if (null != resp) {
			sendMsgToPlayer(TreasureOpenRs.ext, resp);
		}
	}

}
