package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName SavePartyThread.java
 * @Description 军团数据保存线程
 * @author TanDonghai
 * @date 创建时间：2017年4月26日 下午11:31:21
 *
 */
public class SavePartyThread extends SaveThread {
    private CampDataManager campDataManager;
    private static int MAX_SIZE = 10000;
    private LinkedBlockingQueue<DbParty> party_queue = new LinkedBlockingQueue<DbParty>();

    public SavePartyThread(String threadName) {
        super(threadName);
        this.campDataManager = DataResource.ac.getBean(CampDataManager.class);
    }

    public void run() {
        stop = false;
        done = false;
        while (!stop || party_queue.size() > 0) {
            DbParty dbParty = null;
            synchronized (this) {
                Object o = party_queue.poll();
                if (o != null) {
                    dbParty = (DbParty) o;
                }
            }
            if (dbParty == null) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    LogUtil.error(threadName + " Wait Exception:" + e.getMessage(), e);
                }
            } else {
                if (party_queue.size() > MAX_SIZE) {
                    party_queue.clear();
                }

                try {
                    campDataManager.updateParty(dbParty);
                    if (logFlag) {
                        saveCount++;
                        LogUtil.common("停服保存阵营数据成功camp=" + dbParty.getCamp());
                    }
                } catch (Exception e) {
                    LogUtil.error("Role Exception:" + dbParty, e);
                    LogUtil.warn("Role save Exception:" + dbParty);
                    LogUtil.common("停服保存阵营数据失败camp=" + dbParty.getCamp());
                    this.add(dbParty);
                }
            }
        }

        done = true;
    }

    @Override
    public void add(Object object) {
        try {
            DbParty dbParty = (DbParty) object;
            synchronized (this) {
                this.party_queue.add(dbParty);
                LogUtil.save("保存DbParty事件插入， camp:" + dbParty.getCamp());
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

}
