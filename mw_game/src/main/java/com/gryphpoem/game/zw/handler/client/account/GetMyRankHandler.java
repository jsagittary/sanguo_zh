package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.GetMyRankRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetMyRankRs;
import com.gryphpoem.game.zw.service.RankService;

/**
 * @ClassName GetMyRankHandler.java
 * @Description 获取自己在排行榜的名次
 * @author QiuKun
 * @date 2018年4月11日
 */
public class GetMyRankHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GetMyRankRq req = msg.getExtension(GetMyRankRq.ext);
        GetMyRankRs resp = getService(RankService.class).getMyRank(getRoleId(), req.getType());
        if (resp != null) sendMsgToPlayer(GetMyRankRs.EXT_FIELD_NUMBER, GetMyRankRs.ext, resp);
    }

}
