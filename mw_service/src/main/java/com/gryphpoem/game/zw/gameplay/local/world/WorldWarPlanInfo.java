package com.gryphpoem.game.zw.gameplay.local.world;

import java.util.List;

import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.WorldWarPlanInfoPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarPlan;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName WorldWarPlanInfo.java
 * @Description 世界争霸计划数据
 * @author QiuKun
 * @date 2019年3月26日
 */
public class WorldWarPlanInfo {

    /** 未开始,或已经结束 */
    public static final int STAGE_OVER = 0;
    /** 预显示阶段 */
    public static final int STAGE_DISPLAYER = 1;
    /** 赛季运行中 阶段 */
    public static final int STAGE_SEASON_RUNNING = 2;
    /** 结束预览阶段 */
    public static final int STAGE_END_DISPLAYER = 3;

    private int id; // 对应配置id
    private int worldWarType; // 档位
    private int displayBegin;// 预显示时间
    private int beginTime;// 赛季开始的时间
    private int endTime; // 赛季结束时间
    private int displayTime;// 结束时间显示时间

    public static WorldWarPlanInfo createByCfg(StaticWorldWarPlan cfg) {
        WorldWarPlanInfo info = new WorldWarPlanInfo();
        info.setId(cfg.getId());
        info.setWorldWarType(cfg.getWorldWarType());
        info.setDisplayBegin(TimeHelper.dateToSecond(cfg.getDisplayBegin()));
        info.setBeginTime(TimeHelper.dateToSecond(cfg.getBeginTime()));
        info.setEndTime(TimeHelper.dateToSecond(cfg.getEndTime()));
        info.setDisplayTime(TimeHelper.dateToSecond(cfg.getDisplayTime()));
        return info;
    }

    public static WorldWarPlanInfo createByPb(WorldWarPlanInfoPb pb) {
        WorldWarPlanInfo info = new WorldWarPlanInfo();
        info.setId(pb.getId());
        info.setWorldWarType(pb.getWorldWarType());
        info.setDisplayBegin(pb.getDisplayBegin());
        info.setBeginTime(pb.getBeginTime());
        info.setEndTime(pb.getEndTime());
        info.setDisplayTime(pb.getDisplayTime());
        return info;
    }

    /**
     * 刷新数据
     * 
     * @param cfg
     */
    public void refreshByCfg(StaticWorldWarPlan cfg) {
        setId(cfg.getId());
        setWorldWarType(cfg.getWorldWarType());
        setDisplayBegin(TimeHelper.dateToSecond(cfg.getDisplayBegin()));
        setBeginTime(TimeHelper.dateToSecond(cfg.getBeginTime()));
        setEndTime(TimeHelper.dateToSecond(cfg.getEndTime()));
        setDisplayTime(TimeHelper.dateToSecond(cfg.getDisplayTime()));

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorldWarType() {
        return worldWarType;
    }

    public void setWorldWarType(int worldWarType) {
        this.worldWarType = worldWarType;
    }

    public int getDisplayBegin() {
        return displayBegin;
    }

    public void setDisplayBegin(int displayBegin) {
        this.displayBegin = displayBegin;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(int displayTime) {
        this.displayTime = displayTime;
    }

    /**
     * 获取阶段
     * 
     * @return
     */
    public int getStage() {
        int now = TimeHelper.getCurrentSecond();
        if (now < displayBegin || now > displayTime) {
            return STAGE_OVER; // 结束
        } else if (now >= displayBegin && now < beginTime) {
            return STAGE_DISPLAYER;// 预显示
        } else if (now >= beginTime && now < endTime) {
            return STAGE_SEASON_RUNNING; // 赛季进行中
        } else if (now >= endTime && now <= displayTime) {
            return STAGE_END_DISPLAYER;
        }
        return STAGE_OVER;
    }

    /**
     * 是否完全结束
     * 
     * @param info
     * @return true 完全结束
     */
    public static boolean isFinish(WorldWarPlanInfo info) {
        if (info == null) return true;
        return info.getStage() == STAGE_OVER;
    }

    public static CommonPb.WorldWarPlanInfoPb toWorldWarPlanInfoPb(WorldWarPlanInfo warPlanInfo) {
        CommonPb.WorldWarPlanInfoPb.Builder builder = CommonPb.WorldWarPlanInfoPb.newBuilder();
        builder.setId(warPlanInfo.getId());
        builder.setWorldWarType(warPlanInfo.getWorldWarType());
        builder.setDisplayBegin(warPlanInfo.getDisplayBegin());
        builder.setBeginTime(warPlanInfo.getBeginTime());
        builder.setEndTime(warPlanInfo.getEndTime());
        builder.setDisplayTime(warPlanInfo.getDisplayTime());
        StaticWorldWarPlan sPlan = StaticCrossWorldDataMgr.getWorldWarPlan().stream()
                .filter(wp -> wp.getId() == warPlanInfo.getId()).findFirst().orElse(null);
        if (sPlan != null) {
            builder.setFunctionOpen(sPlan.getFunctionOpen());
        }
        List<Integer> weeks = Constant.WORLDWAR_OPEN_WEEK;
        if (weeks != null && !weeks.isEmpty()) {
            builder.addAllOpenWeekday(weeks);
        }
        return builder.build();
    }
}
