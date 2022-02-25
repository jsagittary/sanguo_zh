package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.BuyMedalRq;
import com.gryphpoem.game.zw.pb.GamePb1.BuyMedalRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: BuyMedalHandler
* @Description: 购买勋章
* @author chenqi
* @date 2018年9月12日
*
 */
public class BuyMedalHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuyMedalRq req = msg.getExtension(BuyMedalRq.ext);
		MedalService medalService = getService(MedalService.class);
		BuyMedalRs resp = medalService.buyMedalGoods(getRoleId(),req.getMedalGoodsId());

		if (null != resp) {
			sendMsgToPlayer(BuyMedalRs.ext, resp);
		}
	}

}
