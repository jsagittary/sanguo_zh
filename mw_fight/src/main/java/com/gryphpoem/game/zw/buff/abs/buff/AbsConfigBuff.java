package com.gryphpoem.game.zw.buff.abs.buff;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.data.s.StaticBuff;

import java.util.LinkedList;

/**
 * Description: 带有buff配置的buff抽象类
 * Author: zhangpeng
 * createTime: 2022-10-21 13:37
 */
public abstract class AbsConfigBuff implements IFightBuff {
    /**
     * buff唯一id(运行时)
     */
    protected int buffKeyId;
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
        this.staticBuff = staticBuff;
        this.buffActionTimes = this.staticBuff.getContinuousRound();
    }

    public AbsConfigBuff(int buffKeyId, StaticBuff staticBuff) {
        this.buffKeyId = buffKeyId;
        this.staticBuff = staticBuff;
        this.buffActionTimes = this.staticBuff.getContinuousRound();
    }

    public int getBuffKeyId() {
        return buffKeyId;
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
    public void deductBuffTimes() {
        if (this.buffActionTimes == 999)
            return;
        this.buffActionTimes--;
    }
}
