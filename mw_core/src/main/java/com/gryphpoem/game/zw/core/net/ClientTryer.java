package com.gryphpoem.game.zw.core.net;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * 重试与心跳检测
 */
public class ClientTryer {

    private ScheduledThreadPoolExecutor executor;

    public ClientTryer() {
        executor = new ScheduledThreadPoolExecutor(1);// 初始化1个线程
    }

    public void ctry(InnerServer client, int index, int period) {
        if (executor.isShutdown()) {
            return;
        }
        TryRunner tryRunner = new TryRunner(client, index);
        ScheduledFuture<?> future = executor.schedule(tryRunner, period, TimeUnit.SECONDS);
        tryRunner.setFuture(future);
    }

    public void startSendHeart(Runnable command, int period) {
        if (executor.isShutdown()) {
            return;
        }
        executor.scheduleWithFixedDelay(command, period, period, TimeUnit.SECONDS);
    }

    public void stop() {
        if (executor != null) {
            try {
                executor.shutdown();
            } catch (Exception e) {
                LogUtil.error("executor shutdown error", e);
            }
        }
    }

}

class TryRunner implements Runnable {

    private ScheduledFuture<?> future = null;
    private InnerServer client = null;
    private int index;
    private int count;

    public TryRunner(InnerServer client, int index) {
        this.client = client;
        this.index = index;
        this.count = 0;
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public void run() {
        try {
            boolean result = client.reConnect(index);
            if (result) {
                future.cancel(true);
                LogUtil.common("Reconnect to " + client.toString(index) + " Succeed! exit Reconnect.");
            } else {
                count++;
                LogUtil.common("Reconnect to " + client.toString(index) + " Failed! count :" + count);
            }
        } catch (Exception e) {
            LogUtil.error("Reconnect exception,index=" + index + ", client=" + client, e);
        }
    }

}
