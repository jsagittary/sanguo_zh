package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureChallengePlayerService;

import java.util.Objects;

/**
 * 宝具副本 刷新挑战玩家
 */
public class TreasureRefreshChallengeHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.TreasureRefreshChallengeRq req = msg.getExtension(GamePb4.TreasureRefreshChallengeRq.ext);
        TreasureChallengePlayerService service = getService(TreasureChallengePlayerService.class);
        GamePb4.TreasureRefreshChallengeRs resp = service.refreshChallenge(getRoleId(), req.getCostDiamond());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.TreasureRefreshChallengeRs.ext, resp);
        }
    }
}
