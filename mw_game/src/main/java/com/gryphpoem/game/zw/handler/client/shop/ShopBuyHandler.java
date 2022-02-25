package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb3.ShopBuyRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * 商店购买
 * 
 * @author tyler
 *
 */
public class ShopBuyHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ShopBuyRq req = msg.getExtension(ShopBuyRq.ext);
		ShopService service = getService(ShopService.class);
		ShopBuyRs resp = service.shopBuy(getRoleId(), req.getId(), req.getUseItem(), req.getNum());
		sendMsgToPlayer(ShopBuyRs.EXT_FIELD_NUMBER, ShopBuyRs.ext, resp);
	}
}
