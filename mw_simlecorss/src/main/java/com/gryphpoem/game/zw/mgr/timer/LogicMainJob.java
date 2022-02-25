package com.gryphpoem.game.zw.mgr.timer;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.executor.ExcutorQueueType;
import com.gryphpoem.game.zw.executor.ExcutorType;
import com.gryphpoem.game.zw.mgr.ExecutorPoolMgr;
import com.gryphpoem.game.zw.quartz.jobs.AbsGameJob;
import com.gryphpoem.game.zw.task.QuartzTimerTask;

/**
 * @ClassName LogicMainJob.java
 * @Description 主线程运行的定时器
 * @author QiuKun
 * @date 2019年5月14日
 */
public abstract class LogicMainJob extends AbsGameJob {

  
    @Override
    protected void process() {
        // 放到主线程运行
        ExecutorPoolMgr.getIns().addTask(ExcutorType.LOGIC, ExcutorQueueType.LOGIC_MAIN,
                QuartzTimerTask.newInstance(this::executeInMain, context));
    }

    protected abstract void executeInMain(JobExecutionContext context);
}
