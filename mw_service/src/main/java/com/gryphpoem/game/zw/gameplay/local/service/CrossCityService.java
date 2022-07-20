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
 * @Description 跨服城池逻辑相关处理
 * @author QiuKun
 * @date 2019年4月1日
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
     * 根据cityId获取mapId
     * 
     * @param cityId
     * @return
     * @throws MwException
     */
    private int getMapIdByCityId(int cityId) throws MwException {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (staticCity == null) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "未找到城池 cityId:", cityId);
        }
        return staticCity.getArea();
    }

    /**
     * 跨服城池征收
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
        // 检查城池是否存在
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池征收，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        // 本阵营判断
        City city = cityMapEntity.getCity();
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "城池征收，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }

        // 检查城池现在是否有产出
        if (!city.hasFirstKillReward(roleId) && city.getProduced() <= 0) {// 既没有首杀奖励 又没产出时
            throw new MwException(GameError.CITY_NO_PRODUCED.getCode(), "城池当前没有已产出次数，不能征收, roleId:", roleId,
                    ", cityId:", cityId, ", produced:", city.getProduced());
        }
        // 检查是否有双倍卡
        long doubleCardCnt = 0;
        Integer porpId = StaticPropDataMgr.getCityLevyCardPorpId(staticCity.getType());
        if (porpId != null) {
            doubleCardCnt = rewardDataManager.getRoleResByType(player, AwardType.PROP, porpId);
        }
        int cost = 1; // 消耗的倍数
        if (doubleCardCnt > 0) {
            try {
                // 检测双倍资源是否充足
                rewardDataManager.checkPlayerResIsEnough(player, staticCity.getLevy(), 2, "城池征收");
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, porpId, 1, AwardFrom.USE_PROP);
                cost = 2; // 使用双倍卡
            } catch (MwException e) {
                LogUtil.debug("roleId:", roleId, " 城池征收双倍卡使用失败,资源不足");
            }
        } else { // 征收城的图纸，双倍征收道具数量为0
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_CITY_LEVY, player);
        }

        // 检查征收消耗并扣除
        rewardDataManager.checkAndSubPlayerRes(player, staticCity.getLevy(), cost, AwardFrom.CITY_LEVY);

        // 随机奖励结果并发送奖励
        int rewardNum = 1;// 奖励次数
        List<Integer> otherRandomAward;
        List<List<Integer>> rewardList = new ArrayList<>();
        for (int i = 0; i < rewardNum; i++) {
            rewardList.add(staticCity.randomDropReward());
            otherRandomAward = staticCity.randomOtherReward();
            if (CheckNull.nonEmpty(otherRandomAward)) {
                rewardList.add(staticCity.randomOtherReward());
            }
        }
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        int sum = num * cost; // 总数
        List<Award> awards = rewardDataManager.sendReward(player, rewardList, sum, AwardFrom.CITY_LEVY);

        boolean useFristKillReward = false;
        // 首杀奖励
        if (city.hasFirstKillReward(roleId)) {
            city.getFirstKillReward().put(roleId, 1); // 消耗首杀奖励
            LogUtil.debug("消耗首杀奖励 cityId:", city.getCityId(), ", roleId:", roleId);
            useFristKillReward = true;
        } else {
            // 征收操作，更新城池产出
            useFristKillReward = false;
        }
        city.levy(TimeHelper.getCurrentSecond());

        // 发系统消息
        if (cost == 1) {
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_LEVY, player.lord.getCamp(), 0, player.lord.getNick(),
                    cityId, rewardList.get(0).get(1), sum);
        } else if (cost == 2) {
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_LEVY_DOUBLE, player.lord.getCamp(), 0,
                    player.lord.getNick(), cityId, rewardList.get(0).get(1), sum, porpId);
        }

        // 攻城掠地
        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_CITY_LEVY);

        // 推送给他人
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        CityLevyRs.Builder builder = CityLevyRs.newBuilder();
        builder.setFinishTime(city.getFinishTime());
        builder.addAllAward(awards);
        builder.setUseFristKill(useFristKillReward);
        return builder.build();
    }

    /**
     * 获取单个城池信息
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
     * 跨服城池重建
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
        // 检查城池是否存在
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池征收，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        City city = cityMapEntity.getCity();
        // 检查城池是否属于本国
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "城池重建，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
        // 检查城池是否已有主人
        if (city.getOwnerId() > 0) {
            throw new MwException(GameError.CITY_HAVE_OWNER.getCode(), "城池已有城主了, roleId:", roleId, ", cityId:", cityId,
                    ", ownerId:", city.getOwnerId());
        }
        // 检查玩家是否已是其他城的城主
        long cityOwnerCount = cMap.getCityMap().values().stream().filter(c -> c.getCity().getOwnerId() == roleId)
                .count();
        if (cityOwnerCount > 0) {
            throw new MwException(GameError.OWN_OTHER_CITY.getCode(), "只能拥有一座城池, roleId:", roleId, ", cityId:", cityId);
        }
        // 检测玩家是否参与其他城池竞选
        CityMapEntity oCity = cMap.getCityMap().values().stream().filter(c -> c.getCity().roleHasJoinRebuild(roleId))
                .findFirst().orElse(null);
        if (oCity != null) {
            throw new MwException(GameError.ALREADY_JOIN_CAMPAGIN_OTHER.getCode(), "已参加其他城池竞选, roleId:", roleId,
                    ", cityId:", cityId, ", otherCityId:", oCity.getCity().getCityId());
        }
        // 检查城池是否已有主人
        if (city.getOwnerId() > 0) {
            throw new MwException(GameError.CITY_HAVE_OWNER.getCode(), "城池已有城主了, roleId:", roleId, ", cityId:", cityId,
                    ", ownerId:", city.getOwnerId());
        }
        String cityLordName = null;// 城主
        // 竞选中，加入竞选列表
        if (city.isInCampagin()) {
            if (city.roleHasJoinRebuild(roleId)) {
                throw new MwException(GameError.ROLE_HAS_REBUILD_CITY.getCode(), "玩家已对改成发起过重建, roleId:", roleId,
                        ", cityId:", cityId, ", ownerId:", city.getOwnerId());
            }
            // 检查并扣除消耗
            rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRebuild(), AwardFrom.CITY_REBUILD);
            city.getCampaignList()
                    .add(CityService.createCampaignRolePb(player.lord.getNick(), player.lord.getRanks(), roleId));
            LogUtil.debug("加入竞选列表=" + roleId + ",city=" + city);
        } else {
            // 解决bug 同时加入多个城池选举， 导致拥有多个城池
            if (city.roleHasJoinRebuild(roleId)) {// 已经对其他城池申请过
                throw new MwException(GameError.ROLE_HAS_REBUILD_CITY.getCode(), "玩家已对改成发起过重建, roleId:", roleId,
                        ", cityId:", cityId, ", ownerId:", city.getOwnerId());
            }
            // 检查并扣除消耗
            rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRebuild(), AwardFrom.CITY_REBUILD);
            // 更新城池拥有者
            city.setOwner(roleId, TimeHelper.getCurrentSecond());
            // 发送竞选成功邮件
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_NEWMAP_CAMPAIGN_SUCC, TimeHelper.getCurrentSecond(),
                    city.getCityId(), city.getCityId());
            LogUtil.debug("当城主,不用竞选=" + roleId + ",city=" + city);
            // 记录军团日志
            Turple<Integer, Integer> xy = staticCity.getCityPosXy();
            PartyLogHelper.addPartyLog(player.lord.getCamp(), PartyConstant.LOG_CITY_REBUILD, player.lord.getNick(),
                    city.getCityId(), xy.getA(), xy.getB());
            cityLordName = player.lord.getNick();

            // 如果在交战中，则更新兵力
            MapWarData mapWarData = cMap.getMapWarData();
            List<Integer> battleIdList = mapWarData.getBattlePosCache().get(staticCity.getCityPos());
            if (!CheckNull.isEmpty(battleIdList)) {
                for (Integer battleId : battleIdList) {
                    BaseMapBattle baseMapBattle = mapWarData.getAllBattles().get(battleId);
                    if (baseMapBattle != null) {
                        Battle b = baseMapBattle.getBattle();
                        b.updateArm(city.getCamp(), city.getCurArm());
                        b.setDefencer(player);// 设置城主信息
                    }
                }
            }
            syncPartyCity(city, staticCity, cMap);
        }
        // 攻城掠地
        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_CAMPAGIN); // 申请城主

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
     * 跨服城池修复
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
        // 检查城池是否存在
        StaticCity staticCity = StaticCrossWorldDataMgr.getCityMap().get(cityId);
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == staticCity || null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池征收，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        City city = cityMapEntity.getCity();
        // 检查城池是否属于本国
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "城池重建，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
//        if (!city.hasOwner()) {
//            throw new MwException(GameError.NO_CASTELLAN_DONOT_REPAIR.getCode(), "城池修复，没有城主的城池不能修复, roleId:", roleId,
//                    ", cityId:", cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
//        }
        int curArm = city.getCurArm();
        int totalArm = city.getTotalArm();
        if (curArm >= totalArm) {
            throw new MwException(GameError.CITY_ARM_FULL.getCode(), "城池兵已满，不用修复 , roleId:", roleId, ", cityId:",
                    cityId, ", curArm:", curArm, ", totalArm:", totalArm);
        }
        // 检查并扣除消耗
        rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRepair(), AwardFrom.CITY_REPAIR);
        // 修复兵力
        cityArmRepair(city, staticCity, cMap);
        // 地图通知
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        CityRepairRs.Builder builder = CityRepairRs.newBuilder();
        builder.setCurArm(city.getCurArm());
        return builder.build();
    }

    /**
     * 城池单次兵力修复
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
        Collections.reverse(formList);// 反序，从后往前遍历
        LogUtil.debug("cityId=" + staticCity.getCityId() + ",totalArm=" + totalArm + ",add=" + add);
        // 获得兵力
        int totalAdd = 0;
        for (CityHero hero : formList) {
            if (add <= 0) {// 修复兵力已用完，退出
                break;
            }

            npc = StaticNpcDataMgr.getNpcMap().get(hero.getNpcId());
            npcArm = npc.getTotalArm();
            if (hero.getCurArm() < npcArm) {// 兵力未满，修复
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

        // 如果在交战中，则更新兵力
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
     * 跨服城主撤离城池
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
        // 检查城池是否存在
        StaticCity staticCity = StaticCrossWorldDataMgr.getCityMap().get(cityId);
        CityMapEntity cityMapEntity = cMap.getCityMapEntityByCityId(cityId);
        if (null == staticCity || null == cityMapEntity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池征收，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        City city = cityMapEntity.getCity();
        // 检查城池是否属于本国
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "撤离城池，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
        // 检查玩家是否是城主
        if (city.getOwnerId() != roleId) {
            throw new MwException(GameError.LEAVE_CITY_NOT_OWNER.getCode(), "不是城主，不能撤离城池, roleId:", roleId, ", cityId:",
                    cityId, ", ownerId:", city.getOwnerId());
        }
        // 更新城池拥有者信息
        city.cleanOwner(false);
        // 记录军团日志
        Turple<Integer, Integer> xy = cMap.posToTurple(cityMapEntity.getPos());
        PartyLogHelper.addPartyLog(player.lord.getCamp(), PartyConstant.LOG_LEAVE_CITY, player.lord.getNick(),
                city.getCityId(), xy.getA(), xy.getB());
        LeaveCityRs.Builder builder = LeaveCityRs.newBuilder();
        builder.setCityId(cityId);
        builder.setCurArm(city.getCurArm());
        builder.setMaxArm(city.getTotalArm());
        // 推送城池
        syncPartyCity(city, staticCity, cMap);
        // 地图通知
        cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        return builder.build();
    }

    /**
     * 推送partyCity
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
     * 跑秒定时器执行
     */
    public void runSecCityTimeLogic() {
        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        // 判断一下时间
        int now = TimeHelper.getCurrentSecond();
        for (CityMapEntity cityEntity : cMap.getCityMap().values()) {
            try {
                processRunSecCity(cMap, cityEntity, now);
            } catch (Exception e) {
                LogUtil.error(e, "城池定时任务执行出错, city:", cityEntity.getCity());
            }
        }
    }

 

    private void processRunSecCity(CrossWorldMap cMap, CityMapEntity cityEntity, int now) throws MwException {
        City city = cityEntity.getCity();
        if (!city.isNpcCity() && !city.isProducedFull()) {// 城池产出处理
            cityService.cityProduceHandle(city, now);
        }
        if (city.isInCampagin()) {// 竞选城主处理
            cityCampaignHandle(city, now, cMap);
        }
        if (city.hasOwner()) {// 城主相关操作
            cityService.cityOwnerEndTimeHandle(city, now);// 城主到期处理
        }
        if (city.isFree()) {
            if (now >= city.getCloseTime()) {// 城池保护结束
                city.endFree();
            }
        }
    }

    /**
     * 城池竞选处理逻辑
     * 
     * @param city
     * @param now
     * @param cMap
     * @throws MwException
     */
    private void cityCampaignHandle(City city, int now, CrossWorldMap cMap) throws MwException {
        if (city.isCampaignEndTime(now)) {// 竞选结束
            // 设置竞选结束
            city.setCampaignTime(0);

            // 军衔最高的人拥有城池
            Player player;
            Player owner = null;
            for (CamppaignRole role : city.getCampaignList()) {
                player = playerDataManager.getPlayer(role.getRoleId());
                if (null == owner || player.lord.getRanks() > owner.lord.getRanks()) {
                    owner = player;
                }
            }

            String nick = null;// 竞选成功玩家名称
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            if (null != owner) {
                city.setOwner(owner.roleId, now);
                nick = owner.lord.getNick();

                // 发送竞选成功邮件
                mailDataManager.sendNormalMail(owner, MailConstant.MOLD_NEWMAP_CAMPAIGN_SUCC, now, city.getCityId(),
                        city.getCityId());

                // 记录军团日志
                Turple<Integer, Integer> xy = staticCity.getCityPosXy();
                PartyLogHelper.addPartyLog(owner.lord.getCamp(), PartyConstant.LOG_CITY_REBUILD, nick, city.getCityId(),
                        xy.getA(), xy.getB());
            }
            // 通过邮件给竞选失败者返还竞选资源
            List<Award> awards = PbHelper.createAwardsPb(staticCity.getRebuild());
            for (CamppaignRole role : city.getCampaignList()) {
                if (role.getRoleId() != owner.roleId) {
                    player = playerDataManager.getPlayer(role.getRoleId());
                    if (player != null) {
                        // 邮件通知
                        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_CAMPAIGN_FAIL,
                                AwardFrom.CITY_CAMPAIGN_FAIL, now, nick, city.getCityId(), nick, city.getCityId());
                        LogUtil.debug("竞选城主失败退还物质  roleId:", player.roleId, ", cityId:", city.getCityId());
                    }
                }
            }
            city.getCampaignList().clear();
            city.getAttackRoleId().clear();// 清空攻城玩家
            // 推送刷新地图数据
            cMap.publishMapEvent(MapEvent.mapEntity(staticCity.getCityPos(), MapCurdEvent.UPDATE));
        }
    }

    /**
     * 获取城池竞选信息
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
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "未找到城池 cityId:", cityId);
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
     * 获取军团城池信息（只获取玩家所在分区的）
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
     * 获取军团战争信息
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
            // 过滤需要显示在集结列表的战斗
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
