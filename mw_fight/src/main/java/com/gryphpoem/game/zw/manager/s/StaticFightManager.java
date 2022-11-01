package com.gryphpoem.game.zw.manager.s;

import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.data.s.StaticFightEffect;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-22 20:49
 */
@Component
public class StaticFightManager {
    private HashMap<Integer, StaticBuff> buffHashMap = new HashMap<>();
    private HashMap<Integer, StaticFightEffect> effectHashMap = new HashMap<>();
    private HashMap<Integer, StaticEffectRule> effectRuleHashMap = new HashMap<>();

    public StaticBuff getStaticBuff(int id) {
        return buffHashMap.get(id);
    }

    public StaticFightEffect getStaticFightEffect(int id) {
        return effectHashMap.get(id);
    }

    public StaticEffectRule getStaticEffectRule(int id) {
        return effectRuleHashMap.get(id);
    }
}
