package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.GetCabinetRs;
import com.gryphpoem.game.zw.service.WarFactoryService;

/**
 * @ClassName GetCabinetHandler.java
 * @Description 内阁获取天策府数据
 * @author QiuKun
 * @date 2017年7月15日
 */
public class GetCabinetHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
//        GetCabinetRq req = msg.getExtension(GetCabinetRq.ext);
        WarFactoryService service = getService(WarFactoryService.class);
        GetCabinetRs resp = service.getCabinetInfo(getRoleId());
        if (null != resp) sendMsgToPlayer(GetCabinetRs.ext, resp);
    }

}
