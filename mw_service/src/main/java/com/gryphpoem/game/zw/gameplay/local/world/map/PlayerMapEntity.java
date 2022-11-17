package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.AttackPlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapForce.Builder;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.HeroUtil;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.RebelService;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName RoleMapEntity.java
 * @Description
 * @date 2019年3月21日
 */
public class PlayerMapEntity extends BaseWorldEntity {

    private final Player player;

    private final List<Guard> helpGurad; // 守军部队

    public PlayerMapEntity(int pos, Player player) {
        super(pos, WorldEntityType.PLAYER);
        this.player = player;
        this.helpGurad = new ArrayList<>();
    }

    public Player getPlayer() {
        return player;
    }

    public List<Guard> getHelpGurad() {
        return helpGurad;
    }

    public CommonPb.AreaForce toAreaForcePb() {
        Player p = this.player;
        return PbHelper.createAreaForcePb(p.lord.getPos(), p.lord.getCamp(), p.lord.getLevel(),
                p.building.getCommand());
    }

    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        // 同阵营不允许战斗
        long roleId = param.getInvokePlayer().roleId;
        Player invokePlayer = param.getInvokePlayer();
        Player targetPlayer = this.player;
        if (targetPlayer.lord.getCamp() == invokePlayer.lord.getCamp()) {
            throw new MwException(GameError.SAME_CAMP.getCode(), "同阵营，不能攻击, roleId:", roleId, ", pos:", pos);
        }
        int now = TimeHelper.getCurrentSecond();
        Effect effect = targetPlayer.getEffect().get(EffectConstant.PROTECT);
        if (effect != null && effect.getEndTime() > now) {
            throw new MwException(GameError.PROTECT.getCode(), "该坐标开启保护，不能攻击, roleId:", roleId, ", pos:",
                    pos + ",tarRoleId:" + targetPlayer.roleId);
        }
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        if (crossWorldMap.isInSafeArea(targetPlayer)) {
            throw new MwException(GameError.WAR_FIRE_PLAYER_IN_SAFE_AREA.getCode(), "该坐标在安全区，不能攻击, roleId:", roleId, ", pos:", pos + ",tarRoleId:" + targetPlayer.roleId);
        }
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        // 对方开启自动补兵
        playerDataManager.autoAddArmy(targetPlayer);
        rewardDataManager.checkAndSubPlayerResHasSync(invokePlayer, AwardType.RESOURCE, AwardType.Resource.FOOD,
                param.getNeedFood(), AwardFrom.ATK_POS);

        int marchTime = param.getMarchTime();
        int battleMarchTime = param.getMarchTime();
        int battleType = param.getBattleType();
        if (marchTime <= WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) {// 行军时间小于5分钟
            if (WorldConstant.CITY_BATTLE_RAID == battleType) {
                battleMarchTime = marchTime + WorldConstant.CITY_BATTLE_INCREASE_TIME;
            } else if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                battleMarchTime = marchTime + (WorldConstant.CITY_BATTLE_INCREASE_TIME * 2);
            }
        } else if (marchTime <= WorldConstant.MARCH_UPPER_LIMIT_TIME.get(1)
                && marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) { // 大于分钟
            // 小于10分钟时
            if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                battleMarchTime = marchTime + WorldConstant.CITY_BATTLE_INCREASE_TIME;
            }
        }

        // 创建Battle
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_CITY);
        battle.setBattleType(battleType);
        battle.setBattleTime(now + battleMarchTime);
        battle.setDefencerId(targetPlayer.lord.getLordId());
        battle.setPos(pos);
        battle.setSponsor(invokePlayer);
        battle.setDefencer(targetPlayer);
        battle.setDefCamp(targetPlayer.lord.getCamp());
        battle.addAtkArm(param.getArmCount());
        int defArmCount = 0;
        for (PartnerHero partnerHero : targetPlayer.getPlayerFormation().getHeroBattle()) {
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            defArmCount += partnerHero.getPrincipalHero().getCount();
        }
        battle.addDefArm(defArmCount);
        battle.getAtkRoles().add(roleId);

        // 添加Battle
        BaseMapBattle baseBattle = BaseMapBattle.mapBattleFactory(battle);
        crossWorldMap.getMapWarData().addBattle(baseBattle);

        worldService.syncAttackRole(targetPlayer, invokePlayer.lord, battle.getBattleTime(),
                WorldConstant.ATTACK_ROLE_1);
        // 推送发送
        // PushMessageUtil.pushMessage(targetPlayer.account, PushConstant.ID_ATTACKED, targetPlayer.lord.getNick(),
        //         invokePlayer.lord.getNick());
        // 保护取消
        // worldService.removeProTect(invokePlayer);

        // 部队逻辑
        List<TwoInt> form = param.getHeroIdList().stream().map(heroId -> {
            Hero hero = invokePlayer.heros.get(heroId);
            return PbHelper.createTwoIntPb(heroId, hero.getCount());
        }).collect(Collectors.toList());

        int endTime = now + marchTime;
        Army army = new Army(invokePlayer.maxKey(), ArmyConstant.ARMY_TYPE_ATK_PLAYER, pos,
                ArmyConstant.ARMY_STATE_MARCH, form, marchTime, endTime - 1, invokePlayer.getDressUp());
        army.setBattleId(battle.getBattleId());
        army.setLordId(roleId);
        army.setTarLordId(targetPlayer.roleId);
        army.setBattleTime(battle.getBattleTime());
        army.setOriginPos(invokePlayer.lord.getPos());

        // 添加行军路线
        AttackPlayerArmy attackplayerArmy = new AttackPlayerArmy(army);
        attackplayerArmy.setArmyPlayerHeroState(crossWorldMap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        crossWorldMap.getMapMarchArmy().addArmy(attackplayerArmy);

        // 事件通知
        crossWorldMap.publishMapEvent(attackplayerArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(invokePlayer.lord.getPos(), MapCurdEvent.UPDATE),
                MapEvent.mapEntity(pos, MapCurdEvent.UPDATE));

        // 填充返回值
        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setBattle(PbHelper.createBattlePb(battle));
    }

    @Override
    public Builder toMapForcePb(CrossWorldMap cMap) {
        Builder builder = super.toMapForcePb(cMap);
        int now = TimeHelper.getCurrentSecond();
        Effect effect = player.getEffect().get(EffectConstant.PROTECT);
        int prot = (effect != null && effect.getEndTime() > now) ? 1 : 0;
        boolean hasBattleByPos = !CheckNull.isEmpty(cMap.getMapWarData().getBattlesByPos(pos));
        int protTime = effect != null ? effect.getEndTime() : 0;
        builder.setParam(player.building.getCommand());
        builder.setName(player.lord.getNick());
        builder.setCamp(player.lord.getCamp());
        builder.setBattle(hasBattleByPos);
        builder.setProt(prot);
        builder.setProtectTime(protTime);
        builder.setLordId(player.lord.getLordId());
        RebelService rebelService = DataResource.ac.getBean(RebelService.class); // 匪军叛乱
        builder.setRebelRoundId(rebelService.getRoundIdByPlayer(player));
        builder.setSeqId(player.getCurCastleSkin());

        // 召唤数据填充
        boolean showSummon = player.summon != null && player.summon.getStatus() != 0
                && now < player.summon.getLastTime() + Constant.SUMMON_KEEP_TIME
                && player.lord.getCamp() == player.lord.getCamp();
        if (showSummon && player.summon != null) {
            builder.setSummonCnt(player.summon.getRespondId().size());
            builder.setSummonSum(player.summon.getSum());
            builder.setSummonTime(player.summon.getLastTime() + Constant.SUMMON_KEEP_TIME);
        }
        builder.setCurrSkinStar(player.getCastleSkinStarById(player.getCurCastleSkin()));
        builder.setCurNamePlate(player.getDressUp().getCurNamePlate());
        builder.setTitleId(player.getDressUp().getCurTitle());
        return builder;
    }
}
