package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.constant.MergeUtils;
import com.gryphpoem.game.zw.domain.MasterCacheData;
import com.gryphpoem.game.zw.domain.MasterServer;
import com.gryphpoem.game.zw.domain.MasterServerSetting;

/**
 * @ClassName MasterServerWork.java
 * @Description
 * @author QiuKun
 * @date 2018年9月17日
 */
public class MasterServerWork extends BaseMergeWork {
	// 自己服务的id
	private final MasterServer masterServer;
	// 该服处理完成之后的数据保存
	private final MasterCacheData masterCacheData;

	public MasterServerWork(MasterServer masterServer) {
		this.masterServer = masterServer;
		MasterServerSetting setting = MergeServer.getIns().getmServerCfg().get(masterServer.getServerId());
		this.masterCacheData = new MasterCacheData(masterServer, setting);
		this.masterCacheData.initAct();
	}

	@Override
	protected String threadName() {
		return "mergeWork-processData-serverid:" + masterServer.getServerId();
	}

	@Override
	protected void work() throws Exception {
		int serverId = masterServer.getServerId();
		PlayerMergeService pMergeService = MergeServer.ac.getBean(PlayerMergeService.class);
		GlobalMergeService globalMergeService = MergeServer.ac.getBean(GlobalMergeService.class);
		// 加载公共数据
		MergeUtils.invokeCalcExecTime("加载公共数据 serverId:" + serverId,
				() -> globalMergeService.loadGlobal(masterCacheData));
		// 加载军团数据
		MergeUtils.invokeCalcExecTime("加载军团数据 serverId:" + serverId,
				() -> globalMergeService.loadPatry(masterCacheData));
		// 加载玩家数据
		MergeUtils.invokeCalcExecTime("加载玩家数据 serverId:" + serverId, () -> pMergeService.loadPlayer(masterCacheData));

		// 公共数据处理
		MergeUtils.invokeCalcExecTime("公共数据处理 serverId:" + serverId,
				() -> globalMergeService.globalDataProcess(masterCacheData));
		// 处理军团数据
		MergeUtils.invokeCalcExecTime("处理军团数据 serverId:" + serverId,
				() -> globalMergeService.partyDataProcess(masterCacheData));
		// 数据处理玩家开始
		MergeUtils.invokeCalcExecTime("数据处理玩家 serverId:" + serverId,
				() -> pMergeService.allPlayerDataproccess(masterCacheData));

		// 世界进程每阶段限时目标处理
		MergeUtils.invokeCalcExecTime("数据处理玩家 serverId:" + serverId,
				() -> globalMergeService.worldTaskScheduleGoalProcess(masterCacheData));

		// 数据保存
		MergeUtils.invokeCalcExecTime("数据保存 serverId:" + serverId, () -> globalMergeService.saveData(masterCacheData));
		// 玩家数据
		MergeUtils.invokeCalcExecTime("玩家数据 serverId:" + serverId, () -> pMergeService.saveAllPlayer(masterCacheData));

	}

	@Override
	int serverId() {
		return masterServer.getServerId();
	}

}
