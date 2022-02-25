package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityHotProductService;

/**
 * 热销商品活动的奖励
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-08-26 16:00
 */
public class ActHotProductAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.ActHotProductAwardRq req = msg.getExtension(GamePb4.ActHotProductAwardRq.ext);
        GamePb4.ActHotProductAwardRs resp = getService(ActivityHotProductService.class).actHotProductAward(getRoleId(), req.getKeyId());
        sendMsgToPlayer(GamePb4.ActHotProductAwardRs.EXT_FIELD_NUMBER, GamePb4.ActHotProductAwardRs.ext, resp);
    }
}