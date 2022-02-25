package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticPushApp;
import com.gryphpoem.push.bean.AppInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 消息推送相关配置数据管理类
 * @author TanDonghai
 * @date 创建时间：2017年9月12日 下午2:42:18
 *
 */
public class StaticPushDataMgr {
    private StaticPushDataMgr() {
    }

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static List<StaticPushApp> sPushAppList;

    // key: platNo_appId
    private static Map<String, AppInfo> appInfoMap = new HashMap<>();

    public static void init() {
        List<StaticPushApp> sPushAppList = staticDataDao.selectPushAppList();
        StaticPushDataMgr.sPushAppList = sPushAppList;
        for (StaticPushApp app : sPushAppList) {
            AppInfo appInfo = new AppInfo();
            appInfo.setPackName(String.valueOf(app.getAppId()));
            String key = getKey(app.getPlatNo(), String.valueOf(app.getAppId()));
            appInfoMap.put(key, appInfo);
        }
    }

    public static AppInfo getAppInfo(int platNo, String appId) {
        String key = getKey(platNo, appId);
        AppInfo appInfo = appInfoMap.get(key);
        if (appInfo == null) {
            appInfo = new AppInfo("test");
            appInfoMap.put(key, appInfo);
        }
        return appInfo;
    }

    public static List<StaticPushApp> getsPushAppList() {
        return sPushAppList;
    }

    public static boolean canPush(int platNo, String appId) {
        // String key = getKey(platNo, appId);
        // return appInfoMap.containsKey(key);
        // 此处不做验证
        return true;
    }

    private static String getKey(int platNo, String appId) {
        String key = platNo + "_" + appId;
        return key;
    }
}
