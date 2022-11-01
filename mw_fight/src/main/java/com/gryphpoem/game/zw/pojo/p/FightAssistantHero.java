package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.skill.IHeroSkill;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-22 17:30
 */
@Data
public class FightAssistantHero {
    private int heroId;
    /**
     * 兵种类型
     */
    public int armType;
    private AttrData attrData;
    private List<IHeroSkill> skillList;
    private LinkedList<IFightBuff> buffList = new LinkedList<>();
    /**
     * 战斗中的buff与效果
     */
    private FightBuffEffect fightBuffEffect;

    public FightAssistantHero(int heroId, AttrData attrData, List<IHeroSkill> skillList) {
        this.heroId = heroId;
        this.attrData = attrData;
        this.skillList = skillList;
        this.fightBuffEffect = new FightBuffEffect();
    }
}
