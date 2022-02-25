package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SaveServer;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.resource.domain.p.DbSerializeId;
import com.gryphpoem.game.zw.server.thread.SaveCommonThread;

/**
 * @ClassName SaveCommonServer.java
 * @Description 保存数据服务抽取
 * @author QiuKun
 * @date 2019年4月2日
 */
public abstract class SaveCommonServer<DbData extends DbSerializeId> extends SaveServer {

    public SaveCommonServer(String name, int threadNum) {
        super(name, threadNum);
    }

    @Override
    public void saveData(Object object) {
        @SuppressWarnings("unchecked")
        DbData data = (DbData) object;
        SaveThread thread = threadPool.get((data.getSerializeIdId() % threadNum));
        thread.add(object);
    }

    @Override
    public SaveThread createThread(String name) {
        return new SaveCommonThread<DbData>(name, data -> {
            saveOne(data);
        });
    }

    /**
     * 存储一个逻辑
     * 
     * @param data
     */
    protected abstract void saveOne(DbData data);

    /**
     * 全部保存
     */
    public abstract void saveAll();

}
