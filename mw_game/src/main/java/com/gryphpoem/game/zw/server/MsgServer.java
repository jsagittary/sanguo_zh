package com.gryphpoem.game.zw.server;

import java.util.HashMap;
import java.util.Iterator;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.Handler;
import com.gryphpoem.game.zw.core.thread.ServerThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.server.timer.SendMsgTimer;

public class MsgServer implements Runnable {

	private long createTime;
	private String serverName;
	private int heart;

	protected HashMap<Integer, ServerThread> threadPool = new HashMap<Integer, ServerThread>();

	private ThreadGroup threadGroup;

	public MsgServer(String serverName, int heart) {
		this.createTime = System.currentTimeMillis();
		this.serverName = serverName;
		this.heart = heart;

		threadGroup = new ThreadGroup(serverName);
		createServerThread(DealType.BUILD_QUE);

		init();
	}

	private void createServerThread(DealType dealType) {
		ServerThread serverThread = new ServerThread(threadGroup, dealType.getName(), heart);
		threadPool.put(dealType.getCode(), serverThread);
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
		// 定时发送推送消息
		threadPool.get(DealType.BUILD_QUE.getCode()).addTimerEvent(new SendMsgTimer());
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
			}catch (Exception e){
				if (e.getCause() instanceof MwException) {
					MwException mwe = (MwException) e.getCause();
					if (handler instanceof AbsClientHandler) { // 返回错误消息
						AbsClientHandler handler0 = (AbsClientHandler) handler;
						handler0.sendErrorMsgToPlayer(mwe.getCode());
					}
				}else{
					LogUtil.error(handler.getClass().getSimpleName() + " exception -->" + e.getMessage(), e);
				}
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
}
