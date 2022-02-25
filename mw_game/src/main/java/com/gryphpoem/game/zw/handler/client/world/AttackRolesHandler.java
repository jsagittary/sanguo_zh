package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.AttackRolesRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * 
 * @Description 获取被攻击的情报
 *
 */
public class AttackRolesHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldService worldService = getService(WorldService.class);
        AttackRolesRs resp = worldService.getAttackRoles(getRoleId());

        if (null != resp) {
            sendMsgToPlayer(AttackRolesRs.ext, resp);
        }
    }

}
