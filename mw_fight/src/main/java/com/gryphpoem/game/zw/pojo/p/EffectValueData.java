package com.gryphpoem.game.zw.pojo.p;

import lombok.Data;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-26 17:15
 */
@Data
public class EffectValueData<T> {
    private long keyId;
    private T value;

    public void clear() {
        this.keyId = 0;
        this.value = null;
    }
}
