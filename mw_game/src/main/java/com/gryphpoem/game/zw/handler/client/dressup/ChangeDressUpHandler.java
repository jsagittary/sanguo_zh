package com.gryphpoem.game.zw.handler.client.dressup;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * 修改装扮数据
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 15:23
 */
public class ChangeDressUpHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.ChangeDressUpRq req = msg.getExtension(GamePb4.ChangeDressUpRq.ext);
        DressUpService service = getService(DressUpService.class);
        GamePb4.ChangeDressUpRs resp = service.changeDressUp(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(GamePb4.ChangeDressUpRs.ext, resp);
    }

}