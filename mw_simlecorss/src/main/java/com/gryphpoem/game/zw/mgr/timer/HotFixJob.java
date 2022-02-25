package com.gryphpoem.game.zw.mgr.timer;

import com.gryphpoem.game.zw.quartz.jobs.AbsGameJob;
import com.gryphpoem.game.zw.server.CrossServer;
import com.gryphpoem.game.zw.server.hotfix.HotfixServer;

/**
 * @ClassName HotFixJob.java
 * @Description 热更服务
 * @author QiuKun
 * @date 2019年6月12日
 */
public class HotFixJob extends AbsGameJob {

    @Override
    protected void process() {
        CrossServer.ac.getBean(HotfixServer.class).hotfixWithTimeLogic();
    }

}
