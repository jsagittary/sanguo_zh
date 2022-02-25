package com.gryphpoem.game.zw.handler.client.cia;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.CiaService;

/**
 * @ClassName UnlockAgentHandler.java
 * @Description 解锁特工
 * @author QiuKun
 * @date 2018年6月5日
 */
public class UnlockAgentHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.UnlockAgentRq req = msg.getExtension(GamePb4.UnlockAgentRq.ext);
        CiaService service = getService(CiaService.class);
        GamePb4.UnlockAgentRs resp = service.unlockAgent(getRoleId(), req.getId());
        if (null != resp) {
            sendMsgToPlayer(GamePb4.UnlockAgentRs.ext, resp);
        }
    }

}
