package com.gryphpoem.game.zw.buff.abs.buff;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.data.p.FightResult;
import com.gryphpoem.game.zw.data.s.StaticBuff;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
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
        this.buffEffectiveTimes = this.staticBuff.getBuffEffectiveTimes();
    }

    public StaticBuff getStaticBuff() {
        return staticBuff;
    }

    @Override
    public StaticBuff getBuffConfig() {
        return staticBuff;
    }

    @Override
    public void deductBuffRounds() {
        if (this.buffEffectiveRounds == 999)
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
        }

        return true;
    }

    @Override
    public boolean hasRemainBuffTimes(Force attacker, Force defender, FightLogic fightLogic, Object... params) {
        return this.buffEffectiveRounds > 0 && this.buffEffectiveTimes > 0;
    }

    public Force getForce() {
        return force;
    }

    @Override
    public void setForce(Force force) {
        this.force = force;
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
    public void releaseBuff(LinkedList actingBuffList, FightLogic fightLogic, List staticBuffConfig, FightResult fightResult, Object... params) {
        actingBuffList.add(this);
    }
}
