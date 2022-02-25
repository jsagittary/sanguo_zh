package com.gryphpoem.game.zw.core.util;

import org.quartz.*;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

/**
 * @ClassName QuartzHelper.java
 * @Description quartz管理类型,动态添加定时任务
 * @author QiuKun
 * @date 2017年11月23日
 */
public abstract class QuartzHelper {

    /**
     * 只会执行一次
     *
     * @param sched
     * @param jobName
     * @param jobGroupName
     * @param jobClass
     * @param startAt 开始执行的时间
     */
    public static void addJob(Scheduler sched, String jobName, String jobGroupName, Class<? extends Job> jobClass,
            Date startAt, Date endAt, int intervalInMinutes) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
            SimpleTrigger st = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInMinutes).repeatForever())
                    .startAt(startAt).endAt(endAt).build();
            sched.scheduleJob(jobDetail, st);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 只会执行一次
     *
     * @param sched
     * @param jobName
     * @param jobGroupName
     * @param jobClass
     * @param startAt 开始执行的时间
     */
    public static void addJob(Scheduler sched, String jobName, String jobGroupName, Class<? extends Job> jobClass,
            Date startAt) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger == null) {
                JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
                SimpleTrigger st = (SimpleTrigger) TriggerBuilder.newTrigger().withIdentity(triggerKey).startAt(startAt)
                        .build();
                sched.scheduleJob(jobDetail, st);
            } else {
                Date startTime = trigger.getStartTime();
                if (startTime.getTime() != startAt.getTime()) {
                    SimpleTrigger st = (SimpleTrigger) TriggerBuilder.newTrigger().withIdentity(triggerKey)
                            .startAt(startAt).build();
                    sched.rescheduleJob(triggerKey, st);
                }
            }
            LogUtil.debug("----------添加or修改定时器成功 : Group=",jobGroupName,", name=", jobName, "---------------", startAt);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public static JobDetail getJobDetail(Scheduler sched,String group,String name) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name,group);
        return sched.getJobDetail(jobKey);
    }

    /**
     * 添加任务
     * 
     * @param sched
     * @param jobName
     * @param jobGroupName
     * @param jobClass
     * @param cron
     */
    public static void addJob(Scheduler sched, String jobName, String jobGroupName, Class<? extends Job> jobClass,
            String cron) {

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        // 获取trigger
        CronTrigger trigger;
        try {
            trigger = (CronTrigger) sched.getTrigger(triggerKey);
            // 不存在，创建一个
            if (null == trigger) {
                JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron)
                        // misfire恢复后立即执行, 然后继续按照cron规则执行
                        .withMisfireHandlingInstructionFireAndProceed();
                // 按新的cronExpression表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
                sched.scheduleJob(jobDetail, trigger);
                LogUtil.debug("----------添加定时器成功 : Group=",jobGroupName,", name=", jobName, "---------------", cron);
            } else {
                // 否则就修改cron值
                modifyJobCron(sched, jobName, jobGroupName, cron);
            }
        } catch (SchedulerException e) {
            LogUtil.error(e);
        }
    }

    public static void addJobForCirc(Scheduler sched, String jobName, String jobGroupName,
            Class<? extends Job> jobClass, int intervalInSeconds) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        // 获取trigger
        Trigger trigger;
        try {
            trigger = sched.getTrigger(triggerKey);
            // 不存在，创建一个
            if (null == trigger) {
                JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
                // 表达式调度构建器
                trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).startNow().withSchedule(
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalInSeconds).repeatForever())
                        .build();
                sched.scheduleJob(jobDetail, trigger);
                LogUtil.debug("----------添加定时器成功 :", jobName, "---------------");
            }
        } catch (SchedulerException e) {
            LogUtil.error(e);
        }
    }

    /**
     * 修改触发器的时间
     * 
     * @param sched
     * @param jobName
     * @param jobGroupName
     * @param cron
     */
    public static void modifyJobCron(Scheduler sched, String jobName, String jobGroupName, String cron) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        try {
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            String oldCron = trigger.getCronExpression();
            if (!oldCron.equalsIgnoreCase(cron)) {
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron)
                        // misfire恢复后立即执行, 然后继续按照cron规则执行
                        .withMisfireHandlingInstructionFireAndProceed();
                // 按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
                // 按新的trigger重新设置job执行
                sched.rescheduleJob(triggerKey, trigger);
            }

        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 移除定时器
     * 
     * @param sched
     * @param jobName
     * @param jobGroupName
     */
    public static void removeJob(Scheduler sched, String jobName, String jobGroupName) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            sched.pauseTrigger(triggerKey);
            boolean unscheduleJob = sched.unscheduleJob(triggerKey);
            boolean deleteJob = sched.deleteJob(jobKey);
            if (deleteJob || unscheduleJob) {
                LogUtil.debug("deleteJob success!!! jobName: " , jobName, ", jobGroupName: ", jobGroupName);
            } else {
                LogUtil.debug("deleteJob fail!!! jobName: " , jobName, ", jobGroupName: ", jobGroupName);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public static void removeJobByGroup(Scheduler scheduler,String group){
        try {
            GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(group);
            Set<JobKey> jobKeySet = scheduler.getJobKeys(groupMatcher);
            if(Objects.nonNull(jobKeySet)){
                List<JobKey> jobKeyList = new ArrayList<>(jobKeySet);
                scheduler.deleteJobs(jobKeyList);
                LogUtil.debug(String.format("根据Group删除jobs,Group=%s,jobs=%s",group, Arrays.toString(jobKeyList.toArray())));
            }
        }catch (Exception e) {
            LogUtil.error("根据Group删除job发生错误",e);
        }
    }

    /**
     * 开启定时任务
     * 
     * @param sched
     */
    public static void startJobs(Scheduler sched) {
        try {
            sched.start();
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 关闭所有定时任务
     * 
     * @param sched
     */
    public static void shutdownJobs(Scheduler sched) {
        try {
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 获取开始时间
     * 
     * @param sched
     * @param name
     * @param group
     * @return
     */
    public static Date getStartTime(Scheduler sched, String name, String group) {
        try {
            Trigger trigger = sched.getTrigger(new TriggerKey(name, group));
            if (trigger != null) {
                return trigger.getStartTime();
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    /**
     * 获取下次触发时间
     *
     */
    public static Date getNextFireTime(Scheduler sched, String name, String group) {
        try {
            Trigger trigger = sched.getTrigger(new TriggerKey(name, group));
            if (trigger != null) {
                return trigger.getNextFireTime();
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    /**
     * 获取剩余时间
     *
     */
    public static long getRemainTime(Scheduler sched, String name, String group) {
        Date d = getNextFireTime(sched, name, group);
        if (d != null) {
            long t = d.getTime() - System.currentTimeMillis();
            return t >= 0 ? t : 0;
        }
        return 0;
    }

    /**
     * 添加执行一次的任务
     *
     * @param job
     * @param run
     * @param startAt
     * @return
     */
    public static boolean addJobAtDate(Scheduler sched, QuartzJob job, QuartzJobRun run, Date startAt) {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());
        JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger != null) {// 移除定时任务
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(job.getRunKey(), run);
            trigger = TriggerBuilder.newTrigger().withIdentity(job.getName(), job.getGroup()).startAt(startAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------添加定时器成功 :", job, ", Date,", startAt);
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("无法添加延时任务：", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * 添加一个延迟
     * 
     * @param sched
     * @param job
     * @param run
     * @param delaySeconds
     * @return
     */
    public static boolean addJobDelay(Scheduler sched, QuartzJob job, QuartzJobRun run, int delaySeconds) {
        return addJobAtDate(sched, job, run, DateBuilder.futureDate(delaySeconds, IntervalUnit.SECOND));
    }

    /**
     * 周期性执行
     *
     * @param job
     * @param run
     * @param startAt 开始时间
     * @param endAt 结束时间
     * @param intervalInSeconds 间隔时间
     * @return
     */
    public static boolean addJobPeriod(Scheduler sched, QuartzJob job, QuartzJobRun run, Date startAt, Date endAt,
            int intervalInSeconds) {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());
        JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
        try {
            Trigger trigger = sched.getTrigger(triggerKey);
            if (trigger != null) {// 移除定时任务
                QuartzHelper.removeJob(sched, job.getName(), job.getGroup());
            }
            JobDetail detail = JobBuilder.newJob(job.getClass()).withIdentity(jobKey).build();
            detail.getJobDataMap().put(job.getRunKey(), run);
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getName(), job.getGroup()).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds).repeatForever())
                    .startAt(startAt).endAt(endAt).build();
            sched.scheduleJob(detail, trigger);
            LogUtil.debug("----------添加定时器成功 :", job, "startAt:", startAt, ", endAt:", endAt, ", intervalInSeconds:",
                    intervalInSeconds);
            return true;
        } catch (SchedulerException ex) {
            LogUtil.error("无法添加延时任务：", job.toString(), ":", ex);
        }
        return false;
    }

    /**
     * 定时器是否存在
     * 
     * @param sched
     * @param name
     * @param group
     * @return
     */
    public static boolean isExistSched(Scheduler sched, String name, String group) {
        try {
            Trigger trigger = sched.getTrigger(new TriggerKey(name, group));
            return trigger != null;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FunctionalInterface
    public static interface QuartzJobRun {
        void run(JobExecutionContext context);
    }

    public static interface QuartzJob extends Job {
        String getName();

        String getGroup();

        String getRunKey();
    }
}
