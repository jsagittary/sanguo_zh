package com.gryphpoem.game.zw.server.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName SaveCommonThread.java
 * @Description 存储的线程
 * @author QiuKun
 * @date 2019年4月2日
 */
public class SaveCommonThread<T> extends SaveThread {

    private static final int MAX_SIZE = 10000;
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>(); // 存储的队列
    private Consumer<T> consumer; // 存储的消费者


    public SaveCommonThread(String threadName, Consumer<T> consumer) {
        super(threadName);
        this.consumer = consumer;
    }

    @Override
    public void run() {
        stop = false;
        done = false;
        while (!stop || !queue.isEmpty()) {
            T data = null;
            synchronized (this) {
                T o = queue.poll();
                if (o != null) {
                    data = o;
                }
            }
            if (data == null) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    LogUtil.error(threadName + " Wait Exception:" + e.getMessage(), e);
                }
            } else {
                if (queue.size() > MAX_SIZE) {
                    queue.clear();
                }
                try {
                    // do save
                    consumer.accept(data);
                    if (logFlag) {
                        saveCount++;
                        LogUtil.common("停服保存Global或Cross成功");
                    }
                } catch (Exception e) {
                    LogUtil.error("Save exception:" + data, e);
                    LogUtil.common("停服保存Global或Cross失败");
                    this.add(data);
                }
            }
        }
        done = true;
    }

    @Override
    public void add(Object object) {
        try {
            @SuppressWarnings("unchecked")
            T data = (T) object;
            synchronized (this) {
                this.queue.add(data);
                LogUtil.save("保存事件插入 threadName:" + threadName);
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

}
