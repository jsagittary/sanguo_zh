package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.ConnectMessageHandler;
import com.gryphpoem.game.zw.core.HttpMessageHandler;
import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.intercept.InterceptAspect;
import com.gryphpoem.game.zw.core.intercept.MessageInterceptorManager;
import com.gryphpoem.game.zw.core.message.MessagePool;
import com.gryphpoem.game.zw.core.net.ConnectServer;
import com.gryphpoem.game.zw.core.net.HttpServer;
import com.gryphpoem.game.zw.core.net.InnerServer;
import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.core.net.base.HttpBaseChannelHandler;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.work.HttpWork;
import com.gryphpoem.game.zw.core.work.RWork;
import com.gryphpoem.game.zw.core.work.WWork;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.intercept.FunctionUnlockInterceptor;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.SmallIdManager;
import com.gryphpoem.game.zw.mgr.InnerServerMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1.BeginGameRq;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.pb.HttpPb.RegisterRq;
import com.gryphpoem.game.zw.push.PushServer;
import com.gryphpoem.game.zw.push.PushUtil;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.dao.impl.p.AccountDao;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.SmallId;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.rpc.provider.GameServerRpcServerImpl;
import com.gryphpoem.game.zw.service.AbsGameService;
import com.gryphpoem.game.zw.service.GmCmdProcessor;
import com.start.GameDataLoader;
import com.start.GameDataManager;
import com.start.ProtoRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.TrafficCounter;
import org.apache.dubbo.config.DubboShutdownHook;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AppGameServer extends Server {

    public ConnectServer connectServer;
    private HttpServer httpServer;
    private InnerServer innerServer;
    public LogicServer mainLogicServer;
    // public MsgServer msgServer;
    private PushServer pushServer;

    private SavePlayerServer savePlayerServer;
    private SaveGlobalServer saveGlobalServer;
    private SavePartyServer savePartyServer;
    private SaveGlobalActivityServer saveActivityServer;
    private SaveCrossMapServer saveCrossMapServer;
    private SendMsgServer sendMsgServer;
    private SendEventDataServer sendEventDataServer;
    private SaveMailReportServer saveMailReportServer;

    private boolean startSuccess = false;// 记录游戏进程启动是否成功，用于启动失败退出时，跳过数据保存

    public ConcurrentHashMap<Long, ChannelHandlerContext> userChannels = new ConcurrentHashMap<Long, ChannelHandlerContext>();

    public static ApplicationContext ac;

    // public Date OPEN_DATE = DateHelper.parseDate(ac.getBean(ServerSetting.class).getOpenTime());

    private AppGameServer() {
        super("AppGameServer");
    }

    private static AppGameServer gameServer;

    public static AppGameServer getInstance() {
        if (gameServer == null) {
            // spring的初始化
            ac = new ClassPathXmlApplicationContext("applicationContext.xml");
            DataResource.ac = ac;
            gameServer = new AppGameServer();
            removeSpringShutdownHook();
            removeDubboShutdownHook();
        }
        return gameServer;
    }

    private static void removeSpringShutdownHook() {
        try {
            Class<?> shutdownHookClass = ac.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass();
            Field field = shutdownHookClass.getDeclaredField("shutdownHook");
            if (Objects.nonNull(field)) {
                field.setAccessible(true);
                Thread shutdownHook = (Thread) field.get(ac);
                if (Objects.nonNull(shutdownHook)){
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                }
            }
        } catch (Exception e) {
            LogUtil.error("移除 spring shutdownHook 失败!!! ", e);
        }
    }
    private static void removeDubboShutdownHook(){
        DubboShutdownHook dubboShutdownHook = DubboShutdownHook.getDubboShutdownHook();
        Runtime.getRuntime().removeShutdownHook(dubboShutdownHook);
    }


    public void sendMsgToGamer(ChannelHandlerContext ctx, Base.Builder baseBuilder) {
        Base msg = baseBuilder.build();
        int cmd = msg.getCmd();
        // if (!Constant.IGNORE_LOG_CMD.contains(cmd)) { // 屏蔽日志输出
        //     LogUtil.c2sMessage(msg, ChannelUtil.getRoleId(ctx));
        // }
        connectServer.sendExcutor.addTask(ChannelUtil.getChannelId(ctx), new WWork(ctx, msg));
    }

    public void synMsgToGamer(ChannelHandlerContext ctx, Base msg) {
        // LogUtil.c2sMessage(msg, ChannelUtil.getRoleId(ctx));
        connectServer.sendExcutor.addTask(ChannelUtil.getChannelId(ctx), new WWork(ctx, msg));
    }

    public void synMsgToGamer(Msg msg) {
        // LogUtil.c2sMessage(msg.getMsg(), ChannelUtil.getRoleId(msg.getCtx()));
        connectServer.sendExcutor.addTask(ChannelUtil.getChannelId(msg.getCtx()),
                new WWork(msg.getCtx(), msg.getMsg()));
    }

    public void sendMsgToPublic(Base.Builder baseBuilder, int serverId) {
        sendPublicMsg(baseBuilder.build(), serverId);
    }

    public void sendPublicMsg(Base msg, int serverId) {
        LogUtil.s2sMessage(msg);
        String u = AppGameServer.ac.getBean(ServerSetting.class).getAccountServerUrl() + "?serverId=" + serverId;
        sendPublicMsg(msg, u);
    }

    public void sendMsgToPublic(Base.Builder baseBuilder) {
        sendPublicMsg(baseBuilder.build(), AppGameServer.ac.getBean(ServerSetting.class).getAccountServerUrl());
    }

    public void sendPublicMsg(Base msg, String url) {
        LogUtil.s2sMessage(msg);
        httpServer.sendExcutor.execute(new HttpWork(httpServer, msg, url));
    }

    // 向支付服发送消息
    public void sendMsgToPublicPay(Base.Builder baseBuilder) {
        sendPublicMsg(baseBuilder.build(), AppGameServer.ac.getBean(ServerSetting.class).getPayServerUrl());
    }

    /**
     * 发消息到跨服战服务器
     *
     * @param baseBuilder
     */
    public void sendMsgToCross(Base.Builder baseBuilder) {
        sendMsgToCross(baseBuilder, 0);
    }

    /**
     * 发消息到跨服战服务器
     *
     * @param baseBuilder
     */
    public void sendMsgToCross(Base.Builder baseBuilder, long lordId) {
        if (lordId > 0) {
            baseBuilder.setLordId(lordId);
        }
        Base msg = baseBuilder.build();
        innerServer.sendMsg(msg);
    }

    /**
     * 未捕获异常处理
     *
     * @author TanDonghai
     * @Description
     */
    private class GameUncaughtExceptionHandler implements UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            LogUtil.error("GameUncaughtExceptionHandler uncaughtException", e);

            AppGameServer.getInstance().startSuccess = false;
            System.exit(1);// 重要线程启动失败，立即退出
        }

    }

    private void startServerThread(Runnable runnable) {
        if (runnable != null) {
            Thread thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler(new GameUncaughtExceptionHandler());
            thread.start();
        }
    }

    @Override
    public void run() {
        super.run();

        GameServerRpcServerImpl impl = ac.getBean("gameServerRpcServer", GameServerRpcServerImpl.class);
//        impl.checkServerAlreadyStart();


        //检查gm命令
        try {
            ac.getBean(GmCmdProcessor.class).checkErrors();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 加载协议
        ProtoRegistry.registry();

        // 在DataResource中记录服务器信息
        ServerSetting serverSetting = ac.getBean(ServerSetting.class);
        DataResource.serverId = serverSetting.getServerID();
        DataResource.serverName = serverSetting.getServerName();
        DataResource.environment = serverSetting.getEnvironment();

        // 初始化行为树节点管理类
        // BtreeNodeManager.init();

        // 加载数据
        try {
            GameDataLoader.getIns().loadGameData();
        } catch (MwException e) {
            e.printStackTrace();
            LogUtil.error("数据加载失败，退出", e);
            System.exit(1);
        }

        // 修复数据
        try {
            GameDataManager.getIns().dataHandle();
            // 跨服服务器连接,此处会向注册中心获取ip 端口
            innerServer = InnerServerMgr.createInnerServer();
            DataResource.innerServer = innerServer;
        } catch (MwException e) {
            LogUtil.error("数据修复出错，退出", e);
            System.exit(1);
        }

        LogUtil.start("数据加载完成，开始启动服务器业务线程");

        // 注册协议
        MessagePool.getIns().setAgentMessagePool(new com.gryphpoem.game.zw.message.MessagePool());

        // 设置客户端协议前置拦截器
        MessageInterceptorManager.getIns().registerInterceptor(InterceptAspect.CLIENT_MESSAGE,
                new FunctionUnlockInterceptor());
        // 初始化通信服务
        initMsgServer(serverSetting);
        // 创建定时任务
        initTimer();

        // 向账号服注册服务器信息
        registerGameToPublic();
        // 发送所有的小号到账号服
        // sendSmallToAccount();
        //启服时的处理
        Collection<AbsGameService> absGameServices = ac.getBeansOfType(AbsGameService.class).values();
        absGameServices.forEach(o -> {
            try {
                o.handleOnStartup();
            } catch (Exception e) {
                LogUtil.error("服务器启动时处理业务数据发生错误," + absGameServices,e);
                System.exit(1);
            }
        });
        LogUtil.start("AppGameServer " + AppGameServer.ac.getBean(ServerSetting.class).getServerName() + " Started");
        startSuccess = true;
    }

    /**
     * 创建定时任务,注意使用Timer后不支持时间回调,不然会失效
     */
    private void initTimer() {
        // 发送任务使用
        new Timer("Send-Task-Timer").schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.flow("等待发送个数:" + connectServer.sendExcutor.getTaskCounts());
                LogUtil.flow("等待解码个数:" + connectServer.recvExcutor.getTaskCounts());
            }
        }, 5 * 1000L, 30 * 1000L);

        // 消息数量用
        new Timer("AllMessage-Timer").schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.flow("接收消息个数：" + connectServer.maxMessage.get());
            }
        }, 1 * 1000, 60 * 1000);

        // 流量测试使用
        new Timer("Quantity-Timer").schedule(new TimerTask() {

            @Override
            public void run() {
                if (connectServer.trafficShapingHandler == null) return;

                TrafficCounter trafficCounter = connectServer.trafficShapingHandler.trafficCounter();
                StringBuffer buf = new StringBuffer();
                buf.append("WB:" + trafficCounter.currentWrittenBytes()).append(",");
                buf.append("RB:" + trafficCounter.currentReadBytes()).append(",");

                buf.append("WT:" + trafficCounter.cumulativeWrittenBytes()).append(",");
                buf.append("RT:" + trafficCounter.cumulativeReadBytes()).append(",");
                buf.append("WS:" + trafficCounter.lastWriteThroughput()).append(",");
                buf.append("RS:" + trafficCounter.lastReadThroughput());
                LogUtil.flow(buf);
            }
        }, 5 * 1000, 60 * 1000);
    }

    /**
     * 创建通信服务
     *
     * @param serverSetting 游戏服配置
     */
    private void initMsgServer(ServerSetting serverSetting) {
        // 与客户端交互的服务
        connectServer = new ConnectServer(
                Integer.parseInt(AppGameServer.ac.getBean(ServerSetting.class).getClientPort())) {
            @Override
            protected BaseChannelHandler initGameServerHandler() {
                return new ConnectMessageHandler(this);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Base msg) {
                maxMessage.incrementAndGet();
                Long roleId = ChannelUtil.getRoleId(ctx);
                LogUtil.c2sReqMessage(msg, roleId);

                int cmd = msg.getCmd();
                if (roleId == 0L && cmd != BeginGameRq.EXT_FIELD_NUMBER) {
                    ChannelUtil.closeChannel(ctx, "没有发送beginGame消息");
                    return;
                }

                Long id = ChannelUtil.getChannelId(ctx);
                recvExcutor.addTask(id, new RWork(ctx, msg));
            }
        };

        // 内部HTTP交互服务
        httpServer = new HttpServer(Integer.parseInt(AppGameServer.ac.getBean(ServerSetting.class).getHttpPort())) {
            public HttpBaseChannelHandler initHandler() {
                return new HttpMessageHandler(this);
            }
        };

        mainLogicServer = new LogicServer(serverSetting.getServerName(), 500);
        DataResource.logicServer = mainLogicServer;// 赋值
        // msgServer = new MsgServer(ac.getBean(ServerSetting.class).getServerName(), 500);
        pushServer = new PushServer();
        PushUtil.setPushServer(pushServer);

        startServerThread(connectServer);
        startServerThread(httpServer);
        startServerThread(mainLogicServer);
        // startServerThread(msgServer);
        startServerThread(pushServer);
        // 跨服
        startServerThread(innerServer);

        // 启动数据保存线程
        savePlayerServer = SavePlayerServer.getIns();
        saveGlobalServer = SaveGlobalServer.getIns();
        savePartyServer = SavePartyServer.getIns();
        saveActivityServer = SaveGlobalActivityServer.getIns();
        saveCrossMapServer = SaveCrossMapServer.getIns();
        saveMailReportServer = SaveMailReportServer.getIns();
        sendMsgServer = SendMsgServer.getIns(connectServer);
        sendEventDataServer = SendEventDataServer.getIns();

        startServerThread(savePlayerServer);
        startServerThread(saveGlobalServer);
        startServerThread(savePartyServer);
        startServerThread(saveActivityServer);
        startServerThread(saveCrossMapServer);
        startServerThread(sendEventDataServer);
        startServerThread(saveMailReportServer);
        // quartz 任务启动
        ScheduleManager.getInstance().initRegisterJob();
    }

    /**
     * 发送所有的小号到账号服
     */
    private void sendSmallToAccount() {
        AccountDao accountDao = ac.getBean(AccountDao.class);
        SmallIdManager smallIdManager = ac.getBean(SmallIdManager.class);
        Map<Long, SmallId> smallIdCache = smallIdManager.getSmallIdCache();
        if (CheckNull.isEmpty(smallIdCache)) {
            return;
        }
        // 加载所有account
        List<Account> accountList = accountDao.load();
        if (CheckNull.isEmpty(accountList)) {
            return;
        }
        // 以账号key分组
        Map<Long, Account> accountMap = accountList.stream().collect(Collectors.toMap(Account::getAccountKey, Function.identity(), (oldA, newA) -> newA));
        // 生成需要上传的小号列表
        List<CommonPb.SmallAccountData> sendList = smallIdCache.values().stream()
                .filter(si -> accountMap.containsKey(si.getAccountKey()))
                .map(smallId -> {
                    Account account = accountMap.get(smallId.getAccountKey());
                    return PbHelper.createSmallAccountData(smallId.getLordId(), account.getAccountKey(), account.getServerId());
                }).collect(Collectors.toList());

        // 一次最多发送1000个角色
        int num = 1000;
        // 批量发送小号数据信息
        HttpPb.SendSmallToAccountRq.Builder push = HttpPb.SendSmallToAccountRq.newBuilder();

        for (int i = 0; i < sendList.size(); i++) {
            num--;
            push.addSmallData(sendList.get(i));
            if (num == 0 || i == sendList.size() - 1) {
                // 推送小号信息给账户服
                BasePb.Base.Builder baseBuilder = PbHelper.createRqBase(HttpPb.SendSmallToAccountRq.EXT_FIELD_NUMBER, null,
                        HttpPb.SendSmallToAccountRq.ext, push.build());
                sendMsgToPublic(baseBuilder);
                push = HttpPb.SendSmallToAccountRq.newBuilder();
                num = 1000;
            }
        }
        LogUtil.start("总上传小号Account数据 " + sendList.size() + "条");
    }

    /**
     * 向账号服注册游戏服的信息
     */
    private void registerGameToPublic() {
        RegisterRq.Builder builder = RegisterRq.newBuilder();
        builder.setServerId(AppGameServer.ac.getBean(ServerSetting.class).getServerID());
        builder.setServerName(AppGameServer.ac.getBean(ServerSetting.class).getServerName());
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(RegisterRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(RegisterRq.ext, builder.build());

        Base msg = baseBuilder.build();
        sendPublicMsg(msg, AppGameServer.ac.getBean(ServerSetting.class).getAccountServerUrl());
    }

    /**
     * 所有的数据都保存完了
     *
     * @return
     */
    private boolean allSaveDone() {
        if (savePlayerServer.saveDone() && saveGlobalServer.saveDone() && savePartyServer.saveDone()
                && saveActivityServer.saveDone() && saveCrossMapServer.saveDone()) { //&& sendEventDataServer.sendDone()
            return true;
        }

        return false;
    }

    @Override
    protected void stop() {
        try {
            long stopMillis = System.currentTimeMillis();
            if (mainLogicServer != null) {
                mainLogicServer.stop();
            }

            if (mainLogicServer != null) {
                while (!mainLogicServer.isStopped()) {
                    Thread.sleep(1);
                }
            }

            if (!startSuccess) {// 如果启动失败退出，不走保存数据流程
                LogUtil.error("启动异常，不保存数据，直接退出");
                return;
            }

            if (savePlayerServer != null) {
                savePlayerServer.stopServer();
            }

            if (saveGlobalServer != null) {
                saveGlobalServer.stopServer();
            }

            if (savePartyServer != null) {
                savePartyServer.stopServer();
            }

            if (saveActivityServer != null) {
                saveActivityServer.stopServer();
            }

            if (saveCrossMapServer != null) {
                saveCrossMapServer.stopServer();
            }
            if (saveMailReportServer != null) {
                saveMailReportServer.stopServer();
            }

            if (sendEventDataServer != null) {
                sendEventDataServer.setLogFlag();
                //上报未上报的数据
                EventDataUp.allRequest();
                LogUtil.save(" 上报未上报的数据");
                sendEventDataServer.stop();
            }

            // TODO: 2020/8/12 后期优化这里, 使用CountDownLatch改造
            int sleepTime = 0;
            while (!(/*sleepTime > 60 * 1000 || */allSaveDone())) {
                Thread.sleep(60);
                if (sleepTime != 0 && (sleepTime % TimeHelper.MINUTE_MS) == 0) {
                    LogUtil.error("服务器停服保存时间过长, 请联系服务端同学来处理问题！ 保存使用时间: " + sleepTime + "ms");
                }
                sleepTime += 60;
            }

            LogUtil.stop(savePlayerServer.serverName() + " has done with save :" + savePlayerServer.allSaveCount());
            LogUtil.stop(saveGlobalServer.serverName() + " has done with save :" + saveGlobalServer.allSaveCount());
            LogUtil.stop(savePartyServer.serverName() + " has done with save :" + savePartyServer.allSaveCount());
            LogUtil.stop(saveActivityServer.serverName() + " has done with save :" + saveActivityServer.allSaveCount());
            LogUtil.stop(saveCrossMapServer.serverName() + " has done with save :" + saveCrossMapServer.allSaveCount());
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            String runname = ManagementFactory.getRuntimeMXBean().getName();
            String pid = runname.substring(0, runname.indexOf("@"));
            if (allSaveDone()) {
                LogUtil.stop("数据保存完全GameServer-->" + location + "|" + runname + "|" + pid + "|" + "all saved!");
            } else {
                LogUtil.stop("部分数据未成功保存GameServer-->" + location + "|" + runname + "|" + pid + "|" + "part saved!");
            }
            //游戏数据处理完成后，处理上报线程
            long keepMillis = 5 * 60 * 1000;
            long millis1 = System.currentTimeMillis();
            while (!sendEventDataServer.sendDone()) {
                if (System.currentTimeMillis() - millis1 >= keepMillis) {
                    //超过持续时间还未停下来，强制中断上报线程
                    LogUtil.stop("数数上报线程持续超过 5 minute, 强制中断");
                    sendEventDataServer.interruptAll();
                    break;
                } else {
                    LogUtil.stop("数数上报线程还在处理中, SLEEP 1 sec, keepMillis=" + (System.currentTimeMillis() - millis1));
                    Thread.sleep(1000);
                }
            }
            Thread.sleep(1000);
            stopMillis = System.currentTimeMillis() - stopMillis;
            LogUtil.stop(String.format("玩家数据[%s]共耗时%sMS [%s]服停止耗时: %s 秒",savePlayerServer.allSaveCount(),savePlayerServer.stopMillis,DataResource.serverId,stopMillis/1000));
            LogUtil.stop(">>>>>>>>>>>>>>>>>>>>++++++++++++++++++<<<<<<<<<<<<<<<<<<<<");
            LogUtil.stop(">>>>>>>>>>>>>>>>>>>>GAME SERVER STOPED<<<<<<<<<<<<<<<<<<<<");
            LogUtil.stop(">>>>>>>>>>>>>>>>>>>>++++++++++++++++++<<<<<<<<<<<<<<<<<<<<");
        } catch (Exception e) {
            LogUtil.error("服务器停服异常", e);
        }
    }

    /**
     * 玩家退出游戏
     *
     * @param closeCtx
     * @param roleId
     */
    public void gamerExit(ChannelHandlerContext closeCtx, long roleId) {
        PlayerDataManager playerDataManager = ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            return;
        }
        if (player.ctx != null && player.ctx != closeCtx) {// 重复登录
            DataResource.getBean(CrossGamePlayService.class).enterLeaveCrossMap(player);
            player.immediateSave = true;
            return;
        }

        playerDataManager.removeOnline(player);
        player.logOut();

        DataResource.removeRoleChannel(roleId);
    }

    /**
     * 检测区服时候已经启动
     *
     * @param serverId 区服ID
     */
    private void checkServerAlreadyStart(String serverId) {

//        try {
//            Map<String, String> serverCheckParamMap = new HashMap<>();
//            serverCheckParamMap.put("serverId", serverId);
////            serverCheckParamMap.put("jdbcUrl", )
//            String rspString = HttpUtils.sendGet("http://172.16.13.28:8849/rpc/manager/server/test", serverCheckParamMap);
//            LogUtil.start("启动服务器ID唯一性检测结果: " + rspString);
//            JSONObject obj = JSONObject.parseObject(rspString);
//            String code = obj.getString("code");
//            if ("200".equals(code)) {
//                JSONArray data = obj.getJSONArray("data");
//                if (Objects.nonNull(data)) {
//                    if (data.size() > 0) {
//                        LogUtil.error2Sentry(String.format("区服ID: %s, 已经启动, 程序启动失败!!!", serverId));
//                        System.exit(1);
//                    }
//                }
//            } else {
//                LogUtil.error(String.format("启动服务器ID: %s 唯一性校验失败, 程序退出!!!", serverId));
//                System.exit(1);
//            }
//        } catch (Exception e) {
//            LogUtil.error(String.format("区服ID :%s 唯一性检测异常 程序启动失败!!!", serverId), e);
//            System.exit(1);
//        }
    }

    @Override
    public String getGameType() {
        return "game";
    }
}
