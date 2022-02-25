package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemDecomposeHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemDecomposeRq req = msg.getExtension(GamePb5.TotemDecomposeRq.ext);
		GamePb5.TotemDecomposeRs resp = getService(TotemService.class).totemDecompose(getRoleId(), req);
		sendMsgToPlayer(GamePb5.TotemDecomposeRs.EXT_FIELD_NUMBER, GamePb5.TotemDecomposeRs.ext, resp);
	}

}
