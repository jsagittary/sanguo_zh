package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.resource.domain.s.StaticAnniversaryEgg;
import com.gryphpoem.game.zw.resource.domain.s.StaticAnniversaryTurntable;
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomLibrary;
import com.gryphpoem.game.zw.resource.domain.s.StaticActSkinEncore;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 周年庆活动
 * @author xwind
 * @date 2021/7/22
 */
@Service
public class StaticAnniversaryMgr extends AbsStaticIniService {

    private static int playTurntablePrice;//转盘消耗钻石
    private static List<Integer> playTurntableProp;//转盘消耗道具
    private static Map<Integer, Map<Integer,List<StaticAnniversaryTurntable>>> turntableGroupMap;//转盘<activityId,<round,List>>
    private static Map<Integer,StaticAnniversaryTurntable> turntableMap;//转盘<id,Object>
    private static Map<Integer,Integer> turntableMaxRoundMap;//<activityId,Max>

    private static List<StaticAnniversaryEgg> eggList;//彩蛋配置
    private static Map<Integer,StaticAnniversaryEgg> eggMap;//彩蛋配置<id,Object>
    private static int eggRefreshInterval;//彩蛋刷新间隔
    private static int eggRefreshCount;//彩蛋刷新数量
    private static Map<Integer, StaticActSkinEncore> actSkinEncoreMap;

    private static Map<Integer,List<StaticRandomLibrary>> randomLibraryGroupMap;//随机奖励库<randomId,List>

    @Override
    public void load() {
        playTurntablePrice = ActParamTabLoader.getIntegerSystemValue(338,200);
        playTurntableProp = ActParamTabLoader.getListIntSystemValue(339,"[]");

        List<StaticAnniversaryTurntable> tmps1 = staticIniDao.selectStaticAnniversaryTurntableList();
        turntableMap = tmps1.stream().collect(Collectors.toMap(StaticAnniversaryTurntable::getId,v->v));
        turntableGroupMap = tmps1.stream().collect(Collectors.groupingBy(StaticAnniversaryTurntable::getActivityId,Collectors.groupingBy(StaticAnniversaryTurntable::getRound)));
        turntableMaxRoundMap = new HashMap<>();
        turntableGroupMap.entrySet().forEach(tmp -> {
            int actId = tmp.getKey();
            int max = tmp.getValue().keySet().stream().max(Comparator.comparingInt(Integer::intValue)).get();
            turntableMaxRoundMap.put(actId,max);
        });

        eggList = staticIniDao.selectStaticAnniversaryEggList();
        eggMap = eggList.stream().collect(Collectors.toMap(StaticAnniversaryEgg::getId,v->v));
        List<Integer> list350 = ActParamTabLoader.getListIntSystemValue(350,"[3600,3]");
        eggRefreshInterval = list350.get(0);
        eggRefreshCount = list350.get(1);

        List<StaticRandomLibrary> randomLibraryList = staticIniDao.selectStaticRandomLibraryList();
        randomLibraryGroupMap = randomLibraryList.stream().collect(Collectors.groupingBy(StaticRandomLibrary::getRandomId));

        actSkinEncoreMap = staticIniDao.selectStaticActSkinEncore();
    }

    @Override
    public void check() {

    }

    public static Map<Integer, Integer> getTurntableMaxRoundMap() {
        return turntableMaxRoundMap;
    }

    public static StaticRandomLibrary getRandomLibrary(int randomId, int lv) {
        List<StaticRandomLibrary> tmps = randomLibraryGroupMap.get(randomId);
        if (ListUtils.isBlank(tmps)) {
            return null;
        }
        return tmps.stream().filter(tmp -> tmp.getLv().get(0) <= lv && tmp.getLv().get(1) >= lv).findFirst().orElse(null);
    }

    public static int getEggRefreshInterval() {
        return eggRefreshInterval;
    }

    public static int getEggRefreshCount() {
        return eggRefreshCount;
    }

    public static List<StaticAnniversaryEgg> getEggList() {
        return eggList;
    }

    public static StaticAnniversaryEgg getEggById(int id){
        return eggMap.get(id);
    }

    public static List<StaticAnniversaryTurntable> getStaticAnniversaryTurntableList(int activityId, int round){
        Map<Integer,List<StaticAnniversaryTurntable>> map = turntableGroupMap.get(activityId);
        if(!CheckNull.isEmpty(map)){
            return map.get(round);
        }
        return Collections.EMPTY_LIST;
    }

    public static StaticAnniversaryTurntable getStaticAnniversaryTurntable(int id){
        return turntableMap.get(id);
    }

    public static int getPlayTurntablePrice() {
        return playTurntablePrice;
    }

    public static List<Integer> getPlayTurntableProp() {
        return playTurntableProp;
    }

    public static StaticActSkinEncore getStaticActSkinEncore(int uid){
        return actSkinEncoreMap.get(uid);
    }

    public static Map<Integer, StaticActSkinEncore> getActSkinEncoreMap() {
        return actSkinEncoreMap;
    }
}
