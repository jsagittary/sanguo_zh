package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetSuperMineRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetSuperMineRs;
import com.gryphpoem.game.zw.service.SuperMineService;

/**
 * @ClassName GetSuperMineHandler.java
 * @Description 获取超级矿点信息
 * @author QiuKun
 * @date 2018年7月29日
 */
public class GetSuperMineHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetSuperMineRq req = msg.getExtension(GetSuperMineRq.ext);
        SuperMineService service = getService(SuperMineService.class);
        GetSuperMineRs resp = service.getSuperMine(getRoleId(), req);
        if (null != resp) {
            sendMsgToPlayer(GetSuperMineRs.ext, resp);
        }
    }

}
