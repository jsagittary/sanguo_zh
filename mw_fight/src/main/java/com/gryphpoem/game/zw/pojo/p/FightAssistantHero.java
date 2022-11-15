package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Description: 副将战斗信息
 * Author: zhangpeng
 * createTime: 2022-10-22 17:30
 */
@Data
public class FightAssistantHero {
    private int heroId;
    /**
     * 兵种类型
     */
    private int armType;
    private int intensifyLv; // 兵种强化等级
    private AttrData attrData;
    private List<SimpleHeroSkill> skillList;
    private LinkedList<IFightBuff> buffList = new LinkedList<>();
    /**
     * 战斗中的buff与效果
     */
    private FightBuffEffect fightBuffEffect;
    /**
     * 普攻伤害
     */
    private int attackDamage;
    // 普攻次数
    public int attackCount;

    public FightAssistantHero(Force force, int heroId, AttrData attrData, List<SimpleHeroSkill> skillList) {
        this.heroId = heroId;
        this.attrData = attrData;
        this.skillList = skillList;
        this.fightBuffEffect = new FightBuffEffect(force, heroId);
    }
}
