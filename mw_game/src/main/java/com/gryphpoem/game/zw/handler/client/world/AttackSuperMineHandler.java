package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.AttackSuperMineRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackSuperMineRs;
import com.gryphpoem.game.zw.service.SuperMineService;

/**
 * @ClassName AttackSuperMineHandler.java
 * @Description 超级矿点 采集,驻防,攻打
 * @author QiuKun
 * @date 2018年7月29日
 */
public class AttackSuperMineHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        AttackSuperMineRq req = msg.getExtension(AttackSuperMineRq.ext);
        SuperMineService service = getService(SuperMineService.class);
        AttackSuperMineRs resp = service.attackSuperMine(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(AttackSuperMineRs.ext, resp);
        }
    }

}
