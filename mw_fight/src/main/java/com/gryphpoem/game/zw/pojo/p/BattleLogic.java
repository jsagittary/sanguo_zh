package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-04 18:34
 */
@Component
public class BattleLogic {
    @Autowired
    private FightManager fightManager;
    @Autowired
    private StaticFightManager staticFightManager;

    /**
     * 释放buff通用逻辑
     *
     * @param buffs          buff挂载者的buff列表
     * @param staticBuff     当前buff的配置
     * @param removeBuffList 即将移除buff的列表
     * @param actingForce    buff挂载者
     * @param heroId         buff挂载者武将id
     * @param contextHolder  战斗上下文
     * @return
     */
    public IFightBuff releaseBuff(LinkedList<IFightBuff> buffs, StaticBuff staticBuff,
                                  List<IFightBuff> removeBuffList, Force actingForce, int heroId,
                                  FightContextHolder contextHolder, List<Integer> buffConfig, Object... params) {
        boolean removed;
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
            return null;
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
                            if (!CheckNull.isEmpty(staticBuff.getEffects())) {
                                removed = false;
                                IFightBuff removedBuff;
                                FightBuffEffect fightBuffEffect = actingForce.getFightEffectMap(heroId);
                                for (List<Integer> effectConfig : staticBuff.getEffects()) {
                                    if (CheckNull.isEmpty(effectConfig))
                                        continue;
                                    StaticEffectRule rule = staticFightManager.getStaticEffectRule(effectConfig.get(2));
                                    if (CheckNull.isNull(rule))
                                        continue;
                                    IFightEffect fightEffect = fightManager.getSkillEffect(rule.getEffectLogicId());
                                    if (CheckNull.isNull(fightEffect)) continue;
                                    if ((removedBuff = fightEffect.compareTo(sameIdBuffList, effectConfig, fightBuffEffect, contextHolder)) != null) {
                                        removeBuffList.add(removedBuff);
                                        removed = true;
                                        break;
                                    }
                                }
                                if (!removed) addBuff = false;
                            }
                            break;
                    }
                }
            }
        }

        if (!addBuff)
            return null;

        IFightBuff fightBuff = fightManager.createFightBuff(staticBuff.getBuffEffectiveWay(), staticBuff);
        fightBuff.setForce(actingForce);
        fightBuff.setBuffGiver(contextHolder.getContext().getAttacker());
        fightBuff.setForceId(heroId);

        fightBuff.releaseBuff(buffs, contextHolder, buffConfig, params);
        if (!CheckNull.isEmpty(removeBuffList)) {
            buffs.removeAll(removeBuffList);
            // TODO 预留buff失效还原逻辑
            removeBuffList.forEach(buff -> buff.buffLoseEffectiveness(contextHolder, params));
            // TODO 客户端表现PB 处理
            removeBuffList.clear();
        }
        return fightBuff;
    }
}
