package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetEquipFactoryRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 兵工厂信息
 * 
 * @author tyler
 *
 */
public class GetEquipFactoryHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		BuildingService buildingService = getService(BuildingService.class);
		GetEquipFactoryRs resp = buildingService.getEquipFactoryRs(getRoleId());
		sendMsgToPlayer(GetEquipFactoryRs.EXT_FIELD_NUMBER, GetEquipFactoryRs.ext, resp);
	}
}
