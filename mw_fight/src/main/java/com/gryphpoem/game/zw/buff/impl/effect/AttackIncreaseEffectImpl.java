package com.gryphpoem.game.zw.buff.impl.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.abs.effect.AbsFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.data.p.EffectValueData;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.FightEffectData;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: 攻击提升
 * Author: zhangpeng
 * createTime: 2022-11-01 16:46
 */
public class AttackIncreaseEffectImpl extends AbsFightEffect {
    @Override
    public int effectType() {
        return FightConstant.EffectType.ATTACK_INCREASE;
    }

    @Override
    public Object calEffectValue(IFightBuff fightBuff, FightContextHolder contextHolder, Object... params) {
        return null;
    }

    @Override
    public EffectValueData compareTo(EffectValueData e1, EffectValueData e2) {
        return null;
    }

    @Override
    public List<Integer> effectCalculateValue(FightContextHolder contextHolder, Object... params) {
        Force attacker = contextHolder.getContext().getAttacker();
        if (CheckNull.isEmpty(attacker.getFightEffectMap(attacker.actionId).getEffectMap()))
            return null;

        int fixValue = 0;
        int tenThousandthRatio = 0;
        Set<Integer> buffIdSet = new HashSet<>();
        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        List<FightEffectData> list_ = attacker.getFightEffectMap(attacker.actionId).getEffectMap().get(effectType());
        for (FightEffectData tmp : list_) {
            if (CheckNull.isNull(tmp)) continue;
            StaticEffectRule rule = staticFightManager.getStaticEffectRule(tmp.getBuffId());
            if (CheckNull.isNull(rule)) continue;
            buffIdSet.add(tmp.getBuffId());
            int ruleId = buffIdSet.contains(tmp.getBuffId()) ? rule.getSameBuffRule() : rule.getDiffBuffRule();
            switch (ruleId) {
                case 1:
                    if ((attacker.calcAttack() * (1 + (tmp.getData().get(0) /
                            FightConstant.TEN_THOUSAND)) + tmp.getData().get(1)) > (attacker.calcAttack() * (1 + (tenThousandthRatio / FightConstant.TEN_THOUSAND)) + fixValue)) {
                        tenThousandthRatio = tmp.getData().get(0);
                        fixValue = tmp.getData().get(1);
                    }
                    break;
                case 2:
                    tenThousandthRatio += tmp.getData().get(0);
                    fixValue += tmp.getData().get(1);
                    break;
                default:
                    break;
            }
        }

        List<Integer> data = new ArrayList<>(2);
        data.add(tenThousandthRatio);
        data.add(fixValue);
        return data;
    }
}
