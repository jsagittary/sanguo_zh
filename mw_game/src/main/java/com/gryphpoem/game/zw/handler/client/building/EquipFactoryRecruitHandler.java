package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.EquipFactoryRecruitRq;
import com.gryphpoem.game.zw.pb.GamePb1.EquipFactoryRecruitRs;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 兵工厂雇佣
 * 
 * @author tyler
 *
 */
public class EquipFactoryRecruitHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		EquipFactoryRecruitRq req = msg.getExtension(EquipFactoryRecruitRq.ext);
		BuildingService buildingService = getService(BuildingService.class);
		EquipFactoryRecruitRs resp = buildingService.doEquipFactoryRecruit(getRoleId(), req.getId());
		sendMsgToPlayer(EquipFactoryRecruitRs.EXT_FIELD_NUMBER, EquipFactoryRecruitRs.ext, resp);
	}
}
