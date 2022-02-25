package com.gryphpoem.game.zw.handler.client.cross.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarCampRankPlayersDateRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarCampRankPlayersDateRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/4/4 17:32
 * <br>Description: 玩家世界争霸-世界阵营-积分排行-玩家数据
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class GetWorldWarCampRankPlayersDateHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldWarCampRankPlayersDateRq req = msg.getExtension(WorldWarCampRankPlayersDateRq.ext);
        WorldWarSeasonDateService service = getService(WorldWarSeasonDateService.class);
        WorldWarCampRankPlayersDateRs rs = service.getWorldWarCampRankPlayersDate(req.getPage());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(WorldWarCampRankPlayersDateRs.ext,rs);
        }
    }
}
