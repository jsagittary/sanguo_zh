package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuffCross;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-22 16:07
 */
@Component
public class StaticCrossWarFireDataMgr extends AbsStaticIniService {

    //KEY0:type, KEY1:lv, VALUE: BUFF
    private static Map<Integer, TreeMap<Integer, StaticWarFireBuffCross>> crossBuffMap = new HashMap<>();

    @Override
    public void load() {
        reloadCrossWarFireBuffConfig();
    }

    private void reloadCrossWarFireBuffConfig() {
        List<StaticWarFireBuffCross> sBuffList = staticIniDao.selectStaticWarFireBuffCross();
        if (CheckNull.nonEmpty(sBuffList)) {
            Function<StaticWarFireBuffCross, Integer> key1Func = StaticWarFireBuffCross::getType;
            Function<StaticWarFireBuffCross, Integer> key2Func = StaticWarFireBuffCross::getLv;
            crossBuffMap = sBuffList.stream().collect(Java8Utils.groupByMapTreeMap(key1Func, key2Func));
        } else {
            LogUtil.error("读取 StaticWarFireBuffCross 配置失败!!!");
        }
    }


    public static StaticWarFireBuffCross getWarFireBuffByTypeLv(int type, int lv) {
        TreeMap<Integer, StaticWarFireBuffCross> lvMap = crossBuffMap.get(type);
        return CheckNull.nonEmpty(lvMap) ? lvMap.get(lv) : null;
    }

    public static TreeMap<Integer, StaticWarFireBuffCross> getWarFireBuffs(int type){
        return crossBuffMap.get(type);
    }

    @Override
    public void check() {

    }
}
