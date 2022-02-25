package com.gryphpoem.game.zw.core.eventbus;


/**
 * 异步线程
 */
public class AsyncPoster implements Runnable {
    private final PendingPostQueue queue;

    AsyncPoster() {
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        queue.enqueue(pendingPost);
        InvokeHelper.getDefault().getExecutorService().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if (pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        InvokeHelper.getDefault().invokeSubscriber(pendingPost);
    }
}
