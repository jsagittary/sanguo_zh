package com.gryphpoem.game.zw.handler.client.cross.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/4/4 17:32
 * <br>Description: 玩家世界争霸-世界阵营-城市征战领奖
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class GetWorldWarAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        WorldWarAwardRq req = msg.getExtension(WorldWarAwardRq.ext);
        WorldWarSeasonDateService service = getService(WorldWarSeasonDateService.class);
        WorldWarAwardRs rs = service.getPlayerWorldWarAward(getRoleId(),req);
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(WorldWarAwardRs.ext,rs);
        }
    }
}
