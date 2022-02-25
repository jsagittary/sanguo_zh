package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName StaticPartyDataMgr.java
 * @Description 军团相关配置信息
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午4:10:51
 *
 */
public class StaticPartyDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 军团等级配置信息, key:lv
    private static Map<Integer, StaticPartyLv> partyLvMap;

    private static int maxPartyLv;

    private static int minPartyLv;

    // 军团建设配置信息, key1:军团等级, key2:建设次数
    private static Map<Integer, Map<Integer, StaticPartyBuild>> buildMap;
    
    // 军团军阶配置信息, key:ranks
    private static Map<Integer, StaticPartyRanks> ranksMap;

    private static int maxPartyRanks;

    // 军团官职配置信息
    private static Map<Integer, StaticPartyJob> jobMap;

    // 军团荣誉礼包信息<id,Object>
    private static Map<Integer, StaticPartyHonorGift> honorGiftMap;

    // 军团荣誉排行榜配置信息
    private static Map<Integer, Map<Integer, StaticPartyHonorRank>> honorRankMap;

    /**
     * 军团补给箱, key=id, val=补给箱配置
     */
    private static Map<Integer, StaticPartySupply> partySupplyMap;

    /**
     * 军团超级补给箱, key=lv, val=超级补给箱配置
     */
    private static Map<Integer, StaticPartySuperSupply> partySuperSupplyMap;

    /**
     * 超级补给箱最小等级
     */
    private static int minSuperSupplyLv;

    /**
     * 超级补给箱最大等级
     */
    private static int maxSuperSupplyLv;

    public static int getMinSuperSupplyLv() {
        return minSuperSupplyLv;
    }

    public static int getMaxSuperSupplyLv() {
        return maxSuperSupplyLv;
    }

    public static void init() {
        Map<Integer, StaticPartyLv> partyLvMap = staticDataDao.selectPartyLvMap();
        int maxLv = 0;
        int minLv = Integer.MAX_VALUE;
        for (Integer partyLv : partyLvMap.keySet()) {
            if (partyLv > maxLv) {
                maxLv = partyLv;
            }
            if (partyLv < minLv) {
                minLv = partyLv;
            }
        }
        StaticPartyDataMgr.partyLvMap = partyLvMap;
        StaticPartyDataMgr.maxPartyLv = maxLv;
        StaticPartyDataMgr.minPartyLv = minLv;

        List<StaticPartyBuild> buildList = staticDataDao.selectPartyBuildList();
        Map<Integer, Map<Integer, StaticPartyBuild>> buildMap = new HashMap<>();
        for (StaticPartyBuild spb : buildList) {
            Map<Integer, StaticPartyBuild> map = buildMap.get(spb.getPartyLv());
            if (null == map) {
                map = new HashMap<>();
                buildMap.put(spb.getPartyLv(), map);
            }
            map.put(spb.getBuild(), spb);
        }
        StaticPartyDataMgr.buildMap = buildMap;
        
        Map<Integer, StaticPartyRanks> ranksMap = staticDataDao.selectPartyRanksMap();
        for (Integer ranks : ranksMap.keySet()) {
            if (ranks > maxLv) {
                maxLv = ranks;
            }
        }
        StaticPartyDataMgr.ranksMap = ranksMap;
        StaticPartyDataMgr.maxPartyRanks = maxLv;

        Map<Integer, StaticPartyJob> jobMap = staticDataDao.selectPartyJobMap();
        StaticPartyDataMgr.jobMap = jobMap;

        Map<Integer, StaticPartyHonorGift> honorGiftMap = staticDataDao.selectPartyHonorGiftMap();
        StaticPartyDataMgr.honorGiftMap = honorGiftMap;

        Map<Integer, Map<Integer, StaticPartyHonorRank>> honorRankMap = new HashMap<>();
        List<StaticPartyHonorRank> rankList = staticDataDao.selectPartyHonorRankList();
        for (StaticPartyHonorRank rank : rankList) {
            Map<Integer, StaticPartyHonorRank> map = honorRankMap.get(rank.getRankType());
            if (null == map) {
                map = new HashMap<>();
                honorRankMap.put(rank.getRankType(), map);
            }
            map.put(rank.getRank(), rank);
        }
        StaticPartyDataMgr.honorRankMap = honorRankMap;

        StaticPartyDataMgr.partySupplyMap = staticDataDao.selectPartySupplyMap();
        StaticPartyDataMgr.partySuperSupplyMap = staticDataDao.selectPartySuperSupplyMap();
        StaticPartyDataMgr.maxSuperSupplyLv = StaticPartyDataMgr.partySuperSupplyMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        StaticPartyDataMgr.minSuperSupplyLv = StaticPartyDataMgr.partySuperSupplyMap.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);

    }

    public static Map<Integer, StaticPartySupply> getPartySupplyMap() {
        return partySupplyMap;
    }

    public static List<StaticPartySupply> getPartySupplyByType(int type) {
        return partySupplyMap.values().stream().filter(ps -> ps.getType() == type).collect(Collectors.toList());
    }

    public static StaticPartySupply getPartySupply(int id) {
        return partySupplyMap.get(id);
    }

    public static StaticPartySuperSupply getPartySuperSupply(int lv) {
        return partySuperSupplyMap.get(lv);
    }

    public static Map<Integer, StaticPartyLv> getPartyLvMap() {
        return partyLvMap;
    }

    public static Map<Integer, Map<Integer, StaticPartyBuild>> getBuildMap() {
        return buildMap;
    }
    
    public static Map<Integer, StaticPartyRanks> getRanksMap() {
        return ranksMap;
    }

    public static int getMaxPartyLv() {
        return maxPartyLv;
    }

    public static int getMinPartyLv() {
        return minPartyLv;
    }

    public static int getMaxPartyRanks() {
        return maxPartyRanks;
    }

    public static StaticPartyLv getPartyLv(int partyLv) {
        return getPartyLvMap().get(partyLv);
    }

    /**
     * 获取本级军团升级需要的经验
     * 
     * @param partyLv
     * @return
     */
    public static int getPartyNeedExpByIv(int partyLv) {
        StaticPartyLv spl = getPartyLv(partyLv);
        if (null == spl) {
            return 0;
        }
        return spl.getNeedExp();
    }

    public static StaticPartyRanks getPartyRanks(int ranks) {
        return getRanksMap().get(ranks);
    }

    public static StaticPartyBuild getPartyBuildConfig(int partyLv, int build) {
        Map<Integer, StaticPartyBuild> map = buildMap.get(partyLv);
        if (null == map) {
            return null;
        }

        return map.get(build);
    }

    /**
     * 返回职务是否有指定特权
     * 
     * @param job
     * @param privilege
     * @return
     */
    public static boolean jobHavePrivilege(int job, int privilege) {
        StaticPartyJob spj = jobMap.get(job);
        if (null == spj) {
//            LogUtil.error("军团职务为配置, job:", job);
            return false;
        }

        return spj.getPrivilege().contains(privilege);
    }

    public static List<Integer> getJobPrivilegeVal(int job, int privilege) {
        StaticPartyJob spj = jobMap.get(job);
        if (null != spj && spj.getPrivilege().contains(privilege) && spj.getVal() != null) {
            for (List<Integer> v : spj.getVal()) {
                if (v.get(0) == privilege) {
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * 获取军团荣誉礼包配置信息
     *
     * @param honorIndex id
     * @return
     */
    public static StaticPartyHonorGift getHonorGift(int honorIndex,int campLv) {
        return honorGiftMap.values().stream().filter(o -> o.getHonorIndex()==honorIndex&&o.getPartyLv()==campLv).findFirst().orElse(null);
    }

    /**
     * 根据荣誉排行榜类型和名次，返回配置信息，如果未找到对应名次的配置信息，返回默认最低名次配置的信息
     * 
     * @param rankType 军团荣誉排行榜类型
     * @param rank 名次
     * @return
     */
    public static StaticPartyHonorRank getHonorRank(int rankType, int rank) {
        Map<Integer, StaticPartyHonorRank> map = honorRankMap.get(rankType);
        if (null == map) {
            return null;
        }

        if (map.containsKey(rank)) {
            return map.get(rank);
        }

        return map.get(PartyConstant.HONOR_RANK_OTHER);
    }

    /**
     * 未上榜名次的排行的票数
     * 
     * @param rankType
     * @return
     */
    public static int getHonorRankOtherCnt(int rankType) {
        return honorRankMap.get(rankType).get(PartyConstant.HONOR_RANK_OTHER).getReward();
    }

    public static int getAllHonorRankOtherCnt() {
        return getHonorRankOtherCnt(PartyConstant.RANK_TYPE_CITY) + getHonorRankOtherCnt(PartyConstant.RANK_TYPE_CAMP)
                + getHonorRankOtherCnt(PartyConstant.RANK_TYPE_BUILD);
    }

}
