package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.activity.ActivityMonsterNianService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-22 21:30
 */
public class SetOffFirecrackersNianHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.SetOffFirecrackersRq req = msg.getExtension(GamePb4.SetOffFirecrackersRq.ext);
        ActivityMonsterNianService service = getService(ActivityMonsterNianService.class);
        GamePb4.SetOffFirecrackersRs res = service.setOffFirecrackers(getRoleId(), req);
        if (res != null) sendMsgToPlayer(GamePb4.SetOffFirecrackersRs.ext, res);
    }
}
