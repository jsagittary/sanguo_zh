package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.AbsLogicServer;
import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.Handler;
import com.gryphpoem.game.zw.core.thread.ServerThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.server.timer.*;

import java.util.HashMap;
import java.util.Iterator;

public class LogicServer extends AbsLogicServer {
    private long createTime;
    private String serverName;
    private int heart;

    protected HashMap<Integer, ServerThread> threadPool = new HashMap<Integer, ServerThread>();

    private ThreadGroup threadGroup;

    public LogicServer(String serverName, int heart) {
        this.createTime = System.currentTimeMillis();
        this.serverName = serverName;
        this.heart = heart;

        threadGroup = new ThreadGroup(serverName);
        createServerThread(DealType.MAIN);
        createServerThread(DealType.BACKGROUND);
        createHttpServerThread();// 创建线程用于后台交互

        init();
    }

    private void createServerThread(DealType dealType) {
        ServerThread serverThread = new ServerThread(threadGroup, dealType.getName(), heart);
        threadPool.put(dealType.getCode(), serverThread);
    }

    private void createHttpServerThread() {
        ServerThread serverThread = new ServerThread(threadGroup, DealType.PUBLIC.getName(), -1);
        threadPool.put(DealType.PUBLIC.getCode(), serverThread);
    }

    private void init() {

    }

    public void stop() {
        Iterator<ServerThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().stop(true);
        }
    }

    public boolean isStopped() {
        Iterator<ServerThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            if (!it.next().stopped) {
                return false;
            }
        }

        return true;
    }

    public void run() {
        Iterator<ServerThread> it = threadPool.values().iterator();
        while (it.hasNext()) {
            it.next().start();
        }

        // 定时保存玩家数据
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SavePlayerTimer());

        // 公用数据保存
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SaveGlobalTimer());

        // 军团数据保存
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SavePartyTimer());

        // 定时完成建筑数据
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new BuildQueTimer());

        // 定时完成装备打造 不用推送
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new EquipQueTimer());

        // 定时增加领取资源次数
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new ResourceTimer());

        // 定时完成执行世界相关任务
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new WorldTimer());

        // 定时完成执行将领相关任务
//        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new HeroTimer());

        // 定时执行军团相关任务
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new PartyTimer());

        // 定时恢复体力
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RestoreTimer());

        // 月卡每日奖励
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new MonthCardRewardTimer());

        // 活动全局
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new SaveActivityTimer());

        // 删邮件
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new DelMailTimer());

        // 机器人相关逻辑
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new RobotTimer());

        // 服务器热更程序
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new HotfixTimer());

        // 定时移除过期的盖世太保
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new GestapoTimer());

        // 推送定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new PushMsgTimer());

        // 闪电战相关定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new LightningWarTimer());
        // 新地图的定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CrossMapTimer());
        //定时数据上报 修改数数上报使用的线程
        threadPool.get(DealType.BACKGROUND.getCode()).addTimerEvent(new SendEventDataTimer());
        // 服务端检查
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new CheckTimer());
        // 延时执行定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new DelayRunTimer());
        // 刷新经济订单定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new EconomicOrderTimer());
        // 模拟器延时定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new DelayLifeSimulatorTimer());
        // 经济作物收取定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new GainEconomicCropTimer());
        // 土匪过期清除定时器
        threadPool.get(DealType.MAIN.getCode()).addTimerEvent(new AutoDelBanditTimer());
    }

    public void addCommand(Handler handler) {
        // 寻找处理队列
        ServerThread thread = threadPool.get(handler.dealType().getCode());
        if (thread != null) {
            // 添加命令
            thread.addCommand(handler);
        } else {
            try {
                handler.action();
            } catch (MwException e) {
                LogUtil.error(e.getMessage(), e);

                if (handler instanceof AbsClientHandler) { // 返回错误消息
                    AbsClientHandler clientHandler = (AbsClientHandler) handler;
                    clientHandler.sendErrorMsgToPlayer(e.getCode());
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    public void addCommand(ICommand command, DealType dealType) {
        ServerThread thread = threadPool.get(dealType.getCode());
        if (thread != null) {
            // 添加命令
            thread.addCommand(command);
        } else {
            try {
                command.action();
            } catch (MwException e) {
                LogUtil.error(e.getMessage(), e);

                if (command instanceof AbsClientHandler) { // 返回错误消息
                    AbsClientHandler handler = (AbsClientHandler) command;
                    handler.sendErrorMsgToPlayer(e.getCode());
                }
            }catch (Exception e){
                if (e.getCause() instanceof MwException) {
                    MwException mwe = (MwException) e.getCause();
                    if (command instanceof AbsClientHandler) { // 返回错误消息
                        AbsClientHandler handler = (AbsClientHandler) command;
                        handler.sendErrorMsgToPlayer(mwe.getCode());
                    }
                }else{
                    LogUtil.error(command.getClass().getSimpleName() + " exception -->" + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void addCommandByType(ICommand command, DealType dealType) {
        ServerThread thread = threadPool.get(dealType.getCode());
        if (thread != null) {
            thread.addCommand(command);
        }
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heart) {
        this.heart = heart;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public void setThreadGroup(ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    @Override
    public Thread getThreadByDealType(DealType dealType) {
        return threadPool.get(dealType.getCode());
    }

}
