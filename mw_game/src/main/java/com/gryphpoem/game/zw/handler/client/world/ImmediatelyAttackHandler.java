package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ImmediatelyAttackRq;
import com.gryphpoem.game.zw.pb.GamePb4.ImmediatelyAttackRs;
import com.gryphpoem.game.zw.service.BerlinWarService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-09-10 14:28
 * @description: 将领立即出击
 * @modified By:
 */
public class ImmediatelyAttackHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ImmediatelyAttackRq req = msg.getExtension(ImmediatelyAttackRq.ext);
        BerlinWarService berlinWarService = getService(BerlinWarService.class);
        berlinWarService.immediatelyAttack(getRoleId(), req.getHeroId());
        sendMsgToPlayer(ImmediatelyAttackRs.ext, ImmediatelyAttackRs.newBuilder().build());
    }
}
