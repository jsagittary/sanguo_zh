package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemPutonHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemPutonRq req = msg.getExtension(GamePb5.TotemPutonRq.ext);
		GamePb5.TotemPutonRs resp = getService(TotemService.class).totemPuton(getRoleId(), req);
		sendMsgToPlayer(GamePb5.TotemPutonRs.EXT_FIELD_NUMBER, GamePb5.TotemPutonRs.ext, resp);
	}

}
