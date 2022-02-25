package com.gryphpoem.game.zw.dataMgr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossFort;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossPersonalTrophy;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossServerRule;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossWarRank;

/**
 * @ClassName StaticCrossDataMgr.java
 * @Description 跨服相关的配置数据
 * @author QiuKun
 * @date 2019年5月11日
 */
public class StaticCrossDataMgr {
    private static StaticDataDao staticDataDao;

    // 跨服规则
    private static List<StaticCrossServerRule> ruleList;
    // <id,StaticCrossFort> 跨服的堡垒信息
    private static Map<Integer, StaticCrossFort> fortMap;
    // 大本营的堡垒 <camp,StaticCrossFort>
    private static Map<Integer, StaticCrossFort> fortMapByCamp;

    // 跨服buff key:buffId val:buff
    private static Map<Integer, StaticCrossBuff> buffMap;
    // 跨服buff key:type_lv val:buff
    private static Map<String, StaticCrossBuff> buffTypeLvMap;

    // 排行榜的奖励配置 <type,<ranking,StaticCrossWarRank>>
    private static Map<Integer, Map<Integer, StaticCrossWarRank>> rankMap;
    // 个人成就 <id,StaticCrossPersonalTrophy>
    private static Map<Integer, StaticCrossPersonalTrophy> personalTropyMap;

    /**
     * 初始化
     */
    public static void init() {
        if (staticDataDao == null) {
            staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
        }

        StaticCrossDataMgr.ruleList = staticDataDao.selectStaticCrossServerRuleList();
        StaticCrossDataMgr.fortMap = staticDataDao.selectStaticCrossFortMap();
        StaticCrossDataMgr.fortMapByCamp = fortMap.values().stream().filter(StaticCrossFort::isCampType)
                .collect(Collectors.toMap(StaticCrossFort::getCamp, c -> c));

        // buff相关
        StaticCrossDataMgr.buffMap = staticDataDao.selectStaticCrossBuffMap();
        StaticCrossDataMgr.buffTypeLvMap = buffMap.values().stream()
                .collect(Collectors.toMap(StaticCrossBuff::getMapKey, buff -> buff));
        List<StaticCrossWarRank> sCrossWarRankList = staticDataDao.selectStaticCrossWarRankList();
        StaticCrossDataMgr.rankMap = sCrossWarRankList.stream().collect(Collectors
                .groupingBy(StaticCrossWarRank::getType, Collectors.toMap(StaticCrossWarRank::getRanking, v -> v)));

        StaticCrossDataMgr.personalTropyMap = staticDataDao.selectStaticCrossPersonalTrophyMap();
    }

    public static List<StaticCrossServerRule> getRuleList() {
        return ruleList;
    }

    public static Map<Integer, StaticCrossFort> getFortMap() {
        return fortMap;
    }

    public static StaticCrossFort getFortById(int id) {
        return fortMap.get(id);
    }

    public static StaticCrossBuff getBuffByBuffId(int buffId) {
        return buffMap.get(buffId);
    }

    public static StaticCrossBuff getBuffByTypeLv(int type, int lv) {
        String key = StaticCrossBuff.mapKey(type, lv);
        return buffTypeLvMap.get(key);
    }

    /**
     * 获取大本营堡垒
     * 
     * @param camp
     */
    public static StaticCrossFort getCampFort(int camp) {
        return fortMapByCamp.get(camp);
    }

    public static StaticCrossPersonalTrophy getPersonalTropyMap(int id) {
        return personalTropyMap.get(id);
    }

    public static Map<Integer, StaticCrossWarRank> getRankByType(int type) {
        return rankMap.get(type);
    }
}
