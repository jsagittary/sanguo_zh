package com.gryphpoem.game.zw.handler.client.signin;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.GetSignInRewardRq;
import com.gryphpoem.game.zw.service.SignInService;

/**
 * 签到领取奖励
 */
public class GetSignInRewardRqHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetSignInRewardRq req = msg.getExtension(GetSignInRewardRq.ext);
        SignInService signInService = getService(SignInService.class);
        GamePb4.GetSignInRewardRs resp = signInService.getSignInRewardRq(getRoleId(), req.getType());
        if (null != resp) {
            sendMsgToPlayer(GamePb4.GetSignInRewardRs.ext, resp);
        }
    }

}
