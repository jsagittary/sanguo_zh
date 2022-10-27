package com.gryphpoem.game.zw.pojo.p;

import lombok.Data;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-27 15:00
 */
@Data
public class ActingForce {
    private Force force;
    /**
     * 是否是主将
     */
    private boolean isPrincipalGeneral;

    public boolean isTrigger() {
        if (isPrincipalGeneral) {
            return force.actionId == force.id;
        } else {
            return force.actionId != force.id;
        }
    }
}
