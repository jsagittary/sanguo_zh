package com.gryphpoem.game.zw.mgr.timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobExecutionContext;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper.QuartzJobRun;

/**
 * @ClassName CrontabJob.java
 * @Description
 * @author QiuKun
 * @date 2019年5月23日
 */
public class CrontabJob extends LogicMainJob {

    /** <class,<name,TimerTaskRun>> */
    static final Map<Class<? extends CrontabJob>, Map<String, QuartzJobRun>> timerTaskRunMap = new ConcurrentHashMap<>();

    public static void registQuartzJobRun(Class<? extends CrontabJob> clazz, String name, QuartzJobRun r) {
        Map<String, QuartzJobRun> nt = timerTaskRunMap.computeIfAbsent(clazz, (k) -> new ConcurrentHashMap<>());
        nt.put(name, r);
        LogUtil.debug("注册 name:"+name,", clazz:"+clazz.getName());
    }

    public static void unRegistQuartzJobRun(Class<? extends CrontabJob> clazz, String name) {
        Map<String, QuartzJobRun> nt = timerTaskRunMap.get(clazz);
        nt.remove(name);
    }

    @Override
    protected void executeInMain(JobExecutionContext context) {
        Map<String, QuartzJobRun> map = timerTaskRunMap.get(this.getClass());
        if (map != null) {
            map.forEach((name, timerTaskRun) -> {
                try {
                    timerTaskRun.run(context);
                } catch (Exception e) {
                    LogUtil.error(e, "定时器运行出错 name:", name);
                }
            });
        }
    }

    /**
     * 跨天处理
     */
    public static final class AcrossDayJob extends CrontabJob {
    }

    /**
     * 跑秒定时器
     *
     */
    public static final class RunSecJob extends CrontabJob {
    }
 


   
}
