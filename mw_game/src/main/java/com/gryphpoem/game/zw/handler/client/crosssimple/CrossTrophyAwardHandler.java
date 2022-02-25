package com.gryphpoem.game.zw.handler.client.crosssimple;


import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.service.CrossDataService;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyAwardRs;

/**
 * @ClassName CrossTrophyAwardHandler.java
 * @Description 领取跨服成就奖励
 * @author QiuKun
 * @date 2019年5月28日
 */
public class CrossTrophyAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CrossTrophyAwardRq req = getMsg().getExtension(CrossTrophyAwardRq.ext);
        CrossDataService service = getService(CrossDataService.class);
        CrossTrophyAwardRs res = service.crossTrophyAward(getRoleId(), req);
        if (res != null) {
            sendMsgToPlayer(CrossTrophyAwardRs.ext, res);
        }
    }

}
