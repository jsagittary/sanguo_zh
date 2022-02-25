package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pengshuo on 2019/4/13 11:33
 * <br>Description: 巴顿活动
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class ActBarton {
    /** 活动的activityId */
    private int activityId;
    /** 免费刷新剩余次数 */
    private int refreshCount;
    /** key:活动的 keyId ;val:每个格子数据 */
    private Map<Integer, ActBartonItem> bartonItems = new HashMap<>();
    /** 活动开始时间 */
    private int beginTime;

    public ActBarton() {
    }

    public ActBarton(CommonPb.ActBarton barton) {
        this();
        setActivityId(barton.getActivityId());
        setRefreshCount(barton.getRefreshCount());
        for (CommonPb.ActBartonItem e : barton.getItemsList()) {
            bartonItems.put(e.getKeyId(), new ActBartonItem(e));
        }
        setBeginTime(barton.getBeginTime());
    }

    public CommonPb.ActBarton dser() {
        CommonPb.ActBarton.Builder builder = CommonPb.ActBarton.newBuilder();
        builder.setActivityId(activityId);
        builder.setRefreshCount(refreshCount);
        bartonItems.values().forEach(e -> builder.addItems(e.dser()));
        builder.setBeginTime(beginTime);
        return builder.build();
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public void setRefreshCount(int refreshCount) {
        this.refreshCount = refreshCount;
    }

    public Map<Integer, ActBartonItem> getBartonItems() {
        return bartonItems;
    }

    public void setBartonItems(Map<Integer, ActBartonItem> bartonItems) {
        this.bartonItems = bartonItems;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    @Override
    public String toString() {
        return "ActBarton{" +
                "activityId=" + activityId +
                ", refreshCount=" + refreshCount +
                ", bartonItems=" + bartonItems +
                ", beginTime=" + beginTime +
                '}';
    }

}
