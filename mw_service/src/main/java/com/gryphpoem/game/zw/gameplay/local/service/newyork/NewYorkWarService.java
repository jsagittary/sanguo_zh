package com.gryphpoem.game.zw.gameplay.local.service.newyork;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.NewYorkPlayerIntegralRank;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.NewYorkWar;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticNewYorkWarAchievement;
import com.gryphpoem.game.zw.resource.pojo.IntegralRank;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant.*;

/**
 * Created by pengshuo on 2019/5/9 9:58
 * <br>Description: ????????????
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class NewYorkWarService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private NewYorkWarAwardService newYorkWarAwardService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private CrossWorldMapService crossWorldMapService;
    @Autowired
    private WarService warService;

    /** ????????????????????? ??????????????????????????????*/
    public void initNewYorkWar() throws MwException{
        NewYorkWar openNewYorkWar = crossWorldMapDataManager.getOpenNewYorkWar();
        // ????????? ?????? ??????
        int preViewSecond = WorldConstant.getNewYorkCron(WorldConstant.NEWYORK_WAR_PRE_TIME,"-",0);
        int beginSecond = WorldConstant.getNewYorkCron(WorldConstant.NEWYORK_WAR_START_END_TIME,"-",0);
        int endSecond = WorldConstant.getNewYorkCron(WorldConstant.NEWYORK_WAR_START_END_TIME,"-",1);
        int roundSecond = (WorldConstant.NEWYORK_WAR_EACH_ROUND_ATTACK + WorldConstant.NEWYORK_WAR_EACH_ROUND_TRUCE) * 60;
        int roundAttackSecond = WorldConstant.NEWYORK_WAR_EACH_ROUND_ATTACK * 60;
        int round = (int)Math.ceil((endSecond - beginSecond) / roundSecond);
        openNewYorkWar.setPreViewDate(preViewSecond);
        openNewYorkWar.setBeginDate(beginSecond);
        openNewYorkWar.setEndDate(endSecond);
        openNewYorkWar.setTotalRound(round);
        Stream.iterate(0,i -> ++i).limit(round).forEach(i -> {
            openNewYorkWar.getBeginRoundDate().add(beginSecond + roundSecond*i);
            openNewYorkWar.getEndRoundDate().add(beginSecond + roundSecond*i + roundAttackSecond);
        });
        // ??????????????????????????????
        initNewYorkWarJob(openNewYorkWar);
    }

    /** ?????????????????????????????? */
    public void initNewYorkWarJob(NewYorkWar newYorkWar) {
        int now = TimeHelper.getCurrentSecond();
        // ????????????(?????????1??????)
        ScheduleManager.getInstance().addOrModifyDefultJob(
                new DefultJob("NewYorkWarPreheatJob","NewYorkWar"),job -> {
                    //??????????????????????????????????????????????????????
                    LogUtil.common("--??????????????????????????????1H?????????--");
                    crossWorldMapDataManager.getNewYorkWar().setStatus(NEW_YORK_CITY_PRE_VIEW);
                },new Date((newYorkWar.getBeginDate() - 60*60) * 1000L));
        // ????????????(?????????5??????)
        ScheduleManager.getInstance().addOrModifyDefultJob(
                new DefultJob("NewYorkWarImmediatelyJob","NewYorkWar"), job -> {
                    // ??????
                    LogUtil.common("--??????????????????????????????5m?????????--");
                    chatDataManager.sendSysChat(ChatConst.CHAT_NEW_YORK_WAR_IMMEDIATELY, 0, 0);
                }, new Date((newYorkWar.getBeginDate() - 5*60) * 1000L));
        // ????????????????????????
        newYorkWar.getBeginRoundDate().stream().filter(second -> now <= second).forEach(second->
            ScheduleManager.getInstance().addOrModifyDefultJob(
                    new DefultJob("NewYorkWarRoundStartJob-" + second, "NewYorkWar"), job -> {
                        LogUtil.common("--????????????????????????????????????--" + second);
                        // ??????
                        chatDataManager.sendSysChat(ChatConst.CHAT_NEW_YORK_WAR_ROUND_START, 0, 0);
                        // ???????????????????????????????????????????????????
                        crossWorldMapDataManager.getNewYorkWar().setStatus(NEW_YORK_CITY_ATTACK);
                        // ??????battle??????
                        createNewYorkWarBattle(second + WorldConstant.NEWYORK_WAR_EACH_ROUND_ATTACK * 60);
                    }, new Date(second * 1000L))
        );
        // ??????????????????
        ScheduleManager.getInstance().addOrModifyDefultJob(
                new DefultJob("NewYorkWarClosingJob","NewYorkWar"),job -> {
                    LogUtil.common("--????????????????????????????????????--");
                    //????????????????????????????????????????????????
                    crossWorldMapDataManager.getNewYorkWar().setStatus(NEW_YORK_CITY_END);
                    // ????????????
                    newYorkWarAwardService.giveNewYorkWarAward();
                    // ???????????????
                    removeNewYorkWarJob(newYorkWar);
                },new Date((newYorkWar.getEndDate() + 1) * 1000L));
        // ?????????????????????????????????
        playerDataManager.getAllOnlinePlayer().values().forEach(player -> syncActivityChange(player,newYorkWar));
    }

    /** ?????????????????????battle?????? */
    public void createNewYorkWarBattle(int battleTime){
        CrossWorldMap crossWorldMap = crossWorldMapDataManager.getCrossWorldMapById(CROSS_MAP_ID);
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(NEW_YORK_CITY_ID);
        CityMapEntity cityMapEntity = crossWorldMap.getCityMapEntityByCityId(NEW_YORK_CITY_ID);
        City city = cityMapEntity.getCity();
        if (null == staticCity) {
            LogUtil.error("?????????????????????????????????????????? cityId: ", NEW_YORK_CITY_ID);
            return;
        }
        // battle
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_NEW_YORK_WAR);
        battle.setBattleTime(battleTime);
        battle.setPos(staticCity.getCityPos());
        battle.setDefCamp(city.getCamp());
        battle.addDefArm(city.getCurArm());
        city.setStatus(WorldConstant.CITY_STATUS_BATTLE);
        int now = TimeHelper.getCurrentSecond();
        if(city.getProtectTime() > now){
            city.setProtectTime(now);
        }
        // ????????????
        crossWorldMapDataManager.getNewYorkWar().setFinalOccupyCamp(city.getCamp());
        // ??????Battle
        BaseMapBattle baseBattle = BaseMapBattle.mapBattleFactory(battle);
        crossWorldMap.getMapWarData().addBattle(baseBattle);
        // ????????????
        crossWorldMap.publishMapEvent(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));
    }

    /** ????????????????????????????????? */
    public NewYorkWarInfoRs getNewYorkWarInfo(long roleId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        NewYorkWar openNewYorkWar = Optional.ofNullable(crossWorldMapDataManager.getNewYorkWar()).orElse(new NewYorkWar());
        return NewYorkWarInfoRs.newBuilder().setPreViewDate(openNewYorkWar.getPreViewDate())
        .setBeginDate(openNewYorkWar.getBeginDate())
        .setEndDate(openNewYorkWar.getEndDate())
        .addAllBeginRoundDate(openNewYorkWar.getBeginRoundDate())
        .addAllEndRoundDate(openNewYorkWar.getEndRoundDate())
        .build();
    }

    /** ?????????????????????????????? */
    public NewYorkWarProgressDataRs getNewYorkWarProgressData(long roleId)throws MwException{
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        NewYorkWarProgressDataRs.Builder res = NewYorkWarProgressDataRs.newBuilder();
        NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
        List<IntegralRank> campRanks = newYorkWar.getCampIntegral().values().stream()
                .filter(integralRank -> integralRank.getValue() > 0)
                .sorted(Comparator.comparingLong(IntegralRank::getValue).reversed()
                        .thenComparingInt(IntegralRank::getSecond)
                ).collect(Collectors.toList());
        Stream.iterate(1, i-> ++i).limit(campRanks.size()).forEach(y->{
            IntegralRank rank = campRanks.get(y - 1);
            res.addCampRank(CommonPb.IntegralRank.newBuilder()
                    .setCamp(rank.getCamp()).setValue(rank.getValue()).setRanking(y)
            );
        });
        // ??????
        Optional.ofNullable(newYorkWar.getPlayersIntegral().get(roleId)).ifPresent(p->
            res.setPlayersRank(
                    CommonPb.IntegralRank.newBuilder()
                    .setLordId(roleId).setCamp(p.getCamp()).setValue(p.getValue())
                    .setRanking(getPlayerIntegralRanking(newYorkWar,roleId))
            )
            // ???????????????
            .setLostArmy(p.getLostArmy())
            .setCommanderExp(p.getCommanderExp())
        );
        // ?????? ??????
        res.setMaxAttack(player.newYorkWar.getMaxAttack())
                .addAllAchievementsAward(PbHelper.createTwoIntListByMap(player.newYorkWar.getAchievements()))
                .setTips(getTips(player));
        return res.build();
    }

    /** ???????????????????????????????????????????????? ???????????? ????????????????????? */
    public NewYorkWarPlayerRankDataRs playerNewYorkWarRankVal(long roleId, NewYorkWarPlayerRankDataRq req) throws MwException{
        playerDataManager.checkPlayerIsExist(roleId);
        NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
        Map<Long, NewYorkPlayerIntegralRank> playerData = newYorkWar.getPlayersIntegral();
        List<NewYorkPlayerIntegralRank> playersIntegral = playerData.values()
                .stream().filter(integral -> integral.getValue() >= WorldConstant.NEWYORK_WAR_RANK_MIN_ATTACK)
                .sorted(Comparator.comparingLong(IntegralRank::getValue).reversed())
                .collect(Collectors.toList());

        NewYorkWarPlayerRankDataRs.Builder res = NewYorkWarPlayerRankDataRs.newBuilder();
            // ??????????????????????????????
        res.setCurrentPage(req.getPage());
        res.setTotalSize(playersIntegral.size());
        int rank = 0;
        for (IntegralRank integralRank : playersIntegral) {
            CommonPb.IntegralRank.Builder playerBuilder = CommonPb.IntegralRank.newBuilder();
            long lordId = integralRank.getLordId();
            Player cache = playerDataManager.getPlayer(lordId);
            playerBuilder.setCamp(integralRank.getCamp()).setLordId(lordId).setNick(cache.lord.getNick())
                    .setValue(integralRank.getValue()).setRanking(++rank);
            // ???????????????
            res.addPlayersRank(playerBuilder);
        }
        return res.build();
    }

    /**
     * ????????????
     * @param attackSuccess ????????????
     */
    public void sendSysChat(boolean attackSuccess){
        NewYorkWar newYorkWar = crossWorldMapDataManager.getNewYorkWar();
        int camp = newYorkWar.getFinalOccupyCamp();
        int currentRound = newYorkWar.getCurrentRound() + 1;
        newYorkWar.setCurrentRound(currentRound);
        if(currentRound < newYorkWar.getTotalRound()){
            if(attackSuccess){
                // ??????????????????
                chatDataManager.sendSysChat(ChatConst.CHAT_NEW_YORK_WAR_ATTACK_SUCCESS,0,0,
                        camp,WorldConstant.NEWYORK_WAR_EACH_ROUND_TRUCE);
            }else{
                chatDataManager.sendSysChat(ChatConst.CHAT_NEW_YORK_WAR_DEFEND_SUCCESS,0,0,
                        camp,WorldConstant.NEWYORK_WAR_EACH_ROUND_TRUCE);
            }
        }else{
            // ????????????
            chatDataManager.sendSysChat(ChatConst.CHAT_NEW_YORK_WAR_END, 0, 0,camp);
        }
    }

    /**
     * ????????????????????????????????? ????????????
     * @param forces
     */
    public void addPlayerNewYorkWarAttackVal(List<Force> forces){
        if(forces != null){
            forces.stream().filter(f -> f.roleType == Constant.Role.PLAYER && f.ownerId != 0).forEach(f ->
                addPlayerNewYorkWarAttackVal(playerDataManager.getPlayer(f.ownerId),f.killed,f.totalLost)
            );
        }
    }

    /** ????????????????????????????????? */
    public void checkIsNewYorkWarTime() throws MwException{
        NewYorkWar newYorkWar = crossWorldMapDataManager.getNewYorkWar();
        if(newYorkWar == null){
            // ??????????????????????????????
            return;
        }
        int status = newYorkWar.getStatus();
        if(status == NEW_YORK_CITY_PRE_VIEW){
            throw new MwException(GameError.NEW_YORK_WAR_IMMEDIATELY_START.getCode(), "?????????????????????????????????????????????????????????");
        }
        if(status == NEW_YORK_CITY_ATTACK){
            throw new MwException(GameError.IN_NEW_YORK_WAR.getCode(), "??????????????????????????????????????????????????????");
        }
    }

    /** ?????????????????????????????????????????? */
    private void addPlayerNewYorkWarAttackVal(Player player,long killed,long lost){
        try {
            int a = Optional.ofNullable(WorldConstant.NEWYORK_WAR_LOST_EXP).map(s-> s.get(0)).orElse(1);
            int b = Optional.ofNullable(WorldConstant.NEWYORK_WAR_LOST_EXP).map(s-> s.get(1)).orElse(1);
            NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
            long roleId = player.roleId;
            int camp = player.lord.getCamp();
            int now = TimeHelper.getCurrentSecond();
            // ??????
            if (newYorkWar.getPlayersIntegral().containsKey(roleId)) {
                //????????????
                NewYorkPlayerIntegralRank playerIntegral = newYorkWar.getPlayersIntegral().get(roleId);
                playerIntegral.setValue(playerIntegral.getValue() + killed);
                playerIntegral.setSecond(now);
                playerIntegral.setLostArmy(playerIntegral.getLostArmy() + lost);
                playerIntegral.setCommanderExp(playerIntegral.getCommanderExp() + lost * b / a);
            }else{
                newYorkWar.getPlayersIntegral().put(roleId,new NewYorkPlayerIntegralRank(camp,roleId,killed,now));
                NewYorkPlayerIntegralRank playerIntegral = newYorkWar.getPlayersIntegral().get(roleId);
                playerIntegral.setLostArmy(playerIntegral.getLostArmy() + lost);
                playerIntegral.setCommanderExp(playerIntegral.getCommanderExp() + lost * b / a);
            }
            // ??????
            if (newYorkWar.getCampIntegral().containsKey(camp)) {
                //????????????
                IntegralRank campIntegral = newYorkWar.getCampIntegral().get(camp);
                campIntegral.setValue(campIntegral.getValue() + killed);
                campIntegral.setSecond(now);
            }else{
                newYorkWar.getCampIntegral().put(camp,new IntegralRank(camp,killed,now));
            }
            //???????????? ????????????
            long current = newYorkWar.getPlayersIntegral().get(roleId).getValue();
            if (current > player.newYorkWar.getMaxAttack()) {
                player.newYorkWar.setMaxAttack(current);
            }
        }catch (Exception e){
            LogUtil.error("??????????????????????????????????????????  error",e.getMessage());
        }
    }

    /**
     * ????????????????????????
     * @param player
     * @param openNewYorkWar
     */
    private void syncActivityChange(Player player,NewYorkWar openNewYorkWar){
        SyncNewYorkWarInfoRs.Builder actBuild = SyncNewYorkWarInfoRs.newBuilder();
        actBuild.setPreViewDate(openNewYorkWar.getPreViewDate())
                .setBeginDate(openNewYorkWar.getBeginDate())
                .setEndDate(openNewYorkWar.getEndDate())
                .addAllBeginRoundDate(openNewYorkWar.getBeginRoundDate())
                .addAllEndRoundDate(openNewYorkWar.getEndRoundDate());
        BasePb.Base.Builder builder = PbHelper.createSynBase(
                SyncNewYorkWarInfoRs.EXT_FIELD_NUMBER, SyncNewYorkWarInfoRs.ext,actBuild.build()
        );
        MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
    }

    /**????????????*/
    private int getTips(Player player){
        // ????????????
        long value = player.newYorkWar.getMaxAttack();
        Map<Integer, Integer> achievements = player.newYorkWar.getAchievements();
        List<StaticNewYorkWarAchievement> achievementList = StaticCrossWorldDataMgr.getStaticNewYorkWarAchievement();
        int tips = 0;
        for(StaticNewYorkWarAchievement e : achievementList){
            // ??????????????????????????????????????????
            boolean boo = !achievements.containsKey(e.getId()) || achievements.get(e.getId()) < e.getCount();
            if(value >= e.getCond() && boo){
                tips++;
            }
        }
        return tips;
    }

    /**???????????? ??????*/
    private int getPlayerIntegralRanking(NewYorkWar newYorkWar,long lordId){
        // ??????
        return newYorkWar.getPlayersIntegral().values().stream()
                .filter(i -> i.getValue() >= WorldConstant.NEWYORK_WAR_RANK_MIN_ATTACK)
                .sorted(Comparator.comparingLong(NewYorkPlayerIntegralRank::getValue).reversed()
                        .thenComparingInt(NewYorkPlayerIntegralRank::getSecond)
                ).map(NewYorkPlayerIntegralRank::getLordId)
                .collect(Collectors.toList())
                .indexOf(lordId) + 1;
    }

    /** ??????????????????????????????????????????????????????*/
    private void removeNewYorkWarJob(NewYorkWar newYorkWar) {
        QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(),
                "NewYorkWarPreheatJob","NewYorkWar");
        QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(),
                "NewYorkWarImmediatelyJob","NewYorkWar");
        newYorkWar.getBeginRoundDate().forEach(second ->
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(),
                    "NewYorkWarRoundStartJob-" + second,"NewYorkWar")
        );
        QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(),
                "NewYorkWarClosingJob","NewYorkWar");
    }

    /** gm today ?????? */
    public void gmInitTodayNewYorkWar() throws MwException{
        NewYorkWar openNewYorkWar = crossWorldMapDataManager.getOpenNewYorkWar();
        // ????????? ?????? ??????
        LocalDateTime today = LocalDateTime.now();
        int preViewSecond = TimeHelper.getCurrentSecond() + 60;
        int beginSecond = (int)today.withHour(20).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.of("+8"));
        int endSecond = (int)today.withHour(21).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.of("+8"));
        int roundSecond = (WorldConstant.NEWYORK_WAR_EACH_ROUND_ATTACK + WorldConstant.NEWYORK_WAR_EACH_ROUND_TRUCE) * 60;
        int roundAttackSecond = WorldConstant.NEWYORK_WAR_EACH_ROUND_ATTACK * 60;
        int round = (int)Math.ceil((endSecond - beginSecond) / roundSecond);
        openNewYorkWar.setPreViewDate(preViewSecond);
        openNewYorkWar.setBeginDate(beginSecond);
        openNewYorkWar.setEndDate(endSecond);
        openNewYorkWar.setTotalRound(round);
        Stream.iterate(0,i -> ++i).limit(round).forEach(i -> {
            openNewYorkWar.getBeginRoundDate().add(beginSecond + roundSecond*i);
            openNewYorkWar.getEndRoundDate().add(beginSecond + roundSecond*i + roundAttackSecond);
        });
        // ??????????????????????????????
        initNewYorkWarJob(openNewYorkWar);
    }

    /** gm ??????????????? */
    public void gmCloseNewYorkWar() throws MwException{
        NewYorkWar newYorkWar = crossWorldMapDataManager.getNewYorkWar();
        if(newYorkWar == null){
            return;
        }
        // ????????????
        crossWorldMapDataManager.removeNewYorkWarBattle();
        // ???????????????
        removeNewYorkWarJob(newYorkWar);
        // ????????????
        newYorkWarAwardService.giveNewYorkWarAward();
        // ???????????????
        crossWorldMapDataManager.setNewYorkWar(new NewYorkWar());
    }

    /** gm???????????????????????????????????????????????? */
    public void gmJoinNewYorkWar(int count){
        NewYorkWar newYorkWar = crossWorldMapDataManager.getNewYorkWar();
        if(newYorkWar == null){
            return;
        }
        CrossWorldMap crossWorldMap = crossWorldMapDataManager.getCrossWorldMapById(CROSS_MAP_ID);
        if(crossWorldMap == null){
            return;
        }
        crossWorldMap.getMapWarData().getAllBattles().values().stream()
                .filter(baseMapBattle -> baseMapBattle.getBattle().getType() == WorldConstant.BATTLE_TYPE_NEW_YORK_WAR)
                .forEach(baseMapBattle ->
                    crossWorldMap.getPlayerMap().values().stream().limit(count).forEach(playerMapEntity -> {
                        try {
                            Player player = playerMapEntity.getPlayer();
                            Long roleId = player.roleId;
                            Battle battle = baseMapBattle.getBattle();
                            if(battle.getAtkRoles().contains(roleId) || battle.getDefRoles().contains(roleId)){
                                return;
                            }
                            // ????????????
                            warService.autoFillArmy(player);
                            List<Integer> heroes = player.getAllOnBattleHeros().stream()
                                    .filter(hero -> hero.isIdle() && hero.getCount() > 0)
                                    .map(hero -> hero.getHeroId())
                                    .collect(Collectors.toList());
                            int battleId = baseMapBattle.getBattleId();
                            JoinBattleCrossRq.Builder builder =  JoinBattleCrossRq.newBuilder();
                            builder.setMapId(CROSS_MAP_ID).setBattleId(battleId).addAllHeroId(heroes);
                            crossWorldMapService.joinBattleCross(roleId,builder.build());
                        }catch (Exception e){
                            LogUtil.error("gm ?????????????????????error",e);
                        }
                    })
                );
    }
}
