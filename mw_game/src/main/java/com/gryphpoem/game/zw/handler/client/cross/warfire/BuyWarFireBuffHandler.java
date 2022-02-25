package com.gryphpoem.game.zw.handler.client.cross.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.warfire.WarFireCommonService;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireBuffRq;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireBuffRs;

/**
 * 战火燎原购买战前buff
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-08 17:16
 */
public class BuyWarFireBuffHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        BuyWarFireBuffRq req = msg.getExtension(BuyWarFireBuffRq.ext);
        WarFireCommonService service = getService(WarFireCommonService.class);
        BuyWarFireBuffRs resp = service.buyWarFireBuff(getRoleId(), req.getBuffType());
        if (null != resp) {
            sendMsgToPlayer(BuyWarFireBuffRs.ext, resp);
        }
    }
}