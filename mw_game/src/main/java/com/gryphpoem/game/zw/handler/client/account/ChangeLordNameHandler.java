package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeLordNameRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeLordNameRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @ClassName ChangeLordNameHandler.java
 * @Description 修改名称
 * @author QiuKun
 * @date 2017年8月3日
 */
public class ChangeLordNameHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChangeLordNameRq req = msg.getExtension(ChangeLordNameRq.ext);
        PlayerService service = getService(PlayerService.class);
        ChangeLordNameRs resp = service.changeLordName(getRoleId(), req.getName());
        if (resp != null) sendMsgToPlayer(ChangeLordNameRs.ext, resp);
    }

}
