package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 18:25
 */
public class StaticDrawHeoPlan implements GamePb<CommonPb.DrawCardPlan.Builder> {
    private int id;
    private String name;
    private Date previewTime;
    private Date beginTime;
    private Date endTime;
    private int searchTypeId;
    private List<List<Integer>> serverIdList;
    private int functionId;
    /**
     * 预显示时间天数(与开服时间间隔)
     */
    private int previewDays;
    /**
     * 开始时间天数(与开服时间间隔)
     */
    private int openBeginDays;
    /**
     * 活动开放时间天数(与开服时间间隔)
     */
    private int openDuration;

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
     * 初始化数据
     *
     * @return
     */
    public boolean initPlan() {
        if (openBeginDays > 0) {
            if (Objects.nonNull(this.beginTime)) {
                LogUtil.error(String.format("check draw hero plan data, plan:%s", this));
            }
            if (openDuration <= 0)
                return false;
            Date openTime = DateHelper.parseDate(DataResource.ac.getBean(ServerSetting.class).getOpenTime());
            if (CheckNull.isNull(openTime))
                return false;
            // 获取活动预显示时间
            this.previewTime = TimeHelper.getSomeDayAfterOrBerfore(openTime, previewDays - 1, 0, 0, 0);
            // 获取活动开启时间   = （开服后的天数-1） + 开服时间
            this.beginTime = TimeHelper.getSomeDayAfterOrBerfore(openTime, openBeginDays - 1, 0, 0, 0);
            // 活动结束时间  = 开始时间 + （活动时长 - 1）
            this.endTime = TimeHelper.getSomeDayAfterOrBerfore(beginTime, openDuration - 1, 23, 59, 59);
        } else {
            return Objects.nonNull(beginTime) && Objects.nonNull(endTime);
        }
        return true;
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
    public String toString() {
        return "StaticDrawHeoPlan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", previewTime=" + previewTime +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", searchTypeId=" + searchTypeId +
                ", serverIdList=" + serverIdList +
                ", functionId=" + functionId +
                ", previewDays=" + previewDays +
                ", openBeginDays=" + openBeginDays +
                ", openDuration=" + openDuration +
                '}';
    }

    @Override
    public CommonPb.DrawCardPlan.Builder createPb(boolean isSaveDb) {
        CommonPb.DrawCardPlan.Builder builder = CommonPb.DrawCardPlan.newBuilder();
        builder.setFunctionId(functionId);
        builder.setName(name);
        builder.setBeginTime(CheckNull.isNull(beginTime) ? 0 : (int) (beginTime.getTime() / 1000l));
        builder.setEndTime(CheckNull.isNull(endTime) ? 0 : (int) (endTime.getTime() / 1000l));
        builder.setOpen(true);
        builder.setKeyId(id);
        builder.setPreviewTime(CheckNull.isNull(previewTime) ? 0 : (int) (previewTime.getTime() / 1000l));
        builder.setSearchTypeId(searchTypeId);
        return builder;
    }
}
