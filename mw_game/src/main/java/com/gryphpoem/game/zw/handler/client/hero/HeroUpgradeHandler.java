package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.HeroUpgradeService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-21 15:54
 */
public class HeroUpgradeHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb5.UpgradeHeroRs rsp = DataResource.ac.getBean(HeroUpgradeService.class).upgradeHero(getRoleId(), msg.getExtension(GamePb5.UpgradeHeroRq.ext).getHeroId());
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.UpgradeHeroRs.ext, rsp);
        }
    }
}
