package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobal;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;

/**
 * @ClassName SaveGlobalServer.java
 * @Description 保存公用数据服务
 * @author TanDonghai
 * @date 创建时间：2017年3月23日 下午3:48:10
 *
 */
public class SaveGlobalServer extends SaveCommonServer<DbGlobal> {

	private static SaveGlobalServer ins = new SaveGlobalServer();

	private GlobalDataManager globalDataManager;

	private SaveGlobalServer() {
		super("SAVE_GLOBAL_SERVER", 1);
		this.globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
	}

	public static SaveGlobalServer getIns() {
		return ins;
	}

	@Override
	protected void saveOne(DbGlobal data) {
		globalDataManager.updateGlobal(data);
	}

	/**
	 * 保存数据
	 */
	@Override
	public void saveAll() {
		GameGlobal gameGlobal = globalDataManager.getGameGlobal();
		int saveCount = 0;
		if (gameGlobal != null) {
			try {
				saveData(gameGlobal.ser());
				saveCount = 1;
			} catch (Exception e) {
				LogUtil.error("保存Global数据出错", e);
			}

		}
		LogUtil.save(name + " ser data count:" + saveCount);
	}

	public void stopServer(){
		try {
			LogUtil.stop("开始保存全局数据...");
			long startMillis = System.currentTimeMillis();
			setLogFlag();
			saveAll();
			stop();
			while (!saveDone()) {
				LogUtil.stop(String.format("处理全局数据中, 等待3s, 已耗时=%s",System.currentTimeMillis()-startMillis));
				Thread.sleep(3000);
			}
			LogUtil.stop(String.format("保存全局数据完成, 共耗时=%s",System.currentTimeMillis()-startMillis));
		} catch (Exception e) {
			LogUtil.error("停服保存全局数据发生错误",e);
		}
	}
}
