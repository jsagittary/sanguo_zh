package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.UsePropRq;
import com.gryphpoem.game.zw.pb.GamePb1.UsePropRs;
import com.gryphpoem.game.zw.service.PropService;

public class UsePropHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		UsePropRq req = msg.getExtension(UsePropRq.ext);
		UsePropRs resp = getService(PropService.class).useProp(getRoleId(), req.getPropId(), req.getCount(), req.getParams());
		sendMsgToPlayer(UsePropRs.EXT_FIELD_NUMBER, UsePropRs.ext, resp);
	}

}
