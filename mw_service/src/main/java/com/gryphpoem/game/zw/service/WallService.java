package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1.GetWallRq;
import com.gryphpoem.game.zw.pb.GamePb1.GetWallRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallCallBackRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallGetOutRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallHelpInfoRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallHelpRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcArmyRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcAutoRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcFullRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcLvUpRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallNpcRs;
import com.gryphpoem.game.zw.pb.GamePb1.WallSetRs;
import com.gryphpoem.game.zw.pb.GamePb4.FixWallRs;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.Constant.AttrId;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
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
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ??????
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
     * ????????????
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
        if (!req.hasTargetId()) { // ?????????????????????
            // ??????????????????
            try {
                processAutoAddArmy(player);
            } catch (MwException e) {
                LogUtil.error("roleId:", roleId, " ??????????????????:", e.toString());
            }
            for (Entry<Integer, WallNpc> ks : player.wallNpc.entrySet()) {
                builder.addWallNpc(PbHelper.createWallNpcPb(ks.getValue()));
            }
            pos = player.lord.getPos();
        } else { // ??????????????????
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
                    LogUtil.debug("????????????????????????army=" + army);
                    // it.remove();
                    rmArmy.add(army);
                    continue;
                }
                int armyCnt = army.getHero().get(0).getV2();
                Hero hero = tarPlayer.heros.get(army.getHero().get(0).getV1());
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
     * ????????????
     *
     * @param player
     * @throws MwException
     */
    public void processAutoAddArmy(Player player) throws MwException {

        int now = TimeHelper.getCurrentSecond();
        Resource resource = player.resource;
        ChangeInfo change = ChangeInfo.newIns();

        // NPC??????
        if (!player.isFireState()) {// ????????????????????????
            // ??????id???????????????
            autoNpcAddArmy(player, now, resource, change);
        }
        /*-------------------------------------?????????????????????----------------------------------*/
        autoWallHeroAddArmy(player, now, resource, change);

        rewardDataManager.syncRoleResChanged(player, change);

    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param now
     * @param resource
     * @param change
     * @throws MwException
     */
    private void autoWallHeroAddArmy(Player player, int now, Resource resource, ChangeInfo change) throws MwException {
        int allSecond = 0;// ?????????????????????????????????
        int armyCount = 0; // ?????????????????????????????????????????????
        boolean flag = true;
        for (int i = 1; i < player.heroWall.length; i++) {
            Hero hero = player.heros.get(player.heroWall[i]);
            if (hero == null) {
                continue;
            }
            int maxArmy = hero.getAttr()[AttrId.LEAD];
            if (hero.getCount() >= maxArmy) {
                continue;
            }
            if (flag) {// ???????????????????????????????????????????????????
                allSecond = now - hero.getWallArmyTime();
                double armyPerSec = (1.0 * maxArmy) / HeroConstant.WALL_HERO_AUTO_ARMY_COEFFICIENT;
                armyCount = (int) (allSecond * armyPerSec);
                flag = !flag;
            }

            int add = armyCount;
            if (add < 1) {// ???????????????????????????
                break;
            }
            if (hero.getCount() + add > maxArmy) {
                add = maxArmy - hero.getCount();
            }
            // ????????????
            // ??????????????????
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
                            AwardFrom.WALL_NPC_AUTO_ARMY);// , "??????????????????"
                    hero.setCount(hero.getCount() + add);
                    change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);

                    //??????????????????????????????
                    // LogLordHelper.filterHeroArm(AwardFrom.WALL_NPC_AUTO_ARMY, player.account, player.lord, hero.getHeroId(), hero.getCount(), add,
                    //         Constant.ACTION_ADD, armyType, hero.getQuality());

                    // ??????????????????????????????
//                    LogLordHelper.playerArm(
//                            AwardFrom.WALL_NPC_AUTO_ARMY,
//                            player,
//                            armyType,
//                            Constant.ACTION_ADD,
//                            add,
//                            playerDataManager.getArmCount(player.resource, armyType)
//                    );
                } else {
                    // ?????????,?????????????????????
                    add = (int) (resource.getFood() / radio);
                    needFood = add * radio;
                    if (needFood > 0) {
                        rewardDataManager.subResource(player, AwardType.Resource.FOOD, needFood,
                                AwardFrom.WALL_NPC_AUTO_ARMY);// , "??????????????????"
                        hero.setCount(hero.getCount() + add);
                        change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);

                        //??????????????????????????????
                        // LogLordHelper.filterHeroArm(AwardFrom.WALL_NPC_AUTO_ARMY, player.account, player.lord, hero.getHeroId(), hero.getCount(), add,
                        //         Constant.ACTION_ADD, armyType, hero.getQuality());

                        // ??????????????????????????????
//                        LogLordHelper.playerArm(
//                                AwardFrom.WALL_NPC_AUTO_ARMY,
//                                player,
//                                armyType,
//                                Constant.ACTION_ADD,
//                                add,
//                                playerDataManager.getArmCount(player.resource, armyType)
//                        );
                    }
                }
                hero.setWallArmyTime(now);
                armyCount -= add; // ?????????????????????????????????
                // ????????????????????????????????????????????????????????????
                if (hero.getCount() >= maxArmy) {// ??????????????????????????????????????????????????????
                    for (int j = i + 1; j < player.heroWall.length; j++) {
                        Hero heroNest = player.heros.get(player.heroWall[j]);
                        if (heroNest != null) {
                            heroNest.setWallArmyTime(now);
                        }
                    }
                }
            }
        }
    }

    /**
     * npc???????????????????????????
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
        int allSecond = 0;// ?????????????????????????????????
        int armyCount = 0; // ?????????????????????????????????????????????
        boolean flag = true;// ??????????????????
        for (int i = 0; i < npcList.size(); i++) {
            WallNpc wallNpc = npcList.get(i);
            if (wallNpc.getAutoArmy() > 0) {
                StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr
                        .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
                if (staticWallHeroLv == null) {
                    continue;
                }
                int maxArmy = staticWallHeroLv.getAttr().get(AttrId.LEAD);
                if (wallNpc.getCount() >= maxArmy) { // ????????????????????????
                    wallNpc.setAutoArmy(0);
                    continue;
                }
                if (flag) {// ???????????????????????????????????????????????????
                    allSecond = now - wallNpc.getAddTime();
                    double armyPerSec = (1.0 * maxArmy) / HeroConstant.WALL_HERO_AUTO_ARMY_COEFFICIENT;
                    armyCount = (int) (allSecond * armyPerSec);
                    flag = !flag;
                }
                int add = armyCount;
                if (add < 1) {// ???????????????????????????
                    break;
                }
                if (wallNpc.getCount() + add > maxArmy) {
                    add = maxArmy - wallNpc.getCount();
                }
                if (add > 0) {
                    if (wallNpc.getCount() + add > maxArmy) {
                        add = maxArmy - wallNpc.getCount();
                    }
                    // ????????????
                    // ??????????????????
                    int radio = techDataManager.getFood4ArmyType(player, staticWallHeroLv.getType());
                    if (radio == 0) {
                        radio = Constant.FACTORY_ARM_NEED_FOOD;
                    }
                    int needFood = add * radio;
                    if (needFood > 0) {
                        if (resource.getFood() < needFood) {
                            // ?????????,?????????????????????
                            add = (int) (resource.getFood() / radio);
                            needFood = add * radio;
                        }
                        if (needFood > 0) {
                            rewardDataManager.subResource(player, AwardType.Resource.FOOD, needFood,
                                    AwardFrom.WALL_NPC_AUTO_ARMY);// , "NPC????????????"
                            wallNpc.setCount(wallNpc.getCount() + add);
                            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
                        }
                        wallNpc.setAddTime(now);
                    }
                    armyCount -= add; // ?????????????????????????????????
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
     * ????????????
     *
     * @param roleId
     * @param pos
     * @param heroId
     * @param type
     * @return
     * @throws MwException
     */
    public WallSetRs doWallSet(Long roleId, int pos, int heroId, int type, boolean swap, boolean swapTreasure, boolean swapMedal) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // ?????????????????????????????????
        if (player.building.getWar() < Constant.CABINET_CONDITION.get(1)) {
            // ??????????????????3???????????????
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "?????? ????????????");
        }

        // ??????pos???????????????
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "?????????????????????????????????, roleId:", roleId, ", pos:",
                    pos);
        }
        // for (int id : player.heroBattle) {
        // if (heroId == id) {
        // throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), "???????????????, roleId:", roleId, ", heroId:",
        // heroId);
        // }
        // }

        // ????????????????????????
        List<Integer> lvRequire = Constant.GUARDS_HERO_REQUIRE;
        if (CheckNull.isEmpty(lvRequire) || lvRequire.size() != 4) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????????????????????????????, roleId:", roleId, ", pos:", pos);
        }
        int lv = player.lord.getLevel();
        // ????????????????????????
        if (pos == HeroConstant.HERO_BATTLE_1) {
            if (lv < lvRequire.get(0)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "?????????,??????????????????????????????????????? roleId:",
                        roleId, ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_2) {
            if (lv < lvRequire.get(1)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "?????????,??????????????????????????????????????? roleId:",
                        roleId, ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_3) {
            if (lv < lvRequire.get(2)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "?????????,??????????????????????????????????????? roleId:",
                        roleId, ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_4) {
            if (lv < lvRequire.get(3)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "?????????,??????????????????????????????????????? roleId:",
                        roleId, ", pos:", pos);
            }
        }

        Hero hero = heroService.checkHeroIsExist(player, heroId);
        WallSetRs.Builder builder = WallSetRs.newBuilder();
        boolean sysClientUpdateMedal = false;
        if (type == 1) {
            // ????????????????????????????????????
            if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId)) {
                throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), "????????????????????????, roleId:", roleId, ", heroId:",
                        heroId);
            }
            Hero battleHero = player.getWallHeroByPos(pos);

            if (null != battleHero) {// ?????????????????????????????????????????????????????????
                if (!battleHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "????????????????????????????????????");
                }
                // ?????????????????????????????????????????????????????????
                if (swap) {// ??????????????????????????????????????????????????????
                    // rewardDataManager.checkBagCnt(player);
                    heroService.swapHeroEquip(player, hero, battleHero);
                }
                if (swapTreasure) {// ??????????????????????????????????????????????????????
                    heroService.swapHeroTreasure(player, battleHero, hero);
                }
                if (swapMedal) {// ??????????????????????????????????????????????????????
                    heroService.swapHeroMedal(player, battleHero, hero);
                    sysClientUpdateMedal = true;
                }
                downWallHeroAndBackRes(player, battleHero);
                // ?????????????????????????????????
                CalculateUtil.processAttr(player, battleHero);
                // ??????
                builder.setDownHero(PbHelper.createHeroPb(battleHero, player));
            }

            List<TwoInt> seasonTalentAttr = null;
            // ????????????
            hero.onWall(pos);
            player.heroWall[pos] = heroId;// ?????????????????????????????????
            // ??????????????????
            reAdjustHeroPos(player.heroWall, player.heros);
            // ?????????????????????????????????
            CalculateUtil.processAttr(player, hero);
            // ????????????????????????
            if (hero.isOnWall()) {
                //???????????????????????????
                List<TwoInt> janitorAttr = DataResource.getBean(SeasonTalentService.class).getSeasonTalentEffectTwoInt(player, hero, SeasonConst.TALENT_EFFECT_619);
                if (!ObjectUtils.isEmpty(janitorAttr)) {
                    seasonTalentAttr = new ArrayList<>(janitorAttr);
                }
            }

            builder.setUpHero(PbHelper.createHeroPb(hero, player, seasonTalentAttr));
        } else {
            // ??????
            int myPos = 0;
            for (int i = 1; i < player.heroAcq.length; i++) {
                if (player.heroWall[i] == heroId) {
                    myPos = i;
                    break;
                }
            }
            Hero battleHero = player.getWallHeroByPos(myPos);
            if (null != battleHero) {// ?????????????????????????????????????????????????????????
                if (!battleHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "????????????????????????????????????");
                }
                downWallHeroAndBackRes(player, battleHero);
                player.heroWall[myPos] = 0; // ??????????????????0
                // ??????????????????
                reAdjustHeroPos(player.heroWall, player.heros);
                // ?????????????????????????????????
                CalculateUtil.processAttr(player, battleHero);
                // ??????
                builder.setDownHero(PbHelper.createHeroPb(battleHero, player));
            }
        }
        for (int i = 1; i < player.heroWall.length; i++) {
            if (player.heroWall[i] != 0)
                builder.addHeroIds(player.heroWall[i]);
        }

        builder.setUpdateMedal(sysClientUpdateMedal);
        return builder.build();
    }

    /**
     * ??????????????????
     * @param heroIds
     * @param heros
     */
    public void reAdjustHeroPos(int[] heroIds, Map<Integer, Hero> heros) {
        // ??????????????????
        List<Integer> heroList = new ArrayList<>();
        for (int i = 1; i < heroIds.length; i++) {
            int hid = heroIds[i];
            if (heros.get(hid) != null) {
                heroList.add(hid);
            }
        }
        for (int i = 0; i < heroIds.length - 1; i++) {
            int pos = i + 1;
            if (i < heroList.size()) {
                int hId = heroList.get(i);
                heroIds[pos] = hId;
                heros.get(hId).onWall(pos);
            } else {
                // ?????? ??????
                heroIds[pos] = 0;
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param downHero ??????????????????
     */
    private void downWallHeroAndBackRes(Player player, Hero downHero) {
        downHero.onWall(0);// ???????????????pos?????????0
        // ????????????
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
        //??????????????????????????????
        // LogLordHelper.filterHeroArm(AwardFrom.HERO_DOWN, player.account, player.lord, downHero.getHeroId(), downHero.getCount(), -sub,
        //         Constant.ACTION_SUB, armyType, downHero.getQuality());

        // ??????????????????????????????
//        LogLordHelper.playerArm(
//                AwardFrom.HERO_DOWN,
//                player,
//                armyType,
//                Constant.ACTION_SUB,
//                -sub,
//                playerDataManager.getArmCount(player.resource, armyType)
//        );

        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
        rewardDataManager.syncRoleResChanged(player, change);

    }

    /**
     * ????????????NPC
     *
     * @param roleId
     * @return
     */
    public WallNpcRs doWallNpc(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticWallHero staticWallHero = StaticBuildingDataMgr.getWallHero(id);
        if (staticWallHero == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????NPC,??????????????? roleId:", roleId, ", id:", id);
        }
        if (staticWallHero.getNeedWallLv() > buildingDataManager.getBuildingTopLv(player, BuildingType.WALL)) {
            throw new MwException(GameError.WALL_LV_NOT_NPC.getCode(), "??????????????????,????????????????????? roleId:", roleId, ", id:", id);
        }
        if (player.wallNpc.containsKey(id)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "?????????????????????????????????, roleId:", roleId, ", id:",
                    id);
        }
        WallNpc wallNpc = new WallNpc();
        wallNpc.setHeroNpcId(RandomUtil.getKeyByMap(staticWallHero.getGainHero(), 0));
        wallNpc.setId(id);
        wallNpc.setAddTime(TimeHelper.getCurrentSecond());
        StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
        wallNpc.setCount(staticWallHeroLv.getAttr().get(Constant.AttrId.LEAD));
        player.wallNpc.put(id, wallNpc);
        // ????????????????????????????????????????????????
        // player.removePushRecord(PushConstant.WALL_RECRUIT);
        WallNpcRs.Builder builder = WallNpcRs.newBuilder();
        for (Entry<Integer, WallNpc> ks : player.wallNpc.entrySet()) {
            builder.addWallNpc(PbHelper.createWallNpcPb(ks.getValue()));
        }
        return builder.build();
    }

    /**
     * ??????NPC ??????
     *
     * @param roleId
     * @param pos
     * @param type   0???????????? 1????????????
     * @return
     * @throws MwException
     */
    public WallNpcLvUpRs doWallNpcLvUp(Long roleId, int pos, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!player.wallNpc.containsKey(pos)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "?????????????????????????????????, roleId:", roleId, ", id:",
                    pos);
        }
        WallNpc wallNpc = player.wallNpc.get(pos);

        StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel() + 1);
        if (staticSuperEquipLv == null) {
            throw new MwException(GameError.WALLNPC_LV_FULL.getCode(), "??????????????????, roleId:", roleId, ", type:", pos);
        }
        int wallLv = BuildingDataManager.getBuildingTopLv(player, BuildingType.WALL);
        staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());

        if (staticSuperEquipLv.getNeedWallLv() > wallLv) {
            throw new MwException(GameError.WALLNPC_NEED_WALL_LV.getCode(), "NPC???????????????????????????????????????????????? roleId:", roleId,
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
                throw new MwException(GameError.SUPER_EQUIP_LV_FULL.getCode(), "??????????????????, roleId:", roleId, ", type:",
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
     * ??????????????????
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
            throw new MwException(GameError.EMPTY_POS.getCode(), "???????????????????????????????????????, roleId:", roleId, ", pos:", pos);
        }
        Player target = worldDataManager.getPosData(pos);
        //??????????????????????????????,?????????????????????
        if (player.getDecisiveInfo().isDecisive() || target.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_NO_ATK.getCode(), "????????????????????????, roleId:", roleId,
                    ", pos:", pos);
        }
        // ????????????????????????
        worldService.checkFormHero(player, heroIdList);

        if (!worldDataManager.isPlayerPos(pos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "???????????????????????????????????????, roleId:", roleId, ", pos:", pos);
        }

        if (target.lord.getArea() != player.lord.getArea()) {
            throw new MwException(GameError.WALL_HELP_AREA_ERROR.getCode(), "????????????????????????, roleId:", roleId, ", area:",
                    target.lord.getArea());
        }
        // ??????????????????
        int wallLv = BuildingDataManager.getBuildingLv(BuildingType.WALL, target);
        if (wallLv < Constant.GARRISON_WALL_REQUIRE_LV) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", wallLv:", wallLv);
        }
        // ????????????
        StaticBuildingLv buildingLv = StaticBuildingDataMgr
                .getStaticBuildingLevel(BuildingType.WALL, target.building.getWall());
        int max = buildingLv != null && !CheckNull.isEmpty(buildingLv.getCapacity()) ?
                buildingLv.getCapacity().get(0).get(0) :
                0;
        List<Army> guardArmys = worldDataManager.getPlayerGuard(pos);
        if (guardArmys != null && guardArmys.size() + heroIdList.size() > max) { // Constant.WALL_HELP_MAX_NUM
            throw new MwException(GameError.WALL_HELP_FULL.getCode(), "????????????????????????????????????, roleId:", roleId, ", pos:", pos,
                    ",max:", max);
        }

        // ?????????
        if (target.lord.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.NOT_SELF_CAMP_CITY.getCode(), "?????????????????????????????????, roleId:", roleId, ", pos:",
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
            List<TwoInt> form = new ArrayList<>();
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));

            Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_GUARD, pos, ArmyConstant.ARMY_STATE_MARCH,
                    form, marchTime, now + marchTime, player.getDressUp());
            army.setLordId(roleId);
            army.setTarLordId(target.roleId);
            army.setOriginPos(player.lord.getPos());
            Optional.ofNullable(medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0))
                    .ifPresent(medal -> {
                        army.setHeroMedals(Collections.singletonList(PbHelper.createMedalPb(medal)));
                    });
            //??????????????? ??????????????????,??????????????????????????????
            army.setSeasonTalentAttr(DataResource.getBean(SeasonTalentService.class).
                    getSeasonTalentEffectTwoInt(worldDataManager.getPosData(pos), hero, SeasonConst.TALENT_EFFECT_612));

            player.armys.put(army.getKeyId(), army);

            // ??????????????????
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            builder.addArmy(PbHelper.createArmyPb(army, false));
        }
        // ???????????????
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
        return builder.build();
    }

    /**
     * ??????????????????
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
            throw new MwException(GameError.EMPTY_POS.getCode(), "??????????????????,???????????????, roleId:", roleId, ", keyId:", keyId);
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
     * ??????????????????
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
            throw new MwException(GameError.EMPTY_POS.getCode(), "??????????????????,???????????????, roleId:", roleId, ", keyId:", keyId);
        }
        int now = TimeHelper.getCurrentSecond();
        worldService.retreatArmyByDistance(tarPlayer, army, now);
        worldService.synRetreatArmy(tarPlayer, army, now);
        worldDataManager.removePlayerGuard(army.getTarget(), army);
        // ???????????????????????????????????????
        int heroId = army.getHero().get(0).getV1();
        mailDataManager
                .sendNormalMail(tarPlayer, MailConstant.MOLD_GARRISON_REPATRIATE, now, player.lord.getNick(), heroId,
                        player.lord.getNick(), heroId);
        WallGetOutRs.Builder builder = WallGetOutRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param pos
     * @param isAll true??????????????????, false??????????????????
     */
    public void retreatArmy(int pos, boolean isAll, Player targetP) {
        List<Army> armys = worldDataManager.getPlayerGuard(pos);
        if (armys == null) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        LogUtil.debug("??????????????????????????????=" + armys + ",??????????????????=" + isAll);
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
                // ?????????????????????
                int heroId = army.getHero().get(0).getV1();
                    /* mailDataManager.sendNormalMail(player, MailConstant.DECISIVE_BATTLE_GARRISON_CANCEL, now, targetNick,xyInArea.getA(),xyInArea.getB(),heroId,
                             targetNick,xyInArea.getA(),xyInArea.getB(),heroId);*/
                mailDataManager
                        .sendNormalMail(player, MailConstant.WALL_HELP_KILLED, now, targetNick, heroId, targetNick,
                                heroId);
                armys.remove(army); // ?????????????????????armys CopyOnWriteArrayList,???????????? ??????????????????????????????
            } else {
                for (TwoInt twoInt : army.getHero()) {
                    hero = player.heros.get(twoInt.getV1());
                    if (hero != null && hero.getCount() <= 0) {
                        worldService.retreatArmyByDistance(player, army, now);
                        worldService.synWallCallBackRs(0, army);
                        worldService.synRetreatArmy(player, army, now);
                        LogUtil.debug("??????????????????????????????,??????=" + army + ",hero=" + hero);
                        // ?????????????????????
                        int heroId = hero.getHeroId();
                        mailDataManager.sendNormalMail(player, MailConstant.WALL_HELP_KILLED, now, targetNick, heroId,
                                targetNick, heroId);
                        // worldDataManager.removePlayerGuard(pos, army);
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
     * ??????npc????????????
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public WallNpcArmyRs doWallNpcArmy(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!player.wallNpc.containsKey(id)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "????????????NPC, roleId:", roleId, ", id:", id);
        }
        StaticWallHero staticWallHero = StaticBuildingDataMgr.getWallHero(id);

        WallNpc wallNpc = player.wallNpc.get(id);

        StaticWallHeroLv beforeNpc = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
        if (beforeNpc != null && beforeNpc.getChangeArmy() == 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????, roleId:", roleId, ", type:", id);
        }
        // ??????
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                Constant.WALL_CHANGE_ARMY_GOLD, AwardFrom.WALL_CHANGE_ARMY);
        double coef = (wallNpc.getCount() * 1.0) / beforeNpc.getAttr().getOrDefault(Constant.AttrId.LEAD, 0);

        wallNpc.setHeroNpcId(RandomUtil.getKeyByMap(staticWallHero.getGainHero(), wallNpc.getHeroNpcId()));
        StaticWallHeroLv afterNpc = StaticBuildingDataMgr
                .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());

        int count = (int) Math.ceil(coef * afterNpc.getAttr().get(AttrId.LEAD));
        LogUtil.debug("??????npc????????????: ????????????, heroId: ", beforeNpc.getHeroId(), ", count:", wallNpc.getCount(), ", ????????????, heroId:", afterNpc.getHeroId(), ", count:", count);
        wallNpc.setCount(count);

        WallNpcArmyRs.Builder builder = WallNpcArmyRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        return builder.build();
    }

    /**
     * ??????NPC??????????????????
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public WallNpcAutoRs doWallNpcAuto(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!player.wallNpc.containsKey(id)) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "?????????????????????????????????, roleId:", roleId, ", id:",
                    id);
        }
        if (player.isFireState()) {
            throw new MwException(GameError.WALL_IS_FIRE_NOT_AUTO_ARMY.getCode(), "??????????????????????????????, roleId:", roleId);
        }
        WallNpc wallNpc = player.wallNpc.get(id);
        wallNpc.setAutoArmy(1);
        WallNpcAutoRs.Builder builder = WallNpcAutoRs.newBuilder();
        builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        return builder.build();
    }

    /**
     * ??????????????????
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
        // ??????NPC
        if (type == 2) {
            if (!player.wallNpc.containsKey(id)) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "?????????????????????????????????, roleId:", roleId,
                        ", id:", id);
            }
            WallNpc wallNpc = player.wallNpc.get(id);
            StaticWallHeroLv staticWallHeroLv = StaticBuildingDataMgr
                    .getWallHeroLv(wallNpc.getHeroNpcId(), wallNpc.getLevel());
            double d = (wallNpc.getCount() * 1.0) / staticWallHeroLv.getAttr().get(AttrId.LEAD);
            int needGold = (int) Math.ceil((1 - d) * Constant.WALL_FULL_ARMY_NEED_GOLD);
            if (needGold <= 1) {
                needGold = 1;
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.WALL_FULL_ARMY);

            wallNpc.setCount(staticWallHeroLv.getAttr().get(Constant.AttrId.LEAD));
            wallNpc.setAutoArmy(0);
            builder.setWallNpc(PbHelper.createWallNpcPb(wallNpc));
        } else {
            if (!player.isOnWallHero(id)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "???????????????, roleId:", roleId, ", heroId:", id);
            }
            Hero hero = player.heros.get(id);
            if(Objects.isNull(hero)){
                throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.PARAM_ERROR.errMsg(roleId,"???????????????",id));
            }
            if (hero.getCount() >= hero.getAttr()[AttrId.LEAD]) {
                throw new MwException(GameError.HERO_LEAD_IS_MAX.getCode(), "??????????????????,???????????????????????????");
            }
            double d = (hero.getCount() * 1.0) / hero.getAttr()[AttrId.LEAD];
            int needGold = (int) Math.ceil((1 - d) * Constant.WALL_FULL_ARMY_NEED_GOLD);
            if (needGold <= 1) {
                needGold = 1;
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.WALL_FULL_ARMY);
            hero.setCount(hero.getAttr()[AttrId.LEAD]);
            builder.setHero(PbHelper.createHeroPb(hero, player));

            // ??????????????????????????????
            int now = TimeHelper.getCurrentSecond();
            for (int j = 1; j < player.heroWall.length; j++) {
                Hero heroNest = player.heros.get(player.heroWall[j]);
                if (heroNest != null) {
                    heroNest.setWallArmyTime(now);
                }
            }
        }
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * ??????????????????
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
                // ????????????????????????
                int armyCnt = army.getHero().get(0).getV2();
                Hero hero = tarPlayer.heros.get(army.getHero().get(0).getV1());
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
     * ????????????
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
     * ??????????????????????????????
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
