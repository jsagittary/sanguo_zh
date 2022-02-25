package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetBodyImageRs;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * @ClassName GetBodyImageHandler.java
 * @Description 获取拥有的形象
 * @author QiuKun
 * @date 2018年9月1日
 */
public class GetBodyImageHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetBodyImageRq req = msg.getExtension(GetBodyImageRq.ext);
        DressUpService service = getService(DressUpService.class);
        GetBodyImageRs resp = service.getBodyImage(getRoleId());
        if (resp != null) sendMsgToPlayer(GetBodyImageRs.ext, resp);
    }

}
