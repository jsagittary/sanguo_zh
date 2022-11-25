package com.gryphpoem.game.zw.service.dominate;

import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-22 15:17
 */
public interface IDominateWorldMapService {
    /**
     * 获取活动信息
     *
     * @param roleId
     * @param req
     * @return
     */
     GamePb8.GetDominateWorldMapInfoRs getDominateWorldMapInfo(long roleId, GamePb8.GetDominateWorldMapInfoRq req);

    /**
     * 攻击雄踞一方城池
     *
     * @param roleId
     * @param req
     * @return
     */
     GamePb8.AttackDominateCityRs attackDominateCity(long roleId, GamePb8.AttackDominateCityRq req);

    /**
     * 获取雄踞一方城池详情
     *
     * @param roleId
     * @param req
     * @return
     */
     GamePb8.GetDominateDetailRs getDominateDetail(long roleId, GamePb8.GetDominateDetailRq req);

    /**
     * 获取雄踞一方城池历届都督
     *
     * @param roleId
     * @param req
     * @return
     */
     GamePb8.GetDominateGovernorListRs getDominateGovernorList(long roleId, GamePb8.GetDominateGovernorListRq req);

    /**
     * 获取雄踞一方排行榜信息
     *
     * @param roleId
     * @param req
     * @return
     */
     GamePb8.GetDominateRankRs getDominateRank(long roleId, GamePb8.GetDominateRankRq req);

    /**
     * 行军达到
     *
     * @param player
     * @param army
     * @param nowSec
     */
     void marchEnd(Player player, Army army, int nowSec);

    /**
     * 同步雄踞一方活动信息
     *
     */
    void syncDominateWorldMapInfo(WorldPb.BaseWorldFunctionPb builder);

    /**
     * 获取地图玩法id
     *
     * @return
     */
    int getWorldMapFunction();
}
