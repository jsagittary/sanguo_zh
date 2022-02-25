package com.gryphpoem.game.zw.resource.domain.p;

public class UploadCrossDataType {
    /**
     * 更新玩家数据
     */
    private int mainType;
    /**
     * 更新玩家数据子类型
     */
    private int subType;

    public int getMainType() {
        return mainType;
    }

    public void setMainType(int mainType) {
        this.mainType = mainType;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public UploadCrossDataType(int mainType, int subType) {
        this.mainType = mainType;
        this.subType = subType;
    }

}
