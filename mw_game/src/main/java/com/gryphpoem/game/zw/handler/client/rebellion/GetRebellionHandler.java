package com.gryphpoem.game.zw.handler.client.rebellion;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetRebellionRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetRebellionRs;
import com.gryphpoem.game.zw.service.RebelService;

/**
 * @ClassName GetRebellionHandler.java
 * @Description 获取匪军叛乱信息
 * @author QiuKun
 * @date 2018年10月29日
 */
public class GetRebellionHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetRebellionRq req = msg.getExtension(GetRebellionRq.ext);
        GetRebellionRs resp = getService(RebelService.class).getRebellion(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(GetRebellionRs.EXT_FIELD_NUMBER, GetRebellionRs.ext, resp);
    }

}
