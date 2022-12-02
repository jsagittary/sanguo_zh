package com.gryphpoem.game.zw.handler.client.dominate;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 16:10
 */
public class GetDominateWorldMapInfoHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb8.GetDominateWorldMapInfoRq req = msg.getExtension(GamePb8.GetDominateWorldMapInfoRq.ext);
        GamePb8.GetDominateWorldMapInfoRs rsp = DataResource.ac.getBean(DominateWorldMapService.class).getDominateWorldMapInfo(getRoleId(), req);
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb8.GetDominateWorldMapInfoRs.ext, rsp);
        }
    }
}
