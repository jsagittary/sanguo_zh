package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotem;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemLink;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemDrop;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemUp;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author xwind
 * @date 2021/11/18
 */
@Service
public class StaticTotemDataMgr extends AbsStaticIniService {

    private static List<StaticTotem> staticTotemList;
    private static Map<Integer,StaticTotem> staticTotemMap;
    private static  List<StaticTotemLink> staticTotemLinkList;
    private static  List<StaticTotemDrop> staticTotemDropList;
    private static  List<StaticTotemUp> staticTotemUpList;
    private static Map<Integer,Map<Integer,List<StaticTotemUp>>> staticTotemUpGroupMap;

    public static List<StaticTotemLink> getStaticTotemLinkList() {
        return staticTotemLinkList;
    }

    public static StaticTotem getStaticTotem(int id){
        return staticTotemMap.get(id);
    }

    public static StaticTotemUp getStaticTotemUp(int upType,int quality,int lv){
        AtomicReference<StaticTotemUp> reference = new AtomicReference<>();
        Optional.ofNullable(staticTotemUpGroupMap.get(upType)).ifPresent(map -> Optional.ofNullable(map.get(quality)).ifPresent(tmps -> {
            reference.set(tmps.stream().filter(o -> o.getLv() == lv).findFirst().orElse(null));
        }));
        return reference.get();
    }

    public static List<StaticTotemUp> getStaticTotemUpList(int upType,int quality){
        return staticTotemUpGroupMap.get(upType).get(quality);
    }

    public static List<StaticTotemDrop> getStaticTotemDropList() {
        return staticTotemDropList;
    }

    @Override
    public void load() {
        staticTotemList = staticIniDao.selectStaticTotemList();
        staticTotemMap = staticTotemList.stream().collect(Collectors.toMap(StaticTotem::getId,v->v));
        staticTotemLinkList = staticIniDao.selectStaticTotemLinkList().stream().sorted(Comparator.comparing(StaticTotemLink::getQuality).thenComparing(StaticTotemLink::getLv).reversed()).collect(Collectors.toList());
        staticTotemDropList = staticIniDao.selectStaticTotemDropList();
        staticTotemUpList = staticIniDao.selectStaticTotemUpList();
        staticTotemUpGroupMap = staticTotemUpList.stream().collect(Collectors.groupingBy(StaticTotemUp::getUpType,Collectors.groupingBy(StaticTotemUp::getQuality)));
        LogUtil.common("------------------加载数据：阵法图腾-----------------");
    }

    @Override
    public void check() {

    }
}
