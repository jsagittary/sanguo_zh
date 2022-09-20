package com.gryphpoem.game.zw.handler.client.bandit;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.bandit.BanditService;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-20 18:07
 */
public class SearchBanditHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        DataResource.ac.getBean(BanditService.class).searchBandit(getRoleId(), msg.getExtension(GamePb5.SearchBanditRq.ext));
    }
}
