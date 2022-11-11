package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        CommonPb.Record.Builder recordData = contextHolder.getRecordData();
        for (Force force : contextHolder.getAtkFighter().forces) {
            CommonPb.Form formA = FightUtil.createFormPb(force);
            recordData.addFormA(formA);
        }

        for (Force force : contextHolder.getDefFighter().forces) {
            CommonPb.Form formB = FightUtil.createFormPb(force);
            recordData.addFormB(formB);
        }
    }

    public void start() {
        logForm();
        contextHolder.getBattleLogic().battleStart(contextHolder);
        while (winState == -1) {
            round();
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
        contextHolder.getActionDirection().setAtk(force);
        contextHolder.getActionDirection().setDef(target);
        contextHolder.setCurAtkHeroId(force.id);
        // 主将释放登场技能
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
                contextHolder.setCurAtkHeroId(ass.getHeroId());
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
        force.maxRoundMorale = force.morale;
        target.maxRoundMorale = target.morale;

        LogUtil.fight("============= 回合开始, 进攻方: force: ", force.ownerId, ", 防守方: target: ",
                target.ownerId, ", 恢复并记录最大士气, 进攻方: ", force.maxRoundMorale, ", 防守方: ", target.maxRoundMorale, " =============");
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
            // 当前回合结束处理
            contextHolder.getBattleLogic().nextRoundBefore(force, target, contextHolder);
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
        while (force.alive() && target.alive()) {
            // 比较武将速度, 排列出场顺序
            List<FightEntity> fightEntityList = contextHolder.getSortedFightEntity(force, target);
            if (CheckNull.isEmpty(fightEntityList)) {
                LogUtil.fight("出战顺序列表为空? 检查, forceId: ", force.ownerId, ", targetId: ", target.ownerId);
                break;
            }

            // 结算buff
            contextHolder.getBattleLogic().settlementBuff(force, contextHolder);
            contextHolder.getBattleLogic().settlementBuff(target, contextHolder);
            // 主动buff触发
            FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.ROUND_START);

            List<Integer> heroList = new ArrayList<>();
            for (FightEntity fe : fightEntityList) {
                Force atk = fe.getOwnId() == force.ownerId ? force : target;
                Force def = atk.ownerId == force.ownerId ? target : force;
                // 释放技能
                contextHolder.getBattleLogic().releaseSkill(atk, def, fe, fe.getHeroId(), contextHolder);
                // 普攻
                contextHolder.getBattleLogic().ordinaryAttack(atk, def, fe.getHeroId(), heroList, this.battleType, contextHolder);
            }

            // 修正士气
            contextHolder.getBattleLogic().roundMoraleDeduction(force, target);
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
            this.winState = FightConstant.FIGHT_RESULT_DRAW;
            LogUtil.fight("**********************战斗结束**********************, 双方战成平局");
            return;
        }

        if (!attackerAlive || !defenderAlive) {
            this.winState = attackerAlive ? FightConstant.FIGHT_RESULT_SUCCESS : FightConstant.FIGHT_RESULT_FAIL;
            LogUtil.fight("**********************战斗结束**********************, 获胜方: ",
                    this.winState == FightConstant.FIGHT_RESULT_SUCCESS ? "进攻方胜" : "防守方胜");
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

    public CommonPb.Record generateRecord() {
        return CommonPb.Record.newBuilder().setKeyId(0).build();
    }

    public CommonPb.Record.Builder getRecordBuild() {
        return null;
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
