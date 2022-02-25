package com.gryphpoem.game.zw.handler.client.cross.activity;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossRechargeRankingRq;
import com.gryphpoem.game.zw.pb.GamePb6.GetCrossRechargeRankingRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.activity.cross.CrossRechargeLocalActivityService;

import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-09-01 14:05
 */
public class GetCrossRechargeActivityRankHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb6.GetCrossRechargeRankingRq req = msg.getExtension(GetCrossRechargeRankingRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        CrossRechargeLocalActivityService service = DataResource.ac.getBean(CrossRechargeLocalActivityService.class);
        GetCrossRechargeRankingRs rsp = service.getCrossRechargeRanking(player, req);
        if (Objects.nonNull(rsp)){
            sendMsgToPlayer(GetCrossRechargeRankingRs.ext, rsp);
        }
    }
}
