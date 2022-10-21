package com.gryphpoem.game.zw.resource.pojo.fight.skill;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.resource.constant.FightConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-17 10:45
 */
public class SkillTriggerUtils {

    public static boolean doTriggerCond(FightSkillAction skillAction) {
        switch (skillAction.getSkillConfig().getTriggerCond()) {
            case FightConstant.TriggerCond.TriggerCond_0:
                return doTriggerCond0(skillAction);
            case FightConstant.TriggerCond.TriggerCond_1:
                return doTriggerCond1(skillAction);
            default:
                return false;
        }
    }

    /**
     * 必定触发的技能
     *
     * @param skillAction 技能
     * @return true:成功触发
     */
    private static boolean doTriggerCond0(FightSkillAction skillAction) {
        //必定触发的技能
        StaticSkillAction s_skill = skillAction.getSkillConfig();
        //触发次数
        List<Integer> list0 = s_skill.getTriggerParam().get(0);
        if (CheckNull.nonEmpty(list0)) {
            int totalCount = list0.get(0);//0-没有释放次数限制
            return totalCount == 0 || totalCount - skillAction.getReleaseCount() > 0;
        }
        return false;
    }

    /**
     * 概率触发的技能
     *
     * @param skillAction 技能
     * @return true:触发成功
     */
    private static boolean doTriggerCond1(FightSkillAction skillAction) {
        StaticSkillAction s_skill = skillAction.getSkillConfig();
        List<Integer> lst0 = s_skill.getTriggerParam().get(0);
        //触发概率
        int probability = lst0.get(0);
        //增加的释放技能概率
        probability += skillAction.getAddProbability();
        LogUtil.debug("skillAction: ", s_skill.getBaseSkill(), ", probability: ", probability);

        //触发次数
        List<Integer> lst1 = s_skill.getTriggerParam().get(1);
        //技能还有释放次数
        if (lst1.get(0) - skillAction.getReleaseCount() > 0) {
            if (skillAction.getTriggerCount() <= 0) {
                //没有roll 过技能
                skillAction.setTriggerCount(skillAction.getTriggerCount() + 1);
                boolean isTriggerSucc = RandomHelper.isHitRangeIn10000(probability);
                if (isTriggerSucc) {
                    skillAction.setTriggerSuccCount(skillAction.getTriggerSuccCount() + 1);
                } else {
                    skillAction.setTriggerFailCount(skillAction.getTriggerFailCount() + 1);
                }
            }
            return skillAction.getTriggerSuccCount() - skillAction.getReleaseCount() > 0;
        }
        return false;
    }

    public static boolean checkSkillTriggerConfig(StaticSkillAction s_skill) {
        switch (s_skill.getTriggerCond()) {
            case FightConstant.TriggerCond.TriggerCond_0:
                return checkTriggerCond0(s_skill);
            case FightConstant.TriggerCond.TriggerCond_1:
                return checkTriggerCond1(s_skill);
            default:
                return false;
        }
    }

    /**
     * 检测必定触发的技能的触发配置
     *
     * @param s_skill 技能配置
     * @return true : 配置正确
     */
    private static boolean checkTriggerCond0(StaticSkillAction s_skill) {
        if (s_skill.getTriggerCond() == FightConstant.TriggerCond.TriggerCond_0) {
            List<List<Integer>> triggerParams = s_skill.getTriggerParam();
            if (triggerParams != null && triggerParams.size() == 1) {
                List<Integer> list0 = triggerParams.get(0);
                return list0 != null && list0.size() == 1 && list0.get(0) > 0;
            }
        }
        return false;
    }

    private static boolean checkTriggerCond1(StaticSkillAction s_skill) {
        if (s_skill.getTriggerCond() == FightConstant.TriggerCond.TriggerCond_1) {
            List<List<Integer>> triggerParams = s_skill.getTriggerParam();
            if (triggerParams != null && triggerParams.size() == 2) {
                List<Integer> lst0 = triggerParams.get(0);
                //触发概率配置检测
                if (lst0 != null && lst0.size() == 1 && lst0.get(0) > 0) {
                    //触发次数配置检测
                    List<Integer> lst1 = triggerParams.get(1);
                    return lst1 != null && lst1.size() == 1 && lst1.get(0) > 0;
                }
            }
        }
        return false;
    }

}
