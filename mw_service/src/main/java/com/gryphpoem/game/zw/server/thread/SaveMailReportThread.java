package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;

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

    }

    @Override
    public void add(Object object) {

    }

    @Override
    public void remove(Object obj) {
        super.remove(obj);
    }
}
