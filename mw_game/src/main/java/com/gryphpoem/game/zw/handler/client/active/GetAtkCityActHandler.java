package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetAtkCityActRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-05-09 14:13
 * @Description: 攻城掠地活动显示
 * @Modified By:
 */
public class GetAtkCityActHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetAtkCityActRs resp = getService(ActivityService.class).getAtkCityAct(getRoleId());
        sendMsgToPlayer(GetAtkCityActRs.EXT_FIELD_NUMBER, GetAtkCityActRs.ext, resp);
    }
}
