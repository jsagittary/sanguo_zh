package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.SupplyDorpAwardRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName SupplyDorpAwardHandler.java
 * @Description 空降补给领取奖励
 * @author QiuKun
 * @date 2017年8月9日
 */
public class SupplyDorpAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SupplyDorpAwardRq req = msg.getExtension(SupplyDorpAwardRq.ext);
        ActivityService service = getService(ActivityService.class);
        SupplyDorpAwardRs res = service.supplyDorpAward(getRoleId(), req.getParam(),req.getKeyId());
        if (res != null) sendMsgToPlayer(SupplyDorpAwardRs.ext, res);
    }

}
