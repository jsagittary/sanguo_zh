package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.RecvDay7ActAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.RecvDay7ActAwardRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

public class RecvDay7ActAwardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		RecvDay7ActAwardRq req = msg.getExtension(RecvDay7ActAwardRq.ext);
		RecvDay7ActAwardRs resp = getService(ActivityService.class).recvDay7ActAward(getRoleId(), req.getKeyId());
		sendMsgToPlayer(RecvDay7ActAwardRs.EXT_FIELD_NUMBER, RecvDay7ActAwardRs.ext, resp);
	}

}
