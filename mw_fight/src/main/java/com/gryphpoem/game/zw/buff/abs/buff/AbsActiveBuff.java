package com.gryphpoem.game.zw.buff.abs.buff;

/**
 * Description: 主动buff 释放当前回合不生效, 下回合生效
 * Author: zhangpeng
 * createTime: 2022-10-21 12:06
 */
public abstract class AbsActiveBuff extends AbsConfigBuff {
    protected boolean effect;

    public boolean isEffect() {
        return effect;
    }

    public void setEffect(boolean effect) {
        this.effect = effect;
    }
}
