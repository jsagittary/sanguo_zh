package com.gryphpoem.game.zw.handler.client.stone;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.StoneMountingRq;
import com.gryphpoem.game.zw.pb.GamePb1.StoneMountingRs;
import com.gryphpoem.game.zw.service.StoneService;

/**
 * @ClassName StoneMountingHandler.java
 * @Description 宝石镶嵌
 * @author QiuKun
 * @date 2018年5月9日
 */
public class StoneMountingHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        StoneMountingRq req = msg.getExtension(StoneMountingRq.ext);
        StoneService service = getService(StoneService.class);
        StoneMountingRs resp = service.stoneMounting(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(StoneMountingRs.EXT_FIELD_NUMBER, StoneMountingRs.ext, resp);
    }

}
