package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeBodyImageRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeBodyImageRs;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * @ClassName ChangeBodyImageHandler.java
 * @Description 修改形象
 * @author QiuKun
 * @date 2018年9月1日
 */
public class ChangeBodyImageHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChangeBodyImageRq req = msg.getExtension(ChangeBodyImageRq.ext);
        DressUpService service = getService(DressUpService.class);
        ChangeBodyImageRs resp = service.changeBodyImage(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(ChangeBodyImageRs.ext, resp);

    }

}
