package com.gryphpoem.game.zw.core;

import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;

public abstract class SaveServer implements Runnable {
    private long createTime;

    protected HashMap<Integer, SaveThread> threadPool = new HashMap<Integer, SaveThread>();

    protected int threadNum;

    protected String name;

    public SaveServer(String name, int threadNum) {
        this.createTime = System.currentTimeMillis();
        this.name = name;
        this.threadNum = threadNum;

        createThreads();
        init();

        LogUtil.start(String.format("创建线程[%s]数量[%s]",name,threadNum));
    }

    public String serverName() {
        return name;
    }

    public void createThreads() {
        for (int i = 0; i < threadNum; i++) {
            threadPool.put(i, createThread(name + " thread " + i));
        }
    }

    public void init() {

    }

    abstract public void saveData(Object object);

    public void removeData(Object obj) {}

    public boolean saveDone() {
        Iterator<SaveThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            if (!it.next().workDone()) {
                return false;
            }
        }

        return true;
    }

    public int allSaveCount() {
        int saveCount = 0;
        Iterator<SaveThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            saveCount += it.next().getSaveCount();
        }
        return saveCount;
    }

    public void stop() {
        Iterator<SaveThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().stop(true);
        }
    }

    public void setLogFlag() {
        Iterator<SaveThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().setLogFlag();
        }
    }

    public void run() {
        Iterator<SaveThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().start();
        }
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    abstract public SaveThread createThread(String name);
}
