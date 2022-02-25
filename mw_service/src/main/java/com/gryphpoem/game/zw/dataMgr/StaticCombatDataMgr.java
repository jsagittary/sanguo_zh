package com.gryphpoem.game.zw.dataMgr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticMultCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticPitchCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticStoneCombat;

public class StaticCombatDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 普通副本
    private static Map<Integer, StaticCombat> combatMap;

    private static Map<Integer, List<StaticCombat>> combatPreIdMap;

    // 宝石副本
    private static Map<Integer, StaticStoneCombat> stoneCombatMap;
    // 宝石副本前置副本
    private static Map<Integer, List<StaticStoneCombat>> stoneCombatPreIdMap;

    // 荣耀演练场副本 key:combatId
    private static Map<Integer, StaticPitchCombat> pitchCombatMap;
    // 荣耀演练场副本分组 key1:type, value: 会按照combatId从小到大进行排序
    private static Map<Integer, List<StaticPitchCombat>> pitchCombatGroupByType;
    // 多人副本<combatId,MultCombat>
    private static Map<Integer, StaticMultCombat> multCombat;

    // private static Map<Integer, StaticSection> sectionMap;

    // private static StaticSection equipSection;
    //
    // private static StaticSection partSection;
    //
    // private static StaticSection timeSection;
    //
    // private static StaticSection medalSection;

    public static void init() {
        // initSection();
        initCombat();
    }

    // private static void initSection() {
    // Map<Integer, StaticSection> sectionMap = staticDataDao.selectSection();
    // if (sectionMap == null) {
    // sectionMap = new HashMap<>();
    // }
    // StaticCombatDataMgr.sectionMap = sectionMap;
    //
    // Iterator<StaticSection> it = sectionMap.values().iterator();
    // while (it.hasNext()) {
    // StaticSection staticSection = (StaticSection) it.next();
    // if (staticSection.getType() == 2) {
    // StaticCombatDataMgr.equipSection = staticSection;
    // } else if (staticSection.getType() == 3) {
    // StaticCombatDataMgr.partSection = staticSection;
    // } else if (staticSection.getType() == 4) {
    // StaticCombatDataMgr.timeSection = staticSection;
    // } else if (staticSection.getType() == 9) {
    // StaticCombatDataMgr.medalSection = staticSection;
    // }
    // }
    // }

    private static void initCombat() {
        Map<Integer, StaticCombat> combatMap = staticDataDao.selectCombatMap();
        Map<Integer, List<StaticCombat>> combatPreIdMap = new HashMap<>();
        for (StaticCombat staticCombat : combatMap.values()) {
            List<StaticCombat> list = combatPreIdMap.get(staticCombat.getPreId());
            if (list == null) {
                list = new ArrayList<>();
                combatPreIdMap.put(staticCombat.getPreId(), list);
            }
            list.add(staticCombat);
        }
        StaticCombatDataMgr.combatMap = combatMap;
        StaticCombatDataMgr.combatPreIdMap = combatPreIdMap;

        // 宝石副本
        Map<Integer, StaticStoneCombat> stoneCombatMap = staticDataDao.selectStoneCombatMap();
        Map<Integer, List<StaticStoneCombat>> stoneCombatPreIdMap = new HashMap<>();
        for (StaticStoneCombat c : stoneCombatMap.values()) {
            List<StaticStoneCombat> list = stoneCombatPreIdMap.get(c.getPreId());
            if (list == null) {
                list = new ArrayList<>();
                stoneCombatPreIdMap.put(c.getPreId(), list);
            }
            list.add(c);
        }
        StaticCombatDataMgr.stoneCombatMap = stoneCombatMap;
        StaticCombatDataMgr.stoneCombatPreIdMap = stoneCombatPreIdMap;

        // 荣耀演练场副本
        StaticCombatDataMgr.pitchCombatMap = staticDataDao.selectPitchCombatMap();
        StaticCombatDataMgr.pitchCombatGroupByType = pitchCombatMap.values().stream()
                .collect(Collectors.groupingBy(StaticPitchCombat::getType,
                        Java8Utils.toSortedList(Comparator.comparing(StaticPitchCombat::getCombatId))));

        // 多人副本
        StaticCombatDataMgr.multCombat = staticDataDao.selectMultCombatMap();

        // int preId = 0;
        // Map<Integer, StaticCombat> tmpCombatMap = new HashMap<>();
        // for (StaticCombat staticCombat : combatMap.values()) {
        // int combatId = staticCombat.getCombatId();
        // tmpCombatMap.put(combatId, staticCombat);
        //
        //// StaticSection staticSection = sectionMap.get(staticCombat.getSectionId());
        //// if (staticSection.getStartId() == 0) {
        //// staticSection.setStartId(combatId);
        //// }
        //// if (staticSection.getEndId() < combatId) {
        //// staticSection.setEndId(combatId);
        //// }
        //
        // staticCombat.setPreId(preId);
        // preId = staticCombat.getCombatId();
        // }

    }

    // public static StaticSection getStaticSection(int sectionId) {
    // return sectionMap.get(sectionId);
    // }

    public static Map<Integer, StaticCombat> getCombatMap() {
        return combatMap;
    }

    public static StaticCombat getStaticCombat(int combatId) {
        return combatMap.get(combatId);
    }

    public static int getStaticCombatSize() {
        return combatMap.size();
    }

    public static List<StaticCombat> getPreIdCombat(int preId) {
        return combatPreIdMap.get(preId);
    }

    /*----------------------------宝石副本相关----------------------------------*/
    public static Map<Integer, StaticStoneCombat> getStoneCombatMap() {
        return stoneCombatMap;
    }

    public static StaticStoneCombat getStoneCombatById(int combatId) {
        return stoneCombatMap.get(combatId);
    }

    public static Map<Integer, List<StaticStoneCombat>> getStoneCombatPreIdMap() {
        return stoneCombatPreIdMap;
    }

    public static List<StaticStoneCombat> getStonePreCombat(int preId) {
        return stoneCombatPreIdMap.get(preId);
    }

    /*----------------------------荣耀演练场副本相关----------------------------------*/
    public static Map<Integer, StaticPitchCombat> getPitchCombatMap() {
        return pitchCombatMap;
    }

    public static List<StaticPitchCombat> getPitchCombatGroupByType(int type) {
        return pitchCombatGroupByType.get(type);
    }

    public static StaticMultCombat getMultCombatById(int combatId) {
        return multCombat.get(combatId);
    }

    public static Map<Integer, StaticMultCombat> getMultCombatMap() {
        return multCombat;
    }

}
