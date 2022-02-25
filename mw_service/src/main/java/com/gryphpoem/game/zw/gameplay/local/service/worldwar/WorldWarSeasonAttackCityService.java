package com.gryphpoem.game.zw.gameplay.local.service.worldwar;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.camp.WorldWarAttackCity;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRq;
import com.gryphpoem.game.zw.pb.GamePb5.WorldWarAwardRs;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarCampCityAward;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDateService.WORLD_WAR_ATTACK_CITY_AWARD;

/**
 * Created by pengshuo on 2019/3/25 10:31
 * <br>Description: 世界争霸-世界阵营-城市征战
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class WorldWarSeasonAttackCityService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private CampDataManager campDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private WorldWarSeasonIntegralRankService seasonIntegralRankService;

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;

    @Autowired
    private WorldWarSeasonDateService worldWarSeasonDateService;

    /**
     * 获取玩家世界阵营 城市征战 个人、阵营相关军威值
     * @param player
     * @return
     */
    public WorldWarAttackCity getPersonalAttackCityIntegral(Player player){
        WorldWarAttackCity attackCity = new WorldWarAttackCity();
        // 本阵营军威值
        attackCity.setPersonalIntegral(campDataManager.getPartyMap().get(player.lord.getCamp()).getWorldWarAttackCityIntegral());
        // 本阵营积分值
        attackCity.setCampIntegral(campDataManager.getPartyMap().get(player.lord.getCamp()).getWorldWarRankingIntegral());
        // 玩家领奖记录
        attackCity.setAwardRecord(player.getPlayerWorldWarData().getAttackCityAward());
        // 红点数量
        attackCity.setTips(getAttackCityTips(player));
        return attackCity;
    }

    /**
     * 世界争霸 城市征战 - 阵营城市征战领取
     * @param lordId
     * @param rq
     * @return
     * @throws MwException
     */
    public WorldWarAwardRs playerGetWarAttackCityAward(long lordId, WorldWarAwardRq rq) throws MwException {
        int keyId = rq.getKeyId();
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Map<Integer, Integer> attackCityAward = Optional.ofNullable(player.getPlayerWorldWarData().getAttackCityAward()).orElse(new HashMap<>(2));
        // 已领取
        int hasChange = Optional.ofNullable(attackCityAward.get(keyId)).orElse(0);
        StaticWorldWarCampCityAward award = StaticCrossWorldDataMgr.getWorldWarCampCityAwardById(keyId);
        if (award == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId: ", lordId);
        }
        if(hasChange > 0){
            throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取奖励已达上限, roleId: ", lordId);
        }
        // 领取奖励所需军威值
        int value = award.getValue();
        // 达到军威值奖励
        List<List<Integer>> awardList = award.getAward();
        int integral = campDataManager.getPartyMap().get(player.lord.getCamp()).getWorldWarAttackCityIntegral();
        if(integral < value){
            throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(),"世界争霸城市征战玩家军威值不足, roleId: ",
                    lordId, ", need:", value, ", have:", integral);
        }
        if(awardList == null || awardList.isEmpty()){
            throw new MwException(GameError.NO_CONFIG.getCode(), "awardList 找不到配置, roleId: ", lordId);
        }
        // 检查玩家背包
        rewardDataManager.checkBag(player,awardList);
        // 发放奖励
        // 玩家获得装备 "[type,id,cnt]"
        List<CommonPb.Award> awards = rewardDataManager.sendReward(player, awardList, AwardFrom.WORLD_WAR_ATTACK_CITY_INTEGRAL_AWARD, keyId);
        // 保存以获取状态
        attackCityAward.put(keyId,++hasChange);
        player.getPlayerWorldWarData().setAttackCityAward(attackCityAward);
        // 推送红点
        worldWarSeasonDateService.syncAwardChange(player,WORLD_WAR_ATTACK_CITY_AWARD,getAttackCityTips(player));
        // 返回结果
        WorldWarAwardRs.Builder builder = WorldWarAwardRs.newBuilder();
        // 对返回结果进行处理
        builder.addAllAward(awards);
        builder.setKeyId(keyId);
        builder.setType(rq.getType());
        return builder.build();
    }

    /**
     * 增加阵营城市征战军威值
     * <p>新地图生效 area = 26</p>
     * @param player 玩家
     * @param value 积分
     */
    public void addAttackCityIntegral(Player player,int value){
        if(worldWarSeasonDateService.isInSeason() && worldWarSeasonDateService.functionIsOpen(player)){
            int camp = player.lord.getCamp();
            Camp party = campDataManager.getParty(camp);
            party.setWorldWarAttackCityIntegral(party.getWorldWarAttackCityIntegral() + value);
            party.setWorldWarRankingIntegral(party.getWorldWarRankingIntegral() + value);
            LogLordHelper.otherLog("worldWar",AwardFrom.DO_SOME.getCode(),
                    player.lord.getLordId(),camp,value,"attackCity");
        }
    }

    /**
     * 增加阵营城市征战军威值、增加对应赛季积分值
     * <p>新地图生效 area = 26</p>
     */
    public void addAttackCityCampIntegral(){
        if(worldWarSeasonDateService.isInSeason()){
            List<City> cityInArea = worldDataManager.getCityInArea(WorldConstant.AREA_TYPE_13);
            cityInArea.stream()
                    .filter(city -> city != null && !city.isNpcCity())
                    .forEach(city -> {
                        int camp = city.getCamp();
                        int cityPoint = Optional.ofNullable(StaticWorldDataMgr.getCityMap().get(city.getCityId()))
                                .map(StaticCity::getCityPoint).orElse(0);
                        if(cityPoint > 0){
                            Camp party = campDataManager.getParty(camp);
                            party.setWorldWarAttackCityIntegral(party.getWorldWarAttackCityIntegral() + cityPoint);
                            // 增加阵营赛季积分
                            seasonIntegralRankService.addWorldWarSeasonRankingCampIntegral(camp,cityPoint);
                            // 日志记录(定时增加军威值)
                            LogLordHelper.otherLog("worldWar",AwardFrom.WORLD_WAR_ATTACK_CITY_INTEGRAL.getCode(),
                                    city.getCityId(),camp,cityPoint,"attackCity");
                        }
            });
            /*CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            cMap.getCityMap().entrySet().stream()
                    // 非npc 已被占领
                    .filter(e -> e.getValue() != null && e.getValue().getCity() !=null
                            && !e.getValue().getCity().isNpcCity())
                    .forEach(e -> {
                        City city = e.getValue().getCity();
                        int camp = city.getCamp();
                        int cityPoint = Optional.ofNullable(StaticWorldDataMgr.getCityMap().get(city.getCityId()))
                                .map(sc -> sc.getCityPoint()).orElse(0);
                        if(cityPoint > 0){
                            Camp party = campDataManager.getParty(camp);
                            party.setWorldWarAttackCityIntegral(party.getWorldWarAttackCityIntegral() + cityPoint);
                            // 增加阵营赛季积分
                            seasonIntegralRankService.addWorldWarSeasonRankingCampIntegral(camp,cityPoint);
                            // 日志记录(定时增加军威值)
                            LogLordHelper.otherLog("worldWar",AwardFrom.WORLD_WAR_ATTACK_CITY_INTEGRAL.getCode(),
                                    city.getCityId(),camp,cityPoint,"attackCity");
                        }
                    });*/
        }
    }

    /**
     * 获取玩家城市城战未领取数量
     * @param player
     * @return
     */
    private int getAttackCityTips(Player player){
        int value = campDataManager.getPartyMap().get(player.lord.getCamp()).getWorldWarAttackCityIntegral();
        Map<Integer, Integer> integerIntegerMap = player.getPlayerWorldWarData().getAttackCityAward();
        List<StaticWorldWarCampCityAward> worldWarCampCityAward =
                StaticCrossWorldDataMgr.getWorldWarCampCityAward(worldWarSeasonDateService.worldWarType());
        int tips = 0;
        for(StaticWorldWarCampCityAward e : worldWarCampCityAward){
            // 个人值大于条件值，未领取奖励
            if(value >= e.getValue() && !integerIntegerMap.containsKey(e.getId())){
                tips++;
            }
        }
        return tips;
    }

    /**
     * 赛季结束 发放玩家城市征战未领取奖励
     */
    public void seasonOverGiveAward(){
        int now = TimeHelper.getCurrentSecond();
        // 赛季城市征战奖励
        List<StaticWorldWarCampCityAward> warCampCityAwards = StaticCrossWorldDataMgr
                .getWorldWarCampCityAward(worldWarSeasonDateService.worldWarType());
        if(warCampCityAwards != null && !warCampCityAwards.isEmpty()){
            playerDataManager.getPlayers().entrySet().stream().filter(
                    e->worldWarSeasonDateService.functionIsOpen(e.getValue())).forEach(e->{
                int attackCityIntegral = campDataManager.getPartyMap()
                        .get(e.getValue().lord.getCamp()).getWorldWarAttackCityIntegral();
                Map<Integer, Integer> attackCityAward = e.getValue().getPlayerWorldWarData().getAttackCityAward();
                // 未领取并且大于领奖条件发放奖励
                List<List<Integer>> awards = new ArrayList<>();
                warCampCityAwards.forEach(w ->{
                    if(!attackCityAward.containsKey(w.getId()) && attackCityIntegral >= w.getValue()){
                        // 汇总至一个邮件发送
                        awards.addAll(w.getAward());
                    }
                });
                if(!awards.isEmpty()){
                    mailDataManager.sendAttachMail(e.getValue(), PbHelper.createAwardsPb(awards),
                            MailConstant.WORLD_WAR_COMMON_REWARD, AwardFrom.WORLD_WAR_PERSONAL_RANKING,
                            now,"世界争霸城市征战","世界争霸城市征战"
                    );
                    // 日志记录(阵营、排名、奖励)
                    LogLordHelper.commonLog("worldWar",AwardFrom.WORLD_WAR_ATTACK_CITY_INTEGRAL_AWARD,
                            e.getValue(),e.getValue().lord.getCamp(),awards,"attackCityAward");
                }
            });
        }
    }

}
