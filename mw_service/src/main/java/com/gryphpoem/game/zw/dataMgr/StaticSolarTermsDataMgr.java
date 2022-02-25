package com.gryphpoem.game.zw.dataMgr;

import java.util.List;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticSolarTerms;

/**
 * @ClassName StaticSolarTermsDataMgr.java
 * @Description 节气相关
 * @author QiuKun
 * @date 2017年11月21日
 */
public class StaticSolarTermsDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static List<StaticSolarTerms> solarTerms;

    public static void init() {
        solarTerms = staticDataDao.selectSolarTerms();
    }

    public static List<StaticSolarTerms> getSolarTerms() {
        return solarTerms;
    }

    public static StaticSolarTerms getSolarTermsById(int id) {
        return getSolarTerms().stream().filter(sst -> sst.getId() == id).findFirst().orElse(null);
    }

    /**
     * 获取节气开始的配置
     * 
     * @param week
     * @return
     */
    public static StaticSolarTerms getStartSolarTermsByWeek(int week) {
        return getSolarTerms().stream().filter(sst -> sst.getWeek() == week).findFirst().orElse(null);
    }

}
