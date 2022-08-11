package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureChallengePlayerService;

import java.util.Objects;

/**
 * 宝具副本 挑战玩家请求
 */
public class TreasureChallengePlayerHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.TreasureChallengePlayerRq req = msg.getExtension(GamePb4.TreasureChallengePlayerRq.ext);
        TreasureChallengePlayerService service = getService(TreasureChallengePlayerService.class);
        GamePb4.TreasureChallengePlayerRs resp = service.challengePlayer(getRoleId(), req);
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.TreasureChallengePlayerRs.ext, resp);
        }
    }
}
