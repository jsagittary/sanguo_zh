package com.gryphpoem.game.zw.handler.client.cross.newyork;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarProgressDataRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/5/14 18:26
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWarProgressDataHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        NewYorkWarService service = getService(NewYorkWarService.class);
        NewYorkWarProgressDataRs rs = service.getNewYorkWarProgressData(getRoleId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(NewYorkWarProgressDataRs.ext,rs);
        }
    }
}
