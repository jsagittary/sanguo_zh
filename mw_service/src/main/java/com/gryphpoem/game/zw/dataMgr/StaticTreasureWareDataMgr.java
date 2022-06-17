package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StaticTreasureWareDataMgr extends AbsStaticIniService {

    private static Map<Integer, StaticTreasureWare> staticTreasureWareMakeMap;
    private static Map<Integer, List<StaticTreasureWare>> staticRandomTreasureWareMap;
    private static Map<Integer, StaticTreasureWareSpecial> staticTreasureWareSpecialMap;
    private static Map<Integer, Map<Integer, StaticTreasureWareSpecial>> staticTreasureWareSpecialLevelMap;
    private static Map<Integer, Map<Integer, StaticTreasureWareLevel>> staticTreasureWareLevelMap;
    private static Map<Integer, StaticTreasureCombat> treasureCombatMap;
    /** 每章节的最大关卡id */
    private static Map<Integer, Integer> treasureCombatMaxCombatMap;
    private static List<StaticTreasureCombatBuff> treasureCombatBuffs;
    private static List<Integer> treasureSection;
    private static Map<Integer, Map<Integer, StaticTreasureWareProfile>> staticTreasureWareProfileMap;
    private static Map<Integer, StaticTreasureWareProfile> seasonTreasureWareProfileMap;

    /**
     * 宝具副本配置
     *
     * @param combatId 副本id
     * @return 配置
     */
    public static StaticTreasureCombat getTreasureCombatMap(int combatId) {
        return treasureCombatMap.get(combatId);
    }

    public static Integer getInitSectionId() {
        return treasureSection.get(0);
    }

    public static Integer getNextSectionId(Integer sectionId) {
        int index = treasureSection.indexOf(sectionId);
        if (index >= treasureSection.size() - 1) {
            return null;
        }

        return treasureSection.get(++index);
    }

    /**
     * 宝具副本buff
     *
     * @param armyType 兵种类型
     * @param count    改兵种将领数量
     * @return buff
     */
    public static Map<Integer, Integer> getTreasureCombatBuff(int armyType, long count) {
        return treasureCombatBuffs.stream()
                .filter(sBuff -> sBuff.getArmyType() == armyType && count >= sBuff.getArmyNum())
                .max(Comparator.comparingInt(StaticTreasureCombatBuff::getArmyNum))
                .map(StaticTreasureCombatBuff::getAttr)
                .orElse(null);
    }

    public static void loadQualityTreasureWare() {
        Optional.ofNullable(staticTreasureWareMakeMap).ifPresent(map -> {
            staticRandomTreasureWareMap = new HashMap<>();
            map.values().forEach(data -> {
                if (CheckNull.isNull(data))
                    return;
                staticRandomTreasureWareMap.computeIfAbsent(data.
                        getQuality(), list -> new ArrayList<>()).add(data);
            });
        });
    }

    public static StaticTreasureWareLevel getStaticTreasureWareLevel(int quality, int level) {
        if (ObjectUtils.isEmpty(staticTreasureWareLevelMap.get(quality))) {
            return null;
        }

        return staticTreasureWareLevelMap.get(quality).get(level);
    }

    public static int getMaxLevelByQuality(int quality) {
        if (ObjectUtils.isEmpty(staticTreasureWareLevelMap.get(quality))) {
            return 0;
        }

        return staticTreasureWareLevelMap.get(quality).keySet().stream().max(Comparator.comparingInt(Integer::intValue)).get();
    }

    /**
     * 获得下一等级
     *
     * @param quality
     * @param level
     * @return
     */
    public static Integer getNextStaticTreasureWareLevel(int quality, int level) {
        if (ObjectUtils.isEmpty(staticTreasureWareLevelMap.get(quality))) {
            return null;
        }

        Set<Integer> levelSet = staticTreasureWareLevelMap.get(quality).keySet();
        if (ObjectUtils.isEmpty(levelSet))
            return null;

        List<Integer> levelList = levelSet.stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());
        for (Integer temp : levelList) {
            if (temp > level) {
                return temp;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * 过滤获得对应可随机的宝具列表
     *
     * @param quality
     * @param season
     * @param maxPlayerCheckpoint
     * @return
     */
    public static List<StaticTreasureWare> filterTreasureWare(int quality, int season, int maxPlayerCheckpoint) {
        List<StaticTreasureWare> list = staticRandomTreasureWareMap.get(quality);
        if (ObjectUtils.isEmpty(list))
            return null;

        return list.stream().filter(data -> data.getSeason() == season && data.getLimitLevel() <= maxPlayerCheckpoint).collect(Collectors.toList());
    }

    public static List<StaticTreasureWare> getQualityTreasureWare(int quality) {
        return staticRandomTreasureWareMap.get(quality);
    }

    public static StaticTreasureWareSpecial getTreasureWareSpecial(int id) {
        return staticTreasureWareSpecialMap.get(id);
    }

    public static StaticTreasureWare getStaticTreasureWare(int id) {
        return staticTreasureWareMakeMap.get(id);
    }

    public static StaticTreasureWareSpecial getStaticTreasureWareSpecial(int specialId, int level) {
        if (ObjectUtils.isEmpty(staticTreasureWareSpecialLevelMap.get(specialId)))
            return null;

        return staticTreasureWareSpecialLevelMap.get(specialId).get(level);
    }

    public static Map<Integer, List<StaticTreasureWareSpecial>> getTypesTreasureWareSpecial() {
        return staticTreasureWareSpecialMap.values().stream().
                collect(Collectors.groupingBy(StaticTreasureWareSpecial::getType));
    }

    /**
     * 获取宝具名id
     *
     * @param attrType
     * @param quality
     * @param specialId
     * @return
     */
    public static int getProfileId(int attrType, int quality, Integer specialId) {
        if (Objects.nonNull(specialId)) {
            StaticTreasureWareSpecial special = staticTreasureWareSpecialMap.get(specialId);
            if (Objects.nonNull(special) && Objects.nonNull(seasonTreasureWareProfileMap.get(special.getSpecialId()))) {
                return seasonTreasureWareProfileMap.get(special.getSpecialId()).getId();
            }
        }

        if (ObjectUtils.isEmpty(staticTreasureWareProfileMap.get(attrType)) || ObjectUtils.isEmpty(staticTreasureWareProfileMap.get(attrType).get(quality)))
            return -1;

        return staticTreasureWareProfileMap.get(attrType).get(quality).getId();
    }

    @Override
    public void load() {
        Map<Integer, StaticTreasureWare> staticTreasureWareMap = super.staticIniDao.selectStaticTreasureWareMake();
        Optional.ofNullable(staticTreasureWareMap).ifPresent(map -> {
            staticTreasureWareMakeMap = map;
            map.values().forEach(data -> data.initData());
        });
        loadQualityTreasureWare();

        staticTreasureWareSpecialMap = super.staticIniDao.selectStaticTreasureWareSpecial();
        Optional.ofNullable(staticTreasureWareSpecialMap).ifPresent(map -> {
            staticTreasureWareSpecialLevelMap = new HashMap<>();
            staticTreasureWareSpecialMap.values().forEach(staticTreasureWareSpecial -> {
                Map<Integer, StaticTreasureWareSpecial> temp = staticTreasureWareSpecialLevelMap.get(staticTreasureWareSpecial.getSpecialId());
                if (CheckNull.isNull(temp)) {
                    temp = new HashMap<>();
                    staticTreasureWareSpecialLevelMap.put(staticTreasureWareSpecial.getSpecialId(), temp);
                }
                temp.put(staticTreasureWareSpecial.getLevel(), staticTreasureWareSpecial);
            });
        });

        List<StaticTreasureWareLevel> staticTreasureWareLevelList = super.staticIniDao.selectStaticTreasureWareLevel();
        Optional.ofNullable(staticTreasureWareLevelList).ifPresent(list -> {
            staticTreasureWareLevelMap = new HashMap<>();
            list.forEach(staticTreasureWareLevel -> {
                Map<Integer, StaticTreasureWareLevel> map = staticTreasureWareLevelMap.
                        get(staticTreasureWareLevel.getQuality());
                if (CheckNull.isNull(map)) {
                    map = new HashMap<>();
                    staticTreasureWareLevelMap.put(staticTreasureWareLevel.getQuality(), map);
                }
                map.put(staticTreasureWareLevel.getLevel(), staticTreasureWareLevel);
            });
        });

        treasureCombatMap = staticIniDao.selectStaticTreasureCombat();
        treasureCombatBuffs = staticIniDao.selectTreasureCombatBuffs();

        Optional.ofNullable(treasureCombatMap).ifPresent(map -> {
            treasureSection = new ArrayList<>();
            treasureCombatMap.values().forEach(treasureCombat -> {
                if (treasureSection.contains(treasureCombat.getSectionId()))
                    return;

                treasureSection.add(treasureCombat.getSectionId());
            });
            treasureSection = treasureSection.stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());

            // 找到每个章节的最大关卡
            treasureCombatMaxCombatMap = treasureCombatMap.values().stream()
                    .collect(
                            Collectors.groupingBy(
                                    StaticTreasureCombat::getSectionId,
                                    Collectors.collectingAndThen(
                                            Collectors.maxBy(Comparator.comparingInt(StaticTreasureCombat::getCombatId)),
                                            o -> o.map(StaticTreasureCombat::getCombatId).orElse(0)
                                    )
                            )
                    );
        });

        staticTreasureWareProfileMap = new HashMap<>();
        seasonTreasureWareProfileMap = new HashMap<>();
        List<StaticTreasureWareProfile> profileList = super.staticIniDao.selectTreasureWareProfile();
        if (!ObjectUtils.isEmpty(profileList)) {
            staticTreasureWareProfileMap = profileList
                    .stream().filter(profile -> profile.getSpecialId() == 0)
                    .collect(Collectors.groupingBy(StaticTreasureWareProfile::getAttrType,
                            Collectors.toMap(StaticTreasureWareProfile::getQuality, Function.identity(), (oldV, newV) -> newV)));
            seasonTreasureWareProfileMap = profileList
                    .stream().filter(profile -> profile.getSpecialId() != 0)
                    .collect(Collectors.toMap(StaticTreasureWareProfile::getSpecialId, Function.identity()));
        }
        LogUtil.common("------------------加载数据：宝具-----------------");
    }

    @Override
    public void check() {
        if (ObjectUtils.isEmpty(staticTreasureWareMakeMap)) {
            LogUtil.error("<<<<<<宝具打造表为空>>>>>>>");
        }
    }

    public static Map<Integer, Integer> getTreasureCombatMaxCombatMap() {
        return treasureCombatMaxCombatMap;
    }

    public static Map<Integer, StaticTreasureCombat> getTreasureCombatMap() {
        return treasureCombatMap;
    }
}
