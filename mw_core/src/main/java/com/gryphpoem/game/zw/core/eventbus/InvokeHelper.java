package com.gryphpoem.game.zw.core.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 方法执行帮助类
 */
public final class InvokeHelper {
    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static InvokeHelper ourInstance;
    private AsyncPoster asyncPoster;
    private BackgroundPoster backgroundPoster;

    private InvokeHelper() {
        asyncPoster = new AsyncPoster();
        backgroundPoster = new BackgroundPoster();
    }

    public static InvokeHelper getDefault() {
        if (ourInstance == null) {
            synchronized (InvokeHelper.class) {
                if (ourInstance == null) {
                    ourInstance = new InvokeHelper();
                }
            }
        }
        return ourInstance;
    }

    public void post(final Subscription subscription, final Object event) {
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                // 直接执行
                invokeSubscriber(subscription, event);
                break;
            case BACKGROUND:
                backgroundPoster.enqueue(subscription, event);
                break;
            case ASYNC:
                // 放在异步线程内执行
                asyncPoster.enqueue(subscription, event);
                break;
            default:
                // 抛异常
                throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
        }
    }

    public void invokeSubscriber(PendingPost pendingPost) {
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        if (subscription.active) {
            invokeSubscriber(subscription, event);
        }
    }

    private void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            // throw new InvocationTargetException(subscriberMethod.subscriber,e.getCause(),event);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    ExecutorService getExecutorService() {
        return DEFAULT_EXECUTOR_SERVICE;
    }
}
