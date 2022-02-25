package com.gryphpoem.game.zw.core.thread;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.gryphpoem.game.zw.core.timer.ITimerEvent;
import com.gryphpoem.game.zw.core.timer.TimerEvent;

public class TimerThread extends Timer {
	// 事件集合
	private Vector<ITimerEvent> events = new Vector<ITimerEvent>();
	// 主线程
	private ServerThread main;
	// 定时任务
	private TimerTask task;

	public TimerThread(ServerThread main) {
		super("main" + "-Timer");
		this.main = main;
	}

	public void start() {
		task = new TimerTask() {
			@Override
			public void run() {
				synchronized (events) {
					// 事件迭代器
					Iterator<ITimerEvent> it = events.iterator();
					// 派发事件
					while (it.hasNext()) {
						TimerEvent event = (TimerEvent) it.next();
						if (event.remain() <= 0) {// 未结束
							if (event.getLoop() > 0) {
								event.setLoop(event.getLoop() - 1);
							} else {
								event.setLoop(event.getLoop());
							}
							// 需要放入主线程
							main.addCommand(event);
						}
						if (event.getLoop() == 0) {
							it.remove();
						}
					}
				}
			}
		};
		this.schedule(task, 0, main.getHeart());
	}

	public void stop(boolean flag) {
		synchronized (events) {
			events.clear();
			if (task != null)
				task.cancel();
			this.cancel();
		}
	}

	/**
	 * 添加定时事件
	 * 
	 * @param event
	 *            定时事件
	 */
	public void addTimerEvent(ITimerEvent event) {
		synchronized (events) {
			events.add(event);
		}
	}

	/**
	 * 移除定时事件
	 * 
	 * @param event
	 *            定时事件
	 */
	public void removeTimerEvent(ITimerEvent event) {
		synchronized (events) {
			events.remove(event);
		}
	}
}
