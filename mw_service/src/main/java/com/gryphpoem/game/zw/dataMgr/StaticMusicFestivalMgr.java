package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.resource.domain.s.StaticCreativeOffice;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreativeOfficeAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticMusicFestivalBoxOffice;
import com.gryphpoem.game.zw.resource.domain.s.StaticMusicFestivalBoxOfficeParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/10/27 14:35
 */
@Component
public class StaticMusicFestivalMgr extends AbsStaticIniService {

    private static Map<Integer, StaticMusicFestivalBoxOffice> boxOfficeIdMap;
    private static Map<Integer, List<StaticMusicFestivalBoxOffice>> boxOfficeGroupMap;
    private static Map<Integer, StaticMusicFestivalBoxOfficeParam> boxOfficeParamMap;

    private static Map<Integer, StaticCreativeOffice> creativeOfficeMap;
    //KEY0:activityId, KEY1:type, List:任务列表
    private static Map<Integer, Map<Integer, List<StaticCreativeOffice>>> creativeOfficeListMap = new HashMap<>();
    //KEY0:activityId, KEY1:type, VALUE:类型
    private static Map<Integer, StaticCreativeOfficeAward> creativeOfficeAwardMap;

    @Override
    public void load() {
        List<StaticMusicFestivalBoxOffice> boxOffices = staticIniDao.selectStaticMusicFestivalBoxOfficeList();
        boxOfficeIdMap = boxOffices.stream().collect(Collectors.toMap(StaticMusicFestivalBoxOffice::getId, Function.identity()));
        boxOfficeGroupMap = boxOffices.stream().collect(Collectors.groupingBy(StaticMusicFestivalBoxOffice::getActivityId));
        boxOfficeParamMap = staticIniDao.selectStaticMusicFestivalBoxOfficeParamMap();

        //加载音乐创作任务表
        reloadCreativeOffice();
        //加载音乐创作奖励表
        reloadCreativeOfficeAwards();
    }

    @Override
    public void check() {

    }

    public static StaticMusicFestivalBoxOffice findBoxOfficeById(int id) {
        return boxOfficeIdMap.get(id);
    }

    public static List<StaticMusicFestivalBoxOffice> findBoxOfficesByActId(int actId) {
        return boxOfficeGroupMap.get(actId);
    }

    public static StaticMusicFestivalBoxOfficeParam findBoxOfficeParamByActId(int actId) {
        return boxOfficeParamMap.get(actId);
    }

    private void reloadCreativeOffice() {
        creativeOfficeMap = staticIniDao.selectStaticCreativeOffice();
        Map<Integer, Map<Integer, List<StaticCreativeOffice>>> creativeOfficeListMap0 = new HashMap<>();
        for (Map.Entry<Integer, StaticCreativeOffice> entry : creativeOfficeMap.entrySet()) {
            StaticCreativeOffice data = entry.getValue();
            Map<Integer, List<StaticCreativeOffice>> actMap = creativeOfficeListMap0.computeIfAbsent(data.getActivityId(), map -> new HashMap<>());
            List<StaticCreativeOffice> creativeList = actMap.computeIfAbsent(data.getType(), lst -> new ArrayList<>());
            creativeList.add(data);
        }
        creativeOfficeListMap = creativeOfficeListMap0;
    }

    private void reloadCreativeOfficeAwards(){
        creativeOfficeAwardMap = staticIniDao.selectStaticCreativeOfficeAward();
    }


    public static StaticCreativeOffice getStaticCreativeOfficeById(int uid){
        return creativeOfficeMap.get(uid);
    }

    public static List<StaticCreativeOffice> getCreativeOfficeTaskListByType(int actId, int type) {
        Map<Integer, List<StaticCreativeOffice>> map = creativeOfficeListMap.get(actId);
        return Objects.nonNull(map) ? map.get(type) : null;
    }

    public static StaticCreativeOfficeAward getCreativeOfficeAward(int uid){
        return creativeOfficeAwardMap.get(uid);
    }

    public static Map<Integer, StaticCreativeOfficeAward> getAllCreativeOfficeAwards(){
        return creativeOfficeAwardMap;
    }
}
