package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-12 11:37
 * @description:
 * @modified By:
 */
public class StaticWarPlaneDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // key:warPlaneId
    private static Map<Integer, StaticPlaneUpgrade> warPlaneMap;

    // key:warPlaneType
    private static Map<Integer, StaticPlaneInit> planeInitMap;

    private static List<StaticPlaneLv> planeLvs;

    private static List<StaticPlaneSearch> planeSearches;

    // key:searchType
    private static Map<Integer, List<StaticPlaneSearch>> planeSearchesTypeMap;

    // key:skillId
    private static Map<Integer, StaticPlaneSkill> planeSkillMap;

    // key:chipId
    private static Map<Integer, StaticPlaneTransform> planeTransformIdMap;
    // key:planeType
    private static Map<Integer, StaticPlaneTransform> planeTransformTypeMap;

    public static void init() {
        StaticWarPlaneDataMgr.warPlaneMap = staticDataDao.selectPlaneUpgradeMap();

        StaticWarPlaneDataMgr.planeInitMap = staticDataDao.selectPlaneInitMap();

        StaticWarPlaneDataMgr.planeLvs = staticDataDao.selectPlaneLvList();

        StaticWarPlaneDataMgr.planeSearches = staticDataDao.selectPlaneSearchList();

        HashMap<Integer, List<StaticPlaneSearch>> planeSearchesTypeMap = new HashMap<>();
        StaticWarPlaneDataMgr.planeSearches.forEach(e -> {
            int type = e.getType();
            List<StaticPlaneSearch> searchList = planeSearchesTypeMap.get(type);
            if (CheckNull.isNull(searchList)) {
                searchList = new ArrayList<>();
                planeSearchesTypeMap.put(type, searchList);
            }
            searchList.add(e);
        });
        StaticWarPlaneDataMgr.planeSearchesTypeMap = planeSearchesTypeMap;

        StaticWarPlaneDataMgr.planeSkillMap = staticDataDao.selectPlaneSkillMap();

        StaticWarPlaneDataMgr.planeTransformIdMap = staticDataDao.selectPlaneTransform();
        Map<Integer, StaticPlaneTransform> planeTransformTypeMap = new HashMap<>();
        StaticWarPlaneDataMgr.planeTransformIdMap.values().forEach(e -> {
            int planeType = e.getPlaneType();
            planeTransformTypeMap.put(planeType, e);
        });
        StaticWarPlaneDataMgr.planeTransformTypeMap = planeTransformTypeMap;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????0
     * @param quality ??????
     * @param qualityLv ????????????
     * @param planeLv ????????????
     * @return
     */
    public static int getExpByQuality(int quality, int qualityLv,  int planeLv) {
        StaticPlaneLv sPlaneLv = planeLvs.stream().filter(staticPlaneLv -> staticPlaneLv.getQuality() == quality
                && staticPlaneLv.getQualityLevel() == qualityLv && staticPlaneLv.getLevel() == planeLv).findFirst()
                .orElse(null);
        return CheckNull.isNull(sPlaneLv) ? 0 : sPlaneLv.getExp();
    }

    /**
     * ????????????????????????
     * @return
     */
    public static Map<Integer, StaticPlaneUpgrade> getWarPlaneMap() {
        return warPlaneMap;
    }

    /**
     * ????????????id?????????????????????????????????
     * @param planeId
     * @return
     */
    public static StaticPlaneUpgrade getPlaneUpgradeById(int planeId) {
        return warPlaneMap.get(planeId);
    }

    /**
     * ????????????type????????????????????????
     * @param planeType
     * @return
     */
    public static StaticPlaneInit getPlaneInitByType(int planeType) {
        return planeInitMap.get(planeType);
    }

    /**
     * ??????????????????????????????????????????
     * @param searchType
     * @return
     */
    public static List<StaticPlaneSearch> getPlaneSearchesByType(int searchType) {
        return planeSearchesTypeMap.get(searchType);
    }

    /**
     * ????????????Id????????????????????????
     * @param chipId
     * @return
     */
    public static StaticPlaneTransform getPlaneTransformById(int chipId) {
        return planeTransformIdMap.get(chipId);
    }

    /**
     * ????????????Type????????????????????????
     * @param planeType
     * @return
     */
    public static StaticPlaneTransform getPlaneTransformByType(int planeType) {
        return planeTransformTypeMap.get(planeType);
    }

    /**
     * ????????????????????????????????????
     * @param condition
     * @return
     */
    public static StaticPlaneUpgrade getPlaneMaxLvByFilter(Predicate<StaticPlaneUpgrade> condition) {
        return warPlaneMap.values().stream().filter(condition).findFirst().orElse(null);
    }

    /**
     * ??????????????????Id??????????????????
     * @param skillId
     * @return
     */
    public static StaticPlaneSkill getPlaneSkillById(int skillId) {
        return planeSkillMap.get(skillId);
    }

}
