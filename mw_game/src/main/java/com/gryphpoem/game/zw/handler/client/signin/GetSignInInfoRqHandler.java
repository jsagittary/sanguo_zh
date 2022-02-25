package com.gryphpoem.game.zw.handler.client.signin;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.GetSignInInfoRq;
import com.gryphpoem.game.zw.service.SignInService;

/**
 * 获取签到信息
 */
public class GetSignInInfoRqHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetSignInInfoRq req = msg.getExtension(GetSignInInfoRq.ext);
        SignInService signInService = getService(SignInService.class);
        GamePb4.GetSignInInfoRs resp = signInService.getSignInInfoRq(getRoleId(), req.getType());
        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetSignInInfoRs.ext, resp);
        }
    }

}
