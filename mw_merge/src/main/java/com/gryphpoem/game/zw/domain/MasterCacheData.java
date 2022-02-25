package com.gryphpoem.game.zw.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;

/**
 * @ClassName MasterCacheData.java
 * @Description 主服数据数据数据
 * @author QiuKun
 * @date 2018年9月17日
 */
public class MasterCacheData {

	private final MasterServer masterServer;
	// 所有的玩家数据
	private Map<Long, Player> allPlayer;
	// 已经使用过的坐标
	private Set<Integer> usedPos = new HashSet<>();
	// 主服的公共数据
	private GameGlobal gameGlobal;
	// 阵营数据
	private Map<Integer, Camp> partyMap;
	// 全服的活动
	private List<GlobalActivity> globalActivitieList;
	// 主服的server_setting数据
	private final MasterServerSetting masterServerSetting;
	// 主服的活配置列表
	private List<ActivityBase> activityList;
	// 主服的活配置列表 key:actType
	private Map<Integer, ActivityBase> activityMap;

	public MasterCacheData(MasterServer masterServer, MasterServerSetting masterServerSetting) {
		this.masterServer = masterServer;
		this.masterServerSetting = masterServerSetting;
	}

	public void initAct() {
		// 加载主服的配置信息
		int serverId = masterServer.getServerId();
		LogUtil.debug("加载主服活动配置信息 serverId:" + serverId);
		this.activityList = StaticActivityDataMgr.getActivityBaseByServerIdAndMoldId(masterServer.getServerId(),
				masterServerSetting.getOpenTime(), masterServerSetting.getActMold());
		this.activityList.forEach(ab -> LogUtil.debug("活动配置信息 serverId:", serverId, ", actType:", ab.getActivityType(),
				", actId:", ab.getActivityId(), ", keyId:", ab.getPlan().getKeyId()));
		this.activityMap = this.activityList.stream()
				.collect(Collectors.toMap(ActivityBase::getActivityType, ab -> ab));

	}

	public MasterServer getMasterServer() {
		return masterServer;
	}

	public Map<Long, Player> getAllPlayer() {
		return allPlayer;
	}

	public void setAllPlayer(Map<Long, Player> allPlayer) {
		this.allPlayer = allPlayer;
	}

	public int getMasterServerId() {
		return masterServer.getServerId();
	}

	public Set<Integer> getUsedPos() {
		return usedPos;
	}

	public GameGlobal getGameGlobal() {
		return gameGlobal;
	}

	public void setGameGlobal(GameGlobal gameGlobal) {
		this.gameGlobal = gameGlobal;
	}

	public Map<Integer, Camp> getPartyMap() {
		return partyMap;
	}

	public void setPartyMap(Map<Integer, Camp> partyMap) {
		this.partyMap = partyMap;
	}

	public List<GlobalActivity> getGlobalActivitieList() {
		return globalActivitieList;
	}

	public void setGlobalActivitieList(List<GlobalActivity> globalActivitieList) {
		this.globalActivitieList = globalActivitieList;
	}

	public MasterServerSetting getMasterServerSetting() {
		return masterServerSetting;
	}

	public List<ActivityBase> getActivityList() {
		return activityList;
	}

	public Map<Integer, ActivityBase> getActivityMap() {
		return activityMap;
	}

}
