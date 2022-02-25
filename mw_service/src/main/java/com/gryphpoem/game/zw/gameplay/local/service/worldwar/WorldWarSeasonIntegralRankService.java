package com.gryphpoem.game.zw.gameplay.local.service.worldwar;

import com.gryphpoem.game.zw.gameplay.local.world.camp.WorldWarIntegral;
import com.gryphpoem.game.zw.gameplay.local.world.camp.WorldWarIntegralRank;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarCampRank;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarPersonalRank;
import com.gryphpoem.game.zw.resource.pojo.PlayerWorldWarData;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pengshuo on 2019/3/23 16:47
 * <br>Description: 世界争霸-世界阵营-积分排行
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonIntegralRankService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private CampDataManager campDataManager;

    @Autowired
    private WorldWarSeasonDateService worldWarSeasonDateService;

    /**
     * 获取玩家 世界阵营-积分排行
     * @param player
     * @return WorldWarIntegralRank
     */
    public WorldWarIntegralRank getWorldWarSeasonIntegralRank(Player player){
        WorldWarIntegralRank integralRank = new WorldWarIntegralRank();
        integralRank.setCampRank(getCampIntegrals());
        integralRank.setPersonalIntegral(
                new WorldWarIntegral(
                        player.lord.getCamp(),player.lord.getLordId()
                        ,player.getPlayerWorldWarData().getSeasonIntegral()
                )
        );
        return integralRank;
    }

    /**
     * 增加世界争霸积分排行积分（玩家积分）
     * @param player
     * @param value
     */
    public void addWorldWarSeasonRankingIntegral(Player player,int value){
        // 当前世界争霸配置
        if(worldWarSeasonDateService.isInSeason() && worldWarSeasonDateService.functionIsOpen(player)){
            int camp = player.lord.getCamp();
            // 个人积分
            player.getPlayerWorldWarData().setSeasonIntegral(
                    player.getPlayerWorldWarData().getSeasonIntegral() + value
            );
            // 阵营积分
            Camp party = campDataManager.getParty(camp);
            party.setWorldWarRankingIntegral(party.getWorldWarRankingIntegral() + value);
            LogLordHelper.commonLog("worldWar", AwardFrom.WORLD_WAR_SEASON_INTEGRAL,
                    player,camp,value,"seasonIntegral");
        }
    }

    /**
     * 增加世界争霸积分排行积分
     * @param camp
     * @param value
     * @see WorldWarSeasonAttackCityService#addAttackCityCampIntegral
     */
    public void addWorldWarSeasonRankingCampIntegral(int camp,int value){
        if(worldWarSeasonDateService.isInSeason()){
            // 阵营积分
            Camp party = campDataManager.getParty(camp);
            party.setWorldWarRankingIntegral(party.getWorldWarRankingIntegral() + value);
        }
    }

    /**
     * 赛季结束，发放阵营、个人排行奖励
     */
    public void seasonOverGiveAward(){
        int now = TimeHelper.getCurrentSecond();
        // 阵营排行奖励
        List<WorldWarIntegral> campIntegrals = getCampIntegrals();
        Stream.iterate(0, i -> ++i).limit(campIntegrals.size()).forEach(i-> {
            // 根据排名获取阵营(获取阵营玩家，获取阵营相关奖励，判断官职发放奖励)
            int camp = campIntegrals.get(i).getCamp();
            int rank = ++i;
            List<StaticWorldWarCampRank> campRanks = StaticCrossWorldDataMgr.getWorldWarCampRankList(rank);
            playerDataManager.getPlayerByCamp(camp).values().stream()
                    .filter(e -> worldWarSeasonDateService.functionIsOpen(e)).forEach(p->{
                int job = p.lord.getJob();
                List<List<Integer>> awardList = campRanks.stream().filter(s -> s.getJob() == job).findFirst()
                        .map(s -> s.getAward()).orElse(null);
                if(awardList != null && !awardList.isEmpty()){
                    mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awardList),
                            MailConstant.WORLD_WAR_CAMP_RANK_REWARD, AwardFrom.WORLD_WAR_CAMP_RANKING,
                            now, "世界争霸阵营积分",rank,job
                    );
                    // 日志记录(阵营、排名、官职、奖励)
                    LogLordHelper.commonLog("worldWar",AwardFrom.WORLD_WAR_CAMP_RANKING,p,camp,rank,job,awardList);
                }
            });
        });

        // 个人排行奖励
        List<WorldWarIntegral> playersIntegral = getPlayersIntegral();
        Stream.iterate(0, i -> ++i).limit(playersIntegral.size()).forEach(i-> {
            Player player = playerDataManager.getPlayer(playersIntegral.get(i).getLordId());
            int rank = ++i;
            List<List<Integer>> awardList = getPersonalRankingAward(worldWarSeasonDateService.worldWarType(),rank);
            if(awardList != null && !awardList.isEmpty()){
                mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(awardList),
                        MailConstant.WORLD_WAR_PERSONAL_RANK_REWARD, AwardFrom.WORLD_WAR_PERSONAL_RANKING,
                        now,"世界争霸个人积分","世界争霸个人积分",rank
                );
                // 日志记录(阵营、排名、奖励)
                LogLordHelper.commonLog("worldWar",AwardFrom.WORLD_WAR_PERSONAL_RANKING,
                        player,player.lord.getCamp(),rank,awardList);
            }
        });
    }

    /**
     * 赛季展示结束 清除玩家赛季数据和阵营赛季积分、军威值
     */
    public void seasonOverClearIntegral(){
        /** 清除阵营赛季 城市征战积分、阵营积分排行积分 importance */
        campDataManager.getPartyMap().entrySet().forEach(e -> {
            e.getValue().setWorldWarAttackCityIntegral(0);
            e.getValue().setWorldWarRankingIntegral(0);
        });
        /** 清除玩家 赛季积分道具 清空赛季数据 */
        playerDataManager.getPlayers().entrySet().forEach(e->{
            // 玩家积分道具
            Prop prop = e.getValue().props.get(PropConstant.WORLD_WAR_INTEGRAL);
            if (null != prop && prop.getCount() > 0) {
                int count = prop.getCount();
                // 扣除道具
                prop.setCount(0);
                // 记录道具变更
                LogLordHelper.prop(AwardFrom.WORLD_WAR_SEASON_CLEAR_INTEGRAL,
                        e.getValue().account,e.getValue().lord ,PropConstant.WORLD_WAR_INTEGRAL,
                        prop.getCount(), count, Constant.ACTION_SUB,"season_over_clear_integral");
            }
            // 玩家赛季积分
            e.getValue().setPlayerWorldWarData(new PlayerWorldWarData());
        });
    }

    /**
     * 获取个人排行奖励
     * @param ranking
     * @return
     * @see StaticWorldWarPersonalRank#ranking
     */
    private List<List<Integer>> getPersonalRankingAward(int worldWarType,int ranking){
        // worldWarType获取
        List<StaticWorldWarPersonalRank> staticWorldWarPersonalRanks
                = Optional.ofNullable(StaticCrossWorldDataMgr.getWorldWarPersonalRank(worldWarType))
                .orElse(new ArrayList<>());
        // 在 排行名次[名次开始,名次结束] 之间
        for (StaticWorldWarPersonalRank s : staticWorldWarPersonalRanks) {
            int beginRanking = s.getRanking().get(0);
            int endRanking = s.getRanking().get(1);
            if(beginRanking <= ranking && ranking <= endRanking){
                return s.getAward();
            }
        }
        return null;
    }

    /**
     * 获取世界争霸积分排行（阵营积分）
     * @return
     */
    public List<WorldWarIntegral> getCampIntegrals(){
        return campDataManager.getPartyMap().entrySet().stream()
                .filter(e -> e.getValue().getWorldWarRankingIntegral() > 0)
                .map(e-> new WorldWarIntegral(e.getKey(),e.getValue().getWorldWarRankingIntegral()))
                .sorted(Comparator.comparingInt(WorldWarIntegral::getValue).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取世界争霸积分排行（玩家积分）
     * @return
     */
    public List<WorldWarIntegral> getPlayersIntegral() {
        return playerDataManager.getPlayers().entrySet().stream()
                .filter(e -> e.getValue().getPlayerWorldWarData().getSeasonIntegral() > 0)
                .map(e -> new WorldWarIntegral(
                                e.getValue().lord.getCamp(), e.getKey(),
                                e.getValue().getPlayerWorldWarData().getSeasonIntegral(),
                                e.getValue().getPlayerWorldWarData().getIntegralSecond()
                        )
                ).sorted(Comparator.comparingInt(WorldWarIntegral::getValue).reversed().thenComparing(WorldWarIntegral::getSecond))
                .collect(Collectors.toList());
    }

    /**
     * 获取玩家世界争霸积分排行 排名
     * @param lordId
     * @return
     */
    public int getPlayerIntegralRanking(long lordId){
        return getPlayersIntegral().stream().map(WorldWarIntegral::getLordId).
                collect(Collectors.toList())
                .indexOf(lordId) + 1;
    }
}
