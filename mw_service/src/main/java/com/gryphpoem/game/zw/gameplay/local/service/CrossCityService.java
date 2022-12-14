package com.gryphpoem.game.zw.gameplay.local.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.CamppaignRole;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyBattleRs;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyCityRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncPartyCityRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.CampService;
import com.gryphpoem.game.zw.service.CityService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CrossCityService.java
 * @Description ??????????????????????????????
 * @author QiuKun
 * @date 2019???4???1???
 */
@Component
public class CrossCityService {

    @Autowired
    CrossWorldMapService crossWorldMapService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private CityService cityService;
    @Autowired
    private CampService campService;

    /**
     * ??????cityId??????mapId
     * 
     * @param cityId
     * @return
     * @throws MwException
     */
    private int getMapIdByCityId(int cityId) throws MwException {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (staticCity == null) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "??????????????? cityId:", cityId);
        }
        return staticCity.getArea();
    }

    /**
     * ??????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CityLevyRs crossCityLevy(Player player, int cityId) throws MwException {
        int mapId = getMapIdByCityId(cityId);
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);

        long roleId = player.lord.getLordId();
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        // ????????????????????????
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "??????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId);
        }
        // ???????????????
        City city = cityMapEntity.getCity();
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "???????????????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }

        // ?????????????????????????????????
        if (!city.hasFirstKillReward(roleId) && city.getProduced() <= 0) {// ????????????????????? ???????????????
            throw new MwException(GameError.CITY_NO_PRODUCED.getCode(), "????????????????????????????????????????????????, roleId:", roleId,
                    ", cityId:", cityId, ", produced:", city.getProduced());
        }
        // ????????????????????????
        long doubleCardCnt = 0;
        Integer porpId = StaticPropDataMgr.getCityLevyCardPorpId(staticCity.getType());
        if (porpId != null) {
            doubleCardCnt = rewardDataManager.getRoleResByType(player, AwardType.PROP, porpId);
        }
        int cost = 1; // ???????????????
        if (doubleCardCnt > 0) {
            try {
                // ??????????????????????????????
                rewardDataManager.checkPlayerResIsEnough(player, staticCity.getLevy(), 2, "????????????");
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, porpId, 1, AwardFrom.USE_PROP);
                cost = 2; // ???????????????
            } catch (MwException e) {
                LogUtil.debug("roleId:", roleId, " ?????????????????????????????????,????????????");
            }
        } else { // ????????????????????????????????????????????????0
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_CITY_LEVY, player);
        }

        // ???????????????????????????
        rewardDataManager.checkAndSubPlayerRes(player, staticCity.getLevy(), cost, AwardFrom.CITY_LEVY);

        // ?????????????????????????????????
        int rewardNum = 1;// ????????????
        List<List<Integer>> otherRandomAward;
        List<List<Integer>> rewardList = new ArrayList<>();
        for (int i = 0; i < rewardNum; i++) {
            rewardList.add(staticCity.randomDropReward());
            otherRandomAward = staticCity.randomOtherReward();
            if (CheckNull.nonEmpty(otherRandomAward)) {
                rewardList.addAll(staticCity.randomOtherReward());
            }
        }
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        int sum = num * cost; // ??????
        List<Award> awards = rewardDataManager.sendReward(player, rewardList, sum, AwardFrom.CITY_LEVY);

        boolean useFristKillReward = false;
        // ????????????
        if (city.hasFirstKillReward(roleId)) {
            city.getFirstKillReward().put(roleId, 1); // ??????????????????
            LogUtil.debug("?????????????????? cityId:", city.getCityId(), ", roleId:", roleId);
            useFristKillReward = true;
        } else {
            // ?????????????????????????????????
            useFristKillReward = false;
        }
        city.levy(TimeHelper.getCurrentSecond());

        // ???????????????
        if (cost == 1) {
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_LEVY, player.lord.getCamp(), 0, player.lord.getNick(),
                    cityId, rewardList.get(0).get(1), sum);
        } else if (cost == 2) {
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_LEVY_DOUBLE, player.lord.getCamp(), 0,
                    player.lord.getNick(), cityId, rewardList.get(0).get(1), sum, porpId);
        }

        // ????????????
        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_CITY_LEVY);

        // ???????????????
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        CityLevyRs.Builder builder = CityLevyRs.newBuilder();
        builder.setFinishTime(city.getFinishTime());
        builder.addAllAward(awards);
        builder.setUseFristKill(useFristKillReward);
        return builder.build();
    }

    /**
     * ????????????????????????
     * 
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public GetCityRs getCity(Player player, int cityId) throws MwException {
        int mapId = getMapIdByCityId(cityId);
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        City city = cityMapEntity.getCity();
        GetCityRs.Builder builder = GetCityRs.newBuilder();
        if (null != staticCity && null != city) {
            builder.setCity(PbHelper.createCityPb(city));
        }
        return builder.build();
    }

    /**
     * ??????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CityRebuildRs crossCityRebuild(Player player, int cityId) throws MwException {
        int mapId = getMapIdByCityId(cityId);
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        long roleId = player.lord.getLordId();
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        // ????????????????????????
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "??????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId);
        }
        City city = cityMapEntity.getCity();
        // ??????????????????????????????
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "???????????????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
        // ??????????????????????????????
        if (city.getOwnerId() > 0) {
            throw new MwException(GameError.CITY_HAVE_OWNER.getCode(), "?????????????????????, roleId:", roleId, ", cityId:", cityId,
                    ", ownerId:", city.getOwnerId());
        }
        // ??????????????????????????????????????????
        long cityOwnerCount = cMap.getCityMap().values().stream().filter(c -> c.getCity().getOwnerId() == roleId)
                .count();
        if (cityOwnerCount > 0) {
            throw new MwException(GameError.OWN_OTHER_CITY.getCode(), "????????????????????????, roleId:", roleId, ", cityId:", cityId);
        }
        // ??????????????????????????????????????????
        CityMapEntity oCity = cMap.getCityMap().values().stream().filter(c -> c.getCity().roleHasJoinRebuild(roleId))
                .findFirst().orElse(null);
        if (oCity != null) {
            throw new MwException(GameError.ALREADY_JOIN_CAMPAGIN_OTHER.getCode(), "???????????????????????????, roleId:", roleId,
                    ", cityId:", cityId, ", otherCityId:", oCity.getCity().getCityId());
        }
        // ??????????????????????????????
        if (city.getOwnerId() > 0) {
            throw new MwException(GameError.CITY_HAVE_OWNER.getCode(), "?????????????????????, roleId:", roleId, ", cityId:", cityId,
                    ", ownerId:", city.getOwnerId());
        }
        String cityLordName = null;// ??????
        // ??????????????????????????????
        if (city.isInCampagin()) {
            if (city.roleHasJoinRebuild(roleId)) {
                throw new MwException(GameError.ROLE_HAS_REBUILD_CITY.getCode(), "?????????????????????????????????, roleId:", roleId,
                        ", cityId:", cityId, ", ownerId:", city.getOwnerId());
            }
            // ?????????????????????
            rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRebuild(), AwardFrom.CITY_REBUILD);
            city.getCampaignList()
                    .add(CityService.createCampaignRolePb(player.lord.getNick(), player.lord.getRanks(), roleId));
            LogUtil.debug("??????????????????=" + roleId + ",city=" + city);
        } else {
            // ??????bug ????????????????????????????????? ????????????????????????
            if (city.roleHasJoinRebuild(roleId)) {// ??????????????????????????????
                throw new MwException(GameError.ROLE_HAS_REBUILD_CITY.getCode(), "?????????????????????????????????, roleId:", roleId,
                        ", cityId:", cityId, ", ownerId:", city.getOwnerId());
            }
            // ?????????????????????
            rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRebuild(), AwardFrom.CITY_REBUILD);
            // ?????????????????????
            city.setOwner(roleId, TimeHelper.getCurrentSecond());
            // ????????????????????????
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_NEWMAP_CAMPAIGN_SUCC, TimeHelper.getCurrentSecond(),
                    city.getCityId(), city.getCityId());
            LogUtil.debug("?????????,????????????=" + roleId + ",city=" + city);
            // ??????????????????
            Turple<Integer, Integer> xy = staticCity.getCityPosXy();
            PartyLogHelper.addPartyLog(player.lord.getCamp(), PartyConstant.LOG_CITY_REBUILD, player.lord.getNick(),
                    city.getCityId(), xy.getA(), xy.getB());
            cityLordName = player.lord.getNick();

            // ????????????????????????????????????
            MapWarData mapWarData = cMap.getMapWarData();
            List<Integer> battleIdList = mapWarData.getBattlePosCache().get(staticCity.getCityPos());
            if (!CheckNull.isEmpty(battleIdList)) {
                for (Integer battleId : battleIdList) {
                    BaseMapBattle baseMapBattle = mapWarData.getAllBattles().get(battleId);
                    if (baseMapBattle != null) {
                        Battle b = baseMapBattle.getBattle();
                        b.updateArm(city.getCamp(), city.getCurArm());
                        b.setDefencer(player);// ??????????????????
                    }
                }
            }
            syncPartyCity(city, staticCity, cMap);
        }
        // ????????????
        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_CAMPAGIN); // ????????????

        CityRebuildRs.Builder builder = CityRebuildRs.newBuilder();
        builder.setEndTime(city.getEndTime());
        builder.setCurArm(city.getCurArm());
        builder.setTotal(city.getTotalArm());
        if (cityLordName != null) {
            builder.setNick(cityLordName);
        }
        builder.addAllRole(city.getCampaignList());
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));

        return builder.build();
    }

    /**
     * ??????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CityRepairRs crossCityRepair(Player player, int cityId) throws MwException {
        int mapId = getMapIdByCityId(cityId);
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        long roleId = player.lord.getLordId();
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        // ????????????????????????
        StaticCity staticCity = StaticCrossWorldDataMgr.getCityMap().get(cityId);
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == staticCity || null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "??????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId);
        }
        City city = cityMapEntity.getCity();
        // ??????????????????????????????
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "???????????????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
//        if (!city.hasOwner()) {
//            throw new MwException(GameError.NO_CASTELLAN_DONOT_REPAIR.getCode(), "????????????????????????????????????????????????, roleId:", roleId,
//                    ", cityId:", cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
//        }
        int curArm = city.getCurArm();
        int totalArm = city.getTotalArm();
        if (curArm >= totalArm) {
            throw new MwException(GameError.CITY_ARM_FULL.getCode(), "?????????????????????????????? , roleId:", roleId, ", cityId:",
                    cityId, ", curArm:", curArm, ", totalArm:", totalArm);
        }
        // ?????????????????????
        rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRepair(), AwardFrom.CITY_REPAIR);
        // ????????????
        cityArmRepair(city, staticCity, cMap);
        // ????????????
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        CityRepairRs.Builder builder = CityRepairRs.newBuilder();
        builder.setCurArm(city.getCurArm());
        return builder.build();
    }

    /**
     * ????????????????????????
     * 
     * @param city
     * @param staticCity
     * @param cMap
     */
    private void cityArmRepair(City city, StaticCity staticCity, CrossWorldMap cMap) {
        int npcArm;
        StaticNpc npc;
        int totalArm = city.getTotalArm();
        int add = (int) Math.ceil(totalArm * WorldConstant.CITY_REPAIR_RATIO);
        List<CityHero> formList = new ArrayList<>(city.getFormList());
        Collections.reverse(formList);// ???????????????????????????
        LogUtil.debug("cityId=" + staticCity.getCityId() + ",totalArm=" + totalArm + ",add=" + add);
        // ????????????
        int totalAdd = 0;
        for (CityHero hero : formList) {
            if (add <= 0) {// ??????????????????????????????
                break;
            }

            npc = StaticNpcDataMgr.getNpcMap().get(hero.getNpcId());
            npcArm = npc.getTotalArm();
            if (hero.getCurArm() < npcArm) {// ?????????????????????
                if (hero.getCurArm() + add > npcArm) {
                    add -= (npcArm - hero.getCurArm());
                    totalAdd += (npcArm - hero.getCurArm());
                    hero.setCurArm(npcArm);
                } else {
                    hero.addArm(add);
                    totalAdd += add;
                    add = 0;
                }
            }
        }

        // ????????????????????????????????????
        MapWarData mapWarData = cMap.getMapWarData();
        List<Integer> battleIdList = mapWarData.getBattlePosCache().get(staticCity.getCityPos());
        if (!CheckNull.isEmpty(battleIdList)) {
            for (Integer battleId : battleIdList) {
                BaseMapBattle baseMapBattle = mapWarData.getAllBattles().get(battleId);
                if (baseMapBattle != null) {
                    Battle b = baseMapBattle.getBattle();
                    b.updateArm(city.getCamp(), totalAdd);
                }
            }
        }
    }

    /**
     * ????????????????????????
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public LeaveCityRs crossLeaveCity(Player player, int cityId) throws MwException {
        int mapId = getMapIdByCityId(cityId);
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        long roleId = player.lord.getLordId();
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        // ????????????????????????
        StaticCity staticCity = StaticCrossWorldDataMgr.getCityMap().get(cityId);
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == staticCity || null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "??????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId);
        }
        City city = cityMapEntity.getCity();
        // ??????????????????????????????
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "???????????????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
        // ???????????????????????????
        if (city.getOwnerId() != roleId) {
            throw new MwException(GameError.LEAVE_CITY_NOT_OWNER.getCode(), "?????????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId, ", ownerId:", city.getOwnerId());
        }
        // ???????????????????????????
        city.cleanOwner(false);
        // ??????????????????
        Turple<Integer, Integer> xy = cMap.posToTurple(cityMapEntity.getPos());
        PartyLogHelper.addPartyLog(player.lord.getCamp(), PartyConstant.LOG_LEAVE_CITY, player.lord.getNick(),
                city.getCityId(), xy.getA(), xy.getB());
        LeaveCityRs.Builder builder = LeaveCityRs.newBuilder();
        builder.setCityId(cityId);
        builder.setCurArm(city.getCurArm());
        builder.setMaxArm(city.getTotalArm());
        // ????????????
        syncPartyCity(city, staticCity, cMap);
        // ????????????
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        return builder.build();
    }

    /**
     * ??????partyCity
     * 
     * @param city
     * @param staticCity
     * @param cMap
     */
    private void syncPartyCity(City city, StaticCity staticCity, CrossWorldMap cMap) {
        if (city == null || staticCity == null) return;
        List<Player> playerList = cMap.getPlayerMap().values().stream().map(mp -> mp.getPlayer())
                .collect(Collectors.toList());
        SyncPartyCityRs.Builder builder = SyncPartyCityRs.newBuilder();
        builder.setPartyCity(PbHelper.createPartyCityPb(city, playerDataManager));
        SyncPartyCityRs spcPb = builder.build();
        for (Player player : playerList) {
            if (player.isLogin && player.ctx != null) {
                Base.Builder msg = PbHelper.createSynBase(SyncPartyCityRs.EXT_FIELD_NUMBER, SyncPartyCityRs.ext, spcPb);
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * ?????????????????????
     */
    public void runSecCityTimeLogic() {
        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        // ??????????????????
        int now = TimeHelper.getCurrentSecond();
        for (CityMapEntity cityEntity : cMap.getCityMap().values()) {
            try {
                processRunSecCity(cMap, cityEntity, now);
            } catch (Exception e) {
                LogUtil.error(e, "??????????????????????????????, city:", cityEntity.getCity());
            }
        }
    }

 

    private void processRunSecCity(CrossWorldMap cMap, CityMapEntity cityEntity, int now) throws MwException {
        City city = cityEntity.getCity();
        if (!city.isNpcCity() && !city.isProducedFull()) {// ??????????????????
            cityService.cityProduceHandle(city, now);
        }
        if (city.isInCampagin()) {// ??????????????????
            cityCampaignHandle(city, now, cMap);
        }
        if (city.hasOwner()) {// ??????????????????
            cityService.cityOwnerEndTimeHandle(city, now);// ??????????????????
        }
        if (city.isFree()) {
            if (now >= city.getCloseTime()) {// ??????????????????
                city.endFree();
            }
        }
    }

    /**
     * ????????????????????????
     * 
     * @param city
     * @param now
     * @param cMap
     * @throws MwException
     */
    private void cityCampaignHandle(City city, int now, CrossWorldMap cMap) throws MwException {
        if (city.isCampaignEndTime(now)) {// ????????????
            // ??????????????????
            city.setCampaignTime(0);

            // ??????????????????????????????
            Player player;
            Player owner = null;
            for (CamppaignRole role : city.getCampaignList()) {
                player = playerDataManager.getPlayer(role.getRoleId());
                if (null == owner || player.lord.getRanks() > owner.lord.getRanks()) {
                    owner = player;
                }
            }

            String nick = null;// ????????????????????????
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            if (null != owner) {
                city.setOwner(owner.roleId, now);
                nick = owner.lord.getNick();

                // ????????????????????????
                mailDataManager.sendNormalMail(owner, MailConstant.MOLD_NEWMAP_CAMPAIGN_SUCC, now, city.getCityId(),
                        city.getCityId());

                // ??????????????????
                Turple<Integer, Integer> xy = staticCity.getCityPosXy();
                PartyLogHelper.addPartyLog(owner.lord.getCamp(), PartyConstant.LOG_CITY_REBUILD, nick, city.getCityId(),
                        xy.getA(), xy.getB());
            }
            // ????????????????????????????????????????????????
            List<Award> awards = PbHelper.createAwardsPb(staticCity.getRebuild());
            for (CamppaignRole role : city.getCampaignList()) {
                if (role.getRoleId() != owner.roleId) {
                    player = playerDataManager.getPlayer(role.getRoleId());
                    if (player != null) {
                        // ????????????
                        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_CAMPAIGN_FAIL,
                                AwardFrom.CITY_CAMPAIGN_FAIL, now, nick, city.getCityId(), nick, city.getCityId());
                        LogUtil.debug("??????????????????????????????  roleId:", player.roleId, ", cityId:", city.getCityId());
                    }
                }
            }
            city.getCampaignList().clear();
            city.getAttackRoleId().clear();// ??????????????????
            // ????????????????????????
            cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        }
    }

    /**
     * ????????????????????????
     * 
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public GetCityCampaignRs getCityCampaign(Player player, int cityId) throws MwException {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        int mapId = staticCity.getArea();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (cityMapEntity == null) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "??????????????? cityId:", cityId);
        }
        City city = cityMapEntity.getCity();
        GetCityCampaignRs.Builder builder = GetCityCampaignRs.newBuilder();
        if (null != staticCity && null != city) {
            List<CamppaignRole> campaignList = city.getCampaignList();
            if (!CheckNull.isEmpty(campaignList)) {
                for (CamppaignRole role : campaignList) {
                    long rId = role.getRoleId();
                    Player p = playerDataManager.getPlayer(rId);
                    if (p != null && p.lord != null) {
                        CamppaignRole.Builder rb = CamppaignRole.newBuilder();
                        rb.setRoleId(p.lord.getLordId());
                        rb.setNick(p.lord.getNick());
                        rb.setRanks(p.lord.getRanks());
                        builder.addList(rb);
                    }
                }
            }
            builder.addAllAtkRoles(city.getAttackRoleId());
            builder.setEndTime(city.getCampaignTime());
        }
        return builder.build();
    }

    /**
     * ????????????????????????????????????????????????????????????
     * 
     * @param player
     * @return
     * @throws MwException
     */
    public GetPartyCityRs getPartyCity(Player player) throws MwException {
        int mapId = player.lord.getArea();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);

        GetPartyCityRs.Builder builder = GetPartyCityRs.newBuilder();
        Collection<CityMapEntity> cityCollection = cMap.getCityMap().values();
        int myCmap = player.lord.getCamp();
        long myLordId = player.lord.getLordId();
        cityCollection.stream().filter(
                cityEntity -> cityEntity.getCity().getCamp() == myCmap && cityEntity.getCity().getOwnerId() != myLordId)
                .forEach(cityEntity -> builder
                        .addCity(PbHelper.createPartyCityPb(cityEntity.getCity(), playerDataManager)));
        cityCollection.stream().filter(
                cityEntity -> cityEntity.getCity().getCamp() == myCmap && cityEntity.getCity().getOwnerId() == myLordId)
                .findFirst().ifPresent(cityEntity -> builder
                        .addCity(PbHelper.createPartyCityPb(cityEntity.getCity(), playerDataManager)));
        return builder.build();
    }

    /**
     * ????????????????????????
     * 
     * @param player
     * @return
     * @throws MwException
     */
    public GetPartyBattleRs getPartyBattle(Player player) throws MwException {
        int mapId = player.lord.getArea();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        GetPartyBattleRs.Builder builder = GetPartyBattleRs.newBuilder();
        for (BaseMapBattle baseBattle : cMap.getMapWarData().getAllBattles().values()) {
            Battle battle = baseBattle.getBattle();
            // ??????????????????????????????????????????
            if (!campService.checkRallyBattle(battle, player)) {
                continue;
            }
            if (battle.getType() == WorldConstant.BATTLE_TYPE_CAMP) {
                builder.addBattle(PbHelper.createBattlePb(battle));
            } else if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY) {
                builder.addBattle(
                        PbHelper.createBattlePb(battle, BaseMapBattle.getDefArmCntByBattle(battle, cMap)));
            }
        }
        return builder.build();
    }

}
