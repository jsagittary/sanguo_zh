package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SyncRoleRebuildRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 获取重建家园信息
 * @author tyler
 *
 */
public class SyncRoleRebuildHanlder extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuildingService buildingService = getService(BuildingService.class);
		SyncRoleRebuildRs resp = buildingService.getRoleRebuild(getRoleId());
		sendMsgToPlayer(SyncRoleRebuildRs.EXT_FIELD_NUMBER, SyncRoleRebuildRs.ext, resp);
	}
}
