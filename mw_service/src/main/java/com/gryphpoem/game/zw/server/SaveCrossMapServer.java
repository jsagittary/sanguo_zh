package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.resource.domain.p.DbCrossMap;

/**
 * @ClassName SaveCrossMapServer.java
 * @Description 跨服地图的保存
 * @author QiuKun
 * @date 2019年4月2日
 */
public class SaveCrossMapServer extends SaveCommonServer<DbCrossMap> {
    private static SaveCrossMapServer ins = new SaveCrossMapServer();

    private CrossWorldMapDataManager crossWorldMapDataManager;

    public static SaveCrossMapServer getIns() {
        return ins;
    }

    private SaveCrossMapServer() {
        super("SAVE_CROSSMAP", 1);
        this.crossWorldMapDataManager = DataResource.ac.getBean(CrossWorldMapDataManager.class);
    }

    @Override
    protected void saveOne(DbCrossMap data) {
        crossWorldMapDataManager.updateDbCrossMap(data);
    }

    private int beforeSaveCount;

    @Override
    public void saveAll() {
        crossWorldMapDataManager.removeNewYorkWarBattle();
        int saveCount = crossWorldMapDataManager.saveCrossMap(false);
        beforeSaveCount = saveCount;
    }

    public void stopServer() {
        try {
            LogUtil.stop("开始保存跨服地图数据...");
            long startMillis = System.currentTimeMillis();
            setLogFlag();
            saveAll();
            stop();
            LogUtil.stop("预入库的跨服地图数据数量: " + beforeSaveCount);
            while (!saveDone()) {
                LogUtil.stop(String.format("已入库的跨服地图数量: %s, 等待3s, 已耗时=%s",allSaveCount(),System.currentTimeMillis()-startMillis));
                Thread.sleep(3000);
            }
            LogUtil.stop(String.format("保存跨服地图数据完成,共处理: %s, 共耗时=%s",allSaveCount(),System.currentTimeMillis()-startMillis));
        } catch (Exception e) {
            LogUtil.error("停服保存跨服地图数据发生错误",e);
        }
    }
}
