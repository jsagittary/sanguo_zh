package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerCastle;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerCharge;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerTurnplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xwind
 * @date 2021/7/7
 */
@Service
public class StaticSummerMgr extends AbsStaticIniService {

    //key:活动id value:配置列表
    private static Map<Integer, List<StaticSummerCharge>> staticSummerChargeGroupMap;
    //key:配表id value:配置
    private static Map<Integer,StaticSummerCharge> staticSummerChargeMap;
    //<活动id,<轮数,配置>>
    private static Map<Integer,Map<Integer,List<StaticSummerTurnplate>>> staticSummerTurnplateGroupMap;
    //<配表id,配置>
    private static Map<Integer, StaticSummerCastle> staticSummerCastleMap;
    //<互动id,配置列表>
    private static Map<Integer,List<StaticSummerCastle>> staticSummerCastleGroupMap;

    @Override
    public void load() {
        List<StaticSummerCharge> list =  staticIniDao.selectStaticSummerChargeList();
        staticSummerChargeGroupMap = list.stream().collect(Collectors.groupingBy(StaticSummerCharge::getActivityId));
        staticSummerChargeMap = list.stream().collect(Collectors.toMap(StaticSummerCharge::getId,v->v));

        List<StaticSummerTurnplate> list1 = staticIniDao.selectStaticSummerTurnplateList();
        staticSummerTurnplateGroupMap = list1.stream().collect(Collectors.groupingBy(StaticSummerTurnplate::getActivityId, Collectors.groupingBy(StaticSummerTurnplate::getRound)));

        staticSummerCastleMap = staticIniDao.selectStaticSummerCastleMap();
        staticSummerCastleGroupMap = staticSummerCastleMap.values().stream().collect(Collectors.groupingBy(StaticSummerCastle::getActivityId));

        LogUtil.common("------------------加载数据：夏日活动-----------------");
    }

    @Override
    public void check() {

    }

    public static Map<Integer, List<StaticSummerCharge>> getStaticSummerChargeGroupMap() {
        return staticSummerChargeGroupMap;
    }

    public static Map<Integer, StaticSummerCharge> getStaticSummerChargeMap() {
        return staticSummerChargeMap;
    }

    public static Map<Integer, Map<Integer, List<StaticSummerTurnplate>>> getStaticSummerTurnplateGroupMap() {
        return staticSummerTurnplateGroupMap;
    }

    public static List<StaticSummerTurnplate> getStaticSummerTurnplateList(int actId,int round){
        Map<Integer, List<StaticSummerTurnplate>> map = staticSummerTurnplateGroupMap.get(actId);
        if(map != null){
            return map.get(round);
        }
        return null;
    }

    public static Map<Integer, StaticSummerCastle> getStaticSummerCastleMap() {
        return staticSummerCastleMap;
    }

    public static List<StaticSummerCastle> getStaticSummerCastleListByActivityId(int activityId) {
        return staticSummerCastleGroupMap.get(activityId);
    }
}
