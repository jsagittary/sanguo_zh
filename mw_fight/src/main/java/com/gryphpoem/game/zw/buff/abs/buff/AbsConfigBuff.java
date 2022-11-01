package com.gryphpoem.game.zw.buff.abs.buff;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Description: 带有buff配置的buff抽象类
 * Author: zhangpeng
 * createTime: 2022-10-21 13:37
 */
public abstract class AbsConfigBuff implements IFightBuff {
    /**
     * buff唯一id(运行时)
     */
    protected long buffKeyId;
    /**
     * buff 拥有者
     */
    protected Force force;
    /**
     * buff 施予方
     */
    protected Force buffGiver;
    /**
     * buff拥有者id
     */
    protected int forceId;
    /**
     * buff施予方id
     */
    protected int buffGiverId;
    /**
     * buff配置
     */
    protected StaticBuff staticBuff;
    /**
     * buff 回合数
     */
    protected int buffEffectiveRounds;
    /**
     * buff生效次数
     */
    protected int buffEffectiveTimes;

    public AbsConfigBuff() {
    }

    public AbsConfigBuff(StaticBuff staticBuff) {
        this.buffKeyId = FightUtil.uniqueId();
        this.staticBuff = staticBuff;
        this.buffEffectiveRounds = this.staticBuff.getContinuousRound();
    }

    @Override
    public StaticBuff getBuffConfig() {
        return staticBuff;
    }

    @Override
    public void deductBuffRounds() {
        if (this.buffEffectiveRounds == -1)
            return;
        this.buffEffectiveRounds--;
    }

    @Override
    public long uniqueId() {
        return this.buffKeyId;
    }

    @Override
    public boolean buffCoexistenceCheck(StaticBuff targetBuff, List removeBuff) {
        if (CheckNull.isNull(targetBuff) || CheckNull.isNull(this.staticBuff))
            return true;

        if (targetBuff.getBuffId() == this.staticBuff.getBuffId()) {
            // buffId相同不在这里校验
            return true;
        } else {
            if (!CheckNull.isEmpty(targetBuff.getNotCoExistGroup()) && !CheckNull.isEmpty(staticBuff.getTypeGrouping())) {
                for (Integer id : targetBuff.getNotCoExistGroup()) {
                    if (CheckNull.isNull(id)) continue;
                    if (!this.staticBuff.getTypeGrouping().contains(id) ||
                            targetBuff.getPriority() == this.staticBuff.getPriority()) continue;
                    if (targetBuff.getPriority() < this.staticBuff.getPriority())
                        return false;
                    removeBuff.add(this);
                }
            }
            if (!CheckNull.isEmpty(targetBuff.getTypeGrouping()) && !CheckNull.isEmpty(staticBuff.getNotCoExistGroup())) {
                for (Integer id : targetBuff.getTypeGrouping()) {
                    if (CheckNull.isNull(id)) continue;
                    if (!this.staticBuff.getNotCoExistGroup().contains(id) ||
                            targetBuff.getPriority() == this.staticBuff.getPriority()) continue;
                    if (targetBuff.getPriority() < this.staticBuff.getPriority())
                        return false;
                    removeBuff.add(this);
                }
            }
        }

        return true;
    }

    @Override
    public boolean hasRemainBuffTimes(FightContextHolder contextHolder, Object... params) {
        if (this.buffEffectiveRounds == -1) {
            return this.staticBuff.getBuffEffectiveTimes() > 0 && this.buffEffectiveTimes < this.staticBuff.getBuffEffectiveTimes();
        } else {
            return this.buffEffectiveRounds > 0 && this.staticBuff.getBuffEffectiveTimes() > 0 && this.buffEffectiveTimes < this.staticBuff.getBuffEffectiveTimes();
        }
    }

    @Override
    public int getBuffEffectiveRounds() {
        return buffEffectiveRounds;
    }

    @Override
    public int getForceId() {
        return this.forceId;
    }

    @Override
    public void setForceId(int heroId) {
        this.forceId = heroId;
    }

    @Override
    public int getBuffGiverId() {
        return this.buffGiverId;
    }

    @Override
    public Force getForce() {
        return force;
    }

    @Override
    public void setForce(Force force) {
        this.force = force;
    }

    @Override
    public void setBuffGiver(Force force) {
        this.buffGiver = force;
        this.buffGiverId = force.actionId;
    }

    @Override
    public Force getBuffGiver() {
        return buffGiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsConfigBuff that = (AbsConfigBuff) o;
        return buffKeyId == that.buffKeyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buffKeyId);
    }

    @Override
    public void releaseBuff(LinkedList actingBuffList, FightContextHolder contextHolder, List staticBuffConfig, Object... params) {
        actingBuffList.add(this);
    }

    /**
     * 释放buff所有效果通用逻辑
     *
     * @param contextHolder
     * @param params
     */
    protected void releaseBuffEffect(FightContextHolder contextHolder, Object... params) {
        if (CheckNull.isNull(staticBuff) || CheckNull.isEmpty(staticBuff.getEffects())) return;
        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        for (List<Integer> effectList : staticBuff.getEffects()) {
            if (CheckNull.isEmpty(effectList)) continue;
            IFightEffect fightEffect = fightManager.getSkillEffect(effectList.get(0));
            if (CheckNull.isNull(fightEffect) || staticFightManager.getStaticFightEffect(effectList.get(0)) == null)
                continue;

            fightEffect.effectiveness(this, contextHolder, effectList, params);
        }
    }

    /**
     * 还原buff所有效果通用逻辑
     *
     * @param contextHolder
     * @param params
     */
    protected void buffEffectiveness(FightContextHolder contextHolder, Object... params) {
        if (CheckNull.isNull(staticBuff) || CheckNull.isEmpty(staticBuff.getEffects())) return;
        StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        for (List<Integer> effectList : staticBuff.getEffects()) {
            if (CheckNull.isEmpty(effectList)) continue;
            IFightEffect fightEffect = fightManager.getSkillEffect(effectList.get(0));
            if (CheckNull.isNull(fightEffect) || staticFightManager.getStaticFightEffect(effectList.get(0)) == null)
                continue;
            fightEffect.effectRestoration(this, contextHolder, effectList, params);
        }
    }
}
