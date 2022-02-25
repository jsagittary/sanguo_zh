package com.gryphpoem.game.zw.handler.client.stone;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.DoStoneImproveRq;
import com.gryphpoem.game.zw.pb.GamePb1.DoStoneImproveRs;
import com.gryphpoem.game.zw.service.StoneService;

/**
 * @ClassName DoStoneImproveHandler.java
 * @Description 对宝石进阶
 * @author QiuKun
 * @date 2018年11月16日
 */
public class DoStoneImproveHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DoStoneImproveRq req = msg.getExtension(DoStoneImproveRq.ext);
        StoneService service = getService(StoneService.class);
        DoStoneImproveRs resp = service.doStoneImprove(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(DoStoneImproveRs.EXT_FIELD_NUMBER, DoStoneImproveRs.ext, resp);
    }

}
