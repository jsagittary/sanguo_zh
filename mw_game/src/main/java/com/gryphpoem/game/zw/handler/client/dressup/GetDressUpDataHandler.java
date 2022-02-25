package com.gryphpoem.game.zw.handler.client.dressup;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * 获取装扮数据
 * @description:
 * @author: zhou jie
 * @time: 2021/3/9 15:23
 */
public class GetDressUpDataHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.GetDressUpDataRq req = msg.getExtension(GamePb4.GetDressUpDataRq.ext);
        DressUpService service = getService(DressUpService.class);
        GamePb4.GetDressUpDataRs resp = service.getDressUp(getRoleId(), req.getType());
        if (resp != null) sendMsgToPlayer(GamePb4.GetDressUpDataRs.ext, resp);
    }

}