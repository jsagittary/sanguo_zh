package com.gryphpoem.game.zw.handler.inner;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.InnerHandler;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.GamePb5.SyncCrossWarFinishRs;

/**
 * @ClassName SyncCrossWarFinishHandler.java
 * @Description 跨服战结束
 * @author QiuKun
 * @date 2019年5月30日
 */
public class SyncCrossWarFinishHandler extends InnerHandler {

    @Override
    public void action() throws MwException {
        SyncCrossWarFinishRs req = msg.getExtension(SyncCrossWarFinishRs.ext);
        getService(PlayerForCrossService.class).syncCrossWarFinish(getLordId(), req);
        directTranspondClient();
    }

}
