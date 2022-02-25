package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetEquipTurnplatRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * 
* @ClassName: GetEquipTurnplatHandler
* @Description: 获取装备转盘信息
* @author chenqi
* @date 2018年8月30日
*
 */
public class GetEquipTurnplatHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ActivityService service = getService(ActivityService.class);
        GetEquipTurnplatRs res = service.getEquipTurnplat(getRoleId());
        if (res != null) sendMsgToPlayer(GetEquipTurnplatRs.ext, res);
    }
}
