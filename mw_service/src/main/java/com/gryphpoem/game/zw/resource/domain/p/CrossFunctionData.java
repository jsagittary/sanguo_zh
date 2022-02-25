package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.Objects;

public abstract class CrossFunctionData {
    /** 是否在跨服中*/
    private volatile boolean inCross;
    /** 玩家所处在的跨服功能*/
    private CrossFunction crossFunction;
    /** 需要更新数据的内容标识*/
    private UploadCrossDataType uploadCrossData;
    /** 离开跨服玩法时间*/
    private int leaveTime;
    /** 用于是否重置数据*/
    private int planKey;

    public int getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(int leaveTime) {
        this.leaveTime = leaveTime;
    }

    public boolean isInCross() {
        return inCross;
    }

    public void setInCross(Boolean inCross) {
        if (CheckNull.isNull(inCross))
            return;

        this.inCross = inCross;
    }

    public CrossFunction getCrossFunction() {
        return crossFunction;
    }

    public void setCrossFunction(CrossFunction crossFunction) {
        if (CheckNull.isNull(crossFunction))
            return;

        this.crossFunction = crossFunction;
    }

    public UploadCrossDataType getUploadCrossData() {
        return uploadCrossData;
    }

    public void setUploadCrossData(UploadCrossDataType uploadCrossData) {
        this.uploadCrossData = uploadCrossData;
    }

    public CommonPb.CrossPlayerFunction createPb(boolean isSave) {
        CommonPb.CrossPlayerFunction.Builder builder = CommonPb.CrossPlayerFunction.newBuilder();
        builder.setFunctionId(crossFunction.getFunctionId());
        builder.setInCross(isInCross());
        if (isSave && Objects.nonNull(uploadCrossData)) builder.setUploadType(PbHelper.createTwoIntPb
                (this.uploadCrossData.getMainType(), this.uploadCrossData.getSubType()));
        builder.setLeaveTime(this.leaveTime);
        builder.setPlanKey(this.planKey);
        return builder.build();
    }

    protected void dseData(CommonPb.CrossPlayerFunction function) {
        this.crossFunction = CrossFunction.convertTo(function.getFunctionId());
        this.inCross = function.getInCross();
        this.uploadCrossData = new UploadCrossDataType(function.getUploadType().getV1(), function.getUploadType().getV2());
        this.leaveTime = function.getLeaveTime();
        this.planKey = function.getPlanKey();
    }

    public CrossFunctionData() {
    }

    public CrossFunctionData(int keyId) {
        this.planKey = keyId;
    }

    public CrossFunctionData(CrossFunction crossFunction, int keyId) {
        this.inCross = false;
        this.crossFunction = crossFunction;
        this.leaveTime = 0;
        this.planKey = keyId;
    }

    public int getPlanKey() {
        return planKey;
    }

    public void setPlanKey(int planKey) {
        this.planKey = planKey;
    }

    public abstract void reset(int keyId);

    @Override
    public String toString() {
        return "CrossPlayerFunction{" +
                "inCross=" + inCross +
                ", crossFunction=" + crossFunction +
                '}';
    }
}