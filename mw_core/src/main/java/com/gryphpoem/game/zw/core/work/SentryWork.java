package com.gryphpoem.game.zw.core.work;

import com.hundredcent.game.ai.util.CheckNull;

import io.sentry.Sentry;

/**
 * @Description 用于向Sentry服务器发送消息
 * @author TanDonghai
 * @date 创建时间：2017年11月9日 下午3:07:05
 *
 */
public class SentryWork implements Runnable {

    private Throwable throwable;

    private String message;

    public SentryWork(Throwable throwable) {
        this.throwable = throwable;
    }

    public SentryWork(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        if (!CheckNull.isNullTrim(message)) {
            Sentry.capture(message);
        }

        if (null != throwable) {
            Sentry.capture(throwable);
        }
    }

}
