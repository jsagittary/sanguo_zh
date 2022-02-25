package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 战令的相关配置
 *
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-02 11:47
 */
public class StaticBattlePassDataMgr {

    // dao
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
    // 服务器配置
    private static ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
    // key: 配置表里的Key
    private static Map<Integer, StaticBattlePassPlan> sBattlePassPlanMap = new HashMap<>();
    // 所有的战令活动配置
    private static List<StaticBattlePassPlan> sBattlePassPlanList = new ArrayList<>();
    // key: 配置表里的planKey, key 任务id, value: 任务配置
    private static Map<Integer, Map<Integer, StaticBattlePassTask>> sBattlePassTaskPlanMap = new HashMap<>();
    // 所有的战令的任务配置
    private static List<StaticBattlePassTask> sBattlePassTaskList = new ArrayList<>();
    // 所有的战令等级配置
    private static List<StaticBattlePassLv> sBattlePassPlanlvList = new ArrayList<>();
    // key: 配置表里的planKey
    private static Map<Integer, Map<Integer, StaticBattlePassLv>> sBattlePassLvByPlanMap = new HashMap<>();
    // key: 配置表里的planKey, value: 最大的等级
    private static Map<Integer, Integer> sBattlePassMaxLvByPlan = new HashMap<>();

    // 初始化配置
    public static void init() {
        // 开服时间
        Date openServerDate = serverSetting.getOpenServerDate();
        StaticBattlePassDataMgr.sBattlePassPlanList = staticDataDao.selectBattlePassPlan();
        StaticBattlePassDataMgr.sBattlePassPlanMap = StaticBattlePassDataMgr.sBattlePassPlanList.stream()
                .filter(plan -> {
                    // 时间配置有问题, 就过滤掉不影响应用启动
                    if (plan.checkConfig()) {
                        LogUtil.error("StaticBattlePassPlan 的时间配置有问题 key: " + plan.getKeyId());
                        return false;
                    }
                    // 过滤不是自己区服配置的
                    else return plan.checkServerPlan(serverSetting.getServerID());
                })
                // 初始化活动的开启时间为具体的时间点
                .peek(plan -> plan.initDate(openServerDate))
                .collect(Collectors.toMap(StaticBattlePassPlan::getKeyId, plan -> plan, (oldK, newK) -> newK));
        StaticBattlePassDataMgr.sBattlePassTaskList = staticDataDao.selectBattlePassTask();
        StaticBattlePassDataMgr.sBattlePassTaskPlanMap = StaticBattlePassDataMgr.sBattlePassTaskList.stream().collect(Collectors.groupingBy(StaticBattlePassTask::getPlanKey, HashMap::new, Collectors.toMap(StaticBattlePassTask::getId, conf -> conf)));
        StaticBattlePassDataMgr.sBattlePassPlanlvList = staticDataDao.selectBattlePasslv();
        StaticBattlePassDataMgr.sBattlePassLvByPlanMap = StaticBattlePassDataMgr.sBattlePassPlanlvList.stream().collect(Collectors.groupingBy(StaticBattlePassLv::getPlanKey, HashMap::new, Collectors.toMap(StaticBattlePassLv::getLv, conf -> conf)));
        StaticBattlePassDataMgr.sBattlePassMaxLvByPlan = StaticBattlePassDataMgr.sBattlePassPlanlvList.stream().collect(Collectors.toMap(StaticBattlePassLv::getPlanKey, StaticBattlePassLv::getLv, (oldV, newV) -> newV > oldV ? newV : oldV));
    }

    /**
     * 当前开放的战令活动
     *
     * @return 当前开放的战令活动
     */
    public static StaticBattlePassPlan currentOpenPlan() {
        // 现在的时间
        Date now = new Date();
        // sBattlePassPlanMap已经过滤了serverId
        if (!CheckNull.isEmpty(StaticBattlePassDataMgr.sBattlePassPlanMap)) {
            // 获取当前正在活动时间内的, 如果同时有多个活动在活动时间内, 就获取KeyId最小的那个
            return StaticBattlePassDataMgr.sBattlePassPlanMap.values().stream().filter(plan -> plan.checkOpen(now)).min(Comparator.comparingInt(StaticBattlePassPlan::getKeyId)).orElse(null);
        }
        return null;
    }

    /**
     * 根据key获取战令的plan配置
     *
     * @param staticKey plan配置表里的主键
     * @return 战令的plan配置
     */
    public static StaticBattlePassPlan getPlanById(int staticKey) {
        return sBattlePassPlanMap.get(staticKey);
    }


    /**
     * 根据key获取战令的任务配置
     *
     * @param staticKey plan配置表里的主键
     * @return 战令的任务配置
     */
    public static Collection<StaticBattlePassTask> getTasksByPlanKey(int staticKey) {
        return Optional.ofNullable(sBattlePassTaskPlanMap.get(staticKey)).map(Map::values).orElse(null);
    }

    /**
     * 根据taskId获取战令的任务配置
     *
     * @param id taskId
     * @return 战令的任务配置
     */
    public static StaticBattlePassTask getTaskById(int staticKey, int id) {
        return sBattlePassTaskPlanMap.getOrDefault(staticKey, new HashMap<>()).get(id);
    }

    /**
     * 根据key获取战令的奖励和等级配置
     *
     * @param staticKey plan配置表里的主键
     * @return 战令的奖励和等级配置
     */
    public static Collection<StaticBattlePassLv> getLvAwardByPlanKey(int staticKey) {
        return Optional.ofNullable(sBattlePassLvByPlanMap.get(staticKey)).map(Map::values).orElse(null);
    }

    /**
     * 根据key和lv获取战令的奖励和等级配置
     *
     * @param staticKey plan配置表里的主键
     * @param lv        等级
     * @return 战令的奖励和等级配置
     */
    public static StaticBattlePassLv getLvAwardByPlanKey(int staticKey, int lv) {
        return sBattlePassLvByPlanMap.getOrDefault(staticKey, new HashMap<>()).get(lv);
    }

    /**
     * 根据key获取最大等级
     *
     * @param staticKey plan配置表里的主键
     * @return 最大等级
     */
    public static int getMaxLvByPlanKey(int staticKey) {
        return sBattlePassMaxLvByPlan.getOrDefault(staticKey, 0);
    }
}