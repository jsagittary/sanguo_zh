package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ChangePortraitRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangePortraitRs;
import com.gryphpoem.game.zw.service.DressUpService;

/**
 * @ClassName ChangePortraitHandler.java
 * @Description 修改头像
 * @author QiuKun
 * @date 2017年8月3日
 */
public class ChangePortraitHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChangePortraitRq req = msg.getExtension(ChangePortraitRq.ext);
        DressUpService service = getService(DressUpService.class);
        ChangePortraitRs resp = service.changePortrait(getRoleId(), req.getPortraitId());
        if (resp != null) sendMsgToPlayer(ChangePortraitRs.ext, resp);
    }

}
