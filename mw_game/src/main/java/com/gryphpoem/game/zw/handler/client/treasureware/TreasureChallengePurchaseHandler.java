package com.gryphpoem.game.zw.handler.client.treasureware;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.TreasureChallengePlayerService;

import java.util.Objects;

/**
 * @Author: duanShQ
 * 宝具副本 购买挑战玩家次数
 */
public class TreasureChallengePurchaseHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        TreasureChallengePlayerService service = getService(TreasureChallengePlayerService.class);
        GamePb4.TreasureChallengePurchaseRs resp = service.purchaseChallengeNum(getRoleId());
        if (Objects.nonNull(resp)) {
            sendMsgToPlayer(GamePb4.TreasureChallengePurchaseRs.ext, resp);
        }
    }
}
