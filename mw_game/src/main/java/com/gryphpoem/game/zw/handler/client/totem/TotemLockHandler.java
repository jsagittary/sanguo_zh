package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemLockHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemLockRq req = msg.getExtension(GamePb5.TotemLockRq.ext);
		GamePb5.TotemLockRs resp = getService(TotemService.class).totemLock(getRoleId(), req);
		sendMsgToPlayer(GamePb5.TotemLockRs.EXT_FIELD_NUMBER, GamePb5.TotemLockRs.ext, resp);
	}

}
