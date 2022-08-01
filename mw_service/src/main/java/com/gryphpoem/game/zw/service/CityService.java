package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.service.CrossCityService;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.CamppaignRole;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb4.SyncPartyCityRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Events.AreaChangeNoticeEvent;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName CityService.java
 * @Description 城池相关处理 特殊说明：定时处理城池任务cityTimeLogic包含城池产出，竞选，都城自动发起攻城
 * @author TanDonghai
 * @date 创建时间：2017年4月19日 下午8:37:28
 *
 */
@Service
public class CityService extends AbsGameService implements DelayInvokeEnvironment,GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private WorldService worldService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private SuperMineService superMineService;
    @Autowired
    private CrossCityService crossCityService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;

    /**
     * 城池征收
     * 
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public CityLevyRs cityLevy(long roleId, int cityId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if(checkIsCrossCity(cityId)){//本地跨服使用
           return crossCityService.crossCityLevy(player, cityId);
        }
        
        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        if (null == staticCity || null == city) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池征收，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }

        // 检查是否属于本国城池
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "城池征收，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }

        // 检查玩家是否是本城所在区域的
        int area = player.lord.getArea();
        if (staticCity.getArea() != area) {
            throw new MwException(GameError.CAN_NOT_LEVY_OTHER_AREA.getCode(), "不能跨区征收, roleId:", roleId, ", cityId:",
                    cityId, ", roleArea:", area, ", cityArea:", staticCity.getArea());
        }

        // 检查城池是否处于战斗中， 此限制去掉
        // if (city.isInBattle()) {
        // throw new MwException(GameError.CITY_BATTLE_CAN_NOT_LEVY.getCode(), "城池当前在交战中，不能征收, roleId:", roleId,
        // ", cityId:", cityId);
        // }

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
        List<List<Integer>> otherRandomAward;
        List<List<Integer>> rewardList = new ArrayList<>();
        for (int i = 0; i < rewardNum; i++) {
            rewardList.add(staticCity.randomDropReward());
            otherRandomAward = staticCity.randomOtherReward(roleId);
            if (CheckNull.nonEmpty(otherRandomAward)) {
                rewardList.addAll(otherRandomAward);
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
            city.levy(TimeHelper.getCurrentSecond());
            useFristKillReward = false;
        }

        // 判断能否发送跑马灯
        List<List<Integer>> sendChatRewardList = null;
        if (CheckNull.nonEmpty(rewardList)) {
            int curSchId = worldScheduleService.getCurrentSchduleId();
            if (curSchId >= 1 && curSchId <= 3) {
                sendChatRewardList = rewardList;
            } else if (curSchId >= 4 && curSchId <= 7) {
                sendChatRewardList = rewardList.stream()
                        .filter(award -> award.get(0) == AwardType.PROP)
                        .filter(award -> {
                            StaticProp sProp = StaticPropDataMgr.getPropMap(award.get(1));
                            return Objects.nonNull(sProp) && sProp.getQuality() >= Constant.Quality.blue;
                        }).collect(Collectors.toList());
            } else if (curSchId >= 8 && curSchId <= 10) {
                sendChatRewardList = rewardList.stream()
                        .filter(award -> award.get(0) == AwardType.PROP)
                        .filter(award -> {
                            StaticProp sProp = StaticPropDataMgr.getPropMap(award.get(1));
                            return Objects.nonNull(sProp) && sProp.getQuality() >= Constant.Quality.purple;
                        }).collect(Collectors.toList());
            }
        }

        if (CheckNull.nonEmpty(sendChatRewardList)) {
            String chatStr = rewardList.stream().map(list -> {
                if (CheckNull.isEmpty(list))
                    return "";
                return list.stream().map(i -> String.valueOf(i)).collect(Collectors.joining("_"));
            }).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.joining("|"));
            // 发系统消息
            if (StringUtils.isNotBlank(chatStr)) {
                if (cost == 1) {
                    chatDataManager.sendSysChat(ChatConst.CHAT_CITY_LEVY, player.lord.getCamp(), 0, player.lord.getNick(),
                            cityId, chatStr);
                } else if (cost == 2) {
                    chatDataManager.sendSysChat(ChatConst.CHAT_CITY_LEVY_DOUBLE, player.lord.getCamp(), 0,
                            player.lord.getNick(), cityId, chatStr, porpId);
                }
            }
        }


        // 攻城掠地
        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_CITY_LEVY);

        // 推送给他人
        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new AreaChangeNoticeEvent(posList, AreaChangeNoticeEvent.MAP_TYPE));

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
    public GetCityRs getCity(long roleId, int cityId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if(checkIsCrossCity(cityId)){//本地跨服使用
            return crossCityService.getCity(player, cityId);
         }
        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        GetCityRs.Builder builder = GetCityRs.newBuilder();
        if (null != staticCity && null != city) {
            builder.setCity(PbHelper.createCityPb(city));
        }
        int now = TimeHelper.getCurrentSecond();
        if (city.isCaptainCity() && city.getCamp() > 0) {// 都城获取超级矿点信息
            List<SuperMine> smList = worldDataManager.getSuperMineCampMap().get(city.getCamp());
            if (!CheckNull.isEmpty(smList)) {
                for (SuperMine sm : smList) {
                    try {
                        builder.addSuperMine(PbHelper.createSuperMinePbShowCity(sm, now, playerDataManager));
                    } catch (Exception e) {
                        LogUtil.error(e, sm);
                    }
                }
            }
        }
        return builder.build();
    }

    /**
     * 获取城池竞选信息
     * 
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public GetCityCampaignRs getCityCampaign(long roleId, int cityId) throws MwException {
        Player p = playerDataManager.checkPlayerIsExist(roleId);
        if(checkIsCrossCity(cityId)){
            return crossCityService.getCityCampaign(p, cityId);
        }
        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        GetCityCampaignRs.Builder builder = GetCityCampaignRs.newBuilder();
        if (null != staticCity && null != city) {
            List<CamppaignRole> campaignList = city.getCampaignList();
            if (!CheckNull.isEmpty(campaignList)) {
                for (CamppaignRole role : campaignList) {
                    long rId = role.getRoleId();
                    Player player = playerDataManager.getPlayer(rId);
                    if (player != null && player.lord != null) {
                        CamppaignRole.Builder rb = CamppaignRole.newBuilder();
                        rb.setRoleId(player.lord.getLordId());
                        rb.setNick(player.lord.getNick());
                        rb.setRanks(player.lord.getRanks());
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
     * 城池重建
     * 
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public CityRebuildRs cityRebuild(long roleId, int cityId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // if(checkIsCrossCity(cityId)){
        // return crossCityService.crossCityRebuild(player, cityId);
        // }
        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        if (null == staticCity || null == city) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池重建，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }

        // 检查城池是否属于本国
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "城池重建，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }

        // 检查玩家是否是本城所在区域的
        int area = player.lord.getArea();
        if (staticCity.getArea() != area) {
            throw new MwException(GameError.CAN_NOT_REBUILD_OTHER_AREA.getCode(), "不能跨区重建, roleId:", roleId,
                    ", cityId:", cityId, ", roleArea:", area, ", cityArea:", staticCity.getArea());
        }

        // 检查城池是否处于战斗中
        // if (city.isInBattle()) {
        // throw new MwException(GameError.CITY_BATTLE_CAN_NOT_BUILD.getCode(), "城池当前在交战中，不能重建, roleId:", roleId,
        // ", cityId:", cityId);
        // }

        // 检查玩家是否已是其他城的城主
        if (worldDataManager.isCityOwner(roleId)) {
            throw new MwException(GameError.OWN_OTHER_CITY.getCode(), "只能拥有一座城池, roleId:", roleId, ", cityId:", cityId);
        }
        // 检测玩家是否参与其他城池竞选
        City oCity = joinOtherCampagin(roleId);
        if (oCity != null) {
            throw new MwException(GameError.ALREADY_JOIN_CAMPAGIN_OTHER.getCode(), "已参加其他城池竞选, roleId:", roleId,
                    ", cityId:", cityId, ", otherCityId:", oCity.getCityId());
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
            city.getCampaignList().add(createCampaignRolePb(player.lord.getNick(), player.lord.getRanks(), roleId));
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
//            mailDataManager.sendNormalMail(player, MailConstant.MOLD_CAMPAIGN_SUCC, TimeHelper.getCurrentSecond(),
//                    city.getCityId(), city.getCityId());
            //发送重建奖励邮件
            Award ownerAward = PbHelper.createAward(staticCity.getOutAward());
            List<Award> ownerAwards = ListUtils.createList(ownerAward);
            mailDataManager.sendAttachMail(player,ownerAwards,MailConstant.MOLD_CAMPAIGN_SUCC,AwardFrom.CITY_CAMPAIGN_SUC,TimeHelper.getCurrentSecond(),city.getCityId(),"重建",city.getCityId());
            LogUtil.debug("当城主,不用竞选=" + roleId + ",city=" + city);

            // 记录军团日志
            Turple<Integer, Integer> xy = staticCity.getCityPosXy();
            PartyLogHelper.addPartyLog(player.lord.getCamp(), PartyConstant.LOG_CITY_REBUILD, player.lord.getNick(),
                    city.getCityId(), xy.getA(), xy.getB());
            cityLordName = player.lord.getNick();

            // 如果在交战中，则更新兵力
            LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
            if (null != battleList) {
                for (Battle b : battleList) {
                    b.updateArm(city.getCamp(), city.getCurArm());
                    b.setDefencer(player);// 设置城主信息
                }
            }
            syncPartyCity(city, staticCity);
        }

        // 攻城掠地
        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_CAMPAGIN); // 申请城主
        // 推送刷新数据
        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));

        CityRebuildRs.Builder builder = CityRebuildRs.newBuilder();
        builder.setEndTime(city.getEndTime());
        builder.setCurArm(city.getCurArm());
        builder.setTotal(city.getTotalArm());
        if (cityLordName != null) {
            builder.setNick(cityLordName);
        }
        builder.addAllRole(city.getCampaignList());
        return builder.build();
    }

    public static CommonPb.CamppaignRole createCampaignRolePb(String nick, int ranks, long roleId) {
        CommonPb.CamppaignRole.Builder builder = CommonPb.CamppaignRole.newBuilder();
        builder.setNick(nick);
        builder.setRanks(ranks);
        builder.setRoleId(roleId);
        return builder.build();
    }

    /**
     * 是否已经参与了其他城池竞选
     * 
     * @param roleId
     * @return null 没有加过其他城池竞选
     */
    private City joinOtherCampagin(long roleId) {
        Map<Integer, City> cityMap = worldDataManager.getCityMap();
        for (City city : cityMap.values()) {
            if (city.roleHasJoinRebuild(roleId)) {
                return city;
            }
        }
        return null;
    }

    /**
     * 城池修复
     * 
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public CityRepairRs cityRepair(long roleId, int cityId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if(checkIsCrossCity(cityId)){
            return crossCityService.crossCityRepair(player, cityId);
        }
        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        if (null == staticCity || null == city) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "城池修复，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }

        // 检查玩家是否有资格操作
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "城池修复，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }

        if (!city.hasOwner()) {
            throw new MwException(GameError.NO_CASTELLAN_DONOT_REPAIR.getCode(), "城池修复，没有城主的城池不能修复, roleId:", roleId,
                    ", cityId:", cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }

        int curArm = city.getCurArm();
        int totalArm = city.getTotalArm();
        if (curArm >= totalArm) {
            throw new MwException(GameError.CITY_ARM_FULL.getCode(), "城池兵已满，不用修复 , roleId:", roleId, ", cityId:",
                    cityId, ", curArm:", curArm, ", totalArm:", totalArm);
        }

        // 检查并扣除消耗
        rewardDataManager.checkAndSubPlayerRes(player, staticCity.getRepair(), AwardFrom.CITY_REPAIR);

        // 兵力回复
        cityArmRepair(city, staticCity);

        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));

        CityRepairRs.Builder builder = CityRepairRs.newBuilder();
        builder.setCurArm(city.getCurArm());
        return builder.build();
    }

    /**
     * 城池单次兵力修复
     * 
     * @param city
     * @param staticCity
     */
    public void cityArmRepair(City city, StaticCity staticCity) {
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
        LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
        if (null != battleList) {
            for (Battle b : battleList) {
                b.updateArm(city.getCamp(), totalAdd);
            }
        }
    }

    /**
     * 城主撤离城池
     *  MODIFY BY XWIND ON 2021-9-2：设置撤离倒计时
     * @param roleId
     * @param cityId
     * @return
     * @throws MwException
     */
    public LeaveCityRs leaveCity(long roleId, int cityId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if(checkIsCrossCity(cityId)){
            return crossCityService.crossLeaveCity(player, cityId);
        }
        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        if (null == staticCity || null == city) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "撤离城池，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }

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

        // 检查城池是否处于战斗中
        if (city.isInBattle()) {
            throw new MwException(GameError.CITY_BATTLE_CAN_NOT_LEVY.getCode(), "城池当前在交战中，不能撤离, roleId:", roleId,
                    ", cityId:", cityId);
        }

        //检查当前是否处于撤离中
        if(city.getLeaveOver() > 0 && city.getLeaveOver() >= TimeHelper.getCurrentSecond()){
            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"城主当前正在撤离中",cityId));
        }

        city.setLeaveOver(TimeHelper.getCurrentSecond() + Constant.CAMP_REBUILD_LEAVE_SECONDS);

        DELAY_QUEUE.add(new CityDelayRun(city,staticCity,player));

        // 推送刷新数据
        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));

        syncPartyCity(city,staticCity);

        LeaveCityRs.Builder builder = LeaveCityRs.newBuilder();
        builder.setCityId(cityId);
        builder.setCurArm(city.getCurArm());
        builder.setMaxArm(city.getTotalArm());
        builder.setLeaveOver(city.getLeaveOver());
        return builder.build();
    }

    private DelayQueue<CityDelayRun> DELAY_QUEUE = new DelayQueue<>(this);

    public void doLeave(City city,StaticCity staticCity,Player player) {
        try {
            // 更新城池拥有者信息
            city.cleanOwner(false);
            // 记录军团日志
            Turple<Integer, Integer> xy = staticCity.getCityPosXy();
            PartyLogHelper.addPartyLog(player.lord.getCamp(), PartyConstant.LOG_LEAVE_CITY, player.lord.getNick(), city.getCityId(), xy.getA(), xy.getB());
            // 推送城池
            syncPartyCity(city, staticCity);
            // 通知地图数据改变
            List<Integer> posList = new ArrayList<>();
            posList.add(staticCity.getCityPos());
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }catch (Exception e) {
            LogUtil.error("城主撤离阵营城池发生异常, ",e);
        }
    }

    /**
     * 城主奖励逻辑处理每天21点
     */
    public void cityLordRewardTimeJob() {
        Map<Integer, City> cityMap = worldDataManager.getCityMap();
        int now = TimeHelper.getCurrentSecond();
        int today = TimeHelper.getCurrentDay();
        for (City city : cityMap.values()) {
            if (city.hasOwner() && city.getExtraReward() != today && city.getEndTime() >= now) {// 城主相关操作
                city.setExtraReward(today);
                cityExtraRewardHandle(city, now);
            }
        }
    }

    /**
     * 城池相关定时任务
     */
    public void cityTimeLogic() {
        Map<Integer, City> cityMap = worldDataManager.getCityMap();
        int now = TimeHelper.getCurrentSecond();
        // boolean npcAtk = false;// 中立NPC是否攻城
        for (City city : cityMap.values()) {
            try {
                if (!city.isNpcCity() && !city.isProducedFull()) {// 城池产出处理
                    cityProduceHandle(city, now);
                }

                if (city.isInCampagin()) {// 竞选城主处理
                    cityCampaignHandle(city, now);
                }

                if (city.hasOwner()) {// 城主相关操作
                    // cityOwnerHandle(city, now, hour);
                    cityOwnerEndTimeHandle(city, now);// 城主到期处理
                }

                if (city.isFree()) {
                    if (now >= city.getCloseTime()) {// 城池保护结束
                        city.endFree();
                    }
                }
                /*
                // 都城怪物攻城
                if (city.isAtkCity(now)) {
                    // 中立都城不发起战斗
                    if (city.getCamp() == Constant.Camp.NPC) {
                        continue;
                    }
                    // 中立名城 超过数量不发动
                    // if (city.getCamp() == Constant.Camp.NPC
                    // && !worldService.checkAttkableType8NpcCity(city.getCamp())) {
                    // continue;
                    // }
                    // // 中立NPC已攻城
                    // if (npcAtk && city.getCamp() == 0) {
                    // continue;
                    // }
                    int myCityCnt = worldDataManager.getPeoPle4MiddleCity(city.getCamp());
                    LogUtil.debug("怪物攻城，阵营=" + city.getCamp() + ",已拥有城池" + myCityCnt + ",最大拥有配置="
                            + WorldConstant.CITY_TYPE_8_MAX);
                    // 按都城的位置发起进攻
                    if (city.getCityId() == WorldConstant.HOME_CITY_1) {
                        cityAtk(city, WorldConstant.HOME_1_CITY_ROUND);
                    } else if (city.getCityId() == WorldConstant.HOME_CITY_2) {
                        cityAtk(city, WorldConstant.HOME_2_CITY_ROUND);
                    } else if (city.getCityId() == WorldConstant.HOME_CITY_3) {
                        cityAtk(city, WorldConstant.HOME_3_CITY_ROUND);
                    } else if (city.getCityId() == WorldConstant.HOME_CITY_4) {
                        cityAtk(city, WorldConstant.HOME_4_CITY_ROUND);
                    }
                
                    // // 标记中立NPC攻城
                    // if (city.getCamp() == 0) {
                    // npcAtk = true;
                    // }
                } */
            } catch (MwException e) {
                LogUtil.error(e, "城池定时任务执行出错, city:", city);
            }
        }
        // 本地跨服地图使用
        crossCityService.runSecCityTimeLogic();
    }

    /**
     * NPC自动攻城
     * 
     * @param city NPC攻方都城
     * @param list 将要被攻击的城池，根据配置
     */
    @Deprecated
    private void cityAtk(City city, List<List<Integer>> list) {
        // 如果不是本阵营的则发起战斗, 只发动名城
        StaticCity staticCity = null;
        for (List<Integer> cityIds : list) {
            for (int cityId : cityIds) {
                City worldCity = worldDataManager.getCityById(cityId);
                // 同一个阵营的不打
                if (worldCity.getCamp() == city.getCamp()) {
                    LogUtil.debug("怪物攻城，同一个阵营的不打出发city=" + city.getCityId() + ",防守cityId=" + cityId);
                    continue;
                }
                // 中立的不打
                if (worldCity.getCamp() == 0) {
                    LogUtil.debug("怪物攻城，中立的不打阵营=" + city.getCityId() + ",防守cityId=" + cityId);
                    continue;
                }
                staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
                // 只打名城
                if (staticCity.getType() != WorldConstant.CITY_TYPE_8) {
                    LogUtil.debug("怪物攻城，只能攻打名城阵营=" + city.getCityId() + ",防守cityId=" + cityId);
                    continue;
                }
                // 城池保护中
                if (worldCity.getProtectTime() > TimeHelper.getCurrentSecond()) {
                    LogUtil.debug("怪物攻城，城池保护中阵营=" + city.getCityId() + ",防守cityId=" + cityId);
                    continue;
                }
                worldService.processAtk(city, worldCity);
                return;
            }
        }
    }

//    /**
//     * 城主相关处理任务
//     *
//     * @param city
//     * @param now
//     * @param hour
//     */
//    public void cityOwnerHandle(City city, int now, int hour) {
//        cityOwnerEndTimeHandle(city, now);// 城主到期处理
//
//        if (needSendExtraReward(city, hour)) {// 城主额外奖励
//            city.setExtraReward(TimeHelper.getCurrentDay());
//
//            cityExtraRewardHandle(city, now);
//        }
//
//    }

    /**
     * 城主任期到期处理
     * 
     * @param city
     * @param now
     */
    public void cityOwnerEndTimeHandle(City city, int now) {
        if (city.getEndTime() <= now) {
            city.cleanOwner(false);

            // 邮件通知
            mailDataManager.sendNormalMail(playerDataManager.getPlayer(city.getOwnerId()),
                    MailConstant.MOLD_CITY_OWNER_END, now, city.getCityId(), city.getCityId());
        }
    }

    /**
     * 城主额外奖励处理
     * @param city
     * @param now
     */
    public void cityExtraRewardHandle(City city, int now) {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
        int rewardNum = WorldConstant.CITY_EXTRA_REWARD_NUM;

        List<List<Integer>> otherRandomAward;
        List<List<Integer>> rewardList = new ArrayList<>();
        for (int i = 0; i < rewardNum; i++) {
            rewardList.add(staticCity.randomDropReward());
            otherRandomAward = staticCity.randomOtherReward();
            if (CheckNull.nonEmpty(otherRandomAward)) {
                rewardList.addAll(otherRandomAward);
            }
        }

        Player player = playerDataManager.getPlayer(city.getOwnerId());
        if (player != null) {
            List<Award> awards = PbHelper.createAwardsPb(rewardList);
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_CITY_EXTRA_REWARD,
                    AwardFrom.CITY_EXTRA_REWARD, now, city.getCityId(), city.getCityId(), city.getEndTime());
            LogUtil.debug("给城主发奖励邮件  roleId:", city.getOwnerId(), ", cityId:", city.getCityId());
        }
    }

    /**
     * 检查当前是否需要发城主额外奖励
     * 
     * @param city
     * @param hour
     * @return
     */
    public boolean needSendExtraReward(City city, int hour) {
        if (hour >= 21) {// 每天21点
            int today = TimeHelper.getCurrentDay();
            int now = TimeHelper.getCurrentSecond();
            if (city.getExtraReward() != today && city.getEndTime() >= now) {
                return true;
            }
        }
        return false;
    }

    /**
     * 城池竞选处理逻辑
     * 
     * @param city
     * @param now
     * @throws MwException
     */
    public void cityCampaignHandle(City city, int now) throws MwException {
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

                Award ownerAward = PbHelper.createAward(staticCity.getInAward());
                List<Award> ownerAwards = ListUtils.createList(ownerAward);
                mailDataManager.sendAttachMail(owner,ownerAwards,MailConstant.MOLD_CAMPAIGN_SUCC,AwardFrom.CITY_CAMPAIGN_SUC,now,city.getCityId(),"竞选",city.getCityId());

                // 发送竞选成功邮件
//                mailDataManager.sendNormalMail(owner, MailConstant.MOLD_CAMPAIGN_SUCC, now, city.getCityId(),
//                        city.getCityId());

                // 记录军团日志
                Turple<Integer, Integer> xy = staticCity.getCityPosXy();
                PartyLogHelper.addPartyLog(owner.lord.getCamp(), PartyConstant.LOG_CITY_REBUILD, nick, city.getCityId(),
                        xy.getA(), xy.getB());
            }
            int oldCamppaignRoleSize = city.getCampaignList().size();
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
            if (staticCity.getType() == WorldConstant.CITY_TYPE_8 && oldCamppaignRoleSize == 0
                    && worldDataManager.getPeoPle4MiddleCity(city.getCamp()) > 4) {// 名城没人竞选
                city.setCamp(Constant.Camp.NPC);
                city.setProduced(0);
                city.setFinishTime(0);
                city.clearFormList();
            }
            superMineService.changeSuperState(city);
            // 推送刷新地图数据
            List<Integer> posList = new ArrayList<>();
            posList.add(staticCity.getCityPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }
    }

    /**
     * 城池产出处理逻辑
     * 
     * @param city
     * @param now
     */
    public void cityProduceHandle(City city, int now) {
        if (city.getFinishTime() <= 0) {
            city.beginProduce(now);
        } else if (city.getFinishTime() <= now) {
            city.produceFinish();
        }
    }

    /**
     * 都城修改城市名称
     * 
     * @param roleId 角色id
     * @param cityId 城市id
     * @param name 待修改的城市名称
     * @return
     * @throws MwException
     */
    public CityRenameRs cityRename(long roleId, int cityId, String name) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查城池是否存在
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        City city = worldDataManager.getCityById(cityId);
        if (null == staticCity || null == city) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "修改城池名称，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        // 检查是否属于本国城池
        if (city.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "修改城池名称，不是本阵营的城池, roleId:", roleId,
                    ", cityId:", cityId, ", cityCamp:", city.getCamp(), ", playerCamp:", player.lord.getCamp());
        }
        // 都城才能改名
        if (staticCity.getType() != WorldConstant.CITY_TYPE_HOME) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "都城才能改名  roleId:", roleId);
        }
        // 检查玩家是否有权限
        int job = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CITY_RENAME;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "没有这个特权, roleId:", roleId, ", job:", job,
                    ", privilege:", privilege);
        }
        name = name.replaceAll("\\s*", "");
        // 检测待修改城池名称是否合法
        if (nickIsIllegal(name)) {
            throw new MwException(GameError.CITY_NAME_ERROR.getCode(), "城池名称含有非法字符   roleId:", roleId);
        }

        // 与原来名称相同
        if (city.getName() != null && city.getName().equals(name)) {
            throw new MwException(GameError.SAME_NICK.getCode(), "城池名称和原来名称一样 roleId:", roleId);
        }
        // 检测重名城池
        if (StaticWorldDataMgr.isNpcCityName(name) || isSameCity(cityId, name)) {
            throw new MwException(GameError.SAME_NICK.getCode(), "与其他城池有相同名称 roleId:", roleId);
        }

        // 检测角色是满足道具需求
        int propId = PropConstant.PROP_RENAME_CARD;
        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "道具使用,无此配置道具, roleId:" + roleId + ",propId=" + propId);
        }
        int propCount = Constant.CITY_RENAME_COST; // 需要消耗道具数量
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, propCount, AwardFrom.CITY_RENAME);
        city.setName(name); // 修改名称

        // 通知客户端
        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        CityRenameRs.Builder builder = CityRenameRs.newBuilder();
        builder.setName(name);
        return builder.build();
    }

    /**
     * 城池名城是否合法
     * 
     * @param nick
     * @return
     */
    private static boolean nickIsIllegal(String nick) {
        return CheckNull.isNullTrim(nick) || nick.length() >= 12 || EmojiHelper.containsEmoji(nick)
                || EmojiHelper.containsSpecialSymbol(nick);
    }

    /**
     * 是否有相同的名称
     * 
     * @param cityId 城池id
     * @param name  名称
     * @return 是否有同名
     */
    private boolean isSameCity(final int cityId, String name) {
        return StaticWorldDataMgr.getCityByArea(WorldConstant.AREA_TYPE_13).stream().filter(sc -> sc.getCityId() != cityId && sc.getType() == WorldConstant.CITY_TYPE_HOME).map(sc -> {
            String cityName = worldDataManager.getCityById(sc.getCityId()).getName();
            // 如果没有取都城的名称
            if (CheckNull.isNullTrim(cityName)) {
                cityName = StaticIniDataMgr.getTextName("s_city_" + sc.getCityId());
            }
            return cityName;
        }).anyMatch(name::equals);
        // 之前的判断是都城不能同名
        /*        return Stream
                .of(WorldConstant.HOME_CITY_1, WorldConstant.HOME_CITY_2, WorldConstant.HOME_CITY_3,
                        WorldConstant.HOME_CITY_3)
                .filter(id -> cityId != id).map(id -> worldDataManager.getCityById(id).getName())
                .anyMatch(name::equals);*/
    }

    /**
     * 都城开发
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public UpCityRs upCity(Long roleId, UpCityRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cityId = req.getCityId();
        // int type = req.getType();
        City city = worldDataManager.getCityById(cityId);
        if (player.lord.getCamp() != city.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "开发时，不是本阵营的城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (staticCity == null || staticCity.getType() != WorldConstant.CITY_TYPE_HOME) {
            throw new MwException(GameError.CITY_CAN_NOT_UP.getCode(), "不是都城，不允许开发, roleId:", roleId, ", cityId:",
                    cityId);
        }
        // 跨区不允许
        if (staticCity.getArea() != player.lord.getArea()) {
            throw new MwException(GameError.UPCITY_AREA_ERROR.getCode(), "跨区域不允许,开发, roleId:", roleId, ", cityId:",
                    staticCity.getCityId());
        }

        StaticCityDev staticCityDev = StaticWorldDataMgr.getCityDev(city.getCityLv());
        if (staticCityDev == null) {
            throw new MwException(GameError.CITY_CAN_NOT_UP.getCode(), "开发时，找不到配置, roleId:", roleId, ", cityId:",
                    cityId + ",lv=" + city.getCityLv());
        }
        int upCnt = player.getMixtureDataById(PlayerConstant.UP_CAPITALCITY_CNT);
        if (upCnt >= WorldConstant.UP_CAPITALCITY_MAX_CNT) {
            throw new MwException(GameError.UP_CAPITALCITY_MAX_CNT_ERR.getCode(), "都城开发超过上限次数:", roleId, ", cnt:",
                    player.getMixtureDataById(PlayerConstant.UP_CAPITALCITY_CNT));
        }
        // 都城满级判断
        StaticCityDev nextStaticCityDev = StaticWorldDataMgr.getCityDev(city.getCityLv() + 1);
        if (nextStaticCityDev == null) {
            if (city.getExp() >= staticCityDev.getExp()) {
                throw new MwException(GameError.CITY_DEV_LV_MAX.getCode(), "开发时，已满级, roleId:", roleId, ", cityId:",
                        cityId);
            }
        }

        // 判断冷却时间
        // if (city.getNextDevTime() > now && type == 0) {
        // // throw new MwException(GameError.CITY_DEV_CD.getCode(), "开发时，时间未到, roleId:", roleId, ", cityId:",
        // // cityId);
        // UpCityRs.Builder builder = UpCityRs.newBuilder();
        // builder.setCityId(city.getCityId());
        // builder.setCityExp(city.getExp());
        // builder.setCityLv(city.getCityLv());
        // builder.setDevTime(city.getNextDevTime());
        // return builder.build();
        // }

        // 花钱秒CD
        // int needGold = 0;
        // if (city.getNextDevTime() > now && type > 0) {
        // needGold = (int) Math.ceil((city.getNextDevTime() - now) * 1.00 / 60) * 5;
        // // if (needGold > 0 && needGold > player.lord.getGold()) {
        // // throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(), "开发时，金币不够, roleId:", roleId, ", cityId:",
        // // cityId);
        // // }
        // rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
        // AwardFrom.CAPITAL_SPEED_GOLD);// 都城开发
        // city.setNextDevTime(now - 1);
        // UpCityRs.Builder builder = UpCityRs.newBuilder();
        // builder.setCityId(city.getCityId());
        // builder.setCityExp(city.getExp());
        // builder.setCityLv(city.getCityLv());
        // builder.setDevTime(city.getNextDevTime());
        // return builder.build();
        // }
        StaticCityAward sCityAward = StaticWorldDataMgr.getCityAwardMap().get(upCnt + 1);
        if (sCityAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), ", roleId:", roleId, ",curUpCnt:", upCnt);
        }
        int freeCnt = player.getMixtureDataById(PlayerConstant.FREE_UP_CAPITALCITY_CNT);
        if (freeCnt < 1) {// 有免费次数先用掉免费次数
            player.setMixtureData(PlayerConstant.FREE_UP_CAPITALCITY_CNT, freeCnt + 1);
        } else {
            rewardDataManager.checkAndSubPlayerRes(player, sCityAward.getConsume(), AwardFrom.HOME_CITY_DEV); // 扣材料
        }
        player.setMixtureData(PlayerConstant.UP_CAPITALCITY_CNT, upCnt + 1);// 加上限次数

        battlePassDataManager.updTaskSchedule(roleId, TaskType.COND_UP_CITY_CNT, 1);
        Award awardExploit = rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.EXPLOIT,
                sCityAward.getExploit(), AwardFrom.HOME_CITY_DEV);
        // if (needGold > 0) {
        // rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
        // AwardFrom.FACTORYRECRUIT_SPEED_GOLD, "都城开发加速");
        // }
        // int now = TimeHelper.getCurrentSecond();
        // city.setNextDevTime(now + WorldConstant.HOME_DEV_CD * TimeHelper.MINUTE);
        // 升级都城
        upCaptainCityLv(city, sCityAward.getExp());
        UpCityRs.Builder builder = UpCityRs.newBuilder();
        builder.setCityId(city.getCityId());
        builder.setBuildingExp(city.getBuildingExp());
        builder.setCityLv(city.getCityLv());
        // builder.setDevTime(city.getNextDevTime());
        builder.setFreeCnt(player.getMixtureDataById(PlayerConstant.FREE_UP_CAPITALCITY_CNT));
        builder.setUpCityCnt(player.getMixtureDataById(PlayerConstant.UP_CAPITALCITY_CNT));
        builder.addAward(awardExploit);
        return builder.build();
    }

    public void upCaptainCityLv(City city, int addExp) {
        if (city == null || !city.isCaptainCity() || addExp < 1 || city.getCamp() <= 0) return;
        StaticCityDev curCityDev = StaticWorldDataMgr.getCityDev(city.getCityLv());
        boolean isUp = false;
        int exp = addExp;
        if (curCityDev != null) {
            while (exp > 0) {
                int lvExp = curCityDev.getExp();
                if (city.getBuildingExp() + exp >= lvExp) {// 可能升级
                    StaticCityDev nextCityDev = StaticWorldDataMgr.getCityDev(curCityDev.getLv() + 1);
                    if (nextCityDev == null) {
                        exp = 0;
                        break;// 最大等级了
                    }
                    int incrExp = curCityDev.getExp() - city.getBuildingExp();
                    if (incrExp < 0) {// 防止策划把经验值改小
                        incrExp = exp;
                    }
                    exp -= incrExp;
                    city.setCityLv(city.getCityLv() + 1);
                    city.setBuildingExp(0);
                    isUp = true;
                    curCityDev = StaticWorldDataMgr.getCityDev(city.getCityLv());
                } else {
                    city.setBuildingExp(city.getBuildingExp() + exp);
                    exp -= exp;
                }
            }
        } else {
            LogUtil.error("都城升级时,找不到对应等级配置 city:", city.getCityId(), ", lv:", city.getCityLv());
        }
        if (isUp) {
            worldDataManager.addCampSuperMine(city.getCamp()); // 触发刷超级矿点
            LogUtil.common("-----------触发刷新超级矿点 camp:", city.getCamp());
        }

    }

    /**
     * 都城加人口
     * @param city
     * @param population
     */
    public void addHomeCityPopulation(City city, int population) {
        if (city == null || population < 1) return;
        int totalExp = city.getExp() + population;
        city.setExp(totalExp);
    }

    /**
     * 阵营city推送
     * 
     * @param city
     */
    public void syncPartyCity(City city, StaticCity staticCity) {
        if (city == null || staticCity == null) return;
        ConcurrentHashMap<Long, Player> playerByAreaMap = playerDataManager.getPlayerByArea(staticCity.getArea());
        if (playerByAreaMap == null) return;
        SyncPartyCityRs.Builder builder = SyncPartyCityRs.newBuilder();
        builder.setPartyCity(PbHelper.createPartyCityPb(city, playerDataManager));
        SyncPartyCityRs spcPb = builder.build();
        for (Player player : playerByAreaMap.values()) {
            if (player.isLogin && player.ctx != null) {
                Base.Builder msg = PbHelper.createSynBase(SyncPartyCityRs.EXT_FIELD_NUMBER, SyncPartyCityRs.ext, spcPb);
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * 检测该城池是否是跨服的城池
     * 
     * @param cityId
     * @return true 是跨服的城池
     * @throws MwException
     */
    private boolean checkIsCrossCity(int cityId) throws MwException {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "未找到城池配置,  cityId:", cityId);
        }
        return staticCity.getArea() > WorldConstant.AREA_MAX_ID;
    }

    @Override
    public DelayQueue getDelayQueue() {
        return DELAY_QUEUE;
    }

    @Override
    public void handleOnStartup() throws Exception {
        worldDataManager.getCityMap().values().forEach(city -> {
            if(city.getLeaveOver() > 0){
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
                if(city.getOwnerId() != 0 && city.getLeaveOver() > 0){
                    DELAY_QUEUE.add(new CityDelayRun(city,staticCity,playerDataManager.getPlayer(city.getOwnerId())));
                }
            }
        });
    }

    @GmCmd("city")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "clearState":
                int cityId = Integer.parseInt(params[1]);
                City city = worldDataManager.getCityById(cityId);
                if(Objects.nonNull(city)){
                    city.setStatus(WorldConstant.CITY_STATUS_CALM);
                }
                break;
            default:
        }
    }
}
