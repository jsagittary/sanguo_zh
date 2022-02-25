package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossChatRq;
import com.gryphpoem.game.zw.service.CrossChatService;

/**
 * @ClassName GetCrossChatCmd.java
 * @Description 获取跨服聊天消息
 * @author QiuKun
 * @date 2019年5月16日
 */
@Cmd(rqCmd = GetCrossChatRq.EXT_FIELD_NUMBER)
public class GetCrossChatCmd extends PlayerBaseCommond {
    @Autowired
    private CrossChatService crossChatService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        GetCrossChatRq req = base.getExtension(GetCrossChatRq.ext);
        crossChatService.getCrossChat(player, req);
    }

}
