package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.BlackhawkHeroRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @ClassName BlackhawkHeroHandler.java
 * @Description 黑鹰计划招募将领
 * @author QiuKun
 * @date 2017年7月10日
 */
public class BlackhawkHeroHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
//        BlackhawkHeroRq req = msg.getExtension(BlackhawkHeroRq.ext);
        ActivityService service = getService(ActivityService.class);
        long roleId = getRoleId();
        BlackhawkHeroRs resp = service.blackhawkHero(roleId);
        if (null != resp) {
            sendMsgToPlayer(BlackhawkHeroRs.ext, resp);
        }
    }

}
