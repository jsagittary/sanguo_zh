package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemSyntheticHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemSyntheticRq req = msg.getExtension(GamePb5.TotemSyntheticRq.ext);
		GamePb5.TotemSyntheticRs resp = getService(TotemService.class).totemSynthetic(getRoleId(), req);
		sendMsgToPlayer(GamePb5.TotemSyntheticRs.EXT_FIELD_NUMBER, GamePb5.TotemSyntheticRs.ext, resp);
	}

}
