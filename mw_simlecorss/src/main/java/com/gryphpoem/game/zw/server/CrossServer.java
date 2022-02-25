package com.gryphpoem.game.zw.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hotfix.GameAgent;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.mgr.CmdMgr;
import com.gryphpoem.game.zw.mgr.DataLoaderMgr;
import com.gryphpoem.game.zw.mgr.DbSaveMgr;
import com.gryphpoem.game.zw.mgr.ExecutorPoolMgr;
import com.gryphpoem.game.zw.mgr.ServerCfgMgr;
import com.gryphpoem.game.zw.mgr.ZkRegisterMgr;
import com.gryphpoem.game.zw.mgr.timer.TimerMgr;
import com.gryphpoem.game.zw.network.tcp.CrossChannelHandler;
import com.gryphpoem.game.zw.network.tcp.NettyTcpServer;
import com.gryphpoem.game.zw.server.hotfix.HotfixServer;
import com.gryphpoem.game.zw.service.CrossPlayerService;
import com.gryphpoem.game.zw.service.CrossWarService;
import com.gryphpoem.game.zw.util.ProtoRegistry;

/**
 * @ClassName CrossServer.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
public class CrossServer extends BaseServer {
    private static CrossServer ins;
    private NettyTcpServer crossTcpServer;
    public static ApplicationContext ac;

    public static CrossServer getIns() {
        if (ins == null) {
            ins = new CrossServer();
        }
        return ins;
    }

    private CrossServer() {
        super("Cross");
    }

    @Override
    public String getGameType() {
        return "Cross";
    }

    @Override
    protected boolean onBeforeStarted() {
        ProtoRegistry.registry(); // 协议注册

        // 初始化spring
        ac = new ClassPathXmlApplicationContext("crossAppCtx.xml");
        DataResource.ac = ac;

        ac.getBean(HotfixServer.class).init();
        LogUtil.start("服务器热更已启动");
        LogUtil.start("服务器热更钩子 game-agent : " + GameAgent.inst);

        // 服务器配置加载
        ServerCfgMgr serverCfgMgr = ac.getBean(ServerCfgMgr.class);
        serverCfgMgr.init();

        // cmd初始化
        CmdMgr cmdMgr = ac.getBean(CmdMgr.class);
        cmdMgr.init();

        // 线程池的初始化
        ExecutorPoolMgr.getIns();
        return true;
    }

    @Override
    protected boolean loadCfg() {
        LoadCfgServer.loadCfg(); // 加载配置
        return true;
    }

    @Override
    protected boolean loadData() {
        DataLoaderMgr dataLoaderMgr = ac.getBean(DataLoaderMgr.class);
        dataLoaderMgr.dataHandle();
        return true;
    }

    @Override
    protected boolean netStart() {
        ServerCfgMgr serverCfgMgr = ac.getBean(ServerCfgMgr.class);
        crossTcpServer = new NettyTcpServer(serverCfgMgr.getTcpPort(), () -> new CrossChannelHandler());
        boolean isTcpStart = crossTcpServer.startService(); // 开启服务
        if (!isTcpStart) {
            LogUtil.error("tcp服务启动失败");
            return false;
        }
        invokeStart(c -> {
            ZkRegisterMgr zkRegisterMgr = ac.getBean(ZkRegisterMgr.class);
            if (!zkRegisterMgr.init()) {
                return false;
            }
            return zkRegisterMgr.register();
        }, "向zk注册");

        return true;
    }

    @Override
    protected boolean onAfterStarted() {
        ac.getBean(TimerMgr.class).init();
        LogUtil.start("定时器初始成功");
        ac.getBean(CrossWarService.class).startInit();
        LogUtil.start("跨服战斗初始...");
        LogUtil.start("启动成功...");
        return true;
    }

    @Override
    protected void onStop() {
        LogUtil.start("onStop...");
        // 停服时将领都会到主服 临时处理方案
        ac.getBean(CrossPlayerService.class).stopSeverPlayerHeroProcess();
        if (crossTcpServer != null) {
            crossTcpServer.stopService();
        }
        ac.getBean(ZkRegisterMgr.class).stop();
        ac.getBean(TimerMgr.class).stop();
        if (!isStarted()) { // 没有启动不做后面的保存
            return;
        }
        // 保存数据
        ac.getBean(DbSaveMgr.class).onStop();
        // 线程池关闭
        try {
            ExecutorPoolMgr.getIns().stop();
        } catch (InterruptedException e) {
            LogUtil.error(e);
        }
    }

}
