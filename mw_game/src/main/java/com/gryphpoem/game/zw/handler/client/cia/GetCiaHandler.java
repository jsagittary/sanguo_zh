package com.gryphpoem.game.zw.handler.client.cia;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetCiaRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetCiaRs;
import com.gryphpoem.game.zw.service.CiaService;

/**
 * @ClassName GetCiaHandler.java
 * @Description 获取情报部信息
 * @author QiuKun
 * @date 2018年6月5日
 */
public class GetCiaHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCiaRq req = msg.getExtension(GetCiaRq.ext);
        CiaService service = getService(CiaService.class);
        GetCiaRs resp = service.getCia(getRoleId(), req.getParam());
        if (null != resp) {
            sendMsgToPlayer(GetCiaRs.ext, resp);
        }
    }

}
