package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeCastleSkinRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeCastleSkinRs;
import com.gryphpoem.game.zw.service.CastleSkinService;

/**
 * @ClassName ChangeCastleSkinHandler.java
 * @Description 更换城堡皮肤
 * @author QiuKun
 * @date 2019年4月14日
 */
public class ChangeCastleSkinHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChangeCastleSkinRq req = msg.getExtension(ChangeCastleSkinRq.ext);
        CastleSkinService service = getService(CastleSkinService.class);
        ChangeCastleSkinRs resp = service.changeCastleSkin(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(ChangeCastleSkinRs.ext, resp);
    }

}
