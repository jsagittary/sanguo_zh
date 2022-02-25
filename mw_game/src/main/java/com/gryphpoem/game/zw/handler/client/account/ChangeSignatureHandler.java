package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeSignatureRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeSignatureRs;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * @ClassName ChangeSignatureHandler.java
 * @Description 修改个性签名
 * @author QiuKun
 * @date 2017年8月3日
 */
public class ChangeSignatureHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChangeSignatureRq req = msg.getExtension(ChangeSignatureRq.ext);
        PlayerService service = getService(PlayerService.class);
        ChangeSignatureRs resp = service.changeSignature(getRoleId(), req.getSignature());
        if (resp != null) sendMsgToPlayer(ChangeSignatureRs.ext, resp);
    }

}
