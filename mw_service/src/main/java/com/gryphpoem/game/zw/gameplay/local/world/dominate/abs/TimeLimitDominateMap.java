package com.gryphpoem.game.zw.gameplay.local.world.dominate.abs;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;

/**
 * Description: 限时雄踞一方地图玩法
 * Author: zhangpeng
 * createTime: 2022-11-22 14:56
 */
public abstract class TimeLimitDominateMap implements WorldMapPlay {
    /** 当前活动次数*/
    protected int curTimes;
    /** 活动是否开放*/
    private boolean open;
    /** 世界地图玩法*/
    private int worldFunction;
    /** 当前预显示时间*/
    private Date curPreviewDate;
    /** 当前活动开始时间*/
    private Date curBeginDate;
    /** 当前活动结束时间*/
    private Date curEndTime;
    /** 开放的城池 <活动次数, 开放的城池list>*/
    protected Map<Integer, List<DominateSideCity>> curOpenCityList;

    public TimeLimitDominateMap(int worldFunction) {
        this.worldFunction = worldFunction;
        this.curOpenCityList = new HashMap<>();
    }

    @Override
    public int getWorldMapFunction() {
        return worldFunction;
    }

    public Date getCurPreviewDate() {
        return curPreviewDate;
    }

    public void setCurPreviewDate(Date curPreviewDate) {
        this.curPreviewDate = curPreviewDate;
    }

    public Date getCurBeginDate() {
        return curBeginDate;
    }

    public void setCurBeginDate(Date curBeginDate) {
        this.curBeginDate = curBeginDate;
    }

    public Date getCurEndTime() {
        return curEndTime;
    }

    public void setCurEndTime(Date curEndTime) {
        this.curEndTime = curEndTime;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Map<Integer, List<DominateSideCity>> getCurOpenCityList() {
        return curOpenCityList;
    }

    public int getCurTimes() {
        return curTimes;
    }

    /**
     * 加入定时器
     *
     * @param name              定时器名称
     * @param r                 雄踞一方定时任务
     * @param time              定时时间
     */
    protected void addSchedule(String name, TimeLimitDominateMap.DominateJob r, Date time, TimeLimitDominateMap dominateMap) {
        Date now = new Date();
        if (now.before(time)) {
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> r.onRun(dominateMap, job), time);
            LogUtil.world("雄踞一方定时器初始  name:", name, ", time:", DateHelper.formatDateMiniTime(time));
        } else {
            LogUtil.error("雄踞一方定时器超时 name:", name, ", time:", DateHelper.formatDateMiniTime(time), ", now:", now, ", time:", time);
        }
    }

    /**
     * 雄踞一方定时任务接口
     */
    private interface DominateJob {

        void onRun(TimeLimitDominateMap timeLimitDominateMap, DefultJob job);
    }

    @Override
    public int state() {
        Date now = new Date();
        if (now.before(this.getCurPreviewDate())) {
            return WorldPb.WorldFunctionStateDefine.NOT_START_VALUE;
        } else if (now.after(this.getCurPreviewDate()) && now.before(this.getCurBeginDate())) {
            return WorldPb.WorldFunctionStateDefine.ON_PREVIEW_VALUE;
        } else if (now.after(this.getCurBeginDate()) && now.before(this.getCurEndTime())) {
            return WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE;
        } else {
            return WorldPb.WorldFunctionStateDefine.END_VALUE;
        }
    }

    @Override
    public String getWorldMapFunctionName() {
        return "Dominate_" + getWorldMapFunction();
    }

    /**
     * 检测当前活动是否有城池阵营胜出
     */
    public void checkWinOfOccupyTime() {
        if (state() != WorldPb.WorldFunctionStateDefine.IN_PROGRESS_VALUE) {
            LogUtil.error("雄踞一方活动未在进行中");
            return;
        }
        List<DominateSideCity> sideCityList;
        if (CheckNull.isEmpty(this.curOpenCityList) || CheckNull.isEmpty(
                sideCityList = this.curOpenCityList.get(this.curTimes))) {
            LogUtil.error("活动进行中, 正在开放的城池为空");
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        sideCityList.forEach(sideCity -> {
            if (sideCity.isOver())
                return;

            for (int camp : Constant.Camp.camps) {
                int campOccupyTime = sideCity.getCampOccupyTime(now, camp);
                if (campOccupyTime >= Constant.TEN_THROUSAND) {
                    sideCity.setOver(true);
                    sideCity.setCamp(camp);
                    break;
                }
            }
        });
    }
}
