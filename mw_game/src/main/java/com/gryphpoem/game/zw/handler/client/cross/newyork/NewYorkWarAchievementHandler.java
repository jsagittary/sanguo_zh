package com.gryphpoem.game.zw.handler.client.cross.newyork;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarAwardService;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarAchievementRq;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarAchievementRs;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Created by pengshuo on 2019/5/14 18:30
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWarAchievementHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        NewYorkWarAchievementRq req = msg.getExtension(NewYorkWarAchievementRq.ext);
        NewYorkWarAwardService service = getService(NewYorkWarAwardService.class);
        NewYorkWarAchievementRs rs = service.lifeLongAward(getRoleId(),req);
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(NewYorkWarAchievementRs.ext,rs);
        }
    }
}
