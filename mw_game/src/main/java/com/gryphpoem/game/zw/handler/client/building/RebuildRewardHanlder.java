package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.RebuildRewardRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 领取重建家园奖励
 * @author tyler
 *
 */
public class RebuildRewardHanlder extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuildingService buildingService = getService(BuildingService.class);
		RebuildRewardRs resp = buildingService.rebuildReward(getRoleId());
		sendMsgToPlayer(RebuildRewardRs.EXT_FIELD_NUMBER, RebuildRewardRs.ext, resp);
	}
}
