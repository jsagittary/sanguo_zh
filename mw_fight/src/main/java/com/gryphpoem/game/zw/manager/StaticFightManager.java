package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSkill;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-09 14:11
 */
public class StaticFightManager {
    /**
     * buff配置
     */
    private static Map<Integer, StaticBuff> buffHashMap = new HashMap<>();
    /**
     * 技能配置
     */
    private static Map<Integer, Map<Integer, StaticHeroSkill>> heroSkillMap = new HashMap<>();
    /**
     * 效果规则配置
     */
    private static Map<Integer, StaticEffectRule> effectRuleHashMap = new HashMap<>();

    public static StaticHeroSkill getHeroSkill(int skillGroupId, int lv) {
        Map<Integer, StaticHeroSkill> lvMap;
        if ((lvMap = heroSkillMap.get(skillGroupId)) != null)
            return lvMap.get(lv);
        return null;
    }

    public static StaticBuff getStaticBuff(int id) {
        return buffHashMap.get(id);
    }

    public static StaticEffectRule getStaticEffectRule(int id) {
        return effectRuleHashMap.get(id);
    }

    public static void setBuffHashMap(Map<Integer, StaticBuff> buffHashMap) {
        StaticFightManager.buffHashMap = buffHashMap;
    }

    public static void setHeroSkillMap(Map<Integer, Map<Integer, StaticHeroSkill>> heroSkillMap) {
        StaticFightManager.heroSkillMap = heroSkillMap;
    }

    public static void setEffectRuleMap(Map<Integer, StaticEffectRule> effectRuleHashMap) {
        StaticFightManager.effectRuleHashMap = effectRuleHashMap;
    }
}
