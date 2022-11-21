package com.gryphpoem.game.zw.util;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.ActionDirection;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.pojo.p.MultiEffectActionPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-14 19:08
 */
public class FightPbUtil {

    public static <T> BattlePb.BaseBattleStage.Builder createBaseBattleStagePb(int stage,
                                                                               GeneratedMessage.GeneratedExtension<BattlePb.BaseBattleStage, T> ext, T msg) {
        BattlePb.BaseBattleStage.Builder builder = BattlePb.BaseBattleStage.newBuilder().setStage(stage);
        if (msg != null) {
            builder.setExtension(ext, msg);
        }

        return builder;
    }

    public static <T> BattlePb.BaseEffectAction.Builder createBaseEffectActionPb(GeneratedMessage.GeneratedExtension<BattlePb.BaseEffectAction, T> ext, T msg,
                                                                                 int effectLogicId, int buffGiver, int bufferOwner, int timing, int actionType,
                                                                                 boolean isOnStageSkill, int skillId) {
        BattlePb.BaseEffectAction.Builder builder = BattlePb.BaseEffectAction.newBuilder().setEffectLogicId(effectLogicId).setActors(bufferOwner).setActionType(actionType);
        if (actionType == FightConstant.EffectStatus.APPEAR) {
            builder.setEffectSource(buffGiver).setTiming(timing).setOnStageSkill(isOnStageSkill).setSkillId(skillId);
        }
        if (msg != null) {
            builder.setExtension(ext, msg);
        }

        return builder;
    }

    public static <T> BattlePb.BaseAction.Builder createBaseActionPb(GeneratedMessage.GeneratedExtension<BattlePb.BaseAction, T> ext, T msg,
                                                                     int actionType, int actionOwner) {
        BattlePb.BaseAction.Builder builder = BattlePb.BaseAction.newBuilder().setType(actionType).setActors(actionOwner);
        if (msg != null) {
            builder.setExtension(ext, msg);
        }

        return builder;
    }

    /**
     * 创建开场pb
     *
     * @param force
     * @param target
     * @return
     */
    public static BattlePb.BattlePreparationStage.Builder createBattlePreparationStagePb(Force force, Force target) {
        BattlePb.BattlePreparationStage.Builder builder = BattlePb.BattlePreparationStage.newBuilder();
        builder.setTotalArms(createDataInt(force.hp, target.hp));
        builder.addArrangeArms(createDataInt(force.curLine, force.count));
        builder.addArrangeArms(createDataInt(target.curLine, target.count));
        return builder;
    }

    public static BattlePb.BothBattleEntityPb.Builder createBothBattleEntityPb() {
        BattlePb.BothBattleEntityPb.Builder builder = BattlePb.BothBattleEntityPb.newBuilder();
        return builder;
    }

    /**
     * 双方武将战斗结束, 填充战斗数值到PB中
     *
     * @param atk
     * @param def
     * @param builder
     */
    public static void paddingBattleEntity(Force atk, Force def, BattlePb.BothBattleEntityPb.Builder builder) {
        addBattleEntity(atk, builder, true);
        addBattleEntity(def, builder, false);
    }

    private static void addBattleEntity(Force force, BattlePb.BothBattleEntityPb.Builder builder, boolean atk) {
        if (atk) {
            builder.addAtk(createBattleEntityPb(force, force.id, BattlePb.BattleHeroTypeDefine.PRINCIPAL_HERO_VALUE));
        } else {
            builder.addDef(createBattleEntityPb(force, force.id, BattlePb.BattleHeroTypeDefine.PRINCIPAL_HERO_VALUE));
        }
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            force.assistantHeroList.forEach(ass -> {
                if (CheckNull.isNull(ass)) return;
                if (atk) {
                    builder.addAtk(createBattleEntityPb(force, ass.getHeroId(), BattlePb.BattleHeroTypeDefine.DEPUTY_HERO_VALUE));
                } else {
                    builder.addDef(createBattleEntityPb(force, ass.getHeroId(), BattlePb.BattleHeroTypeDefine.PRINCIPAL_HERO_VALUE));
                }
            });
        }
    }

    public static BattlePb.BattleEntityPb.Builder createBattleEntityPb(Force force, int heroId, int heroType) {
        BattlePb.BattleEntityPb.Builder builder = BattlePb.BattleEntityPb.newBuilder();
        builder.setEntityType(force.roleType);
        builder.setHeroType(heroType);
        builder.setOwnerId(String.valueOf(force.ownerId));
        builder.setHeroId(heroId);

        // 添加普攻记录
        BattlePb.ActionDetails.Builder detailPb = BattlePb.ActionDetails.newBuilder();
        detailPb.setActionType(BattlePb.ActionTypeDefine.ORDINARY_ATTACK_VALUE);
        detailPb.setKilled(force.getAttackKilled(heroId));
        detailPb.setActionCnt(force.attackCount);
        builder.addDetails(detailPb.build());

        detailPb.clear();
        List<SimpleHeroSkill> skillList;
        if (!CheckNull.isEmpty(skillList = force.getSkillList(heroId))) {
            // 添加技能记录
            skillList.forEach(simpleHeroSkill -> {
                detailPb.setActionCnt(simpleHeroSkill.getReleaseCount());
                detailPb.setActionType(BattlePb.ActionTypeDefine.SKILL_ATTACK_VALUE);
                detailPb.setSkillId(simpleHeroSkill.getS_skill().getSkillId());
                detailPb.setKilled(simpleHeroSkill.getSkillDamage());
                detailPb.setKilled(simpleHeroSkill.getS_skill().getLevel());
                builder.addDetails(detailPb.build());
                detailPb.clear();
            });
        }
        return builder;
    }

    public static BattlePb.DataInt createDataInt(int v1, int v2) {
        return BattlePb.DataInt.newBuilder().setV1(v1).setV2(v2).build();
    }

    public static BattlePb.Data3Int create3DataInt(int v1, int v2, int v3) {
        return BattlePb.Data3Int.newBuilder().setV1(v1).setV2(v2).setV3(v3).build();
    }

    public static int getActingSize(Force force, int heroId) {
        if (force.fighter.isAttacker) {
            if (force.id == heroId) {
                return BattlePb.ActorsDefine.ATTACKER_PRINCIPAL_HERO_VALUE;
            } else {
                return BattlePb.ActorsDefine.ATTACKER_DEPUTY_HERO_VALUE;
            }
        } else {
            if (force.id == heroId) {
                return BattlePb.ActorsDefine.DEFENDER_PRINCIPAL_HERO_VALUE;
            } else {
                return BattlePb.ActorsDefine.DEFENDER_DEPUTY_HERO_VALUE;
            }
        }
    }

    /**
     * 获取当前行动
     *
     * @param contextHolder
     * @return
     */
    public static void addEffectActionList(FightContextHolder contextHolder, BattlePb.BaseEffectAction.Builder basePb) {
        if (!CheckNull.isEmpty(contextHolder.getMultiEffectActionList())) {
            MultiEffectActionPb firstPb = contextHolder.getInitMultiEffectActionList().peekFirst();
            if (Objects.nonNull(firstPb)) {
                boolean padding = false;
                if (Objects.nonNull(firstPb.getCurSkillPb())) {
                    firstPb.getCurSkillPb().addEffectAction(basePb.build());
                    padding = true;
                }
                if (Objects.nonNull(firstPb.getCurAttackPb())) {
                    firstPb.getCurAttackPb().addEffectAction(basePb.build());
                    padding = true;
                }
                if (!padding && Objects.nonNull(firstPb.getRoundActionPb())) {
                    firstPb.getRoundActionPb().addEffectAction(basePb.build());
                }
                return;
            }
        }

        if (contextHolder.getCurSkillActionPb() != null) {
            contextHolder.getCurSkillActionPb().addEffectAction(basePb.build());
            return;
        }
        if (contextHolder.getCurAttackActionPb() != null) {
            contextHolder.getCurAttackActionPb().addEffectAction(basePb.build());
            return;
        }

        if (contextHolder.getRoundActionPb() != null) {
            contextHolder.getRoundActionPb().addEffectAction(basePb.build());
        }
    }

    /**
     * 往当前动作塞动作结果
     *
     * @param contextHolder
     * @param resultPb
     */
    public static void setActionResult(FightContextHolder contextHolder, BattlePb.ActionResult resultPb) {
        if (!CheckNull.isEmpty(contextHolder.getMultiEffectActionList())) {
            MultiEffectActionPb firstPb = contextHolder.getInitMultiEffectActionList().peekFirst();
            if (Objects.nonNull(firstPb)) {
                if (Objects.nonNull(firstPb.getCurSkillPb())) {
                    firstPb.getCurSkillPb().setResult(resultPb);
                }
                if (Objects.nonNull(firstPb.getCurAttackPb())) {
                    firstPb.getCurAttackPb().setResult(resultPb);
                }
                return;
            }
        }

        if (contextHolder.getCurSkillActionPb() != null) {
            contextHolder.getCurSkillActionPb().setResult(resultPb);
        }
        if (contextHolder.getCurAttackActionPb() != null) {
            contextHolder.getCurAttackActionPb().setResult(resultPb);
        }
    }

    /**
     * 创建下一个动作信息内容pb类
     *
     * @param contextHolder
     * @param actionDirection
     * @param skill
     */
    public static void initNextMultiEffectAction(FightContextHolder contextHolder, ActionDirection actionDirection, boolean skill) {
        MultiEffectActionPb curMultiAction = new MultiEffectActionPb();
        BattlePb.MultiEffectAction.Builder builder = BattlePb.MultiEffectAction.newBuilder();
        curMultiAction.setCurMultiEffectActionPb(builder);
        LinkedList<MultiEffectActionPb> multiEffectActionList = contextHolder.getInitMultiEffectActionList();

        MultiEffectActionPb mAction = multiEffectActionList.peekFirst();
        if (Objects.nonNull(mAction)) {
            if (Objects.nonNull(mAction.getCurSkillPb()))
                curMultiAction.setLastSkillPb(mAction.getCurSkillPb());

            if (Objects.nonNull(mAction.getCurAttackPb())) {
                curMultiAction.setLastAttackPb(mAction.getCurAttackPb());
            }
        } else {
            boolean padding = false;
            if (Objects.nonNull(contextHolder.getCurSkillActionPb())) {
                curMultiAction.setLastSkillPb(contextHolder.getCurSkillActionPb());
                padding = true;
            }
            if (Objects.nonNull(contextHolder.getCurAttackActionPb())) {
                curMultiAction.setLastAttackPb(contextHolder.getCurAttackActionPb());
                padding = true;
            }
            if (!padding && Objects.nonNull(contextHolder.getRoundActionPb())) {
                curMultiAction.setRoundActionPb(contextHolder.getRoundActionPb());
            }
        }

        if (skill)
            curMultiAction.setCurSkillPb(BattlePb.SkillAction.newBuilder());
        else
            curMultiAction.setCurAttackPb(BattlePb.OrdinaryAttackAction.newBuilder());
        curMultiAction.setActionDirection(actionDirection);
        multiEffectActionList.addFirst(curMultiAction);
    }

    /**
     * 释放完技能伤害效果后的处理
     *
     * @param fightBuff
     * @param contextHolder
     * @param timing
     */
    public static void handleAfterSkillDamageEffect(IFightBuff fightBuff, FightContextHolder contextHolder, StaticEffectRule rule, int timing) {
        MultiEffectActionPb curMultiAction = contextHolder.getMultiEffectActionList().removeFirst();
        SimpleHeroSkill simpleHeroSkill = (SimpleHeroSkill) fightBuff.getSkill();
        BattlePb.BaseEffectAction.Builder basePb = FightPbUtil.createBaseEffectActionPb(BattlePb.MultiEffectAction.effect,
                curMultiAction.getCurMultiEffectActionPb().build(), FightConstant.EffectLogicId.SKILL_DAMAGE,
                FightPbUtil.getActingSize(fightBuff.getBuffGiver(), fightBuff.getBuffGiverId()),
                FightPbUtil.getActingSize(fightBuff.getForce(), fightBuff.getForceId()), timing, FightConstant.EffectStatus.APPEAR,
                simpleHeroSkill.isOnStageSkill(), simpleHeroSkill.getS_skill().getSkillId());

        boolean padding = false;
        if (curMultiAction.getLastSkillPb() != null) {
            curMultiAction.getLastSkillPb().addEffectAction(basePb.build());
            padding = true;
        }
        if (curMultiAction.getLastAttackPb() != null) {
            curMultiAction.getLastAttackPb().addEffectAction(basePb.build());
            padding = true;
        }
        if (!padding) {
            if (curMultiAction.getRoundActionPb() != null) {
                curMultiAction.getRoundActionPb().addEffectAction(basePb.build());
            } else {
                LogUtil.error("嵌套技能伤害有问题, rule: ", rule.getEffectId(), ", curList: ", contextHolder.getMultiEffectActionList());
            }
        }
    }

    /**
     * 判断当前动作是否可以进行反击
     *
     * @param contextHolder
     * @return
     */
    public static boolean curActionCounterattack(FightContextHolder contextHolder) {
        if (CheckNull.isEmpty(contextHolder.getMultiEffectActionList()))
            return true;
        MultiEffectActionPb pb = contextHolder.getMultiEffectActionList().peekFirst();
        if (CheckNull.isNull(pb))
            return true;
        return pb.isCounterattack();
    }

    /**
     * 获取上一个动作执行与被执行方信息
     *
     * @param contextHolder
     * @return
     */
    public static ActionDirection getLastActionDirection(FightContextHolder contextHolder) {
        if (!CheckNull.isEmpty(contextHolder.getMultiEffectActionList())) {
            MultiEffectActionPb lastEffectActionPb = contextHolder.getMultiEffectActionList().peekFirst();
            if (Objects.nonNull(lastEffectActionPb)) {
                return lastEffectActionPb.getActionDirection();
            }
        }

        return contextHolder.getActionDirection();
    }
}
