package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
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
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
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
     * 对指定坐标发起决战
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackDecisiveBattleRs decisiveBattleRs(long roleId, AttackDecisiveBattleRq req) throws MwException {
        int attHeroLevel = 0;
        int heroLevel = 0;

        // 检查角色是否存在
        Player atkP = playerDataManager.checkPlayerIsExist(roleId);

        Map<Integer, Integer> mixtureData = atkP.getMixtureData();
        // s_system表中获取决战指挥官等级限制
        List<Integer> playerLevel = WorldConstant.DECISIVE_BATTLE_LEVEL;
        if (playerLevel == null) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "s_system表中未配置决战指挥官等级, roleId:", roleId);
        }
        // 双方坐标
        int defPos = req.getPos();
        if (worldDataManager.isEmptyPos(defPos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "该坐标为空闲坐标，不能攻击或采集, roleId:", roleId, ", defPos:",
                    defPos);
        }
        Player defP = worldDataManager.getPosData(defPos);
        if (CheckNull.isNull(defP)) {
            StringBuffer message = new StringBuffer();
            message.append("角色不存在, roleId:").append(roleId);
            throw new MwException(GameError.PLAYER_NOT_EXIST.getCode(), message.toString());
        }

        // 决战信息
        DecisiveInfo atkInfo = atkP.getDecisiveInfo();
        DecisiveInfo defInfo = defP.getDecisiveInfo();
        // 各势力类型的条件判断
        int now = TimeHelper.getCurrentSecond();
        if (worldDataManager.isPlayerPos(defPos)) {
            // 如果发起决战玩家或者被选择目标玩家等级小于100级时不能进行决战
            if (atkP.lord.getLevel() < playerLevel.get(0)) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(),
                        "发起决战玩家等级<" + attHeroLevel + "级,不能发起攻击, roleId:", roleId, ", level:", defP.lord.getLevel());
            }
            if (defP.lord.getLevel() < playerLevel.get(1)) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "被决战玩家等级<" + heroLevel + "级,不能被攻击, roleId:",
                        defP.roleId, ", level:", defP.lord.getLevel());
            }
            // 判断双方是否有在决战
            if (atkInfo.isDecisive() || defInfo.isDecisive()) {
                throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "玩家决战中..不能再次决战, roleId:", atkP.roleId,
                        ", defPos:", defPos);
            }
        }

        List<PartnerHero> heroIdList = new ArrayList<>();
        heroIdList.addAll(req.getHeroIdList().stream().distinct().map(heroId ->
                atkP.getPlayerFormation().getPartnerHero(heroId)).filter(pa -> !HeroUtil.isEmptyPartner(pa)).collect(Collectors.toList()));
        // 检查出征将领信息
        worldService.checkFormHeroSupport(atkP, heroIdList, defPos);

        Hero hero;
        int armCount = 0;
        int defArmCount = 0;
        List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
        for (PartnerHero partnerHero : heroIdList) {
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            form.add(partnerHero.convertTo());
            armCount += partnerHero.getPrincipalHero().getCount();
        }

        // 各势力类型的条件判断
        int battleId;
        long tarLordId;
        int battleType = WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE;// 9:决战
        int marchTime = worldService.marchTime(atkP, defPos);
        int needFood = worldService.checkMarchFood(atkP, marchTime, armCount); // 检查补给

        // 战斗即将开始时间(默认的规则)
        int battleMarchTime = marchTime;
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(defP.lord.getArea());
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(atkP.lord.getArea());
        if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// 自己在州的情况,州只能打州
            if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许,发起决战, roleId:", roleId,
                        ", my area:", mySArea.getArea(), ", defP area:", defP.lord.getArea());
            }

        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // 自己在郡只能打本区域的
            if (targetSArea.getArea() != mySArea.getArea()) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许,发起决战, roleId:", roleId,
                        ", my area:", mySArea.getArea(), ", defP area:", defP.lord.getArea());
            }
        }

        // 对方开启自动补兵
        playerDataManager.autoAddArmy(defP);

        // 检测战斗消耗
        worldService.checkPower(atkP, battleType, marchTime);
        rewardDataManager.checkAndSubPlayerResHasSync(atkP, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        // 玩家每日决战次数相关
        int battleCount = atkP.getMixtureData().getOrDefault(PlayerConstant.DECISIVE_BATTLE_COUNT, 0);// 次数
        List<Integer> val = StaticPartyDataMgr
                .getJobPrivilegeVal(PartyConstant.Job.KING, PartyConstant.PRIVILEGE_DECISIVE);
        if (CheckNull.isEmpty(val)) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "司令官配置异常, roleId:", roleId, ", job:",
                    atkP.lord.getJob());
        }
        // 如果玩家是司令每天有免费次数
        if (atkP.lord.getJob() != PartyConstant.Job.KING || battleCount >= val.get(1)) {
            // 扣除玩家指定资源
            rewardDataManager.checkAndSubPlayerResHasSync(atkP, AwardType.PROP, PropConstant.ITEM_ID_5044, 1,
                    AwardFrom.USE_PROP);
        }
        // 记录决战次数
        mixtureData.put(PlayerConstant.DECISIVE_BATTLE_COUNT, battleCount + 1);

        // 发起决战时取消城战, 以及撤回驻防部队
        cancelCityBattle(atkP, defP);

        // 设置玩家的决战状态
        atkInfo.setDecisive(true);
        defInfo.setDecisive(true);

        for (PartnerHero partnerHero : defP.getPlayerFormation().getHeroBattle()) {
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            defArmCount += partnerHero.getPrincipalHero().getCount();
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

        // 通知被攻击玩家
        if (defP.isLogin) {
            worldService
                    .syncAttackRole(defP, atkP.lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1, battleType);
        }

        // 给被攻击玩家推送消息（应用外推送）
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
                .map(partnerHero -> medalDataManager.getHeroMedalByHeroIdAndIndex(atkP, partnerHero.getPrincipalHero().getHeroId(), MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        atkP.armys.put(army.getKeyId(), army);
        // 攻击玩家, 加入部队逻辑全部放入到达后加入队列
        if (worldDataManager.isPlayerPos(defPos) && battle != null) {
            worldService.addBattleArmy(battle, atkP.roleId, form, army.getKeyId(), true);
        }

        // 添加行军路线
        March march = new March(atkP, army);
        worldDataManager.addMarch(march);
        // 改变行军状态
        for (PartnerHero partnerHero : heroIdList) {
            partnerHero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        // 返回协议
        Builder builder = AttackDecisiveBattleRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        if (battle != null) {
            builder.setBattle(PbHelper.createBattlePb(battle));
        }

        builder.setBattleCount(atkP.getMixtureData().getOrDefault(PlayerConstant.DECISIVE_BATTLE_COUNT, 0));
        // 区域变化推送
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(defPos, atkP.lord.getPos()));
        posList.add(defPos);
        posList.add(atkP.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, roleId,
                Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));

        return builder.build();
    }

    /**
     * 发起决战时取消城战, 以及撤回驻防部队
     *
     * @param atkP
     * @param defP
     */
    private void cancelCityBattle(Player atkP, Player defP) throws MwException {
        cancelCityBattle(atkP);
        cancelCityBattle(defP);
    }

    /**
     * 取消城战, 已经撤回驻防部队
     *
     * @param player
     */
    private void cancelCityBattle(Player player) throws MwException {
        if (CheckNull.isNull(player)) {
            return;
        }
        int pos = player.lord.getPos();
        decisiveRetreatArmy(pos, player);// 遣回驻防将领
        player.armys.values().stream().filter(army -> army.getType() == ArmyConstant.ARMY_TYPE_GUARD // 我去驻防的部队返回
                && army.getState() != ArmyConstant.ARMY_STATE_RETREAT).forEach(
                army -> decisiveRetreatArmy(army.getTarget(), playerDataManager.getPlayer(army.getTarLordId())));
        List<Army> armys = player.armys.values().stream()
                .filter(army -> army.getBattleId() != null && army.getBattleId() > 0
                        && army.getType() == ArmyConstant.ARMY_TYPE_ATK_PLAYER
                        && army.getState() != ArmyConstant.ARMY_STATE_RETREAT).collect(Collectors.toList());
        for (Army army : armys) {
            worldService.originalReturnArmyProcess(player.roleId, player, army.getKeyId(), 0, army);
        }
        warService.cancelCityBattle(pos, pos, true, false); // 取消坐标的城战
    }

    /**
     * 决战部队撤回处理
     *
     * @param roleId
     * @param player
     * @param type
     * @param army
     * @throws MwException
     */
    public void retreatDecisiveArmy(long roleId, Player player, int type, Army army) throws MwException {
        // 决战撤回，相关处理
        Integer battleId = army.getBattleId();
        int keyId = army.getKeyId();
        LogUtil.debug("决战部队撤回,battleId=" + battleId + ",keyId=" + keyId + ",type=" + type);
        if (null != battleId && battleId > 0) {// 战斗类型
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (null != battle) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                LogUtil.debug(roleId + ",撤退部队=" + armCount);
                battle.updateArm(camp, -armCount);
                // 主动召回
                worldService.retreatArmy(player, army, TimeHelper.getCurrentSecond(), type);
                if (battle.getType() == WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE) {
                    if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId) {// 发起者撤回

                        // 取消玩家决战状态标识
                        Player atkP = playerDataManager.checkPlayerIsExist(army.getLordId());
                        Player defP = playerDataManager.checkPlayerIsExist(army.getTarLordId());
                        atkP.getDecisiveInfo().setDecisive(false);
                        defP.getDecisiveInfo().setDecisive(false);

                        // 玩家发起的决战，发起人撤回部队，城战取消
                        warService.cancelCityBattle(army.getTarget(), true, battle, false);

                        // 通知撤退, 客户端收到消息 重新拉数据
                        if (defP != null && defP.isLogin) {
                            worldService
                                    .syncAttackRole(defP, player.lord, army.getEndTime(), WorldConstant.ATTACK_ROLE_0,
                                            battle.getBattleType());
                        }

                        // 更新地图速度
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
     * 决战时遣返回所有有驻防部队
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
        LogUtil.debug("决战时，驻防部队撤回=" + armys);
        Player player;
        String nick = target.lord.getNick();
        for (Army army : armys) {
            player = playerDataManager.getPlayer(army.getLordId());
            if (player == null) {
                LogUtil.debug("retreatArmy,player is null," + army.getLordId());
                continue;
            }
            worldService.retreatArmy(player, army, now);    // 不带地图同步
            worldService.synRetreatArmy(player, army, now); // 同步army状态
            worldDataManager.removePlayerGuard(army.getTarget(), army); // 移除驻防部队
            // 给派兵驻防的玩家发遣返邮件
            int heroId = army.getHero().get(0).getPrincipleHeroId();
            mailDataManager
                    .sendNormalMail(player, MailConstant.DECISIVE_BATTLE_GARRISON_CANCEL, now, nick, xyInArea.getA(),
                            xyInArea.getB(), heroId, nick, xyInArea.getA(), xyInArea.getB(), heroId);
        }

    }

    /**
     * 获取失败消息
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
     * 获取决战指令
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.DecisiveBattleRs getDecisiveBattleInstruction(Long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        DecisiveInfo decisiveInfo = player.getDecisiveInfo();
        decisiveInfo.init(); // 初始化
        decisiveInfo.checkPropStatus(); // 检测产出状态

        GamePb4.DecisiveBattleRs.Builder builder = GamePb4.DecisiveBattleRs.newBuilder();
        builder.setInstructionTime(decisiveInfo.getPropTime());
        builder.setInstructionStatus(decisiveInfo.isDecisive() ? 1 : 0);
        return builder.build();
    }

    /**
     * 领取决战指令
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GainInstructionsRs getGainInstructions(Long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 初始化
        DecisiveInfo decisiveInfo = player.getDecisiveInfo();
        decisiveInfo.init();

        int now = TimeHelper.getCurrentSecond();
        if (now < decisiveInfo.getPropTime()) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "暂时无法领取决战指令, roleId:", roleId);
        }

        if (CheckNull.isEmpty(StaticBuildingDataMgr.getBobmConf())) {
            throw new MwException(GameError.SYSTEM_NO_CONFIG.getCode(), "暂时无法领取决战指令, roleId:", roleId);
        }
        CommonPb.Award award = rewardDataManager.addAwardSignle(player, AwardType.PROP, PropConstant.ITEM_ID_5044,
                StaticBuildingDataMgr.getBobmConf().get(1), AwardFrom.DECISIVE_FREE_AWARD);

        // 下次的产出时间
        decisiveInfo.nextPropTime();
        com.gryphpoem.game.zw.pb.GamePb4.GainInstructionsRs.Builder builder = GamePb4.GainInstructionsRs.newBuilder();
        builder.setInstructionStatus(decisiveInfo.isPropStatus() ? 1 : 0);
        builder.setInstructionTime(decisiveInfo.getPropTime());
        builder.addAward(award);
        return builder.build();
    }

}
