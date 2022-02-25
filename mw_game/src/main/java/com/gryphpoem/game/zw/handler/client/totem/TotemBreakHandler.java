package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemBreakHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemBreakRq req = msg.getExtension(GamePb5.TotemBreakRq.ext);
		GamePb5.TotemBreakRs resp = getService(TotemService.class).totemBreak(getRoleId(), req.getTotemKey());
		sendMsgToPlayer(GamePb5.TotemBreakRs.EXT_FIELD_NUMBER, GamePb5.TotemBreakRs.ext, resp);
	}

}
