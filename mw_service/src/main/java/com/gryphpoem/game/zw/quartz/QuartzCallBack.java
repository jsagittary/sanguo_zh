package com.gryphpoem.game.zw.quartz;

import com.gryphpoem.game.zw.quartz.jobs.DefultJob;

/**
 * @ClassName QuartzCallBack.java
 * @Description
 * @author QiuKun
 * @date 2018年5月18日
 */
public interface QuartzCallBack {
    void run(DefultJob job);
}
