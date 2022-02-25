package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.SendCrossChatRq;
import com.gryphpoem.game.zw.service.CrossChatService;

/**
 * @ClassName SendCrossChatCmd.java
 * @Description
 * @author QiuKun
 * @date 2019年5月13日
 */
@Cmd(rqCmd = SendCrossChatRq.EXT_FIELD_NUMBER)
public class SendCrossChatCmd extends PlayerBaseCommond {

    @Autowired
    private CrossChatService crossChatService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        SendCrossChatRq req = base.getExtension(SendCrossChatRq.ext);
        crossChatService.sendCrossChat(player, req);
    }

}
