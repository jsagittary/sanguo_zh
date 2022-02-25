/**
 * @Title: MainLogicServer.java
 * @Package com.game.server
 * @Description:
 * @author ZhangJun
 * @date 2015年7月29日 下午7:24:35    
 * @version V1.0
 */
package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SaveServer;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.thread.SaveActivityThread;

import java.util.Iterator;

public class SaveGlobalActivityServer extends SaveServer {
	private static SaveGlobalActivityServer ins = new SaveGlobalActivityServer();

	public SaveGlobalActivityServer() {
		super("SAVE_ACTIVITY_SERVER", 2);
	}
	public static SaveGlobalActivityServer getIns() {
		return ins;
	}

	public SaveThread createThread(String name) {
		return new SaveActivityThread(name);
	}

	public void saveData(Object object) {
		GlobalActivity servActivity = (GlobalActivity) object;
		SaveThread thread = threadPool.get((servActivity.getActivityType() % threadNum));
		thread.add(object);
	}

	private int beforeSaveCount;

	public void saveAllActivity() {
		ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);

		Iterator<GlobalActivityData> iterator = activityDataManager.getActivityMap().values().iterator();
		int now = TimeHelper.getCurrentSecond();
		while (iterator.hasNext()) {
			try {
				GlobalActivityData servActivityData = iterator.next();
				servActivityData.setLastSaveTime(now);
				saveData(servActivityData.copyData());
				beforeSaveCount ++;
			} catch (Exception e) {
				LogUtil.error("Save Activity Exception", e);
			}

		}
	}

	public void stopServer() {
		try {
			LogUtil.stop("开始保存全局活动数据...");
			long startMillis = System.currentTimeMillis();
			setLogFlag();
			saveAllActivity();
			stop();
			LogUtil.stop("预入库的全局活动数据数量: " + beforeSaveCount);
			while (!saveDone()) {
				LogUtil.stop(String.format("已入库的全局活动数量: %s, 等待3s, 已耗时=%s",allSaveCount(),System.currentTimeMillis()-startMillis));
				Thread.sleep(3000);
			}
			LogUtil.stop(String.format("保存全局活动数据完成,共处理: %s, 共耗时=%s",allSaveCount(),System.currentTimeMillis()-startMillis));
		} catch (Exception e) {
			LogUtil.error("停服保存全局活动数据发生错误",e);
		}
	}
}
