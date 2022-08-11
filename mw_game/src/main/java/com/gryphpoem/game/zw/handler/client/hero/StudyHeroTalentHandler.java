package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.HeroUpgradeService;

/**
 * @program: zombie_trunk
 * @description:
 * @author: zhou jie
 * @create: 2019-10-25 11:32
 */
public class StudyHeroTalentHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb5.StudyHeroTalentRq req = msg.getExtension(GamePb5.StudyHeroTalentRq.ext);
        HeroUpgradeService heroService = getService(HeroUpgradeService.class);
        GamePb5.StudyHeroTalentRs resp = heroService.studyHeroTalent(getRoleId(), req.getHeroId(), req.getType(), req.hasIndex() ? req.getIndex() : 0);

        if (null != resp) {
            sendMsgToPlayer(GamePb5.StudyHeroTalentRs.ext, resp);
        }
    }
}