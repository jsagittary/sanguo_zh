package com.gryphpoem.game.zw.core.holder;

/**
 * bool值容器
 * 
 * @author
 * @date 2019/4/23 9:32
 */
public class BooleanHolder {
    private boolean value;

    public BooleanHolder(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
