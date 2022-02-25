package com.gryphpoem.game.zw.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @ClassName AbsGameJob.java
 * @Description 自己项目中Quartz的job的基类,不要直接实现Quartz的Job
 * @author QiuKun
 * @date 2018年5月17日
 */
public abstract class AbsGameJob implements Job {
    protected JobExecutionContext context;
    protected String name;
    protected String group;

    public AbsGameJob() {
    }

    public AbsGameJob(String name, String group) {
        this.name = name;
        this.group = group;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.context = context;
        this.name = context.getJobDetail().getKey().getName();
        this.group = context.getJobDetail().getKey().getGroup();
        this.process();
    }

    protected abstract void process();

    public JobExecutionContext getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public void immediatelyProcess(String name, String group, Runnable run) {
        if (run != null) run.run();
    }

    @Override
    public String toString() {
        return "AbsGameJob [name=" + name + ", group=" + group + "]";
    }

}
