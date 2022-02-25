package com.gryphpoem.game.zw.handler.client.cross.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarTaskDateRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/4/4 17:28
 * <br>Description: 玩家世界争霸-赛季任务数据
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class GetWorldWarTaskDateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldWarSeasonDateService service = getService(WorldWarSeasonDateService.class);
        WorldWarTaskDateRs rs = service.getPersonalWorldWarTaskDate(getRoleId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(WorldWarTaskDateRs.ext,rs);
        }
    }
}
