package com.gryphpoem.game.zw.handler.client.factory;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb1.RecruitSpeedRq;
import com.gryphpoem.game.zw.pb.GamePb1.RecruitSpeedRs;
import com.gryphpoem.game.zw.service.FactoryService;

/**
 * 募兵加速
 *
 * @author tyler
 */
public class RecruitSpeedHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        RecruitSpeedRq req = msg.getExtension(RecruitSpeedRq.ext);
        FactoryService fctoryService = getService(FactoryService.class);
        RecruitSpeedRs resp = fctoryService.getRecruitSpeedRs(getRoleId(), req.getId(), req.getEndTime(), req.getItemId(), req.getIsGoldSpeed(), req.getItemNum());
        sendMsgToPlayer(RecruitSpeedRs.EXT_FIELD_NUMBER, RecruitSpeedRs.ext, resp);
    }
}
