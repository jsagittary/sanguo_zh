package com.gryphpoem.game.zw.handler.client.shop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.BuyActRq;
import com.gryphpoem.game.zw.pb.GamePb3.BuyActRs;
import com.gryphpoem.game.zw.service.ShopService;

/**
 * 购买体力
 * 
 * @author tyler
 *
 */
public class BuyActHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuyActRq req = msg.getExtension(BuyActRq.ext);
		ShopService service = getService(ShopService.class);
		BuyActRs resp = service.buyAct(getRoleId(), req);
        sendMsgToPlayer(BuyActRs.EXT_FIELD_NUMBER, BuyActRs.ext, resp);
	}
}
