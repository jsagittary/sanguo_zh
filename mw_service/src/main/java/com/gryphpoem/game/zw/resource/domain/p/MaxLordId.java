package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName MaxLordId.java
 * @Description 最大lordId
 * @author QiuKun
 * @date 2017年11月6日
 */
public class MaxLordId {
    private int platNo;// 平台id
    private long maxLordId;// 最大的

    public int getPlatNo() {
        return platNo;
    }

    public void setPlatNo(int platNo) {
        this.platNo = platNo;
    }

    public long getMaxLordId() {
        return maxLordId;
    }

    public void setMaxLordId(long maxLordId) {
        this.maxLordId = maxLordId;
    }

    @Override
    public String toString() {
        return "MaxLordId [platNo=" + platNo + ", maxLordId=" + maxLordId + "]";
    }

}
