package com.gryphpoem.game.zw.handler.client.stone;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.StoneUpLvRq;
import com.gryphpoem.game.zw.pb.GamePb1.StoneUpLvRs;
import com.gryphpoem.game.zw.service.StoneService;

/**
 * @ClassName StoneUpLvHandler.java
 * @Description 宝石升级
 * @author QiuKun
 * @date 2018年5月9日
 */
public class StoneUpLvHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        StoneUpLvRq req = msg.getExtension(StoneUpLvRq.ext);
        StoneService service = getService(StoneService.class);
        StoneUpLvRs resp = service.stoneUpLv(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(StoneUpLvRs.EXT_FIELD_NUMBER, StoneUpLvRs.ext, resp);
    }

}
