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

    private boolean startSuccess = false;// ???????????????????????????????????????????????????????????????????????????????????????

    public ConcurrentHashMap<Long, ChannelHandlerContext> userChannels = new ConcurrentHashMap<Long, ChannelHandlerContext>();

    public static ApplicationContext ac;

    // public Date OPEN_DATE = DateHelper.parseDate(ac.getBean(ServerSetting.class).getOpenTime());

    private AppGameServer() {
        super("AppGameServer");
    }

    private static AppGameServer gameServer;

    public static AppGameServer getInstance() {
        if (gameServer == null) {
            // spring????????????
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
            LogUtil.error("?????? spring shutdownHook ??????!!! ", e);
        }
    }
    private static void removeDubboShutdownHook(){
        DubboShutdownHook dubboShutdownHook = DubboShutdownHook.getDubboShutdownHook();
        Runtime.getRuntime().removeShutdownHook(dubboShutdownHook);
    }


    public void sendMsgToGamer(ChannelHandlerContext ctx, Base.Builder baseBuilder) {
        Base msg = baseBuilder.build();
        int cmd = msg.getCmd();
        // if (!Constant.IGNORE_LOG_CMD.contains(cmd)) { // ??????????????????
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

    // ????????????????????????
    public void sendMsgToPublicPay(Base.Builder baseBuilder) {
        sendPublicMsg(baseBuilder.build(), AppGameServer.ac.getBean(ServerSetting.class).getPayServerUrl());
    }

    /**
     * ??????????????????????????????
     *
     * @param baseBuilder
     */
    public void sendMsgToCross(Base.Builder baseBuilder) {
        sendMsgToCross(baseBuilder, 0);
    }

    /**
     * ??????????????????????????????
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
     * ?????????????????????
     *
     * @author TanDonghai
     * @Description
     */
    private class GameUncaughtExceptionHandler implements UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            LogUtil.error("GameUncaughtExceptionHandler uncaughtException", e);

            AppGameServer.getInstance().startSuccess = false;
            System.exit(1);// ???????????????????????????????????????
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


        //??????gm??????
        try {
            ac.getBean(GmCmdProcessor.class).checkErrors();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // ????????????
        ProtoRegistry.registry();

        // ???DataResource????????????????????????
        ServerSetting serverSetting = ac.getBean(ServerSetting.class);
        DataResource.serverId = serverSetting.getServerID();
        DataResource.serverName = serverSetting.getServerName();
        DataResource.environment = serverSetting.getEnvironment();

        // ?????????????????????????????????
        // BtreeNodeManager.init();

        // ????????????
        try {
            GameDataLoader.getIns().loadGameData();
        } catch (MwException e) {
            e.printStackTrace();
            LogUtil.error("???????????????????????????", e);
            System.exit(1);
        }

        // ????????????
        try {
            GameDataManager.getIns().dataHandle();
            // ?????????????????????,??????????????????????????????ip ??????
            innerServer = InnerServerMgr.createInnerServer();
            DataResource.innerServer = innerServer;
        } catch (MwException e) {
            LogUtil.error("???????????????????????????", e);
            System.exit(1);
        }

        LogUtil.start("??????????????????????????????????????????????????????");

        // ????????????
        MessagePool.getIns().setAgentMessagePool(new com.gryphpoem.game.zw.message.MessagePool());

        // ????????????????????????????????????
        MessageInterceptorManager.getIns().registerInterceptor(InterceptAspect.CLIENT_MESSAGE,
                new FunctionUnlockInterceptor());
        // ?????????????????????
        initMsgServer(serverSetting);
        // ??????????????????
        initTimer();

        // ?????????????????????????????????
        registerGameToPublic();
        // ?????????????????????????????????
        // sendSmallToAccount();
        //??????????????????
        Collection<AbsGameService> absGameServices = ac.getBeansOfType(AbsGameService.class).values();
        absGameServices.forEach(o -> {
            try {
                o.handleOnStartup();
            } catch (Exception e) {
                LogUtil.error("????????????????????????????????????????????????," + absGameServices,e);
                System.exit(1);
            }
        });
        LogUtil.start("AppGameServer " + AppGameServer.ac.getBean(ServerSetting.class).getServerName() + " Started");
        startSuccess = true;
    }

    /**
     * ??????????????????,????????????Timer????????????????????????,???????????????
     */
    private void initTimer() {
        // ??????????????????
        new Timer("Send-Task-Timer").schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.flow("??????????????????:" + connectServer.sendExcutor.getTaskCounts());
                LogUtil.flow("??????????????????:" + connectServer.recvExcutor.getTaskCounts());
            }
        }, 5 * 1000L, 30 * 1000L);

        // ???????????????
        new Timer("AllMessage-Timer").schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.flow("?????????????????????" + connectServer.maxMessage.get());
            }
        }, 1 * 1000, 60 * 1000);

        // ??????????????????
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
     * ??????????????????
     *
     * @param serverSetting ???????????????
     */
    private void initMsgServer(ServerSetting serverSetting) {
        // ???????????????????????????
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
                    ChannelUtil.closeChannel(ctx, "????????????beginGame??????");
                    return;
                }

                Long id = ChannelUtil.getChannelId(ctx);
                recvExcutor.addTask(id, new RWork(ctx, msg));
            }
        };

        // ??????HTTP????????????
        httpServer = new HttpServer(Integer.parseInt(AppGameServer.ac.getBean(ServerSetting.class).getHttpPort())) {
            public HttpBaseChannelHandler initHandler() {
                return new HttpMessageHandler(this);
            }
        };

        mainLogicServer = new LogicServer(serverSetting.getServerName(), 500);
        DataResource.logicServer = mainLogicServer;// ??????
        // msgServer = new MsgServer(ac.getBean(ServerSetting.class).getServerName(), 500);
        pushServer = new PushServer();
        PushUtil.setPushServer(pushServer);

        startServerThread(connectServer);
        startServerThread(httpServer);
        startServerThread(mainLogicServer);
        // startServerThread(msgServer);
        startServerThread(pushServer);
        // ??????
        startServerThread(innerServer);

        // ????????????????????????
        savePlayerServer = SavePlayerServer.getIns();
        saveGlobalServer = SaveGlobalServer.getIns();
        savePartyServer = SavePartyServer.getIns();
        saveActivityServer = SaveGlobalActivityServer.getIns();
        saveCrossMapServer = SaveCrossMapServer.getIns();
        sendMsgServer = SendMsgServer.getIns(connectServer);
        sendEventDataServer = SendEventDataServer.getIns();

        startServerThread(savePlayerServer);
        startServerThread(saveGlobalServer);
        startServerThread(savePartyServer);
        startServerThread(saveActivityServer);
        startServerThread(saveCrossMapServer);
        startServerThread(sendEventDataServer);
        // quartz ????????????
        ScheduleManager.getInstance().initRegisterJob();
    }

    /**
     * ?????????????????????????????????
     */
    private void sendSmallToAccount() {
        AccountDao accountDao = ac.getBean(AccountDao.class);
        SmallIdManager smallIdManager = ac.getBean(SmallIdManager.class);
        Map<Long, SmallId> smallIdCache = smallIdManager.getSmallIdCache();
        if (CheckNull.isEmpty(smallIdCache)) {
            return;
        }
        // ????????????account
        List<Account> accountList = accountDao.load();
        if (CheckNull.isEmpty(accountList)) {
            return;
        }
        // ?????????key??????
        Map<Long, Account> accountMap = accountList.stream().collect(Collectors.toMap(Account::getAccountKey, Function.identity(), (oldA, newA) -> newA));
        // ?????????????????????????????????
        List<CommonPb.SmallAccountData> sendList = smallIdCache.values().stream()
                .filter(si -> accountMap.containsKey(si.getAccountKey()))
                .map(smallId -> {
                    Account account = accountMap.get(smallId.getAccountKey());
                    return PbHelper.createSmallAccountData(smallId.getLordId(), account.getAccountKey(), account.getServerId());
                }).collect(Collectors.toList());

        // ??????????????????1000?????????
        int num = 1000;
        // ??????????????????????????????
        HttpPb.SendSmallToAccountRq.Builder push = HttpPb.SendSmallToAccountRq.newBuilder();

        for (int i = 0; i < sendList.size(); i++) {
            num--;
            push.addSmallData(sendList.get(i));
            if (num == 0 || i == sendList.size() - 1) {
                // ??????????????????????????????
                BasePb.Base.Builder baseBuilder = PbHelper.createRqBase(HttpPb.SendSmallToAccountRq.EXT_FIELD_NUMBER, null,
                        HttpPb.SendSmallToAccountRq.ext, push.build());
                sendMsgToPublic(baseBuilder);
                push = HttpPb.SendSmallToAccountRq.newBuilder();
                num = 1000;
            }
        }
        LogUtil.start("???????????????Account?????? " + sendList.size() + "???");
    }

    /**
     * ????????????????????????????????????
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
     * ??????????????????????????????
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
//        try {
//            DubboShutdownHook.destroyAll();
//            LogUtil.stop("????????????dubbo??????");
//        } catch (Exception e) {
//            LogUtil.error("??????dubbo ?????? ??????!!!", e);
//        }

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

            if (!startSuccess) {// ???????????????????????????????????????????????????
                LogUtil.error("?????????????????????????????????????????????");
                return;
            }

            if (savePlayerServer != null) {
//                savePlayerServer.setLogFlag();
//                savePlayerServer.saveAllPlayer();
//                savePlayerServer.stop();
                savePlayerServer.stopServer();
            }

            if (saveGlobalServer != null) {
//                saveGlobalServer.setLogFlag();
//                saveGlobalServer.saveAll();
//                saveGlobalServer.stop();
                saveGlobalServer.stopServer();
            }

            if (savePartyServer != null) {
//                savePartyServer.setLogFlag();
//                savePartyServer.saveAllParty();
//                savePartyServer.stop();
                savePartyServer.stopServer();
            }

            if (saveActivityServer != null) {
//                saveActivityServer.setLogFlag();
//                saveActivityServer.saveAllActivity();
//                saveActivityServer.stop();
                saveActivityServer.stopServer();
            }

            if (saveCrossMapServer != null) {
//                saveCrossMapServer.setLogFlag();
//                saveCrossMapServer.saveAll();
//                saveCrossMapServer.stop();
                saveCrossMapServer.stopServer();
            }
            if (sendEventDataServer != null) {
                sendEventDataServer.setLogFlag();
                //????????????????????????
                EventDataUp.allRequest();
                LogUtil.save(" ????????????????????????");
                sendEventDataServer.stop();
            }

            // TODO: 2020/8/12 ??????????????????, ??????CountDownLatch??????
            int sleepTime = 0;
            while (!(/*sleepTime > 60 * 1000 || */allSaveDone())) {
                Thread.sleep(60);
                if (sleepTime != 0 && (sleepTime % TimeHelper.MINUTE_MS) == 0) {
                    LogUtil.error("?????????????????????????????????, ?????????????????????????????????????????? ??????????????????: " + sleepTime + "ms");
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
                LogUtil.stop("??????????????????GameServer-->" + location + "|" + runname + "|" + pid + "|" + "all saved!");
            } else {
                LogUtil.stop("???????????????????????????GameServer-->" + location + "|" + runname + "|" + pid + "|" + "part saved!");
            }
            //????????????????????????????????????????????????
            long keepMillis = 5 * 60 * 1000;
            long millis1 = System.currentTimeMillis();
            while (!sendEventDataServer.sendDone()) {
                if (System.currentTimeMillis() - millis1 >= keepMillis) {
                    //????????????????????????????????????????????????????????????
                    LogUtil.stop("?????????????????????????????? 5 minute, ????????????");
                    sendEventDataServer.interruptAll();
                    break;
                } else {
                    LogUtil.stop("?????????????????????????????????, SLEEP 1 sec, keepMillis=" + (System.currentTimeMillis() - millis1));
                    Thread.sleep(1000);
                }
            }
            Thread.sleep(1000);
            stopMillis = System.currentTimeMillis() - stopMillis;
            LogUtil.stop(String.format("????????????[%s]?????????%sMS [%s]???????????????: %s ???",savePlayerServer.allSaveCount(),savePlayerServer.stopMillis,DataResource.serverId,stopMillis/1000));
            LogUtil.stop(">>>>>>>>>>>>>>>>>>>>++++++++++++++++++<<<<<<<<<<<<<<<<<<<<");
            LogUtil.stop(">>>>>>>>>>>>>>>>>>>>GAME SERVER STOPED<<<<<<<<<<<<<<<<<<<<");
            LogUtil.stop(">>>>>>>>>>>>>>>>>>>>++++++++++++++++++<<<<<<<<<<<<<<<<<<<<");
        } catch (Exception e) {
            LogUtil.error("?????????????????????", e);
        }
    }

    /**
     * ??????????????????
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
        if (player.ctx != null && player.ctx != closeCtx) {// ????????????
            DataResource.getBean(CrossGamePlayService.class).enterLeaveCrossMap(player);
            player.immediateSave = true;
            return;
        }

        playerDataManager.removeOnline(player);
        player.logOut();

        DataResource.removeRoleChannel(roleId);
    }

    /**
     * ??????????????????????????????
     *
     * @param serverId ??????ID
     */
    private void checkServerAlreadyStart(String serverId) {

//        try {
//            Map<String, String> serverCheckParamMap = new HashMap<>();
//            serverCheckParamMap.put("serverId", serverId);
////            serverCheckParamMap.put("jdbcUrl", )
//            String rspString = HttpUtils.sendGet("http://172.16.13.28:8849/rpc/manager/server/test", serverCheckParamMap);
//            LogUtil.start("???????????????ID?????????????????????: " + rspString);
//            JSONObject obj = JSONObject.parseObject(rspString);
//            String code = obj.getString("code");
//            if ("200".equals(code)) {
//                JSONArray data = obj.getJSONArray("data");
//                if (Objects.nonNull(data)) {
//                    if (data.size() > 0) {
//                        LogUtil.error2Sentry(String.format("??????ID: %s, ????????????, ??????????????????!!!", serverId));
//                        System.exit(1);
//                    }
//                }
//            } else {
//                LogUtil.error(String.format("???????????????ID: %s ?????????????????????, ????????????!!!", serverId));
//                System.exit(1);
//            }
//        } catch (Exception e) {
//            LogUtil.error(String.format("??????ID :%s ????????????????????? ??????????????????!!!", serverId), e);
//            System.exit(1);
//        }
    }

    @Override
    public String getGameType() {
        return "game";
    }
}
