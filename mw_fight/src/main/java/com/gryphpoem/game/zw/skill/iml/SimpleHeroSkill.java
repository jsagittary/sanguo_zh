package com.gryphpoem.game.zw.skill.iml;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.BattleLogic;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.skill.abs.AbstractHeroSkill;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description: 通用技能实现
 * Author: zhangpeng
 * createTime: 2022-10-20 17:08
 */
public class SimpleHeroSkill extends AbstractHeroSkill {

    /**
     * 技能唯一id
     */
    private long skillKeyId;
    /**
     * 技能当前能量值
     */
    private int curEnergy;
    /**
     * 是否是登场技能
     */
    private boolean isOnStageSkill = false;

    public SimpleHeroSkill(StaticHeroSkill s_skill) {
        super(s_skill);
        this.skillKeyId = FightUtil.uniqueId();
        this.curEnergy = s_skill.getDebutEnergy();
    }

    public SimpleHeroSkill(StaticHeroSkill s_skill, boolean isOnStageSkill) {
        super(s_skill);
        this.isOnStageSkill = isOnStageSkill;
        this.curEnergy = s_skill.getDebutEnergy();
    }

    public boolean isOnStageSkill() {
        return isOnStageSkill;
    }

    public int getCurEnergy() {
        return curEnergy;
    }

    public void setCurEnergy(int curEnergy) {
        if (curEnergy < 0) {
            this.curEnergy = 0;
            return;
        }
        if (curEnergy > s_skill.getEnergyUpperLimit()) {
            this.curEnergy = s_skill.getEnergyUpperLimit();
            return;
        }
        this.curEnergy = curEnergy;
    }

    @Override
    public void releaseSkillEffect(FightContextHolder contextHolder, Object... params) {
        // TODO 计算技能伤害

    }

    @Override
    public void releaseSkillBuff(FightContextHolder contextHolder, Object... params) {
        if (CheckNull.isNull(s_skill)) {
            // 技能配置为空
            LogUtil.error("skill config is null, activeBuffImpl");
            return;
        }

        // 添加技能的buff
        if (!CheckNull.isEmpty(s_skill.getBuff())) {
            LinkedList<IFightBuff> removeBuffList = new LinkedList<>();
            StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
            BattleLogic battleLogic = DataResource.ac.getBean(BattleLogic.class);

            // 释放buff
            for (List<Integer> buffConfig : s_skill.getBuff()) {
                // 概率释放
                if (!RandomHelper.isHitRangeIn10000(buffConfig.get(2)))
                    continue;
                FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(buffConfig.get(0));
                if (CheckNull.isNull(buffObjective))
                    continue;
                StaticBuff staticBuff = staticFightManager.getStaticBuff(buffConfig.get(1));
                if (CheckNull.isNull(staticBuff))
                    continue;

                Force actingForce = FightUtil.actingForce(contextHolder.getAttacker(), contextHolder.getDefender(), buffObjective, true);
                Map<Integer, LinkedList<IFightBuff>> buffMap = FightUtil.actingForceBuff(actingForce, buffObjective);
                if (CheckNull.isNull(buffMap)) {
                    continue;
                }

                for (Map.Entry<Integer, LinkedList<IFightBuff>> buffsEntry : buffMap.entrySet()) {
                    LinkedList<IFightBuff> buffs = buffsEntry.getValue();
                    // 释放buff
                    battleLogic.releaseBuff(buffs, staticBuff, removeBuffList,
                            actingForce, buffsEntry.getKey(), contextHolder, buffConfig);
                }
            }
        }
    }

    @Override
    public long uniqueId() {
        return this.skillKeyId;
    }

    @Override
    public void releaseSkill(FightContextHolder contextHolder, Object... params) {
        super.releaseSkill(contextHolder, params);
        this.curEnergy = this.curEnergy - this.s_skill.getReleaseNeedEnergy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleHeroSkill that = (SimpleHeroSkill) o;
        return skillKeyId == that.skillKeyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillKeyId);
    }
}
