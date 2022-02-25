package com.gryphpoem.game.zw.handler.client.cross.newyork;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarInfoRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/5/14 18:25
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWarInfoHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        NewYorkWarService service = getService(NewYorkWarService.class);
        NewYorkWarInfoRs rs = service.getNewYorkWarInfo(getRoleId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(NewYorkWarInfoRs.ext,rs);
        }
    }
}
