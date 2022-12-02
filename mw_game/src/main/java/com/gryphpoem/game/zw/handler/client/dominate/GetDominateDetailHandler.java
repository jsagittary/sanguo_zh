package com.gryphpoem.game.zw.handler.client.dominate;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 16:13
 */
public class GetDominateDetailHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb8.GetDominateDetailRs res = DataResource.ac.getBean(DominateWorldMapService.class).
                getDominateDetail(getRoleId(), msg.getExtension(GamePb8.GetDominateDetailRq.ext));
        if (Objects.nonNull(res)) {
            sendMsgToPlayer(GamePb8.GetDominateDetailRs.ext, res);
        }
    }
}
