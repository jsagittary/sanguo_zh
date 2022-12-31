package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-09 13:36
 */
@Component
public class StaticFightDataMgr extends AbsStaticIniService {
    @Override
    public void load() {
        StaticFightManager.setBuffHashMap(staticDataDao.selectBattleBuffMap());
        StaticFightManager.setEffectRuleMap(staticDataDao.selectEffectRule());
        List<StaticHeroSkill> skillList = staticDataDao.selectHeroSkill();
        if (!CheckNull.isEmpty(skillList)) {
            Map<Integer, Map<Integer, StaticHeroSkill>> heroSkillMap = new HashMap<>();
            skillList.forEach(staticHeroSkill -> {
                heroSkillMap.computeIfAbsent(staticHeroSkill.getSkillGroupId(), m -> new HashMap<>()).
                        put(staticHeroSkill.getLevel(), staticHeroSkill);
            });
            StaticFightManager.setHeroSkillMap(heroSkillMap);
        }
    }

    @Override
    public void check() {

    }

    public static StaticSkillAction getSkillAction(int id) {
        return null;
    }
}
