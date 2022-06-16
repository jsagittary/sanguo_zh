package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Date;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 18:25
 */
public class StaticDrawHeoPlan implements GamePb<CommonPb.DrawCardPlan> {
    private int id;
    private String name;
    private Date previewTime;
    private Date beginTime;
    private Date endTime;
    private int searchTypeId;
    private List<List<Integer>> serverIdList;
    private int functionId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getPreviewTime() {
        return previewTime;
    }

    public void setPreviewTime(Date previewTime) {
        this.previewTime = previewTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getSearchTypeId() {
        return searchTypeId;
    }

    public void setSearchTypeId(int searchTypeId) {
        this.searchTypeId = searchTypeId;
    }

    public List<List<Integer>> getServerIdList() {
        return serverIdList;
    }

    public void setServerIdList(List<List<Integer>> serverIdList) {
        this.serverIdList = serverIdList;
    }

    public int getFunctionId() {
        return functionId;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取活动状态
     *
     * @param now
     * @return
     */
    public PlanFunction.PlanStatus planStatus(Date now) {
        if (CheckNull.isNull(this.previewTime) || CheckNull.isNull(this.beginTime) || CheckNull.isNull(this.endTime)) {
            return PlanFunction.PlanStatus.NOT_START;
        }
        if (now.before(previewTime))
            return PlanFunction.PlanStatus.NOT_START;
        if (now.after(previewTime) && now.before(beginTime))
            return PlanFunction.PlanStatus.PREVIEW;
        if (now.after(beginTime) && now.before(endTime))
            return PlanFunction.PlanStatus.OPEN;
        return PlanFunction.PlanStatus.OVER;
    }

    @Override
    public CommonPb.DrawCardPlan createPb(boolean isSaveDb) {
        CommonPb.DrawCardPlan.Builder builder = CommonPb.DrawCardPlan.newBuilder();
        builder.setFunctionId(functionId);
        builder.setName(name);
        builder.setBeginTime(CheckNull.isNull(beginTime) ? 0 : (int) (beginTime.getTime() / 1000l));
        builder.setEndTime(CheckNull.isNull(endTime) ? 0 : (int) (endTime.getTime() / 1000l));
        builder.setOpen(true);
        builder.setKeyId(id);
        builder.setPreviewTime(CheckNull.isNull(previewTime) ? 0 : (int) (previewTime.getTime() / 1000l));
        return builder.build();
    }
}
