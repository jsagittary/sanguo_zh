package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticLightningWar;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-15 14:53
 * @description: 闪电战活动
 * @modified By:
 */
public class StaticLightningWarDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static StaticLightningWar lightningWar;

    public static StaticLightningWar getLightningWar() {
        return lightningWar;
    }

    public static void init() {
        StaticLightningWarDataMgr.lightningWar = staticDataDao.selectLightningWar().get(0);
    }
}
