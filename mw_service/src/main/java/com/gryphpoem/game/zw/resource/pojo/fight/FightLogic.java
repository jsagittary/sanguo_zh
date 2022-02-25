package com.gryphpoem.game.zw.resource.pojo.fight;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkill;
import com.gryphpoem.game.zw.resource.pojo.fight.skill.FightActionDto;
import com.gryphpoem.game.zw.resource.pojo.fight.skill.FightSkillAction;
import com.gryphpoem.game.zw.resource.pojo.fight.skill.FightSkillUtils;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.apache.zookeeper.Op;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author TanDonghai
 * @ClassName FightLogic.java
 * @Description 战斗逻辑处理类
 * @date 创建时间：2017年3月31日 下午5:08:15
 */
public class FightLogic {
    public boolean recordFlag = true;
    private Fighter attacker;
    private Fighter defender;
    private static final int MAX_LOOP_CNT = 10000;// 最大回合次数
    private int loopCounter = MAX_LOOP_CNT;
    private int battleType;

    // 进攻方胜负 1.胜 2.负
    private int winState = 0;

    private CommonPb.Record.Builder recordData;// 总战报
    private CommonPb.Round.Builder roundData;
    public CommonPb.Action.Builder actionDataA;
    public CommonPb.Action.Builder actionDataB;
    public List<CommonPb.FightBuff> buffs;
    private boolean careDodge = true;// 是否考虑闪避 默认考虑
    private boolean careCrit = true;// 是否考虑暴击

    /** 根据战斗结果判断是否可以改变属性 （世界争霸城池专用） 1 不满足条件，2 满足条件 */
    private int AttrChangeState = 0;

    public FightLogic() {
    }

    public FightLogic(Fighter attacker, Fighter defender, boolean recordFlag, int battleType) {
        this.attacker = attacker;
        this.defender = defender;
        this.battleType = battleType;
        attacker.isAttacker = true;
        this.recordFlag = recordFlag;
        if (recordFlag) {
            recordData = CommonPb.Record.newBuilder();
            recordData.setKeyId(0);
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
        packAura(attacker);
        packAura(defender);
    }

    public FightLogic(Fighter attacker, Fighter defender, boolean recordFlag) {
        this(attacker, defender, recordFlag, 0);
        packForm();
    }

    /**
     * 记录双方阵型
     */
    public void packForm() {
        for (Force force : attacker.forces) {
            CommonPb.Form formA = createFormPb(force);
            recordData.addFormA(formA);
        }

        for (Force force : defender.forces) {
            CommonPb.Form formB = createFormPb(force);
            recordData.addFormB(formB);
        }
    }

    private CommonPb.Form createFormPb(Force force){
        CommonPb.Form.Builder formPb = CommonPb.Form.newBuilder();
        formPb.setId(force.id);
        formPb.setCount(force.hp);
        formPb.setLine(force.maxLine);
        formPb.setCamp(force.getCamp());
        formPb.setHeroType(force.roleType);
        formPb.setCurLine(force.curLine);
        formPb.setIntensifyLv(force.getIntensifyLv() == 0 ? 1 : force.getIntensifyLv());
        // formPb.setOwnerId(force.ownerId);
        return formPb.build();
    }

    private void packAura(Fighter fighter) {
        for (Map.Entry<Long, List<AuraInfo>> entry : fighter.auraInfos.entrySet()) {
            Long roleId = entry.getKey();
            for (AuraInfo auraInfo : entry.getValue()) {
                CommonPb.Aura auraPb = createAuraPb(roleId, auraInfo);
                recordData.addAura(auraPb);
            }
        }
    }
    private CommonPb.Aura createAuraPb(long roleId, AuraInfo auraInfo){
        CommonPb.Aura.Builder auraPb = CommonPb.Aura.newBuilder();
        auraPb.setRoleId(roleId);
        auraPb.setHeroId(auraInfo.getHeroId());
        auraPb.setId(auraInfo.getMedalAuraId());
        auraPb.setNick(auraInfo.getNick());
        return auraPb.build();
    }

    public void fight() {
        logForm();
        while (loopCounter-- > 0 && winState == 0) {
            round();
            checkAllNpcIsDied();
            checkDie();
        }
        if (Objects.nonNull(recordData)) {
            int size = recordData.build().toByteArray().length;
            if (size >= 1024 * 1024 || recordData.getRoundCount() > 1000) {//大于1M的战报,或者回合数超过1000的战报
                LogUtil.debug(String.format("battleType :%d, 战报大小 :%d 战斗回合数 :%d", battleType, size, recordData.getRoundCount()));
            }
        }
    }

    /**
     * 打印当前战斗双方阵容信息
     *
     */
    private void logForm() {
        Optional.ofNullable(attacker).ifPresent(atk -> {
            Optional.of(atk.getForces()).ifPresent(forces -> {
                forces.forEach(force -> {
                    if (CheckNull.isNull(force)) {
                        return;
                    }
                    LogUtil.debug("attacker force: ", force.toBattleString());
                });
            });
        });
        Optional.ofNullable(defender).ifPresent(atk -> {
            Optional.of(atk.getForces()).ifPresent(forces -> {
                forces.forEach(force -> {
                    if (CheckNull.isNull(force)) {
                        return;
                    }
                    LogUtil.debug("defender force: ", force.toBattleString());
                });
            });
        });
    }

    /** 检测所有的npc是否死亡(进攻npc一方损兵不为0的指挥官小于10人) */
    private void checkAllNpcIsDied(){
        if(this.battleType == WorldConstant.BATTLE_TYPE_CAMP && this.getAttrChangeState() == ArmyConstant.ATTR_CHANGE_STATE_NO_DO){
            if (this.defender.forces.stream()
                    .filter(f -> f.roleType == Constant.Role.CITY)
                    .filter(f -> f.alive()).count() == 0
            ){
                if(this.attacker.forces.stream().filter(f -> f.totalLost > 0)
                        .map(f -> f.ownerId).distinct().count() < ArmyConstant.ATTR_CHANGE_STATE_LORD_COUNT){
                    this.setAttrChangeState(ArmyConstant.ATTR_CHANGE_STATE_YES);
                }else{
                    this.setAttrChangeState(ArmyConstant.ATTR_CHANGE_STATE_NO);
                }
            }
        }
    }

    /**
     * 一轮攻击 【战斗1--用于区分程序走向】
     */
    private void round() {
        if (recordFlag) {
            roundData = CommonPb.Round.newBuilder();
            buffs = new ArrayList<>();
        }
        Force force = attacker.getAliveForce();
        Force target = defender.getAliveForce();
        if (force != null && target != null) {
            FightSkill forceSkill = force.getCurrentSkill0();
            FightSkill targetSkill = target.getCurrentSkill0();
            //无论什么英雄，进攻方先手放技能
            if (force.planeHasSkill() || force.hasSkill()) {//进攻方非赛季英雄技能
                skillAction(force, target, true);
            } else if (!CheckNull.isNull(forceSkill) ) {//进攻方赛季英雄技能
                FightActionDto dto = new FightActionDto(this, force, target, FightActionDto.Direction.ACTION_ATK);
                FightSkillUtils.releaseSkill((FightSkillAction) forceSkill, dto);
//                forceSkill.releaseSkill(force, target, this, true);
            }else if (target.planeHasSkill() || target.hasSkill()) {//防守方非赛季英雄技能
                skillAction(target, force, false);
            }  else if (!CheckNull.isNull(targetSkill)) {  // 防守方赛季英雄技能
                FightActionDto dto = new FightActionDto(this, target, force, FightActionDto.Direction.ACTION_DEF);
                FightSkillUtils.releaseSkill((FightSkillAction) targetSkill, dto);
//                targetSkill.releaseSkill(target, force, this, false);
            } else {
                // 普通的砍
                action(force, target, true);
                action(target, force, false);
            }
            roundData.setActionA(this.actionDataA);
            roundData.setActionB(this.actionDataB);
            // 一轮战斗结束后，执行扣血减兵操作, 扣除buff的兵排
            if (force.subHp(target) && dealLineSubBuff(force)) {
                // 记录击杀排数
                target.fighter.addKillLine(target);
            }
            if (target.subHp(force) && dealLineSubBuff(target)) {
                // 记录击杀排数
                force.fighter.addKillLine(force);
            }

            clearDeadBuff(force);
            clearDeadBuff(target);
            subAuraSkill(force, target);
            roundData.addAllBuff(buffs);
        }

// <editor-fold desc="备注：技能增加buff修改" defaultstate="collapsed">
        //现在释放技能增加的buff放在Round的buff字段中，前端表现要求先释放完技能再加buff
        //自带buff是进场就显示，而技能buff是要求释放完技能后再显示，需要将这两种buff区分处理数据，否则表现上就有问题
// </editor-fold>

        if (recordFlag) {
            recordData.addRound(roundData);
        }
    }

    /**
     * 英雄死亡，清除buff信息
     *
     * @param force
     */
    private void clearDeadBuff(Force force) {
        if (CheckNull.isNull(force))
            return;

        //英雄不存活，清除buff, 应某石姓策划要求只处理赛季英雄
        if (!force.alive() && !ObjectUtils.isEmpty(force.fightBuff)) {
            force.fightBuff.values().forEach(fightBuff -> {
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(fightBuff.getHeroId());
                if (CheckNull.isNull(staticHero) || !staticHero.isSeasonHero()) {
                    return;
                }

                if (fightBuff.clearBuff()) {
                    CommonPb.FightBuff.Builder builder = fightBuff.createFightBuffpb();
                    buffs.add(builder.build());
                }
            });
        }
    }

    /**
     * 死亡兵排后的处理
     *
     * @param force
     * @return
     */
    private boolean dealLineSubBuff(Force force) {
        force.fightBuff.values().stream().filter(buff -> buff.getEffectRow() > 0 || buff.getContinueNum() > 0).forEach(buff -> {
            if (buff.subEffectRow()) {
                CommonPb.FightBuff.Builder builder = buff.createFightBuffpb();
                builder.addParam(PbHelper.createTwoIntPb(CommonPb.FightBuffParam.FIRST_RELEASE_VALUE, 2)); // 重置
                buffs.add(builder.build());
            } else {
                CommonPb.FightBuff.Builder builder = buff.createFightBuffpb();
                buffs.add(builder.build());
            }
        });

        return true;
    }

    /**
     * 技能触发计算 【技能攻击2--用于区分程序走向】
     *
     * @param force
     * @param target
     * @param actionData
     */
    private void skillAttck(Force force, Force target, CommonPb.Action.Builder actionData) {
        int skillId = force.skillId;
        StaticSkill skill = StaticHeroDataMgr.getSkillMapById(skillId);
        int val = skill == null || skill.getVal() <= 0 ? 1 : skill.getVal();
        int hurt = (int) Math.ceil(force.maxHp * 1.0 / val);// 技能计算公式
        hurt = target.hurt(hurt, force, battleType);
        force.killed += hurt;// 记录攻击方击杀数
        force.fighter.hurt += hurt;// 记录总击杀数
        target.fighter.lost += hurt;// 记录总伤兵数
        if (recordFlag) {
            actionData.setHurt(hurt);
            actionData.setCount(target.getSurplusCount());
            actionData.setDeadLine(target.getDeadLine());
            actionData.setSkillId(skillId);
        }
    }

    /**
     * 单次技能攻击的行动 【技能攻击1--用于区分程序走向】
     *
     * @param force     本次技能释放的攻击方
     * @param target    本次技能释放的防守方
     * @param isAttacker 本次攻击方是否是本场战斗的进攻方
     */
    private void skillAction(Force force, Force target, boolean isAttacker) {
        CommonPb.Action.Builder actionData;
        if (isAttacker) {
            actionData = createAction(force, target, true);
        } else {
            actionData = createAction(target, force, false);
        }
        PlaneInfo info = force.getPlaneInfos().get(1);
        if (!CheckNull.isNull(info) && info.hasSkill()) {
            planeSkillAttack(force, target, info, actionData);
            info.useSkill();
            return;
        }
        if (force.hasSkill()) {
            skillAttck(force, target, actionData);
            force.useSkill();
            return;
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

    /**
     * 战机技能释放
     *
     * @param force  攻击方
     * @param target 防守方
     */
    private void planeSkillAction(Force force, Force target) {
        actionDataA = CommonPb.Action.newBuilder();
        actionDataA.setTarget(force.id);
        actionDataB = CommonPb.Action.newBuilder();
        actionDataB.setTarget(target.id);
        PlaneInfo info = force.getPlaneInfos().get(1);
        if (info.hasSkill()) {
            planeSkillAttack(force, target, info, actionDataA);
            info.useSkill();
            return;
        }
        info = target.getPlaneInfos().get(1);
        if (info.hasSkill()) {
            planeSkillAttack(target, force, info, actionDataB);
            info.useSkill();
            return;
        }
    }

    /**
     * 战机技能计算
     *
     * @param force
     * @param target
     * @param info
     * @param actionData
     */
    private void planeSkillAttack(Force force, Force target, PlaneInfo info, CommonPb.Action.Builder actionData) {
        int skillId = info.getSkillId();
        StaticPlaneSkill planeSkill = StaticWarPlaneDataMgr.getPlaneSkillById(skillId);
        int hurt = FightCalc.calcPlaneSkillHurt(force, target, planeSkill, battleType);
        hurt = target.hurt(hurt, force, battleType);
        force.killed += hurt;// 记录攻击方击杀数
        force.fighter.hurt += hurt;// 记录总击杀数
        target.fighter.lost += hurt;// 记录总伤兵数
        if (recordFlag) {
            actionData.setHurt(hurt);
            actionData.setCount(target.getSurplusCount());
            actionData.setDeadLine(target.getDeadLine());
            actionData.setPlaneId(info.getPlaneId()); // 记录释放技能的战机
        }
    }

    /**
     * 单次攻击行动  【战斗2--用于区分程序走向】
     *
     * @param force     攻击方
     * @param target    防守方
     * @param isAttcker 本次攻击方是否是本场战斗的进攻方
     */
    private void action(Force force, Force target, boolean isAttcker) {
        if (recordFlag) {
            if (isAttcker) {
                actionDataA = CommonPb.Action.newBuilder();
                actionDataA.setTarget(target.id);
                actionDataA.setTargetRoleId(target.ownerId);
            } else {
                actionDataB = CommonPb.Action.newBuilder();
                actionDataB.setTarget(target.id);
                actionDataB.setTargetRoleId(target.ownerId);
            }
        }
        if (!force.hasFight) {
            force.hasFight = true;
        }
        if (!target.hasFight) {
            target.hasFight = true;
        }
        attack(force, target, isAttcker);
    }

    /**
     * 单次攻击计算  【战斗3--用于区分程序走向】
     *
     * @param force     攻击方
     * @param target    防御方
     * @param isAttacker 本次攻击计算的攻击方是否为战斗的进攻方
     */
    private void attack(Force force, Force target, boolean isAttacker) {
        CommonPb.Action.Builder actionData;
        if (isAttacker) {
            actionData = actionDataA;
        } else {
            actionData = actionDataB;
        }

        if (isDodge(force, target, actionData, isAttacker)) // 闪避
            return;

        float crit = isCrit(force, target, actionData);
        int hurt = FightCalc.calcHurt2(force, target, crit, battleType);
        // 预扣血，并返回真实伤害，真实扣血逻辑在回合结束是执行
        hurt = hurt(target, force, hurt);
        force.killed += hurt;// 记录攻击方击杀数
        force.fighter.hurt += hurt;// 记录总击杀数
        target.fighter.lost += hurt;// 记录总伤兵数

        if (recordFlag) {
            actionData.setHurt(hurt);
            actionData.setCount(target.getSurplusCount());
            actionData.setDeadLine(target.getDeadLine());
        }
    }

    /**
     * 预扣血，并返回真实伤害，真实扣血逻辑在回合结束是执行
     * @param force
     * @param hurt
     * @return
     */
    public int hurt(Force target, Force force, int hurt) {
        hurt = upHurtBuff(target, hurt);  //  提升伤害Buff

        //天赋优化 战斗增益
        //攻击方的伤害加成与防守方伤害减免
        hurt = seasonTalentBuff(force, target, hurt, battleType);

        hurt = defCntBuff(target, hurt);  //  免伤buff
        hurt = defHurtBuff(target, hurt); //  抵消伤害buff
        hurt = notDeadBuff(target, hurt); //  免死buff
        return target.hurt(hurt, null, Integer.MIN_VALUE);
    }

    public static boolean checkPvp(Force force, Force targetForce) {
        if (Objects.isNull(force) || Objects.isNull(targetForce)) {
            return false;
        }

        return force.roleType == Constant.Role.PLAYER && targetForce.roleType == Constant.Role.PLAYER;
    }

    /**
     * //天赋优化 战斗增益
     * @param force 攻击方
     * @param target 防守方
     * @param hurt
     */
    public static int seasonTalentBuff(Force force, Force target, int hurt, int battleType) {
        if (battleType == Integer.MIN_VALUE) {
            //以免重复计算赛季天赋效果
            return hurt;
        }

        //天赋优化 战斗增益
        double hurt_ = hurt;
        if (FightLogic.checkPvp(force, target)) {
            double debugHurt;
            Player forcePlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
            Player targetPlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(target.ownerId);
            if (!CheckNull.isNull(forcePlayer)) {
                //伤害加成
                debugHurt = hurt;
                hurt_ *= (1 + (DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValue(forcePlayer, SeasonConst.TALENT_EFFECT_607) / Constant.TEN_THROUSAND));
                LogUtil.debug("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
                        "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "赛季天赋-攻其不备: ", hurt_ - debugHurt, ", 伤害结果:", hurt_);
            }
            if (!CheckNull.isNull(targetPlayer)) {
                //伤害减免
                debugHurt = hurt_;
                hurt_ *= (1 - (DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValue(targetPlayer, SeasonConst.TALENT_EFFECT_611) / Constant.TEN_THROUSAND));
                LogUtil.debug("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
                        "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "赛季天赋-随形卸力: ", hurt_ - debugHurt, ", 伤害结果:", hurt_);
            }
        }

        return (int) hurt_;
    }

    /**
     * 免死buff
     * @param force
     * @param hurt
     * @return
     */
    private int notDeadBuff(Force force, int hurt) {
        if (hurt >= force.count) {
            double debugHurt = hurt;
            FightBuff notDead = getFightBuff(force, FightConstant.BuffType.BUFF_TYPE_NOT_DEAD);   // 致命伤害
            if (!CheckNull.isNull(notDead)) {
                hurt = force.count - 1;
                CommonPb.FightBuff.Builder builder = notDead.releaseBuff();
                builder.setCurrentLine(force.curLine);
                buffs.add(builder.build());

                LogUtil.debug("防守方角色id: ", force.ownerId, "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "免死buff, 原伤害: ", debugHurt,
                        " 伤害结果:", hurt);
            }
        }
        return hurt;
    }

    public static boolean isCityBattle(int battleType) {
        return battleType == WorldConstant.BATTLE_TYPE_CITY;
    }

    /**
     * 抵消伤害buff
     * @param force
     * @param hurt
     * @return
     */
    private int defHurtBuff(Force force, int hurt) {
        if (hurt > 0) {
            FightBuff defHurt = getFightBuff(force, FightConstant.BuffType.BUFF_TYPE_DEF_HURT);  // 可抵消XXX伤害
            if (!CheckNull.isNull(defHurt)) {
                int buffVal = defHurt.getBuffVal();
                if (hurt >= buffVal) {
                    hurt -= buffVal;
                    defHurt.setBuffVal(0);
                } else {
                    defHurt.subEffectVal(hurt);
                    hurt = 0;
                }
                CommonPb.FightBuff.Builder builder = defHurt.releaseBuff();
                builder.addParam(PbHelper.createTwoIntPb(CommonPb.FightBuffParam.DEFENCE_HURT_VALUE, buffVal - defHurt.getBuffVal()));
                builder.setCurrentLine(force.curLine);
                buffs.add(builder.build());

                LogUtil.debug("防守方角色id: ", force.ownerId, "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "抵消伤害buff: ", buffVal,
                        " 伤害结果:", hurt);
            }
        }
        return hurt;
    }

    /**
     * 免伤buff
     * @param force
     * @param hurt
     * @return
     */
    private int defCntBuff(Force force, int hurt) {
        if (hurt > 0) {
            FightBuff defCnt = getFightBuff(force, FightConstant.BuffType.BUFF_TYPE_DEF_CNT);   // 免除伤害
            if (!CheckNull.isNull(defCnt)) {
                hurt = 0;
                CommonPb.FightBuff.Builder builder = defCnt.releaseBuff();
                builder.setCurrentLine(force.curLine);
                buffs.add(builder.build());

                LogUtil.debug("防守方角色id: ", force.ownerId, "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "免伤buff, ", " 伤害结果:", hurt);
            }
        }
        return hurt;
    }

    /**
     * 取防守方的身上的提升伤害DeBuff,
     * @param force     本次的防守方
     * @param hurt      伤害
     * @return
     */
    private int upHurtBuff(Force force, int hurt) {
        if (hurt > 0) {
            FightBuff upHurt = getFightBuff(force, FightConstant.BuffType.BUFF_TYPE_UP_HURT);   // 燃烧弹的debuff
            if (!CheckNull.isNull(upHurt)) {
                int buffVal = upHurt.getBuffVal();
                hurt += buffVal; // 伤害加成
                CommonPb.FightBuff.Builder builder = upHurt.releaseBuff();
                builder.setCurrentLine(force.curLine);
                buffs.add(builder.build());
            }
        }
        return hurt;
    }

    /**
     * 是否暴击
     *
     * @param force
     * @param target
     * @param actionData
     * @return
     */
    private float isCrit(Force force, Force target, CommonPb.Action.Builder actionData) {
        FightBuff buff = getFightBuff(force, FightConstant.BuffType.BUFF_TYPE_CRIT);
        float crit = 1;
        // 判断是否暴击
        int buffVal = CheckNull.isNull(buff) ? 0 : buff.getBuffVal();
        if (careCrit && FightCalc.isCrit(force, target, buffVal)) {
            if (recordFlag) {
                actionData.setCrit(true);
            }
            if (!CheckNull.isNull(buff) && buffVal > 0) {
                CommonPb.FightBuff.Builder builder = buff.releaseBuff();
                builder.setCurrentLine(force.curLine);
                buffs.add(builder.build());
            }
            // 计算伤害倍率
            crit = FightCalc.calcCrit(force, target);
        }
        return crit;
    }

    /**
     * 是否闪避
     *
     * @param force
     * @param target
     * @param actionData
     * @param isAttcker
     * @return
     */
    private boolean isDodge(Force force, Force target, CommonPb.Action.Builder actionData, boolean isAttcker) {
        FightBuff buff = getFightBuff(force, FightConstant.BuffType.BUFF_TYPE_HIT);
        // 进攻方必中buff
        if (!CheckNull.isNull(buff)) {
            CommonPb.FightBuff.Builder builder = buff.releaseBuff();
            builder.setCurrentLine(force.curLine);
            buffs.add(builder.build());
            return false;
        }
        // 判断是否闪避
        if (careDodge && FightCalc.isDodge(force, target)) {
            if (recordFlag) {
                actionData.setDodge(true);
            }
            return true;
        }
        return false;
    }

    /**
     * 获取战斗buff
     *
     * @param force
     * @param buffType
     * @return
     */
    private FightBuff getFightBuff(Force force, int buffType) {
        FightBuff buff = null;
        if (force.fightBuff.containsKey(buffType)) {
            FightBuff fightBuff = force.fightBuff.get(buffType);
            if (fightBuff.canRelease()) {
                buff = fightBuff;
            }
        }
        return buff;
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
        for (Force force : attacker.forces) {
            if (force != null) {
                if (force.alive()) {
                    attackerAlive = true;
                }
            }
        }

        for (Force force : defender.forces) {
            if (force != null) {
                if (force.alive()) {
                    defenderAlive = true;
                }
            }
        }

        if (attackerAlive && defenderAlive) {
            setWinState(ArmyConstant.FIGHT_RESULT_DRAW);
        } else {
            if (attacker.isAttacker) {
                setWinState(attackerAlive ? ArmyConstant.FIGHT_RESULT_SUCCESS : ArmyConstant.FIGHT_RESULT_FAIL);
            } else {
                setWinState(defenderAlive ? ArmyConstant.FIGHT_RESULT_FAIL : ArmyConstant.FIGHT_RESULT_SUCCESS);
            }
        }
    }

    /**
     * 获取最后一回合
     *
     * @return
     */
    public CommonPb.Round getLastRound() {
        CommonPb.Round round = null;
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

    public void setWinState(int winState) {
        this.winState = winState;
    }

    public CommonPb.Record.Builder getRecordBuild() {
        return recordData;
    }

    public CommonPb.Record generateRecord() {
        return recordData.build();
    }

    public boolean isCareDodge() {
        return careDodge;
    }

    public void setCareDodge(boolean careDodge) {
        this.careDodge = careDodge;
    }

    public boolean isCareCrit() {
        return careCrit;
    }

    public void setCareCrit(boolean careCrit) {
        this.careCrit = careCrit;
    }

    public int getAttrChangeState() {
        return AttrChangeState;
    }

    public void setAttrChangeState(int attrChangeState) {
        AttrChangeState = attrChangeState;
    }

    public Fighter getAttacker() {
        return attacker;
    }

    public Fighter getDefender() {
        return defender;
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }
}
