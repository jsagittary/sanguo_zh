package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetMonthCardRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetMonthCardRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @ClassName GetMonthCardHandler.java
 * @Description 获取月卡信息
 * @author QiuKun
 * @date 2017年8月28日
 */
public class GetMonthCardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetMonthCardRq req = msg.getExtension(GetMonthCardRq.ext);
        PlayerService playerService = getService(PlayerService.class);
        GetMonthCardRs resp = playerService.getMonthCard(req, getRoleId());

        if (null != resp) {
            sendMsgToPlayer(GetMonthCardRs.ext, resp);
        }
    }

}
