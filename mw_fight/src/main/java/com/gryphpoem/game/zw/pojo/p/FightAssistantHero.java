package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.skill.IHeroSkill;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-22 17:30
 */
@Data
public class FightAssistantHero {
    private int heroId;
    private AttrData attrData;
    private List<IHeroSkill> skillList;

    public FightAssistantHero(int heroId, AttrData attrData, List<IHeroSkill> skillList) {
        this.heroId = heroId;
        this.attrData = attrData;
        this.skillList = skillList;
    }
}
