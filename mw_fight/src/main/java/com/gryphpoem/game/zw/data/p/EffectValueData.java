package com.gryphpoem.game.zw.data.p;

import lombok.Data;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-26 17:15
 */
@Data
public class EffectValueData {
    private long keyId;
    private Object value;

    public void clear() {
        this.keyId = 0;
        this.value = null;
    }
}
