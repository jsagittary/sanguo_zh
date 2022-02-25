package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.structs.Priority;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticIniDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xwind
 * @date 2021/7/1
 */
public abstract class AbsStaticIniService implements StaticIniService {
    @Autowired
    protected StaticDataDao staticDataDao;
    @Autowired
    protected StaticIniDao staticIniDao;

    @Override
    public Priority priority() {
        return Priority.EARTH;
    }
}
