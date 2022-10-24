package com.gryphpoem.game.zw.skill.iml;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
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
    public void releaseSkillEffect(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, FightResult fightResult, Object... params) {

    }

    @Override
    public void releaseSkillBuff(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, FightResult fightResult, Object... params) {
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

                Force actingForce = FightUtil.actingForce(attacker, defender, buffObjective);
                Map<Integer, LinkedList<IFightBuff>> buffMap = FightUtil.actingForceBuff(actingForce, buffObjective);
                if (CheckNull.isNull(buffMap)) {
                    continue;
                }

                for (LinkedList<IFightBuff> buffs : buffMap.values()) {
                    boolean addBuff = true;
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
                                                        b.getBuffEffectiveRounds() < staticBuff.getContinuousRound()).
                                                min(Comparator.comparingInt(IFightBuff::getBuffEffectiveRounds)).
                                                orElse(null);
                                        if (Objects.nonNull(fightBuff)) removeBuffList.add(fightBuff);
                                        break;
                                    case FightConstant.ReplacementBuffRule.MORE_STRONG:
                                        // 保留更强buff
                                        
                                        break;
                                }
                            }
                        }
                    }

                    IFightBuff fightBuff = fightManager.createFightBuff(staticBuff.getBuffEffectiveWay(), staticBuff);
                    fightBuff.setForce(actingForce);
                    fightBuff.releaseBuff(buffs, fightLogic, buffConfig, fightResult, params);
                    if (!CheckNull.isEmpty(removeBuffList)) {
                        buffs.removeAll(removeBuffList);
                        // TODO 预留buff失效还原逻辑
                        removeBuffList.forEach(buff -> buff.buffLoseEffectiveness(attacker, defender, fightLogic, fightResult, params));
                        // TODO 客户端表现PB 处理
                        removeBuffList.clear();
                    }
                }
            }
        }
    }
}
