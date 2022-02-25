package com.gryphpoem.game.zw.resource.pojo.fight.skill;

import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.FightConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;
import com.gryphpoem.game.zw.resource.pojo.fight.FightBuff;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-16 15:17
 */
public final class FightSkillUtils {

    /**
     * 技能释放
     *
     * @param skillAction 技能
     * @param dto         战斗 ACTION
     */
    public static void releaseSkill(FightSkillAction skillAction, FightActionDto dto) {
        //增加一次释放次数
        skillAction.setReleaseCount(skillAction.getReleaseCount() + 1);
        StaticSkillAction s_skill = skillAction.getSkillConfig();
        switch (s_skill.getEffect()) {
            case FightConstant.Effect.EFFECT_101:
                releaseSkill101(skillAction, s_skill, dto);
                break;
            case FightConstant.Effect.EFFECT_102:
                releaseSkill102(skillAction, s_skill, dto);
                break;
            case FightConstant.Effect.EFFECT_103:
                releaseSkillOnlyBuff(s_skill, dto);
                break;
            case FightConstant.Effect.EFFECT_104:
                releaseSkill104(s_skill,dto);
                break;
        }
    }

    /**
     * 多段攻击: 可连续攻击X次，首次造成固定的X点伤害，剩余每次伤害为上次的x%
     *
     * @param skillAction 技能
     * @param s_skill     技能配置
     * @param dto         战斗 dto
     */
    private static void releaseSkill101(FightSkillAction skillAction, StaticSkillAction s_skill, FightActionDto dto) {
        CommonPb.ActionSkill.Builder actionSkillBuilder = CommonPb.ActionSkill.newBuilder();
        actionSkillBuilder.setSkillId(s_skill.getId());//释放的技能ID
        List<List<Integer>> params = s_skill.getEffectParam();
        int hurtCount = params.get(0).get(0);//攻击次数
        int hurt_1st = params.get(1).get(0);//首次伤害
        int reduction = params.get(2).get(0);//后续伤害为上次伤害的万分比

        Force target = dto.getTargets().get(0);
        Force source = dto.getSource();
        int totalHurt = 0, hurt = 0;
        for (int i = 1; i <= hurtCount; i++) {
            hurt = (int) (hurt == 0 ? hurt_1st : (hurt * (reduction / Constant.TEN_THROUSAND)));
            totalHurt += hurt;
            CommonPb.ActionSkill.Hurt.Builder hurtBuilder = CommonPb.ActionSkill.Hurt.newBuilder();
            //赛季天赋优化 伤害显示(不是实际伤害添加，只做显示)添加加成， 在第一次伤害时增加数值
            hurtBuilder.setHurt(FightLogic.seasonTalentBuff(source, target, hurt, 0));
            actionSkillBuilder.addHurt(hurtBuilder);
        }

        //处理buff计算最终损兵
        totalHurt = dto.getFightLogic().hurt(target, source, totalHurt);
        target.fighter.lost += totalHurt;// 记录总伤兵数
        source.killed += totalHurt;// 记录攻击方击杀数
        source.fighter.hurt += totalHurt;// 记录总击杀数
        if (dto.getFightLogic().recordFlag) {
            CommonPb.Action.Builder actionBuilder = dto.getActionBuilder();
            actionBuilder.setHurt(totalHurt);
            actionBuilder.setCount(target.getSurplusCount());
            actionBuilder.setDeadLine(target.getDeadLine());
            actionBuilder.setActionSkill(actionSkillBuilder);
        }
    }

    /**
     * 加buff，对应的buff类型{@link com.gryphpoem.game.zw.resource.constant.FightConstant.BuffType#BUFF_TYPE_NOT_DEAD}
     * -> s_fight_buff
     *
     * @param skillAction
     * @param s_skill
     * @param dto
     */
    private static void releaseSkill102(FightSkillAction skillAction, StaticSkillAction s_skill, FightActionDto dto) {
        CommonPb.ActionSkill.Builder actionSkillBuilder = CommonPb.ActionSkill.newBuilder();
        actionSkillBuilder.setSkillId(s_skill.getId());//释放的技能ID
        if (ListUtils.isNotBlank(s_skill.getBuffs())) {
            s_skill.getBuffs().forEach(buffId -> {
                StaticFightBuff staticFightBuff = StaticFightDataMgr.getFightBuffMapById(buffId);
                if (Objects.nonNull(staticFightBuff)) {
                    FightBuff buff = new FightBuff(staticFightBuff, dto.getSource().id, dto.getDirection() == FightActionDto.Direction.ACTION_ATK);
                    buff.releaseBuff(dto.getSource(), dto.getTargets().get(0), dto.getFightLogic());
                }
            });
        }
        if (dto.getFightLogic().recordFlag) {
            CommonPb.Action.Builder actionBuilder = dto.getActionBuilder();
            actionBuilder.setActionSkill(actionSkillBuilder);
        }
    }

    /**
     * 针对技能只释放buff
     *
     * @param s_skill
     * @param dto
     */
    private static void releaseSkillOnlyBuff(StaticSkillAction s_skill, FightActionDto dto) {
        CommonPb.ActionSkill.Builder actionSkillBuilder = CommonPb.ActionSkill.newBuilder();
        actionSkillBuilder.setSkillId(s_skill.getId());//释放的技能ID
        if (ListUtils.isNotBlank(s_skill.getBuffs())) {
            s_skill.getBuffs().forEach(buffId -> {
                StaticFightBuff staticFightBuff = StaticFightDataMgr.getFightBuffMapById(buffId);
                if (Objects.nonNull(staticFightBuff)) {
                    FightBuff buff = new FightBuff(staticFightBuff, dto.getSource().id, dto.getDirection() == FightActionDto.Direction.ACTION_ATK);
                    buff.releaseBuff(dto.getSource(), dto.getTargets().get(0), dto.getFightLogic());
                }
            });
        }
        if (dto.getFightLogic().recordFlag) {
            CommonPb.Action.Builder actionBuilder = dto.getActionBuilder();
            actionBuilder.setActionSkill(actionSkillBuilder);
        }
    }

    private static void releaseSkill104(StaticSkillAction s_skill,FightActionDto dto){
        CommonPb.ActionSkill.Builder actionSkillBuilder = CommonPb.ActionSkill.newBuilder();
        actionSkillBuilder.setSkillId(s_skill.getId());//释放的技能ID
        if (ListUtils.isNotBlank(s_skill.getBuffs())) {
            s_skill.getBuffs().forEach(buffId -> {
                StaticFightBuff staticFightBuff = StaticFightDataMgr.getFightBuffMapById(buffId);
                if (Objects.nonNull(staticFightBuff)) {
                    FightBuff buff = new FightBuff(staticFightBuff, dto.getSource().id, dto.getDirection() == FightActionDto.Direction.ACTION_ATK);
                    buff.releaseBuff(dto.getSource(),dto.getTargets().get(0),dto.getFightLogic());
                }
            });
        }
        if (dto.getFightLogic().recordFlag) {
            CommonPb.Action.Builder actionBuilder = dto.getActionBuilder();
            actionBuilder.setActionSkill(actionSkillBuilder);
        }
    }

    private static boolean checkSkillEffect101(StaticSkillAction s_skill) {
        List<List<Integer>> params = s_skill.getEffectParam();
        if (CheckNull.nonEmpty(params) && params.size() == 3) {
            if (params.get(0).size() == 1 && params.get(1).size() == 1 && params.get(2).size() == 1) {
                int hurtCount = params.get(0).get(0);//攻击次数
                int hurt_1st = params.get(1).get(0);//首次伤害
                int reduction = params.get(2).get(0);//后续伤害为上次伤害的万分比
                return hurtCount > 0 && hurt_1st > 0 && reduction > 0 && reduction <= 10000;
            }
        }
        return false;
    }

    /**
     * 技能检查
     *
     * @param s_skill 技能
     */
    public static boolean checkSkillEffConfig(StaticSkillAction s_skill) {
        //增加一次释放次数
        switch (s_skill.getEffect()) {
            case FightConstant.Effect.EFFECT_101:
                return checkSkillEffect101(s_skill);
            case FightConstant.Effect.EFFECT_102:
                return checkSkillEffect102(s_skill);
            case FightConstant.Effect.EFFECT_103:
                return checkSkillEffectOnlyBuff(s_skill);
            case FightConstant.Effect.EFFECT_104:
                return checkSkillEffect104(s_skill);
        }
        return true;
    }

    private static boolean checkSkillEffect102(StaticSkillAction s_skill) {
        if (!ListUtils.isBlank(s_skill.getBuffs())) {
            StaticFightBuff staticFightBuff = StaticFightDataMgr.getFightBuffMapById(s_skill.getBuffs().get(0));
            if (Objects.nonNull(staticFightBuff)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkSkillEffect104(StaticSkillAction staticSkillAction){
        if(!ListUtils.isBlank(staticSkillAction.getBuffs())){
            StaticFightBuff staticFightBuff = StaticFightDataMgr.getFightBuffMapById(staticSkillAction.getBuffs().get(0));
            if(Objects.nonNull(staticFightBuff)){
                return true;
            }
        }
        return false;
    }

    private static boolean checkSkillEffectOnlyBuff(StaticSkillAction s_skill) {
        if (ObjectUtils.isEmpty(s_skill.getBuffs())) {
            return false;
        }

        for (Integer buffId : s_skill.getBuffs()) {
            StaticFightBuff staticFightBuff = StaticFightDataMgr.getFightBuffMapById(buffId);
            if (CheckNull.isNull(staticFightBuff)) {
                return false;
            }
        }

        return true;
    }

}
