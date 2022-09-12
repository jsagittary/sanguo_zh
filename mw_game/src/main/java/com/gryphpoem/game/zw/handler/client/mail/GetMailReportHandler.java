package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.AsyncGameHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-12 18:45
 */
public class GetMailReportHandler extends AsyncGameHandler {
    @Override
    public void action() throws Exception {
        Player player = DataResource.ac.getBean(PlayerDataManager.class).checkPlayerIsExist(getRoleId());
        player.mails.get(msg.getExtension(GamePb5.GetMailReportRq.ext));
    }
}
