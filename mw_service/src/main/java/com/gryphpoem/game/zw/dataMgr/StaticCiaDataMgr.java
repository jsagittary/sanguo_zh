package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.p.FemaleAgent;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgent;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgentGift;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgentStar;
import com.gryphpoem.game.zw.resource.domain.s.StaticBarrage;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName StaticCiaDataMgr.java
 * @Description 情报部
 * @author QiuKun
 * @date 2018年6月5日
 */
public class StaticCiaDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // key: agentId, val: <unlockType 类型, condition 条件>
    private static Map<Integer, List<Integer>> agentUnlockMap;
    private static Map<Integer, List<Integer>> agentUnlockMap1;

    // key: agentId, val: <StaticAgent 特工配置>
    private static Map<Integer, List<StaticAgent>> agentIdMap;

    private static Map<Integer, StaticAgentGift> agentGiftMap;
    // 弹幕
    private static Map<Integer, StaticBarrage> barrageMap;
    
    //特工星级配置
    private static Map<Integer, List<StaticAgentStar>> agentStarMap;

    // key: agentId, maxStar
    private static Map<Integer, Integer> agentMaxStarMap;

    public static void init() {
        List<StaticAgent> agentList = staticDataDao.selectAgentList();
        StaticCiaDataMgr.agentUnlockMap = agentList
                .stream()
                .collect(Collectors.toMap(StaticAgent::getId, StaticAgent::getUnlock,
                        (oldV, newV) -> {
                            if (!CheckNull.isEmpty(newV)) {
                                return newV;
                            }
                            return oldV;
                        }));
        StaticCiaDataMgr.agentUnlockMap1 = agentList
                .stream()
                .collect(Collectors.toMap(StaticAgent::getId, StaticAgent::getUnlock1,
                        (oldV, newV) -> {
                            if (!CheckNull.isEmpty(newV)) {
                                return newV;
                            }
                            return oldV;
                        }));

        StaticCiaDataMgr.agentIdMap = agentList
                .stream()
                .collect(Collectors.groupingBy(StaticAgent::getId));
        agentIdMap.values().forEach(tmps -> Collections.sort(tmps,Comparator.comparing(StaticAgent::getQuality)));
        agentGiftMap = staticDataDao.selectAgentGiftMap();
        barrageMap = staticDataDao.selectBarrage();
        List<StaticAgentStar> agentStarList = staticDataDao.selectAgentStarList();
        StaticCiaDataMgr.agentStarMap = agentStarList
                .stream()
                .collect(Collectors.groupingBy(StaticAgentStar::getAgentId));
        StaticCiaDataMgr.agentMaxStarMap = agentStarList
                .stream()
                .collect(Collectors.toMap(StaticAgentStar::getAgentId, StaticAgentStar::getStar, (oldV, newV) -> newV > oldV ? newV : oldV));
    }

    public static Map<Integer, StaticAgentGift> getAgentGiftMap() {
        return agentGiftMap;
    }

    public static StaticAgentGift getAgentGiftById(int giftId) {
        return agentGiftMap.get(giftId);
    }

    public static StaticBarrage getBarrageById(int id) {
        return barrageMap.get(id);
    }

    public static Map<Integer, List<Integer>> getAgentUnlockMap() {
        return agentUnlockMap;
    }


    /**
     * 根据agentId获取解锁条件
     *
     * @param id id
     * @return 解锁条件 list<解锁类型, 解锁条件> null 默认开启
     */
    public static List<Integer> getUnlockById(int id) {
        return agentUnlockMap.get(id);
    }

    public static List<Integer> getUnlockById1(int id){
        return agentUnlockMap1.get(id);
    }

    /**
     * 获取所有的特工id
     *
     * @return 特工ids
     */
    public static Set<Integer> getAgentIds() {
        return agentUnlockMap.keySet();
    }

    /**
     * 根据指定特工的好感度, 获取特工的配置
     *
     * @param id       特工id
     * @param intimacy 当前的好感度
     * @return 特工配置
     */
    private static StaticAgent getAgentConfByIdAndIntimacy(int id, int intimacy) {
        List<StaticAgent> sAgents = agentIdMap.get(id);
        if (!CheckNull.isEmpty(sAgents)) {
            return sAgents.stream().filter(sAgent -> intimacy >= sAgent.getIntimacyVal()).max(Comparator.comparingInt(StaticAgent::getIntimacyVal)).orElse(null);
        }
        return null;
    }

    /**
     * <b>
     * <p>注意：这里取不到配置有两种可能<br/>
     * 1: 策划没有配置<br/>
     * 2: 好感度没达到
     * </p>
     * </b>
     * <br/
     * 根据指定特工获取特工的配置
     *
     * @param agent 特工
     * @return 特工配置
     */
    public static StaticAgent getAgentConfByAgent(FemaleAgent agent) {
        return getAgentConfByIdAndIntimacy(agent.getId(), agent.getExp());
    }

    /**
     * 获取特工的所有配置
     *
     * @param id agentId
     * @return 特工配置
     */
    public static List<StaticAgent> getAgentConfById(int id) {
        return agentIdMap.get(id);
    }

    /**
     * 根据特工id获取特工最大好感度
     * @param id agentId
     * @return 特工配置
     */
    public static StaticAgent getAgentMaxExp(int id){
        List<StaticAgent> sAgents = agentIdMap.get(id);
        if (!CheckNull.isEmpty(sAgents)) {
            return sAgents.stream().max(Comparator.comparingInt(StaticAgent::getIntimacyVal)).orElse(null);
        }
        return null;
    }

    /**
     * 根据特工id和星级获取星级配置
     * @param agentId
     * @param star
     * @return 星级配置
     */
    public static StaticAgentStar getStaticAgentStar(int agentId,int star){
        List<StaticAgentStar> sAgentStars = agentStarMap.get(agentId);
        if (!CheckNull.isEmpty(sAgentStars)){
            return sAgentStars.stream().filter(sAgentStar -> star <= sAgentStar.getStar()).min(Comparator.comparingInt(StaticAgentStar::getStar)).orElse(null);
        }
        return null;
    }

    /**
     * 根据指定特工获取特工星级的配置
     * @param fa 特工
     * @return 特工星级的配置
     */
    public static StaticAgentStar getStaticAgentStar(FemaleAgent fa) {
        return getStaticAgentStar(fa.getId(), fa.getStar());
    }

    public static StaticAgent getStaticAgentByQuality(FemaleAgent fa, int maxQuality) {
        List<StaticAgent> sAgentStars = agentIdMap.get(fa.getId());
        if (!CheckNull.isEmpty(sAgentStars)){
            return sAgentStars.stream().filter(sAgentStar -> maxQuality == sAgentStar.getQuality()).findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 获取指定特工最大星级
     * @param agentId 特工id
     * @return 最大星级
     */
    public static int getAgentMaxStar(int agentId) {
        return agentMaxStarMap.getOrDefault(agentId, 0);
    }

    public static Map<Integer, List<Integer>> getAgentUnlockMap1() {
        return agentUnlockMap1;
    }

    public static void setAgentUnlockMap1(Map<Integer, List<Integer>> agentUnlockMap1) {
        StaticCiaDataMgr.agentUnlockMap1 = agentUnlockMap1;
    }
}
