package com.gryphpoem.game.zw.server;

import java.util.concurrent.Callable;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @ClassName BaseMergeWork.java
 * @Description 合服处理的记录
 * @author QiuKun
 * @date 2018年9月10日
 */
public abstract class BaseMergeWork implements Callable<WorkResult> {

    protected abstract void work() throws Exception;

    protected void workBefore() {

    }

    protected void workAfter() {

    }

    abstract int serverId();

    /**
     * 设置线程名称
     */
    protected String threadName() {
        return null;
    }

    @Override
    public WorkResult call() {
        String threadName = threadName();
        if (!CheckNull.isNullTrim(threadName)) {
            Thread.currentThread().setName(threadName);
        }
        WorkResult res = new WorkResult();
        res.setServerId(serverId());
        long startT = System.currentTimeMillis();
        try {
            workBefore();
            work();
            res.setObject("succ");
        } catch (Exception e) {
            res.setThrowable(e);
            LogUtil.error("主服:" + serverId(), e);
        } finally {
            LogUtil.debug("--------------" + Thread.currentThread().getName() + "执行耗时 ："
                    + (System.currentTimeMillis() - startT) + " 毫秒");
            workAfter();
        }
        return res;
    }

}
