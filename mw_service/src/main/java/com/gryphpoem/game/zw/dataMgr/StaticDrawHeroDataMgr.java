package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.DrawCardRewardType;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSearch;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
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
     * 获取当前所有奖池
     *
     * @param now
     * @return
     */
    public List<StaticDrawCardWeight> getDrawCardWeightList(Date now) {
        if (CheckNull.isEmpty(drawCardWeightList) || CheckNull.isEmpty(poolHeroSearchMap))
            return null;
        Set<Integer> poolIdConfigSet = null;
        if (CheckNull.nonEmpty(drawHeoPlanMap)) {
            poolIdConfigSet = drawHeoPlanMap.values().stream().filter(staticData -> Objects.nonNull(staticData) &&
                    staticData.isOver(now)).map(StaticDrawHeoPlan::getSearchTypeId).collect(Collectors.toSet());
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
     * @param list
     * @return
     */
    public StaticHeroSearch randomSpecifyType(List<StaticDrawCardWeight> list, DrawCardRewardType type) {
        if (CheckNull.isEmpty(list) || CheckNull.isNull(type))
            return null;

        return randomPoolReward(list, type);
    }

    /**
     * 随机奖池奖励
     *
     * @param list
     * @return
     */
    public StaticHeroSearch randomReward(List<StaticDrawCardWeight> list) {
        if (CheckNull.isEmpty(list))
            return null;

        return randomPoolReward(list, randomAwardPool(list));
    }

    /**
     * 随机奖励类型
     *
     * @param awardPoolConfig
     * @return
     */
    private DrawCardRewardType randomAwardPool(List<StaticDrawCardWeight> awardPoolConfig) {
        if (CheckNull.isEmpty(awardPoolConfig))
            return null;

        Map<Integer, Integer> allPoolConfig = new HashMap<>();
        for (StaticDrawCardWeight staticWeight : awardPoolConfig) {
            if (CheckNull.isNull(staticWeight) || CheckNull.isEmpty(staticWeight.getWeight()))
                continue;
            staticWeight.getWeight().forEach(list -> {
                if (!allPoolConfig.containsKey(list.get(0))) {
                    allPoolConfig.put(list.get(0), 0);
                }
                allPoolConfig.merge(list.get(0), list.get(1), Integer::sum);
            });
        }
        if (CheckNull.isEmpty(allPoolConfig))
            return null;

        int totalWeight = allPoolConfig.entrySet().stream().mapToInt(entry -> entry.getValue()).sum();
        int randomValue = RandomHelper.randomInSize(totalWeight);
        int temp = 0;
        for (Map.Entry<Integer, Integer> entry : allPoolConfig.entrySet()) {
            temp += entry.getValue();
            if (temp >= randomValue) {
                return DrawCardRewardType.convertTo(entry.getKey());
            }
        }

        return null;
    }

    /**
     * 随机抽卡奖励
     *
     * @param awardPoolConfig
     * @param type
     * @return
     */
    private StaticHeroSearch randomPoolReward(List<StaticDrawCardWeight> awardPoolConfig, DrawCardRewardType type) {
        if (CheckNull.isEmpty(awardPoolConfig) || CheckNull.isNull(type) || CheckNull.isEmpty(poolHeroSearchMap))
            return null;

        int totalWeight = 0;
        List<StaticHeroSearch> list = new ArrayList<>();
        for (StaticDrawCardWeight config : awardPoolConfig) {
            Map<Integer, List<StaticHeroSearch>> configMap = poolHeroSearchMap.get(config.getSearchTypeId());
            if (CheckNull.isEmpty(configMap))
                continue;
            List<StaticHeroSearch> configList = configMap.get(type.getType());
            if (CheckNull.isEmpty(configList))
                continue;
            list.addAll(configList);

            // 若总奖池都可以找到配置, 则总权重也是有的
            Integer typeTotalWeight = totalWeightMap.get(config.getSearchTypeId()).get(type.getType());
            if (Objects.nonNull(typeTotalWeight)) {
                totalWeight += typeTotalWeight;
            }
        }

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
