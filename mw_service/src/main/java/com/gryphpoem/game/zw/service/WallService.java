package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.pb.GamePb4.FixWallRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticWallHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticWallHeroLv;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * 城墙
 *
 * @author tyler
 */
@Service
public class WallService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private HeroService heroService;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private MedalDataManager medalDataManager;

    /**
     * 城墙信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetWallRs getWall(Long roleId, GetWallRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GetWallRs.Builder builder = GetWallRs.newBuilder();
        int pos = 0;
        Player targetPlayer = null;
        if (!req.hasTargetId()) { // 获取自己的城墙
            // 更新自动补兵
            try {
                processAutoAddArmy(player);
            } catch (MwException e) {
                LogUtil.error("roleId:", roleId, " 自动补兵错误:", e.toString());
            }
            for (Entry<Integer, WallNpc> ks : player.wallNpc.entrySet()) {
                builder.addWallNpc(PbHelper.createWallNpcPb(ks.getValue()));
            }
            pos = player.lord.getPos();
        } else { // 获取他人城墙
            long targetId = req.getTargetId();
            targetPlayer = playerDataManager.checkPlayerIsExist(targetId);
            builder.setWallLv(targetPlayer.building.getWall());
            pos = targetPlayer.lord.getPos();
        }
        List<Army> list = worldDataManager.getPlayerGuard(pos);
        List<Army> rmArmy = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            Player tarPlayer = null;
            Iterator<Army> it = list.iterator();
            while (it.hasNext()) {
                Army army = it.next();
                tarPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tarPlayer == null) {
                    // it.remove();
                    rmArmy.add(army);
                    continue;
                }
                if (!tarPlayer.armys.containsKey(army.getKeyId())) {
                    LogUtil.debug("檢測移除驻防信息army=" + army);
                    // it.remove();
                    rmArmy.add(army);
                    continue;
                }
                int armyCnt = army.getHero().get(0).getCount();
                Hero hero = tarPlayer.heros.get(army.getHero().get(0).getPrincipleHeroId());
                if (hero != null) {
                    armyCnt = hero.getCount();
                }
                builder.addWallHero(PbHelper.createWallHeroPb(army, tarPlayer.lord.getLevel(), tarPlayer.lord.getNick(),
                        army.getEndTime(), armyCnt));
            }
        } else {
            worldDataManager.removePlayerGuard(pos);
        }

        for (Army army : rmArmy) {
            list.remove(army);
        }

        if (!CheckNull.isNull(targetPlayer)) {
            List<Integer> talentList = DataResource.getBean(SeasonTalentService.class).
                    getSeasonTalentId(targetPlayer, SeasonConst.TALENT_EFFECT_610);
            if (Objects.nonNull(talentList)) {
                builder.addAllSeasonTalentId(talentList);
            }
        }
        return builder.build();
    }

    /**
     * 自动补兵
     *
     * @param player
     * @throws MwException
     */
    public void processAutoAddArmy(Player player) throws MwException {

        int now = TimeHelper.getCurrentSecond();
        Resource resource = player.resource;
        ChangeInfo change = ChangeInfo.newIns();

        // NPC补兵
        if (!player.isFireState()) {// 失火状态不让补兵
            // 通过id重新排个序
            autoNpcAddArmy(player, now, resource, change);
        }
        /*-------------------------------------城防兵自动补兵----------------------------------*/
        autoWallHeroAddArmy(player, now, resource, change);

        rewardDataManager.syncRoleResChanged(player, change);

    }

    /**
     * 城防将领的自动补兵
     *
     * @param player
     * @param now
     * @param resource
     * @param change
     * @throws MwException
     */
    private void autoWallHeroAddArmy(Player player, int now, Resource resource, ChangeInfo change) throws MwException {
        int allSecond = 0;// 距离上次补兵的间隔时间
        int armyCount = 0; // 先计算距离现在最多可以补兵多少
        boolean flag = true;
        for (int i = 1; i < player.getPlayerFormation().getHeroWall().length; i++) {
            PartnerHero partnerHero = player.getPlayerFormation().getHeroWall()[i];
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            Hero hero = partnerHero.getPrincipalHero();
            if (hero == null) {
                continue;
            }
            int maxArmy = hero.getAttr()[FightCommonConstant.AttrId.LEAD];
            if (hero.getCount() >= maxArmy) {
                continue;
            }
            if (flag) {// 第一个补兵的将领作为补兵的开始时间
                allSecond = now - hero.getWallArmyTime();
                double armyPerSec = (1.0 * maxArmy) / HeroConstant.WALL_HERO_AUTO_ARMY_COEFFICIENT;
                armyCount = (int) (allSecond * armyPerSec);
                flag = !flag;
            }

            int add = armyCount;
            if (add < 1) {// 没可补的兵就别补了
                break;
            }
            if (hero.getCount() + add > maxArmy) {
                add = maxArmy - hero.getCount();
            }
            // 耗粮判断
            // 科技消耗系数
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            int armyType = staticHero.getType();
            int radio = techDataManager.getFood4ArmyType(player, armyType);
            if (radio == 0) {
                radio = Constant.FACTORY_ARM_NEED_FOOD;
            }
            int needFood = add * radio;
            if (needFood > 0) {
                if (resource.getFood() >= needFood) {
                    rewardDataManager.subResource(player, AwardType.Resource.FOOD, needFood,
                            AwardFrom.WALL_NPC_AUTO_ARMY);// , "城墙自动补兵"
                    hero.setCount(hero.getCount() + add);
                    change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);

                    //记录玩家兵力变化信息
                    // LogLordHelper.filterHeroArm(AwardFrom.WALL_NPC_AUTO_ARMY, player.account, player.lord, hero.getHeroId(), hero.getCount(), add,
                    //         Constant.ACTION_ADD, armyType, hero.getQuality());

                    // 上报玩家兵力变化信息
//                    LogLordHelper.playerArm(
//                            AwardFrom.WALL_NPC_AUTO_ARMY,
//                            player,
//                            armyType,
//                            Constant.ACTION_ADD,
//                            add,
//                            playerDataManager.getArmCount(player.resource, armyType)
//                    );
                } else {
                    // 粮不够,能补多少扣多少
                    add = (int) (resource.getFood() / radio);
                    needFood = add * radio;
                    if (needFood > 0) {
                        rewardDataManager.subResource(player, AwardType.Resource.FOOD, needFood,
                                AwardFrom.WALL_NPC_AUTO_ARMY);// , "城墙自动补兵"
                        hero.setCount(hero.getCount() + add);
                        change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
                    }
                }
                hero.setWallArmyTime(now);
                armyCount -= add; // 计算剩余的可以补的兵力
                // 如果本将领补满了修改下一个将领的补兵时间
                if (hero.getCount() >= maxArmy) {// 每次补满兵都会影响都城墙上所有的时间
                    for (int j = i + 1; j < player.getPlayerFormation().getHeroWall().length; j++) {
                        PartnerHero partnerHeroJ = player.getPlayerFormation().getHeroWall()[j];
                        if (HeroUtil.isEmptyPartner(partnerHeroJ)) continue;
                        Hero heroNest = partnerHeroJ.getPrincipalHero();
                        if (heroNest != null) {
                            heroNest.setWallArmyTime(now);
                        }
                    }
                }
            }
        }
    }

    /**
     * npc防守将领的自动补兵
     *
     * @param player
     * @param now
     * @param resource
     * @param change
     * @throws MwException
     */
    private void autoNpcAddArmy(Player player, int now, Resource resource, ChangeInfo change) throws MwException {
        List<WallNpc> npcList = player.wallNpc.values().stream()
                .sorted((npc1, npc2) -> (npc1.getId() < npc2.getId()) ? -1 : ((npc1.getId() == npc2.getId()) ? 0 : 1))
                .collect(Collectors.toList());
        int allSecond = 0;// 距离上次补兵的间隔时间
        int armyCount = 0; // 先计算距离现在最多可以补兵多少
        boolean flag = true;// 第一次的标记
        for (int i = 0; i < npcList.size(); i++) {
            WallNpc wallNpc = npcList.get(i);
            if (wallNpc.getAutoArmy() > 0) {
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr
                        .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
                if (staticWallHeroLv == null) {
                    continue;
                }
                int maxArmy = staticWallHeroLv.getAttr().get(FightCommonConstant.AttrId.LEAD);
                if (wallNpc.getCount() >= maxArmy) { // 已经布满了的过滤
                    wallNpc.setAutoArmy(0);
                    continue;
                }
                if (flag) {// 第一个补兵的将领作为补兵的开始时间
                    allSecond = now - wallNpc.getAddTime();
                    double armyPerSec = (1.0 * maxArmy) / HeroConstant.WALL_HERO_AUTO_ARMY_COEFFICIENT;
                    armyCount = (int) (allSecond * armyPerSec);
                    flag = !flag;
                }
                int add = armyCount;
                if (add < 1) {// 没可补的兵就别补了
                    break;
                }
                if (wallNpc.getCount() + add > maxArmy) {
                    add = maxArmy - wallNpc.getCount();
                }
                if (add > 0) {
                    if (wallNpc.getCount() + add > maxArmy) {
                        add = maxArmy - wallNpc.getCount();
                    }
                    // 耗粮判断
                    // 科技消耗系数
                    int radio = techDataManager.getFood4ArmyType(player, staticWallHeroLv.getType());
                    if (radio == 0) {
                        radio = Constant.FACTORY_ARM_NEED_FOOD;
                    }
                    int needFood = add * radio;
                    if (needFood > 0) {
                        if (resource.getFood() < needFood) {
                            // 粮不够,能补多少扣多少
                            add = (int) (resource.getFood() / radio);
                            needFood = add * radio;
                        }
                        if (needFood > 0) {
                            rewardDataManager.subResource(player, AwardType.Resource.FOOD, needFood,
                                    AwardFrom.WALL_NPC_AUTO_ARMY);// , "NPC自动补兵"
                            wallNpc.setCount(wallNpc.getCount() + add);
                            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
                        }
                        wallNpc.setAddTime(now);
                    }
                    armyCount -= add; // 计算剩余的可以补的兵力
                    if (wallNpc.getCount() >= maxArmy) {
                        wallNpc.setAutoArmy(0);
                        for (int j = i + 1; j < npcList.size(); j++) {
                            WallNpc npc = npcList.get(j);
                            if (npc != null) {
                                npc.setAddTime(now);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 重新调整位置
     *
     * @param heroIds
     * @param heros
     */
    public void reAdjustHeroPos(PartnerHero[] heroIds, Map<Integer, Hero> heros) {
//        // 重新调整位置
//        List<Integer> heroList = new ArrayList<>();
//        for (int i = 1; i < heroIds.length; i++) {
//            int hid = heroIds[i];
//            if (heros.get(hid) != null) {
//                heroList.add(hid);
//            }
//        }
//        for (int i = 0; i < heroIds.length - 1; i++) {
//            int pos = i + 1;
//            if (i < heroList.size()) {
//                int hId = heroList.get(i);
//                heroIds[pos] = hId;
//                heros.get(hId).onWall(pos);
//            } else {
//                // 尾部 清空
//                heroIds[pos] = 0;
//            }
//        }
    }

    /**
     * 下将领并返还资源
     *
     * @param player
     * @param downHero 需要下的将领
     */
    public void downWallHeroAndBackRes(Player player, Hero downHero) {
        downHero.onWall(0);// 将领下阵，pos设置为0
        // 返还资源
        int sub = downHero.getCount();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(downHero.getHeroId());
        int armyType = staticHero.getType();
        int radio = techDataManager.getFood4ArmyType(player, armyType);
        if (radio == 0) {
            radio = Constant.FACTORY_ARM_NEED_FOOD;
        }
        downHero.setCount(0);
        rewardDataManager
                .addAward(player, AwardType.RESOURCE, AwardType.Resource.FOOD, sub * radio, AwardFrom.HERO_DOWN);

        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
        rewardDataManager.syncRoleResChanged(player, change);

    }

    /**
     * 城墙招募NPC
     *
     * @param roleId
     * @return
     */
    public WallNpcRs doWallNpc(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticWallHero staticWallHero = StaticBuildingDataMgr.getWallHero(id);
        if (staticWallHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "城墙招募NPC,找不到配置 roleId:", roleId, ", id:", id);
        }
        if (staticWallHero.getNeedWallLv() > buildingDataManager.getBuildingTopLv(player, BuildingType.WALL)) {
            throw new MwException(GameError.WALL_LV_NOT_NPC.getCode(), "城墙招募失败,请提升城墙等级 roleId:", roleId, ", id:", id);
        }
        if (player.wallNpc.containsKey(id)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", id:",
                    id);
        }
        WallNpc wallNpc = new WallNpc();
        wallNpc.setHeroNpcId(RandomUtil.getKeyByMap(staticWallHero.getGainHero(), 0));
        wallNpc.setId(id);
        wallNpc.setAddTime(TimeHelper.getCurrentSecond());
        StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
        wallNpc.setCount(staticWallHeroLv.getAttr().get(FightCommonConstant.AttrId.LEAD));
        player.wallNpc.put(id, wallNpc);
        // 重置城防将领可招募推送消息的状态
        // player.removePushRecord(PushConstant.WALL_RECRUIT);
        WallNpcRs.Builder builder = WallNpcRs.newBuilder();
        for (Entry<Integer, WallNpc> ks : player.wallNpc.entrySet()) {
            builder.addWallNpc(PbHelper.createWallNpcPb(ks.getValue()));
        }
        return builder.build();
    }

    /**
     * 城墙NPC 升级
     *
     * @param roleId
     * @param pos
     * @param type   0普通升级 1金币升级
     * @return
     * @throws MwException
     */
    public WallNpcLvUpRs doWallNpcLvUp(Long roleId, int pos, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!player.wallNpc.containsKey(pos)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", id:",
                    pos);
        }
        WallNpc wallNpc = player.wallNpc.get(pos);

        StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel() + 1);
        if (staticSuperEquipLv == null) {
            throw new MwException(GameError.WALLNPC_LV_FULL.getCode(), "等级已达上限, roleId:", roleId, ", type:", pos);
        }
        int wallLv = BuildingDataManager.getBuildingTopLv(player, BuildingType.WALL);
        staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());

        if (staticSuperEquipLv.getNeedWallLv() > wallLv) {
            throw new MwException(GameError.WALLNPC_NEED_WALL_LV.getCode(), "NPC升级城墙等级不够，请提升城墙等级 roleId:", roleId,
                    ", type:", pos);
        }

        int addExp = staticSuperEquipLv.getFoodAddExp();
        if (type > 0) {
            addExp = staticSuperEquipLv.getGoldAddExp();
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                    staticSuperEquipLv.getNeedGold(), AwardFrom.WALL_NPC_LV);
        } else {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                    staticSuperEquipLv.getNeedFood(), AwardFrom.WALL_NPC_LV);
        }
        if (Constant.SUPER_EQUIP_MAX_STEP > wallNpc.getExp() + addExp) {
            wallNpc.setExp(Math.min(Constant.SUPER_EQUIP_MAX_STEP, wallNpc.getExp() + addExp));
        } else {
            staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel() + 1);
            if (staticSuperEquipLv != null) {
                wallNpc.setExp(0);
                wallNpc.setLevel(wallNpc.getLevel() + 1);
            } else {
                throw new MwException(GameError.SUPER_EQUIP_LV_FULL.getCode(), "等级已达上限, roleId:", roleId, ", type:",
                        type);
            }
        }
        WallNpcLvUpRs.Builder builder = WallNpcLvUpRs.newBuilder();
        builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        builder.setResource(PbHelper.createCombatPb(player.resource));
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 城墙驻防他人
     *
     * @param roleId
     * @param pos
     * @param heroIdList
     * @return
     * @throws MwException
     */
    public WallHelpRs doWallHelp(Long roleId, int pos, List<Integer> heroIdList) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (worldDataManager.isEmptyPos(pos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "该坐标为空闲坐标，不能驻防, roleId:", roleId, ", pos:", pos);
        }
        Player target = worldDataManager.getPosData(pos);
        //如果玩家处于决战状态,将不能进行驻防
        if (player.getDecisiveInfo().isDecisive() || target.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_NO_ATK.getCode(), "决战中，不能驻防, roleId:", roleId,
                    ", pos:", pos);
        }
        // 检查出征将领信息
        worldService.checkFormHero(player, heroIdList);

        if (!worldDataManager.isPlayerPos(pos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "该坐标为空闲坐标，不能驻防, roleId:", roleId, ", pos:", pos);
        }

        if (target.lord.getArea() != player.lord.getArea()) {
            throw new MwException(GameError.WALL_HELP_AREA_ERROR.getCode(), "跨区域不允许驻防, roleId:", roleId, ", area:",
                    target.lord.getArea());
        }
        // 驻防等级判断
        int wallLv = BuildingDataManager.getBuildingLv(BuildingType.WALL, target);
        if (wallLv < Constant.GARRISON_WALL_REQUIRE_LV) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "驻防等级不满足, roleId:", roleId, ", wallLv:", wallLv);
        }
        // 对方满了
        StaticBuildingLv buildingLv = StaticBuildingDataMgr
                .getStaticBuildingLevel(BuildingType.WALL, target.building.getWall());
        int max = buildingLv != null && !CheckNull.isEmpty(buildingLv.getCapacity()) ?
                buildingLv.getCapacity().get(0).get(0) :
                0;
        List<Army> guardArmys = worldDataManager.getPlayerGuard(pos);
        if (guardArmys != null && guardArmys.size() + heroIdList.size() > max) { // Constant.WALL_HELP_MAX_NUM
            throw new MwException(GameError.WALL_HELP_FULL.getCode(), "该坐标驻防满了，不能驻防, roleId:", roleId, ", pos:", pos,
                    ",max:", max);
        }

        // 同阵营
        if (target.lord.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "不是同已阵营，不能驻防, roleId:", roleId, ", pos:",
                    pos);
        }

        player.getMarchType().put(pos, ArmyConstant.GUARD_MARCH);
        int marchTime = worldService.marchTime(player, pos);
        int needFood = marchTime * WorldConstant.MOVE_COST_FOOD;
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        int now = TimeHelper.getCurrentSecond();
        Hero hero;
        WallHelpRs.Builder builder = WallHelpRs.newBuilder();
        for (Integer heroId : heroIdList) {
            List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
            PartnerHero partnerHero = player.getPlayerFormation().getPartnerHero(heroId);
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            partnerHero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(partnerHero.convertTo());

            Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_GUARD, pos, ArmyConstant.ARMY_STATE_MARCH,
                    form, marchTime, now + marchTime, player.getDressUp());
            army.setLordId(roleId);
            army.setTarLordId(target.roleId);
            army.setOriginPos(player.lord.getPos());
            Optional.ofNullable(medalDataManager.getHeroMedalByHeroIdAndIndex(player, partnerHero.getPrincipalHero().getHeroId(), MedalConst.HERO_MEDAL_INDEX_0))
                    .ifPresent(medal -> {
                        army.setHeroMedals(Collections.singletonList(PbHelper.createMedalPb(medal)));
                    });
            //天赋优化， 驻军属性加成,从被驻防人身上取天赋
            army.setSeasonTalentAttr(DataResource.getBean(SeasonTalentService.class).
                    getSeasonTalentEffectTwoInt(worldDataManager.getPosData(pos), partnerHero.getPrincipalHero(), SeasonConst.TALENT_EFFECT_612));

            player.armys.put(army.getKeyId(), army);

            // 添加行军路线
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            builder.addArmy(PbHelper.createArmyPb(army, false));
        }
        // 通知刷新线
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
        return builder.build();
    }

    /**
     * 城墙驻防召回
     *
     * @param roleId
     * @param keyId
     * @return
     * @throws MwException
     */
    public WallCallBackRs doWallCallBack(Long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Army army = player.armys.get(keyId);
        if (army == null) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "城墙驻防召回,部队不存在, roleId:", roleId, ", keyId:", keyId);
        }
        int now = TimeHelper.getCurrentSecond();
        worldService.retreatArmyByDistance(player, army, now);
        worldService.synWallCallBackRs(0, army);
        worldDataManager.removePlayerGuard(army.getTarget(), army);
        WallCallBackRs.Builder builder = WallCallBackRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * 城墙驻防遣返
     *
     * @param roleId
     * @param keyId
     * @param tarRoleId
     * @return
     * @throws MwException
     */
    public WallGetOutRs doWallGetOut(Long roleId, int keyId, Long tarRoleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Player tarPlayer = playerDataManager.checkPlayerIsExist(tarRoleId);
        Army army = tarPlayer.armys.get(keyId);
        if (army == null) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "城墙驻防召回,部队不存在, roleId:", roleId, ", keyId:", keyId);
        }
        int now = TimeHelper.getCurrentSecond();
        worldService.retreatArmyByDistance(tarPlayer, army, now);
        worldService.synRetreatArmy(tarPlayer, army, now);
        worldDataManager.removePlayerGuard(army.getTarget(), army);
        // 给派兵驻防的玩家发遣返邮件
        int heroId = army.getHero().get(0).getPrincipleHeroId();
        mailDataManager
                .sendNormalMail(tarPlayer, MailConstant.MOLD_GARRISON_REPATRIATE, now, player.lord.getNick(), heroId,
                        player.lord.getNick(), heroId);
        WallGetOutRs.Builder builder = WallGetOutRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * 撤回驻防部队
     *
     * @param pos
     * @param isAll true表示全部撤回, false表示没兵撤回
     */
    public void retreatArmy(int pos, boolean isAll, Player targetP) {
        List<Army> armys = worldDataManager.getPlayerGuard(pos);
        if (armys == null) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        LogUtil.debug("被打后，驻防部队撤回=" + armys + ",是否全部撤退=" + isAll);
        Player player;
        Hero hero;
        String targetNick = targetP.lord.getNick();
        for (Army army : armys) {

            player = playerDataManager.getPlayer(army.getLordId());
            if (player == null) {
                LogUtil.debug("retreatArmy,player is null," + army.getLordId());
                continue;
            }
            if (isAll) {
                worldService.retreatArmyByDistance(player, army, now);
                worldService.synWallCallBackRs(0, army);
                worldService.synRetreatArmy(player, army, now);
                // 驻防被杀发邮件
                int heroId = army.getHero().get(0).getPrincipleHeroId();
                mailDataManager
                        .sendNormalMail(player, MailConstant.WALL_HELP_KILLED, now, targetNick, heroId, targetNick,
                                heroId);
                armys.remove(army); // 此处因为使用的armys CopyOnWriteArrayList,读写分离 所以可以边遍历边删除
            } else {
                for (CommonPb.PartnerHeroIdPb twoInt : army.getHero()) {
                    hero = player.heros.get(twoInt.getPrincipleHeroId());
                    if (hero != null && hero.getCount() <= 0) {
                        worldService.retreatArmyByDistance(player, army, now);
                        worldService.synWallCallBackRs(0, army);
                        worldService.synRetreatArmy(player, army, now);
                        LogUtil.debug("被打后，驻防部队撤回,阵亡=" + army + ",hero=" + hero);
                        // 驻防被杀发邮件
                        int heroId = hero.getHeroId();
                        mailDataManager.sendNormalMail(player, MailConstant.WALL_HELP_KILLED, now, targetNick, heroId,
                                targetNick, heroId);
                        armys.remove(army);
                    }
                }
            }
        }
        if (isAll) {
            worldDataManager.removePlayerGuard(pos);
        }
    }

    /**
     * 城墙npc变换兵种
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public WallNpcArmyRs doWallNpcArmy(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!player.wallNpc.containsKey(id)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "无此城防NPC, roleId:", roleId, ", id:", id);
        }
        StaticWallHero staticWallHero = StaticBuildingDataMgr.getWallHero(id);

        WallNpc wallNpc = player.wallNpc.get(id);

        StaticWallHeroLv beforeNpc = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
        if (beforeNpc != null && beforeNpc.getChangeArmy() == 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "不能变换兵种, roleId:", roleId, ", type:", id);
        }
        // 扣钱
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                Constant.WALL_CHANGE_ARMY_GOLD, AwardFrom.WALL_CHANGE_ARMY);
        double coef = (wallNpc.getCount() * 1.0) / beforeNpc.getAttr().getOrDefault(FightCommonConstant.AttrId.LEAD, 0);

        wallNpc.setHeroNpcId(RandomUtil.getKeyByMap(staticWallHero.getGainHero(), wallNpc.getHeroNpcId()));
        StaticWallHeroLv afterNpc = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());

        int count = (int) Math.ceil(coef * afterNpc.getAttr().get(FightCommonConstant.AttrId.LEAD));
        LogUtil.debug("城墙npc变换兵种: 变换之前, heroId: ", beforeNpc.getHeroId(), ", count:", wallNpc.getCount(), ", 变换之后, heroId:", afterNpc.getHeroId(), ", count:", count);
        wallNpc.setCount(count);

        WallNpcArmyRs.Builder builder = WallNpcArmyRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        return builder.build();
    }

    /**
     * 城墙NPC开启自动补兵
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public WallNpcAutoRs doWallNpcAuto(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!player.wallNpc.containsKey(id)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", id:",
                    id);
        }
        if (player.isFireState()) {
            throw new MwException(GameError.WALL_IS_FIRE_NOT_AUTO_ARMY.getCode(), "城墙失火不让自动补兵, roleId:", roleId);
        }
        WallNpc wallNpc = player.wallNpc.get(id);
        wallNpc.setAutoArmy(1);
        WallNpcAutoRs.Builder builder = WallNpcAutoRs.newBuilder();
        builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        return builder.build();
    }

    /**
     * 城墙花钱满兵
     *
     * @param roleId
     * @param type
     * @param id
     * @return
     * @throws MwException
     */
    public WallNpcFullRs doWallNpcFull(Long roleId, int type, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        WallNpcFullRs.Builder builder = WallNpcFullRs.newBuilder();
        // 城墙NPC
        if (type == 2) {
            if (!player.wallNpc.containsKey(id)) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId,
                        ", id:", id);
            }
            WallNpc wallNpc = player.wallNpc.get(id);
            StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr
                    .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
            double d = (wallNpc.getCount() * 1.0) / staticWallHeroLv.getAttr().get(FightCommonConstant.AttrId.LEAD);
            int needGold = (int) Math.ceil((1 - d) * Constant.WALL_FULL_ARMY_NEED_GOLD);
            if (needGold <= 1) {
                needGold = 1;
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.WALL_FULL_ARMY);

            wallNpc.setCount(staticWallHeroLv.getAttr().get(FightCommonConstant.AttrId.LEAD));
            wallNpc.setAutoArmy(0);
            builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        } else {
            if (!player.isOnWallHero(id)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "将领未上阵, roleId:", roleId, ", heroId:", id);
            }
            Hero hero = player.heros.get(id);
            if (Objects.isNull(hero)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.PARAM_ERROR.errMsg(roleId, "英雄不存在", id));
            }
            if (hero.getCount() >= hero.getAttr()[FightCommonConstant.AttrId.LEAD]) {
                throw new MwException(GameError.HERO_LEAD_IS_MAX.getCode(), "城墙花钱满兵,将领兵力已达到满值");
            }
            double d = (hero.getCount() * 1.0) / hero.getAttr()[FightCommonConstant.AttrId.LEAD];
            int needGold = (int) Math.ceil((1 - d) * Constant.WALL_FULL_ARMY_NEED_GOLD);
            if (needGold <= 1) {
                needGold = 1;
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.WALL_FULL_ARMY);
            hero.setCount(hero.getAttr()[FightCommonConstant.AttrId.LEAD]);
            builder.setHero(PbHelper.createHeroPb(hero, player));

            // 设置所有城防将领时间
            int now = TimeHelper.getCurrentSecond();
            for (int j = 1; j < player.getPlayerFormation().getHeroWall().length; j++) {
                PartnerHero partnerHero = player.getPlayerFormation().getHeroWall()[j];
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                Hero heroNest = partnerHero.getPrincipalHero();
                if (heroNest != null) {
                    heroNest.setWallArmyTime(now);
                }
            }
        }
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 城墙驻防信息
     *
     * @param roleId
     * @param pos
     * @return
     * @throws MwException
     */
    public WallHelpInfoRs doWallHelpInfo(Long roleId, int pos) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        WallHelpInfoRs.Builder builder = WallHelpInfoRs.newBuilder();
        List<Army> list = worldDataManager.getPlayerGuard(pos);
        if (list != null && !list.isEmpty()) {
            Player tarPlayer = null;
            for (Army army : list) {
                tarPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tarPlayer == null) {
                    continue;
                }
                // 重新获取兵力信息
                int armyCnt = army.getHero().get(0).getCount();
                Hero hero = tarPlayer.heros.get(army.getHero().get(0).getPrincipleHeroId());
                if (hero != null) {
                    armyCnt = hero.getCount();
                }
                builder.addWallHero(PbHelper.createWallHeroPb(army, tarPlayer.lord.getLevel(), tarPlayer.lord.getNick(),
                        army.getEndTime(), armyCnt));
            }
        }
        return builder.build();
    }

    /**
     * 修复城墙
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public FixWallRs fixWall(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.isFireState()) {
            int wallLv = BuildingDataManager.getBuildingLv(BuildingType.WALL, player);
            StaticBuildingLv sbl = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.WALL, wallLv);
            List<List<Integer>> cost = sbl.getFixCost();
            if (!CheckNull.isEmpty(cost)) {
                rewardDataManager.checkAndSubPlayerRes(player, cost, AwardFrom.FIX_WALL_COST);
            }
            player.setFireState(false);
        }
        FixWallRs.Builder builder = FixWallRs.newBuilder();
        builder.setIsFireState(player.isFireState());
        return builder.build();
    }

    /**
     * 需要城墙等级是否满足
     *
     * @param level
     * @return
     */
    public boolean wallLevelInNeed(int level) {
        ArrayList<StaticWallHero> wallHeros = new ArrayList<>(StaticBuildingDataMgr.getWallHeroMap().values());
        for (StaticWallHero wallHero : wallHeros) {
            if (wallHero.getNeedWallLv() == level)
                return true;
        }
        return false;
    }

}
