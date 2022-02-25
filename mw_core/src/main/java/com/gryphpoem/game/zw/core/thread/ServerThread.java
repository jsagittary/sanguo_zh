package com.gryphpoem.game.zw.core.thread;

import java.util.concurrent.LinkedBlockingQueue;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.timer.ITimerEvent;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.GameError;
import org.apache.dubbo.remoting.ExecutionException;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.TimeoutException;
import org.apache.dubbo.rpc.RpcException;

public class ServerThread extends Thread {
	// 日志
	// 命令执行队列
	private LinkedBlockingQueue<ICommand> command_queue = new LinkedBlockingQueue<ICommand>();
	// 计时线程
	private TimerThread timer;
	// 线程名称
	protected String threadName;
	// 心跳间隔
	protected int heart;

	// 运行标志
	private boolean stop;

	public boolean stopped = false;

	private boolean processingCompleted = false;

	public ServerThread(ThreadGroup group, String threadName, int heart) {
		super(group, threadName);
		this.threadName = threadName;
		this.heart = heart;
		if (this.heart > 0) {
			timer = new TimerThread(this);
		}

		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				LogUtil.error("threadName uncaughtException", e);
				if (timer != null)
					timer.stop(true);
				command_queue.clear();
			}
		});
	}

	public void run() {
		if (this.heart > 0 && timer != null) {
			// 启动计时线程
			timer.start();
		}

		stop = false;
		int loop = 0;
		while (!stop) {
			ICommand command = command_queue.poll();
			if (command == null) {
				try {
					synchronized (this) {
						loop = 0;
						processingCompleted = true;
						wait();
					}
				} catch (Exception e) {
					LogUtil.error("Thread Wait Exception", e);
				}
			} else {
				long start = System.currentTimeMillis();
				try {
					loop++;
					processingCompleted = false;
					command.action();
				} catch (MwException e) {
					LogUtil.error(e.getMessage(), e);

					if (command instanceof AbsClientHandler) { // 返回错误消息
						AbsClientHandler handler = (AbsClientHandler) command;
						handler.sendErrorMsgToPlayer(e.getCode());
					}
				}catch (RpcException e){
					LogUtil.error(e.getMessage(), e);
					if (command instanceof AbsClientHandler) { // 返回错误消息
						AbsClientHandler handler = (AbsClientHandler) command;
						handler.sendErrorMsgToPlayer(GameError.SERVER_CONNECT_FAIL.getCode());
					}
				}catch (Exception e) {
					if (e.getCause() instanceof MwException) {
						LogUtil.error(e.getMessage(), e);
						MwException mwe = (MwException) e.getCause();
						if (command instanceof AbsClientHandler) { // 返回错误消息
							AbsClientHandler handler = (AbsClientHandler) command;
							handler.sendErrorMsgToPlayer(mwe.getCode());
						}
					} else if (e.getCause() instanceof RemotingException) {
						LogUtil.error(e.getMessage(), e);
						GameError error;
						if (e.getCause() instanceof ExecutionException) {
							error = GameError.INVOKER_FAIL;
						} else if (e.getCause() instanceof TimeoutException) {
							error = GameError.INVOKER_TIMEOUT;
						} else {
							error = GameError.SERVER_CONNECT_EXCEPTION;
						}
						if (command instanceof AbsClientHandler) { // 返回错误消息
							AbsClientHandler handler = (AbsClientHandler) command;
							handler.sendErrorMsgToPlayer(error.getCode());
						}
					} else {
						LogUtil.error(command.getClass().getSimpleName() + " Not Hand  Exception -->" + e.getMessage(), e);
						if (command instanceof AbsClientHandler) { // 返回错误消息
							AbsClientHandler handler = (AbsClientHandler) command;
							handler.sendErrorMsgToPlayer(GameError.UNKNOWN_ERROR.getCode());
						}
					}
				}finally {
					long end = System.currentTimeMillis();

					if (end - start > 50) {
						LogUtil.error(this.getName() + "-->" + command.getClass().getSimpleName() + " haust:"
								+ (end - start));
					}
					if (loop > 1000) {
						loop = 0;
						try {
							Thread.sleep(1);
						} catch (Exception e) {
							LogUtil.error("Thread Sleep Exception", e);
						}
					}
				}
			}
		}

		stopped = true;
	}

	public void stop(boolean flag) {
		stop = flag;
		if (timer != null)
			this.timer.stop(flag);
		this.command_queue.clear();
		try {
			synchronized (this) {
				if (processingCompleted) {
					processingCompleted = false;
					notify();
				}
			}
		} catch (Exception e) {
			LogUtil.error("Server Thread " + threadName + " Notify Exception", e);
		}
	}

	/**
	 * 添加命令
	 *
	 * @param command 命令
	 */
	public void addCommand(ICommand command) {
		try {
			this.command_queue.add(command);
			synchronized (this) {
				notify();
			}
		} catch (Exception e) {
			LogUtil.error("Server Thread " + threadName + " Notify Exception", e);
		}
	}

	/**
	 * 添加定时事件
	 *
	 * @param event 定时事件
	 */
	public void addTimerEvent(ITimerEvent event) {
		if (timer != null)
			this.timer.addTimerEvent(event);
	}

	/**
	 * 移除定时事件
	 *
	 * @param event 定时事件
	 */
	public void removeTimerEvent(ITimerEvent event) {
		if (timer != null)
			this.timer.removeTimerEvent(event);
	}

	public String getThreadName() {
		return threadName;
	}

	public int getHeart() {
		return heart;
	}
}
