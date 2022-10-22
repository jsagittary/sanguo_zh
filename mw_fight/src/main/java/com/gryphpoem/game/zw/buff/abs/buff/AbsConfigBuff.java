package com.gryphpoem.game.zw.buff.abs.buff;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.data.s.StaticHeroSkill;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.manager.s.StaticFightManager;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * buff配置
     */
    protected StaticBuff staticBuff;
    /**
     * buff作用次数
     */
    protected int buffActionTimes;
    /**
     * buff效果list
     */
    protected LinkedList<IFightEffect> effectList = new LinkedList<>();

    public AbsConfigBuff() {
    }

    public AbsConfigBuff(StaticBuff staticBuff) {
        this.buffKeyId = FightUtil.uniqueId();
        this.staticBuff = staticBuff;
        this.buffActionTimes = this.staticBuff.getContinuousRound();
    }

    public StaticBuff getStaticBuff() {
        return staticBuff;
    }

    public LinkedList<IFightEffect> getEffectList() {
        return effectList;
    }

    public void setEffectList(LinkedList<IFightEffect> effectList) {
        this.effectList = effectList;
    }

    @Override
    public StaticBuff getBuffConfig() {
        return staticBuff;
    }

    @Override
    public void deductBuffTimes() {
        if (this.buffActionTimes == 999)
            return;
        this.buffActionTimes--;
    }

    @Override
    public long uniqueId() {
        return this.buffKeyId;
    }

    @Override
    public boolean buffCoexistenceCheck(StaticBuff targetBuff, List removeBuff) {
        if (CheckNull.isNull(targetBuff) || CheckNull.isNull(this.staticBuff))
            return true;

        if (!CheckNull.isEmpty(targetBuff.getNotCoExistGroup()) && !CheckNull.isEmpty(staticBuff.getTypeGrouping())) {
            for (Integer id : targetBuff.getNotCoExistGroup()) {
                if (CheckNull.isNull(id)) continue;
                if (!this.staticBuff.getTypeGrouping().contains(id)) continue;
                if (targetBuff.getPriority() < this.staticBuff.getPriority())
                    return false;
                removeBuff.add(this);
            }
        }
        if (!CheckNull.isEmpty(targetBuff.getTypeGrouping()) && !CheckNull.isEmpty(staticBuff.getNotCoExistGroup())) {
            for (Integer id : targetBuff.getTypeGrouping()) {
                if (CheckNull.isNull(id)) continue;
                if (!this.staticBuff.getNotCoExistGroup().contains(id)) continue;
                if (targetBuff.getPriority() < this.staticBuff.getPriority())
                    return false;
                removeBuff.add(this);
            }
        }

        return true;
    }

    @Override
    public FightResult releaseSkill(Force attacker, Force defender, FightLogic fightLogic, StaticHeroSkill staticHeroSkill, Object... params) {
        if (CheckNull.isNull(staticHeroSkill)) {
            // 技能配置为空
            LogUtil.error("skill config is null, activeBuffImpl");
            return null;
        }

        if (!CheckNull.isEmpty(staticHeroSkill.getSkillEffect())) {
            // 释放技能主体效果

        }

        // 添加技能的buff
        if (!CheckNull.isEmpty(staticHeroSkill.getBuff())) {
            LinkedList<IFightBuff> removeBuffList = new LinkedList<>();
            FightManager fightManager = DataResource.ac.getBean(FightManager.class);
            StaticFightManager staticFightManager = DataResource.ac.getBean(StaticFightManager.class);

            // 释放buff
            for (List<Integer> buffConfig : staticHeroSkill.getBuff()) {
                // 概率释放
                if (!RandomHelper.isHitRangeIn10000(buffConfig.get(2)))
                    continue;
                FightConstant.BuffObjective buffObjective = FightConstant.BuffObjective.convertTo(buffConfig.get(0));
                if (CheckNull.isNull(buffObjective))
                    continue;
                StaticBuff staticBuff = staticFightManager.getStaticBuff(buffConfig.get(1));
                if (CheckNull.isNull(staticBuff))
                    continue;

                Map<Integer, LinkedList<IFightBuff>> buffMap = FightUtil.actingForce(attacker, defender, buffObjective);
                for (LinkedList<IFightBuff> buffs : buffMap.values()) {
                    if (CheckNull.isEmpty(buffs)) {
                        buffs.add(fightManager.createFightBuff(staticBuff.getBuffEffectiveWay(), staticBuff));
                        continue;
                    }
                    boolean addBuff = true;
                    for (IFightBuff fightBuff : buffs) {
                        if (CheckNull.isNull(fightBuff)) continue;
                        if (!fightBuff.buffCoexistenceCheck(staticBuff, removeBuffList)) {
                            addBuff = false;
                            break;
                        }
                    }
                    if (!addBuff) {
                        break;
                    }

                    buffs.add(fightManager.createFightBuff(staticBuff.getBuffEffectiveWay(), staticBuff));
                    if (!CheckNull.isEmpty(removeBuffList)) {
                        buffs.removeAll(removeBuffList);
                        // TODO 预留buff失效还原逻辑
                        removeBuffList.forEach(buff -> buff.buffLoseEffectiveness(attacker, defender, fightLogic, params));
                    }
                    // TODO 客户端表现PB 处理
                    removeBuffList.clear();
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasRemainBuffTimes(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        return this.buffActionTimes > 0;
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
}
