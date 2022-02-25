package com.gryphpoem.game.zw.resource.pojo.global;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-22 11:08
 * @description: 世界进度的目标
 * @modified By:
 */
public class ScheduleGoal {

    /**
     * id
     */
    private int id;

    /**
     * 领取记录, key=roleId, val = 0 未领取, 1 已领取
     */
    private Map<Long, Integer> statusMap = new HashMap<>(500);

    /**
     * 未领取
     */
    public static final int UN_RECEIVED = 0;

    /**
     * 已领取
     */
    public static final int ALREADY_RECEIVED = 1;


    public Map<Long, Integer> getStatusMap() {
        return statusMap;
    }

    public int getId() {
        return id;
    }

    public ScheduleGoal() {
    }

    public ScheduleGoal(int goalId) {
        this();
        this.id = goalId;
    }

    /**
     * 序列化
     * @return
     */
    public CommonPb.ScheduleGoal ser(Player player) {
        CommonPb.ScheduleGoal.Builder builder = CommonPb.ScheduleGoal.newBuilder();
        builder.setId(this.id);
        if (!CheckNull.isNull(this.statusMap)) {
            if (!CheckNull.isNull(player)) {
                builder.addStatus(PbHelper.createLongIntPb(player.roleId, this.statusMap.getOrDefault(player.roleId, UN_RECEIVED)));
            } else {
                for (Map.Entry<Long, Integer> en : this.statusMap.entrySet()) {
                    builder.addStatus(PbHelper.createLongIntPb(en.getKey(), en.getValue()));
                }
            }
        }
        return builder.build();
    }

    /**
     * 反序列化
     * @param scheduleGoal
     */
    public ScheduleGoal(CommonPb.ScheduleGoal scheduleGoal) {
        this();
        this.id = scheduleGoal.getId();
        List<CommonPb.LongInt> statusList = scheduleGoal.getStatusList();
        if (!CheckNull.isEmpty(statusList)) {
            for (CommonPb.LongInt longInt : statusList) {
                statusMap.put(longInt.getV1(), longInt.getV2());
            }
        }
    }

    /**
     * 记录领奖
     * @param roleId
     */
    public void receiveAward(long roleId) {
        this.statusMap.put(roleId, ALREADY_RECEIVED);
    }
}
