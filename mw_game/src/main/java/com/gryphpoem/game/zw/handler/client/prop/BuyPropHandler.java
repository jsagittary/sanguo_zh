package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.BuyPropRq;
import com.gryphpoem.game.zw.pb.GamePb1.BuyPropRs;
import com.gryphpoem.game.zw.service.PropService;

/**
 * 道具购买
 * 
 * @author tyler
 *
 */
public class BuyPropHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuyPropRq req = msg.getExtension(BuyPropRq.ext);
		BuyPropRs resp = getService(PropService.class).buyProp(getRoleId(), req.getPropId(), req.getNum());
		sendMsgToPlayer(BuyPropRs.EXT_FIELD_NUMBER, BuyPropRs.ext, resp);
	}

}
