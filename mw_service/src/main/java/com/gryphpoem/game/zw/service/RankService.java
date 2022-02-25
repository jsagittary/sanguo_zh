package com.gryphpoem.game.zw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RankDataManager;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRq;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetMyRankRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;

/**
 * 排行榜
 * 
 * @author tyler
 *
 */
@Service
public class RankService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RankDataManager rankDataManager;

    public GetRankRs getRank(Long roleId, GetRankRq req ) throws MwException {
        //int type, int page, int scope
        // req.getType(), req.getPage(),req.getScope()
        int type =  req.getType();
        int page =  req.getPage();
        int scope = req.getScope();
        int camp = req.getCamp();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Lord myLord = player.lord;
        return rankDataManager.getRank(myLord, type, page, scope,camp);
    }

    /**
     * 获取自己的排名
     * 
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public GetMyRankRs getMyRank(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Lord myLord = player.lord;
        GetMyRankRs.Builder builder = GetMyRankRs.newBuilder();
        builder.setWorldRank(rankDataManager.getMyRankByTypeAndScop(type, myLord, RankDataManager.WORLD_SCOPE));
        builder.setAreaRank(rankDataManager.getMyRankByTypeAndScop(type, myLord, RankDataManager.AREA_SCOPE));
        builder.setCampRank(rankDataManager.getMyRankByTypeAndScop(type, myLord, RankDataManager.CAMP_SCOPE));
        return builder.build();
    }
}
