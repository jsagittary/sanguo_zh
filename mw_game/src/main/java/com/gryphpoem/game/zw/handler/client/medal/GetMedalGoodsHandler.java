package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetMedalGoodsRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetMedalGoodsRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: GetMedalGoodsHandler
* @Description: 获取勋章商店的商品
* @author chenqi
* @date 2018年9月12日
*
 */
public class GetMedalGoodsHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMedalGoodsRq req = msg.getExtension(GetMedalGoodsRq.ext);
		MedalService medalService = getService(MedalService.class);
		GetMedalGoodsRs resp = medalService.getMedalGoods(getRoleId(),req.getType());

		if (null != resp) {
			sendMsgToPlayer(GetMedalGoodsRs.ext, resp);
		}
	}

}
