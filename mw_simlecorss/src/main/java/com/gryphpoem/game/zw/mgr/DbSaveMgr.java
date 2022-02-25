package com.gryphpoem.game.zw.mgr;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.executor.ExcutorQueueType;
import com.gryphpoem.game.zw.executor.ExcutorType;
import com.gryphpoem.game.zw.model.global.BaseGlobalSaveModel;
import com.gryphpoem.game.zw.model.global.GlobalModelType;
import com.gryphpoem.game.zw.resource.dao.impl.c.DbGlobalDataDao;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobalData;
import com.gryphpoem.game.zw.task.ParamTask;

/**
 * @ClassName DbSaveMgr.java
 * @Description 保存的逻辑
 * @author QiuKun
 * @date 2019年6月19日
 */
@Component
public class DbSaveMgr {

    @Autowired
    private DbGlobalDataDao globalDataDao;

    /**
     * 服务器停服时触发
     */
    public void onStop() {
        // 公共数据保存
        saveGloabal();
    }

    private void saveGloabal() {
        List<DbGlobalData> gloabalData = new ArrayList<>();
        // 公共数据保存
        for (GlobalModelType t : GlobalModelType.values()) {
            DbGlobalData data = new DbGlobalData();
            data.setType(t.getType());
            gloabalData.add(data);
        }
        gloabalData.forEach(dbDate -> {
            ExecutorPoolMgr.getIns().addTask(ExcutorType.SAVE, ExcutorQueueType.SAVE_1, new ParamTask(dbDate) {
                @Override
                public void work() {
                    DbGlobalData data = (DbGlobalData) param;
                    GlobalModelType type = GlobalModelType.getGlobalModelType(data.getType());
                    if (type != null) {
                        BaseGlobalSaveModel gsavemodel = type.getFunc().get();
                        if (gsavemodel != null) {
                            data.setData(gsavemodel.getData());
                        }
                    }
                    globalDataDao.replace(data);
                    LogUtil.start("---------保存 " + type.getDesc() + "成功 -------------");
                }
            });
        });
    }
}
