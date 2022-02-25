package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityLotteryService;

/**
 * 好运道活动抽奖
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-09-10 15:23
 */
public class ActGoodLuckAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.ActGoodLuckAwardRq req = msg.getExtension(GamePb4.ActGoodLuckAwardRq.ext);
        GamePb4.ActGoodLuckAwardRs resp = getService(ActivityLotteryService.class).actGoodLuckAward(getRoleId(), req);
        sendMsgToPlayer(GamePb4.ActGoodLuckAwardRs.EXT_FIELD_NUMBER, GamePb4.ActGoodLuckAwardRs.ext, resp);
    }
}