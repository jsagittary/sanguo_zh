package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.service.CrossDataService;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossInfoRs;

/**
 * @ClassName GetCrossInfoHandler.java
 * @Description 获取跨服信息(时间信息,buff信息)
 * @author QiuKun
 * @date 2019年5月16日
 */
public class GetCrossInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetCrossInfoRq req = getMsg().getExtension(GetCrossInfoRq.ext);
        CrossDataService service = getService(CrossDataService.class);
        GetCrossInfoRs res = service.getCrossInfo(getRoleId(), req);
        if (res != null) sendMsgToPlayer(GetCrossInfoRs.ext, res);
    }

}
