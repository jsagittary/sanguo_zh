package com.gryphpoem.game.zw.dataMgr;

import com.google.common.collect.Lists;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.DrawCardRewardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSearch;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 18:15
 */
@Component
public class StaticDrawHeroDataMgr extends AbsStaticIniService {
    /**
     * 常驻抽卡池id
     */
    public static final int RESIDENT_CARD_DRAW_POOL_ID = 1;

    /**
     * 英雄抽卡池 (uniqueId, StaticHeroSearch)
     */
    private Map<Integer, StaticHeroSearch> heroSearchMap;
    /**
     * 英雄抽卡池 (poolId, List<StaticHeroSearch>)
     */
    private Map<Integer, Map<Integer, List<StaticHeroSearch>>> poolHeroSearchMap;
    /**
     * 总权重map (poolId, Map<rewardType, typeTotalWeight>)
     */
    private Map<Integer, Map<Integer, Integer>> totalWeightMap;
    /**
     * 活动计划配置map
     */
    private Map<Integer, StaticDrawHeoPlan> drawHeoPlanMap;
    /**
     * 抽卡权重表
     */
    private List<StaticDrawCardWeight> drawCardWeightList;

    @Override
    public void load() {
        int serverId = DataResource.ac.getBean(ServerSetting.class).getServerID();
        Map<Integer, StaticHeroSearch> heroSearchMap = staticDataDao.selectHeroSearchMap();
        this.heroSearchMap = heroSearchMap;
        if (CheckNull.nonEmpty(heroSearchMap)) {
            Map<Integer, Map<Integer, Integer>> totalWeightMap_ = new HashMap<>();
            Map<Integer, Map<Integer, List<StaticHeroSearch>>> poolHeroSearchMap_ = new HashMap<>();
            heroSearchMap.values().forEach(staticData -> {
                poolHeroSearchMap_.computeIfAbsent(staticData.getSearchType(), map -> new HashMap<>()).
                        computeIfAbsent(staticData.getRewardType(), list -> new ArrayList<>()).add(staticData);
                Map<Integer, Integer> typeTotalWeight_ = totalWeightMap_.computeIfAbsent(staticData.getSearchType(), map -> new HashMap<>());
                typeTotalWeight_.merge(staticData.getRewardType(), staticData.getWeight(), Integer::sum);
            });
            this.poolHeroSearchMap = poolHeroSearchMap_;
            this.totalWeightMap = totalWeightMap_;
        }

        Map<Integer, StaticDrawHeoPlan> drawHeoPlanMap = staticIniDao.selectStaticDrawHeoPlanMap();
        if (CheckNull.nonEmpty(drawHeoPlanMap)) {
            this.drawHeoPlanMap = drawHeoPlanMap.values().stream().filter(staticData -> Objects.nonNull(staticData) && CheckNull.nonEmpty(staticData.getServerIdList()) &&
                    checkServerId(serverId, staticData.getServerIdList())).collect(Collectors.toMap(StaticDrawHeoPlan::getId, v -> v));
        }

        List<StaticDrawCardWeight> drawCardWeightList_ = staticIniDao.selectStaticDrawCardWeightList();
        if (CheckNull.nonEmpty(drawCardWeightList_)) {
            this.drawCardWeightList = drawCardWeightList_.stream().filter(staticData -> {
                if (CheckNull.isNull(staticData) || CheckNull.isEmpty(staticData.getServerIdList()))
                    return false;
                return checkServerId(serverId, staticData.getServerIdList());
            }).map(staticData -> staticData.initTotalWeight()).collect(Collectors.toList());
        }
    }

    public Map<Integer, StaticHeroSearch> getHeroSearchMap() {
        return heroSearchMap;
    }

    public Map<Integer, StaticDrawHeoPlan> getDrawHeoPlanMap() {
        return drawHeoPlanMap;
    }

    /**
     * 检测配置是否为空
     *
     * @return
     */
    public StaticDrawCardWeight checkConfigEmpty(int poolId) {
        if (CheckNull.isEmpty(poolHeroSearchMap) || CheckNull.isEmpty(drawCardWeightList))
            return null;
        Map<Integer, List<StaticHeroSearch>> poolConfigMap = poolHeroSearchMap.get(poolId);
        if (CheckNull.isEmpty(poolConfigMap))
            return null;
        StaticDrawCardWeight config = drawCardWeightList.stream().filter(config_ -> config_.getSearchTypeId() == poolId).findFirst().orElse(null);
        if (CheckNull.isNull(config) || CheckNull.isEmpty(config.getWeight()))
            return null;
        return config;
    }

    /**
     * 获取所有符合状态的plan
     *
     * @param now
     * @param functionId
     * @param status
     * @return
     */
    public List<StaticDrawHeoPlan> getPlanList(Date now, int functionId, PlanFunction.PlanStatus... status) {
        if (CheckNull.isEmpty(drawHeoPlanMap))
            return null;
        return drawHeoPlanMap.values().stream().filter(plan -> Objects.nonNull(plan) && plan.getFunctionId() == functionId &&
                ArrayUtils.contains(status, plan.planStatus(now))).collect(Collectors.toList());
    }

    /**
     * 获取对应status planList
     *
     * @param now
     * @param status
     * @return
     */
    public List<StaticDrawHeoPlan> getPlanList(Date now, PlanFunction.PlanStatus... status) {
        if (CheckNull.isEmpty(drawHeoPlanMap))
            return null;
        return drawHeoPlanMap.values().stream().filter(plan -> Objects.nonNull(plan) && ArrayUtils.contains(status, plan.planStatus(now))).collect(Collectors.toList());
    }

    /**
     * 获取指定奖池
     *
     * @param searchTypeId
     * @return
     */
    public List<StaticDrawCardWeight> getSpecifyPool(int searchTypeId) {
        if (CheckNull.isEmpty(drawCardWeightList))
            return null;

        return drawCardWeightList.stream().filter(pool -> Objects.nonNull(pool) && pool.getSearchTypeId() == searchTypeId).collect(Collectors.toList());
    }

    /**
     * 常驻抽卡获取可抽取奖池
     *
     * @param now
     * @return
     */
    public List<StaticDrawCardWeight> getPermanentDrawCardWeightList(Date now) {
        if (CheckNull.isEmpty(drawCardWeightList) || CheckNull.isEmpty(poolHeroSearchMap))
            return null;
        Set<Integer> poolIdConfigSet = null;
        if (CheckNull.nonEmpty(drawHeoPlanMap)) {
            poolIdConfigSet = drawHeoPlanMap.values().stream().filter(staticData -> Objects.nonNull(staticData) &&
                    staticData.planStatus(now) == PlanFunction.PlanStatus.OVER).map(StaticDrawHeoPlan::getSearchTypeId).collect(Collectors.toSet());
        }

        // 获取所有英雄池子
        Set<Integer> finalSet = poolIdConfigSet;
        return drawCardWeightList.stream().filter(drawConfig -> {
            if (CheckNull.isNull(drawConfig))
                return false;
            if (drawConfig.getSearchTypeId() == RESIDENT_CARD_DRAW_POOL_ID)
                return true;
            return CheckNull.nonEmpty(finalSet) && finalSet.contains(drawConfig.getSearchTypeId());
        }).collect(Collectors.toList());
    }

    /**
     * 校验区服id是否在范围内
     *
     * @param serverId
     * @param serverList
     * @return
     */
    private boolean checkServerId(int serverId, List<List<Integer>> serverList) {
        if (CheckNull.isEmpty(serverList))
            return false;

        for (List<Integer> list : serverList) {
            if (CheckNull.isEmpty(list))
                continue;
            if (list.get(0) <= serverId && list.get(1) >= serverId)
                return true;
        }
        return false;
    }

    /**
     * 随机指定类型奖励
     *
     * @param config
     * @param type
     * @param now
     * @return
     */
    public StaticHeroSearch randomSpecifyType(StaticDrawCardWeight config, DrawCardRewardType type, Date now) {
        if (CheckNull.isNull(config) || CheckNull.isNull(type))
            return null;
        if (config.getSearchTypeId() == RESIDENT_CARD_DRAW_POOL_ID) {
            // 常驻抽卡池为融合抽卡池
            switch (type) {
                case PROP_REWARD:
                    return randomPoolReward(null, type, true);
                default:
                    return randomPoolReward(getPermanentDrawCardWeightList(now), type, true);
            }
        } else {
            return randomOnePoolReward(config, type);
        }
    }

    /**
     * 随机奖池奖励
     *
     * @param config
     * @param now
     * @return
     */
    public StaticHeroSearch randomReward(StaticDrawCardWeight config, Date now) {
        if (CheckNull.isEmpty(drawCardWeightList))
            return null;
        if (CheckNull.isNull(config))
            return null;
        DrawCardRewardType rewardType = randomAwardPool(config);
        if (CheckNull.isNull(rewardType))
            return null;
        if (config.getSearchTypeId() == RESIDENT_CARD_DRAW_POOL_ID) {
            switch (rewardType) {
                case PROP_REWARD:
                    return randomPoolReward(null, rewardType, true);
                default:
                    return randomPoolReward(getPermanentDrawCardWeightList(now), rewardType, true);
            }
        } else {
            return randomOnePoolReward(config, rewardType);
        }
    }

    /**
     * 随机奖励类型
     *
     * @param weightConfig
     * @return
     */
    private DrawCardRewardType randomAwardPool(StaticDrawCardWeight weightConfig) {
        if (CheckNull.isNull(weightConfig) || CheckNull.isEmpty(weightConfig.getWeight()))
            return null;
        if (weightConfig.getTotalWeight() <= 0) {
            weightConfig.initTotalWeight();
        }
        int randomValue = RandomHelper.randomInSize(weightConfig.getTotalWeight());
        int temp = 0;
        for (List<Integer> config : weightConfig.getWeight()) {
            temp += config.get(1);
            if (temp >= randomValue) {
                return DrawCardRewardType.convertTo(config.get(0));
            }
        }

        return null;
    }

    /**
     * 随机一个奖池奖励
     *
     * @param config
     * @param type
     * @return
     */
    private StaticHeroSearch randomOnePoolReward(StaticDrawCardWeight config, DrawCardRewardType type) {
        if (CheckNull.isNull(type) || CheckNull.isEmpty(poolHeroSearchMap))
            return null;

        if (CheckNull.isNull(config))
            return null;
        Map<Integer, List<StaticHeroSearch>> configMap = poolHeroSearchMap.get(config.getSearchTypeId());
        if (CheckNull.isEmpty(configMap))
            return null;
        List<StaticHeroSearch> configList = configMap.get(type.getType());
        if (CheckNull.isEmpty(configList))
            return null;

        // 若总奖池都可以找到配置, 则总权重也是有的
        Integer totalWeight = totalWeightMap.get(config.getSearchTypeId()).get(type.getType());

        int temp = 0;
        int random = RandomHelper.randomInSize(totalWeight);
        if (!CheckNull.isEmpty(configList)) {
            for (StaticHeroSearch shs : configList) {
                temp += shs.getWeight();
                if (temp >= random) {
                    return shs;
                }
            }
        }

        return null;
    }

    /**
     * 随机多个抽卡池奖励
     *
     * @param awardPoolConfig
     * @param type
     * @return
     */
    private StaticHeroSearch randomPoolReward(List<StaticDrawCardWeight> awardPoolConfig, DrawCardRewardType type, boolean permanent) {
        if (CheckNull.isNull(type) || CheckNull.isEmpty(poolHeroSearchMap))
            return null;

        int totalWeight = 0;
        List<StaticHeroSearch> list = null;
        if (permanent && DrawCardRewardType.PROP_REWARD.equals(type)) {
            // 若为常驻抽卡, 则道具奖励不与限时奖池融合
            Map<Integer, List<StaticHeroSearch>> configMap = poolHeroSearchMap.get(RESIDENT_CARD_DRAW_POOL_ID);
            if (CheckNull.nonEmpty(configMap)) {
                list = configMap.get(type.getType());
                totalWeight = totalWeightMap.get(RESIDENT_CARD_DRAW_POOL_ID).get(type.getType());
            }
        } else {
            if (CheckNull.isEmpty(awardPoolConfig))
                return null;
            for (StaticDrawCardWeight config : awardPoolConfig) {
                Map<Integer, List<StaticHeroSearch>> configMap = poolHeroSearchMap.get(config.getSearchTypeId());
                if (CheckNull.isEmpty(configMap))
                    continue;
                List<StaticHeroSearch> configList = configMap.get(type.getType());
                if (CheckNull.isEmpty(configList))
                    continue;
                if (CheckNull.isNull(list))
                    list = new ArrayList<>();
                list.addAll(configList);

                // 若总奖池都可以找到配置, 则总权重也是有的
                Integer typeTotalWeight = totalWeightMap.get(config.getSearchTypeId()).get(type.getType());
                if (Objects.nonNull(typeTotalWeight)) {
                    totalWeight += typeTotalWeight;
                }
            }
        }
        if (CheckNull.isEmpty(list))
            return null;

        int temp = 0;
        int random = RandomHelper.randomInSize(totalWeight);
        if (!CheckNull.isEmpty(list)) {
            for (StaticHeroSearch shs : list) {
                temp += shs.getWeight();
                if (temp >= random) {
                    return shs;
                }
            }
        }

        return null;
    }

    /**
     * 获取心愿武将英雄池
     *
     * @param awardPoolConfig
     * @return
     */
    public List<Integer> getWishedHeroPool(List<StaticDrawCardWeight> awardPoolConfig) {
        if (CheckNull.isEmpty(awardPoolConfig))
            return null;

        List<Integer> list = new ArrayList<>();
        for (StaticDrawCardWeight config : awardPoolConfig) {
            Map<Integer, List<StaticHeroSearch>> configMap = poolHeroSearchMap.get(config.getSearchTypeId());
            if (CheckNull.isEmpty(configMap))
                continue;
            List<StaticHeroSearch> configList = configMap.get(DrawCardRewardType.ORANGE_HERO.getType());
            if (CheckNull.isEmpty(configList))
                continue;
            for (StaticHeroSearch staticData : configList) {
                if (CheckNull.isNull(staticData) || CheckNull.isEmpty(staticData.getRewardList()))
                    continue;
                list.add(staticData.getRewardList().get(0).get(1));
            }
        }
        return list;
    }

    @Override
    public void check() {
        if (CheckNull.isEmpty(heroSearchMap) || !poolHeroSearchMap.containsKey(RESIDENT_CARD_DRAW_POOL_ID)) {
            LogUtil.error(String.format("permanent hero pool is empty!"));
        }
    }
}
