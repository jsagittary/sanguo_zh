package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.AnniversaryJigsawService;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/7/22 19:01
 */
public class AnniversaryJigsawHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.AnniversaryJigsawRq req = this.msg.getExtension(GamePb4.AnniversaryJigsawRq.ext);
        GamePb4.AnniversaryJigsawRs resp = getService(AnniversaryJigsawService.class).anniversaryJigsaw(req, getRoleId(), req.getActType());
        if (resp != null) {
            sendMsgToPlayer(GamePb4.AnniversaryJigsawRs.ext, resp);
        }
    }
}