package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetShopRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * 商店信息
 * 
 * @author tyler
 *
 */
public class GetShopHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ShopService service = getService(ShopService.class);
		GetShopRs resp = service.getShop(getRoleId());
		sendMsgToPlayer(GetShopRs.EXT_FIELD_NUMBER, GetShopRs.ext, resp);
	}
}
