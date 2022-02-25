package com.gryphpoem.game.zw.handler.inner;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.InnerHandler;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.GamePb5.SyncFortHeroRs;

/**
 * @ClassName SyncFortHeroHandler.java
 * @Description 将领信息同步
 * @author QiuKun
 * @date 2019年5月25日
 */
public class SyncFortHeroHandler extends InnerHandler {

    @Override
    public void action() throws MwException {
        // 先做处理再转发给玩家
        SyncFortHeroRs req = msg.getExtension(SyncFortHeroRs.ext);
        getService(PlayerForCrossService.class).syncFortHeroProcess(getLordId(), req);
        directTranspondClient();
    }

}
