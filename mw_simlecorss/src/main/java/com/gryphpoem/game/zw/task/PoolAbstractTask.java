package com.gryphpoem.game.zw.task;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;

/**
 * @ClassName PoolAbstractTask.java
 * @Description 池化的任务
 * @author QiuKun
 * @date 2019年5月14日
 */
public abstract class PoolAbstractTask extends AbstractTask {

    private final Recycler.Handle recyclerHandle;

    public PoolAbstractTask(Handle recyclerHandle) {
        super();
        this.recyclerHandle = recyclerHandle;
    }

    @Override
    public void run() {
        super.run();
        recycle();
    }

    protected final void reuse() {
        createTime = System.currentTimeMillis();
        setTasksQueue(null);
    }

    @SuppressWarnings("unchecked")
    private void recycle() {
        Recycler.Handle recyclerHandle = this.recyclerHandle;
        if (recyclerHandle != null) {
            ((Recycler<Object>) recycler()).recycle(this, recyclerHandle);
        }
    }

    protected abstract Recycler<?> recycler();
}
