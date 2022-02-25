package com.gryphpoem.game.zw.handler.client.combat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.MultCombatShopBuyRq;
import com.gryphpoem.game.zw.pb.GamePb2.MultCombatShopBuyRs;
import com.gryphpoem.game.zw.service.MultCombatService;

/**
 * @ClassName MultCombatShopBuyHandler.java
 * @Description 多人副本商店购买
 * @author QiuKun
 * @date 2018年12月26日
 */
public class MultCombatShopBuyHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        MultCombatShopBuyRq req = msg.getExtension(MultCombatShopBuyRq.ext);
        MultCombatService combatService = getService(MultCombatService.class);
        MultCombatShopBuyRs resp = combatService.multCombatShopBuy(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(MultCombatShopBuyRs.EXT_FIELD_NUMBER, MultCombatShopBuyRs.ext, resp);
    }

}
