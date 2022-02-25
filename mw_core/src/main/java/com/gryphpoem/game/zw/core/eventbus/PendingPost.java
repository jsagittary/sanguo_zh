package com.gryphpoem.game.zw.core.eventbus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I on 2017/8/9.
 */
public class PendingPost {
    private final static List<PendingPost> pendingPostPool = new ArrayList<>();

    Object event;// 事件类型
    Subscription subscription; // 订阅者
    PendingPost next;// 队列下一个待发送对象

    private PendingPost(Object event, Subscription subscription) {
        this.event = event;
        this.subscription = subscription;
    }

    /**
     * 首先检查复用池中是否有可用,如果有则返回复用,否则返回一个新的
     * 
     * @param subscription 订阅者
     * @param event 订阅事件
     * @return 待发送对象
     */
    static PendingPost obtainPendingPost(Subscription subscription, Object event) {
        synchronized (pendingPostPool) {
            int size = pendingPostPool.size();
            if (size > 0) {
                PendingPost pendingPost = pendingPostPool.remove(size - 1);
                pendingPost.event = event;
                pendingPost.subscription = subscription;
                pendingPost.next = null;
                return pendingPost;
            }
        }
        return new PendingPost(event, subscription);
    }

    /**
     * 回收一个待发送对象,并加入复用池
     * 
     * @param pendingPost 待回收的待发送对象
     */
    static void releasePendingPost(PendingPost pendingPost) {
        pendingPost.event = null;
        pendingPost.subscription = null;
        pendingPost.next = null;
        synchronized (pendingPostPool) {
            // Don't let the pool grow indefinitely
            if (pendingPostPool.size() < 100000) {
                pendingPostPool.add(pendingPost);
            }
        }
    }
}
