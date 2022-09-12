package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.hero.HeroBiographyService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-15 10:34
 */
public class GetHeroBiographyInfoHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb5.GetHeroBiographyInfoRs rsp = DataResource.ac.getBean(HeroBiographyService.class).getHeroBiographyInfo(getRoleId());
        if (Objects.nonNull(rsp))
            sendMsgToPlayer(GamePb5.GetHeroBiographyInfoRs.ext, rsp);
    }
}
