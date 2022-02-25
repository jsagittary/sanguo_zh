package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.SyntheticPropRq;
import com.gryphpoem.game.zw.pb.GamePb1.SyntheticPropRs;
import com.gryphpoem.game.zw.service.PropService;

/**
 * @author: ZhouJie
 * @date: Create in 2018-06-08 15:39
 * @description: 合成道具
 * @modified By:
 */
public class SyntheticPropHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SyntheticPropRq req = msg.getExtension(SyntheticPropRq.ext);
        SyntheticPropRs resp = getService(PropService.class).syntheticProp(getRoleId(), req.getPropId());
        sendMsgToPlayer(SyntheticPropRs.EXT_FIELD_NUMBER, SyntheticPropRs.ext, resp);
    }
}
