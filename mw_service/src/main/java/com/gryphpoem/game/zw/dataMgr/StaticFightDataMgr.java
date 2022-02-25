package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;

import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 14:26
 * @description: 战斗相关配置
 * @modified By:
 */
public class StaticFightDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 战斗技能
    private static Map<Integer, StaticFightSkill> fightSkillMap;
    // 战斗buff配置
    private static Map<Integer, StaticFightBuff> fightBuffMap;

    private static Map<Integer, StaticSkillAction> skillActionMap;

    public static void init() {
        StaticFightDataMgr.fightSkillMap = staticDataDao.selectFightSkill();
        StaticFightDataMgr.fightBuffMap = staticDataDao.selectFightBuff();
        StaticFightDataMgr.skillActionMap = staticDataDao.selectSkillAction();
    }

    public static StaticFightSkill getFightSkillMapById(int id) {
        return fightSkillMap.get(id);
    }

    public static StaticFightBuff getFightBuffMapById(int id) {
        return fightBuffMap.get(id);
    }

    public static StaticSkillAction getSkillAction(int id) {
        return skillActionMap.get(id);
    }

    public static Map<Integer, StaticSkillAction> getSkillActionMap() {
        return skillActionMap;
    }
}
