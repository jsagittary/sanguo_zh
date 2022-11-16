package com.gryphpoem.game.zw.util;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

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
        if (contextHolder.getCurEffectSkillActionPb() != null) {
            contextHolder.getCurEffectSkillActionPb().addEffectAction(basePb.build());
            return;
        }
        if (contextHolder.getCurEffectAttackActionPb() != null) {
            contextHolder.getCurEffectAttackActionPb().addEffectAction(basePb.build());
            return;
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
     * 获取当前动作结算结果pb
     *
     * @param contextHolder
     * @return
     */
    public static BattlePb.ActionResult.Builder curActionResult(FightContextHolder contextHolder) {
        if (contextHolder.getCurEffectSkillActionPb() != null) {
            BattlePb.ActionResult builder = contextHolder.getCurEffectSkillActionPb().getResult();
            if (builder == null) {
                BattlePb.ActionResult.Builder resultPb = BattlePb.ActionResult.newBuilder();
                contextHolder.getCurEffectSkillActionPb().setResult(resultPb);
                return resultPb;
            }
        }
        if (contextHolder.getCurEffectAttackActionPb() != null) {
            BattlePb.ActionResult builder = contextHolder.getCurEffectAttackActionPb().getResult();
            if (builder == null) {
                BattlePb.ActionResult.Builder resultPb = BattlePb.ActionResult.newBuilder();
                contextHolder.getCurEffectAttackActionPb().setResult(resultPb);
                return resultPb;
            }
        }

        if (contextHolder.getCurSkillActionPb() != null) {
            BattlePb.ActionResult resultPb = contextHolder.getCurSkillActionPb().getResult();
            if (resultPb == null) {
                BattlePb.ActionResult.Builder resultPb_ = BattlePb.ActionResult.newBuilder();
                contextHolder.getCurSkillActionPb().setResult(resultPb_);
                return resultPb_;
            }
        } else {
            BattlePb.ActionResult resultPb = contextHolder.getCurAttackActionPb().getResult();
            if (resultPb == null) {
                BattlePb.ActionResult.Builder resultPb_ = BattlePb.ActionResult.newBuilder();
                contextHolder.getCurAttackActionPb().setResult(resultPb_);
                return resultPb_;
            }
        }

        return null;
    }

    /**
     * 往当前动作塞动作结果
     *
     * @param contextHolder
     * @param resultPb
     */
    public static void setActionResult(FightContextHolder contextHolder, BattlePb.ActionResult resultPb) {
        if (contextHolder.getCurEffectSkillActionPb() != null) {
            contextHolder.getCurEffectSkillActionPb().setResult(resultPb);
            return;
        }
        if (contextHolder.getCurEffectAttackActionPb() != null) {
            contextHolder.getCurEffectAttackActionPb().setResult(resultPb);
            return;
        }

        if (contextHolder.getCurSkillActionPb() != null) {
            contextHolder.getCurSkillActionPb().setResult(resultPb);
        }
        if (contextHolder.getCurAttackActionPb() != null) {
            contextHolder.getCurAttackActionPb().setResult(resultPb);
        }

    }
}
