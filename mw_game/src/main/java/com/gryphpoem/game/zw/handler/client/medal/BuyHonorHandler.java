package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.BuyHonorRq;
import com.gryphpoem.game.zw.pb.GamePb1.BuyHonorRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: BuyHonorHandler
* @Description: 购买荣誉
* @author chenqi
* @date 2018年9月13日
*
 */
public class BuyHonorHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuyHonorRq req = msg.getExtension(BuyHonorRq.ext);
		MedalService medalService = getService(MedalService.class);
		BuyHonorRs resp = medalService.buyHonor(getRoleId(),req.getHonorGoodsId());

		if (null != resp) {
			sendMsgToPlayer(BuyHonorRs.ext, resp);
		}
	}

}
