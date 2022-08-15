package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyAttr;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyShow;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 16:43
 */
@Component
public class StaticHeroBiographyDataMgr extends AbsStaticIniService {

    private Map<Integer, TreeMap<Integer, StaticHeroBiographyAttr>> attrMap;
    private Map<Integer, StaticHeroBiographyShow> showMap;

    @Override
    public void load() {
        List<StaticHeroBiographyAttr> attrList = staticIniDao.selectStaticHeroBiographyAttrList();
        if (CheckNull.nonEmpty(attrList)) {
            Map<Integer, TreeMap<Integer, StaticHeroBiographyAttr>> attrMap_ = new HashMap<>();
            attrList.forEach(attrInit -> {
                if (Objects.isNull(attrInit))
                    return;
                attrMap_.computeIfAbsent(attrInit.getType(), map -> new TreeMap<>(Integer::compareTo)).put(attrInit.getLevel(), attrInit);
            });
            attrMap = attrMap_;
        }

        showMap = staticIniDao.selectStaticHeroBiographyShowList();
    }

    public StaticHeroBiographyAttr initBiographyAttr(int type) {
        if (CheckNull.isEmpty(attrMap))
            return null;
        TreeMap<Integer, StaticHeroBiographyAttr> attrTreeMap = attrMap.get(type);
        if (CheckNull.isEmpty(attrTreeMap))
            return null;
        return attrTreeMap.firstEntry().getValue();
    }

    public StaticHeroBiographyAttr nextBiographyAttr(int type, int level) {
        if (CheckNull.isEmpty(attrMap))
            return null;
        TreeMap<Integer, StaticHeroBiographyAttr> attrTreeMap = attrMap.get(type);
        if (CheckNull.isEmpty(attrTreeMap))
            return null;
        Map.Entry<Integer, StaticHeroBiographyAttr> attrEntry = attrTreeMap.higherEntry(level);
        if (CheckNull.isNull(attrEntry))
            return null;
        return attrEntry.getValue();
    }

    public StaticHeroBiographyShow getBiographyShow(int type) {
        if (CheckNull.isNull(showMap))
            return null;
        return showMap.get(type);
    }

    @Override
    public void check() {
        if (CheckNull.isEmpty(attrMap) || CheckNull.isEmpty(showMap)) {
            LogUtil.error("hero biography static data is empty! check");
        }
    }
}
