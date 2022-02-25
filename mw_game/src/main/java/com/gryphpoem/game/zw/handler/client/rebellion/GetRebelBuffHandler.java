package com.gryphpoem.game.zw.handler.client.rebellion;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetRebelBuffRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetRebelBuffRs;
import com.gryphpoem.game.zw.service.RebelService;

/**
 * @ClassName GetRebelBuffHandler.java
 * @Description 获取自己的匪军叛乱buff
 * @author QiuKun
 * @date 2018年10月29日
 */
public class GetRebelBuffHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetRebelBuffRq req = msg.getExtension(GetRebelBuffRq.ext);
        GetRebelBuffRs resp = getService(RebelService.class).getRebelBuff(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(GetRebelBuffRs.EXT_FIELD_NUMBER, GetRebelBuffRs.ext, resp);
    }

}
