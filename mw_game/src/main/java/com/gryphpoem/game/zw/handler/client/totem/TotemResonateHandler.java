package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemResonateHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemResonateRq req = msg.getExtension(GamePb5.TotemResonateRq.ext);
		GamePb5.TotemResonateRs resp = getService(TotemService.class).totemResonate(getRoleId(), req.getTotemKey());
		sendMsgToPlayer(GamePb5.TotemResonateRs.EXT_FIELD_NUMBER, GamePb5.TotemResonateRs.ext, resp);
	}

}
