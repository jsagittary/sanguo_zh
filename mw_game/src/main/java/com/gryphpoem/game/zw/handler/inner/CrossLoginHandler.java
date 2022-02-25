package com.gryphpoem.game.zw.handler.inner;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.InnerHandler;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRs;

/**
 * @ClassName CrossLoginHandler.java
 * @Description 跨服登录返回
 * @author QiuKun
 * @date 2019年5月13日
 */
public class CrossLoginHandler extends InnerHandler {

    @Override
    public void action() throws MwException {
        CrossLoginRs msg = getMsg().getExtension(CrossLoginRs.ext);
        PlayerForCrossService service = getService(PlayerForCrossService.class);
        service.crossLoginCallBack(getLordId(), msg);
    }

}
