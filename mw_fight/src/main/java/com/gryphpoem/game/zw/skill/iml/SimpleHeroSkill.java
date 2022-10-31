package com.gryphpoem.game.zw.skill.iml;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.skill.abs.AbstractHeroSkill;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 通用技能实现
 * Author: zhangpeng
 * createTime: 2022-10-20 17:08
 */
public class SimpleHeroSkill extends AbstractHeroSkill {
    public SimpleHeroSkill(StaticHeroSkill s_skill) {
        super(s_skill);
    }

    @Override
    public void releaseSkillEffect(FightContextHolder contextHolder, StaticHeroSkill staticHeroSkill, Object... params) {
        // TODO 计算技能伤害

        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.BEFORE_SKILL_DAMAGE);
        // TODO 扣血

        FightUtil.releaseAllBuffEffect(contextHolder, FightConstant.BuffEffectTiming.AFTER_SKILL_DAMAGE);
    }

    @Override
    public void releaseSkillBuff(FightContextHolder contextHolder, StaticHeroSkill staticHeroSkill, Object... params) {
        if (CheckNull.isNull(staticHeroSkill)) {
            // 技能配置为空
            LogUtil.error("skill config is null, activeBuffImpl");
            return;
        }

        // 添加技能的buff
        if (!CheckNull.isEmpty(staticHeroSkill.getBuff())) {
            LinkedList<IFightBuff> removeBuffList = new LinkedList<>();
            FightManager fightManager = DataResource.ac.getBean(FightManager.class);
            StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);

            // 释放buff
            for (List<Integer> buffConfig : staticHeroSkill.getBuff()) {
                // 概率释放
                if (!RandomHelper.isHitRangeIn10000(buffConfig.get(2)))
                    continue;
                FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(buffConfig.get(0));
                if (CheckNull.isNull(buffObjective))
                    continue;
                StaticBuff staticBuff = staticFightManager.getStaticBuff(buffConfig.get(1));
                if (CheckNull.isNull(staticBuff))
                    continue;

                Force actingForce = FightUtil.actingForce(contextHolder.getContext().getAttacker(), contextHolder.getContext().getDefender(), buffObjective, true);
                Map<Integer, LinkedList<IFightBuff>> buffMap = FightUtil.actingForceBuff(actingForce, buffObjective);
                if (CheckNull.isNull(buffMap)) {
                    continue;
                }

                for (Map.Entry<Integer, LinkedList<IFightBuff>> buffsEntry : buffMap.entrySet()) {
                    boolean addBuff = true;
                    LinkedList<IFightBuff> buffs = buffsEntry.getValue();
                    if (!CheckNull.isEmpty(buffs)) {
                        for (IFightBuff fightBuff : buffs) {
                            if (CheckNull.isNull(fightBuff)) continue;
                            if (!fightBuff.buffCoexistenceCheck(staticBuff, removeBuffList)) {
                                addBuff = false;
                                break;
                            }
                        }
                    }

                    if (!addBuff) {
                        continue;
                    }

                    // buff列表是否重复
                    if (!CheckNull.isEmpty(buffs)) {
                        List<IFightBuff> sameIdBuffList = buffs.stream().filter(b ->
                                b.getBuffConfig().getBuffId() == staticBuff.getBuffId()).collect(Collectors.toList());
                        if (!CheckNull.isEmpty(sameIdBuffList)) {
                            if (sameIdBuffList.size() >= staticBuff.getCoexistingIdNum()) {
                                switch (staticBuff.getSameIdReplacementRule()) {
                                    // 比较buff留存规则
                                    case FightConstant.ReplacementBuffRule.LONGER_ROUNDS:
                                        // 保留更长回合数buff
                                        IFightBuff fightBuff = sameIdBuffList.stream().filter(b ->
                                                        b.getBuffEffectiveRounds() <= staticBuff.getContinuousRound()).
                                                min(Comparator.comparingInt(IFightBuff::getBuffEffectiveRounds)).
                                                orElse(null);
                                        if (Objects.nonNull(fightBuff)) removeBuffList.add(fightBuff);
                                        break;
                                    case FightConstant.ReplacementBuffRule.MORE_STRONG:
                                        // 保留更强buff
                                        // TODO 比较两个相同buffId的强度(相同buffId比较相同效果)

                                        break;
                                }
                            }
                        }
                    }

                    IFightBuff fightBuff = fightManager.createFightBuff(staticBuff.getBuffEffectiveWay(), staticBuff);
                    fightBuff.setForce(actingForce);
                    fightBuff.setBuffGiver(contextHolder.getContext().getAttacker());
                    fightBuff.setForceId(buffsEntry.getKey());
                    fightBuff.releaseBuff(buffs, contextHolder, buffConfig, params);
                    if (!CheckNull.isEmpty(removeBuffList)) {
                        buffs.removeAll(removeBuffList);
                        // TODO 预留buff失效还原逻辑
                        removeBuffList.forEach(buff -> buff.buffLoseEffectiveness(contextHolder, params));
                        // TODO 客户端表现PB 处理
                        removeBuffList.clear();
                    }
                }
            }
        }
    }
}
