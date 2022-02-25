package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetPortraitRs;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * @ClassName GetPortraitHandler.java
 * @Description 获取拥有的头像
 * @author QiuKun
 * @date 2017年8月3日
 */
public class GetPortraitHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
//        GetPortraitRq req = msg.getExtension(GetPortraitRq.ext);
        DressUpService service = getService(DressUpService.class);
        GetPortraitRs resp = service.getPortrait(getRoleId());
        if (resp != null) sendMsgToPlayer(GetPortraitRs.ext, resp);
    }

}
