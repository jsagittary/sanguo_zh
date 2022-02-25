package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.VipBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.VipBuyRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * VIP礼包购买
 * 
 * @author tyler
 *
 */
public class VipBuyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		VipBuyRq req = msg.getExtension(VipBuyRq.ext);
		ShopService service = getService(ShopService.class);
		VipBuyRs resp = service.vipBuy(getRoleId(), req.getId());
		sendMsgToPlayer(VipBuyRs.EXT_FIELD_NUMBER, VipBuyRs.ext, resp);
	}
}
