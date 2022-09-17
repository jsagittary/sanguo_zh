package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 17:15
 */
public class SaveMailReportThread extends SaveThread {
    /** 保存queue*/
    private LinkedBlockingQueue<DbMailReport> saveQueue = new LinkedBlockingQueue<>();
    /** 删除queue*/
    private LinkedBlockingQueue<DbMailReport> removeQueue = new LinkedBlockingQueue<>();

    public SaveMailReportThread(String threadName) {
        super(threadName);
    }

    @Override
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

    }

    @Override
    public void remove(Object obj) {
        super.remove(obj);
    }
}
