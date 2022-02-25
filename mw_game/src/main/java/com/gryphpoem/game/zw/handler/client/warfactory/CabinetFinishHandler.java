package com.gryphpoem.game.zw.handler.client.warfactory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.CabinetFinishRs;
import com.gryphpoem.game.zw.service.WarFactoryService;

/**
 * @ClassName CabinetFinishHandler.java
 * @Description 内阁完成当前点兵任务
 * @author QiuKun
 * @date 2017年7月15日
 */
public class CabinetFinishHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // CabinetFinishRq req = msg.getExtension(CabinetFinishRq.ext);
        WarFactoryService service = getService(WarFactoryService.class);
        CabinetFinishRs resp = service.cabinetFinish(getRoleId());
        if (null != resp) sendMsgToPlayer(CabinetFinishRs.ext, resp);
    }

}
