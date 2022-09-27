package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.constant.SystemId;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName StaticPropDataMgr.java
 * @Description 道具、装备相关配置
 * @date 创建时间：2017年3月28日 上午11:05:01
 */
public class StaticPropDataMgr {

    /**
     * 引导蝎王解锁时间, 获取的将领
     */
    public static List<Integer> SCORPION_ACTIVATE_TIME;

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    /**
     * 道具
     */
    private static Map<Integer, StaticProp> propMap;

    /**
     * 道具跑马灯
     */
    private static Map<Integer, List<StaticPropChat>> propChatMap;

    /**
     * 装备
     */
    private static Map<Integer, StaticEquip> equipMap;

    /**
     * 国器
     */
    private static Map<Integer, StaticSuperEquip> superEquipMap;
    private static Map<String, StaticSuperEquipLv> superEquipLvMap;
    private static Map<Integer, StaticSuperEquipBomb> superEquipBombMap;

    /**
     * 装备额外属性
     */
    private static Map<Integer, Map<String, StaticEquipExtra>> partExtraMap;

    /**
     * 装备品质与额外属性关联信息
     */
    private static Map<Integer, StaticEquipQualityExtra> qualityMap;

    /**
     * 城池类型 对应 双倍征收卡 key:cityType ; value:propId
     */
    private static final Map<Integer, Integer> CITY_LEVY_CARD_MAP = new HashMap<>();

    /**
     * 宝石 key:宝石id
     */
    private static Map<Integer, StaticStone> stoneMap;
    /**
     * 宝石孔位 key:type 孔位
     */
    private static Map<Integer, StaticStoneHole> stoneHoleMap;
    /**
     * 宝石 key:type_lv
     */
    private static Map<String, StaticStone> stoneMapByTypeLvMap;
    /**
     * 宝石最大等级 key:type 宝石的类型
     */
    private static Map<Integer, Integer> stoneMaxLvByType;
    /**
     * 进阶后的宝石 key:id
     */
    private static Map<Integer, StaticStoneImprove> stoneImproveMap;
    /**
     * 进阶后的宝石 key:type_lv
     */
    private static Map<String, StaticStoneImprove> stoneImproveByTypeLvMap;
    /**
     * 随机道具配置 (propType=11的道具)
     */
    private static Map<Integer, StaticRandomProp> randomPropMap;
    /**
     * 权重次数随机箱
     */
    private static Map<Integer, StaticWeightBoxProp> weightBoxPropMap;
    /**
     * 戒指强化配置, key:equipId, val:<"level, StaticRingStrengthen">
     */
    private static Map<Integer, Map<Integer, StaticRingStrengthen>> ringStrengthenMap;
    /**
     * 戒指最低等级
     */
    public static int RING_STRENGTHEN_MIN;
    /**
     * 戒指最高等级
     */
    public static int RING_STRENGTHEN_MAX;
    /**
     * 宝石最小等级
     */
    public static int JEWEL_LEVEL_MIN;
    /**
     * 宝石最大等级
     */
    public static int JEWEL_LEVEL_MAX;
    /**
     * 装备宝石配置, key:level
     */
    private static Map<Integer, StaticJewel> jewelMap;

    public static void init() {
        Map<Integer, StaticProp> propMap = staticDataDao.selectPropMap();
        StaticPropDataMgr.propMap = propMap;

        List<StaticPropChat> staticPropChats = staticDataDao.selectPropChatList();
        StaticPropDataMgr.propChatMap = staticPropChats.stream().collect(Collectors.groupingBy(StaticPropChat::getSource));

        Map<Integer, StaticEquip> equipMap = staticDataDao.selectEquipMap();
        StaticPropDataMgr.equipMap = equipMap;

        List<StaticEquipExtra> extraList = staticDataDao.selectEquipExtraList();
        Map<Integer, Map<String, StaticEquipExtra>> partExtraMap = new HashMap<>();
        for (StaticEquipExtra extra : extraList) {
            List<Integer> equipPart = extra.getEquipPart();
            for (int part : equipPart) {
                Map<String, StaticEquipExtra> extraMap = partExtraMap.get(part);
                if (CheckNull.isNull(extraMap)) {
                    extraMap = new HashMap<>();
                    partExtraMap.put(part, extraMap);
                }
                extraMap.put(getMapKey(extra.getArrtId(), extra.getLevel()), extra);
            }
        }
        StaticPropDataMgr.partExtraMap = partExtraMap;

        Map<Integer, StaticEquipQualityExtra> qualityMap = staticDataDao.selectEquipQualityExtraMap();
        StaticPropDataMgr.qualityMap = qualityMap;

        Map<Integer, StaticSuperEquip> superEquipMap = staticDataDao.selectSuperEquipMap();
        StaticPropDataMgr.superEquipMap = superEquipMap;
        Map<Integer, StaticSuperEquipLv> superEquipLvMap = staticDataDao.selectSuperEquipLvMap();

        Map<String, StaticSuperEquipLv> superEquipLvMapTmp = new HashMap<>();
        for (StaticSuperEquipLv lv : superEquipLvMap.values()) {
            superEquipLvMapTmp.put(lv.getType() + "_" + lv.getLv(), lv);
        }
        StaticPropDataMgr.superEquipLvMap = superEquipLvMapTmp;
        Map<Integer, StaticSuperEquipBomb> superEquipBombMap = staticDataDao.selectSuperEquipBombMap();
        StaticPropDataMgr.superEquipBombMap = superEquipBombMap;
        StaticPropDataMgr.randomPropMap = staticDataDao.selectRandomPropMap();
        StaticPropDataMgr.weightBoxPropMap = staticDataDao.selectWeightBoxPropMap();

        // 初始化城市类型对应征收卡
        initCityLevyCard();
        // 宝石初始化
        initStone();
        // 装备扩展初始化
        initEquipExtra();

        SCORPION_ACTIVATE_TIME = SystemTabLoader.getListIntSystemValue(SystemId.SCORPION_ACTIVATE_TIME, "[600,]");
    }

    /**
     * 装备扩展初始化
     */
    private static void initEquipExtra() {
        StaticPropDataMgr.ringStrengthenMap = staticDataDao.selectRingStrengthenList().stream()
                .collect(Collectors.groupingBy(StaticRingStrengthen::getEquipId, HashMap::new,
                        Collectors.toMap(StaticRingStrengthen::getLevel, conf -> conf)));
        StaticPropDataMgr.RING_STRENGTHEN_MAX = StaticPropDataMgr.ringStrengthenMap.values().stream()
                .flatMapToInt(map -> map.keySet().stream().mapToInt(Integer::intValue)).max().getAsInt();
        StaticPropDataMgr.jewelMap = staticDataDao.selectJewelMap();
        StaticPropDataMgr.JEWEL_LEVEL_MIN = StaticPropDataMgr.jewelMap.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
        StaticPropDataMgr.JEWEL_LEVEL_MAX = StaticPropDataMgr.jewelMap.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
    }

    private static void initStone() {
        stoneMap = staticDataDao.selectStaticStone();
        stoneHoleMap = staticDataDao.selectStaticStoneHole();
        stoneMapByTypeLvMap = new HashMap<>();
        stoneMap.values().forEach(stone -> {
            String key = stone.getType() + "_" + stone.getLv();
            stoneMapByTypeLvMap.put(key, stone);
        });

        // 宝石最大等级 按类型分 Java8写法
        stoneMaxLvByType = stoneMap.values().stream().collect(Collectors.groupingBy(StaticStone::getType, Collectors
                .collectingAndThen(Collectors.maxBy(Comparator.comparingInt(StaticStone::getLv)),
                        stoneOption -> stoneOption.flatMap(sStone -> Optional.ofNullable(sStone.getLv())).orElse(0))));

        // 进阶宝石的配置
        stoneImproveMap = staticDataDao.selectStaticStoneImprove();
        stoneImproveByTypeLvMap = stoneImproveMap.values().stream()
                .collect(Collectors.toMap(si -> si.getType() + "_" + si.getLv(), si -> si));
    }

    private static void initCityLevyCard() {
        CITY_LEVY_CARD_MAP.put(1, PropConstant.ITEM_ID_5401);
        CITY_LEVY_CARD_MAP.put(2, PropConstant.ITEM_ID_5402);
        CITY_LEVY_CARD_MAP.put(3, PropConstant.ITEM_ID_5403);
        CITY_LEVY_CARD_MAP.put(4, PropConstant.ITEM_ID_5404);
        CITY_LEVY_CARD_MAP.put(5, PropConstant.ITEM_ID_5405);
        CITY_LEVY_CARD_MAP.put(6, PropConstant.ITEM_ID_5406);
    }

    /**
     * 根据道具id, 获取道具广播配置
     *
     * @param propId
     * @return
     */
    public static List<StaticPropChat> getPropChatByPropId(int propId) {
        return propChatMap.get(propId);
    }

    public static StaticRingStrengthen getRingConfByLv(int equipId, int lv) {
        Map<Integer, StaticRingStrengthen> map = ringStrengthenMap.get(equipId);
        if (!CheckNull.isEmpty(map)) {
            return map.get(lv);
        }
        return null;
    }

    public static StaticJewel getJewelByLv(int lv) {
        return jewelMap.get(lv);
    }

    public static Integer getCityLevyCardPorpId(int cityType) {
        return CITY_LEVY_CARD_MAP.get(cityType);
    }

    private static String getMapKey(int attrId, int lv) {
        return attrId + "_" + lv;
    }

    public static Map<Integer, StaticEquip> getEquipMap() {
        return equipMap;
    }

    public static StaticEquip getEquip(int equipId) {
        return equipMap.get(equipId);
    }

    public static Map<Integer, StaticProp> getPropMap() {
        return propMap;
    }

    public static StaticProp getPropMap(int id) {
        return propMap.get(id);
    }

    public static Map<Integer, StaticEquipQualityExtra> getQualityMap() {
        return qualityMap;
    }

    /**
     * 根据部位和唯一key, 获取装备技能上的配置
     *
     * @param attrId 唯一key
     * @param lv     等级
     * @param part   部位
     * @return
     */
    public static StaticEquipExtra getEuqipExtraByIdAndLv(int attrId, int lv, int part) {
        Map<String, StaticEquipExtra> extraMap = partExtraMap.get(part);
        if (!CheckNull.isNull(extraMap)) {
            return extraMap.get(getMapKey(attrId, lv));
        }
        return null;
    }

    /**
     * 根据神器类型获取对应神器的配置
     *
     * @param type 对应数据库中sanguo_ini_dev.s_super_quip的type字段
     * @return
     */
    public static StaticSuperEquip getSuperEquip(int type) {
        return superEquipMap.get(type);
    }

    public static StaticSuperEquipLv getSuperEquipLv(int type, int lv) {
        return superEquipLvMap.get(type + "_" + lv);
    }

    public static StaticSuperEquipBomb getSuperEquipBomb(int lv) {
        return superEquipBombMap.get(lv);
    }

    public static StaticStone getStoneMapById(int id) {
        return stoneMap.get(id);
    }

    public static StaticStone getStoneByTypeAndLv(int type, int lv) {
        String key = type + "_" + lv;
        return stoneMapByTypeLvMap.get(key);
    }

    public static Integer getStoneMaxLvByType(int type) {
        return stoneMaxLvByType.get(type);
    }

    public static StaticStoneHole getStoneHoleByIndex(int type) {
        return stoneHoleMap.get(type);
    }

    public static StaticRandomProp getRandomPropById(int propId) {
        return randomPropMap.get(propId);
    }

    public static StaticWeightBoxProp getWeightBoxPropMapById(int propId) {
        return weightBoxPropMap.get(propId);
    }

    public static StaticStoneImprove getStoneImproveById(int id) {
        return stoneImproveMap.get(id);
    }

    public static StaticStoneImprove getStoneImproveByTypeLv(int type, int lv) {
        String key = type + "_" + lv;
        return stoneImproveByTypeLvMap.get(key);
    }

}
