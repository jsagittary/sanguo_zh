package com.gryphpoem.game.zw.task;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.util.QuartzHelper.QuartzJobRun;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;

/**
 * @ClassName SecTimerTask.java
 * @Description
 * @author QiuKun
 * @date 2019年5月14日
 */
public class QuartzTimerTask extends PoolAbstractTask {

    private static final Recycler<QuartzTimerTask> RECYCLER = new Recycler<QuartzTimerTask>() {
        @Override
        protected QuartzTimerTask newObject(Handle handle) {
            return new QuartzTimerTask(handle);
        }
    };

    private QuartzJobRun timerTaskRun;
    private JobExecutionContext context;

    public static QuartzTimerTask newInstance(QuartzJobRun run, JobExecutionContext context) {
        QuartzTimerTask task = RECYCLER.get();
        task.reuse(run, context);
        return task;
    }

    /**
     * 重置对象
     */
    public void reuse(QuartzJobRun timerTaskRun, JobExecutionContext context) {
        reuse();
        this.timerTaskRun = timerTaskRun;
        this.context = context;
    }

    public QuartzTimerTask(Handle recyclerHandle) {
        super(recyclerHandle);
    }

    @Override
    protected Recycler<?> recycler() {
        return RECYCLER;
    }

    @Override
    public void work() {
        if (timerTaskRun != null && context != null) {
            timerTaskRun.run(context);
        }
    }

}
