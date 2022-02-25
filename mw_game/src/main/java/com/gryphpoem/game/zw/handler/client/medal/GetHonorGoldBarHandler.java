package com.gryphpoem.game.zw.handler.client.medal;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetHonorGoldBarRs;
import com.gryphpoem.game.zw.service.MedalService;
/**
 * 
* @ClassName: GetHonorGoldBarHandler
* @Description: 获取当前拥有的荣誉点数和金条数
* @author chenqi
* @date 2018年9月12日
*
 */
public class GetHonorGoldBarHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		MedalService medalService = getService(MedalService.class);
		GetHonorGoldBarRs resp = medalService.getHonorGoldBar(getRoleId());

		if (null != resp) {
			sendMsgToPlayer(GetHonorGoldBarRs.ext, resp);
		}
	}
}
