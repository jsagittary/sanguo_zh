package com.gryphpoem.game.zw.mgr.timer;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.QuartzHelper;

/**
 * @ClassName TimerMgr.java
 * @Description 定时器管理
 * @author QiuKun
 * @date 2019年5月8日
 */
@Component
public class TimerMgr {

    @Autowired
    @Qualifier("schedulerFactoryBean")
    private Scheduler scheduler;

    public void init() {
        QuartzHelper.addJobForCirc(scheduler, "hotfix", "hotfix", HotFixJob.class, 5);// 热更文件扫描
        QuartzHelper.addJobForCirc(scheduler, "runSec", "runSec", CrontabJob.RunSecJob.class, 1);// 跑秒定时器
        QuartzHelper.addJob(scheduler, "AcrossDayJob", "AcrossDayJob", CrontabJob.AcrossDayJob.class, "1 0 0 * * ?");// 转点定时器
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void stop() {
        QuartzHelper.shutdownJobs(scheduler);
    }
}
