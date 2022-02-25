package com.gryphpoem.game.zw.core.thread;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class SendThread extends Thread {
	// 日志
	private Logger log = LogManager.getLogger(SendThread.class);

	// 运行标志
	protected boolean stop;

	protected boolean done;

	protected boolean logFlag = false;


	// 线程名称
	protected String threadName;

	protected SendThread(String threadName) {
		super(threadName);
		this.threadName = threadName;
	}

	abstract public void run();


	abstract public void add(int type, String body);

	public boolean workDone() {
		return done;
	}


	public void setLogFlag() {
		logFlag = true;
	}

	public void stop(boolean flag) {
		stop = flag;
		try {
			synchronized (this) {
				notify();
			}
		} catch (Exception e) {
			log.error(threadName + " Notify Exception", e);
		}
	}

	// abstract boolean workDone();
}
