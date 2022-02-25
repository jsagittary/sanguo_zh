package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Administrator
 *
 */
public class SaveActivityThread extends SaveThread {
	// 命令执行队列
	private LinkedBlockingQueue<Integer> activity_queue = new LinkedBlockingQueue<Integer>();

	private HashMap<Integer, GlobalActivity> acvitivty_map = new HashMap<Integer, GlobalActivity>();

	private ActivityDataManager activityDataManager;

	private static int MAX_SIZE = 10000;

	public SaveActivityThread(String threadName) {
		super(threadName);
		this.activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || activity_queue.size() > 0) {
			GlobalActivity activity = null;
			synchronized (this) {
				Integer activityId = activity_queue.poll();
				if (activityId != null) {
					activity = acvitivty_map.remove(activityId);
				}
			}
			if (activity == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogUtil.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}
			} else {
				if (activity_queue.size() > MAX_SIZE) {
					activity_queue.clear();
					acvitivty_map.clear();
				}

				try {
					activityDataManager.updateActivityData(activity);
					if (logFlag) {
						saveCount++;
						LogUtil.common("停服保存活动数据成功activityType=" + activity.getActivityType());
					}
				} catch (Exception e) {
					LogUtil.error("Activity Exception UPDATE SQL: " + activity.getActivityType(), e);
					LogUtil.warn("Activity save Exception");
					LogUtil.common("停服保存活动数据失败activityType=" + activity.getActivityType());
					this.add(activity);
				}
			}
		}

		done = true;
	}

	/**
	 * Overriding: add
	 *
	 * @param object
	 * @see
	 */
	@Override
	public void add(Object object) {
		try {
			GlobalActivity activity = (GlobalActivity) object;
			synchronized (this) {
				if (!acvitivty_map.containsKey(activity.getActivityType())) {
					this.activity_queue.add(activity.getActivityType());
				}
				this.acvitivty_map.put(activity.getActivityType(), activity);
				notify();
			}
		} catch (Exception e) {
			LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
