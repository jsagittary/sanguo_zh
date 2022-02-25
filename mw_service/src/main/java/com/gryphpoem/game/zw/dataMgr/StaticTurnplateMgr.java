package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticAutumnTurnplate;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xwind
 * @date 2021/9/14
 */
@Service
public class StaticTurnplateMgr extends AbsStaticIniService {

    private static List<StaticAutumnTurnplate> staticAutumnTurnplateList;
    private static Map<Integer,Map<Integer,List<StaticAutumnTurnplate>>> staticAutumnTurnplateGroupMap;
    private static Map<Integer,StaticAutumnTurnplate> staticAutumnTurnplateMap;

    private static List<List<Integer>> param丰收转盘抽奖价格;
    private static List<List<Integer>> param丰收转盘替代道具;

    @Override
    public void load() {
        staticAutumnTurnplateList = super.staticIniDao.selectStaticAutumnTurnplateList();
        staticAutumnTurnplateMap = staticAutumnTurnplateList.stream().collect(Collectors.toMap(StaticAutumnTurnplate::getId,v->v));
        staticAutumnTurnplateGroupMap = staticAutumnTurnplateList.stream().collect(Collectors.groupingBy(StaticAutumnTurnplate::getActivityId,Collectors.groupingBy(StaticAutumnTurnplate::getType)));
        param丰收转盘抽奖价格 = ActParamTabLoader.getListListIntSystemValue(360,"[[21301,228,5,10],[21302,268,5,10]]");
        param丰收转盘替代道具 = ActParamTabLoader.getListListIntSystemValue(365,"[[4,1945,1]]");
        LogUtil.common("------------------加载数据：丰收转盘-----------------");
    }

    @Override
    public void check() {

    }

    public static List<StaticAutumnTurnplate> getStaticAutumnTurnplateList(int activityId,int type){
        Map<Integer,List<StaticAutumnTurnplate>> map =  staticAutumnTurnplateGroupMap.get(activityId);
        if(Objects.nonNull(map)){
            return map.get(type);
        }
        return null;
    }

    public static StaticAutumnTurnplate getStaticAutumnTurnplate(int id){
        return staticAutumnTurnplateMap.get(id);
    }

    public static List<Integer> get抽奖价格(int activityId){
        List<Integer> list = new ArrayList<>();
        param丰收转盘抽奖价格.stream().filter(o -> o.get(0) == activityId).findFirst().ifPresent(tmps -> list.addAll(tmps.subList(1,tmps.size())));
        return list;
    }

    public static List<List<Integer>> get抽奖道具(){
        return param丰收转盘替代道具;
    }


}
