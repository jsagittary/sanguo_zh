package com.gryphpoem.game.zw.resource.pojo.plan;

import com.gryphpoem.game.zw.resource.pojo.GamePb;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 16:13
 */
public abstract class FunctionPlanData<T> implements GamePb<T> {

    /**
     * 已经领取状态
     */
    public static final int HAS_RECEIVED_STATUS = 2;
    /**
     * 可以领取状态
     */
    public static final int CAN_RECEIVE_STATUS = 1;
    /**
     * 不可领取状态
     */
    public static final int CANNOT_RECEIVE_STATUS = 0;

    /** 功能plan keyId*/
    private int keyId;

    public int getKeyId() {
        return keyId;
    }

    public FunctionPlanData(int keyId) {
        this.keyId = keyId;
    }

    /**
     * 功能类型
     *
     * @return
     */
    public abstract PlanFunction[] functionId();

    @Override
    public T createPb(boolean isSaveDb) {
        return null;
    }
}
