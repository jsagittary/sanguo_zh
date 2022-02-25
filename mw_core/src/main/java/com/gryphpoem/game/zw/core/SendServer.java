package com.gryphpoem.game.zw.core;

import com.gryphpoem.game.zw.core.thread.SendThread;

import java.util.HashMap;
import java.util.Iterator;

public abstract class SendServer implements Runnable {
    private long createTime;

    protected HashMap<Integer, SendThread> threadPool = new HashMap<Integer, SendThread>();

    protected int threadNum;

    protected String name;

    public SendServer(String name, int threadNum) {
        this.createTime = System.currentTimeMillis();
        this.name = name;
        this.threadNum = threadNum;

        createThreads();
        init();
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

    abstract public void sendData(String body);

    public boolean sendDone() {
        Iterator<SendThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            if (!it.next().workDone()) {
                return false;
            }
        }

        return true;
    }


    public void stop() {
        Iterator<SendThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().stop(true);
        }
    }

    public void setLogFlag() {
        Iterator<SendThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().setLogFlag();
        }
    }

    public void run() {
        Iterator<SendThread> it = threadPool.values().iterator();
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

    abstract public SendThread createThread(String name);
}
