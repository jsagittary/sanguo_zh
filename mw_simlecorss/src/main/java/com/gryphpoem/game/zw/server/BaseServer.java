package com.gryphpoem.game.zw.server;

import java.util.function.Predicate;

import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName BaseServer.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
public abstract class BaseServer extends Server {

    /** 是否已经启动 */
    private volatile boolean started;

    protected BaseServer(String name) {
        super(name);
    }

    @Override
    public void run() {
        super.run();
        invokeStart((t) -> onBeforeStarted(), "onBeforeStarted 启动前配置");
        invokeStart((t) -> loadCfg(), "loadCfg 加载配置数据");
        invokeStart((t) -> loadData(), "loadCfg 加载数据");
        invokeStart((t) -> netStart(), "netStart 启动端口");
        invokeStart((t) -> onAfterStarted(), "onAfterStarted 启动后执行");
        this.started = true;
        LogUtil.start(FileUtil.readClassPathFileStr("/icon.txt"));
    }

    public static void invokeStart(Predicate<Void> c, String param) {
        boolean isSucc = false;
        long start = System.currentTimeMillis();
        try {
            isSucc = c.test(null);
            LogUtil.start(param + "--- 执行耗时:" + (System.currentTimeMillis() - start) + " 毫秒");
        } catch (Exception e) {
            LogUtil.error(param + "启动失败，退出", e);
        }
        if (!isSucc) {
            LogUtil.error(param + "启动失败，退出");
            System.exit(1);
        }
    }

    /**
     * 启动前
     */
    protected abstract boolean onBeforeStarted();

    /**
     * 加载配置数据
     * 
     * @return
     */
    protected abstract boolean loadCfg();

    /**
     * 加载跨服数据
     * 
     * @return
     */
    protected abstract boolean loadData();

    /**
     * 服务启动
     */
    protected abstract boolean netStart();

    /**
     * 启动完成之后
     */
    protected abstract boolean onAfterStarted();

    /**
     * 停止时
     */
    protected abstract void onStop();

    public boolean isStarted() {
        return started;
    }

    @Override
    protected void stop() {
        onStop();
    }
}
