package com.gryphpoem.game.zw.gameplay.local.service.warfire;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.rank.RankItem;
import com.gryphpoem.game.zw.core.rank.SimpleRank4SkipSet;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.WarFireUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 战火燎原积分相关服务
 *
 * @Description
 * @Author zhangdh
 * @Date 2020-12-31 15:27
 */
@Component
public class WarFireScoreService {

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 获取积分排名
     *
     * @param lordId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.GetWarFireCampRankRs getWarFireCampRankRq(long lordId, GetWarFireCampRankRq req) throws MwException {
        GlobalWarFire globalWarFire = getGlobalWarFire(req.getMapId());
        int page = req.getPage();
        int camp = req.getCamp();
        int pageCount = Constant.RANK_PAGE_CNT;// 每页显示多少个
        int begin = (page - 1) * pageCount;//包含
        int end = begin + pageCount;//不包含
        GetWarFireCampRankRs.Builder rsb = GetWarFireCampRankRs.newBuilder();
        //阵营积分排名
        SimpleRank4SkipSet<Integer> campRanks = globalWarFire.getCampRanks();
        for (RankItem<Integer> rit : campRanks.getAll()) {
            rsb.addCamps(PbHelper.createTwoIntPb((int) rit.getLordId(), rit.getRankValue()));
        }
        //玩家自己积分与排名信息
        SimpleRank4SkipSet<Integer> playerRanks = globalWarFire.getScoreRanks().get(camp);
        int lordRank = playerRanks.getRank(lordId);
        rsb.setSelfRank(lordRank);
        PlayerWarFire pwf = globalWarFire.getPlayerWarFire(lordId);
        rsb.setSelfScore(pwf != null ? pwf.getScore() : 0);
        //排名列表
        if (begin < playerRanks.size()) {
            List<RankItem<Integer>> pageList = playerRanks.getRankList(begin, end);
            int curRank = begin;
            for (RankItem<Integer> rit : pageList) {
                Player player = playerDataManager.getPlayer(rit.getLordId());
                rsb.addRit(PbHelper.createIntegralRank(player, curRank++, rit));
            }
        }
        return rsb.build();
    }

    /**
     * 阵营据点(占领)积分信息
     *
     * @param req
     * @return
     * @throws MwException
     */
    public GetWarFireCampScoreRs getGetWarFireCampScoreDetail(GetWarFireCampScoreRq req) throws MwException {
        GlobalWarFire globalWarFire = getGlobalWarFire(req.getMapId());
        GetWarFireCampScoreRs.Builder rsb = GetWarFireCampScoreRs.newBuilder();
        //据点信息
        Map<Integer, CityMapEntity> cityMap = globalWarFire.getCrossWorldMap().getCityMap();
        cityMap.forEach((k, v) -> {
            City city = v.getCity();
            rsb.addCity(PbHelper.createTwoIntPb(city.getCityId(), city.getCamp()));
        });
        return rsb.build();
    }

    private GlobalWarFire getGlobalWarFire(int mapId) throws MwException {
        CrossWorldMap crossWorldMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
        GlobalWarFire gwf = crossWorldMap != null ? crossWorldMap.getGlobalWarFire() : null;
        if (gwf != null && (gwf.getStage() == GlobalWarFire.STAGE_RUNNING || gwf.getStage() == GlobalWarFire.STAGE_END_PLAYER)) {
            return gwf;
        }
        throw new MwException(GameError.PARAM_ERROR.getCode(), "活动尚未开始, 当前活动状态 : " + (gwf != null ? gwf.getStage() : 0));
    }


    /**
     * 客户端每分钟刷新一次
     *
     * @param req
     * @return
     * @throws MwException
     */
    public GetWarFireCampSummaryRs getCampSummary(GetWarFireCampSummaryRq req) throws MwException {
        GlobalWarFire globalWarFire = getGlobalWarFire(req.getMapId());
        //阵营玩家数量
        Map<Integer, Integer> campPlayerCountMap = globalWarFire.getCrossWorldMap().getPlayerMap().values().stream()
                .collect(Collectors.toMap(mapEntity -> mapEntity.getPlayer().getCamp(), entity -> 1, Integer::sum));
        //统计阵营每分钟产量
        Map<Integer, Integer> outputMap = WarFireUtil.calcCampOutputMin(globalWarFire);

        GetWarFireCampSummaryRs.Builder rsb = GetWarFireCampSummaryRs.newBuilder();
        globalWarFire.getCampMap().forEach((k, v) -> {
            CommonPb.WarFireCampSummaryPb.Builder builder = CommonPb.WarFireCampSummaryPb.newBuilder();
            builder.setCamp(k);
            builder.setScore(v.getScore());
            Integer op = outputMap.get(k);
            builder.setOutputMin(op == null ? 0 : op);
            Integer cnt = campPlayerCountMap.get(k);
            builder.setPlayerCount(cnt == null ? 0 : cnt);
            rsb.addCampSummary(builder);
        });
        return rsb.build();
    }

    /**
     * 获取玩家战火燎原信息
     *
     * @param lordId
     * @param req
     * @return
     * @throws MwException
     */
    public GetPlayerWarFireRs getPlayerWarFireInfo(long lordId, GetPlayerWarFireRq req) throws MwException {
        GlobalWarFire gwf = getGlobalWarFire(req.getMapId());
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        int outputMin = WarFireUtil.getPlayerOutputMin(gwf, player);
        PlayerWarFire pwf = gwf.getPlayerWarFire(lordId);
        GetPlayerWarFireRs.Builder rsb = GetPlayerWarFireRs.newBuilder();
        rsb.setFirstBloodScore(pwf.getFirstOccupyScore());
        rsb.setKilled(pwf.getKilled());
        rsb.setOutputMin(outputMin);
        rsb.setTotalScore(pwf.getScore());
        pwf.getEvents().forEach(event -> rsb.addWfevt(event.ser()));
        return rsb.build();
    }
}
