package com.gryphpoem.game.zw.resource.domain.s;

import lombok.Data;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-01 18:51
 */
@Data
public class StaticEffectRule {
    /**
     * 效果id
     */
    private int effectId;
    /**
     * 效果逻辑id
     */
    private int effectLogicId;
    /**
     * 相同buff来源规则
     */
    private int sameBuffRule;
    /**
     * 不同buff来源规则
     */
    private int diffBuffRule;

    public int getSameBuffRule() {
        if (this.sameBuffRule == 0) return 2;
        return sameBuffRule;
    }

    public int getDiffBuffRule() {
        if (this.diffBuffRule == 0) return 2;
        return diffBuffRule;
    }
}
