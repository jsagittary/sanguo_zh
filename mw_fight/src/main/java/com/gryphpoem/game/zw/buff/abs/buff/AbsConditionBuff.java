package com.gryphpoem.game.zw.buff.abs.buff;

/**
 * Description: 条件触发buff
 * Author: zhangpeng
 * createTime: 2022-10-21 9:56
 */
public abstract class AbsConditionBuff extends AbsConfigBuff {
    protected boolean effect;

    public boolean isEffect() {
        return effect;
    }

    public void setEffect(boolean effect) {
        this.effect = effect;
    }
}
