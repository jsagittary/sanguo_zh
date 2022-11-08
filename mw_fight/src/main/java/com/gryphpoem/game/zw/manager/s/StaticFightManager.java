package com.gryphpoem.game.zw.manager.s;

import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticEffectRule;
import com.gryphpoem.game.zw.data.s.StaticFightEffect;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;

import java.util.HashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-22 20:49
 */
public class StaticFightManager {
    private static HashMap<Integer, StaticBuff> buffHashMap = new HashMap<>();
    private static HashMap<Integer, StaticHeroSkill> heroSkillHashMap = new HashMap<>();
    private static HashMap<Integer, StaticFightEffect> effectHashMap = new HashMap<>();
    private static HashMap<Integer, StaticEffectRule> effectRuleHashMap = new HashMap<>();

    public static StaticBuff getStaticBuff(int id) {
        return buffHashMap.get(id);
    }

    public static StaticFightEffect getStaticFightEffect(int id) {
        return effectHashMap.get(id);
    }

    public static StaticEffectRule getStaticEffectRule(int id) {
        return effectRuleHashMap.get(id);
    }

    public static StaticHeroSkill getStaticHeroSkill(int id) {
        return heroSkillHashMap.get(id);
    }
}
