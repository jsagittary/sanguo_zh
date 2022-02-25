package com.gryphpoem.game.zw.handler.client.cia;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.CiaService;

/**
 * @ClassName AgentUpgradeStarHandler.java
 * @Description 特工升级星级
 * @author QiuKun
 * @date 2018年6月6日
 */
public class AgentUpgradeStarHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.AgentUpgradeStarRq req = msg.getExtension(GamePb4.AgentUpgradeStarRq.ext);
        CiaService service = getService(CiaService.class);
        GamePb4.AgentUpgradeStarRs resp = service.agentUpgradeStar(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GamePb4.AgentUpgradeStarRs.ext, resp);
        }
    }
}
