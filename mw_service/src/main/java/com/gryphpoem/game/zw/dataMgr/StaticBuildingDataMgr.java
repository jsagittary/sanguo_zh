package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;

/**
 * @ClassName StaticBuildingDataMgr.java
 * @Description 建筑相关
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 上午11:05:30
 *
 */
public class StaticBuildingDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
    public static final int CD_TYPE = 2;

    // key:buildingId
    private static Map<Integer, StaticBuildingInit> buildingInitMap; // --
    // key1:buildingType, key2:buildingId
    private static Map<Integer, Map<Integer, StaticBuildingInit>> buildingByTypeMap;  // --

    private static Map<Integer, StaticCommandMult> commandMultMap; // --
    private static Map<Integer, StaticFactoryRecruit> factoryRecruitMap;
    // key1:buildingId, key2:lv
    private static Map<Integer, Map<Integer, StaticFactoryRecruit>> factoryRecruit;
    // key1:buildingId, key2:lv
    private static Map<Integer, Map<Integer, StaticFactoryExpand>> factoryExpand;
    private static Map<Integer, StaticTechLv> techMap;
    private static Map<String, StaticTechLv> techLvMap;

    private static Map<Integer, StaticChemical> chemicalMap;
    private static Map<Integer, StaticChemicalExpand> chemicalExpandMap;

    private static Map<Integer, StaticTreasure> treasureMap;
    private static Map<Integer, Map<Integer, StaticTreasure>> typeTreasureMap;

    public static List<StaticCommandMult> commandMultMapCDType;// 加速类官员

    // Map<buildingId, Map<level, StaticBuildingLv>>
    private static Map<Integer, Map<Integer, StaticBuildingLv>> levelMap;

    // 城墙
    private static Map<Integer, StaticWallHero> wallHeroMap;
    private static Map<String, StaticWallHeroLv> wallHeroLvMap;
    private static Map<Integer, StaticWallHeroLv> wallIdHeroLvMap;

    // 战争工厂(内阁 )
    // key:id
    private static Map<Integer, StaticCabinetPlan> cabinetPlanMap;// 内阁天策府配置
    // key:点兵的level
    private static Map<Integer, StaticCabinetLv> cabinetLvMap;// 内阁天策府等级配置
    private static int cabinetMaxLv; // 点兵统领最大级别
    private static int cabinetMinLv; // 点兵统领最小级别
    // key:buildingId 新手指引建筑部分的存档配置
    private static Map<Integer, StaticGuideBuild> guideBuildMap;
    
    // 改建配置信息, key1:改建id, 改建消耗配置
    private static Map<Integer,StaticUptBuild> uptBuildMap;

    public static void init() {
        // 初始化建筑配置
        Map<Integer, StaticBuildingInit> buildingInitMap = staticDataDao.selectBuildingInitMap();
        StaticBuildingDataMgr.buildingInitMap = buildingInitMap;
        buildingByTypeMap = new HashMap<>();
        for (StaticBuildingInit building : buildingInitMap.values()) {
            Map<Integer, StaticBuildingInit> bmap = buildingByTypeMap.get(building.getBuildingType());
            if (bmap == null) {
                bmap = new HashMap<>();
                buildingByTypeMap.put(building.getBuildingType(), bmap);
            }
            bmap.put(building.getBuildingId(), building);
        }

        Map<Integer, StaticCommandMult> commandMultMap = staticDataDao.selectCommandMultMap();
        StaticBuildingDataMgr.commandMultMap = commandMultMap;
        Map<Integer, StaticFactoryRecruit> factoryRecruitMap = staticDataDao.selectFactoryRecruitMap();
        Map<Integer, Map<Integer, StaticFactoryRecruit>> factoryRecruit = new HashMap<>();
        for (StaticFactoryRecruit v : factoryRecruitMap.values()) {
            Map<Integer, StaticFactoryRecruit> map = factoryRecruit.get(v.getBuildingId());
            if (CheckNull.isEmpty(map)) {
                map = new HashMap<>();
                factoryRecruit.put(v.getBuildingId(), map);
            }
            map.put(v.getLv(), v);
        }
        StaticBuildingDataMgr.factoryRecruit = factoryRecruit;
        StaticBuildingDataMgr.factoryRecruitMap = factoryRecruitMap;
        Map<Integer, StaticFactoryExpand> factoryExpand = staticDataDao.selectFactoryExpandMap();
        Map<Integer, Map<Integer, StaticFactoryExpand>> factoryExpandMap = new HashMap<>();
        for (StaticFactoryExpand v : factoryExpand.values()) {
            Map<Integer, StaticFactoryExpand> map = factoryExpandMap.get(v.getType());
            if (map == null) {
                map = new HashMap<>();
                factoryExpandMap.put(v.getType(), map);
            }
            map.put(v.getCnt(), v);
        }
        StaticBuildingDataMgr.factoryExpand = factoryExpandMap;

        List<StaticCommandMult> commandMultMapCDType = new ArrayList<>();
        for (StaticCommandMult commandMult : commandMultMap.values()) {
            if (commandMult.getEffectType() == CD_TYPE) {
                commandMultMapCDType.add(commandMult);
            }
        }
        StaticBuildingDataMgr.commandMultMapCDType = commandMultMapCDType;

        Map<Integer, StaticTechLv> techMap = staticDataDao.selectTechLvMap();
        StaticBuildingDataMgr.techMap = techMap;

        Map<String, StaticTechLv> techLvMap = new HashMap<>();
        for (StaticTechLv techLv : techMap.values()) {
            techLvMap.put(techLv.getTechId() + "_" + techLv.getLv(), techLv);
        }
        StaticBuildingDataMgr.techLvMap = techLvMap;

        List<StaticBuildingLv> buildinglvList = staticDataDao.selectBuildingLvList();
        Map<Integer, Map<Integer, StaticBuildingLv>> levelMap = new HashMap<>();
        Map<Integer, StaticBuildingLv> map;
        for (StaticBuildingLv buildingLv : buildinglvList) {
            map = levelMap.get(buildingLv.getBuildingType());
            if (null == map) {
                map = new HashMap<>();
                levelMap.put(buildingLv.getBuildingType(), map);
            }
            map.put(buildingLv.getLevel(), buildingLv);
        }
        StaticBuildingDataMgr.levelMap = levelMap;

        Map<Integer, StaticChemical> chemicalMap = staticDataDao.selectChemicalMap();
        StaticBuildingDataMgr.chemicalMap = chemicalMap;

        Map<Integer, StaticChemicalExpand> chemicalExpandMap = staticDataDao.selectChemicalExpandMap();
        StaticBuildingDataMgr.chemicalExpandMap = chemicalExpandMap;

        Map<Integer, Map<Integer, StaticTreasure>> typeTreasureMap = new HashMap<>();
        Map<Integer, StaticTreasure> treasureMap = staticDataDao.selectTreasureMap();
        for (StaticTreasure treasure : treasureMap.values()) {
            Map<Integer, StaticTreasure> tmap = typeTreasureMap.get(treasure.getType());
            if (null == tmap) {
                tmap = new HashMap<>();
                typeTreasureMap.put(treasure.getType(), tmap);
            }
            tmap.put(treasure.getId(), treasure);
        }
        StaticBuildingDataMgr.typeTreasureMap = typeTreasureMap;
        StaticBuildingDataMgr.treasureMap = treasureMap;

        Map<Integer, StaticWallHero> wallHeroMap = staticDataDao.selectWallHeroMap();
        StaticBuildingDataMgr.wallHeroMap = wallHeroMap;

        Map<Integer, StaticWallHeroLv> wallHeroLvMap = staticDataDao.selectWallHeroLvMap();
        Map<String, StaticWallHeroLv> wallHeroLvMapTmp = new HashMap<>();
        for (StaticWallHeroLv obj : wallHeroLvMap.values()) {
            wallHeroLvMapTmp.put(obj.getHeroId() + "_" + obj.getLv(), obj);
        }
        StaticBuildingDataMgr.wallHeroLvMap = wallHeroLvMapTmp;
        StaticBuildingDataMgr.wallIdHeroLvMap = wallHeroLvMap;

        // 内阁相关配置加载
        StaticBuildingDataMgr.cabinetPlanMap = staticDataDao.selectCabinetPlanMap();
        StaticBuildingDataMgr.cabinetLvMap = staticDataDao.selectCabinetLv();

        int maxLv = 0;
        int minLv = Integer.MAX_VALUE;
        for (Integer lv : cabinetLvMap.keySet()) {
            if (lv > maxLv) {
                maxLv = lv;
            }
            if (lv < minLv) {
                minLv = lv;
            }
        }
        StaticBuildingDataMgr.cabinetMaxLv = maxLv;
        StaticBuildingDataMgr.cabinetMinLv = minLv;

        StaticBuildingDataMgr.guideBuildMap = staticDataDao.selectGuideBuildMap();
        
        //读取改建配置信息
        List<StaticUptBuild> uptBuildList = staticDataDao.selectUptBuildList();
        Map<Integer,StaticUptBuild> uptBuildMap = new HashMap<>();
        for (StaticUptBuild spb : uptBuildList) {
            uptBuildMap.put(spb.getKeyId(), spb);
        }
        StaticBuildingDataMgr.uptBuildMap = uptBuildMap;
    }

    /**
     * 获取核弹的产出配置
     * @return
     */
    public static List<Integer> getBobmConf() {
        List<Integer> propConf = null;
        StaticBuildingLv sl = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.SMALL_GAME_HOUSE, 1);
        List<List<Integer>> capacity = sl.getCapacity();
        if (!CheckNull.isEmpty(capacity)) {
            propConf = capacity.get(0);
        }
        return propConf;
    }

    public static StaticTreasure getTreasureMap(int id) {
        return treasureMap.get(id);
    }

    public static Map<Integer, StaticTreasure> getTypeTreasureMap(int type) {
        return typeTreasureMap.get(type);
    }

    public static StaticCommandMult getCommandMult(int id) {
        return commandMultMap.get(id);
    }

    public static Map<Integer, StaticCommandMult> getCommandMultMap() {
        return commandMultMap;
    }

    public static Map<Integer, StaticBuildingInit> getBuildingInitMap() {
        return buildingInitMap;
    }

    public static StaticBuildingInit getBuildingInitMapById(int id) {
        return buildingInitMap.get(id);
    }

    public static StaticFactoryRecruit getStaticFactoryRecruit(int id) {
        return factoryRecruitMap.get(id);
    }

    public static StaticFactoryRecruit getStaticFactoryRecruit(int id, int lv) {
        return factoryRecruit.get(id) != null ? factoryRecruit.get(id).get(lv) : null;
    }

    public static StaticFactoryExpand getStaticFactoryExpand(int id, int lv) {
        return factoryExpand.get(id) != null ? factoryExpand.get(id).get(lv) : null;
    }

    public static StaticBuildingLv getStaticBuildingLevel(int buildingType, int buildLevel) {
        Map<Integer, StaticBuildingLv> indexIdMap = levelMap.get(buildingType);
        if (indexIdMap != null) {
            return indexIdMap.get(buildLevel);
        }
        return null;
    }

    public static StaticTechLv getTechMap(int id) {
        return techMap.get(id);
    }

    public static Map<Integer, StaticTechLv> getTechMap() {
        return techMap;
    }

    public static StaticTechLv getTechLvMap(int techId, int lv) {
        return techLvMap.get(techId + "_" + lv);
    }

    public static StaticChemical getChemicalMap(int id) {
        return chemicalMap.get(id);
    }

    public static StaticChemicalExpand getChemicalExpandMap(int cnt) {
        return chemicalExpandMap.get(cnt);
    }

    public static int getMaxChemicalNum() {
        StaticChemicalExpand staticChemicalExpand = chemicalExpandMap.values().stream().
                max(Comparator.comparingInt(StaticChemicalExpand::getNum)).orElse(null);
        return staticChemicalExpand == null ? 0 : staticChemicalExpand.getNum();
    }

    public static StaticWallHero getWallHero(int id) {
        return wallHeroMap.get(id);
    }

    public static Map<Integer, StaticWallHero> getWallHeroMap() {
        return wallHeroMap;
    }

    public static StaticWallHeroLv getWallHeroLv(int heroId, int lv) {
        return wallHeroLvMap.get(heroId + "_" + lv);
    }

    public static StaticCabinetPlan getCabinetPlanById(int id) {
        return cabinetPlanMap.get(id);
    }

    public static StaticCabinetLv getCabinetLvByLv(int lv) {
        return cabinetLvMap.get(lv);
    }

    public static int getCabinetMaxLv() {
        return cabinetMaxLv;
    }

    public static int getCabinetMinLv() {
        return cabinetMinLv;
    }

    public static Map<Integer, Map<Integer, StaticBuildingInit>> getBuildingByTypeMap() {
        return buildingByTypeMap;
    }

    public static Map<Integer, StaticBuildingInit> getBuildingByTypeMapByType(int type) {
        return buildingByTypeMap.get(type);
    }

    public static StaticGuideBuild getGuideBuildMapById(int buildingId) {
        return guideBuildMap.get(buildingId);
    }
    
    public static Map<Integer,StaticUptBuild> getUptBuildMap() {
    	return uptBuildMap;
    }

    public static StaticWallHeroLv getStaticWallHeroLv(int id) {
        return wallIdHeroLvMap.get(id);
    }
    
    /**
     * 
    * @Title: getUptBuildConfig
    * @Description: 根据keyId 获取配置
    * @param keyId
    * @return    参数
    * StaticUptBuild    返回类型
    * @throws
     */
    public static StaticUptBuild getUptBuildConfig(int keyId) {
    	StaticUptBuild uptBuild = uptBuildMap.get(keyId);
    	if (null == uptBuild) {
    		return null;
    	}
    	return uptBuild;
    }
}
