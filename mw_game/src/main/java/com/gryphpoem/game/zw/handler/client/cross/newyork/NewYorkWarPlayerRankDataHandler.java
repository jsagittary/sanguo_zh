package com.gryphpoem.game.zw.handler.client.cross.newyork;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarPlayerRankDataRq;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarPlayerRankDataRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/5/14 18:29
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWarPlayerRankDataHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        NewYorkWarPlayerRankDataRq req = msg.getExtension(NewYorkWarPlayerRankDataRq.ext);
        NewYorkWarService service = getService(NewYorkWarService.class);
        NewYorkWarPlayerRankDataRs rs = service.playerNewYorkWarRankVal(getRoleId(),req);
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(NewYorkWarPlayerRankDataRs.ext,rs);
        }
    }
}
