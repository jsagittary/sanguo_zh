package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticIniDao;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-17 12:24
 */
public class StaticActivityCrossDataMgr {
    private static StaticIniDao staticIniDao = DataResource.ac.getBean(StaticIniDao.class);

    private static ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);

    // key: activityId
    private static Map<Integer, List<StaticActAwardCross>> actAwardListMap = new HashMap<>();
    // key: keyId
    private static Map<Integer, StaticActAwardCross> actAwardMap = new HashMap<>();

    private static List<ActivityBase> activityList;




    public static void init() {

    }

    private static void activity() {
        int activityMoldId = serverSetting.getActMoldId();
        List<StaticActivityCrossPlan> planList = staticIniDao.selectStaticActivityPlanCross();
        // 开服时间
        Date openTime = DateHelper.parseDate(serverSetting.getOpenTime());
        Map<Integer, StaticActivity> activityMap = StaticActivityDataMgr.getStaticActivityMap();
        List<ActivityBase> activityList = new ArrayList<>();
        for (StaticActivityPlan e : planList) {
            int activityType = e.getActivityType();
            StaticActivity staticActivity =  activityMap.get(activityType);
            if (staticActivity == null) {
                continue;
            }
            int moldId = e.getMoldId();
            if (activityMoldId != moldId) {
                continue;
            }
            ActivityBase activityBase = new ActivityBase();
            activityBase.setOpenTime(openTime);
            activityBase.setPlan(e);
            activityBase.setStaticActivity(staticActivity);
            boolean flag = activityBase.initData();// 计算活动的各种时间
            if (flag && activityBase.isSelfSeverPlan(serverSetting.getServerID())) {
                activityList.add(activityBase);
            }
        }
        StaticActivityCrossDataMgr.activityList = activityList;
    }

    public static ActivityBase getActivityByType(int activityType) {
        ActivityBase rab = null;
        for (ActivityBase e : activityList) {
            StaticActivity a = e.getStaticActivity();
            StaticActivityPlan plan = e.getPlan();
            if (a == null || plan == null) {
                continue;
            }
            if (a.getType() == activityType && e.getStep0() != ActivityConst.OPEN_CLOSE) {
                if (rab != null && rab.getPlan().getKeyId() > plan.getKeyId()) continue; // 如果是同样的 ,返回keyId大的
                rab = e;
            }
        }
        return rab;
    }
}
