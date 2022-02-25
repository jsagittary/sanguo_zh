package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.ScreenAreaFocusRq;
import com.gryphpoem.game.zw.pb.GamePb2.ScreenAreaFocusRs;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName ScreenAreaFocusHandler.java
 * @Description 客户端当前屏幕所在区域
 * @author QiuKun
 * @date 2017年9月18日
 */
public class ScreenAreaFocusHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ScreenAreaFocusRq req = msg.getExtension(ScreenAreaFocusRq.ext);
        WorldService service = getService(WorldService.class);
        ScreenAreaFocusRs resp = service.screenAreaFocus(getRoleId(), req.getAreaId());

        if (null != resp) {
            sendMsgToPlayer(ScreenAreaFocusRs.ext, resp);
        }
    }

}
