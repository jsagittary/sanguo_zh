package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.EquipTurnplateRq;
import com.gryphpoem.game.zw.pb.GamePb4.EquipTurnplateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 
* @ClassName: EquipTurnplateHandler
* @Description: 装备转盘抽奖
* @author chenqi
* @date 2018年8月30日
*
 */
public class EquipTurnplateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
    	EquipTurnplateRq req = msg.getExtension(EquipTurnplateRq.ext);
        ActivityService service = getService(ActivityService.class);
        EquipTurnplateRs res = service.equipTurnplate(getRoleId(), req.getId(), req.getCostType());
        if (res != null) sendMsgToPlayer(EquipTurnplateRs.ext, res);
    }
}
