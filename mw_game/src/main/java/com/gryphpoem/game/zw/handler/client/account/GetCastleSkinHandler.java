package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetCastleSkinRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetCastleSkinRs;
import com.gryphpoem.game.zw.service.CastleSkinService;

/**
 * @ClassName GetCastleSkinHandler.java
 * @Description 获取皮肤
 * @author QiuKun
 * @date 2019年4月14日
 */
public class GetCastleSkinHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCastleSkinRq req = msg.getExtension(GetCastleSkinRq.ext);
        CastleSkinService service = getService(CastleSkinService.class);
        GetCastleSkinRs resp = service.getCastleSkin(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(GetCastleSkinRs.ext, resp);
    }

}
