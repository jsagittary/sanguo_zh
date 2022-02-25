package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SaveServer;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.Role;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.thread.SavePlayerThread;

import java.util.Iterator;

public class SavePlayerServer extends SaveServer {
	private static SavePlayerServer ins = new SavePlayerServer();

	private SavePlayerServer() {
		super("SAVE_PLAYER_SERVER", 10);
	}

	public static SavePlayerServer getIns() {
		return ins;
	}

	public SaveThread createThread(String name) {
		return new SavePlayerThread(name);
	}

	@Override
	public void saveData(Object object) {
		Role role = (Role) object;
		SaveThread thread = threadPool.get(Math.abs((int) (role.getRoleId() % threadNum)));
		if (thread == null) {
			LogUtil.error("can not find thread:" + role.getRoleId());
			thread = threadPool.get(0);
		}
		thread.add(object);
	}

	private int beforeSaveCount;

	public void saveAllPlayer() {
		PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);

		Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
		int now = TimeHelper.getCurrentSecond();
		int saveCount = 0;
		while (iterator.hasNext()) {
			Player player = iterator.next();
			try {
				saveCount++;
				player.lastSaveTime = now;

				player.tickOut();

				saveData(new Role(player));
				beforeSaveCount ++;
			} catch (Exception e) {
				LogUtil.error("Save player data Exception, lordId:" + player.roleId, e);
			}

		}

		LogUtil.save(name + " ser data count:" + saveCount);
	}
	public long stopMillis;
	public void stopServer() {
		try {
			LogUtil.stop("开始保存玩家数据...");
			long startMillis = System.currentTimeMillis();
			setLogFlag();
			saveAllPlayer();
			stop();
			LogUtil.stop("预入库的玩家数量: " + beforeSaveCount);
			while (!saveDone()) {
				LogUtil.stop(String.format("已入库的玩家数量: %s, 等待3s, 已耗时=%s",allSaveCount(),System.currentTimeMillis()-startMillis));
				Thread.sleep(3000);
			}
			stopMillis = System.currentTimeMillis()-startMillis;
			LogUtil.stop(String.format("保存玩家数据完成,共处理: %s, 共耗时=%s",allSaveCount(),stopMillis));
		} catch (Exception e) {
			LogUtil.error("停服保存玩家数据发生错误",e);
		}
	}
}
