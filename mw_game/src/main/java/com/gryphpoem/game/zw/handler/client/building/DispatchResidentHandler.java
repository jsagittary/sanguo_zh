package com.gryphpoem.game.zw.handler.client.building;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.service.BuildingService;

/**
 * 派遣居民
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/19 14:21
 */
public class DispatchResidentHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb1.DispatchResidentRq rq = msg.getExtension(GamePb1.DispatchResidentRq.ext);
        BuildingService buildingService = getService(BuildingService.class);
        GamePb1.DispatchResidentRs rs = buildingService.dispatchResident(getRoleId(), rq);
        sendMsgToPlayer(GamePb1.DispatchResidentRs.EXT_FIELD_NUMBER, GamePb1.DispatchResidentRs.ext, rs);
    }

}
