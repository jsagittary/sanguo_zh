package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticSkillAction;
import com.gryphpoem.game.zw.resource.pojo.fight.skill.FightSkillUtils;
import com.gryphpoem.game.zw.resource.pojo.fight.skill.SkillTriggerUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-20 10:40
 */
@Service
public class SkillService {

    /**
     * 技能配置检测
     */
    public void checkSkillConfig() throws MwException {
        Map<Integer, StaticSkillAction> skillActionMap = StaticFightDataMgr.getSkillActionMap();
        for (StaticSkillAction s_skill : skillActionMap.values()) {
            //triggerParam 检测
            if (!SkillTriggerUtils.checkSkillTriggerConfig(s_skill)) {
                throw new MwException(String.format("s_skill_action id :%d, triggerCond :%d, triggerParam error !!!", s_skill.getId(), s_skill.getTriggerCond()));
            }
            //技能效果配置参数
            if (!FightSkillUtils.checkSkillEffConfig(s_skill)) {
                throw new MwException(String.format("s_skill_action id :%d, effect type :%d, effectParam error !!!", s_skill.getId(), s_skill.getEffect()));
            }

        }
    }

}
