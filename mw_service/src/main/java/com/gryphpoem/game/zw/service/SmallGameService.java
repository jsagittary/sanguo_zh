package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticSmallGameDataMgr;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticSmallGame;
import com.gryphpoem.game.zw.resource.pojo.SmallGame;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 小游戏, 渠道导流使用
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-11-24 11:23
 */
@Service
public class SmallGameService {
    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 获取玩家小游戏信息
     *
     * @param player
     * @return
     */
    public GetSmallGameRs getSmallGameInfo(Player player) {
        SmallGame smallGame = player.getSmallGame();
        GetSmallGameRs.Builder rsp = GetSmallGameRs.newBuilder();
        if (Objects.nonNull(smallGame)) {
            CommonPb.SmallGame.Builder sgBuilder = CommonPb.SmallGame.newBuilder();
            if (CheckNull.nonEmpty(smallGame.getAwardSet())) {
                sgBuilder.addAllAward(smallGame.getAwardSet());
            }
            if (CheckNull.nonEmpty(smallGame.getExtAwardSet())) {
                sgBuilder.addAllExtAward(smallGame.getExtAwardSet());
            }
            rsp.setSmallGame(sgBuilder);
        }
        return rsp.build();
    }

    public DrawSmallGameAwardRs drawSmallGameAward(Player player, DrawSmallGameAwardRq req) throws MwException {
        int awardType = req.getAwardType();
        if (awardType != 1 && awardType != 2) throw new MwException(GameError.PARAM_ERROR.getCode());
        int id = req.getId();
        StaticSmallGame staticSmallGame = StaticSmallGameDataMgr.getStaticSmallGame(id);
        if (Objects.isNull(staticSmallGame)) throw new MwException(GameError.PARAM_ERROR.getCode());
        List<List<Integer>> awardList = awardType == 1 ? staticSmallGame.getAward() : staticSmallGame.getExtAward();
        if (CheckNull.isEmpty(awardList)) throw new MwException(GameError.NO_CONFIG.getCode());
        SmallGame smallGame = player.getSmallGame();
        if (smallGame == null) {
            smallGame = new SmallGame();
            player.setSmallGame(smallGame);
        }
        Set<Integer> idSet = awardType == 1 ? smallGame.getAwardSet() : smallGame.getExtAwardSet();
        if (idSet.contains(id)) throw new MwException(GameError.PARAM_ERROR.getCode(),
                String.format("roleId: %d, 已经领取过id: %d 的奖励", player.getLordId(), id));
        //此处改为纯客户端判断,用来支持一些未知的小游戏更新时不需要更新服务器
        //服务器只保证玩家奖励不会被重复领取
//        List<List<Integer>> condList = staticSmallGame.getCond();
        idSet.add(id);
        List<CommonPb.Award> pbAward = rewardDataManager.sendReward(player, awardList, AwardFrom.DRAW_SMALL_GAME_AWARD, id);
        DrawSmallGameAwardRs.Builder rsp = DrawSmallGameAwardRs.newBuilder();
        rsp.addAllAward(pbAward);
        return rsp.build();
    }

}
