package com.gryphpoem.game.zw.core.thread;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class SaveThread extends Thread {
	// 日志
	private Logger log = LogManager.getLogger(SaveThread.class);

	// 运行标志
	protected boolean stop;

	protected volatile boolean done;

	protected boolean logFlag = false;

	protected volatile int saveCount = 0;

	// 线程名称
	protected String threadName;

	protected SaveThread(String threadName) {
		super(threadName);
		this.threadName = threadName;
	}

	abstract public void run();

	// abstract public void stop(boolean flag);

	abstract public void add(Object object);

	public void remove(Object obj) {}

	public boolean workDone() {
		return done;
	}

	public int getSaveCount() {
		return saveCount;
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
