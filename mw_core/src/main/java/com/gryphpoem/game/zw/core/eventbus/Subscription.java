package com.gryphpoem.game.zw.core.eventbus;

/**
 * 订阅者对应的注解方法
 *
 */
final class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;

    volatile boolean active;// 为了防止注销后,但事件还在队列排队中,等轮到自己执行时,还可以触发

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
        active = true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Subscription) {
            Subscription otherSubscription = (Subscription) other;
            return subscriber == otherSubscription.subscriber
                    && subscriberMethod.equals(otherSubscription.subscriberMethod);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return subscriber.hashCode() + subscriberMethod.methodString.hashCode();
    }
}