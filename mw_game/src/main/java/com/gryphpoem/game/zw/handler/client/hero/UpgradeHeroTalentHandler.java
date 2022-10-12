package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.HeroUpgradeService;

/**
 * 升级武将天赋
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/12 9:34
 */
public class UpgradeHeroTalentHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb5.UpgradeHeroTalentRq req = msg.getExtension(GamePb5.UpgradeHeroTalentRq.ext);
        HeroUpgradeService heroUpgradeService = getService(HeroUpgradeService.class);
        GamePb5.UpgradeHeroTalentRs resp = heroUpgradeService.upgradeHeroTalent(getRoleId(), req.getHeroId(), req.hasPage() ? req.getPage() : 0, req.getPart());

        if (null != resp) {
            sendMsgToPlayer(GamePb5.UpgradeHeroTalentRs.ext, resp);
        }
    }
}