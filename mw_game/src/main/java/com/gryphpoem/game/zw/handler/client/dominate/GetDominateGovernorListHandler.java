package com.gryphpoem.game.zw.handler.client.dominate;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 16:15
 */
public class GetDominateGovernorListHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb8.GetDominateGovernorListRs res = DataResource.ac.getBean(DominateWorldMapService.class).
                getDominateGovernorList(getRoleId(), msg.getExtension(GamePb8.GetDominateGovernorListRq.ext));
        if (Objects.nonNull(res)) {
            sendMsgToPlayer(GamePb8.GetDominateGovernorListRs.ext, res);
        }
    }
}