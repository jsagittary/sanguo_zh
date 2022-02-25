package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetSupplyDorpRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName GetSupplyDorpHandler.java
 * @Description 获取空降补给
 * @author QiuKun
 * @date 2017年8月9日
 */
public class GetSupplyDorpHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetSupplyDorpRq req = msg.getExtension(GetSupplyDorpRq.ext);
        ActivityService service = getService(ActivityService.class);
        GetSupplyDorpRs res = service.getSupplyDorp(getRoleId());
        if (res != null) sendMsgToPlayer(GetSupplyDorpRs.ext, res);
    }

}
