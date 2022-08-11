package com.gryphpoem.game.zw.handler.client.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.DrawCardService;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-21 16:45
 */
public class SynthesizingHeroFragmentsHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb5.SynthesizingHeroFragmentsRs rsp = DataResource.ac.getBean(DrawCardService.class).
                synthesizingHeroFragments(getRoleId(), msg.getExtension(GamePb5.SynthesizingHeroFragmentsRq.ext).getHeroId());
        if (Objects.nonNull(rsp))
            sendMsgToPlayer(GamePb5.SynthesizingHeroFragmentsRs.ext, rsp);
    }
}
