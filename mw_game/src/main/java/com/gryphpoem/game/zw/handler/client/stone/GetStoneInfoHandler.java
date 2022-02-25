package com.gryphpoem.game.zw.handler.client.stone;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetStoneInfoRs;
import com.gryphpoem.game.zw.service.StoneService;

/**
 * @ClassName GetStoneInfoHandler.java
 * @Description 获取宝石信息
 * @author QiuKun
 * @date 2018年5月9日
 */
public class GetStoneInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        StoneService service = getService(StoneService.class);
        GetStoneInfoRs resp = service.getStoneInfo(getRoleId());
        if (resp != null) sendMsgToPlayer(GetStoneInfoRs.EXT_FIELD_NUMBER, GetStoneInfoRs.ext, resp);
    }
}
