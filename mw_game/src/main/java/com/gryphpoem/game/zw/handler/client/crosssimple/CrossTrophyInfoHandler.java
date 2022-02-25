package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.service.CrossDataService;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyInfoRq;
import com.gryphpoem.game.zw.pb.GamePb5.CrossTrophyInfoRs;

/**
 * @ClassName CrossTrophyInfoHandler.java
 * @Description 获取跨服成就信息
 * @author QiuKun
 * @date 2019年5月28日
 */
public class CrossTrophyInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CrossTrophyInfoRq req = getMsg().getExtension(CrossTrophyInfoRq.ext);
        CrossDataService service = getService(CrossDataService.class);
        CrossTrophyInfoRs res = service.crossTrophyInfo(getRoleId(), req);
        if (res != null) {
            sendMsgToPlayer(CrossTrophyInfoRs.ext, res);
        }
    }

}
