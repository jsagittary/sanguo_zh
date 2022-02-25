package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.GetSolarTermsRs;
import com.gryphpoem.game.zw.service.SolarTermsService;

/**
 * @ClassName GetSolarTermsHandler.java
 * @Description 获取当前节气
 * @author QiuKun
 * @date 2017年11月21日
 */
public class GetSolarTermsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        SolarTermsService service = getService(SolarTermsService.class);
        GetSolarTermsRs resp = service.getSolarTerms(getRoleId());
        if (null != resp) {
            sendMsgToPlayer(GetSolarTermsRs.ext, resp);
        }
    }
}
