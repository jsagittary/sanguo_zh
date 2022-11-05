package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName FightLogic.java
 * @Description 战斗逻辑处理类
 * @date 创建时间：2017年3月31日 下午5:08:15
 */
public class FightLogic {
    public FightContextHolder contextHolder;

    // 战斗类型
    private int battleType;

    // 进攻方胜负 1.胜 2.负
    private int winState = 0;

    public FightLogic() {
    }

    public FightLogic(Fighter attacker, Fighter defender, boolean recordFlag, int battleType) {
        this.battleType = battleType;
        attacker.isAttacker = true;
        this.contextHolder = new FightContextHolder(attacker, defender);
        this.contextHolder.setRecordFlag(recordFlag);
        if (recordFlag) {
            contextHolder.setRecordData(CommonPb.Record.newBuilder().setKeyId(0));
        }

        // 记录勋章光环
        packAura();

        attacker.oppoFighter = defender;
        defender.oppoFighter = attacker;

        attacker.fightLogic = this;
        defender.fightLogic = this;
    }

    // 记录勋章光环
    private void packAura() {
        packAura(contextHolder.getAtkFighter());
        packAura(contextHolder.getDefFighter());
    }

    public FightLogic(Fighter attacker, Fighter defender, boolean recordFlag) {
        this(attacker, defender, recordFlag, 0);
        packForm();
    }

    /**
     * 记录双方阵型
     */
    public void packForm() {
        CommonPb.Record.Builder recordData = contextHolder.getRecordData();
        for (Force force : contextHolder.getAtkFighter().forces) {
            CommonPb.Form formA = createFormPb(force);
            recordData.addFormA(formA);
        }

        for (Force force : contextHolder.getDefFighter().forces) {
            CommonPb.Form formB = createFormPb(force);
            recordData.addFormB(formB);
        }
    }


    private CommonPb.Form createFormPb(Force force) {
        CommonPb.Form.Builder formPb = CommonPb.Form.newBuilder();
        formPb.setId(force.id);
        formPb.setCount(force.hp);
        formPb.setLine(force.maxLine);
        formPb.setCamp(force.getCamp());
        formPb.setHeroType(force.roleType);
        formPb.setCurLine(force.curLine);
        formPb.setIntensifyLv(force.getIntensifyLv() == 0 ? 1 : force.getIntensifyLv());
        return formPb.build();
    }

    private void packAura(Fighter fighter) {
        CommonPb.Record.Builder recordData = contextHolder.getRecordData();
        for (Map.Entry<Long, List<AuraInfo>> entry : fighter.auraInfos.entrySet()) {
            Long roleId = entry.getKey();
            for (AuraInfo auraInfo : entry.getValue()) {
                CommonPb.Aura auraPb = createAuraPb(roleId, auraInfo);
                recordData.addAura(auraPb);
            }
        }
    }

    private CommonPb.Aura createAuraPb(long roleId, AuraInfo auraInfo) {
        CommonPb.Aura.Builder auraPb = CommonPb.Aura.newBuilder();
        auraPb.setRoleId(roleId);
        auraPb.setHeroId(auraInfo.getHeroId());
        auraPb.setId(auraInfo.getMedalAuraId());
        auraPb.setNick(auraInfo.getNick());
        return auraPb.build();
    }

    public void start() {
        logForm();
        while (winState == 0) {
            round();
            checkAllNpcIsDied();
            checkDie();
        }
        if (Objects.nonNull(contextHolder.getRecordData())) {
            CommonPb.Record.Builder recordData = contextHolder.getRecordData();
            int size = recordData.build().toByteArray().length;
            if (size >= 1024 * 1024 || recordData.getRoundCount() > 1000) {//大于1M的战报,或者回合数超过1000的战报
                LogUtil.fight(String.format("battleType :%d, 战报大小 :%d 战斗回合数 :%d", battleType, size, recordData.getRoundCount()));
            }
        }
    }

    /**
     * 回合伊始
     */
    private void roundStart(Force force, Force target) {
        calFighterMorale(force, target);
        // 释放登场技能
        // 顺序为: 功方主将, 攻防副将, 守方主将, 守方副将
        releaseOnStageSkill(force, target);
        releaseOnStageSkill(target, force);
    }

    /**
     * 释放登场技能
     *
     * @param force
     * @param target
     */
    private void releaseOnStageSkill(Force force, Force target) {
        contextHolder.setAttacker(force);
        contextHolder.setDefender(target);

        // 主将释放登场技能
        force.actionId = force.id;
        List<SimpleHeroSkill> skillList = force.skillList.stream().filter(
                skill -> Objects.nonNull(skill) && skill.isOnStageSkill()).collect(Collectors.toList());
        if (!CheckNull.isEmpty(skillList)) {
            skillList.stream().forEach(skill -> skill.releaseSkill(contextHolder));
        }
        // 副将释放登场技能
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            force.assistantHeroList.forEach(ass -> {
                if (CheckNull.isNull(ass)) return;
                if (CheckNull.isEmpty(ass.getSkillList())) return;
                List<SimpleHeroSkill> skills = ass.getSkillList().stream().filter(
                        skill -> Objects.nonNull(skill) && skill.isOnStageSkill()).collect(Collectors.toList());
                if (CheckNull.isEmpty(skills)) return;
                force.actionId = ass.getHeroId();
                skills.forEach(skill -> skill.releaseSkill(contextHolder));
            });
        }
    }

    /**
     * 打印当前战斗双方阵容信息
     */
    private void logForm() {
        Fighter attacker = contextHolder.getAtkFighter();
        Fighter defender = contextHolder.getDefFighter();
        Optional.ofNullable(attacker).ifPresent(atk -> {
            Optional.of(atk.getForces()).ifPresent(forces -> {
                forces.forEach(force -> {
                    if (CheckNull.isNull(force)) {
                        return;
                    }
                    LogUtil.fight("attacker force: ", force.toBattleString());
                });
            });
        });
        Optional.ofNullable(defender).ifPresent(atk -> {
            Optional.of(atk.getForces()).ifPresent(forces -> {
                forces.forEach(force -> {
                    if (CheckNull.isNull(force)) {
                        return;
                    }
                    LogUtil.fight("defender force: ", force.toBattleString());
                });
            });
        });
    }

    /**
     * 回合伊始, 重置当前对阵的武将士气值
     *
     * @param force
     * @param target
     */
    private void calFighterMorale(Force force, Force target) {
        force.morale = force.hp * 2;
        target.morale = force.hp * 2;
    }

    /**
     * 回合战斗
     */
    private void round() {
        if (contextHolder.isRecordFlag()) {
            // TODO pb初始化

        }
        // 找出攻击方与防守方
        Force force = contextHolder.getAtkFighter().getAliveForce();
        Force target = contextHolder.getDefFighter().getAliveForce();
        if (force != null && target != null) {
            // 回合开始时
            roundStart(force, target);
            // 双方武将以及副将战斗
            fight(force, target);
        }

        if (contextHolder.isRecordFlag()) {
        }
    }

    /**
     * 双方武将战斗
     *
     * @param force
     * @param target
     */
    private void fight(Force force, Force target) {
        while (!force.alive() || !target.alive()) {
            // 比较武将速度, 排列出场顺序
            List<FightEntity> fightEntityList = contextHolder.getSortedFightEntity(force, target);
            if (CheckNull.isEmpty(fightEntityList)) {
                LogUtil.fight("出战顺序列表为空? 检查, forceId: ", force.ownerId, ", targetId: ", target.ownerId);
                break;
            }

            List<Integer> heroList = new ArrayList<>();
            for (FightEntity fe : fightEntityList) {
                Force atk = fe.getOwnId() == force.ownerId ? force : target;
                Force def = atk.ownerId == force.ownerId ? target : force;
                atk.actionId = fe.getHeroId();
                contextHolder.setAttacker(atk);
                contextHolder.setDefender(def);
                // 释放技能
                List<SimpleHeroSkill> skillList = atk.getSkillList(fe.getHeroId()).stream().filter(skill -> Objects.nonNull(skill) && !skill.isOnStageSkill() &&
                        skill.getCurEnergy() >= skill.getS_skill().getReleaseNeedEnergy()).collect(Collectors.toList());
                if (!CheckNull.isEmpty(skillList)) {
                    skillList.forEach(skill -> skill.releaseSkill(contextHolder));
                }

                // 普攻
                def.beActionId.clear();
                heroList.add(def.id);
                if (!CheckNull.isEmpty(def.assistantHeroList)) {
                    def.assistantHeroList.forEach(ass -> heroList.add(ass.getHeroId()));
                }
                def.beActionId.add(heroList.get(RandomHelper.randomInSize(heroList.size())));

            }
        }
    }

    /**
     * 创建Action对象
     *
     * @param force
     * @param target
     * @return
     */
    private CommonPb.Action.Builder createAction(Force force, Force target, boolean isAtk) {
        actionDataA = CommonPb.Action.newBuilder();
        actionDataA.setTarget(target.id);
        actionDataA.setTargetRoleId(target.ownerId);
        actionDataB = CommonPb.Action.newBuilder();
        actionDataB.setTarget(force.id);
        actionDataB.setTargetRoleId(force.ownerId);
        return isAtk ? actionDataA : actionDataB;
    }


//    /**
//     * 单次攻击行动  【战斗2--用于区分程序走向】
//     *
//     * @param force     攻击方
//     * @param target    防守方
//     * @param isAttcker 本次攻击方是否是本场战斗的进攻方
//     */
//    private void action(Force force, Force target, boolean isAttcker) {
//        if (contextHolder.isRecordFlag()) {
//            if (isAttcker) {
//                actionDataA = CommonPb.Action.newBuilder();
//                actionDataA.setTarget(target.id);
//                actionDataA.setTargetRoleId(target.ownerId);
//            } else {
//                actionDataB = CommonPb.Action.newBuilder();
//                actionDataB.setTarget(target.id);
//                actionDataB.setTargetRoleId(target.ownerId);
//            }
//        }
//        if (!force.hasFight) {
//            force.hasFight = true;
//        }
//        if (!target.hasFight) {
//            target.hasFight = true;
//        }
//        attack(force, target, isAttcker);
//    }

//    /**
//     * 单次攻击计算  【战斗3--用于区分程序走向】
//     *
//     * @param force      攻击方
//     * @param target     防御方
//     * @param isAttacker 本次攻击计算的攻击方是否为战斗的进攻方
//     */
//    private void attack(Force force, Force target, boolean isAttacker) {
//        CommonPb.Action.Builder actionData;
//        if (isAttacker) {
//            actionData = actionDataA;
//        } else {
//            actionData = actionDataB;
//        }
//
//        if (isDodge(force, target, actionData, isAttacker)) // 闪避
//            return;
//
//        float crit = isCrit(force, target, actionData);
//        int hurt = FightCalc.calcHurt2(force, target, crit, battleType);
//        // 预扣血，并返回真实伤害，真实扣血逻辑在回合结束是执行
//        hurt = hurt(target, force, hurt, crit);
//        force.killed += hurt;// 记录攻击方击杀数
//        force.fighter.hurt += hurt;// 记录总击杀数
//        target.fighter.lost += hurt;// 记录总伤兵数
//
//        if (recordFlag) {
//            actionData.setHurt(hurt);
//            actionData.setCount(target.getSurplusCount());
//            actionData.setDeadLine(target.getDeadLine());
//        }
//    }


    public static boolean checkPvp(Force force, Force targetForce) {
        if (Objects.isNull(force) || Objects.isNull(targetForce)) {
            return false;
        }

        return force.roleType == Constant.Role.PLAYER && targetForce.roleType == Constant.Role.PLAYER;
    }
//
//    /**
//     * //天赋优化 战斗增益
//     *
//     * @param force  攻击方
//     * @param target 防守方
//     * @param hurt
//     */
//    public static int seasonTalentBuff(Force force, Force target, int hurt, int battleType) {
//        if (battleType == Integer.MIN_VALUE) {
//            //以免重复计算赛季天赋效果
//            return hurt;
//        }
//
//        //天赋优化 战斗增益
//        double hurt_ = hurt;
//        if (FightLogic.checkPvp(force, target)) {
//            double debugHurt;
//            Player forcePlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
//            Player targetPlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(target.ownerId);
//            if (!CheckNull.isNull(forcePlayer)) {
//                //伤害加成
//                debugHurt = hurt;
//                hurt_ *= (1 + (DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentEffectValue(forcePlayer, SeasonConst.TALENT_EFFECT_607) / Constant.TEN_THROUSAND));
//                LogUtil.fight("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
//                        "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "赛季天赋-攻其不备: ", hurt_ - debugHurt, ", 伤害结果:", hurt_);
//            }
//            if (!CheckNull.isNull(targetPlayer)) {
//                //伤害减免
//                debugHurt = hurt_;
//                hurt_ *= (1 - (DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentEffectValue(targetPlayer, SeasonConst.TALENT_EFFECT_611) / Constant.TEN_THROUSAND));
//                LogUtil.fight("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
//                        "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "赛季天赋-随形卸力: ", hurt_ - debugHurt, ", 伤害结果:", hurt_);
//            }
//        }
//
//        return (int) hurt_;
//    }

    public static boolean isCityBattle(int battleType) {
        return battleType == WorldConstant.BATTLE_TYPE_CITY;
    }

    /**
     * 阵亡的将领去掉光环效果
     *
     * @param force
     * @param target
     */
    private void subAuraSkill(Force force, Force target) {
        // 双方都存活
        if (force.alive() && target.alive()) {
            return;
        }
        // 阵亡的将领去掉光环效果
        target.fighter.subAuraSkill(target);
        force.fighter.subAuraSkill(force);
    }


    /**
     * 检查战斗双方是否有已经死亡的，并更新战斗状态
     */
    private void checkDie() {
        boolean attackerAlive = false;
        boolean defenderAlive = false;
        for (Force force : contextHolder.getAtkFighter().forces) {
            if (force != null) {
                if (force.morale <= 0)
                    continue;
                if (force.alive()) {
                    attackerAlive = true;
                }
            }
        }

        for (Force force : contextHolder.getDefFighter().forces) {
            if (force != null) {
                if (force.morale <= 0)
                    continue;
                if (force.alive()) {
                    defenderAlive = true;
                }
            }
        }

        if (attackerAlive && defenderAlive) {
            this.winState = FightConstant.FIGHT_RESULT_DRAW;
        } else {
            this.winState = attackerAlive ? FightConstant.FIGHT_RESULT_SUCCESS : FightConstant.FIGHT_RESULT_FAIL;
        }
    }

    /**
     * 获取最后一回合
     *
     * @return
     */
    public CommonPb.Round getLastRound() {
        CommonPb.Round round = null;
        CommonPb.Record.Builder recordData = contextHolder.getRecordData();
        if (!CheckNull.isNull(recordData)) {
            List<CommonPb.Round> roundList = recordData.getRoundList();
            if (!CheckNull.isEmpty(roundList)) {
                round = roundList.get(roundList.size() - 1);
            }
        }
        return round;
    }

    public int getWinState() {
        return winState;
    }


    /**
     * 伤害计算通用接口
     *
     * @param attacker
     * @param defender
     * @param damage
     */
    public void hurt(Force attacker, Force defender, int damage) {

    }
}
