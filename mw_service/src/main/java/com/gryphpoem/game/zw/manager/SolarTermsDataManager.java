package com.gryphpoem.game.zw.manager;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.dataMgr.StaticSolarTermsDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb2.SyncSolarTermsRs;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticSolarTerms;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName SolarTermsDataManager.java
 * @Description 节气系统
 * @author QiuKun
 * @date 2017年11月21日
 */
@Component
public class SolarTermsDataManager {
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;

    private StaticSolarTerms startSolarTerms;// 节气开始的配

    /**
     * 获取夜间防守加成值
     * 
     * @return 返回null说明没有加成
     */
    public Map<Integer, Integer> getNightEffect() {
        if (!isSolarTermsBegin()) {
            return null;
        }
        int nowHour = TimeHelper.getCurrentHour();
        if (nowHour >= Constant.DAY_AND_NIGHT_RANGE.get(0) && nowHour <= Constant.DAY_AND_NIGHT_RANGE.get(1)) {
            return Constant.DAY_AND_NIGHT_DEF_BONUS;
        }
        return null;
    }

    /**
     * 是否开启了节气系统
     * 
     * @return true开启了节气系统
     */
    public boolean isSolarTermsBegin() {
        return globalDataManager.getGameGlobal().getTrophy().getSolarTermsStartTime() > 0;
    }

    /**
     * 设置节气开启时间
     * 
     * @param time
     */
    public void setSolarTermsBeginTime(int time) {
        globalDataManager.getGameGlobal().getTrophy().setSolarTermsStartTime(time);
        startSolarTerms = null;
        getStartSolarTerms();
        syncSolarTerms();// 推送节气
    }

    /**
     * 给全服的人推送当前节气
     */
    public void syncSolarTerms() {
        StaticSolarTerms curSolarTerms = getCurSolarTerms();
        for (Player player : playerDataManager.getPlayers().values()) {
            if (player != null && player.isLogin && player.ctx != null) {
                SyncSolarTermsRs.Builder b = SyncSolarTermsRs.newBuilder();
                b.setId(curSolarTerms == null ? 0 : curSolarTerms.getId());
                Base.Builder builder = PbHelper.createSynBase(SyncSolarTermsRs.EXT_FIELD_NUMBER, SyncSolarTermsRs.ext,
                        b.build());
                MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
            }
        }

    }

    /**
     * 获取节气采集加成万分比
     * 
     * @param type 类型 详见 SolarTermsConstant 类
     * @return
     */
    public int getCollectBoundByType(int type, Date date) {
        if (!isSolarTermsBegin()) {
            return 0;
        }
        StaticSolarTerms curSolarTerms = getSolarTermsByDate(date);
        if (curSolarTerms == null) {
            return 0;
        }
        if (curSolarTerms.getType() != type) {
            return 0;
        }
        return curSolarTerms.getLevyBonus();
    }

    /**
     * 是否有季节采集加成
     * 
     * @param type
     * @return true为有
     */
    public boolean isCollectEffect(int type, Date startDate) {
        return getCollectBoundByType(type, startDate) > 0;
    }

    /**
     * 获取节气征收加成占基础产量的万分比例(募兵加成用是此处)
     * 
     * @param type 类型 详见 SolarTermsConstant 类
     * @return
     */
    public int getLevyBoundByType(int type) {
        if (!isSolarTermsBegin()) {
            return 0;
        }
        StaticSolarTerms curSolarTerms = getCurSolarTerms();
        if (curSolarTerms == null) {
            return 0;
        }
        if (curSolarTerms.getType() != type) {
            return 0;
        }
        return curSolarTerms.getLevyBonus();
    }

    /**
     * 获取当前节气的配置
     * 
     * @return
     */
    public StaticSolarTerms getCurSolarTerms() {
        return getSolarTermsByDate(new Date());
    }

    /**
     * 获取指定日期节气的配置
     * 
     * @param curDate
     * @return
     */
    private StaticSolarTerms getSolarTermsByDate(Date curDate) {
        StaticSolarTerms startSst = getStartSolarTerms();
        if (startSst == null) {
            return null;
        }
        int startTime = globalDataManager.getGameGlobal().getTrophy().getSolarTermsStartTime();
        if (startTime <= 0) {
            return null;
        }
        Date startDate = new Date(startTime * 1000L);
        int startId = startSst.getId();
        int maxId = StaticSolarTermsDataMgr.getSolarTerms().size();
        int dayiy = DateHelper.dayiy(startDate, curDate) - 1; // 相聚多少天
        if (dayiy < 0) {
            return null;
        }
        int sum = startId + dayiy;
        int curId = (sum % maxId);
        curId = curId == 0 ? maxId : curId;
        return StaticSolarTermsDataMgr.getSolarTermsById(curId);
    }

    /**
     * 获取开启节气的配置
     * 
     * @return
     */
    private StaticSolarTerms getStartSolarTerms() {
        int startTime = globalDataManager.getGameGlobal().getTrophy().getSolarTermsStartTime();
        if (startTime <= 0) {
            return null;
        }
        if (startSolarTerms == null) {
            Date startDate = new Date(startTime * 1000L);
            int week = TimeHelper.getCNDayOfWeek(startDate);
            startSolarTerms = StaticSolarTermsDataMgr.getStartSolarTermsByWeek(week);
        }
        return startSolarTerms;
    }

}
