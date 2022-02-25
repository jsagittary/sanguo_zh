package com.gryphpoem.game.zw.handler.client.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.totem.TotemService;

/**
 *
 * @author xwind
 */
public class TotemStrengthenHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GamePb5.TotemStrengthenRq req = msg.getExtension(GamePb5.TotemStrengthenRq.ext);
		GamePb5.TotemStrengthenRs resp = getService(TotemService.class).totemStrengthen(getRoleId(), req.getTotemKey(),req.getRuneNum());
		sendMsgToPlayer(GamePb5.TotemStrengthenRs.EXT_FIELD_NUMBER, GamePb5.TotemStrengthenRs.ext, resp);
	}

}
