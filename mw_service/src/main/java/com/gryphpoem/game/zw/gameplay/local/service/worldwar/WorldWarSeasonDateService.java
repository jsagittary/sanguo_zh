package com.gryphpoem.game.zw.gameplay.local.service.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldWarPlanInfo;
import com.gryphpoem.game.zw.gameplay.local.world.camp.WorldWarAttackCity;
import com.gryphpoem.game.zw.gameplay.local.world.camp.WorldWarIntegral;
import com.gryphpoem.game.zw.gameplay.local.world.camp.WorldWarIntegralRank;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pengshuo on 2019/3/23 16:32
 * <br>Description: 世界争霸-世界阵营数据获取（积分排行、城市征战、赛季商店）
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonDateService {

    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 积分排行
     */
    @Autowired
    private WorldWarSeasonIntegralRankService seasonIntegralRankService;

    /**
     * 城市征战
     */
    @Autowired
    private WorldWarSeasonAttackCityService worldWarAttackCityService;

    /**
     * 赛季商店
     */
    @Autowired
    private WorldWarSeasonShopService worldWarSeasonShopService;

    @Autowired
    private WorldWarSeasonDailyAttackTaskService worldWarSeasonDailyAttackTaskService;

    @Autowired
    private WorldWarSeasonDailyRestrictTaskService worldWarSeasonDailyRestrictTaskService;

    @Autowired
    private WorldWarSeasonWeekIntegralService worldWarSeasonWeekIntegralService;

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;

    /**
     * 数据分页大小
     */
    private final static int PAGE_SIZE = 10;

    /**
     * 每日杀敌奖励领取
     */
    public final static int WORLD_WAR_DAILY_ATTACK_AWARD = 1;
    /**
     * 每日计时奖励领取
     */
    public final static int WORLD_WAR_DAILY_TASK_AWARD = 2;
    /**
     * 每周奖励领取
     */
    public final static int WORLD_WAR_WEEK_AWARD = 3;
    /**
     * 城市征战奖励领取
     */
    public final static int WORLD_WAR_ATTACK_CITY_AWARD = 4;

    /**
     * 获取玩家世界阵营数据
     *
     * @param lordId
     * @return WorldWarCampDateRs
     */
    public WorldWarCampDateRs getPersonalWorldWarCampDate(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        WorldWarCampDateRs.Builder campDateRs = WorldWarCampDateRs.newBuilder();
        // 积分排行
        WorldWarIntegralRank seasonIntegralRank = seasonIntegralRankService.getWorldWarSeasonIntegralRank(player);
        if (seasonIntegralRank != null) {
            CommonPb.WorldWarIntegralRank.Builder integralBuilder = CommonPb.WorldWarIntegralRank.newBuilder();
            // 阵营排行
            List<WorldWarIntegral> campRank = seasonIntegralRank.getCampRank();
            if (campRank != null && !campRank.isEmpty()) {
                int rank = 0;
                for(WorldWarIntegral worldWarIntegral: campRank){
                    CommonPb.WorldWarIntegral.Builder campRankBuilder = CommonPb.WorldWarIntegral.newBuilder();
                    // 阵营
                    campRankBuilder.setCamp(worldWarIntegral.getCamp());
                    // 积分
                    campRankBuilder.setValue(worldWarIntegral.getValue());
                    // 排行
                    campRankBuilder.setRanking(++rank);
                    // 阵营排行榜
                    integralBuilder.addCampRank(campRankBuilder);
                }
            }
            WorldWarIntegral personalIntegral = seasonIntegralRank.getPersonalIntegral();
            if (personalIntegral != null) {
                // 个人排行榜
                CommonPb.WorldWarIntegral.Builder personalBuilder = CommonPb.WorldWarIntegral.newBuilder();
                personalBuilder.setCamp(personalIntegral.getCamp());
                personalBuilder.setLordId(lordId);
                personalBuilder.setValue(personalIntegral.getValue());
                personalBuilder.setRanking(seasonIntegralRankService.getPlayerIntegralRanking(lordId));
                integralBuilder.setPersonalRank(personalBuilder);
            }
            campDateRs.setIntegralRank(integralBuilder);
        }
        // 城市征战
        WorldWarAttackCity worldWarAttackCity = worldWarAttackCityService.getPersonalAttackCityIntegral(player);
        if (worldWarAttackCity != null) {
            CommonPb.WorldWarAttackCity.Builder attackCityBuilder = CommonPb.WorldWarAttackCity.newBuilder();
            // 阵营军威值
            attackCityBuilder.setCampIntegral(worldWarAttackCity.getCampIntegral());
            // 玩家军威值
            attackCityBuilder.setPersonalIntegral(worldWarAttackCity.getPersonalIntegral());
            // 玩家名城值奖励状态
            if (worldWarAttackCity.getAwardRecord() != null) {
                attackCityBuilder.addAllStatus(PbHelper.createTwoIntListByMap(worldWarAttackCity.getAwardRecord()));
            }
            // 红点数量
            attackCityBuilder.setTips(worldWarAttackCity.getTips());
            campDateRs.setAttackCity(attackCityBuilder);
        }
        // 赛季商店
        Map<Integer, Integer> personalSeasonShopData = worldWarSeasonShopService.getPersonalSeasonShopData(lordId);
        if (personalSeasonShopData != null) {
            CommonPb.WorldWarSeasonShop.Builder seasonShopBuilder = CommonPb.WorldWarSeasonShop.newBuilder();
            // 商店兑换数据
            seasonShopBuilder.addAllStatus(PbHelper.createTwoIntListByMap(personalSeasonShopData));
            // 装备兑换劵
            seasonShopBuilder.setExchange(worldWarSeasonShopService.playerEquipExchangeProof(player));
            // 兑换记录
            campDateRs.setSeasonShop(seasonShopBuilder);
        }
        return campDateRs.build();
    }

    /**
     * 玩家排行数据分页
     *
     * @param page
     * @return
     */
    public WorldWarCampRankPlayersDateRs getWorldWarCampRankPlayersDate(int page) {
        WorldWarCampRankPlayersDateRs.Builder campIntegralsBuilder = WorldWarCampRankPlayersDateRs.newBuilder();
        List<WorldWarIntegral> playersIntegral = seasonIntegralRankService.getPlayersIntegral();
        // size
        int size = playersIntegral.size();
        // index
        int start = PAGE_SIZE * (page - 1);
        if (playersIntegral != null && !playersIntegral.isEmpty() && size > start) {
            // 当前分页
            campIntegralsBuilder.setCurrentPage(page);
            // 总数据长度
            campIntegralsBuilder.setTotalSize(size);
            // 分页取值
            int minus = size - start;
            List<WorldWarIntegral> playersIntegralSub;
            if(minus >= PAGE_SIZE){
                playersIntegralSub = playersIntegral.subList(start,start + PAGE_SIZE);
            }else{
                playersIntegralSub = playersIntegral.subList(start,size);
            }
            int rank = 0;
            for (WorldWarIntegral worldWarIntegral : playersIntegralSub) {
                CommonPb.WorldWarIntegral.Builder playerBuilder = CommonPb.WorldWarIntegral.newBuilder();
                // 阵营
                playerBuilder.setCamp(worldWarIntegral.getCamp());
                // lordId
                long lordId = worldWarIntegral.getLordId();
                playerBuilder.setLordId(lordId);
                // nick
                Player cache = playerDataManager.getPlayer(lordId);
                playerBuilder.setNick(cache.lord.getNick());
                // 积分
                playerBuilder.setValue(worldWarIntegral.getValue());
                // 排行
                playerBuilder.setRanking(start + ++rank);
                // 玩家排行榜
                campIntegralsBuilder.addPlayersRank(playerBuilder);
            }
        }
        return campIntegralsBuilder.build();
    }

    /**
     * 获取玩家世界争霸-赛季任务数据
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public WorldWarTaskDateRs getPersonalWorldWarTaskDate(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        WorldWarTaskDateRs.Builder taskDataRs = WorldWarTaskDateRs.newBuilder();
        // 每日杀敌数
        taskDataRs.setDailyAttack(player.getPlayerWorldWarData().getDailyAttack());
        // 每日杀敌奖励
        taskDataRs.addAllDailyAttackAward(PbHelper.createTwoIntListByMap(player.getPlayerWorldWarData().getDailyAttackAward()));
        // 每日限定任务完成
        taskDataRs.addAllRestrictTask(PbHelper.createTwoIntListByMap(player.getPlayerWorldWarData().getRestrictTask()));
        // 每日限定任务奖励领取
        taskDataRs.addAllRestrictTaskAward(PbHelper.createTwoIntListByMap(player.getPlayerWorldWarData().getRestrictTaskAward()));
        // 周积分
        taskDataRs.setWeekIntegral(player.getPlayerWorldWarData().getWeekIntegral());
        // 周积分领取记录
        taskDataRs.addAllWeekIntegralAward(PbHelper.createTwoIntListByMap(player.getPlayerWorldWarData().getWeekAward()));
        // 每日杀敌待领取数量、每日限定任务待领取数量、周积分待领取奖励数量
        Map<Integer,Integer> tips = new HashMap<>(3);
        tips.put(WORLD_WAR_DAILY_ATTACK_AWARD, worldWarSeasonDailyAttackTaskService.getDailyTips(player));
        tips.put(WORLD_WAR_DAILY_TASK_AWARD, worldWarSeasonDailyRestrictTaskService.getDailyTips(player));
        tips.put(WORLD_WAR_WEEK_AWARD, worldWarSeasonWeekIntegralService.getWeekTips(player));
        taskDataRs.addAllTips(PbHelper.createTwoIntListByMap(tips));
        return taskDataRs.build();
    }

    /**
     * 异步推送红点
     * @param player
     * @param keyId
     * @param tips
     */
    public void syncAwardChange(Player player,Integer keyId,int tips){
        SyncWorldWarAwardTipsRs.Builder actBuild = SyncWorldWarAwardTipsRs.newBuilder();
        actBuild.addAward(PbHelper.createTwoIntPb(keyId, tips));
        Base.Builder builder = PbHelper.createSynBase(
                SyncWorldWarAwardTipsRs.EXT_FIELD_NUMBER,SyncWorldWarAwardTipsRs.ext,actBuild.build()
        );
        MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
    }

    /**
     * 活动领取奖励
     *
     * @param lordId
     * @param rq
     * @return
     */
    public WorldWarAwardRs getPlayerWorldWarAward(long lordId, WorldWarAwardRq rq) throws MwException {
        switch (rq.getType()) {
            case WORLD_WAR_DAILY_ATTACK_AWARD:
                return worldWarSeasonDailyAttackTaskService.playerGetDailyTaskAward(lordId, rq);
            case WORLD_WAR_DAILY_TASK_AWARD:
                return worldWarSeasonDailyRestrictTaskService.playerGetDailyTaskAward(lordId, rq);
            case WORLD_WAR_WEEK_AWARD:
                return worldWarSeasonWeekIntegralService.playerGetWeekTaskAward(lordId, rq);
            case WORLD_WAR_ATTACK_CITY_AWARD:
                return worldWarAttackCityService.playerGetWarAttackCityAward(lordId, rq);
            default:
                return null;
        }
    }

    /**
     * 是否在赛季中,定时器只有在赛季中才会去执行
     * @return
     */
    public boolean isInSeason(){
        return crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID).isInSeason();
    }

    /**
     * 获取当前世界争霸档位
     * @return
     */
    public int worldWarType(){
        CrossWorldMap crossWorldMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        WorldWarPlanInfo worldWarPlanInfo = crossWorldMap.getWorldWarOpen().getWorldWarPlanInfo();
        if(worldWarPlanInfo != null){
            return worldWarPlanInfo.getWorldWarType();
        }
        return 0;
    }

    /**
     * 检查玩家是否开启世界争霸
     * @param player
     * @return
     */
    public boolean functionIsOpen(Player player){
        return StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_ENTER_WORLDWAR);
    }

}
