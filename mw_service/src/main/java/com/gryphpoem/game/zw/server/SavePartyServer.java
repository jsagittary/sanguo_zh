package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SaveServer;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.server.thread.SavePartyThread;

import java.util.Map;

/**
 * @ClassName SavePartyServer.java
 * @Description 保存军团数据服务
 * @author TanDonghai
 * @date 创建时间：2017年4月26日 上午11:38:10
 *
 */
public class SavePartyServer extends SaveServer {
	private static SavePartyServer ins = new SavePartyServer();

	private SavePartyServer() {
		super("SAVE_PARTY_SERVER", 3);
	}

	public static SavePartyServer getIns() {
		return ins;
	}

	@Override
	public void saveData(Object object) {
		DbParty dbParty = (DbParty) object;
		SaveThread thread = threadPool.get((dbParty.getCamp() % threadNum));
		thread.add(object);
	}

	@Override
	public SaveThread createThread(String name) {
		return new SavePartyThread(name);
	}

	public void saveAllParty() {
		CampDataManager campDataManager = DataResource.ac.getBean(CampDataManager.class);
		Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
		int saveCount = 0;
		if (partyMap != null) {
			for (Camp camp : partyMap.values()) {
				try {
					saveData(camp.ser());
					saveCount++;
				} catch (Exception e) {
					LogUtil.error("保存Party数据出错", e);
				}
			}
		}

		LogUtil.save(name + " ser data count:" + saveCount);
	}

	public void stopServer() {
		try {
			LogUtil.stop("开始保存阵营数据...");
			long startMillis = System.currentTimeMillis();
			setLogFlag();
			saveAllParty();
			stop();
			while (!saveDone()) {
				LogUtil.stop(String.format("处理阵营数据中, 等待3s, 已耗时=%s",System.currentTimeMillis()-startMillis));
				Thread.sleep(3000);
			}
			LogUtil.stop(String.format("保存阵营数据完成, 共处理: %s, 共耗时=%s",allSaveCount(),System.currentTimeMillis()-startMillis));
		} catch (Exception e) {
			LogUtil.error("停服保存阵营数据发生错误",e);
		}
	}

}
