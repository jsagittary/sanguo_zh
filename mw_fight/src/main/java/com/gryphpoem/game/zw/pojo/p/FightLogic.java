package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangpeng
 * @ClassName FightLogic.java
 * @Description
 * @date 创建时间：2022年11月5日 下午18:43:00
 */
public class FightLogic {
    public FightContextHolder contextHolder;

    public long fightId;
    // 战斗类型
    private int battleType;

    // 进攻方胜负 1.胜 2.负
    private int winState = -1;

    public FightLogic() {
    }

    public FightLogic(Fighter attacker, Fighter defender, boolean recordFlag, int battleType) {
        this.fightId = FightUtil.uniqueId();
        this.battleType = battleType;
        attacker.isAttacker = true;
        this.contextHolder = new FightContextHolder(attacker, defender, battleType);
        if (recordFlag) {
            contextHolder.setRecordData(BattlePb.BattleRoundPb.newBuilder().setKeyId(String.valueOf(fightId)));
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
        FightUtil.packAura(contextHolder.getAtkFighter(), contextHolder);
        FightUtil.packAura(contextHolder.getDefFighter(), contextHolder);
    }

    public FightLogic(Fighter attacker, Fighter defender, boolean recordFlag) {
        this(attacker, defender, recordFlag, 0);
        packForm();
    }

    /**
     * 记录双方阵型
     */
    public void packForm() {
//        CommonPb.Record.Builder recordData = contextHolder.getRecordData();
//        for (Force force : contextHolder.getAtkFighter().forces) {
//            CommonPb.Form formA = FightUtil.createFormPb(force);
//            recordData.addFormA(formA);
//        }
//
//        for (Force force : contextHolder.getDefFighter().forces) {
//            CommonPb.Form formB = FightUtil.createFormPb(force);
//            recordData.addFormB(formB);
//        }
    }

    public void start() {
        logForm();
        contextHolder.getInitRecordData().setKeyId(String.valueOf(this.fightId));
        while (winState == -1) {
            round();
            checkDie();
        }
        if (Objects.nonNull(contextHolder.getRecordData())) {
            BattlePb.BattleRoundPb.Builder recordData = contextHolder.getRecordData();
            int size = recordData.build().toByteArray().length;
            if (size >= 1024 * 1024 || contextHolder.getRoundNum() > 1000) {//大于1M的战报,或者回合数超过1000的战报
                LogUtil.fight(String.format("battleType :%d, 战报大小 :%d 战斗回合数 :%d", battleType, size, contextHolder.getRoundNum()));
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
        contextHolder.getActionDirection().setAtk(force);
        contextHolder.getActionDirection().setDef(target);
        contextHolder.setCurAtkHeroId(force.id);
        // 主将释放登场技能
        contextHolder.getInitSkillActionPb();
        List<SimpleHeroSkill> skillList = force.skillList.stream().filter(
                skill -> Objects.nonNull(skill) && skill.isOnStageSkill()).collect(Collectors.toList());
        if (!CheckNull.isEmpty(skillList)) {
            skillList.stream().forEach(skill -> {
                contextHolder.getActionDirection().setSkill(skill);
                skill.releaseSkill(contextHolder);
                contextHolder.clearCurSkillActionPb();
            });
        }
        // 副将释放登场技能
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            force.assistantHeroList.forEach(ass -> {
                if (CheckNull.isNull(ass)) return;
                if (CheckNull.isEmpty(ass.getSkillList())) return;
                List<SimpleHeroSkill> skills = ass.getSkillList().stream().filter(
                        skill -> Objects.nonNull(skill) && skill.isOnStageSkill()).collect(Collectors.toList());
                if (CheckNull.isEmpty(skills)) return;
                contextHolder.setCurAtkHeroId(ass.getHeroId());
                skills.forEach(skill -> {
                    contextHolder.getActionDirection().setSkill(skill);
                    skill.releaseSkill(contextHolder);
                    contextHolder.clearCurSkillActionPb();
                });
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
//        force.morale = force.hp * 2;
//        target.morale = force.hp * 2;
//        force.maxRoundMorale = force.morale;
//        target.maxRoundMorale = target.morale;
//
//        LogUtil.fight("============= 回合开始, 进攻方: force: ", force.ownerId, ", 防守方: target: ",
//                target.ownerId, ", 恢复并记录最大士气, 进攻方: ", force.maxRoundMorale, ", 防守方: ", target.maxRoundMorale, " =============");
    }

    /**
     * 回合战斗
     */
    private void round() {
        // 找出攻击方与防守方
        Force force = contextHolder.getAtkFighter().getAliveForce();
        Force target = contextHolder.getDefFighter().getAliveForce();
        if (force != null && target != null) {
            // 初始化战斗信息
            contextHolder.getInitBothBattleEntityPb();

            // 添加开场pb
            contextHolder.getInitPreparationStagePb(force, target);
            // 回合开始时
            roundStart(force, target);
            // 添加开场阶段pb到回合字段中
            contextHolder.getCurBothBattleEntityPb().addStage(FightPbUtil.createBaseBattleStagePb(
                    BattlePb.BattleStageDefine.PREPARATION_STAGE_VALUE, BattlePb.BattlePreparationStage.round, contextHolder.getCurPreparationStagePb().build()
            ));
            // 双方武将以及副将战斗
            fight(force, target);
            // 当前回合结束处理
            contextHolder.getBattleLogic().nextRoundBefore(force, target, contextHolder);

            // 添加战斗结束pb到双方武将战报中
            BattlePb.BattleEndStage.Builder battleEndPb = BattlePb.BattleEndStage.newBuilder();
            battleEndPb.setRemainArms(FightPbUtil.createDataInt(force.hp, target.hp));
            contextHolder.getCurBothBattleEntityPb().addStage(FightPbUtil.createBaseBattleStagePb(BattlePb.BattleStageDefine.ENDING_VALUE,
                    BattlePb.BattleEndStage.round, battleEndPb.build()));

            // 填充双方武将战斗结算数值到pb中
            FightPbUtil.paddingBattleEntity(force, target, contextHolder.getCurBothBattleEntityPb());

            // 将双方武将战斗情况加到总战报中
            contextHolder.getRecordData().addBothForce(contextHolder.getCurBothBattleEntityPb().build());
        }

    }

    /**
     * 双方武将战斗
     *
     * @param force
     * @param target
     */
    private void fight(Force force, Force target) {
        while (force.alive() && target.alive()) {
            // 比较武将速度, 排列出场顺序
            List<FightEntity> fightEntityList = contextHolder.getSortedFightEntity(force, target);
            if (CheckNull.isEmpty(fightEntityList)) {
                LogUtil.fight("出战顺序列表为空? 检查, forceId: ", force.ownerId, ", targetId: ", target.ownerId);
                break;
            }

            contextHolder.addRoundNum();

            contextHolder.getInitBattleRoundStagePb().setRoundNum(contextHolder.getRoundNum());
            List<Integer> heroList = new ArrayList<>();
            for (FightEntity fe : fightEntityList) {
                Force atk = fe.getOwnId() == force.ownerId ? force : target;
                Force def = atk.ownerId == force.ownerId ? target : force;

                // 初始化roundActionPb
                contextHolder.getInitRoundActionPb().setActionId(FightPbUtil.getActingSize(atk, fe.getHeroId()));
                LinkedList<IFightBuff> buffList = atk.buffList(fe.getHeroId());
                // 结算行动方buff, 清除次数为0的buff
                contextHolder.getBattleLogic().settlementBuff(buffList, contextHolder);
                // 释放主动buff
                contextHolder.getBattleLogic().releaseSingleBuff(buffList, contextHolder, FightConstant.BuffEffectTiming.ROUND_START);
                // 所有buff作用次数少一次
                contextHolder.getBattleLogic().deductBuffRounds(buffList);
                // 指定回合触发
                contextHolder.getBattleLogic().releaseSingleBuff(buffList, contextHolder, FightConstant.BuffEffectTiming.START_OF_DESIGNATED_ROUND);

                // 释放技能
                contextHolder.getInitSkillActionPb();
                contextHolder.getBattleLogic().releaseSkill(atk, def, fe, fe.getHeroId(), contextHolder);
                contextHolder.clearCurSkillActionPb();

                // 清除没有作用次数的buff
                contextHolder.getBattleLogic().clearBothForceBuffEffectTimes(force, target, contextHolder);

                // 普攻
                contextHolder.getInitAttackActionPb();
                contextHolder.getBattleLogic().ordinaryAttack(atk, def, fe.getHeroId(), heroList, this.battleType, contextHolder);
                contextHolder.getRoundActionPb().setAttack(contextHolder.getCurAttackActionPb().build());
                atk.attackCount++;
                contextHolder.clearCurAttackActionPb();

                // 清除没有作用次数的buff
                contextHolder.getBattleLogic().clearBothForceBuffEffectTimes(force, target, contextHolder);

                // 将此次战斗添加到回合阶段pb中
                contextHolder.getBattleRoundStagePb().addAction(contextHolder.getRoundActionPb().build());
            }

            // 将当前回合数据添加到回合数据中
            contextHolder.getCurBothBattleEntityPb().addStage(FightPbUtil.createBaseBattleStagePb(BattlePb.BattleStageDefine.ROUNDS_VALUE,
                    BattlePb.BattleRoundStage.round, contextHolder.getBattleRoundStagePb().build()));
            // 结算损兵添加到双方武将战斗中
            BattlePb.BattleSettlementLossStage.Builder lossStagePb = BattlePb.BattleSettlementLossStage.newBuilder();
            lossStagePb.addLost(FightPbUtil.createDataInt(force.curLine, force.roundLost));
            lossStagePb.addLost(FightPbUtil.createDataInt(target.curLine, target.roundLost));
            contextHolder.getCurBothBattleEntityPb().addStage(FightPbUtil.createBaseBattleStagePb(BattlePb.BattleStageDefine.SETTLEMENT_LOSS_VALUE,
                    BattlePb.BattleSettlementLossStage.round, lossStagePb.build()));
            force.roundLost = 0;
            target.roundLost = 0;

            // 双方武将都存活时
            if (force.alive() && target.alive()) {
                BattlePb.BattleRegroupStage.Builder regroupPb = BattlePb.BattleRegroupStage.newBuilder();
                if (force.switchPlatoon()) {
                    regroupPb.addBackToBattle(FightPbUtil.create3DataInt(FightConstant.ForceSide.ATTACKER, force.curLine, force.count));
                }
                if (target.switchPlatoon()) {
                    regroupPb.addBackToBattle(FightPbUtil.create3DataInt(FightConstant.ForceSide.DEFENDER, target.curLine, target.count));
                }
                if (regroupPb.getBackToBattleList().size() > 0) {
                    contextHolder.getCurBothBattleEntityPb().addStage(FightPbUtil.createBaseBattleStagePb(BattlePb.BattleStageDefine.REGROUP_VALUE,
                            BattlePb.BattleRegroupStage.round, regroupPb.build()));
                }
            }
        }
    }

    /**
     * 检查战斗双方是否有已经死亡的，并更新战斗状态
     */
    private void checkDie() {
        boolean attackerAlive = false;
        boolean defenderAlive = false;
        for (Force force : contextHolder.getAtkFighter().forces) {
            if (force != null) {
                if (force.alive()) {
                    attackerAlive = true;
                }
            }
        }

        for (Force force : contextHolder.getDefFighter().forces) {
            if (force != null) {
                if (force.alive()) {
                    defenderAlive = true;
                }
            }
        }

        if (!attackerAlive && !defenderAlive) {
            this.winState = FightConstant.FIGHT_RESULT_SUCCESS;
            LogUtil.fight("**********************战斗结束**********************, 双方战成平局, 进攻方胜");
            return;
        }

        if (!attackerAlive || !defenderAlive) {
            this.winState = attackerAlive ? FightConstant.FIGHT_RESULT_SUCCESS : FightConstant.FIGHT_RESULT_FAIL;
            LogUtil.fight("**********************战斗结束**********************, 获胜方: ",
                    this.winState == FightConstant.FIGHT_RESULT_SUCCESS ? "进攻方胜" : "防守方胜");
        }
    }

    public int getWinState() {
        return winState;
    }

    public BattlePb.BattleRoundPb generateRecord() {
        return contextHolder.getRecordData().build();
    }

    public BattlePb.BattleRoundPb.Builder getRecordBuild() {
        return contextHolder.getRecordData();
    }

    public int getBattleType() {
        return battleType;
    }

    public Fighter getAttacker() {
        return contextHolder.getAtkFighter();
    }

    public Fighter getDefender() {
        return contextHolder.getDefFighter();
    }

    public int getAttrChangeState() {
        return 0;
    }

    public void setCareDodge(boolean careDodge) {
    }

    public void setCareCrit(boolean careCrit) {
    }
}
