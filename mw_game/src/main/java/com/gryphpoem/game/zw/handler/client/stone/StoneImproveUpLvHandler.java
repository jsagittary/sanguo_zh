package com.gryphpoem.game.zw.handler.client.stone;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.StoneImproveUpLvRq;
import com.gryphpoem.game.zw.pb.GamePb1.StoneImproveUpLvRs;
import com.gryphpoem.game.zw.service.StoneService;

/**
 * @ClassName StoneImproveUpLvHandler.java
 * @Description 进阶宝石升星
 * @author QiuKun
 * @date 2018年11月16日
 */
public class StoneImproveUpLvHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        StoneImproveUpLvRq req = msg.getExtension(StoneImproveUpLvRq.ext);
        StoneService service = getService(StoneService.class);
        StoneImproveUpLvRs resp = service.stoneImproveUpLv(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(StoneImproveUpLvRs.EXT_FIELD_NUMBER, StoneImproveUpLvRs.ext, resp);
    }

}
