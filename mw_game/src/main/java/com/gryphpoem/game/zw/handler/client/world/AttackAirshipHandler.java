package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AttackAirshipRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackAirshipRs;
import com.gryphpoem.game.zw.service.AirshipService;

/**
 * @ClassName AttackAirshipHandler.java
 * @Description 攻击飞艇
 * @author QiuKun
 * @date 2019年1月21日
 */
public class AttackAirshipHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AttackAirshipRq req = msg.getExtension(AttackAirshipRq.ext);
        AirshipService service = getService(AirshipService.class);
        AttackAirshipRs resp = service.attackAirship(getRoleId(), req);

        if (null != resp) {
            sendMsgToPlayer(AttackAirshipRs.ext, resp);
        }
    }

}
