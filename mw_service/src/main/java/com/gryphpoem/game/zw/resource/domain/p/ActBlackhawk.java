package com.gryphpoem.game.zw.resource.domain.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.PbHelper;

/**
 * @ClassName ActBlackhawk.java
 * @Description 黑鹰计划活动
 * @author QiuKun
 * @date 2017年7月10日
 */
public class ActBlackhawk {

    /** 上一次refreshCount值变动的时间 */
    private int refreshTime;

    /** 付费刷新次数 */
    private int payRefreshCount;

    /** 免费刷新剩余次数 */
    private int refreshCount;

    /** 是否已经招募过将领 , true为已经招募过 */
    private boolean isRecvHero;

    /** key:活动的 keyId ;val:每个格子数据 */
    private Map<Integer, ActBlackhawkItem> blackhawkItemMap = new HashMap<>();

    public ActBlackhawk() {
    }

    public ActBlackhawk(CommonPb.ActBlackhawk ser) {
        this();
        setRefreshTime(ser.getRefreshTime());
        setPayRefreshCount(ser.getPayRefreshCount());
        setRefreshCount(ser.getRefreshCount());
        setRecvHero(ser.getIsRecvHero());
        for (CommonPb.BlackhawkItem item : ser.getBlackhawkItemsList()) {
            blackhawkItemMap.put(item.getKeyId(), new ActBlackhawkItem(item));
        }
    }

    /**
     * 反序列化
     * 
     * @return
     */
    public CommonPb.ActBlackhawk dser() {
        CommonPb.ActBlackhawk.Builder builder = CommonPb.ActBlackhawk.newBuilder();
        builder.setRefreshTime(refreshTime);
        builder.setPayRefreshCount(payRefreshCount);
        builder.setRefreshCount(refreshCount);
        builder.setIsRecvHero(isRecvHero);
        List<CommonPb.BlackhawkItem> items = new ArrayList<>();
        for (ActBlackhawkItem item : blackhawkItemMap.values()) {
            items.add(PbHelper.createBlackhawkItem(item));
        }
        builder.addAllBlackhawkItems(items);
        return builder.build();
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public int getPayRefreshCount() {
        return payRefreshCount;
    }

    public void setPayRefreshCount(int payRefreshCount) {
        this.payRefreshCount = payRefreshCount;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public void setRefreshCount(int refreshCount) {
        this.refreshCount = refreshCount;
    }

    public boolean isRecvHero() {
        return isRecvHero;
    }

    public void setRecvHero(boolean isRecvHero) {
        this.isRecvHero = isRecvHero;
    }

    public Map<Integer, ActBlackhawkItem> getBlackhawkItemMap() {
        return blackhawkItemMap;
    }

    public void setBlackhawkItemMap(Map<Integer, ActBlackhawkItem> blackhawkItemMap) {
        this.blackhawkItemMap = blackhawkItemMap;
    }

}
