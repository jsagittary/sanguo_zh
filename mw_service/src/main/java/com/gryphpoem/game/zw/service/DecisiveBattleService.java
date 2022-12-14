package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.AttackDecisiveBattleRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackDecisiveBattleRs;
import com.gryphpoem.game.zw.pb.GamePb4.AttackDecisiveBattleRs.Builder;
import com.gryphpoem.game.zw.pb.GamePb4.BattleFailMessageRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.DecisiveInfo;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DecisiveBattleService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private MedalDataManager medalDataManager;

    /**
     * ???????????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackDecisiveBattleRs decisiveBattleRs(long roleId, AttackDecisiveBattleRq req) throws MwException {
        int attHeroLevel = 0;
        int heroLevel = 0;

        // ????????????????????????
        Player atkP = playerDataManager.checkPlayerIsExist(roleId);

        Map<Integer, Integer> mixtureData = atkP.getMixtureData();
        // s_system???????????????????????????????????????
        List<Integer> playerLevel = WorldConstant.DECISIVE_BATTLE_LEVEL;
        if (playerLevel == null) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "s_system????????????????????????????????????, roleId:", roleId);
        }
        // ????????????
        int defPos = req.getPos();
        if (worldDataManager.isEmptyPos(defPos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "????????????????????????????????????????????????, roleId:", roleId, ", defPos:",
                    defPos);
        }
        Player defP = worldDataManager.getPosData(defPos);
        if (CheckNull.isNull(defP)) {
            StringBuffer message = new StringBuffer();
            message.append("???????????????, roleId:").append(roleId);
            throw new MwException(GameError.PLAYER_NOT_EXIST.getCode(), message.toString());
        }

        // ????????????
        DecisiveInfo atkInfo = atkP.getDecisiveInfo();
        DecisiveInfo defInfo = defP.getDecisiveInfo();
        // ??????????????????????????????
        int now = TimeHelper.getCurrentSecond();
        if (worldDataManager.isPlayerPos(defPos)) {
            // ???????????????????????????????????????????????????????????????100????????????????????????
            if (atkP.lord.getLevel() < playerLevel.get(0)) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(),
                        "????????????????????????<" + attHeroLevel + "???,??????????????????, roleId:", roleId, ", level:", defP.lord.getLevel());
            }
            if (defP.lord.getLevel() < playerLevel.get(1)) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "?????????????????????<" + heroLevel + "???,???????????????, roleId:",
                        defP.roleId, ", level:", defP.lord.getLevel());
            }
            // ??????????????????????????????
            if (atkInfo.isDecisive() || defInfo.isDecisive()) {
                throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "???????????????..??????????????????, roleId:", atkP.roleId,
                        ", defPos:", defPos);
            }
        }

        List<Integer> heroIdList = new ArrayList<>();
        heroIdList.addAll(req.getHeroIdList());
        heroIdList = heroIdList.stream().distinct().collect(Collectors.toList());
        // ????????????????????????
        worldService.checkFormHeroSupport(atkP, heroIdList, defPos);

        Hero hero;
        int armCount = 0;
        int defArmCount = 0;
        List<TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = atkP.heros.get(heroId);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
            armCount += hero.getCount();
        }

        // ??????????????????????????????
        int battleId;
        long tarLordId;
        int battleType = WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE;// 9:??????
        int marchTime = worldService.marchTime(atkP, defPos);
        int needFood = worldService.checkMarchFood(atkP, marchTime, armCount); // ????????????

        // ????????????????????????(???????????????)
        int battleMarchTime = marchTime;
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(defP.lord.getArea());
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(atkP.lord.getArea());
        if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// ?????????????????????,???????????????
            if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????,????????????, roleId:", roleId,
                        ", my area:", mySArea.getArea(), ", defP area:", defP.lord.getArea());
            }

        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // ?????????????????????????????????
            if (targetSArea.getArea() != mySArea.getArea()) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????,????????????, roleId:", roleId,
                        ", my area:", mySArea.getArea(), ", defP area:", defP.lord.getArea());
            }
        }

        // ????????????????????????
        playerDataManager.autoAddArmy(defP);

        // ??????????????????
        worldService.checkPower(atkP, battleType, marchTime);
        rewardDataManager.checkAndSubPlayerResHasSync(atkP, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        // ??????????????????????????????
        int battleCount = atkP.getMixtureData().getOrDefault(PlayerConstant.DECISIVE_BATTLE_COUNT, 0);// ??????
        List<Integer> val = StaticPartyDataMgr
                .getJobPrivilegeVal(PartyConstant.Job.KING, PartyConstant.PRIVILEGE_DECISIVE);
        if (CheckNull.isEmpty(val)) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "?????????????????????, roleId:", roleId, ", job:",
                    atkP.lord.getJob());
        }
        // ??????????????????????????????????????????
        if (atkP.lord.getJob() != PartyConstant.Job.KING || battleCount >= val.get(1)) {
            // ????????????????????????
            rewardDataManager.checkAndSubPlayerResHasSync(atkP, AwardType.PROP, PropConstant.ITEM_ID_5044, 1,
                    AwardFrom.USE_PROP);
        }
        // ??????????????????
        mixtureData.put(PlayerConstant.DECISIVE_BATTLE_COUNT, battleCount + 1);

        // ???????????????????????????, ????????????????????????
        cancelCityBattle(atkP, defP);

        // ???????????????????????????
        atkInfo.setDecisive(true);
        defInfo.setDecisive(true);

        for (Integer heroId : defP.heroBattle) {
            hero = defP.heros.get(heroId);
            if (hero != null) {
                defArmCount += hero.getCount();
            }
        }
        Battle battle = new Battle();
        battle.setType(battleType);
        battle.setBattleType(battleType);
        battle.setBattleTime(now + battleMarchTime);
        battle.setBeginTime(now);
        battle.setDefencerId(defP.lord.getLordId());
        battle.setPos(defPos);
        battle.setSponsor(atkP);
        battle.setDefencer(defP);
        battle.setDefCamp(defP.lord.getCamp());
        battle.addAtkArm(armCount);
        battle.addDefArm(defArmCount);
        battle.getAtkRoles().add(roleId);
        warDataManager.addBattle(atkP, battle);
        battleId = battle.getBattleId();
        tarLordId = defP.lord.getLordId();
        HashSet<Integer> set = atkP.battleMap.get(defPos);
        if (set == null) {
            set = new HashSet<>();
            atkP.battleMap.put(defPos, set);
        }
        set.add(battleId);
        set = defP.battleMap.get(defPos);
        if (set == null) {
            set = new HashSet<>();
            defP.battleMap.put(defPos, set);
        }
        set.add(battleId);

        LogUtil.debug("==atkP.battleMap===" + atkP.battleMap);

        // ?????????????????????
        if (defP.isLogin) {
            worldService
                    .syncAttackRole(defP, atkP.lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1, battleType);
        }

        // ???????????????????????????????????????????????????
        // PushMessageUtil.pushMessage(defP.account, PushConstant.ID_ATTACKED, defP.lord.getNick(), atkP.lord.getNick());

        Army army = new Army(atkP.maxKey(), WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE, defPos,
                ArmyConstant.ARMY_STATE_MARCH, form, marchTime - 1, now + marchTime - 1, atkP.getDressUp());
        army.setBattleId(battleId);
        army.setType(ArmyConstant.ARMY_TYPE_DECISIVE_BATTLE);
        army.setLordId(roleId);
        army.setTarLordId(tarLordId);
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setOriginPos(atkP.lord.getPos());
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(atkP, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        atkP.armys.put(army.getKeyId(), army);
        // ????????????, ???????????????????????????????????????????????????
        if (worldDataManager.isPlayerPos(defPos) && battle != null) {
            worldService.addBattleArmy(battle, atkP.roleId, heroIdList, army.getKeyId(), true);
        }

        // ??????????????????
        March march = new March(atkP, army);
        worldDataManager.addMarch(march);
        // ??????????????????
        for (Integer heroId : heroIdList) {
            hero = atkP.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        // ????????????
        Builder builder = AttackDecisiveBattleRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        if (battle != null) {
            builder.setBattle(PbHelper.createBattlePb(battle));
        }

        builder.setBattleCount(atkP.getMixtureData().getOrDefault(PlayerConstant.DECISIVE_BATTLE_COUNT, 0));
        // ??????????????????
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(defPos, atkP.lord.getPos()));
        posList.add(defPos);
        posList.add(atkP.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, roleId,
                Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));

        return builder.build();
    }

    /**
     * ???????????????????????????, ????????????????????????
     *
     * @param atkP
     * @param defP
     */
    private void cancelCityBattle(Player atkP, Player defP) throws MwException {
        cancelCityBattle(atkP);
        cancelCityBattle(defP);
    }

    /**
     * ????????????, ????????????????????????
     *
     * @param player
     */
    private void cancelCityBattle(Player player) throws MwException {
        if (CheckNull.isNull(player)) {
            return;
        }
        int pos = player.lord.getPos();
        decisiveRetreatArmy(pos, player);// ??????????????????
        player.armys.values().stream().filter(army -> army.getType() == ArmyConstant.ARMY_TYPE_GUARD // ???????????????????????????
                && army.getState() != ArmyConstant.ARMY_STATE_RETREAT).forEach(
                army -> decisiveRetreatArmy(army.getTarget(), playerDataManager.getPlayer(army.getTarLordId())));
        List<Army> armys = player.armys.values().stream()
                .filter(army -> army.getBattleId() != null && army.getBattleId() > 0
                        && army.getType() == ArmyConstant.ARMY_TYPE_ATK_PLAYER
                        && army.getState() != ArmyConstant.ARMY_STATE_RETREAT).collect(Collectors.toList());
        for (Army army : armys) {
            worldService.originalReturnArmyProcess(player.roleId, player, army.getKeyId(), 0, army);
        }
        warService.cancelCityBattle(pos, pos, true, false); // ?????????????????????
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param player
     * @param type
     * @param army
     * @throws MwException
     */
    public void retreatDecisiveArmy(long roleId, Player player, int type, Army army) throws MwException {
        // ???????????????????????????
        Integer battleId = army.getBattleId();
        int keyId = army.getKeyId();
        LogUtil.debug("??????????????????,battleId=" + battleId + ",keyId=" + keyId + ",type=" + type);
        if (null != battleId && battleId > 0) {// ????????????
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (null != battle) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                LogUtil.debug(roleId + ",????????????=" + armCount);
                battle.updateArm(camp, -armCount);
                // ????????????
                worldService.retreatArmy(player, army, TimeHelper.getCurrentSecond(), type);
                if (battle.getType() == WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE) {
                    if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId) {// ???????????????

                        // ??????????????????????????????
                        Player atkP = playerDataManager.checkPlayerIsExist(army.getLordId());
                        Player defP = playerDataManager.checkPlayerIsExist(army.getTarLordId());
                        atkP.getDecisiveInfo().setDecisive(false);
                        defP.getDecisiveInfo().setDecisive(false);

                        // ????????????????????????????????????????????????????????????
                        warService.cancelCityBattle(army.getTarget(), true, battle, false);

                        // ????????????, ????????????????????? ???????????????
                        if (defP != null && defP.isLogin) {
                            worldService
                                    .syncAttackRole(defP, player.lord, army.getEndTime(), WorldConstant.ATTACK_ROLE_0,
                                            battle.getBattleType());
                        }

                        // ??????????????????
                        worldService.processMarchArmy(player.lord.getArea(), army, keyId);

                        HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
                        if (battleIds != null) {
                            battleIds.remove(battleId);
                        }
                    }
                }
            }
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param pos
     * @param target
     */
    public void decisiveRetreatArmy(int pos, Player target) {
        Turple<Integer, Integer> xyInArea = MapHelper.reducePos(pos);
        List<Army> armys = worldDataManager.getPlayerGuard(pos);
        if (CheckNull.isEmpty(armys)) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        LogUtil.debug("??????????????????????????????=" + armys);
        Player player;
        String nick = target.lord.getNick();
        for (Army army : armys) {
            player = playerDataManager.getPlayer(army.getLordId());
            if (player == null) {
                LogUtil.debug("retreatArmy,player is null," + army.getLordId());
                continue;
            }
            worldService.retreatArmy(player, army, now);    // ??????????????????
            worldService.synRetreatArmy(player, army, now); // ??????army??????
            worldDataManager.removePlayerGuard(army.getTarget(), army); // ??????????????????
            // ???????????????????????????????????????
            int heroId = army.getHero().get(0).getV1();
            mailDataManager
                    .sendNormalMail(player, MailConstant.DECISIVE_BATTLE_GARRISON_CANCEL, now, nick, xyInArea.getA(),
                            xyInArea.getB(), heroId, nick, xyInArea.getA(), xyInArea.getB(), heroId);
        }

    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public BattleFailMessageRs getBattleFailMessage(Long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        DecisiveInfo info = player.getDecisiveInfo();
        int flyTime = info.getFlyTime();
        long flyRoleId = info.getFlyRole();
        Player flyRole = playerDataManager.getPlayer(flyRoleId);
        GamePb4.BattleFailMessageRs.Builder builder = BattleFailMessageRs.newBuilder();
        if (!CheckNull.isNull(flyRole) && !CheckNull.isNull(flyRole.lord.getNick())) {
            builder.setHero(flyRole.lord.getNick());
        }

        builder.setTime(flyTime);
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.DecisiveBattleRs getDecisiveBattleInstruction(Long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        DecisiveInfo decisiveInfo = player.getDecisiveInfo();
        decisiveInfo.init(); // ?????????
        decisiveInfo.checkPropStatus(); // ??????????????????

        GamePb4.DecisiveBattleRs.Builder builder = GamePb4.DecisiveBattleRs.newBuilder();
        builder.setInstructionTime(decisiveInfo.getPropTime());
        builder.setInstructionStatus(decisiveInfo.isDecisive() ? 1 : 0);
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GainInstructionsRs getGainInstructions(Long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ?????????
        DecisiveInfo decisiveInfo = player.getDecisiveInfo();
        decisiveInfo.init();

        int now = TimeHelper.getCurrentSecond();
        if (now < decisiveInfo.getPropTime()) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "??????????????????????????????, roleId:", roleId);
        }

        if (CheckNull.isEmpty(StaticBuildingDataMgr.getBobmConf())) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "??????????????????????????????, roleId:", roleId);
        }
        CommonPb.Award award = rewardDataManager.addAwardSignle(player, AwardType.PROP, PropConstant.ITEM_ID_5044,
                StaticBuildingDataMgr.getBobmConf().get(1), AwardFrom.DECISIVE_FREE_AWARD);

        // ?????????????????????
        decisiveInfo.nextPropTime();
        com.gryphpoem.game.zw.pb.GamePb4.GainInstructionsRs.Builder builder = GamePb4.GainInstructionsRs.newBuilder();
        builder.setInstructionStatus(decisiveInfo.isPropStatus() ? 1 : 0);
        builder.setInstructionTime(decisiveInfo.getPropTime());
        builder.addAward(award);
        return builder.build();
    }

}
