package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.hero.HeroBiographyService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-15 10:37
 */
public class UpgradeHeroBiographyHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb5.UpgradeHeroBiographyRq rq = msg.getExtension(GamePb5.UpgradeHeroBiographyRq.ext);
        GamePb5.UpgradeHeroBiographyRs builder = DataResource.ac.getBean(HeroBiographyService.class).upgradeHeroBiography(getRoleId(), rq.getType());
        if (Objects.nonNull(builder)) {
            sendMsgToPlayer(GamePb5.UpgradeHeroBiographyRs.ext, builder);
        }
    }
}
